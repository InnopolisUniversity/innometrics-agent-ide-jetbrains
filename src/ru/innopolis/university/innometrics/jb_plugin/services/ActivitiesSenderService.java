package ru.innopolis.university.innometrics.jb_plugin.services;

import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;
import ru.innopolis.university.innometrics.jb_plugin.models.Activities;
import ru.innopolis.university.innometrics.jb_plugin.models.Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivitiesSenderService {

    private static final int BATCH_SIZE = 100;
    private static final Logger LOG = Logger.getInstance(ActivitiesSenderService.class);

    private InnometricsComponent innometricsComponent;

    public ActivitiesSenderService(InnometricsComponent innometricsComponent) {
        this.innometricsComponent = innometricsComponent;
    }

    private static String authRequestJsonBody(String login, String password) {
        Gson gson = new Gson();
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", login);
        credentials.put("password", password);
        return gson.toJson(credentials);
    }

    private static String measurementsJson(List<Activity> activities) {
        Gson gson = new Gson();
        Activities activitiesWrapper = new Activities();
        activitiesWrapper.activities = activities;

        return gson.toJson(activitiesWrapper);
    }

    private List<Activity> getNextMeasurementsBatch() {
        List<Activity> activities = new ArrayList<>();
        if (innometricsComponent.getState() != null) {
            activities = innometricsComponent.getState().getActivities();
            if (!activities.isEmpty()) {
                activities = activities.subList(0, Math.min(BATCH_SIZE, activities.size()));
                activities = new ArrayList<>(activities); // to avoid ConcurrentModificationException while removing
            }
        } else {
            LOG.debug("InnometricsComponent state is null");
        }
        return activities;
    }

    private void removeMeasurementsBatch(List<Activity> toRemove) {
        if (innometricsComponent.getState() == null) {
            LOG.debug("InnometricsComponent state is null");
            return;
        }

        List<Activity> activities = innometricsComponent.getState().getActivities();
        activities.removeAll(toRemove);
    }

    private static String requestAuthToken() {
        String innometricsLogin = PropertiesComponent.getInstance().getValue("innometrics.login", "");
        String innometricsPassword = PropertiesComponent.getInstance().getValue("innometrics.password", "");
        String innometricsUrl = PropertiesComponent.getInstance().getValue("innometrics.url", "http://127.0.0.1:8000/");

        String requestBody = authRequestJsonBody(innometricsLogin, innometricsPassword);

        String authUrl = innometricsUrl + "api-token-auth/";
        try {
            URL innometricsAuthUrl = new URL(authUrl);
            HttpURLConnection conn = (HttpURLConnection) innometricsAuthUrl.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "UTF-8");

            try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
                wr.write(requestBody);
                wr.flush();
            }
            conn.connect();
            // TODO find built-in rest request wrapper

            LOG.debug("Response code: " + String.valueOf(conn.getResponseCode()));
            LOG.debug("Response message: " + String.valueOf(conn.getResponseMessage()));

            String result = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            Map tokenResult = new Gson().fromJson(result, Map.class);
            Object token = tokenResult.get("token");
//            return token == null ? null : String.valueOf(token);
            return (String) token;
        } catch (IOException e) {
            Notification notification = new Notification("Innometrics Plugin", "Innometrics connection problems", "An error occurred while trying to connect to the Innometrics server.", NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            LOG.warn("Something went wrong during request '" + authUrl + "'", e);
        }
        return null;
    }

    public void sendMeasurements() {
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Innometrics Measurements Sending") {
            public void run(@NotNull ProgressIndicator indicator) {

                String token = requestAuthToken();
                if (token == null) {
                    LOG.warn("Can't login to the Innometrics server");
                    return;
                }
                //TODO token saving

                String innometricsUrl = PropertiesComponent.getInstance().getValue("innometrics.url", "http://127.0.0.1:8000/");
                String measurementsUrl = innometricsUrl + "activities/";

                Notification notification;
                try {
                    URL innometricsAuthUrl = new URL(measurementsUrl);
                    int activitiesCount = innometricsComponent.getState() != null ? innometricsComponent.getState().getActivities().size() : 0;

                    boolean success = true;
                    while (innometricsComponent.getState() != null && !innometricsComponent.getState().getActivities().isEmpty()) {

                        List<Activity> activitiesBatch = getNextMeasurementsBatch();
                        String requestBody = measurementsJson(activitiesBatch);
                        LOG.debug(requestBody);

                        HttpURLConnection conn = (HttpURLConnection) innometricsAuthUrl.openConnection();
                        conn.setDoOutput(true);
                        conn.setInstanceFollowRedirects(false);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("charset", "UTF-8");
                        conn.setRequestProperty("Authorization", "Token " + token);

                        try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
                            wr.write(requestBody);
                            wr.flush();
                        }
                        conn.connect();

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                            success = true;
                            removeMeasurementsBatch(activitiesBatch);
                            int activitiesLeft = innometricsComponent.getState().getActivities().size();
                            indicator.setText("Sent: " + (activitiesCount - activitiesLeft) + "/" + activitiesCount);
                            indicator.setFraction(1.0 - activitiesLeft * 1.0 / activitiesCount);
                            LOG.debug("Response code: " + conn.getResponseCode());
                            LOG.debug("Response message: " + conn.getResponseMessage());
                        } else {
                            success = false;
                            LOG.warn("Response code: " + conn.getResponseCode());
                            LOG.warn("Response message: " + conn.getResponseMessage());
                            break;
                        }
                    }

                    if (success) {
                        notification = new Notification("Innometrics Plugin", "Innometrics Plugin", "Measurements were sent successfully.", NotificationType.INFORMATION);
                    } else {
                        notification = new Notification("Innometrics Plugin", "Innometrics connection problems", "An error occurred while trying to connect to the Innometrics server.", NotificationType.ERROR);
                    }

                } catch (IOException e) {
                    LOG.warn("Something went wrong during request '" + measurementsUrl + "'", e);
                    notification = new Notification("Innometrics Plugin", "Innometrics request problems", "An error occurred while trying to send data to the Innometrics server.", NotificationType.ERROR);
                }

                Notifications.Bus.notify(notification);
            }
        });

    }
}
