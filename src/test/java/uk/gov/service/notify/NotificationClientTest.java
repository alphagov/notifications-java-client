package uk.gov.service.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.service.notify.domain.NotifyEmailRequest;
import uk.gov.service.notify.domain.NotifyEmailResponse;
import uk.gov.service.notify.domain.NotifyLetterRequest;
import uk.gov.service.notify.domain.NotifyLetterResponse;
import uk.gov.service.notify.domain.NotifyNotificationListResponse;
import uk.gov.service.notify.domain.NotifyNotificationResponse;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterRequest;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterResponse;
import uk.gov.service.notify.domain.NotifyReceivedTextMessagesResponse;
import uk.gov.service.notify.domain.NotifySmsRequest;
import uk.gov.service.notify.domain.NotifySmsResponse;
import uk.gov.service.notify.domain.NotifyTemplate;
import uk.gov.service.notify.domain.NotifyTemplateListResponse;
import uk.gov.service.notify.domain.NotifyTemplatePreviewRequest;
import uk.gov.service.notify.domain.NotifyTemplatePreviewResponse;

import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NotificationClientTest {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            // the following two lines are needed to (de)serialize ZonedDateTime in ISO8601 format
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private static final String SERVICE_ID = UUID.randomUUID().toString();
    private static final String API_KEY = UUID.randomUUID().toString();
    private static final String COMBINED_API_KEY = "Api_key_name-" + SERVICE_ID + "-" + API_KEY;
    private String BASE_URL;

    /**
     * A new wireMockRule is created for every test case (as opposed to using @ClassRule) which would use only one
     */
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Before
    public void beforeEachTest() {
        wireMockRule.start();
        this.BASE_URL = "http://localhost:" + wireMockRule.port();
    }

    @After
    public void afterEachTest() {
        wireMockRule.shutdown();
    }

    @Test
    public void testCreateNotificationClient_withSingleApiKeyAndBaseUrl() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        assertNotificationClient(client);
    }

    @Test
    public void testCreateNotificationClient_withSingleApiKeyAndProxy() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));

        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL, proxy);

        assertNotificationWithProxy(proxy, client);
    }

    @Test
    public void testCreateNotificationClient_withSingleApiKeyServiceIdAndProxy() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));

        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL, proxy);

        assertNotificationWithProxy(proxy, client);
    }

    @Test
    public void testCreateNotificationClientSetsUserAgent() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        assertTrue(client.getUserAgent().startsWith("NOTIFY-API-JAVA-CLIENT/"));
        assertTrue(client.getUserAgent().endsWith("-RELEASE"));
    }

    @Test
    public void testCreateNotificationClient_withSSLContext() throws NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getDefault();

        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL, null, sslContext);

        assertNotificationClient(client);
    }

    private void assertNotificationWithProxy(Proxy proxy, NotificationClient client) {
        assertEquals(client.getApiKey(), API_KEY);
        assertEquals(client.getServiceId(), SERVICE_ID);
        assertEquals(client.getBaseUrl(), BASE_URL);
        assertEquals(client.getProxy(), proxy);
    }

    private void assertNotificationClient(final NotificationClient client) {
        assertEquals(client.getApiKey(), API_KEY);
        assertEquals(client.getServiceId(), SERVICE_ID);
        assertEquals(client.getBaseUrl(), BASE_URL);
        assertNull(client.getProxy());
    }

    @Test
    public void sendPrecompiledLetterBlankReferenceErrors() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        final File pdfFile = new File(this.getClass().getClassLoader().getResource("one_page_pdf.pdf").getFile());

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter(null, pdfFile));

        assertEquals("reference cannot be null or empty", e.getMessage());
    }

    @Test
    public void sendPrecompiledLetterEmptyFileErrors() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        final File pdfFile = new File(this.getClass().getClassLoader().getResource("empty_file.pdf").getFile());

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter("a reference", pdfFile));

        assertEquals("precompiledPDF cannot be null or empty", e.getMessage());
    }

    @Test
    public void sendPrecompiledLetterBase64EncodedPDFFileIsNull() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetterWithInputStream("reference", null));

        assertEquals("Input stream cannot be null", e.getMessage());
    }

    @Test
    public void sendPrecompiledLetterInputStreamIsNull() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter("reference", null));

        assertEquals("File cannot be null", e.getMessage());
    }

    @Test
    public void testSendPrecompiledLetterNotPDF() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("not_a_pdf.txt").getFile());
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter("reference", file));

        assertEquals("base64EncodedPDFFile is not a PDF", e.getMessage());
    }

    @Test
    public void testPrepareUpload() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = NotificationClient.prepareUpload(documentContent);

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertNull(response.optJSONObject("filename"));
        assertNull(response.optJSONObject("confirm_email_before_download"));
        assertNull(response.optJSONObject("retention_period"));
    }

    @Test
    public void testPrepareUploadWithFilename() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = NotificationClient.prepareUpload(documentContent, "report.csv");

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertEquals("report.csv", response.getString("filename"));
        assertNull(response.optJSONObject("confirm_email_before_download"));
        assertNull(response.optJSONObject("retention_period"));
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodString() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = NotificationClient.prepareUpload(
                documentContent,
                "report.csv",
                true,
                "1 weeks");

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertEquals("report.csv", response.getString("filename"));
        assertTrue(response.getBoolean("confirm_email_before_download"));
        assertEquals("1 weeks", response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadWithFilenameAndEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = NotificationClient.prepareUpload(
                documentContent,
                "report.csv",
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertEquals("report.csv", response.getString("filename"));
        assertTrue(response.getBoolean("confirm_email_before_download"));
        assertEquals("1 weeks", response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = NotificationClient.prepareUpload(
                documentContent,
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertNull(response.optJSONObject("filename"));
        assertTrue(response.getBoolean("confirm_email_before_download"));
        assertEquals("1 weeks", response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadThrowsExceptionWhenExceeds2MB() {
        char[] data = new char[(2 * 1024 * 1024) + 50];
        byte[] documentContents = new String(data).getBytes();

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> NotificationClient.prepareUpload(documentContents));

        assertEquals(e.getHttpResult(), 413);
        assertEquals(e.getMessage(), "Status code: 413 File is larger than 2MB");
    }

    @Test
    public void testShouldThrowNotificationExceptionOnErrorResponseCodeAndNoErrorStream() {
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(notFound()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendSms("aTemplateId", "aPhoneNumber", emptyMap(), "aReference"));

        assertEquals(404, e.getHttpResult());
        assertEquals("Status code: 404 ", e.getMessage());

        validateRequest();
    }

    @Test
    public void testSendEmailHandlesErrors() {
        wireMockRule.stubFor(post("/v2/notifications/email")
                .willReturn(serverError()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendEmail("aTemplateId", "anEmailAddress", emptyMap(), "aReference", "an emailReplyToId"));

        assertEquals(500, e.getHttpResult());
        assertEquals("Status code: 500 ", e.getMessage());

        validateRequest();
    }

    @Test
    public void testSendEmailWithoutUnsubscribeURL() throws NotificationClientException, IOException {
        NotifyEmailResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_email_response.json"), NotifyEmailResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/email")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        // setting up this map can be replaced with Map.of() in later Java versions
        Map<String, Object> personalisation = new HashMap<>();
        personalisation.put("application_date", "2018-01-01");

        SendEmailResponse actual = client.sendEmail("aTemplateId", "anEmailAddress", personalisation, "aReference", "an emailReplyToId");

        assertEquals(expected.getNotificationId(), actual.getNotificationId());
        assertEquals(expected.getReference(), actual.getReference().get());
        assertEquals(expected.getContent().getBody(), actual.getBody());
        assertEquals(expected.getContent().getSubject(), actual.getSubject());
        assertEquals(expected.getContent().getFromEmail(), actual.getFromEmail().get());
        // No notification uri in SendEmailResponse?
//        assertEquals(expected.getUri(), actual.getUri());
        assertEquals(expected.getTemplate().getId(), actual.getTemplateId());
        assertEquals(expected.getTemplate().getVersion(), actual.getTemplateVersion());
        assertEquals(expected.getTemplate().getUri().toString(), actual.getTemplateUri());
        assertEquals(Optional.empty(), actual.getOneClickUnsubscribeURL());

        LoggedRequest request = validateRequest();
        NotifyEmailRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyEmailRequest.class);
        assertEquals("anEmailAddress", requestReceivedByNotifyApi.getEmailAddress());
        assertEquals("aTemplateId", requestReceivedByNotifyApi.getTemplateId());
        assertEquals(personalisation, requestReceivedByNotifyApi.getPersonalisation());
        assertEquals("aReference", requestReceivedByNotifyApi.getReference());
        assertEquals("an emailReplyToId", requestReceivedByNotifyApi.getEmailReplyToId());
        assertNull(requestReceivedByNotifyApi.getOneClickUnsubscribeURL());
    }

    @Test
    public void testSendEmailWithUnsubscribeURL() throws NotificationClientException, IOException {
        NotifyEmailResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_email_unsubscribe_response.json"), NotifyEmailResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/email")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        // setting up this map can be replaced with Map.of() in later Java versions
        Map<String, Object> personalisation = new HashMap<>();
        personalisation.put("application_date", "2018-01-01");
        URI oneClickUnsubscribeURL = URI.create("http://localhost/unsubscribe");

        SendEmailResponse actual = client.sendEmail("aTemplateId", "anEmailAddress", personalisation, "aReference", "an emailReplyToId", oneClickUnsubscribeURL);

        assertEquals(expected.getNotificationId(), actual.getNotificationId());
        assertEquals(expected.getReference(), actual.getReference().get());
        assertEquals(expected.getContent().getBody(), actual.getBody());
        assertEquals(expected.getContent().getSubject(), actual.getSubject());
        assertEquals(expected.getContent().getFromEmail(), actual.getFromEmail().get());
        // No notification uri in SendEmailResponse?
//        assertEquals(expected.getUri(), actual.getUri());
        assertEquals(expected.getTemplate().getId(), actual.getTemplateId());
        assertEquals(expected.getTemplate().getVersion(), actual.getTemplateVersion());
        assertEquals(expected.getTemplate().getUri().toString(), actual.getTemplateUri());
        assertEquals(expected.getOneClickUnsubscribeURL(), actual.getOneClickUnsubscribeURL().get());

        LoggedRequest request = validateRequest();
        NotifyEmailRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyEmailRequest.class);
        assertEquals("anEmailAddress", requestReceivedByNotifyApi.getEmailAddress());
        assertEquals("aTemplateId", requestReceivedByNotifyApi.getTemplateId());
        assertEquals(personalisation, requestReceivedByNotifyApi.getPersonalisation());
        assertEquals("aReference", requestReceivedByNotifyApi.getReference());
        assertEquals("an emailReplyToId", requestReceivedByNotifyApi.getEmailReplyToId());
        assertEquals(oneClickUnsubscribeURL, requestReceivedByNotifyApi.getOneClickUnsubscribeURL());
    }

    @Test
    public void testSendSmsHandlesErrors() {
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(serverError()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendSms("aTemplateId", "a phone number", emptyMap(), "aReference", "an smsSenderId"));

        assertEquals(500, e.getHttpResult());
        assertEquals("Status code: 500 ", e.getMessage());

        validateRequest();
    }

    @Test
    public void testSendSms() throws IOException, NotificationClientException {
        NotifySmsResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_sms_response.json"), NotifySmsResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        // setting up this map can be replaced with Map.of() in later Java versions
        Map<String, Object> personalisation = new HashMap<>();
        personalisation.put("application_date", "2018-01-01");

        SendSmsResponse actual = client.sendSms("aTemplateId", "a phone number", personalisation, "aReference", "an smsSenderId");

        assertEquals(expected.getNotificationId(), actual.getNotificationId());
        assertEquals(expected.getReference(), actual.getReference().get());
        assertEquals(expected.getContent().getBody(), actual.getBody());
        assertEquals(expected.getContent().getFromNumber(), actual.getFromNumber().get());
        // No notification uri in SendSmsResponse?
//        assertEquals(expected.getUri(), actual.getUri());
        assertEquals(expected.getTemplate().getId(), actual.getTemplateId());
        assertEquals(expected.getTemplate().getVersion(), actual.getTemplateVersion());
        assertEquals(expected.getTemplate().getUri().toString(), actual.getTemplateUri());

        LoggedRequest request = validateRequest();
        NotifySmsRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifySmsRequest.class);
        assertEquals("a phone number", requestReceivedByNotifyApi.getPhoneNumber());
        assertEquals("aTemplateId", requestReceivedByNotifyApi.getTemplateId());
        assertEquals(personalisation, requestReceivedByNotifyApi.getPersonalisation());
        assertEquals("aReference", requestReceivedByNotifyApi.getReference());
        assertEquals("an smsSenderId", requestReceivedByNotifyApi.getSmsSenderId());
    }

    @Test
    public void testSendLetterHandlesErrors() {
        wireMockRule.stubFor(post("/v2/notifications/letter")
                .willReturn(serverError()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        // this can all be replaced with Map.of() in later Java versions
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("address_line_1", "a1");
        personalisation.put("address_line_2", "a2");
        personalisation.put("address_line_3", "a3");

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendLetter("aTemplateId", personalisation, "aReference"));

        assertEquals(500, e.getHttpResult());
        assertEquals("Status code: 500 ", e.getMessage());

        validateRequest();
    }

    @Test
    public void testSendLetter() throws IOException, NotificationClientException {
        NotifyLetterResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_letter_response.json"), NotifyLetterResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/letter")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        // this can all be replaced with Map.of() in later Java versions
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("address_line_1", "a1");
        personalisation.put("address_line_2", "a2");
        personalisation.put("address_line_3", "a3");

        SendLetterResponse actual = client.sendLetter("aTemplateId", personalisation, "aReference");

        assertEquals(expected.getNotificationId(), actual.getNotificationId());
        assertEquals(expected.getReference(), actual.getReference().get());
        assertEquals(expected.getContent().getBody(), actual.getBody());
        assertEquals(expected.getContent().getSubject(), actual.getSubject());
        // No notification uri in SendLetterResponse?
//        assertEquals(expected.getUri(), actual.getUri());
        assertEquals(expected.getTemplate().getId(), actual.getTemplateId());
        assertEquals(expected.getTemplate().getVersion(), actual.getTemplateVersion());
        assertEquals(expected.getTemplate().getUri().toString(), actual.getTemplateUri());
        // no scheduled for in SendLetterResponse?
//        assertEquals(expected.getScheduledFor(), actual.getScheduledFor());

        LoggedRequest request = validateRequest();
        NotifyLetterRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyLetterRequest.class);
        assertEquals("aTemplateId", requestReceivedByNotifyApi.getTemplateId());
        assertEquals(personalisation, requestReceivedByNotifyApi.getPersonalisation());
        assertEquals("aReference", requestReceivedByNotifyApi.getReference());
    }

    @Test
    public void testGetNotificationById() throws IOException, NotificationClientException {
        final UUID notificationId = UUID.randomUUID();
        NotifyNotificationResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_byid_response.json"), NotifyNotificationResponse.class);
        wireMockRule.stubFor(get("/v2/notifications/" + notificationId)
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        Notification actual = client.getNotificationById(notificationId.toString());

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getReference(), actual.getReference().get());
        assertEquals(expected.getEmailAddress(), actual.getEmailAddress().get());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber().get());
        assertEquals(expected.getLine1(), actual.getLine1().get());
        assertEquals(expected.getLine2(), actual.getLine2().get());
        assertEquals(expected.getLine3(), actual.getLine3().get());
        assertEquals(expected.getLine4(), actual.getLine4().get());
        assertEquals(expected.getLine5(), actual.getLine5().get());
        assertEquals(expected.getLine6(), actual.getLine6().get());
        // currently this client doesn't handle line 7
