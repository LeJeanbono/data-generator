package com.cooperl.injector.core.generator;

import com.cooperl.injector.core.config.DataGeneratorConfig;
import com.cooperl.injector.core.config.InjectorConfig;
import com.cooperl.injector.core.exception.GeneratorException;
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

import static com.cooperl.injector.core.generator.SpecialValueEnum.EMPTY;
import static com.cooperl.injector.core.generator.SpecialValueEnum.NULL;
import static java.lang.reflect.Modifier.isStatic;
import static java.text.MessageFormat.format;

@Service
public class Generator {

    private EasyRandom easyRandom;

    private InjectorConfig injectorConfig;

    private BeanGenerator beanGenerator;

    private DataGeneratorConfig dataGeneratorConfig;

    private Gson gson;

    public Generator(InjectorConfig injectorConfig) {
        this.injectorConfig = injectorConfig;
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
        beanGenerator = new BeanGenerator();
        dataGeneratorConfig = new DataGeneratorConfig();
        gson = new Gson();
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
        for (String bean : injectorConfig.getBeansClassName()) {
            String[] split = bean.split("\\.");
            String className = split[split.length - 1];
            String capitalize = ressource.substring(0, 1).toUpperCase() + ressource.substring(1);
            String clazz = capitalize;
            if (dataGeneratorConfig.getPluralRessources()) {
                clazz = capitalize.substring(0, capitalize.length() - 1);
            }
            if (className.equals(clazz)) {
                return loadAndGetClass(bean);
            }
        }
        return null;
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
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, obj.getClass());
        } catch (IntrospectionException e) {
            throw new GeneratorException(format("Cannot get property {0},  for class {1}", fieldName, obj.getClass()), e);
        }
        return pd.getWriteMethod().getParameterTypes()[0];
    }

    private Class<?> getSetterGenericParamClass(Object obj, String fieldName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, obj.getClass());
        } catch (IntrospectionException e) {
            throw new GeneratorException(format("Cannot get property {0},  for class {1}", fieldName, obj.getClass()), e);
        }
        ParameterizedType stringListType = (ParameterizedType) pd.getWriteMethod().getGenericParameterTypes()[0];
        return (Class<?>) stringListType.getActualTypeArguments()[0];
    }

    private void callSetter(Object obj, String fieldName, Object value) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, obj.getClass());
        } catch (IntrospectionException e) {
            throw new GeneratorException(format("Cannot get property {0},  for class {1}", fieldName, obj.getClass()), e);
        }
        try {
            pd.getWriteMethod().invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GeneratorException(format("Cannot call setter {0},  for class {1}", pd.getWriteMethod().getName(), obj.getClass()), e);
        }
    }

    private Object callGetter(Object obj, String fieldName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, obj.getClass());
        } catch (IntrospectionException e) {
            throw new GeneratorException(format("Cannot get property {0},  for class {1}", fieldName, obj.getClass()), e);
        }
        try {
            return pd.getReadMethod().invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GeneratorException(format("Cannot call getter {0},  for class {1}", pd.getReadMethod().getName(), obj.getClass()), e);
        }
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
            } else if (shallowVal instanceof List) {
                List<?> list = (List<?>) callGetter(myPojo, val);
                List<?> listOfMap = ((List<?>) shallowVal);
                for (int i = 0; i < listOfMap.size(); i++) {
                    if (listOfMap.get(i) instanceof Map) {
                        applySpecialValue((Map<String, Object>) listOfMap.get(i), list.get(i));
                    }
                }
            }
        }
    }
}