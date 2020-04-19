package classes;

import java.util.List;

public class TestClass {

    private String string;

    private Integer integer;

    private Double doublee;

    private List<String> listString;

    private List<TestClass2> listObject;

    public TestClass() {

    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public Double getDoublee() {
        return doublee;
    }

    public void setDoublee(Double doublee) {
        this.doublee = doublee;
    }

    public List<String> getListString() {
        return listString;
    }

    public void setListString(List<String> listString) {
        this.listString = listString;
    }

    public List<TestClass2> getListObject() {
        return listObject;
    }

    public void setListObject(List<TestClass2> listObject) {
        this.listObject = listObject;
    }
}
