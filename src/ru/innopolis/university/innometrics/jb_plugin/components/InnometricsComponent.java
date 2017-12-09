package ru.innopolis.university.innometrics.jb_plugin.components;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.EditorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.innopolis.university.innometrics.jb_plugin.handlers.ApplicationCloseListener;
import ru.innopolis.university.innometrics.jb_plugin.handlers.CodePathCaretListener;
import ru.innopolis.university.innometrics.jb_plugin.models.Activity;
import ru.innopolis.university.innometrics.jb_plugin.models.Measurement;

import java.util.ArrayList;
import java.util.List;

@State(name = "Innometrics.InnometricsComponent", storages = {@Storage("innometrics.activities.xml")})
public class InnometricsComponent implements ApplicationComponent, PersistentStateComponent<InnometricsComponent.State> {

    private static final String COMPONENT_NAME = "Innometrics.InnometricsComponent";

    private String versionName;
    private String fullVersion;
    private String companyName;

    public State state;

    public static class State {
        //TODO replace with thread-safe list
        public List<Activity> activities;

        public State() {
            this.activities = new ArrayList<>();
            System.out.println("new state");
        }

        @Override
        public String toString() {
            return String.valueOf(this.activities.size()) + this.activities;
        }
    }


    public InnometricsComponent() {
        System.out.println("constr");
    }

    @Override
    public void initComponent() {
        System.out.println("init: " + this.state);
        this.versionName = ApplicationInfoImpl.getInstance().getVersionName();
        this.fullVersion = ApplicationInfoImpl.getInstance().getFullVersion();
        this.companyName = ApplicationInfoImpl.getInstance().getCompanyName();
        if (this.state == null) {
            this.state = new State();
        }
        EditorFactory.getInstance().getEventMulticaster().addCaretListener(new CodePathCaretListener(this));
        ApplicationManager.getApplication().addApplicationListener(new ApplicationCloseListener(this));
    }

    @Override
    public void disposeComponent() {
        System.out.println("dispose");
    }

    @Override
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable
    @Override
    public State getState() {
        // TODO log
        System.out.println("get: " + this.state);
        return this.state;
    }

    @Override
    public void loadState(State state) {
        this.state = state;
        System.out.println("load: " + this.state);
    }

    public void sessionActivity() {
        Activity a = new Activity();
        a.name = "JetBrains IDE session time";
        a.addMeasurement(new Measurement("version name", this.versionName, "string"));
        a.addMeasurement(new Measurement("full version", this.fullVersion, "string"));
        a.addMeasurement(new Measurement("company name", this.companyName, "string"));
        a.addMeasurement(new Measurement("start time", ApplicationManager.getApplication().getStartTime() + "", "long"));
        a.addMeasurement(new Measurement("close time", System.currentTimeMillis() + "", "long"));
        a.addMeasurement(new Measurement("idle time", IdeEventQueue.getInstance().getIdleTime() + "", "long"));
        this.state.activities.add(a);
    }

    public void editorLogActivity(String path, String file, long time) {
        Activity a = new Activity();
        a.name = "JetBrains IDE code location";
        a.addMeasurement(new Measurement("version name", this.versionName, "string"));
        a.addMeasurement(new Measurement("full version", this.fullVersion, "string"));
        a.addMeasurement(new Measurement("company name", this.companyName, "string"));
        a.addMeasurement(new Measurement("file path", file, "string"));
        a.addMeasurement(new Measurement("code path", path, "string"));
        a.addMeasurement(new Measurement("code time", String.valueOf(time), "long"));
        this.state.activities.add(a);
    }
}
