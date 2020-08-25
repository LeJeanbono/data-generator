package com.github.lejeanbono.datagenerator.core.generator;

import com.github.lejeanbono.datagenerator.core.config.DataGeneratorConfig;
import com.github.lejeanbono.datagenerator.core.config.InjectorConfig;
import com.github.lejeanbono.datagenerator.core.exception.GeneratorException;
import com.github.lejeanbono.datagenerator.core.exception.RessourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.randomizers.range.DoubleRangeRandomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static com.github.lejeanbono.datagenerator.core.generator.SpecialValueEnum.EMPTY;
import static com.github.lejeanbono.datagenerator.core.generator.SpecialValueEnum.NULL;
import static java.lang.reflect.Modifier.isStatic;
import static java.text.MessageFormat.format;

@Service
public class Generator {

    private final EasyRandom easyRandom;

    private final InjectorConfig injectorConfig;

    private final BeanGenerator beanGenerator;

    private final DataGeneratorConfig dataGeneratorConfig;

    private final Gson gson;

    public Generator(
            InjectorConfig injectorConfig,
            BeanGenerator beanGenerator,
            DataGeneratorConfig dataGeneratorConfig
    ) {
        this.injectorConfig = injectorConfig;
        this.beanGenerator = beanGenerator;
        this.dataGeneratorConfig = dataGeneratorConfig;
        this.gson = new Gson();
        EasyRandomParameters parameters = new EasyRandomParameters()
                .collectionSizeRange(1, 3)
                .stringLengthRange(5, 15)
                .randomize(Double.class, new DoubleRangeRandomizer(1.0, 100.0))
                .randomize(Integer.class, new IntegerRangeRandomizer(0, 100))
                .overrideDefaultInitialization(true)
                .objectPoolSize(2000)
                .scanClasspathForConcreteTypes(true)
                .randomizationDepth(5);
        easyRandom = new EasyRandom(parameters);
    }

    public Object generateObject(Map<String, Object> body, String ressource) {
        Map<String, Object> shallowCopy = copy(body);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        Class<?> c = this.getClassOfRessource(ressource);
        // On traite le ##NULL##
        reinitSpecialValue(body);
        Object pojo = objectMapper.convertValue(body, c);
        Object myPojo = this.generateRandomObject(c);
        myPojo = this.merge(myPojo, pojo);
        // On traite le ##NULL##
        applySpecialValue(shallowCopy, myPojo);
        return myPojo;
    }

    public Object merge(Object generatedObject, Object update) {
        if (!generatedObject.getClass().isAssignableFrom(update.getClass())) {
            throw new GeneratorException(format("Bad class matching, {0} is not assignable to {1}", generatedObject.getClass(), update.getClass()));
        }
        if (ClassUtils.isPrimitiveOrWrapper(generatedObject.getClass()) || generatedObject.getClass() == String.class) {
            if (update != null) {
                return update;
            }
        }
        List<Field> fields = getAllFields(new LinkedList<>(), generatedObject.getClass());

        for (Field field : fields) {
            if (!isStatic(field.getModifiers())) {
                String fieldName = field.getName();
                Class<?> setterParamClass = getSetterParamClass(generatedObject, fieldName);
                // Si un tableau
                if (setterParamClass == List.class) {
                    List<Object> res = new ArrayList<>();
                    List<?> listeUpdate = (List<?>) callGetter(update, fieldName);
                    // Il faut merge dans les items du tableau
                    if (!CollectionUtils.isEmpty(listeUpdate)) {
                        for (Object up : listeUpdate) {
                            Object gene = this.generateRandomObject(getSetterGenericParamClass(generatedObject, fieldName));
                            res.add(merge(gene, up));
                        }
                        callSetter(generatedObject, fieldName, res);
                    }
                } else {
                    Object value = callGetter(update, fieldName);
                    if (value != null) {
                        callSetter(generatedObject, fieldName, value);
                    }
                }
            }
        }

        return generatedObject;
    }

    public List<Class<?>> getAllClassAnnotated() {
        List<Class<?>> result = new ArrayList<>();
        for (String bean : injectorConfig.getBeansClassName()) {
            result.add(loadAndGetClass(bean));
        }
        return result;
    }

