package ru.innopolis.university.innometrics.jb_plugin.services;

import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.RestService;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;
import ru.innopolis.university.innometrics.jb_plugin.models.Activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivitiesSenderService extends RestService {

    private InnometricsComponent innometricsComponent;

    public ActivitiesSenderService(InnometricsComponent innometricsComponent) {
        this.innometricsComponent = innometricsComponent;
    }

    private static final String SERVICE_NAME = "activitiesSenderService";
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
        // TODO find out how to use process() and execute() method
        new Thread(() -> {
            String token = requestAuthToken();
            if (token == null) {
                LOG.warn("Can't login to the Innometrics server");
                return;
            }

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
                    Notification notification = new Notification("Innometrics Plugin", "Innometrics Plugin", "Measurements was sent successfully.", NotificationType.INFORMATION);
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


        }).start();

    }

/*    private static void writeJson(@NotNull OutputStream out, @Nullable QueryStringDecoder urlDecoder) throws IOException {
        JsonWriter writer = createJsonWriter(out);
        writer.beginObject();
        //TODO
    }*/

    @NotNull
    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected boolean isMethodSupported(@NotNull HttpMethod method) {
        return method == HttpMethod.POST;
    }

    @Nullable
    @Override
    public String execute(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request, @NotNull ChannelHandlerContext context) throws IOException {
//        BufferExposingByteArrayOutputStream byteOut = new BufferExposingByteArrayOutputStream();
//        writeJson(byteOut, urlDecoder);
//        send(byteOut, request, context);
        return null;
    }

    @Override
    protected boolean isHostTrusted(@NotNull FullHttpRequest request) throws InterruptedException, InvocationTargetException {
        return true;
    }
}
