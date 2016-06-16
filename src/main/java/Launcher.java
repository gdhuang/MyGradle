import java.io.File;
import java.security.CodeSource;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

public class Launcher {

    //org.gradle.groovy.scripts.internal.DefaultScriptCompilationHandler
    private Script createScript(File buildScriptFiles) throws Exception {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setSourceEncoding("UTF-8");
        compilerConfiguration.setScriptBaseClass(DefaultScript.class.getName());
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader(), compilerConfiguration) {
            @Override
            protected CompilationUnit createCompilationUnit(CompilerConfiguration compilerConfiguration,
                                                            CodeSource codeSource) {

                CompilationUnit compilationUnit = new CompilationUnit(compilerConfiguration, codeSource, null, this);
                compilationUnit.addPhaseOperation(new TaskTransformer(), Phases.CANONICALIZATION);
                return compilationUnit;
            }
        };
        Class aClass = gcl.parseClass(buildScriptFiles);
        final Script script = (Script) aClass.newInstance();
        return script;
    }


    public void run(String args[]) throws Exception {

        File buildScriptFile = new File(args[0]);
        Script buildScript = this.createScript(buildScriptFile);

        buildScript.run();
    }
}
