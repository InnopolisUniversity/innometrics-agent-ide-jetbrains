package ru.innopolis.university.innometrics.jb_plugin.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;
import ru.innopolis.university.innometrics.jb_plugin.models.Activity;
import ru.innopolis.university.innometrics.jb_plugin.models.Measurement;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    public void testMeasurementsRequestJson() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        ActivitiesSenderService service = ServiceManager.getService(ActivitiesSenderService.class);

        Field field = ActivitiesSenderService.class.getDeclaredField("BATCH_SIZE");
        field.setAccessible(true);
        int batchSize = (int) field.get(null);
        ArrayList<Activity> activities = new ArrayList<>();
        for (int i = 0; i < batchSize + 1; i++) {
            Activity a = new Activity();
            a.setName(String.valueOf(i));
            a.addMeasurement(new Measurement("m1", "value1", "type1"));
            activities.add(a);
        }

        ApplicationManager.getApplication().getComponent(InnometricsComponent.class).getState().setActivities(activities);

        assertNotNull(service);

        Method method = service.getClass().getDeclaredMethod("getNextMeasurementsBatch");
        method.setAccessible(true);

        Object result = method.invoke(service);
        List<Activity> batch = (List<Activity>) result;

        assertSize(batchSize, batch);

        assertEquals("0", batch.get(0).getName());
        assertEquals(String.valueOf(batchSize - 1), batch.get(batch.size() - 1).getName());
    }

    public void testMeasurementsJson() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Activity> activities = new ArrayList<>();
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

        Method method = ActivitiesSenderService.class.getDeclaredMethod("measurementsJson", List.class);
        method.setAccessible(true);

        Object result = method.invoke(null, activities);
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

    public void testRemoveMeasurementsBatch() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ActivitiesSenderService service = ServiceManager.getService(ActivitiesSenderService.class);

        List<Activity> activities = new ArrayList<>();
        List<Activity> toRemove = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Activity a = new Activity();
            a.setName(String.valueOf(i));
            a.addMeasurement(new Measurement("m1", "value1", "type1"));
            activities.add(a);
            if ((i < 3) || (i > 12)) {
                toRemove.add(a);
            }
        }
        ApplicationManager.getApplication().getComponent(InnometricsComponent.class).getState().setActivities(activities);

        Method method = service.getClass().getDeclaredMethod("removeMeasurementsBatch", List.class);
        method.setAccessible(true);
        method.invoke(service, toRemove);

        List<Activity> activitiesLeft = ApplicationManager.getApplication().getComponent(InnometricsComponent.class).getState().getActivities();

        assertSize(10, activitiesLeft);

        String[] expected = {"3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        for (Activity a : activitiesLeft) {
            assertOneOf(a.getName(), expected);
        }
    }

}