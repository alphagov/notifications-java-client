package gov.va.vanotify;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static gov.va.vanotify.GsonConfiguration.gsonInstance;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class NotificationClient implements NotificationClientApi {

    private static final Logger LOGGER = Logger.getLogger(NotificationClient.class.toString());
    private static final String LIVE_BASE_URL = "https://api.notifications.va.gov";

    private final String apiKey;
    private final String serviceId;
    private final String baseUrl;
    private final Proxy proxy;
    private final String version;

    // === Legacy API key format (concatenated API key) constructors ===

    /**
     * Constructs a NotificationClient using the legacy concatenated API key format.
     * Defaults to the live VA Notify base URL with no proxy.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param apiKey Legacy API key in the format: (key-label)-(service-id)-(api-key)
     */
    public NotificationClient(final String apiKey) {
        this(apiKey, LIVE_BASE_URL, null);
    }

    /**
     * Constructs a NotificationClient using the legacy concatenated API key format and a proxy.
     * Defaults to the live VA Notify base URL.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param apiKey Legacy API key in the format: (key-label)-(service-id)-(api-key)
     * @param proxy  HTTP proxy to use for outbound HTTPS requests
     */
    public NotificationClient(final String apiKey, final Proxy proxy) {
        this(apiKey, LIVE_BASE_URL, proxy);
    }

    /**
     * Constructs a NotificationClient using the legacy concatenated API key format and a custom base URL.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param apiKey  Legacy API key in the format: (key-label)-(service-id)-(api-key)
     * @param baseUrl Custom base URL (e.g., for testing or staging environments)
     */
    public NotificationClient(final String apiKey, final String baseUrl) {
        this(apiKey, baseUrl, null);
    }

    /**
     * Constructs a NotificationClient using the legacy concatenated API key format,
     * with custom base URL and proxy settings.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * This constructor also attempts to set a default SSL context.
     *
     * @param apiKey  Legacy API key in the format: (key-label)-(service-id)-(api-key)
     * @param baseUrl Custom base URL (e.g., for testing or staging environments)
     * @param proxy   HTTP proxy to use for outbound HTTPS requests
     */
    public NotificationClient(final String apiKey, final String baseUrl, final Proxy proxy) {
        this(apiKey, baseUrl, proxy, null);
        try {
            setDefaultSSLContext();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    /**
     * Primary constructor for legacy API key format.
     * Parses the service ID and API key from the concatenated legacy API key format.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param apiKey       Legacy API key in the format: (key-label)-(service-id)-(api-key)
     * @param baseUrl      Target Notify API base URL
     * @param proxy        Optional HTTP proxy
     * @param sslContext   Optional custom SSL context (if null, caller must call fallback method)
     */
    public NotificationClient(
            final String apiKey,
            final String baseUrl,
            final Proxy proxy,
            final SSLContext sslContext) {

        this.apiKey = extractApiKey(apiKey);
        this.serviceId = extractServiceId(apiKey);
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        if (sslContext != null) {
            setCustomSSLContext(sslContext);
        }
        this.version = getVersion();
    }

    // === New API key format (separate service ID + API key) constructors ===

    /**
     * Constructs a NotificationClient using separate service ID and API key.
     * Defaults to the live VA Notify base URL with no proxy.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param serviceId UUID of the Notify service
     * @param apiKey    Long API key string associated with the service
     */
    public NotificationClient(final UUID serviceId, final String apiKey) {
        this(serviceId, apiKey, LIVE_BASE_URL, null);
    }

    /**
     * Constructs a NotificationClient using a service ID, API key, and proxy.
     * Defaults to the live VA Notify base URL.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param serviceId UUID of the Notify service
     * @param apiKey    API key string
     * @param proxy     HTTP proxy to use for outbound HTTPS requests
     */
    public NotificationClient(final UUID serviceId, final String apiKey, final Proxy proxy) {
        this(serviceId, apiKey, LIVE_BASE_URL, proxy);
    }

    /**
     * Constructs a NotificationClient using a service ID, API key, and custom base URL.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param serviceId UUID of the Notify service
     * @param apiKey    API key string
     * @param baseUrl   Custom base URL (e.g., for testing or staging environments)
     */
    public NotificationClient(final UUID serviceId, final String apiKey, final String baseUrl) {
        this(serviceId, apiKey, baseUrl, null);
    }

    /**
     * Constructs a NotificationClient using a service ID, API key, base URL, and proxy.
     * This constructor also attempts to set a default SSL context.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param serviceId UUID of the Notify service
     * @param apiKey    API key string
     * @param baseUrl   Custom base URL
     * @param proxy     HTTP proxy
     */
    public NotificationClient(final UUID serviceId, final String apiKey, final String baseUrl, final Proxy proxy) {
        this(serviceId, apiKey, baseUrl, proxy, null);
        try {
            setDefaultSSLContext();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    /**
     * Primary constructor for new service ID + API key format.
     * 
     * Note: To get an API key, sign in to [VANotify](https://notifications.va.gov/) and go to the **API integration** page.
     *
     * @param serviceId   UUID of the Notify service
     * @param apiKey      API key string
     * @param baseUrl     Target Notify API base URL
     * @param proxy       Optional HTTP proxy
     * @param sslContext  Optional custom SSL context
     */
    public NotificationClient(
            final UUID serviceId,
            final String apiKey,
            final String baseUrl,
            final Proxy proxy,
            final SSLContext sslContext) {

        this.apiKey = apiKey;
        this.serviceId = serviceId.toString();
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        if (sslContext != null) {
            setCustomSSLContext(sslContext);
        }
        this.version = getVersion();
    }

    public String getUserAgent() {
        return "NOTIFY-API-JAVA-CLIENT/" + version;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public SendEmailResponse sendEmail(EmailRequest emailRequest) throws NotificationClientException {
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/email", "POST");
        String response = performPostRequest(conn, gsonInstance.toJson(emailRequest), HttpsURLConnection.HTTP_CREATED);
        return gsonInstance.fromJson(response, SendEmailResponse.class);
    }

    public SendSmsResponse sendSms(SmsRequest smsRequest) throws NotificationClientException {
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/sms", "POST");
        String response = performPostRequest(conn, gsonInstance.toJson(smsRequest), HttpsURLConnection.HTTP_CREATED);
        return gsonInstance.fromJson(response, SendSmsResponse.class);
    }

    public SendPushResponse sendPush(PushRequest pushRequest) throws NotificationClientException {        
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/push", "POST");
        String response = performPostRequest(conn, gsonInstance.toJson(pushRequest), HttpsURLConnection.HTTP_CREATED);
        return gsonInstance.fromJson(response, SendPushResponse.class);
    }

    public SendPushResponse sendPushBroadcast(PushBroadcastRequest pushBroadcastRequest) throws NotificationClientException {        
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/push/broadcast", "POST");
        String response = performPostRequest(conn, gsonInstance.toJson(pushBroadcastRequest), HttpsURLConnection.HTTP_CREATED);
        return gsonInstance.fromJson(response, SendPushResponse.class);
    }

    public SendLetterResponse sendLetter(String templateId, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        JsonObject body = createBodyForPostRequest(templateId, null, null, personalisation, reference, null, null, null);
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/letter", "POST");
        String response = performPostRequest(conn, body.toString(), HttpsURLConnection.HTTP_CREATED);
        return gsonInstance.fromJson(response, SendLetterResponse.class);
    }

    public Notification getNotificationById(String notificationId) throws NotificationClientException {
        String url = baseUrl + "/v2/notifications/" + notificationId;
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");
        String response = performGetRequest(conn);
        return gsonInstance.fromJson(response, Notification.class);

    }

    public byte[] getPdfForLetter(String notificationId) throws NotificationClientException {
        String url = baseUrl + "/v2/notifications/" + notificationId + "/pdf";
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");

        return performRawGetRequest(conn);
    }

    public NotificationList getNotifications(String status, String notification_type, String reference, String olderThanId) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/notifications");
            if (status != null && !status.isEmpty()) {
                builder.addParameter("status", status);
            }
            if (notification_type != null && !notification_type.isEmpty()) {
                builder.addParameter("template_type", notification_type);
            }
            if (reference != null && !reference.isEmpty()) {
                builder.addParameter("reference", reference);
            }
            if (olderThanId != null && !olderThanId.isEmpty()) {
                builder.addParameter("older_than", olderThanId);
            }

            HttpURLConnection conn = createConnectionAndSetHeaders(builder.toString(), "GET");
            String response = performGetRequest(conn);
            return gsonInstance.fromJson(response, NotificationList.class);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    public Template getTemplateById(String templateId) throws NotificationClientException{
        String url = baseUrl + "/v2/template/" + templateId;
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");
        String response = performGetRequest(conn);
        return gsonInstance.fromJson(response, Template.class);
    }

    public Template getTemplateVersion(String templateId, int version) throws NotificationClientException{
        String url = baseUrl + "/v2/template/" + templateId + "/version/" + version;
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");
        String response = performGetRequest(conn);
        return gsonInstance.fromJson(response, Template.class);
    }

    public TemplateList getAllTemplates(String templateType) throws NotificationClientException{
        try{
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/templates");
            if (templateType != null && !templateType.isEmpty()) {
                builder.addParameter("type", templateType);
            }
            HttpURLConnection conn = createConnectionAndSetHeaders(builder.toString(), "GET");
            String response = performGetRequest(conn);
            return gsonInstance.fromJson(response, TemplateList.class);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    public TemplatePreview generateTemplatePreview(String templateId, Map<String, Object> personalisation) throws NotificationClientException {
        JsonObject body = new JsonObject();
        if (personalisation != null && !personalisation.isEmpty()) {
            body.add("personalisation",gsonInstance.toJsonTree(personalisation));
        }
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/template/" + templateId + "/preview", "POST");
        String response = performPostRequest(conn, body.toString(), HttpsURLConnection.HTTP_OK);
        return gsonInstance.fromJson(response, TemplatePreview.class);
    }

    public ReceivedTextMessageList getReceivedTextMessages(String olderThanId) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/received-text-messages");
            if (olderThanId != null && !olderThanId.isEmpty()) {
                builder.addParameter("older_than", olderThanId);
            }
            HttpURLConnection conn = createConnectionAndSetHeaders(builder.toString(), "GET");
            String response = performGetRequest(conn);
            return gsonInstance.fromJson(response, ReceivedTextMessageList.class);
        } catch (URISyntaxException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JsonObject</code> which will need to be added to the personalisation map.
     *
     * @param documentContents byte[] of the document
     * @param isCsv boolean True if a CSV file, False if not to ensure document is downloaded as correct file type
     * @return <code>JsonObject</code> a json object to be added to the personalisation is returned
     */
    public static JsonObject prepareUpload(final byte[] documentContents, boolean isCsv) throws NotificationClientException {
        if (documentContents.length > 2*1024*1024){
            throw new NotificationClientException(413, "File is larger than 2MB");
        }
        byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
        String fileContent = new String(fileContentAsByte, ISO_8859_1);

        JsonObject jsonFileObject = new JsonObject();
        jsonFileObject.addProperty("file", fileContent);
        jsonFileObject.addProperty("is_csv", isCsv);
        return jsonFileObject;
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JsonObject</code> which will need to be added to the personalisation map.
     *
     * @param documentContents byte[] of the document
     * @return <code>JsonObject</code> a json object to be added to the personalisation is returned
     */
    public static JsonObject prepareUpload(final byte[] documentContents) throws NotificationClientException {
        return prepareUpload(documentContents, false);
    }

    private String performPostRequest(HttpURLConnection conn, String body, int expectedStatusCode) throws NotificationClientException {
        try{
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), UTF_8);
            wr.write(body);
            wr.flush();

            int httpResult = conn.getResponseCode();
            if (httpResult == expectedStatusCode) {
                return readStream(conn.getInputStream());
            } else {
                throw new NotificationClientException(httpResult, readStream(conn.getErrorStream()));
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String performGetRequest(HttpURLConnection conn) throws NotificationClientException {
        try{
            int httpResult = conn.getResponseCode();
            StringBuilder stringBuilder;
            if (httpResult == 200) {
                return readStream(conn.getInputStream());
            } else {
                throw new NotificationClientException(httpResult, readStream(conn.getErrorStream()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private byte[] performRawGetRequest(HttpURLConnection conn) throws NotificationClientException {
        byte[] out;
        try{
            int httpResult = conn.getResponseCode();
            if (httpResult == 200) {
                InputStream is = conn.getInputStream();
                out = IOUtils.toByteArray(is);
            } else {
                throw new NotificationClientException(httpResult, readStream(conn.getErrorStream()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return out;
    }

    private HttpURLConnection createConnectionAndSetHeaders(String urlString, String method) throws NotificationClientException {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection conn = getConnection(url);
            conn.setRequestMethod(method);
            Authentication authentication = new Authentication();
            String token = authentication.create(serviceId, apiKey);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("User-agent", getUserAgent());
            if (method.equals("POST")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

            }
            return conn;
        }catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection conn;

        if (null != proxy) {
            conn = (HttpURLConnection) url.openConnection(proxy);
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        return conn;
    }

    private JsonObject createBodyForPostRequest(final String templateId,
                                                final String phoneNumber,
                                                final String emailAddress,
                                                final Map<String, ?> personalisation,
                                                final String reference,
                                                final String billingCode,
                                                final String encodedFileData,
                                                final String postage) {
        JsonObject body = new JsonObject();

        if(phoneNumber != null && !phoneNumber.isEmpty()) {
            body.addProperty("phone_number", phoneNumber);
        }

        if(emailAddress != null && !emailAddress.isEmpty()) {
            body.addProperty("email_address", emailAddress);
        }

        if(templateId != null && !templateId.isEmpty()) {
            body.addProperty("template_id", templateId);
        }

        if (personalisation != null && !personalisation.isEmpty()) {
            body.add("personalisation", gsonInstance.toJsonTree(personalisation));
        }

        if(reference != null && !reference.isEmpty()){
            body.addProperty("reference", reference);
        }

        if(billingCode != null && !billingCode.isEmpty()){
            body.addProperty("billing_code", billingCode);
        }

        if(encodedFileData != null && !encodedFileData.isEmpty()) {
            body.addProperty("content", encodedFileData);
        }
        if(postage != null && !postage.isEmpty()){
            body.addProperty("postage", postage);
        }
        return body;
    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        return IOUtils.toString(inputStream, UTF_8);
    }

    /**
     * Set default SSL context for HTTPS connections.
     * <p/>
     * This is necessary when client has to use keystore
     * (eg provide certification for client authentication).
     * <p/>
     * Use case: enterprise proxy requiring HTTPS client authentication
     */
    private static void setDefaultSSLContext() throws NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
    }

    private static void setCustomSSLContext(final SSLContext sslContext) {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }


    private static String extractServiceId(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 73), Math.max(0, apiKey.length() - 37));
    }

    private static String extractApiKey(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 36));
    }


    protected String getVersion(){
        InputStream input = null;
        Properties prop = new Properties();
        try
        {
            input = getClass().getClassLoader().getResourceAsStream("application.properties");

            prop.load(input);

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return prop.getProperty("project.version");
    }

    private LetterResponse sendPrecompiledLetter(String reference, String base64EncodedPDFFile, String postage) throws NotificationClientException {
        if( StringUtils.isBlank(reference) )
        {
            throw new NotificationClientException("reference cannot be null or empty");
        }

        if ( StringUtils.isBlank(base64EncodedPDFFile) )
        {
            throw new NotificationClientException("precompiledPDF cannot be null or empty");
        }

        if(!PdfUtils.isBase64StringPDF(base64EncodedPDFFile))
        {
            throw new NotificationClientException("base64EncodedPDFFile is not a PDF");
        }

        JsonObject body = createBodyForPostRequest(null,
                null,
                null,
                null,
                reference,
                null,
                base64EncodedPDFFile,
                postage);

        HttpURLConnection conn = createConnectionAndSetHeaders(
                baseUrl + "/v2/notifications/letter",
                "POST"
        );

        String response = performPostRequest(conn, body.toString(), HttpsURLConnection.HTTP_CREATED);
        return gsonInstance.fromJson(response, LetterResponse.class);

    }

    @Override
    public LetterResponse sendPrecompiledLetter(String reference, File precompiledPDF) throws NotificationClientException {
        return sendPrecompiledLetter(reference, precompiledPDF, null);
    }

    @Override
    public LetterResponse sendPrecompiledLetter(String reference, File precompiledPDF, String postage) throws NotificationClientException {
        if (precompiledPDF == null)
        {
            throw new NotificationClientException("File cannot be null");
        }
        byte[] buf;
        try {
            buf = FileUtils.readFileToByteArray(precompiledPDF);
        } catch (IOException e) {
            throw new NotificationClientException("Can't read file");
        }
        return sendPrecompiledLetterWithInputStream(reference, new ByteArrayInputStream(buf), postage);
    }

    @Override
    public LetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream) throws NotificationClientException
    {
       return sendPrecompiledLetterWithInputStream(reference, stream, null);
    }

    @Override
    public LetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream, String postage) throws NotificationClientException
    {
        if (stream == null)
        {
            throw new NotificationClientException("Input stream cannot be null");
        }
        Base64InputStream base64InputStream = new Base64InputStream(stream, true, 0, null);
        String encoded;
        try {
            encoded = IOUtils.toString(base64InputStream, "ISO-8859-1");
        } catch (IOException e) {
            throw new NotificationClientException("Error when turning Base64InputStream into a string");
        }

        return sendPrecompiledLetter(reference, encoded, postage);
    }

}
