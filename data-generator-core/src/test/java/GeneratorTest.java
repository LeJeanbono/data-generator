import classes.TestClass;
import classes.TestClass2;
import com.cooperl.injector.core.config.DataGeneratorConfig;
import com.cooperl.injector.core.config.InjectorConfig;
import com.cooperl.injector.core.exception.GeneratorException;
import com.cooperl.injector.core.exception.RessourceNotFoundException;
import com.cooperl.injector.core.generator.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.beans.BeanGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneratorTest {

    @InjectMocks
    private Generator generator;

    @Mock
    private InjectorConfig injectorConfigMock;

    @Mock
    private BeanGenerator beanGeneratorMock;

    @Mock
    private ClassLoader classLoaderMock;

    @Mock
    private DataGeneratorConfig dataGeneratorConfig;

    @BeforeEach
    void init() {
        this.generator = null;
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void mergeSimpleClass() {
        TestClass c = new TestClass();
        c.setDoublee(3.3);
        c.setInteger(3);
        c.setString("azerty");
        List<String> listString = new ArrayList<>();
        listString.add("s1");
        listString.add("s2");
        c.setListString(listString);
        List<TestClass2> listObject = new ArrayList<>();
        TestClass2 tc2 = new TestClass2();
        tc2.setBibi(123);
        tc2.setToto("qsdf");
        listObject.add(tc2);
        c.setListObject(listObject);
        TestClass empty = new TestClass();

        TestClass result = (TestClass) generator.merge(empty, c);

        assertThat(result.getDoublee()).isEqualTo(3.3);
        assertThat(result.getInteger()).isEqualTo(3);
        assertThat(result.getString()).isEqualTo("azerty");
        assertThat(result.getListString()).containsExactly("s1", "s2");
        assertThat(result.getListObject()).containsExactly(tc2);
    }

    @Test
    void mergeEcraseSimpleClass() {
        TestClass c = new TestClass();
        c.setDoublee(3.3);
        c.setInteger(3);
        c.setString("azerty");
        List<String> listString = new ArrayList<>();
        listString.add("s1");
        listString.add("s2");
        c.setListString(listString);
        List<TestClass2> listObject = new ArrayList<>();
        TestClass2 tc2 = new TestClass2();
        tc2.setBibi(123);
        tc2.setToto("qsdf");
        listObject.add(tc2);
        c.setListObject(listObject);
        TestClass c2 = new TestClass();
        c2.setDoublee(4.4);
        c2.setInteger(4);
        c2.setString("iop");

        TestClass result = (TestClass) generator.merge(c2, c);

        assertThat(result.getDoublee()).isEqualTo(3.3);
        assertThat(result.getInteger()).isEqualTo(3);
        assertThat(result.getString()).isEqualTo("azerty");
        assertThat(result.getListString()).containsExactly("s1", "s2");
        assertThat(result.getListObject()).containsExactly(tc2);
    }


    @Test
    void mergeEmptyListClass() {
        TestClass c = new TestClass();
        List<String> listString = new ArrayList<>();
        listString.add("toto");
        c.setListString(listString);

        TestClass empty = new TestClass();

        TestClass result = (TestClass) generator.merge(c, empty);

        assertThat(result.getListString()).containsExactly("toto");
    }

    @Test
    void mergeListString() {
        TestClass c = new TestClass();
        List<String> listString = new ArrayList<>();
        listString.add("toto");
        c.setListString(listString);

        TestClass c2 = new TestClass();
        List<String> listString2 = new ArrayList<>();
        listString2.add("titi");
        c2.setListString(listString2);

        TestClass result = (TestClass) generator.merge(c, c2);

        assertThat(result.getListString()).containsExactly("titi");
    }

    @Test
    void mergeListObject() {
        TestClass c = new TestClass();
        List<TestClass2> listObject = new ArrayList<>();
        TestClass2 tc2 = new TestClass2();
        tc2.setBibi(123);
        tc2.setToto("qsdf");
        listObject.add(tc2);
        c.setListObject(listObject);

        TestClass empty = new TestClass();

        TestClass result = (TestClass) generator.merge(c, empty);

        assertThat(result.getListObject()).containsExactly(tc2);
    }

    @Test
    void mergeListObjectEcrase() {
        TestClass c = new TestClass();
        List<TestClass2> listObject = new ArrayList<>();
        TestClass2 tc2 = new TestClass2();
        tc2.setBibi(123);
        tc2.setToto("qsdf");
        listObject.add(tc2);
        c.setListObject(listObject);

        TestClass empty = new TestClass();
        List<TestClass2> listObject2 = new ArrayList<>();
        TestClass2 tc22 = new TestClass2();
        tc22.setBibi(1234);
        tc22.setToto("qsdf2");
        listObject2.add(tc22);
        empty.setListObject(listObject2);

        TestClass result = (TestClass) generator.merge(c, empty);

        assertThat(result.getListObject()).containsExactly(tc22);
    }

    @Test
    void mergeBadIncompatibleClass() {
        String string = "A String";
        Integer number = 6;
        assertThatThrownBy(() -> generator.merge(string, number))
                .isInstanceOf(GeneratorException.class)
                .hasMessageContaining("Bad class matching, class java.lang.String is not assignable to class java.lang.Integer");
    }

    @Test
    void getAllClassAnnotated() throws ClassNotFoundException {
        List<String> classes = new ArrayList<>();
        classes.add("classes.TestClass2");
        classes.add("classes.TestClass");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        doReturn(TestClass.class).when(classLoaderMock).loadClass("classes.TestClass");
        doReturn(TestClass2.class).when(classLoaderMock).loadClass("classes.TestClass2");

        List<Class<?>> classs = generator.getAllClassAnnotated();

        assertThat(classs).hasSize(2).containsOnly(TestClass.class, TestClass2.class);
    }

    @Test
    void getAllClassAnnotatedClassLoadException() throws ClassNotFoundException {
        List<String> classes = new ArrayList<>();
        classes.add("classes.TestClass");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        doThrow(ClassNotFoundException.class).when(classLoaderMock).loadClass("classes.TestClass");

        assertThatThrownBy(() -> generator.getAllClassAnnotated())
                .isInstanceOf(GeneratorException.class)
                .hasMessageContaining("Cannot load class classes.TestClass");
    }

    @Test
    void getClassOfRessource() throws ClassNotFoundException {
        List<String> classes = new ArrayList<>();
        classes.add("classes.Other");
        classes.add("classes.TestClass");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        when(dataGeneratorConfig.getPluralRessources()).thenReturn(false);
        doReturn(TestClass.class).when(classLoaderMock).loadClass("classes.TestClass");

        Class<?> clazz = generator.getClassOfRessource("TestClass");

        assertThat(clazz).isEqualTo(TestClass.class);
    }

    @Test
    void getClassOfRessourceNotFound() {
        List<String> classes = new ArrayList<>();
        classes.add("classes.Other");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);

        assertThatThrownBy(() -> generator.getClassOfRessource("TestClass"))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessageContaining("TestClass.class does not exist, did you add @TestData ?");
    }

    @Test
    void getClassOfRessourcePlural() throws ClassNotFoundException {
        List<String> classes = new ArrayList<>();
        classes.add("classes.Other");
        classes.add("classes.TestClass");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        when(dataGeneratorConfig.getPluralRessources()).thenReturn(true);
        doReturn(TestClass.class).when(classLoaderMock).loadClass("classes.TestClass");

        Class<?> clazz = generator.getClassOfRessource("TestClasss");

        assertThat(clazz).isEqualTo(TestClass.class);
    }

    @Test
    void generateObject() throws ClassNotFoundException {
        HashMap<String, Object> body = new HashMap<>();
        List<String> classes = new ArrayList<>();
        classes.add("classes.Other");
        classes.add("classes.TestClass2");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        when(dataGeneratorConfig.getPluralRessources()).thenReturn(false);
        doReturn(TestClass2.class).when(classLoaderMock).loadClass("classes.TestClass2");

        Object o = generator.generateObject(body, "TestClass2");

        assertThat(o.getClass()).isEqualTo(TestClass2.class);
        TestClass2 testClass = (TestClass2) o;
        assertThat(testClass.getBibi()).isNotNull();
        assertThat(testClass.getToto()).isNotNull();
    }

    @Test
    void generateObjectNullAttribute() throws ClassNotFoundException {
        HashMap<String, Object> body = new HashMap<>();
        body.put("toto", "##NULL##");
        List<String> classes = new ArrayList<>();
        classes.add("classes.TestClass2");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        when(dataGeneratorConfig.getPluralRessources()).thenReturn(false);
        doReturn(TestClass2.class).when(classLoaderMock).loadClass("classes.TestClass2");

        Object o = generator.generateObject(body, "TestClass2");

        assertThat(o.getClass()).isEqualTo(TestClass2.class);
        TestClass2 testClass = (TestClass2) o;
        assertThat(testClass.getBibi()).isNotNull();
        assertThat(testClass.getToto()).isNull();
    }

    @Test
    void generateObjectEmptyAttribute() throws ClassNotFoundException {
        HashMap<String, Object> body = new HashMap<>();
        body.put("listString", "##EMPTY##");
        List<String> classes = new ArrayList<>();
        classes.add("classes.TestClass");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        when(dataGeneratorConfig.getPluralRessources()).thenReturn(false);
        doReturn(TestClass.class).when(classLoaderMock).loadClass("classes.TestClass");

        Object o = generator.generateObject(body, "TestClass");

        assertThat(o.getClass()).isEqualTo(TestClass.class);
        TestClass testClass = (TestClass) o;
        assertThat(testClass.getDoublee()).isNotNull();
        assertThat(testClass.getListString()).isEmpty();
        assertThat(testClass.getListObject()).isNotEmpty();
    }

    @Test
    void d() throws ClassNotFoundException {
        HashMap<String, Object> body = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        object.put("toto", "##NULL##");
        List<Object> listObject = new ArrayList<>();
        listObject.add(object);
        body.put("listObject", listObject);
        List<String> classes = new ArrayList<>();
        classes.add("classes.TestClass");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoaderMock);
        when(dataGeneratorConfig.getPluralRessources()).thenReturn(false);
        doReturn(TestClass.class).when(classLoaderMock).loadClass("classes.TestClass");

        Object o = generator.generateObject(body, "TestClass");

        assertThat(o.getClass()).isEqualTo(TestClass.class);
        TestClass testClass = (TestClass) o;
        assertThat(testClass.getListObject().get(0).getToto()).isNull();
    }

}