//        assertEquals(expected.getLine7(), actual.getLine7().get());
        assertEquals(expected.getType(), actual.getNotificationType());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getTemplate().getId(), actual.getTemplateId());
        assertEquals(expected.getTemplate().getVersion(), actual.getTemplateVersion());
        assertEquals(expected.getTemplate().getUri().toString(), actual.getTemplateUri());
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getSubject(), actual.getSubject().get());
        assertTrue(expected.getCreatedAt().isEqual(actual.getCreatedAt()));
        assertEquals(expected.getCreatedByName(), actual.getCreatedByName().get());
        assertTrue(expected.getSentAt().isEqual(actual.getSentAt().get()));
        assertTrue(expected.getCompletedAt().isEqual(actual.getCompletedAt().get()));

        validateRequest();
    }

    @Test
    public void testGetNotifications() throws IOException, NotificationClientException {
        NotifyNotificationListResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_response.json"), NotifyNotificationListResponse.class);
        NotifyNotificationResponse expectedNotification = expected.getNotifications().get(0);
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/notifications"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationList actual = client.getNotifications("a stat", "a notification_type", "a ref", "an olderThanId");

        assertEquals(expected.getNotifications().size(), actual.getNotifications().size());
        assertEquals(expected.getLinks().getCurrent().toString(), actual.getCurrentPageLink());
        assertEquals(expected.getLinks().getNext().toString(), actual.getNextPageLink().get());

        Notification actualNotification = actual.getNotifications().get(0);
        assertEquals(expectedNotification.getId(), actualNotification.getId());
        assertEquals(expectedNotification.getReference(), actualNotification.getReference().get());
        assertEquals(expectedNotification.getEmailAddress(), actualNotification.getEmailAddress().get());
        assertEquals(expectedNotification.getPhoneNumber(), actualNotification.getPhoneNumber().get());
        assertEquals(expectedNotification.getLine1(), actualNotification.getLine1().get());
        assertEquals(expectedNotification.getLine2(), actualNotification.getLine2().get());
        assertEquals(expectedNotification.getLine3(), actualNotification.getLine3().get());
        assertEquals(expectedNotification.getLine4(), actualNotification.getLine4().get());
        assertEquals(expectedNotification.getLine5(), actualNotification.getLine5().get());
        assertEquals(expectedNotification.getLine6(), actualNotification.getLine6().get());
        // currently this client doesn't handle line 7
//        assertEquals(expectedNotification.getLine7(), actualNotification.getLine7().get());
        assertEquals(expectedNotification.getType(), actualNotification.getNotificationType());
        assertEquals(expectedNotification.getStatus(), actualNotification.getStatus());
        assertEquals(expectedNotification.getTemplate().getId(), actualNotification.getTemplateId());
        assertEquals(expectedNotification.getTemplate().getVersion(), actualNotification.getTemplateVersion());
        assertEquals(expectedNotification.getTemplate().getUri().toString(), actualNotification.getTemplateUri());
        assertEquals(expectedNotification.getBody(), actualNotification.getBody());
        assertEquals(expectedNotification.getSubject(), actualNotification.getSubject().get());
        assertTrue(expectedNotification.getCreatedAt().isEqual(actualNotification.getCreatedAt()));
        assertEquals(expectedNotification.getCreatedByName(), actualNotification.getCreatedByName().get());
        assertTrue(expectedNotification.getSentAt().isEqual(actualNotification.getSentAt().get()));
        assertTrue(expectedNotification.getCompletedAt().isEqual(actualNotification.getCompletedAt().get()));

        LoggedRequest request = validateRequest();
        assertEquals("a stat", request.queryParameter("status").firstValue());
        assertEquals("a notification_type", request.queryParameter("template_type").firstValue());
        assertEquals("a ref", request.queryParameter("reference").firstValue());
        assertEquals("an olderThanId", request.queryParameter("older_than").firstValue());
    }

    @Test
    public void testGetPdfForLetter() throws NotificationClientException, IOException {
        final UUID notificationId = UUID.randomUUID();
        byte[] pdfFile = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("one_page_pdf.pdf"));
        wireMockRule.stubFor(get("/v2/notifications/" + notificationId + "/pdf")
                .willReturn(ok()
                        .withResponseBody(new Body(pdfFile))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        byte[] responsePdf = client.getPdfForLetter(notificationId.toString());

        assertArrayEquals(pdfFile, responsePdf);

        validateRequest();
    }

    @Test
    public void testSendPrecompiledLetter() throws IOException, NotificationClientException {
        NotifyPrecompiledLetterResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_precompiled_letter_response.json"), NotifyPrecompiledLetterResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/letter")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        final File pdfFile = new File(this.getClass().getClassLoader().getResource("one_page_pdf.pdf").getFile());

        LetterResponse actual = client.sendPrecompiledLetter(expected.getReference(), pdfFile, expected.getPostage());

        assertEquals(expected.getId(), actual.getNotificationId());
        assertEquals(expected.getReference(), actual.getReference().get());
        assertEquals(expected.getPostage(), actual.getPostage().get());

        LoggedRequest request = validateRequest();
        NotifyPrecompiledLetterRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyPrecompiledLetterRequest.class);
        assertEquals("your-letter-reference", requestReceivedByNotifyApi.getReference());
        // Is it worth checking that the pdf is sent to Notify correctly?