    public Class<?> getClassOfRessource(String ressource) {
        String capitalize = ressource.substring(0, 1).toUpperCase() + ressource.substring(1);
        String clazz = capitalize;
        for (String bean : injectorConfig.getBeansClassName()) {
            String[] split = bean.split("\\.");
            String className = split[split.length - 1];
            if (dataGeneratorConfig.isPluralRessources()) {
                clazz = capitalize.substring(0, capitalize.length() - 1);
            }
            if (className.equals(clazz)) {
                return loadAndGetClass(bean);
            }
        }
        throw new RessourceNotFoundException(format("{0}.class does not exist, did you add @TestData ?", clazz));
    }

    private Map<String, Object> copy(Map<String, Object> original) {
        String jsonString = gson.toJson(original);
        return gson.fromJson(jsonString, Map.class);
    }

    private Object generateRandomObject(Class<?> c) {
        easyRandom.setSeed(new Random().nextLong());
        return easyRandom.nextObject(c);
    }

    private Class<?> loadAndGetClass(String bean) {
        try {
            return beanGenerator.getClassLoader().loadClass(bean);
        } catch (ClassNotFoundException e) {
            throw new GeneratorException(format("Cannot load class {0}", bean), e);
        }
    }

    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private Class<?> getSetterParamClass(Object obj, String fieldName) {
        PropertyDescriptor pd = getPropertyDescriptor(obj.getClass(), fieldName);
        return pd.getWriteMethod().getParameterTypes()[0];
    }

    private Class<?> getSetterGenericParamClass(Object obj, String fieldName) {
        PropertyDescriptor pd = getPropertyDescriptor(obj.getClass(), fieldName);
        ParameterizedType stringListType = (ParameterizedType) pd.getWriteMethod().getGenericParameterTypes()[0];
        return (Class<?>) stringListType.getActualTypeArguments()[0];
    }

    private void callSetter(Object obj, String fieldName, Object value) {
        PropertyDescriptor pd = getPropertyDescriptor(obj.getClass(), fieldName);
        try {
            pd.getWriteMethod().invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GeneratorException(format("Cannot call setter {0},  for class {1}", pd.getWriteMethod().getName(), obj.getClass()), e);
        }
    }

    private Object callGetter(Object obj, String fieldName) {
        PropertyDescriptor pd;
        pd = getPropertyDescriptor(obj.getClass(), fieldName);
        try {
            return pd.getReadMethod().invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GeneratorException(format("Cannot call getter {0},  for class {1}", pd.getReadMethod().getName(), obj.getClass()), e);
        }
    }

    private PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String fieldName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, clazz);
        } catch (IntrospectionException e) {
            throw new GeneratorException(format("Cannot get property {0},  for class {1}", fieldName, clazz), e);
        }
        return pd;
    }

    private void reinitSpecialValue(Map<String, Object> body) {
        for (String key : body.keySet()) {
            Object val = body.get(key);
            if (NULL.equals(SpecialValueEnum.get(val))) {
                body.put(key, null);
            } else if (EMPTY.equals(SpecialValueEnum.get(val))) {
                body.put(key, new ArrayList<>());
            } else if (val instanceof Map) {
                reinitSpecialValue((Map<String, Object>) val);
            } else if (val instanceof List) {
                for (Object o : ((List<?>) val)) {
                    if (o instanceof Map) {
                        reinitSpecialValue((Map<String, Object>) o);
                    }
                }
            }
        }
    }

    private void applySpecialValue(Map<String, Object> shallowCopy, Object myPojo) {
        for (String val : shallowCopy.keySet()) {
            Object shallowVal = shallowCopy.get(val);
            if (NULL.equals(SpecialValueEnum.get(shallowVal))) {
                callSetter(myPojo, val, null);
            } else if (EMPTY.equals(SpecialValueEnum.get(shallowVal))) {
                callSetter(myPojo, val, new ArrayList<>());
            } else if (shallowVal instanceof Map) {
                applySpecialValue((Map<String, Object>) shallowVal, callGetter(myPojo, val));
            } else if (shallowVal instanceof Collection) {
                Collection<?> list = (Collection<?>) callGetter(myPojo, val);
                Collection<?> listOfMap = ((Collection<?>) shallowVal);
                for (int i = 0; i < listOfMap.size(); i++) {
                    if (listOfMap.toArray()[i] instanceof Map) {
                        applySpecialValue((Map<String, Object>) listOfMap.toArray()[i], list.toArray()[i]);
                    }
                }
            }
        }
    }
}
