package dominus.intg.scripting;


import com.google.common.io.Files;
import dominus.framework.junit.DominusJUnit4TestBase;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.junit.Test;

import javax.script.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * EE: Integrating Groovy in a Java application
 * The Groovy language proposes several ways to integrate itself into applications (Java or even Groovy) at runtime, from the most basic, simple code execution to the most complete, integrating caching and compiler customization.
 * http://docs.groovy-lang.org/latest/html/documentation/index.html#_integrating_groovy_in_a_java_application
 * <p/>
 * EE: JSR223
 * Scripting for the Java Platform is a framework for embedding scripts into Java source code.
 */
public class GroovyIntgTest extends DominusJUnit4TestBase {

    ScriptEngine scriptEngine;
    String conditionExprScript;

    @Override
    protected void doSetUp() throws Exception {
        //TODO classloader
        scriptEngine = new ScriptEngineManager().getEngineByName("groovy");
        System.out.printf("EngineName: %s\n", scriptEngine.getFactory().getEngineName());
        System.out.printf("EngineVersion: %s\n", scriptEngine.getFactory().getEngineVersion());
        System.out.printf("Language Name: %s\n", scriptEngine.getFactory().getLanguageName());
        System.out.printf("LanguageVersion %s\n", scriptEngine.getFactory().getLanguageVersion());
        //EE: thread safe for groovy script engine
        System.out.printf("THREADING %s\n", scriptEngine.getFactory().getParameter("THREADING"));

        conditionExprScript = Files.toString(resourceLoader.getResource("classpath:script/groovy/condition_expr.groovy").getFile(), StandardCharsets.UTF_8);
        System.out.printf("ConditionExprScript: %s\n", conditionExprScript);
    }

    @Test
    public void testConditionalExpr() throws ScriptException {
        ScriptContext newContext = new SimpleScriptContext();
        newContext.setBindings(scriptEngine.createBindings(), ScriptContext.ENGINE_SCOPE);
        Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
        // set the variable to a different value in another scope
        engineScope.put("UICondition", false);
        // evaluate the same code but in a different script context (x = "world")
        assertEquals("show=false,enable=false", scriptEngine.eval(conditionExprScript, newContext));
    }

    @Test
    public void testEval() {
        HashMap bindingValues = new HashMap();
        GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), new Binding(bindingValues));
        Object result = shell.evaluate("33*3");
        String resultString = result != null ? result.toString() : "null";
        assertEquals("99", resultString);
    }
}
