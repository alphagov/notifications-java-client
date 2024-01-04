package uk.gov.service.notify;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NotificationClientTest {

    private final String serviceId = UUID.randomUUID().toString();

    private final String apiKey = UUID.randomUUID().toString();
    private final String combinedApiKey = "Api_key_name-" +serviceId + "-" + apiKey;

    private final String baseUrl = "https://api.notifications.service.gov.uk";

    @Test
    public void testCreateNotificationClient_withSingleApiKeyAndBaseUrl(){
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);
        assertNotificationClient(client);

    }

    @Test
    public void testCreateNotificationClient_withSingleApiKeyAndProxy() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl, proxy);
        assertNotificationWithProxy(proxy, client);
    }

    @Test
    public void testCreateNotificationClient_withSingleApiKeyServiceIdAndProxy() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl, proxy);
        assertNotificationWithProxy(proxy, client);
    }

    @Test
    public void testCreateNotificationClientSetsUserAgent() {
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);
        assertTrue(client.getUserAgent().contains("NOTIFY-API-JAVA-CLIENT/"));
        assertTrue(client.getUserAgent().contains("-RELEASE"));
    }

    @Test
    public void testCreateNotificationClient_withSSLContext() throws NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getDefault();
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl, null, sslContext);
        assertNotificationClient(client);

    }

    private void assertNotificationWithProxy(Proxy proxy, NotificationClient client) {
        assertEquals(client.getApiKey(), apiKey);
        assertEquals(client.getServiceId(), serviceId);
        assertEquals(client.getBaseUrl(), baseUrl);
        assertEquals(client.getProxy(), proxy);
    }

    private void assertNotificationClient(final NotificationClient client){
        assertEquals(client.getApiKey(), apiKey);
        assertEquals(client.getServiceId(), serviceId);
        assertEquals(client.getBaseUrl(), baseUrl);
        assertNull(client.getProxy());
    }

    @Test(expected = NotificationClientException.class)
    public void sendPrecompiledLetterBase64EncodedPDFFileIsNull() throws Exception {
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);
        client.sendPrecompiledLetter("reference", null);
    }

    @Test(expected = NotificationClientException.class)
    public void testSendPrecompiledLetterNotPDF() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("not_a_pdf.txt").getFile());
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);
        client.sendPrecompiledLetter("reference", file);
    }

    @Test
    public void testPrepareUpload() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();
        JSONObject response = NotificationClient.prepareUpload(documentContent);
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("file", new String(Base64.encodeBase64(documentContent), ISO_8859_1));
        expectedResult.put("filename", JSONObject.NULL);
        expectedResult.put("confirm_email_before_download", JSONObject.NULL);
        expectedResult.put("retention_period", JSONObject.NULL);
        assertEquals(expectedResult.getString("file"), response.getString("file"));
        assertEquals(response.optJSONObject("filename"), null);
        assertEquals(response.optJSONObject("confirm_email_before_download"), null);
        assertEquals(response.optJSONObject("retention_period"), null);
    }

    @Test
    public void testPrepareUploadWithFilename() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();
        JSONObject response = NotificationClient.prepareUpload(documentContent, "report.csv");
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("file", new String(Base64.encodeBase64(documentContent), ISO_8859_1));
        expectedResult.put("filename", "report.csv");
        expectedResult.put("confirm_email_before_download", JSONObject.NULL);
        expectedResult.put("retention_period", JSONObject.NULL);
        assertEquals(expectedResult.getString("file"), response.getString("file"));
        assertEquals(expectedResult.getString("filename"), response.getString("filename"));
        assertEquals(response.optJSONObject("confirm_email_before_download"), null);
        assertEquals(response.optJSONObject("retention_period"), null);
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodString() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();
        JSONObject response = NotificationClient.prepareUpload(
                documentContent,
                "report.csv",
                true,
                "1 weeks");
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("file", new String(Base64.encodeBase64(documentContent), ISO_8859_1));
        expectedResult.put("filename", "report.csv");
        expectedResult.put("confirm_email_before_download", true);
        expectedResult.put("retention_period", "1 weeks");
        assertEquals(expectedResult.getString("file"), response.getString("file"));
        assertEquals(expectedResult.getString("filename"), response.getString("filename"));
        assertEquals(
                expectedResult.getBoolean("confirm_email_before_download"),
                response.getBoolean("confirm_email_before_download"));
        assertEquals(expectedResult.getString("retention_period"), response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadWithFilenameAndEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();
        JSONObject response = NotificationClient.prepareUpload(
                documentContent,
                "report.csv",
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("file", new String(Base64.encodeBase64(documentContent), ISO_8859_1));
        expectedResult.put("filename", "report.csv");
        expectedResult.put("confirm_email_before_download", true);
        expectedResult.put("retention_period", "1 weeks");
        assertEquals(expectedResult.getString("file"), response.getString("file"));
        assertEquals(expectedResult.getString("filename"), response.getString("filename"));
        assertEquals(
                expectedResult.getBoolean("confirm_email_before_download"),
                response.getBoolean("confirm_email_before_download"));
        assertEquals(expectedResult.getString("retention_period"), response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();
        JSONObject response = NotificationClient.prepareUpload(
                documentContent,
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("file", new String(Base64.encodeBase64(documentContent), ISO_8859_1));
        expectedResult.put("filename", JSONObject.NULL);
        expectedResult.put("confirm_email_before_download", true);
        expectedResult.put("retention_period", "1 weeks");
        assertEquals(expectedResult.getString("file"), response.getString("file"));
        assertEquals(expectedResult.optJSONObject("filename"), null);
        assertEquals(
                expectedResult.getBoolean("confirm_email_before_download"),
                response.getBoolean("confirm_email_before_download"));
        assertEquals(expectedResult.getString("retention_period"), response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadThrowsExceptionWhenExceeds2MB() throws NotificationClientException {
        char[] data = new char[(2*1024*1024)+50];
        byte[] documentContents = new String(data).getBytes();

        try {
            NotificationClient.prepareUpload(documentContents);
        }catch(NotificationClientException e){
            assertEquals(e.getHttpResult(), 413);
            assertEquals(e.getMessage(), "Status code: 413 File is larger than 2MB");
        }
    }

    @Test(expected = NotificationClientException.class)
    public void testShouldThrowNotificationExceptionOnErrorResponseCodeAndNoErrorStream() throws Exception {
        NotificationClient client = spy(new NotificationClient(combinedApiKey, baseUrl));
        doReturn(mockConnection(404)).when(client).getConnection(any());

        client.sendSms("aTemplateId", "aPhoneNumber", emptyMap(), "aReference");
    }

    private HttpURLConnection mockConnection(int statusCode) throws Exception {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(statusCode);
        return mockConnection;
    }
}
