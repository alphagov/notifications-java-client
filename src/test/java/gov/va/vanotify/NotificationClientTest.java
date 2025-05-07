package gov.va.vanotify;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationClientTest {

    private final UUID serviceUUID = UUID.randomUUID();
    private final String serviceId = serviceUUID.toString();
    private final String apiKey = UUID.randomUUID().toString();
    private final String combinedApiKey = "Api_key_name-" +serviceId + "-" + apiKey;
    private final String baseUrl = "https://api.notifications.va.gov";

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
    public void testCreateNotificationClientSetsUserAgent() {
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);
        assertTrue(client.getUserAgent().contains("NOTIFY-API-JAVA-CLIENT/"));
        assertTrue(client.getUserAgent().contains(client.getVersion()));
    }

    @Test
    public void testCreateNotificationClient_withSSLContext() throws NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getDefault();
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl, null, sslContext);
        assertNotificationClient(client);
    }

    @Test
    public void testCreateNotificationClient_withServiceAndApiKeyAndBaseUrl(){
        NotificationClient client = new NotificationClient(serviceUUID, apiKey, baseUrl);
        assertNotificationClient(client);
    }

    @Test
    public void testCreateNotificationClient_withServiceAndApiKeyAndProxy() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));
        NotificationClient client = new NotificationClient(serviceUUID, apiKey, baseUrl, proxy);
        assertNotificationWithProxy(proxy, client);
    }

    @Test
    public void testCreateNotification_withServiceAndApiKeyAndSetsUserAgent() {
        NotificationClient client = new NotificationClient(serviceUUID, apiKey, baseUrl);
        assertTrue(client.getUserAgent().contains("NOTIFY-API-JAVA-CLIENT/"));
        assertTrue(client.getUserAgent().contains(client.getVersion()));
    }

    @Test
    public void testCreateNotificationClient_withServiceAndApiKeyAndSSLContext() throws NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getDefault();
        NotificationClient client = new NotificationClient(serviceUUID, apiKey, baseUrl, null, sslContext);
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

    @Test
    public void sendPrecompiledLetterBase64EncodedPDFFileIsNull() {
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);

        assertThrows(NotificationClientException.class, () -> {
            client.sendPrecompiledLetter("reference", null);
        });
    }

    @Test
    public void testSendPrecompiledLetterNotPDF() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("not_a_pdf.txt").getFile());
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);

        assertThrows(NotificationClientException.class, () -> {
            client.sendPrecompiledLetter("reference", file);
        });
    }

    @Test
    public void testPrepareUpload() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();
        JsonObject response = NotificationClient.prepareUpload(documentContent);
        JsonObject expectedResult = new JsonObject();
        expectedResult.addProperty("file", new String(Base64.encodeBase64(documentContent), ISO_8859_1));
        expectedResult.addProperty("is_csv", false);
        assertEquals(expectedResult.get("file").getAsString(), response.get("file").getAsString());
        assertEquals(expectedResult.get("is_csv").getAsBoolean(), response.get("is_csv").getAsBoolean());
    }

    @Test
    public void testPrepareUploadForCSV() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();
        JsonObject response = NotificationClient.prepareUpload(documentContent, true);
        JsonObject expectedResult = new JsonObject();
        expectedResult.addProperty("file", new String(Base64.encodeBase64(documentContent), ISO_8859_1));
        expectedResult.addProperty("is_csv", true);
        assertEquals(expectedResult.get("file").getAsString(), response.get("file").getAsString());
        assertEquals(expectedResult.get("is_csv").getAsString(), response.get("is_csv").getAsString());
    }

    @Test
    public void testPrepareUploadThrowsExceptionWhenExceeds2MB() {
        char[] data = new char[(2*1024*1024)+50];
        byte[] documentContents = new String(data).getBytes();

        try {
            NotificationClient.prepareUpload(documentContents);
        }catch(NotificationClientException e){
            assertEquals(e.getHttpResult(), 413);
            assertEquals(e.getMessage(), "Status code: 413 File is larger than 2MB");
        }
    }

    @Test
    public void testShouldThrowNotificationExceptionOnErrorResponseCodeAndNoErrorStream() throws Exception {
        NotificationClient client = Mockito.spy(new NotificationClient(combinedApiKey, baseUrl));
        doReturn(mockConnection(404)).when(client).getConnection(any());

        assertThrows(NotificationClientException.class, () -> {
            client.sendSms(new SmsRequest.Builder()
                    .withTemplateId("aTemplateId")
                    .withPhoneNumber("aPhoneNumber")
                    .withPersonalisation(emptyMap())
                    .withReference("aReference")
                    .withBillingCode("aBillingCode")
                    .build()
            );
        });
    }

    private HttpURLConnection mockConnection(int statusCode) throws Exception {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(statusCode);
        return mockConnection;
    }
}
