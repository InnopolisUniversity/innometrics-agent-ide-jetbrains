package ru.innopolis.university.innometrics.jb_plugin.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;
import ru.innopolis.university.innometrics.jb_plugin.models.Activity;
import ru.innopolis.university.innometrics.jb_plugin.models.Measurement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ActivitiesSenderServiceTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void tearDown() throws Exception {
        ApplicationManager.getApplication().getComponent(InnometricsComponent.class).getState().setActivities(new ArrayList<>());
        super.tearDown();
    }

    public void testAuthRequestJson() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ActivitiesSenderService service = ServiceManager.getService(ActivitiesSenderService.class);

        assertNotNull(service);

        Method method = service.getClass().getDeclaredMethod("authRequestJsonBody", String.class, String.class);
        method.setAccessible(true);

        Object result = method.invoke(service, "test_user", "test_password");
        assertOneOf(result.toString(), "{\"password\":\"test_password\",\"username\":\"test_user\"}", "{\"username\":\"test_user\",\"password\":\"test_password\"}");
    }

    public void testMeasurementsRequestJson() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ActivitiesSenderService service = ServiceManager.getService(ActivitiesSenderService.class);


        ArrayList<Activity> activities = new ArrayList<>();
        Activity a1 = new Activity();
        a1.setName("test activity one");
        a1.addMeasurement(new Measurement("m1_1", "value1_1", "type1_1"));
        a1.addMeasurement(new Measurement("m1_2", "value1_2", "type1_2"));

        Activity a2 = new Activity();
        a2.setName("test activity two");
        a2.addMeasurement(new Measurement("m2_1", "value2_1", "type2_1"));
        a2.addMeasurement(new Measurement("m2_2", "value2_2", "type2_2"));

        activities.add(a1);
        activities.add(a2);
        ApplicationManager.getApplication().getComponent(InnometricsComponent.class).getState().setActivities(activities);

        assertNotNull(service);

        Method method = service.getClass().getDeclaredMethod("measurementsRequestJsonBody");
        method.setAccessible(true);

        Object result = method.invoke(service);
        assertEquals("{\"activities\":[" +
                "{" +
                    "\"measurements\":[" +
                        "{\"name\":\"m1_1\",\"value\":\"value1_1\",\"type\":\"type1_1\"}," +
                        "{\"name\":\"m1_2\",\"value\":\"value1_2\",\"type\":\"type1_2\"}" +
                    "]," +
                    "\"name\":\"test activity one\"" +
                "}," +
                "{" +
                    "\"measurements\":[" +
                        "{\"name\":\"m2_1\",\"value\":\"value2_1\",\"type\":\"type2_1\"}," +
                        "{\"name\":\"m2_2\",\"value\":\"value2_2\",\"type\":\"type2_2\"}" +
                    "]," +
                    "\"name\":\"test activity two\"" +
                "}]}", result.toString());
    }

}