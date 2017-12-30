package ru.innopolis.university.innometrics.jb_plugin.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.lang.reflect.Field;

public class ToolsMenuItemTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDefaultInputFields() throws NoSuchFieldException, IllegalAccessException {
        ToolsMenuItem toolsMenuItem = (ToolsMenuItem) ActionManager.getInstance().getAction("Innometrics.ToolsMenuItem");
        assertNotNull(toolsMenuItem);

        Class<?> aClass = toolsMenuItem.getClass();

        Field field = aClass.getDeclaredField("defaultInnometricsUrl");
        field.setAccessible(true);
        assertEquals("http://some.test.domain:8000/", field.get(toolsMenuItem));
    }

}