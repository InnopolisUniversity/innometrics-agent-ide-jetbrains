package ru.innopolis.university.innometrics.jb_plugin.handlers;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;
import ru.innopolis.university.innometrics.jb_plugin.models.Measurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class CodePathCaretListenerTest extends LightCodeInsightFixtureTestCase {

    private String testDataPath;

    @Override
    protected void setUp() throws Exception {
        Properties pluginProperties = new Properties();
        try {
            pluginProperties.load(getClass().getClassLoader().getResourceAsStream("plugin.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.testDataPath = pluginProperties.getProperty("innometrics.jb_plugin.testData", "./testData");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        resetComponentState();
        super.tearDown();
    }

    @Override
    protected String getTestDataPath() {
//        return "../Innometrics-JB-plugin/resourcesTest/testData";
        return this.testDataPath;
    }

    private void resetComponentState() {
        ApplicationManager.getApplication().getComponent(InnometricsComponent.class).getState().setActivities(new ArrayList<>());
    }

    public void testSimpleClass() {
        resetComponentState();
        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);
        myFixture.configureByFile("SimpleClass.java");
        Editor editor = myFixture.getEditor();

        // inside 'main' method
        editor.getCaretModel().moveToOffset(85);

        Measurement codePath = component.getState().getActivities().get(0)
                .getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("PROJ:light_temp|LANG:Java|CLASS:SimpleClass|FUNC:main|LINE:3", codePath.getValue());
        assertEquals("string", codePath.getType());


        // inside 'SimpleClass' class, but outside 'main' method
        editor.getCaretModel().moveToOffset(28);

        codePath = component.getState().getActivities().get(1)
                .getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("PROJ:light_temp|LANG:Java|CLASS:SimpleClass|LINE:2", codePath.getValue());
        assertEquals("string", codePath.getType());


        // inside 'secondMethod' method
        editor.getCaretModel().moveToOffset(180);

        codePath = component.getState().getActivities().get(2)
                .getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("PROJ:light_temp|LANG:Java|CLASS:SimpleClass|FUNC:secondMethod|LINE:7", codePath.getValue());
        assertEquals("string", codePath.getType());
    }

    public void testPackageClass() {
        resetComponentState();
        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);
        myFixture.configureByFile("ru/innopolis/university/test/PackageClass.java");
        Editor editor = myFixture.getEditor();

        // inside 'main' method
        editor.getCaretModel().moveToOffset(160);

        Measurement codePath = component.getState().getActivities().get(0)
                .getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("PROJ:light_temp|LANG:Java|NS:ru.innopolis.university.test|CLASS:PackageClass|FUNC:main|LINE:7", codePath.getValue());
        assertEquals("string", codePath.getType());
    }

    public void testWithInnerClasses() {
        resetComponentState();
        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);
        myFixture.configureByFile("ru/innopolis/university/test/WithInnerClasses.java");
        Editor editor = myFixture.getEditor();

        // inside 'method' method in 'Inner' class
        editor.getCaretModel().moveToOffset(140);

        Measurement codePath = component.getState().getActivities().get(0)
                .getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("PROJ:light_temp|LANG:Java|NS:ru.innopolis.university.test|CLASS:WithInnerClasses|CLASS:Inner|FUNC:method|LINE:6", codePath.getValue());
        assertEquals("string", codePath.getType());


        // inside anonymous class in 'Inner' class
        editor.getCaretModel().moveToOffset(220);

        codePath = component.getState().getActivities().get(1)
                .getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("PROJ:light_temp|LANG:Java|NS:ru.innopolis.university.test|CLASS:WithInnerClasses|CLASS:Inner|FUNC:method|CLASS:[ANONYMOUS]|FUNC:methodInAnonClass|LINE:8", codePath.getValue());
        assertEquals("string", codePath.getType());
    }

    public void testWithLambdaExpr() {
        resetComponentState();
        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);
        myFixture.configureByFile("ru/innopolis/university/test/WithLambdaExpr.java");
        Editor editor = myFixture.getEditor();

        // inside lambda expression
        editor.getCaretModel().moveToOffset(180);

        Measurement codePath = component.getState().getActivities().get(0)
                .getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("PROJ:light_temp|LANG:Java|NS:ru.innopolis.university.test|CLASS:WithLambdaExpr|FUNC:main|FUNC:[LAMBDA]|LINE:7", codePath.getValue());
        assertEquals("string", codePath.getType());
    }

    public void testSimpleTextFile() {
        resetComponentState();
        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);
        myFixture.configureByFile("SimpleTextFile.txt");
        Editor editor = myFixture.getEditor();

        // inside simple text file
        editor.getCaretModel().moveToOffset(180);

        assertEmpty(component.getState().getActivities());
    }
}