//        assertEquals("base 64 pdf", requestReceivedByNotifyApi.getContent());
        assertEquals("postage-you-have-set-or-None", requestReceivedByNotifyApi.getPostage());
    }

    @Test
    public void testGetTemplateById() throws IOException, NotificationClientException {
        NotifyTemplate expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_byid_response.json"), NotifyTemplate.class);
        UUID templateId = expected.getId();
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/template/" + templateId))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        Template actual = client.getTemplateById(templateId.toString());

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getType(), actual.getTemplateType());
        assertTrue(expected.getCreatedAt().isEqual(actual.getCreatedAt()));
        assertTrue(expected.getUpdatedAt().isEqual(actual.getUpdatedAt().get()));
        assertEquals(expected.getVersion(), actual.getVersion());
        // actual created by is null
//        assertEquals(expected.getCreatedBy(), actual.getCreatedBy());
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getSubject(), actual.getSubject().get());
        assertEquals(expected.getLetterContactBlock(), actual.getLetterContactBlock().get());

        validateRequest();
    }

    @Test
    public void testGetTemplateVersion() throws IOException, NotificationClientException {
        NotifyTemplate expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_byid_response.json"), NotifyTemplate.class);
        UUID templateId = expected.getId();
        final int version = 100;
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/template/" + templateId + "/version/" + version))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        Template actual = client.getTemplateVersion(templateId.toString(), 100);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getType(), actual.getTemplateType());
        assertTrue(expected.getCreatedAt().isEqual(actual.getCreatedAt()));
        assertTrue(expected.getUpdatedAt().isEqual(actual.getUpdatedAt().get()));
        assertEquals(expected.getVersion(), actual.getVersion());
        // actual created by is null
//        assertEquals(expected.getCreatedBy(), actual.getCreatedBy());
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getSubject(), actual.getSubject().get());
        assertEquals(expected.getLetterContactBlock(), actual.getLetterContactBlock().get());

        validateRequest();
    }

    @Test
    public void testGetAllTemplates() throws IOException, NotificationClientException {
        NotifyTemplate template = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_byid_response.json"), NotifyTemplate.class);
        NotifyTemplateListResponse expected = new NotifyTemplateListResponse(Collections.singletonList(template));
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/templates"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        // we ask for the "foo" templates because as far as the client is concerned the string doesn't matter
        TemplateList actual = client.getAllTemplates("foo");

        assertEquals(expected.getTemplates().size(), actual.getTemplates().size());
        assertEquals(expected.getTemplates().get(0).getId(), actual.getTemplates().get(0).getId());
        assertEquals(expected.getTemplates().get(0).getName(), actual.getTemplates().get(0).getName());
        assertEquals(expected.getTemplates().get(0).getType(), actual.getTemplates().get(0).getTemplateType());
        assertTrue(expected.getTemplates().get(0).getCreatedAt().isEqual(actual.getTemplates().get(0).getCreatedAt()));
        assertTrue(expected.getTemplates().get(0).getUpdatedAt().isEqual(actual.getTemplates().get(0).getUpdatedAt().get()));
        assertEquals(expected.getTemplates().get(0).getVersion(), actual.getTemplates().get(0).getVersion());
        // actual created by is null
//        assertEquals(expected.getTemplates().get(0).getCreatedBy(), actual.getTemplates().get(0).getCreatedBy());
        assertEquals(expected.getTemplates().get(0).getBody(), actual.getTemplates().get(0).getBody());
        assertEquals(expected.getTemplates().get(0).getSubject(), actual.getTemplates().get(0).getSubject().get());
        assertEquals(expected.getTemplates().get(0).getLetterContactBlock(), actual.getTemplates().get(0).getLetterContactBlock().get());

        LoggedRequest request = validateRequest();
        assertEquals("foo", request.queryParameter("type").firstValue());
    }

    @Test
    public void testGeneratePreviewTemplate() throws IOException, NotificationClientException {
        NotifyTemplatePreviewResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_preview_response.json"), NotifyTemplatePreviewResponse.class);
        UUID templateId = expected.getId();
        // setting up this map can be replaced with Map.of() in later Java versions
        Map<String, Object> personalisation = new HashMap<>();
        personalisation.put("application_date", "2018-01-01");
        wireMockRule.stubFor(post(urlPathEqualTo("/v2/template/" + templateId + "/preview"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        TemplatePreview actual = client.generateTemplatePreview(templateId.toString(), personalisation);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getType(), actual.getTemplateType());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getSubject(), actual.getSubject().get());

        LoggedRequest request = validateRequest();
        NotifyTemplatePreviewRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyTemplatePreviewRequest.class);
        assertEquals(personalisation, requestReceivedByNotifyApi.getPersonalisation());
    }

    @Test
    public void testGetReceivedTextMessages() throws IOException, NotificationClientException {
        NotifyReceivedTextMessagesResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_received-text-messages_response.json"), NotifyReceivedTextMessagesResponse.class);
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/received-text-messages"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        ReceivedTextMessageList actual = client.getReceivedTextMessages("2024-05-13");

        assertEquals(expected.getReceivedTextMessages().size(), actual.getReceivedTextMessages().size());
        assertEquals(expected.getReceivedTextMessages().get(0).getId(), actual.getReceivedTextMessages().get(0).getId());
        assertTrue(expected.getReceivedTextMessages().get(0).getCreatedAt().isEqual(actual.getReceivedTextMessages().get(0).getCreatedAt()));
        // api docs say service_id is a string and doesn't specify that it's a UUID (however current API here in main specifies that)
        assertEquals(expected.getReceivedTextMessages().get(0).getServiceId(), actual.getReceivedTextMessages().get(0).getServiceId());
        assertEquals(expected.getReceivedTextMessages().get(0).getNotifyNumber(), actual.getReceivedTextMessages().get(0).getNotifyNumber());
        assertEquals(expected.getReceivedTextMessages().get(0).getUserNumber(), actual.getReceivedTextMessages().get(0).getUserNumber());
        assertEquals(expected.getReceivedTextMessages().get(0).getContent(), actual.getReceivedTextMessages().get(0).getContent());
        assertEquals(expected.getLinks().getCurrent().toString(), actual.getCurrentPageLink());
        assertEquals(expected.getLinks().getNext().toString(), actual.getNextPageLink().get());

        LoggedRequest request = validateRequest();
        assertEquals("2024-05-13", request.queryParameter("older_than").firstValue());
    }

    @Test
    public void testJsonParsingFailsWithUnknownValue() {
        // this is testing that we have the object mapper configured correctly to help during development
        UnrecognizedPropertyException e = assertThrows(UnrecognizedPropertyException.class,
                () -> objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_email_unknown_value_response.json"), NotifyEmailResponse.class));

        assertTrue(e.getMessage(), e.getMessage().contains("Unrecognized field \"something\""));
    }

    @Test
    public void testBadApiKeyWouldFail() {
        // not the best test as we're effectively performing a check as the server would
        // but it does allow us to check how the client responds
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(unauthorized()));
        final UUID badApiKey = UUID.randomUUID();
        NotificationClient client = new NotificationClient("Api_key_name-" + SERVICE_ID + "-" + badApiKey, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendSms("aTemplateId", "aPhoneNumber", emptyMap(), "aReference"));

        assertEquals(401, e.getHttpResult());
        assertEquals("Status code: 401 ", e.getMessage());

        List<LoggedRequest> requests = wireMockRule.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertEquals(1, requests.size());
        final String requestUserAgent = requests.get(0).getHeader("User-Agent");
        assertEquals("NOTIFY-API-JAVA-CLIENT", requestUserAgent.substring(0, requestUserAgent.indexOf("/")));
        AssertionError invalidJwtException = assertThrows(AssertionError.class,
                () -> validateBearerTokenTest(requests.get(0).header("Authorization").firstValue().substring("Bearer ".length())));
        final String expectedExceptionMessage = "unable to validate jwt: org.jose4j.jwt.consumer.InvalidJwtSignatureException: JWT rejected due to invalid signature";
        assertEquals(invalidJwtException.getMessage(), expectedExceptionMessage, invalidJwtException.getMessage().substring(0, expectedExceptionMessage.length()));
    }

    private void validateBearerToken(String token) throws InvalidJwtException {
        final int allowedSecondsInTheFuture = 60;
        final int allowedSecondsInThePast = 60;
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setExpectedIssuer(SERVICE_ID)
                .setIssuedAtRestrictions(allowedSecondsInTheFuture, allowedSecondsInThePast)
                // NOTE: COMBINED_API_KEY is passed to the API however the API_KEY is later taken from within that by the client
                .setVerificationKey(new SecretKeySpec(API_KEY.getBytes(StandardCharsets.UTF_8), "RAW"))
                .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256)
                .build();
        jwtConsumer.process(token);
    }

    private LoggedRequest validateRequest() {
        List<LoggedRequest> requests = wireMockRule.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertEquals(1, requests.size());
        final String requestUserAgent = requests.get(0).getHeader("User-Agent");
        assertEquals("NOTIFY-API-JAVA-CLIENT", requestUserAgent.substring(0, requestUserAgent.indexOf("/")));
        validateBearerTokenTest(requests.get(0).header("Authorization").firstValue().substring("Bearer ".length()));
        return requests.get(0);
    }

    private void validateBearerTokenTest(String token) {
        try {
            validateBearerToken(token);
        } catch (InvalidJwtException e) {
            fail("unable to validate jwt: " + e);
        }
    }

}
