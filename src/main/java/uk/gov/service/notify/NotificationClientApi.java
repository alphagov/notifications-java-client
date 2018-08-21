package uk.gov.service.notify;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public interface NotificationClientApi {

    /**
     * The sendEmail method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible on the template page in the application.
     * @param emailAddress    The email address
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>SendEmailResponse</code>
     * @throws NotificationClientException
     */
    SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, String> personalisation, String reference) throws NotificationClientException;


    /**
     * The sendEmail method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible on the template page in the application.
     * @param emailAddress    The email address
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @param emailReplyToId  An optional identifier for a reply to email address for the notification, rather than use the service default.
     *                        Service emailReplyToIds can be accessed via the service settings / manage email reply to addresses page.
     *                        Omit this argument to use the default service email reply to address.
     * @return <code>SendEmailResponse</code>
     * @throws NotificationClientException
     */
    SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, String> personalisation, String reference, String emailReplyToId) throws NotificationClientException;

    /**
     * The sendSms method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible from the template page in the application.
     * @param phoneNumber     The mobile phone number
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>SendSmsResponse</code>
     * @throws NotificationClientException
     */
    SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, String> personalisation, String reference) throws NotificationClientException;

    /**
     * The sendSms method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible from the template page in the application.
     * @param phoneNumber     The mobile phone number
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @param smsSenderId     An optional identifier for the text message sender of the notification, rather than use the service default.
     *                        Service smsSenderIds can be accessed via the service settings / manage text message senders page.
     *                        Omit this argument to use the default service text message sender.
     * @return <code>SendSmsResponse</code>
     * @throws NotificationClientException
     */
    SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, String> personalisation, String reference, String smsSenderId) throws NotificationClientException;

    /**
     * The sendLetter method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      Find templateId by clicking API info for the template you want to send
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob.
     *                        Must include the keys "address_line_1", "address_line_2" and "postcode".
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>SendLetterResponse</code>
     * @throws NotificationClientException
     */
    SendLetterResponse sendLetter(String templateId, Map<String, String> personalisation, String reference) throws NotificationClientException;

    /**
     * The sendPrecompiledLetter method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param reference                 A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                                  This reference can be unique or used used to refer to a batch of notifications.
     *                                  Cannot be an empty string or null for precompiled PDF files.
     * @param precompiledPDF            A file containing a PDF conforming to the Notify standards for printing.
     *                                  The file must be a PDF and cannot be null.
     * @return <code>LetterResponse</code>
     *
     * @throws NotificationClientException
     */
    LetterResponse sendPrecompiledLetter(String reference, File precompiledPDF) throws NotificationClientException;

    /**
     * The sendPrecompiledLetterWithInputStream method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param reference                 A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                                  This reference can be unique or used used to refer to a batch of notifications.
     *                                  Cannot be an empty string or null for precompiled PDF files.
     * @param stream                    An <code>InputStream</code> conforming to the Notify standards for printing.
     *                                  The InputStream cannot be null.
     * @return <code>LetterResponse</code>
     *
     * @throws NotificationClientException
     */
    public LetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream) throws NotificationClientException;

    /**
     * The getNotificationById method will return a <code>Notification</code> for a given notification id.
     * The id is can be retrieved from the <code>NotificationResponse</code> of a <code>sendEmail</code> or <code>sendSms</code> request.
     *
     * @param notificationId The id of the notification.
     * @return <code>Notification</code>
     * @throws NotificationClientException
     */
    Notification getNotificationById(String notificationId) throws NotificationClientException;

    /**
     * The getNotifications method will create a GET HTTPS request to retrieve all the notifications.
     *
     * @param status If status is not empty or null notifications will only return notifications for the given status.
     *               Possible statuses are created|sending|delivered|permanent-failure|temporary-failure|technical-failure
     * @param notification_type If notification_type is not empty or null only notifications of the given status will be returned.
     *                          Possible notificationTypes are sms|email
     * @param reference If reference is not empty or null only the notifications with that reference are returned.
     * @param olderThanId If olderThanId is not empty or null only the notifications older than that notification id are returned.
     * @return <code>NotificationList</code>
     * @throws NotificationClientException
     */
    NotificationList getNotifications(String status, String notification_type, String reference, String olderThanId) throws NotificationClientException;

    /**
     * The getTemplateById returns a <code>Template</code> given the template id.
     *
     * @param templateId The template id is visible on the template page in the application.
     * @return <code>Template</code>
     * @throws NotificationClientException
     */
    Template getTemplateById(String templateId) throws NotificationClientException;

    /**
     * The getTemplateVersion returns a <code>Template</code> given the template id and version.
     *
     * @param templateId The template id is visible on the template page in the application.
     * @param version The version of the template to return
     * @return <code>Template</code>
     * @throws NotificationClientException
     */
    Template getTemplateVersion(String templateId, int version) throws NotificationClientException;

    /**
     * Returns all the templates for your service. Filtered by template type if not null.
     *
     * @param templateType If templateType is not empty or null templates will be filtered by type.
     *          Possible template types are email|sms|letter
     * @return <code>TemplateList</code>
     * @throws NotificationClientException
     */
    TemplateList getAllTemplates(String templateType) throws NotificationClientException;

    /**
     * The generateTemplatePreview returns a template with the placeholders replaced with the given personalisation.
     *
     * @param templateId The template id is visible from the template page in the application.
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @return <code>Template</code>
     * @throws NotificationClientException
     */
    TemplatePreview generateTemplatePreview(String templateId, Map<String, String> personalisation) throws NotificationClientException;

    /**
     * The getReceivedTextMessages returns a list of <code>ReceivedTextMessage</code>, the list is sorted by createdAt descending.
     * @param olderThanId If olderThanId is not empty or null only the received text messages older than that id are returned.
     * @return <code>ReceivedTextMessageList</code>
     */
    ReceivedTextMessageList getReceivedTextMessages(String olderThanId) throws NotificationClientException;
}
