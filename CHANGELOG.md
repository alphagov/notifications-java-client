## 3.10.0-RELEASE
* Updated `Notification` to have an Optional createdByName. If the notification was sent manually, this will be the name of the sender. If the notification was sent through the API, this will be `Optional.empty()`.
* New method, `sendPrecompiledLetterWithInputStream`, to send a precompiled letter using an InputStream rather than a file.

## 3.9.2-RELEASE
* Updated testEmailNotificationWithoutPersonalisationReturnsErrorMessageIT to only look for the BadRequestError rather than the json "error": BadRequestError

## 3.9.1-RELEASE
* Response to `sendPrecompiledLetter` updated
  - The response now only includes the notification id and the client reference

## 3.9.0-RELEASE
* `sendPrecompiledLetter` added to NotificationClient
  - The client can now send PDF files which conform to the Notify printing template
  - Send a Java File object or a base64 encoded string
  - 'reference' must be provided to identify the document

## 3.8.0-RELEASE
* Added `getTextMessages(String olderThan)` method to fetch received text messages.

## 3.7.0-RELEASE
* Update to `NotificationsAPIClient.sendSms()`
    * added `smsSenderId`: an optional smsSenderId specified when adding text message senders under service settings, if this is not provided the default text message sender for the service will be used. `smsSenderId` can be omitted.

## 3.6.0-RELEASE
* Update to `NotificationsAPIClient.sendEmail()`
    * added `emailReplyToId`: an optional email_reply_to_id specified when adding Email reply to addresses under service settings, if this is not provided the reply to email will be the service default reply to email. `emailReplyToId` can be omitted.

## 3.5.1-RELEASE
* Attached source and javadoc artifacts to jar

## 3.5.0-RELEASE
* `Template` now contains `personalisation`, a map of the template placeholder names.

## 3.4.0-RELEASE
* `Notification` now contains `estimatedDelivery`
  - Shows when the letter is expected to be picked up by Royal Mail from our printing providers.
  - `null` for sms and email.
* `NotificationClientApi` interface updated to include `sendLetter`` functionality.

## 3.3.0-RELEASE
* `sendLetter` added to NotificationClient
  - SendLetterResponse sendLetter(String templateId, Map<String, String> personalisation, String reference) throws NotificationClientException
  - `personalisation` map is required, and must contain the recipient's address details.
  - as with sms and email, `reference` is optional.

## 3.2.0-RELEASE
* Template endpoints added to the NotificationClient
* `getTemplateById` - get the latest version of a template by id.
* `getTemplateVersion` - get the template by id and version.
* `getAllTemplates` - get all templates, can be filtered by template type.
* `generateTemplatePreview` - get the contents of a template with the placeholders replaced with the given personalisation.
* See the README for more information about the new template methods.


## 3.1.3-RELEASE

### Changed
* Updated the jose4j dependency in light of the security issues: [https://auth0.com/blog/critical-vulnerability-in-json-web-encryption/](https://auth0.com/blog/critical-vulnerability-in-json-web-encryption/)


## 3.1.2-RELEASE

### Changed
* Added SSLContext to `NotificationClient` constructor, to allow clients to be created with a specified SSL Context.


## 3.1.1-RELEASE

### Fixed
* The Client UserAgent is now populated correctly.


## 3.1.0-RELEASE

### Changed
* `NotificationClientException` now has a getter for the httpResult, `NotificationClientException.getHttpResult()`
* Added `NotificationClientApi` interface for `NotificationClient`
  * The interface is useful if you want to stub the `NotificationClient` for tests.


## 3.0.0-RELEASE

### Changed
* Using version 2 of the notification-api.
* Update to `NotificationClient.sendSms()`:
    * added `reference`: an optional unique identifier for the notification or an identifier for a batch of notifications. `reference` can be an empty string or null.
    * returns SendSmsResponse, this object only contains the necessary information about the notification.
    * only one method signature:
            `public SendSmsResponse sendSms(String templateId, String phoneNumber, HashMap<String, String> personalisation, String reference) throws NotificationClientException;`
      Where `personalisation` can be an empty map or null and `reference` can be an empty string or null.
* Update to `NotificationClient.sendEmail()`:
    * added `reference`: an optional unique identifier for the notification or an identifier for a batch of notifications. `reference` can be an empty string or null.
    * returns SendEmailResponse, this object only contains the necessary information about the notification.
    * only one method signature:
            `public SendEmailResponse sendEmail(String templateId, String emailAddress, HashMap<String, String> personalisation, String reference) throws NotificationClientException;`
      Where `personalisation` can be an empty map or null and `reference` can be an empty string or null.
* Notification class has been changed; return type of `NotificationClient.getNotificationById(id)`, see the README for details.
* `NotificationClient.getAllNotifications()`
    * Notifications can be filtered by `reference`, see the README for details.
    * Notifications can be filtered by `olderThanId`, see the README for details.
    * NotificationList response has changed, see the README for details.
* `NotificationClient` removed the constructors containing the serviceId, which is no longer needed because the api key contains the service id.
