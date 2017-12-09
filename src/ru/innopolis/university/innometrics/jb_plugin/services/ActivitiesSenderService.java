package ru.innopolis.university.innometrics.jb_plugin.services;

import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;
import ru.innopolis.university.innometrics.jb_plugin.models.Activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivitiesSenderService {

    private InnometricsComponent innometricsComponent;

    public ActivitiesSenderService(InnometricsComponent innometricsComponent) {
        this.innometricsComponent = innometricsComponent;
    }

    private static final Logger LOG = Logger.getInstance(ActivitiesSenderService.class);

    private static String requestAuthToken() {
        String innometricsLogin = PropertiesComponent.getInstance().getValue("innometrics.login", "");
        String innometricsPassword = PropertiesComponent.getInstance().getValue("innometrics.password", "");
        String innometricsUrl = PropertiesComponent.getInstance().getValue("innometrics.url", "http://127.0.0.1:8000/");

        Gson gson = new Gson();
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", innometricsLogin);
        credentials.put("password", innometricsPassword);
        String requestBody = gson.toJson(credentials);
//        System.out.println(requestBody);

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

            Map tokenResult = gson.fromJson(result, Map.class);
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
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String token = requestAuthToken();
            if (token == null) {
                LOG.warn("Can't login to the Innometrics server");
                return;
            }
            //TODO token saving

            String innometricsUrl = PropertiesComponent.getInstance().getValue("innometrics.url", "http://127.0.0.1:8000/");

            Gson gson = new Gson();
            Activities activities = new Activities();
            if (innometricsComponent.getState() != null) {
                activities.activities = innometricsComponent.getState().activities;
            } else {
                LOG.debug("InnometricsComponent state is null");
            }

            String requestBody = gson.toJson(activities);
            LOG.debug(requestBody);

            String measurementsUrl = innometricsUrl + "activities/";
            try {
                URL innometricsAuthUrl = new URL(measurementsUrl);
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
                    activities.activities.clear();
                    LOG.debug("Response code: " + conn.getResponseCode());
                    LOG.debug("Response message: " + conn.getResponseMessage());
                    Notification notification = new Notification("Innometrics Plugin", "Innometrics Plugin", "Measurements were sent successfully.", NotificationType.INFORMATION);
                    Notifications.Bus.notify(notification);
                } else {
                    LOG.warn("Response code: " + conn.getResponseCode());
                    LOG.warn("Response message: " + conn.getResponseMessage());
                    Notification notification = new Notification("Innometrics Plugin", "Innometrics connection problems", "An error occurred while trying to send data to the Innometrics server.", NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                }

            } catch (IOException e) {
                LOG.warn("Something went wrong during request '" + measurementsUrl + "'", e);
                Notification notification = new Notification("Innometrics Plugin", "Innometrics request problems", "An error occurred while trying to send data to the Innometrics server.", NotificationType.ERROR);
                Notifications.Bus.notify(notification);
            }
        });

    }
}
