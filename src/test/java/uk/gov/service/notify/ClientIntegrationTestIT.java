package uk.gov.service.notify;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClientIntegrationTestIT {

    @Test
    public void testEmailNotificationIT() throws NotificationClientException, InterruptedException {
        NotificationClient client = getClient();
        SendEmailResponse emailResponse = sendEmailAndAssertResponse(client);
        Notification notification = client.getNotificationById(emailResponse.getNotificationId().toString());
        assertNotification(notification);
    }

    @Test
    public void testSmsNotificationIT() throws NotificationClientException, InterruptedException {
        NotificationClient client = getClient();
        SendSmsResponse response = sendSmsAndAssertResponse(client);
        Notification notification = client.getNotificationById(response.getNotificationId().toString());
        assertNotification(notification);
    }

    @Test
    public void testLetterNotificationIT() throws NotificationClientException, InterruptedException {
        NotificationClient client = getClient();
        SendLetterResponse letterResponse = sendLetterAndAssertResponse(client);
        Notification notification = client.getNotificationById(letterResponse.getNotificationId().toString());
        assertNotification(notification);
    }


    @Test
    public void testGetAllNotifications() throws NotificationClientException {
        NotificationClient client = getClient();
        NotificationList notificationList = client.getNotifications(null, null, null, null);
        assertNotNull(notificationList);
        assertNotNull(notificationList.getNotifications());
        assertFalse(notificationList.getNotifications().isEmpty());
        // Just check the first notification in the list.
        assertNotification(notificationList.getNotifications().get(0));
        String baseUrl = System.getenv("NOTIFY_API_URL");
        assertEquals(baseUrl + "/v2/notifications", notificationList.getCurrentPageLink());
        if (notificationList.getNextPageLink().isPresent()){
            String nextUri = notificationList.getNextPageLink().get();
            String olderThanId = nextUri.substring(nextUri.indexOf("older_than=") + "other_than=".length());
            NotificationList nextList = client.getNotifications(null, null, null, olderThanId);
            assertNotNull(notificationList.getCurrentPageLink());
            assertNotNull(nextList);
            assertNotNull(nextList.getNotifications());
        }
    }

    @Test
    public void testEmailNotificationWithoutPersonalisationReturnsErrorMessageIT() {
        NotificationClient client = getClient();
        try {
            client.sendEmail(System.getenv("EMAIL_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_EMAIL"), null, null);
            fail("Expected NotificationClientException: Template missing personalisation: name");
        } catch (NotificationClientException e) {
            assert(e.getMessage().contains("Missing personalisation: name"));
            assert e.getHttpResult() == 400;
            assert(e.getMessage().contains("BadRequestError"));
        }
    }

    @Test
    public void testEmailNotificationWithValidEmailReplyToIdIT() throws NotificationClientException, InterruptedException {
        NotificationClient client = getClient();
        SendEmailResponse emailResponse = sendEmailAndAssertResponse(client);

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

        SendEmailResponse response = client.sendEmail(
                System.getenv("EMAIL_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_EMAIL"),
                personalisation,
                uniqueName,
                System.getenv("EMAIL_REPLY_TO_ID"));

        assertNotificationEmailResponse(response, uniqueName);

        Notification notification = client.getNotificationById(emailResponse.getNotificationId().toString());
        assertNotification(notification);
    }

    @Test
    public void testEmailNotificationWithInValidEmailReplyToIdIT() throws NotificationClientException, InterruptedException {
        NotificationClient client = getClient();
        SendEmailResponse emailResponse = sendEmailAndAssertResponse(client);

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

        UUID fake_uuid = UUID.randomUUID();

        boolean exceptionThrown = false;

        try
        {
            SendEmailResponse response = client.sendEmail(
                    System.getenv("EMAIL_TEMPLATE_ID"),
                    System.getenv("FUNCTIONAL_TEST_EMAIL"),
                    personalisation,
                    uniqueName,
                    fake_uuid.toString());
        }
        catch (final NotificationClientException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains("does not exist in database for service id"));
        }

        assertTrue(exceptionThrown);

    }

    @Test
    public void testSmsNotificationWithoutPersonalisationReturnsErrorMessageIT() {
        NotificationClient client = getClient();
        try {
            client.sendSms(System.getenv("SMS_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_NUMBER"), null, null);
            fail("Expected NotificationClientException: Template missing personalisation: name");
        } catch (NotificationClientException e) {
            assert(e.getMessage().contains("Missing personalisation: name"));
            assert(e.getMessage().contains("Status code: 400"));
        }
    }

    @Test
    public void testSmsNotificationWithValidSmsSenderIdIT() throws NotificationClientException, InterruptedException {
        NotificationClient client = getClient("API_SENDING_KEY");

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

        SendSmsResponse response = client.sendSms(
                System.getenv("SMS_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_NUMBER"),
                personalisation,
                uniqueName,
                System.getenv("SMS_SENDER_ID"));

        assertNotificationSmsResponse(response, uniqueName);

        Notification notification = client.getNotificationById(response.getNotificationId().toString());
        assertNotification(notification);
    }

    @Test
    public void testSmsNotificationWithInValidSmsSenderIdIT() throws NotificationClientException, InterruptedException {
        NotificationClient client = getClient();

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

        UUID fake_uuid = UUID.randomUUID();

        boolean exceptionThrown = false;

        try
        {
            SendSmsResponse response = client.sendSms(
                    System.getenv("SMS_TEMPLATE_ID"),
                    System.getenv("FUNCTIONAL_TEST_NUMBER"),
                    personalisation,
                    uniqueName,
                    fake_uuid.toString());
        }
        catch (final NotificationClientException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains("does not exist in database for service id"));
        }

        assertTrue(exceptionThrown);

    }

    @Test
    public void testSendAndGetNotificationWithReference() throws NotificationClientException {
        NotificationClient client = getClient();
        HashMap<String, String> personalisation = new HashMap<String, String>();
        String uniqueString = UUID.randomUUID().toString();
        personalisation.put("name", uniqueString);
        SendEmailResponse response = client.sendEmail(System.getenv("EMAIL_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_EMAIL"), personalisation, uniqueString);
        assertNotificationEmailResponse(response, uniqueString);
        NotificationList notifications = client.getNotifications(null, null, uniqueString, null);
        assertEquals(1, notifications.getNotifications().size());
        assertEquals(response.getNotificationId(), notifications.getNotifications().get(0).getId());
    }

    @Test
    public void testGetTemplateById() throws NotificationClientException {
        NotificationClient client = getClient();
        Template template = client.getTemplateById(System.getenv("EMAIL_TEMPLATE_ID"));
        assertEquals(System.getenv("EMAIL_TEMPLATE_ID"), template.getId().toString());
        assertNotNull(template.getVersion());
        assertNotNull(template.getCreatedAt());
        assertNotNull(template.getTemplateType());
        assertNotNull(template.getBody());
        assertNotNull(template.getSubject());
    }

    @Test
    public void testGetTemplateVersion() throws NotificationClientException {
        NotificationClient client = getClient();
        Template template = client.getTemplateVersion(System.getenv("SMS_TEMPLATE_ID"), 1);
        assertEquals(System.getenv("SMS_TEMPLATE_ID"), template.getId().toString());
        assertNotNull(template.getVersion());
        assertNotNull(template.getCreatedAt());
        assertNotNull(template.getTemplateType());
        assertNotNull(template.getBody());
    }

    @Test
    public void testGetAllTemplates() throws NotificationClientException {
        NotificationClient client = getClient();
        TemplateList templateList = client.getAllTemplates("");
        assertTrue(2 <= templateList.getTemplates().size());
    }

    @Test
    public void testGetTemplatePreview() throws NotificationClientException {
        NotificationClient client = getClient();
        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);
        TemplatePreview template = client.generateTemplatePreview(System.getenv("EMAIL_TEMPLATE_ID"), personalisation);
        assertEquals(System.getenv("EMAIL_TEMPLATE_ID"), template.getId().toString());
        assertNotNull(template.getVersion());
        assertNotNull(template.getTemplateType());
        assertNotNull(template.getBody());
        assertNotNull(template.getSubject());
        assertTrue(template.getBody().contains(uniqueName));
    }

    @Test
    public void testGetReceivedTextMessages() throws NotificationClientException {
        NotificationClient client = getClient("INBOUND_SMS_QUERY_KEY");

        ReceivedTextMessageList response = client.getReceivedTextMessages(null);
        ReceivedTextMessage receivedTextMessage = assertReceivedTextMessageList(response);

        testGetReceivedTextMessagesWithOlderThanId(receivedTextMessage.getId(), client);
    }

    @Test
    public void testSendPrecompiledLetterValidPDFFileIT() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        NotificationClient client = getClient();
        LetterResponse response =  client.sendPrecompiledLetter(reference, file);

        assertPrecompiledLetterResponse(reference, response);

    }

    @Test
    public void testSendPrecompiledLetterWithInputStream() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        InputStream stream = new FileInputStream(file);
        NotificationClient client = getClient();
        LetterResponse response =  client.sendPrecompiledLetterWithInputStream(reference, stream);

        assertPrecompiledLetterResponse(reference, response);

    }

    private ReceivedTextMessage assertReceivedTextMessageList(ReceivedTextMessageList response) {
        assertFalse(response.getReceivedTextMessages().isEmpty());
        assertNotNull(response.getCurrentPageLink());
        ReceivedTextMessage receivedTextMessage = response.getReceivedTextMessages().get(0);
        assertNotNull(receivedTextMessage.getId());
        assertNotNull(receivedTextMessage.getNotifyNumber());
        assertNotNull(receivedTextMessage.getUserNumber());
        assertNotNull(receivedTextMessage.getContent());
        assertNotNull(receivedTextMessage.getCreatedAt());
        assertNotNull(receivedTextMessage.getServiceId());
        return receivedTextMessage;
    }

    private void testGetReceivedTextMessagesWithOlderThanId(UUID id, NotificationClient client) throws NotificationClientException {
        ReceivedTextMessageList response = client.getReceivedTextMessages(id.toString());
        assertReceivedTextMessageList(response);
    }

    private NotificationClient getClient(){
        String apiKey = System.getenv("API_KEY");
        String baseUrl = System.getenv("NOTIFY_API_URL");
        return new NotificationClient(apiKey, baseUrl);
    }

    private NotificationClient getClient(String api_key){
        String apiKey = System.getenv(api_key);
        String baseUrl = System.getenv("NOTIFY_API_URL");
        return new NotificationClient(apiKey, baseUrl);
    }

    private SendEmailResponse sendEmailAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);
        SendEmailResponse response = client.sendEmail(System.getenv("EMAIL_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_EMAIL"), personalisation, uniqueName);
        assertNotificationEmailResponse(response, uniqueName);
        return response;
    }

    private SendSmsResponse sendSmsAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);
        SendSmsResponse response = client.sendSms(System.getenv("SMS_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_NUMBER"), personalisation, uniqueName);
        assertNotificationSmsResponse(response, uniqueName);
        return response;
    }

    private SendLetterResponse sendLetterAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        HashMap<String, String> personalisation = new HashMap<>();
        String addressLine1 = UUID.randomUUID().toString();
        String addressLine2 = UUID.randomUUID().toString();
        String postcode = UUID.randomUUID().toString();
        personalisation.put("address_line_1", addressLine1);
        personalisation.put("address_line_2", addressLine2);
        personalisation.put("postcode", postcode);
        SendLetterResponse response = client.sendLetter(System.getenv("LETTER_TEMPLATE_ID"), personalisation, addressLine1);
        assertNotificationLetterResponse(response, addressLine1);
        return response;
    }

    private void assertNotificationSmsResponse(final SendSmsResponse response, final String uniqueName){
        assertNotNull(response);
        assertTrue(response.getBody().contains(uniqueName));
        assertEquals(Optional.of(uniqueName), response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getTemplateVersion());
        assertNotNull(response.getTemplateId());
        assertNotNull(response.getTemplateUri());
        assertNotNull(response.getTemplateVersion());
    }

    private void assertNotificationEmailResponse(final SendEmailResponse response, final String uniqueName){
        assertNotNull(response);
        assertTrue(response.getBody().contains(uniqueName));
        assertEquals(Optional.of(uniqueName), response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getSubject());
        assertNotNull(response.getFromEmail().orElse(null));
        assertNotNull(response.getTemplateUri());
        assertNotNull(response.getTemplateId());
        assertNotNull(response.getTemplateVersion());
    }

    private void assertNotificationLetterResponse(final SendLetterResponse response, final String addressLine1){
        assertNotNull(response);
        assertTrue(response.getBody().contains(addressLine1));
        assertEquals(Optional.of(addressLine1), response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getTemplateVersion());
        assertNotNull(response.getTemplateId());
        assertNotNull(response.getTemplateUri());
        assertNotNull(response.getTemplateVersion());
    }

    private Notification assertNotification(Notification notification){
        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertNotNull(notification.getTemplateId());
        assertNotNull(notification.getTemplateVersion());
        assertNotNull(notification.getTemplateUri());
        assertNotNull(notification.getCreatedAt());
        assertNotNull(notification.getStatus());
        assertNotNull(notification.getNotificationType());
        assertFalse(notification.getCreatedByName().isPresent());
        if(notification.getNotificationType().equals("sms")) {
            assertNotificationWhenSms(notification);
        }
        if(notification.getNotificationType().equals("email")){
            assertNotificationWhenEmail(notification);
        }
        if(notification.getNotificationType().equals("letter")){
            assertNotificationWhenLetter(notification);
        }

        if(notification.getNotificationType().equals("letter")){
            assertTrue("expected status to be accepted or received", Arrays.asList("accepted", "received").contains(notification.getStatus()));
        } else {
            assertTrue("expected status to be created, sending or delivered", Arrays.asList("created", "sending", "delivered").contains(notification.getStatus()));
        }

        return notification;
    }

    private void assertNotificationWhenLetter(Notification notification) {
        assertTrue(notification.getLine1().isPresent());
        assertTrue(notification.getLine2().isPresent());
        assertTrue(notification.getPostcode().isPresent());
        // the other address lines are optional.
        assertFalse(notification.getEmailAddress().isPresent());
        assertFalse(notification.getPhoneNumber().isPresent());
    }

    private void assertNotificationWhenEmail(Notification notification) {
        assertTrue(notification.getSubject().isPresent());
        assertTrue(notification.getEmailAddress().isPresent());
        assertFalse(notification.getPhoneNumber().isPresent());
        assertFalse(notification.getLine1().isPresent());
        assertFalse(notification.getLine2().isPresent());
        assertFalse(notification.getLine3().isPresent());
        assertFalse(notification.getLine4().isPresent());
        assertFalse(notification.getLine5().isPresent());
        assertFalse(notification.getLine6().isPresent());
        assertFalse(notification.getPostcode().isPresent());
    }

    private void assertNotificationWhenSms(Notification notification) {
        assertTrue(notification.getPhoneNumber().isPresent());
        assertFalse(notification.getSubject().isPresent());
        assertFalse(notification.getEmailAddress().isPresent());
        assertFalse(notification.getLine1().isPresent());
        assertFalse(notification.getLine2().isPresent());
        assertFalse(notification.getLine3().isPresent());
        assertFalse(notification.getLine4().isPresent());
        assertFalse(notification.getLine5().isPresent());
        assertFalse(notification.getLine6().isPresent());
        assertFalse(notification.getPostcode().isPresent());
    }

    private void assertPrecompiledLetterResponse(String reference, LetterResponse response) {
        assertNotNull(response);
        assertNotNull(response.getNotificationId());
        assertEquals(response.getReference().get(), reference);
    }

}
