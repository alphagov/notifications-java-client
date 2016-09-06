package uk.gov.service.notify;


import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class NotificationClient implements NotificationClientApi {

    private String secret;
    private String issuer;
    private String baseUrl;
    private Proxy proxy;


    public NotificationClient(String secret, String issuer, String baseUrl) {
        this.secret = secret;
        this.issuer = issuer;
        this.baseUrl = baseUrl;

        // Could be called from here or from outside.
        try {
            setDefaultSSLContext();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public NotificationClient(String secret, String issuer, String baseUrl, Proxy proxy) {
        this(secret, issuer, baseUrl);
        this.proxy = proxy;
    }

    public NotificationResponse sendEmail(String templateId, String to, HashMap<String, String> personalisation) throws NotificationClientException {
        return postRequest("email", templateId, to, personalisation);
    }

    public NotificationResponse sendSms(String templateId, String to, HashMap<String, String> personalisation) throws NotificationClientException {
        return postRequest("sms", templateId, to, personalisation);
    }

    private NotificationResponse postRequest(String messageType, String templateId, String to, HashMap<String, String> personalisation) throws NotificationClientException {
        HttpsURLConnection conn = null;
        try {
            JSONObject body = createBodyForRequest(templateId, to, personalisation);

            Authentication tg = new Authentication();
            String token = tg.create(issuer, secret);
            URL url = new URL(baseUrl + "/notifications/" + messageType);
            conn = postConnection(token, url);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(body.toString());
            wr.flush();

            int httpResult = conn.getResponseCode();
            if (httpResult == HttpsURLConnection.HTTP_CREATED) {
                StringBuilder sb = readStream(new InputStreamReader(conn.getInputStream(), "utf-8"));
                return new NotificationResponse(sb.toString());
            } else {
                StringBuilder sb = readStream(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                throw new NotificationClientException(httpResult, sb.toString());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public Notification getNotificationById(String notificationId) throws NotificationClientException {
        StringBuilder stringBuilder;
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + "/notifications/" + notificationId);
            conn = getConnection(url);
            conn.setRequestMethod("GET");
            Authentication authentication = new Authentication();
            String token = authentication.create(issuer, secret);
            conn.setRequestProperty("Authorization", "Bearer " + token);

            conn.connect();
            int httpResult = conn.getResponseCode();
            if (httpResult == 200) {
                stringBuilder = readStream(new InputStreamReader(conn.getInputStream()));
                conn.disconnect();
                return new Notification(stringBuilder.toString());
            } else {
                stringBuilder = readStream(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                throw new NotificationClientException(httpResult, stringBuilder.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public NotificationList getNotifications(String status, String notification_type) throws NotificationClientException {
        JSONObject data = new JSONObject();
        if (status != null && !status.isEmpty()) {
            data.put("status", status);
        }
        if (notification_type != null && !notification_type.isEmpty()) {
            data.put("template_type", notification_type);
        }
        StringBuilder stringBuilder;
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + "/notifications");
            conn = getConnection(url);
            conn.setRequestMethod("GET");
            Authentication authentication = new Authentication();
            String token = authentication.create(issuer, secret);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.connect();
            int httpResult = conn.getResponseCode();
            if (httpResult == 200) {
                stringBuilder = readStream(new InputStreamReader(conn.getInputStream()));
                conn.disconnect();
                return new NotificationList(stringBuilder.toString());
            } else {
                stringBuilder = readStream(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                throw new NotificationClientException(httpResult, stringBuilder.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private HttpsURLConnection getConnection(URL url) throws IOException {
        HttpsURLConnection conn;

        if (null != proxy) {
            conn = (HttpsURLConnection) url.openConnection(proxy);
        } else {
            conn = (HttpsURLConnection) url.openConnection();
        }

        return conn;
    }

    private HttpsURLConnection postConnection(String token, URL url) throws IOException {
        HttpsURLConnection conn = getConnection(url);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        conn.connect();
        return conn;
    }

    private JSONObject createBodyForRequest(String templateId, String to, HashMap<String, String> personalisation) {
        JSONObject body = new JSONObject();
        body.put("to", to);
        body.put("template", templateId);
        if (personalisation != null && !personalisation.isEmpty()) {
            body.put("personalisation", new JSONObject(personalisation));
        }
        return body;
    }

    private StringBuilder readStream(InputStreamReader streamReader) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(streamReader);
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb;
    }

    /**
     * Set default SSL context for HTTPS connections.
     *
     * This is necessary when client has to use keystore
     * (eg provide certification for client authentication).
     *
     * Use case: enterprise proxy requiring HTTPS client authentication
     *
     * @throws NoSuchAlgorithmException
     */
    private static void setDefaultSSLContext() throws NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
    }

}
