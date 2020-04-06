package com.cooperl.injector.core.generator;

import com.cooperl.injector.core.config.InjectorConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

@Service
public class Generator {

    private EasyRandom easyRandom;

    private InjectorConfig injectorConfig;

    private BeanGenerator beanGenerator;

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
    }

    public Object generateObject(Map<String, Object> body, String ressource) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IntrospectionException {
        HashMap<String, Object> shallowCopy = new HashMap<>();
        shallowCopy.putAll(body);
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

    private Object generateRandomObject(Class<?> c) {
        easyRandom.setSeed(new Random().nextLong());
        return easyRandom.nextObject(c);
    }

    public Object merge(Object generatedObject, Object update) throws InvocationTargetException, IllegalAccessException {
        if (!generatedObject.getClass().isAssignableFrom(update.getClass())) {
            throw new InternalError("Bad class matching");
        }
        if (ClassUtils.isPrimitiveOrWrapper(generatedObject.getClass()) || generatedObject.getClass() == String.class) {
            if (update != null) {
                return update;
            }
        }
        // Method[] methods = generatedObject.getClass().getMethods();
        List<Field> fields = getAllFields(new LinkedList<>(), generatedObject.getClass());

        for (Field field : fields) {
            if (!isStatic(field.getModifiers())) {
                String fieldName = field.getName();
                try {
                    Class setterParamClass = getSetterParamClass(generatedObject, fieldName);
                    // Si un tableau
                    if (setterParamClass == List.class) {
                        List<Object> res = new ArrayList<>();
                        List listeUpdate = (List) callGetter(update, fieldName);
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
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                }
            }
        }

        return generatedObject;
    }

    public List<Class> getAllClassAnnotated() throws ClassNotFoundException {
        List<Class> result = new ArrayList<>();
        for (String bean : injectorConfig.getBeansClassName()) {
            result.add(BeanGenerator.class.getClassLoader().loadClass(bean));
        }
        return result;
    }

    public Class getClassOfRessource(String ressource) throws ClassNotFoundException {
        for (String bean : injectorConfig.getBeansClassName()) {
            String capitalize = ressource.substring(0, 1).toUpperCase() + ressource.substring(1);
            String clazz = capitalize.substring(0, capitalize.length() - 1);
            if (bean.contains(clazz)) {
                return beanGenerator.getClassLoader().loadClass(bean);
                //return BeanGenerator.class.getClassLoader().loadClass(bean);
            }
        }
        return null;
    }

    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private Class getSetterParamClass(Object obj, String fieldName) throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(fieldName, obj.getClass());
        return pd.getWriteMethod().getParameterTypes()[0];
    }

    private Class getSetterGenericParamClass(Object obj, String fieldName) throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(fieldName, obj.getClass());
        ParameterizedType stringListType = (ParameterizedType) pd.getWriteMethod().getGenericParameterTypes()[0];
        return (Class<?>) stringListType.getActualTypeArguments()[0];
    }

    private void callSetter(Object obj, String fieldName, Object value) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        PropertyDescriptor pd = new PropertyDescriptor(fieldName, obj.getClass());
        pd.getWriteMethod().invoke(obj, value);
    }

    private Object callGetter(Object obj, String fieldName) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        PropertyDescriptor pd = new PropertyDescriptor(fieldName, obj.getClass());
        return pd.getReadMethod().invoke(obj);
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
                for (Object o : ((List) val)) {
                    if (o instanceof Map) {
                        reinitSpecialValue((Map<String, Object>) o);
                    }
                }
            }
        }
    }

    private void applySpecialValue(Map<String, Object> shallowCopy, Object myPojo) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        for (String val : shallowCopy.keySet()) {
            Object shallowVal = shallowCopy.get(val);
            if (NULL.equals(SpecialValueEnum.get(shallowVal))) {
                callSetter(myPojo, val, null);
            } else if (EMPTY.equals(SpecialValueEnum.get(shallowVal))) {
                callSetter(myPojo, val, new ArrayList<>());
            } else if (shallowVal instanceof Map) {
                applySpecialValue((Map<String, Object>) shallowVal, callGetter(myPojo, val));
            } else if (shallowVal instanceof List) {
                List list = (List) callGetter(myPojo, val);
                List listOfMap = ((List) shallowVal);
                for (int i = 0; i < listOfMap.size(); i++) {
                    if (listOfMap.get(i) instanceof Map) {
                        applySpecialValue((Map<String, Object>) listOfMap.get(i), list.get(i));
                    }
                }
            }
        }
    }


}
