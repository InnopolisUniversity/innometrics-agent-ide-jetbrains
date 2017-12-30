package ru.innopolis.university.innometrics.jb_plugin.components;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import ru.innopolis.university.innometrics.jb_plugin.models.Activity;
import ru.innopolis.university.innometrics.jb_plugin.models.Measurement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class InnometricsComponentTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        resetComponentState();
        super.tearDown();
    }

    private void resetComponentState() {
        ApplicationManager.getApplication().getComponent(InnometricsComponent.class)
                .loadState(new InnometricsComponent.State());
    }

    public void testComponentCreation() {
        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);
        assertNotNull(component);

        assertNotNull(component.getState());
        assertNotNull(component.getState().getActivities());
        assertEmpty(component.getState().getActivities());
    }

    public void testSessionActivity() {
        resetComponentState();

        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);

        assertNotNull(component.getState());
        assertEmpty(component.getState().getActivities());

        component.sessionActivity();

        List<Activity> activities = component.getState().getActivities();
        assertNotEmpty(activities);
        Activity activity = activities.get(0);

        /** activity example:
         * Activity{
         *     measurements=[
         *         Measurement{name='version name', value='IntelliJ IDEA', type='string'},
         *         Measurement{name='full version', value='2017.2.5', type='string'},
         *         Measurement{name='company name', value='JetBrains s.r.o.', type='string'},
         *         Measurement{name='start time', value='1514582870562', type='long'},
         *         Measurement{name='close time', value='1514582874736', type='long'},
         *         Measurement{name='idle time', value='0', type='long'}
         *     ]
         *     name='JetBrains IDE session time'
         * }
         */

        assertEquals("JetBrains IDE session time", activity.getName());
        assertContainsElements(activity.getMeasurements().stream().map(Measurement::getName).collect(Collectors.toList()),
                "version name", "full version", "company name", "start time", "close time", "idle time");
    }

    public void testLogActivity() {
        resetComponentState();

        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);

        assertNotNull(component.getState());
        assertEmpty(component.getState().getActivities());

        InnometricsComponent.State state = component.getState();

        long millis = System.currentTimeMillis();
        component.switchActivity("some|path", "/some/path/to/a/file.java", millis);

        assertEmpty(state.getActivities());
        component.loadState(state);

        assertNotEmpty(component.getState().getActivities());

        Activity activity = component.getState().getActivities().get(0);
        assertNotEmpty(activity.getMeasurements());

        /**
         * Activity{
         *     measurements=[
         *         Measurement{name='version name', value='IntelliJ IDEA', type='string'},
         *         Measurement{name='full version', value='2017.2.5', type='string'},
         *         Measurement{name='company name', value='JetBrains s.r.o.', type='string'},
         *         Measurement{name='file path', value='/some/path/to/a/file.java', type='string'},
         *         Measurement{name='code path', value='some|path', type='string'},
         *         Measurement{name='code begin time', value='1514584838172', type='long'},
         *         Measurement{name='code end time', value='1514584838174', type='long'}
         *     ],
         *     name='JetBrains IDE code location'
         * }
         */

        assertEquals("JetBrains IDE code location", activity.getName());
        assertContainsElements(activity.getMeasurements().stream().map(Measurement::getName).collect(Collectors.toList()),
                "version name", "full version", "company name", "file path", "code path", "code begin time", "code end time");

        Measurement filePath = activity.getMeasurements().stream().filter(m -> m.getName().equals("file path")).findFirst().orElse(new Measurement());
        assertEquals("/some/path/to/a/file.java", filePath.getValue());
        assertEquals("string", filePath.getType());

        Measurement codePath = activity.getMeasurements().stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("some|path", codePath.getValue());
        assertEquals("string", codePath.getType());
    }

    public void testClosingActivity() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        resetComponentState();

        InnometricsComponent component = ApplicationManager.getApplication().getComponent(InnometricsComponent.class);

        assertNotNull(component.getState());
        assertEmpty(component.getState().getActivities());

        InnometricsComponent.State state = component.getState();

        long millis = System.currentTimeMillis();
        component.switchActivity("some|path", "/some/path/to/a/file.java", millis);

        assertEmpty(state.getActivities());

        long finishTime = System.currentTimeMillis();

        Method method = component.getClass().getDeclaredMethod("closeActivity", long.class);
        method.setAccessible(true);
        method.invoke(component, finishTime);

        assertNotEmpty(state.getActivities());
        List<Measurement> measurements = state.getActivities().get(0).getMeasurements();

        Measurement filePath = measurements.stream().filter(m -> m.getName().equals("file path")).findFirst().orElse(new Measurement());
        assertEquals("/some/path/to/a/file.java", filePath.getValue());
        assertEquals("string", filePath.getType());

        Measurement codePath = measurements.stream().filter(m -> m.getName().equals("code path")).findFirst().orElse(new Measurement());
        assertEquals("some|path", codePath.getValue());
        assertEquals("string", codePath.getType());

        Measurement codeBegin = measurements.stream().filter(m -> m.getName().equals("code begin time")).findFirst().orElse(new Measurement());
        assertEquals(String.valueOf(millis), codeBegin.getValue());
        assertEquals("long", codeBegin.getType());

        Measurement codeEnd = measurements.stream().filter(m -> m.getName().equals("code end time")).findFirst().orElse(new Measurement());
        assertEquals(String.valueOf(finishTime), codeEnd.getValue());
        assertEquals("long", codeEnd.getType());
    }


}