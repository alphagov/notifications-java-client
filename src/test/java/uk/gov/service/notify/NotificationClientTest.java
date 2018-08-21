package uk.gov.service.notify;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        assertEquals(client.getUserAgent(), "NOTIFY-API-JAVA-CLIENT/3.10.0-RELEASE");
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
    public void sendPrecompiledLetterNotPDF() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("not_a_pdf.txt").getFile());
        NotificationClient client = new NotificationClient(combinedApiKey, baseUrl);
        client.sendPrecompiledLetter("reference", file);
    }
}
