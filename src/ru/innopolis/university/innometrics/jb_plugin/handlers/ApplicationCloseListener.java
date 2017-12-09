package ru.innopolis.university.innometrics.jb_plugin.handlers;

import com.intellij.openapi.application.ApplicationListener;
import org.jetbrains.annotations.NotNull;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;

public class ApplicationCloseListener implements ApplicationListener {

    private InnometricsComponent innometricsComponent;

    public ApplicationCloseListener(InnometricsComponent innometricsComponent) {
        this.innometricsComponent = innometricsComponent;
    }

    @Override
    public boolean canExitApplication() {
        return true;
    }

    @Override
    public void applicationExiting() {
        innometricsComponent.sessionActivity();
    }

    @Override
    public void beforeWriteActionStart(@NotNull Object action) {
    }

    @Override
    public void writeActionStarted(@NotNull Object action) {
    }

    @Override
    public void writeActionFinished(@NotNull Object action) {
    }

    @Override
    public void afterWriteActionFinished(@NotNull Object action) {
    }
}
