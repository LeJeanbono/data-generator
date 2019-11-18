package classes;

import java.util.Objects;

public class TestClass2 {

    private String toto;

    private int bibi;

    public TestClass2() {

    }

    public String getToto() {
        return toto;
    }

    public void setToto(String toto) {
        this.toto = toto;
    }

    public int getBibi() {
        return bibi;
    }

    public void setBibi(int bibi) {
        this.bibi = bibi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass2 that = (TestClass2) o;
        return bibi == that.bibi &&
                Objects.equals(toto, that.toto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toto, bibi);
    }
}
