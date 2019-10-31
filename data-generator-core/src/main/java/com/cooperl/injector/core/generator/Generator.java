package com.cooperl.injector.core.generator;

import com.cooperl.injector.core.config.InjectorConfig;
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

import static java.lang.reflect.Modifier.isStatic;

@Service
public class Generator {

    private EasyRandom easyRandom;

    private InjectorConfig injectorConfig;

    public Generator(InjectorConfig injectorConfig) {
        this.injectorConfig = injectorConfig;
        EasyRandomParameters parameters = new EasyRandomParameters()
                .collectionSizeRange(1, 3)
                .stringLengthRange(5, 15)
                .randomize(Double.class, new DoubleRangeRandomizer(1.0, 100.0))
                .randomize(Integer.class, new IntegerRangeRandomizer(0, 100))
                .overrideDefaultInitialization(true)
                .objectPoolSize(2)
                .randomizationDepth(5);
        easyRandom = new EasyRandom(parameters);
    }

    public Object generateObject(Class c) {
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
                                Object gene = this.generateObject(getSetterGenericParamClass(generatedObject, fieldName));
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

        /*for (Method generatedObjectMethod : methods) {
            if (generatedObjectMethod.getDeclaringClass().equals(generatedObject.getClass())
                    && generatedObjectMethod.getName().startsWith("get")) {

                String getterName = generatedObjectMethod.getName();
                String setterName = getterName.replace("get", "set");

                try {
                    Method setterMethod = generatedObject.getClass().getMethod(setterName, generatedObjectMethod.getReturnType());
                    // Si un tableau
                    if (setterMethod.getParameterTypes()[0] == List.class) {
                        ParameterizedType stringListType = (ParameterizedType) setterMethod.getGenericParameterTypes()[0];
                        Class<?> objectOfListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
                        List<Object> res = new ArrayList<>();
                        List listeUpdate = (List) generatedObjectMethod.invoke(update, (Object[]) null);
                        if (listeUpdate != null) {
                            for (Object up : listeUpdate) {
                                Object gene = this.generateObject(objectOfListClass);
                                res.add(merge(gene, up));
                            }
                            setterMethod.invoke(generatedObject, res);
                        }
                    } else {
                        Object value = generatedObjectMethod.invoke(update, (Object[]) null);
                        if (value != null) {
                            setterMethod.invoke(generatedObject, value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }*/
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
                return BeanGenerator.class.getClassLoader().loadClass(bean);
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

}
