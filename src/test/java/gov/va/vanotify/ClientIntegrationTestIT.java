package gov.va.vanotify;

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ClientIntegrationTestIT {

    @Test
    public void testEmailNotificationIT() throws NotificationClientException {
        NotificationClient client = getClient();
        SendEmailResponse emailResponse = sendEmailAndAssertResponse(client);
        Notification notification = client.getNotificationById(emailResponse.getNotificationId().toString());
        assertNotification(notification);
    }

    @Disabled("The service does not have any SMS senders.")
    @Test
    public void testSmsNotificationIT() throws NotificationClientException {
        NotificationClient client = getClient();
        SendSmsResponse response = sendSmsAndAssertResponse(client);
        Notification notification = client.getNotificationById(response.getNotificationId().toString());
        assertNotification(notification);
    }

    @Disabled
    @Test
    public void testLetterNotificationIT() throws NotificationClientException {
        NotificationClient client = getClient();
        SendLetterResponse letterResponse = sendLetterAndAssertResponse(client);
        String notificationId = letterResponse.getNotificationId().toString();
        Notification notification = client.getNotificationById(notificationId);
        assertNotification(notification);
        assertPdfResponse(client, notificationId);
    }

    @Test
    public void sendPushAndAssertResponse() throws NotificationClientException {
        NotificationClient client = getClient();
        HashMap<String, Object> personalisation = new HashMap<>();
        String appointment_date = "12/02/2025";
        String appointment_time = "10:00";
        personalisation.put("appointment_date", appointment_date);
        personalisation.put("appointment_time", appointment_time);
        Identifier identifier = new Identifier(IdentifierType.ICN, "1012832025V743496");
        SendPushResponse response = client.sendPush(new PushRequest.Builder()
                .withTemplateId("1755CD4C33BB0851A416189616441067")
                .withRecipientIdentifier(identifier)
                .withPersonalisation(personalisation)
                .build()
        );
        assertEquals("success", response.getResult());
    }


    @Disabled("This test fails because the API Dev environment doesn't return any notifications.  This route is not exposed to users via Postman.")
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
        //TODO: reenable once department-of-veterans-affairs/notification-api/551 is done
        //assertEquals(baseUrl + "/v2/notifications", notificationList.getCurrentPageLink());
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
            client.sendEmail(new EmailRequest.Builder()
                    .withTemplateId(System.getenv("EMAIL_TEMPLATE_ID"))
                    .withEmailAddress(System.getenv("FUNCTIONAL_TEST_EMAIL"))
                    .build()
            );
            fail("Expected NotificationClientException: Template missing personalisation: name");
        } catch (NotificationClientException e) {
            assert(e.getMessage().contains("Missing personalisation: name"));
            assert e.getHttpResult() == 400;
            assert(e.getMessage().contains("BadRequestError"));
        }
    }

    @Disabled
    @Test
    public void testEmailNotificationWithUploadedDocumentInPersonalisation() throws NotificationClientException, IOException {
        NotificationClient client = getClient();
        HashMap<String, Object> personalisation = new HashMap<>();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        byte [] fileContents = FileUtils.readFileToByteArray(file);

        JsonObject documentFileObject = NotificationClient.prepareUpload(fileContents);
        personalisation.put("name", documentFileObject);

        String reference = UUID.randomUUID().toString();
        SendEmailResponse emailResponse = client.sendEmail(new EmailRequest.Builder()
                .withTemplateId(System.getenv("EMAIL_TEMPLATE_ID"))
                .withEmailAddress(System.getenv("FUNCTIONAL_TEST_EMAIL"))
                .withPersonalisation(personalisation)
                .withReference(reference)
                .build()
        );

        assertNotificationEmailResponseWithDocumentInPersonalisation(emailResponse, reference);
    }

    public static Object[][] emailWithRecipientIdentifierTestData() {
        return new Object[][]{
                {System.getenv("FUNCTIONAL_TEST_EMAIL")},
                {null}
        };
    }

    @ParameterizedTest
    @MethodSource("emailWithRecipientIdentifierTestData")
    public void testEmailNotificationWithRecipientIdentifier(String emailAddress) throws NotificationClientException {
        Identifier recipientIdentifier = new Identifier(IdentifierType.VAPROFILEID, UUID.randomUUID().toString());
        NotificationClient client = getClient();

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("name", "Bob");
        SendEmailResponse response = client.sendEmail(new EmailRequest.Builder()
                .withTemplateId(System.getenv("EMAIL_TEMPLATE_ID"))
                .withPersonalisation(personalisation)
                .withEmailAddress(emailAddress)
                .withRecipientIdentifier(recipientIdentifier)
                .build()
        );
        assertNotNull(response);
        assertNotNull(response.getNotificationId());

        Notification notification = client.getNotificationById(response.getNotificationId().toString());
        assertEquals(notification.getEmailAddress(), Optional.ofNullable(emailAddress));
        assertTrue(notification.getRecipientIdentifiers().contains(recipientIdentifier));
    }


    @Test
    public void testSmsNotificationWithoutPersonalisationReturnsErrorMessageIT() {
        NotificationClient client = getClient();
        try {
            client.sendSms(new SmsRequest.Builder()
                    .withTemplateId(System.getenv("SMS_TEMPLATE_ID"))
                    .withPhoneNumber(System.getenv("FUNCTIONAL_TEST_NUMBER"))
                    .build()
            );
            fail("Expected NotificationClientException: Template missing personalisation: name");
        } catch (NotificationClientException e) {
            assert(e.getMessage().contains("Missing personalisation: name"));
            assert(e.getMessage().contains("Status code: 400"));
        }
    }

    @Disabled("The service does not have any SMS senders.")
    @Test
    public void testSmsNotificationWithValidSmsSenderIdIT() throws NotificationClientException {
        NotificationClient client = getClient("API_SENDING_KEY");

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

        SendSmsResponse response = client.sendSms(new SmsRequest.Builder()
                    .withTemplateId(System.getenv("SMS_TEMPLATE_ID"))
                    .withPhoneNumber(System.getenv("FUNCTIONAL_TEST_NUMBER"))
                    .withPersonalisation(personalisation)
                    .withReference(uniqueName)
                    .withSmsSenderId(System.getenv("SMS_SENDER_ID"))
                    .build()
                );

        assertNotificationSmsResponse(response, uniqueName, null);

        Notification notification = client.getNotificationById(response.getNotificationId().toString());
        assertNotification(notification);
    }

    @Test
    public void testSmsNotificationWithInValidSmsSenderIdIT() {
        NotificationClient client = getClient();

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

        UUID fake_uuid = UUID.randomUUID();

        boolean exceptionThrown = false;

        try {
            client.sendSms(new SmsRequest.Builder()
                    .withTemplateId(System.getenv("SMS_TEMPLATE_ID"))
                    .withPhoneNumber(System.getenv("FUNCTIONAL_TEST_NUMBER"))
                    .withPersonalisation(personalisation)
                    .withReference(uniqueName)
                    .withSmsSenderId(fake_uuid.toString())
                    .build()
            );
        } catch (final NotificationClientException ex) {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains("does not exist in database for service id"), ex.getMessage());
        }

        assertTrue(exceptionThrown);
    }

    public static Object[][] smsWithRecipientIdentifierTestData() {
        return new Object[][]{
                {System.getenv("FUNCTIONAL_TEST_NUMBER")},
                {null}
        };
    }

    @Disabled("The service does not have any SMS senders.")
    @ParameterizedTest
    @MethodSource("smsWithRecipientIdentifierTestData")
    public void testSmsNotificationWithRecipientIdentifier(String phoneNumber) throws NotificationClientException {
        Identifier recipientIdentifier = new Identifier(IdentifierType.VAPROFILEID, UUID.randomUUID().toString());
        NotificationClient client = getClient();

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("name", "Bob");
        SendSmsResponse response = client.sendSms(new SmsRequest.Builder()
                .withTemplateId(System.getenv("SMS_TEMPLATE_ID"))
                .withPersonalisation(personalisation)
                .withPhoneNumber(phoneNumber)
                .withRecipientIdentifier(recipientIdentifier)
                .build()
        );
        assertNotNull(response);
        assertNotNull(response.getNotificationId());

        Notification notification = client.getNotificationById(response.getNotificationId().toString());
        assertEquals(notification.getPhoneNumber(), Optional.ofNullable(phoneNumber));
        assertTrue(notification.getRecipientIdentifiers().contains(recipientIdentifier));
    }

    @Test
    public void testSendAndGetNotificationWithReference() throws NotificationClientException {
        NotificationClient client = getClient();
        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueString = UUID.randomUUID().toString();
        String billingCode = "Client Integration Test";
        personalisation.put("name", uniqueString);
        SendEmailResponse response = client.sendEmail(new EmailRequest.Builder()
                        .withTemplateId(System.getenv("EMAIL_TEMPLATE_ID"))
                        .withEmailAddress(System.getenv("FUNCTIONAL_TEST_EMAIL"))
                        .withPersonalisation(personalisation)
                        .withReference(uniqueString)
                        .withBillingCode(billingCode)
                        .build()
        );
        assertNotificationEmailResponse(response, uniqueString, billingCode);
        NotificationList notifications = client.getNotifications(null, null, uniqueString, null);
        assertEquals(1, notifications.getNotifications().size());
        assertEquals(response.getNotificationId(), notifications.getNotifications().get(0).getId());
    }

    @Disabled
    @Test
    public void testGetTemplateById() throws NotificationClientException {
        NotificationClient client = getClient();
        Template template = client.getTemplateById(System.getenv("LETTER_TEMPLATE_ID"));
        assertEquals(System.getenv("LETTER_TEMPLATE_ID"), template.getId().toString());
        assertNotNull(template.getCreatedAt());
        assertNotNull(template.getTemplateType());
        assertNotNull(template.getBody());
        assertNotNull(template.getName());
        assertNotNull(template.getVersion());
        assertNotNull(template.getSubject());
        assertNotNull(template.getLetterContactBlock());
    }

    @Disabled
    @Test
    public void testGetReceivedTextMessages() throws NotificationClientException {
        NotificationClient client = getClient("INBOUND_SMS_QUERY_KEY");

        ReceivedTextMessageList response = client.getReceivedTextMessages(null);
        ReceivedTextMessage receivedTextMessage = assertReceivedTextMessageList(response);

        testGetReceivedTextMessagesWithOlderThanId(receivedTextMessage.getId(), client);
    }

    @Disabled
    @Test
    public void testSendPrecompiledLetterValidPDFFileIT() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        NotificationClient client = getClient();
        LetterResponse response =  client.sendPrecompiledLetter(reference, file);

        assertPrecompiledLetterResponse(reference, "second", response);
        assertPdfResponse(client, response.getNotificationId().toString());
    }

    @Disabled
    @Test
    public void testSendPrecompiledLetterValidPDFFileITWithPostage() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        NotificationClient client = getClient();
        LetterResponse response =  client.sendPrecompiledLetter(reference, file, "first");

        assertPrecompiledLetterResponse(reference, "first", response);

    }

    @Disabled
    @Test
    public void testSendPrecompiledLetterWithInputStream() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        InputStream stream = new FileInputStream(file);
        NotificationClient client = getClient();
        LetterResponse response =  client.sendPrecompiledLetterWithInputStream(reference, stream);

        assertPrecompiledLetterResponse(reference, "second", response);

    }

    @Disabled
    @Test
    public void testSendPrecompiledLetterWithInputStreamWithPostage() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        InputStream stream = new FileInputStream(file);
        NotificationClient client = getClient();
        LetterResponse response =  client.sendPrecompiledLetterWithInputStream(reference, stream, "first");

        assertPrecompiledLetterResponse(reference, "first", response);

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
        String billingCode = "Client Integration Test";
        personalisation.put("name", uniqueName);
        SendEmailResponse response = client.sendEmail(new EmailRequest.Builder()
                .withTemplateId(System.getenv("EMAIL_TEMPLATE_ID"))
                .withEmailAddress(System.getenv("FUNCTIONAL_TEST_EMAIL"))
                .withPersonalisation(personalisation)
                .withReference(uniqueName)
                .withBillingCode(billingCode)
                .build()
        );
        assertNotificationEmailResponse(response, uniqueName, billingCode);
        return response;
    }

    private SendSmsResponse sendSmsAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        HashMap<String, Object> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        String billingCode = "Client Integration Test";
        personalisation.put("name", uniqueName);
        SendSmsResponse response = client.sendSms(new SmsRequest.Builder()
                .withTemplateId(System.getenv("SMS_TEMPLATE_ID"))
                .withPhoneNumber(System.getenv("FUNCTIONAL_TEST_NUMBER"))
                .withPersonalisation(personalisation)
                .withReference(uniqueName)
                .withBillingCode(billingCode)
                .build()
        );
        assertNotificationSmsResponse(response, uniqueName, billingCode);
        return response;
    }
    
    private SendLetterResponse sendLetterAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        HashMap<String, String> personalisation = new HashMap<>();
        String addressLine1 = UUID.randomUUID().toString();
        String addressLine2 = UUID.randomUUID().toString();
        String postcode = "SW1 1AA";
        personalisation.put("address_line_1", addressLine1);
        personalisation.put("address_line_2", addressLine2);
        personalisation.put("postcode", postcode);
        SendLetterResponse response = client.sendLetter(System.getenv("LETTER_TEMPLATE_ID"), personalisation, addressLine1);
        assertNotificationLetterResponse(response, addressLine1);
        return response;
    }

    private void assertNotificationSmsResponse(final SendSmsResponse response, final String uniqueName, String billingCode){
        assertNotNull(response);
        assertTrue(response.getBody().contains(uniqueName));
        assertEquals(Optional.of(uniqueName), response.getReference());
        assertEquals(Optional.ofNullable(billingCode), response.getBillingCode());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getTemplateId());
        assertNotNull(response.getTemplateUri());
    }

    private void assertNotificationEmailResponse(final SendEmailResponse response, final String uniqueName, String billingCode){
        assertNotNull(response);
        assertEquals(Optional.of(uniqueName), response.getReference());
        assertEquals(Optional.ofNullable(billingCode), response.getBillingCode());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getSubject());
        assertNotNull(response.getTemplateUri());
        assertNotNull(response.getTemplateId());
    }

    private void assertNotificationEmailResponseWithDocumentInPersonalisation(final SendEmailResponse response, final String uniqueName){
        assertNotNull(response);
        assertTrue(response.getBody().contains("https://documents."));
        assertEquals(Optional.of(uniqueName), response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getSubject());
        assertNotNull(response.getTemplateUri());
        assertNotNull(response.getTemplateId());
    }

    private void assertNotificationLetterResponse(final SendLetterResponse response, final String addressLine1){
        assertNotNull(response);
        assertTrue(response.getBody().contains(addressLine1));
        assertEquals(Optional.of(addressLine1), response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getTemplateId());
        assertNotNull(response.getTemplateUri());
    }

    private void assertNotification(Notification notification){
        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertNotNull(notification.getTemplateId());
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
            assertTrue(Arrays.asList("accepted", "received").contains(notification.getStatus()), "expected status to be accepted or received");
        } else {
            assertTrue(Arrays.asList("created", "sending", "delivered").contains(notification.getStatus()), "expected status to be created, sending or delivered");
        }
    }

    private void assertNotificationWhenLetter(Notification notification) {
        assertTrue(notification.getLine1().isPresent());
        // the other address lines are optional. A precompiled letter will only have address_line_1
        assertTrue(notification.getPostage().isPresent());
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
        assertFalse(notification.getPostage().isPresent());

    }

    private void assertNotificationWhenSms(Notification notification) {
        assertTrue(notification.getPhoneNumber().isPresent(), notification.toString());
        assertFalse(notification.getSubject().isPresent());
        assertFalse(notification.getEmailAddress().isPresent());
        assertFalse(notification.getLine1().isPresent());
        assertFalse(notification.getLine2().isPresent());
        assertFalse(notification.getLine3().isPresent());
        assertFalse(notification.getLine4().isPresent());
        assertFalse(notification.getLine5().isPresent());
        assertFalse(notification.getLine6().isPresent());
        assertFalse(notification.getPostcode().isPresent());
        assertFalse(notification.getPostage().isPresent());
    }

    private void assertPrecompiledLetterResponse(String reference, String postage, LetterResponse response) {
        assertNotNull(response);
        assertNotNull(response.getNotificationId());
        assertEquals(response.getReference().orElse("dummy-value"), reference);
        assertEquals(response.getPostage(), Optional.ofNullable(postage));
    }

    private void assertPdfResponse(NotificationClient client, String notificationId) throws NotificationClientException {
        byte[] pdfData;
        int count = 0;
        while (true) {
            try {
                 pdfData = client.getPdfForLetter(notificationId);
                break;
            } catch (NotificationClientException e) {
                if (!e.getMessage().contains("PDFNotReadyError")) {
                    throw e;
                }

                count += 1;
                if (count > 10) { // total time slept at this point is 55 seconds
                    throw e;
                } else {
                    try {
                        Thread.sleep(count * 1000);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        assertFalse(pdfData.length == 0);
        // check that we've got a pdf by looking for the magic bytes
        byte[] magicBytes = Arrays.copyOfRange(pdfData, 0, 5);
        String magicString = new String(magicBytes);
        assertEquals("%PDF-", magicString);
        assertTrue(magicString.startsWith("%PDF-"));
    }

}
