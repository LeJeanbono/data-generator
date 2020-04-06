import com.cooperl.injector.core.config.InjectorConfig;
import com.cooperl.injector.core.generator.Generator;
import classes.TestClass;
import classes.TestClass2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.beans.BeanGenerator;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratorTest {

    @InjectMocks
    private Generator generator;

    @Mock
    private InjectorConfig injectorConfigMock;

    @Mock
    private BeanGenerator beanGeneratorMock;

    @Mock
    private ClassLoader classLoader;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void mergeSimpleClass() throws InvocationTargetException, IllegalAccessException {
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
    void mergeEcraseSimpleClass() throws InvocationTargetException, IllegalAccessException {
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
    void mergeEmptyListClass() throws InvocationTargetException, IllegalAccessException {
        TestClass c = new TestClass();
        List<String> listString = new ArrayList<>();
        listString.add("toto");
        c.setListString(listString);

        TestClass empty = new TestClass();

        TestClass result = (TestClass) generator.merge(c, empty);

        assertThat(result.getListString()).containsExactly("toto");
    }

    @Test
    void mergeListString() throws InvocationTargetException, IllegalAccessException {
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
    void mergeListObject() throws InvocationTargetException, IllegalAccessException {
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
    void mergeListObjectEcrase() throws InvocationTargetException, IllegalAccessException {
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
    void getClassOfRessource() throws ClassNotFoundException {
        List<String> classes = new ArrayList<>();
        classes.add("Other");
        classes.add("TestClass");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoader);
        doReturn(TestClass.class).when(classLoader).loadClass("TestClass");

        Class clazz = generator.getClassOfRessource("TestClass");

        assertThat(clazz).isEqualTo(TestClass.class);
    }

    @Test
    void generateObject() throws ClassNotFoundException, IntrospectionException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> body = new HashMap<>();

        List<String> classes = new ArrayList<>();
        classes.add("Other");
        classes.add("TestClass2");
        when(injectorConfigMock.getBeansClassName()).thenReturn(classes);
        when(beanGeneratorMock.getClassLoader()).thenReturn(classLoader);
        doReturn(TestClass2.class).when(classLoader).loadClass("TestClass2");

        Object o = generator.generateObject(body, "TestClass2");
        assertThat(o.getClass()).isEqualTo(TestClass2.class);
        TestClass2 testClass = (TestClass2) o;
        assertThat(testClass.getBibi()).isNotNull();
        assertThat(testClass.getToto()).isNotNull();
    }

}
