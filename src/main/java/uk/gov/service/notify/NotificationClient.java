package uk.gov.service.notify;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class NotificationClient implements NotificationClientApi {

    private static final Logger LOGGER = Logger.getLogger(NotificationClient.class.toString());
    private static final String LIVE_BASE_URL = "https://api.notifications.service.gov.uk";

    private final String apiKey;
    private final String serviceId;
    private final String baseUrl;
    private final Proxy proxy;
    private final String version;

    /**
     * This client constructor given the api key.
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     */
    public NotificationClient(final String apiKey) {
        this(
                apiKey,
                LIVE_BASE_URL,
                null
        );
    }

    /**
     * Use this client constructor if you require a proxy for https requests.
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param proxy Proxy used on the http requests
     */
    public NotificationClient(final String apiKey, final Proxy proxy) {
        this(
                apiKey,
                LIVE_BASE_URL,
                proxy
        );
    }

    /**
     * This client constructor is used for testing on other environments, used by the GOV.UK Notify team.
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl base URL, defaults to https://api.notifications.service.gov.uk
     */
    public NotificationClient(final String apiKey, final String baseUrl) {
        this(
                apiKey,
                baseUrl,
                null
        );
    }


    /**
     *
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl base URL, defaults to https://api.notifications.service.gov.uk
     * @param proxy Proxy used on the http requests
     */
    public NotificationClient(final String apiKey, final String baseUrl, final Proxy proxy) {
        this(
                apiKey,
                baseUrl,
                proxy,
                null
        );
        try {
            setDefaultSSLContext();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    public NotificationClient(final String apiKey,
                              final String baseUrl,
                              final Proxy proxy,
                              final SSLContext sslContext){

        this.apiKey = extractApiKey(apiKey);
        this.serviceId = extractServiceId(apiKey);
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        if (sslContext != null){
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

    public SendEmailResponse sendEmail(UUID templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference) throws NotificationClientException {
        return sendEmail(templateId, emailAddress, personalisation, reference, null, null);
    }

    @Override
    public SendEmailResponse sendEmail(UUID templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference,
                                       UUID emailReplyToId) throws NotificationClientException {
        return sendEmail(templateId, emailAddress, personalisation, reference, emailReplyToId, null);
    }

    @Override
    public SendEmailResponse sendEmail(UUID templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference,
                                       UUID emailReplyToId,
                                       URI oneClickUnsubscribeURL) throws NotificationClientException {

        JSONObject body = createBodyForPostRequest(templateId,
                null,
                emailAddress,
                personalisation,
                reference,
                null,
                null);

        if(emailReplyToId != null)
        {
            body.put("email_reply_to_id", emailReplyToId);
        }

        if(oneClickUnsubscribeURL != null)
        {
            body.put("one_click_unsubscribe_url", oneClickUnsubscribeURL);
        }

        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/email", "POST");
        String response = performPostRequest(conn, body, HttpsURLConnection.HTTP_CREATED);
        return new SendEmailResponse(response);
    }

    public SendSmsResponse sendSms(UUID templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        return sendSms(templateId, phoneNumber, personalisation, reference, null);
    }

    public SendSmsResponse sendSms(UUID templateId,
                                   String phoneNumber,
                                   Map<String, ?> personalisation,
                                   String reference,
                                   UUID smsSenderId) throws NotificationClientException {

        JSONObject body = createBodyForPostRequest(templateId,
                phoneNumber,
                null,
                personalisation,
                reference,
                null,
                null);

        if( smsSenderId != null){
            body.put("sms_sender_id", smsSenderId);
        }
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/sms", "POST");
        String response = performPostRequest(conn, body, HttpsURLConnection.HTTP_CREATED);
        return new SendSmsResponse(response);
    }

    public SendLetterResponse sendLetter(UUID templateId, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        JSONObject body = createBodyForPostRequest(templateId, null, null, personalisation, reference, null, null);
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/letter", "POST");
        String response = performPostRequest(conn, body, HttpsURLConnection.HTTP_CREATED);
        return new SendLetterResponse(response);
    }

    public Notification getNotificationById(UUID notificationId) throws NotificationClientException {
        String url = baseUrl + "/v2/notifications/" + notificationId;
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");
        String response = performGetRequest(conn);
        return new Notification(response);

    }

    public byte[] getPdfForLetter(UUID notificationId) throws NotificationClientException {
        String url = baseUrl + "/v2/notifications/" + notificationId + "/pdf";
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");

        return performRawGetRequest(conn);
    }

    public NotificationList getNotifications(String status, String notification_type, String reference, UUID olderThanId) throws NotificationClientException {
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
            if (olderThanId != null) {
                builder.addParameter("older_than", olderThanId.toString());
            }

            HttpURLConnection conn = createConnectionAndSetHeaders(builder.toString(), "GET");
            String response = performGetRequest(conn);
            return new NotificationList(response);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    public Template getTemplateById(UUID templateId) throws NotificationClientException{
        String url = baseUrl + "/v2/template/" + templateId;
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");
        String response = performGetRequest(conn);
        return new Template(response);
    }

    public Template getTemplateVersion(UUID templateId, int version) throws NotificationClientException{
        String url = baseUrl + "/v2/template/" + templateId + "/version/" + version;
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");
        String response = performGetRequest(conn);
        return new Template(response);
    }

    public TemplateList getAllTemplates(String templateType) throws NotificationClientException{
        try{
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/templates");
            if (templateType != null && !templateType.isEmpty()) {
                builder.addParameter("type", templateType);
            }
            HttpURLConnection conn = createConnectionAndSetHeaders(builder.toString(), "GET");
            String response = performGetRequest(conn);
            return new TemplateList(response);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    public TemplatePreview generateTemplatePreview(UUID templateId, Map<String, Object> personalisation) throws NotificationClientException {
        JSONObject body = new JSONObject();
        if (personalisation != null && !personalisation.isEmpty()) {
            body.put("personalisation", new JSONObject(personalisation));
        }
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/template/" + templateId + "/preview", "POST");
        String response = performPostRequest(conn, body, HttpsURLConnection.HTTP_OK);
        return new TemplatePreview(response);
    }

    public ReceivedTextMessageList getReceivedTextMessages(UUID olderThanId) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/received-text-messages");
            if (olderThanId != null) {
                builder.addParameter("older_than", olderThanId.toString());
            }
            HttpURLConnection conn = createConnectionAndSetHeaders(builder.toString(), "GET");
            String response = performGetRequest(conn);
            return new ReceivedTextMessageList(response);
        } catch (URISyntaxException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents           byte[] of the document
     * @param filename                   a string setting the filename of the
     *                                   document upon download
     * @param confirmEmailBeforeDownload boolean True to require the user to enter
     *                                   their email address before accessing the
     *                                   file
     * @param retentionPeriod            a string '[1-78] weeks' to change how long
     *                                   the document should be available to the
     *                                   user
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents, String filename,
            boolean confirmEmailBeforeDownload, String retentionPeriod) throws NotificationClientException {
        if (documentContents.length > 2 * 1024 * 1024) {
            throw new NotificationClientException(413, "File is larger than 2MB");
        }
        byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
        String fileContent = new String(fileContentAsByte, ISO_8859_1);

        JSONObject jsonFileObject = new JSONObject();
        jsonFileObject.put("file", fileContent);
        jsonFileObject.put("filename", filename);
        jsonFileObject.put("confirm_email_before_download", confirmEmailBeforeDownload);
        jsonFileObject.put("retention_period", retentionPeriod);
        return jsonFileObject;
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents           byte[] of the document
     * @param confirmEmailBeforeDownload boolean True to require the user to enter
     *                                   their email address before accessing the
     *                                   file
     * @param retentionPeriod            a string '[1-78] weeks' to change how long
     *                                   the document should be available to the
     *                                   user
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents,
            boolean confirmEmailBeforeDownload, RetentionPeriodDuration retentionPeriod)
            throws NotificationClientException {
        if (documentContents.length > 2 * 1024 * 1024) {
            throw new NotificationClientException(413, "File is larger than 2MB");
        }
        byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
        String fileContent = new String(fileContentAsByte, ISO_8859_1);

        JSONObject jsonFileObject = new JSONObject();
        jsonFileObject.put("file", fileContent);
        jsonFileObject.put("filename", JSONObject.NULL);
        jsonFileObject.put("confirm_email_before_download", confirmEmailBeforeDownload);
        jsonFileObject.put("retention_period", retentionPeriod.toString());
        return jsonFileObject;
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents byte[] of the document
     * @param filename         a string setting the filename of the
     *                         document upon download
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents, String filename)
            throws NotificationClientException {
        if (documentContents.length > 2 * 1024 * 1024) {
            throw new NotificationClientException(413, "File is larger than 2MB");
        }
        byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
        String fileContent = new String(fileContentAsByte, ISO_8859_1);

        JSONObject jsonFileObject = new JSONObject();
        jsonFileObject.put("file", fileContent);
        jsonFileObject.put("filename", filename);
        jsonFileObject.put("confirm_email_before_download", JSONObject.NULL);
        jsonFileObject.put("retention_period", JSONObject.NULL);
        return jsonFileObject;
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents byte[] of the document
     * @return <code>JSONObject</code> a json object to be added to the personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents) throws NotificationClientException {
        if (documentContents.length > 2 * 1024 * 1024) {
            throw new NotificationClientException(413, "File is larger than 2MB");
        }
        byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
        String fileContent = new String(fileContentAsByte, ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject();
        jsonFileObject.put("file", fileContent);
        jsonFileObject.put("filename", JSONObject.NULL);
        jsonFileObject.put("confirm_email_before_download", JSONObject.NULL);
        jsonFileObject.put("retention_period", JSONObject.NULL);
        return jsonFileObject;
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * This version of the class overloads prepareUpload to allow for the use of the
     * RetentionPeriodDuration class if
     * desired.
     *
     * @see RetentionPeriodDuration
     *
     * @param documentContents           byte[] of the document
     * @param filename                   a string setting the filename of the
     *                                   document upon download
     * @param confirmEmailBeforeDownload boolean True to require the user to enter
     *                                   their email address before accessing the
     *                                   file
     * @param retentionPeriod            a RetentionPeriodDuration that defines how
     *                                   long a file is held for
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents,
            String filename,
            boolean confirmEmailBeforeDownload,
            RetentionPeriodDuration retentionPeriod) throws NotificationClientException {
        return prepareUpload(documentContents, filename, confirmEmailBeforeDownload, retentionPeriod.toString());
    }

    private String performPostRequest(HttpURLConnection conn, JSONObject body, int expectedStatusCode) throws NotificationClientException {
        try{
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), UTF_8);
            wr.write(body.toString());
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

    private JSONObject createBodyForPostRequest(final UUID templateId,
                                                final String phoneNumber,
                                                final String emailAddress,
                                                final Map<String, ?> personalisation,
                                                final String reference,
                                                final String encodedFileData,
                                                final String postage) {
        JSONObject body = new JSONObject();

        if(phoneNumber != null && !phoneNumber.isEmpty()) {
            body.put("phone_number", phoneNumber);
        }

        if(emailAddress != null && !emailAddress.isEmpty()) {
            body.put("email_address", emailAddress);
        }

        if(templateId != null) {
            body.put("template_id", templateId);
        }

        if (personalisation != null && !personalisation.isEmpty()) {
            body.put("personalisation", personalisation);
        }

        if(reference != null && !reference.isEmpty()){
            body.put("reference", reference);
        }

        if(encodedFileData != null && !encodedFileData.isEmpty()) {
            body.put("content", encodedFileData);
        }
        if(postage != null && !postage.isEmpty()){
            body.put("postage", postage);
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


    private String getVersion(){
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
        if( isBlank(reference) )
        {
            throw new NotificationClientException("reference cannot be null or empty");
        }

        if ( isBlank(base64EncodedPDFFile) )
        {
            throw new NotificationClientException("precompiledPDF cannot be null or empty");
        }

        if(!PdfUtils.isBase64StringPDF(base64EncodedPDFFile))
        {
            throw new NotificationClientException("base64EncodedPDFFile is not a PDF");
        }

        JSONObject body = createBodyForPostRequest(null,
                null,
                null,
                null,
                reference,
                base64EncodedPDFFile,
                postage);

        HttpURLConnection conn = createConnectionAndSetHeaders(
                baseUrl + "/v2/notifications/letter",
                "POST"
        );

        String response = performPostRequest(conn, body, HttpsURLConnection.HTTP_CREATED);
        return new LetterResponse(response);

    }

    private boolean isBlank(String string) {
        if(Objects.isNull(string)
                || string.trim().length()==0) {
            return true;
        }
        return false;
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
