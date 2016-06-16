import org.junit.Test;

public class myGradleTest {


    @Test
    public void test() {
        try {
            new Launcher().run(new String[]{"./src/test/resources/build.gradle"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
