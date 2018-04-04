package ru.innopolis.university.innometrics.jb_plugin.components;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.innopolis.university.innometrics.jb_plugin.handlers.ApplicationCloseListener;
import ru.innopolis.university.innometrics.jb_plugin.handlers.CodePathCaretListener;
import ru.innopolis.university.innometrics.jb_plugin.models.Activity;
import ru.innopolis.university.innometrics.jb_plugin.models.Measurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@State(name = "Innometrics.InnometricsComponent", storages = {@Storage("innometrics.activities.xml")})
public class InnometricsComponent implements ApplicationComponent, PersistentStateComponent<InnometricsComponent.State> {

    public static class State {

        private List<Activity> activities;

        State() {
            this.activities = Collections.synchronizedList(new ArrayList<>());
        }


        public List<Activity> getActivities() {
            return activities;
        }

        public void setActivities(List<Activity> activities) {
            this.activities = Collections.synchronizedList(activities);
        }
    }

    private static final String COMPONENT_NAME = "Innometrics.InnometricsComponent";
    private static final Logger LOG = Logger.getInstance(InnometricsComponent.class);

    private String versionName;
    private String fullVersion;
    private String companyName;

    private State state;
    private Activity tempActivity;

    @Override
    public void initComponent() {
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
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable
    @Override
    public State getState() {
        closeActivity(System.currentTimeMillis());
        LOG.debug("saving activities: " + this.state.activities.size() + " activities collected");
        return this.state;
    }

    @Override
    public void loadState(State state) {
        this.state = state;
        LOG.debug("loading activities: " + (state == null ? "No" : this.state.activities.size()) + " activities collected");
    }

    public void sessionActivity() {
        closeActivity(System.currentTimeMillis());

        // epoch time in seconds
        long startTime = ApplicationManager.getApplication().getStartTime() / 1000;
        long closeTime = System.currentTimeMillis() / 1000;
        long idleTime = IdeEventQueue.getInstance().getIdleTime() / 1000;

        Activity a = new Activity();
        a.name = "JetBrains IDE session time";
        a.addMeasurement(new Measurement("version name", this.versionName, "string"));
        a.addMeasurement(new Measurement("full version", this.fullVersion, "string"));
        a.addMeasurement(new Measurement("company name", this.companyName, "string"));
        a.addMeasurement(new Measurement("start time", String.valueOf(startTime), "long"));
        a.addMeasurement(new Measurement("close time", String.valueOf(closeTime), "long"));
        a.addMeasurement(new Measurement("idle time", String.valueOf(idleTime), "long"));
        this.state.activities.add(a);
    }

    public void switchActivity(String path, String file, long millis) {
        closeActivity(millis);

        Activity a = new Activity();
        a.name = "JetBrains IDE code location";
        a.addMeasurement(new Measurement("version name", this.versionName, "string"));
        a.addMeasurement(new Measurement("full version", this.fullVersion, "string"));
        a.addMeasurement(new Measurement("company name", this.companyName, "string"));
        a.addMeasurement(new Measurement("file path", file, "string"));
        a.addMeasurement(new Measurement("code path", path, "string"));
        a.addMeasurement(new Measurement("code begin time", String.valueOf(millis / 1000), "long"));
        this.tempActivity = a;
    }

    private void closeActivity(long millis) {
        if (tempActivity != null) {
            tempActivity.addMeasurement(new Measurement("code end time", String.valueOf(millis / 1000), "long"));
            this.state.activities.add(tempActivity);
            tempActivity = null;
        }
    }
}
