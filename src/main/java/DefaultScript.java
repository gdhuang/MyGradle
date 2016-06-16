import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.Script;

//org.gradle.groovy.scripts.DefaultScript
public abstract class DefaultScript extends Script {
    Project project = new Project();


    public void apply(Map options) {

        options.forEach((key,value) ->
                                System.out.printf("delegate apply %s:%s to %s%n", key, value, project));
    }

    public void task(String name, Closure closure) {
        System.out.printf("delegate task %s:%s to %s%n", name, closure, project);
    }

}
