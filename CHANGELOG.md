## Next release
* Where UUIDs are expected the types in the API have changed from String to UUID.  You can wrap strings in UUID.fromString() if necessary.

## 5.1.0-RELEASE
* Add a `oneClickUnsubscribeURL` parameter to `sendEmail`. The unsubscribe URL will be added to the headers of your email. Email clients will use it to add an unsubscribe button.  See https://www.notifications.service.gov.uk/using-notify/unsubscribe-links

## 5.0.1-RELEASE
* Bump jose4j from 0.9.3 to 0.9.6

## 5.0.0-RELEASE
* Remove the `isCsv` parameter to `prepareUpload`.
* Add a `filename` parameter to `prepareUpload` to set the document's filename upon download. See our documentation for more specific guidance.

## 4.1.1-RELEASE
* Bump json to 20231013 to address https://github.com/advisories/GHSA-4jq9-2xhw-jpx7.

## 4.1.0-RELEASE
* Remove a dependency on `commons-lang3` by implementing `isBlank` ourselves.

## 4.0.0-RELEASE
* Joda-Time has been removed and replaced with java.time
  * The client now uses [`ZonedDateTime` instead of `DateTime`](https://blog.joda.org/2014/11/converting-from-joda-time-to-javatime.html)

## 3.19.2-RELEASE
* Bump jose4j from 0.7.7 to 0.9.3.

## 3.19.1-RELEASE
* Bump org.json to version 20230227. Although the Notify API client is unaffected by CVE-2022-45688, we bump this package to a version that fixes this vulnerability.

## 3.19.0-RELEASE
* Adds a new interface for specifying custom retention periods when sending a file by email:
  * `retentionPeriod` can be set using a `new RetentionPeriodDuration(int, ChronoUnit)`

## 3.18.0-RELEASE
* Add support for new security features when sending a file by email:
  * `confirmEmailBeforeDownload` can be set to `True` to require the user to enter their email address before accessing the file.
  * `retentionPeriod` can be set to `<1-78> weeks` to set how long the file should be made available.

## 3.17.3-RELEASE
* Removed unused commons-cli dependency

## 3.17.2-RELEASE
* Updated dependencies to latest versions

## 3.17.1-RELEASE
* Minor patch release, changes to pom.xml including adding a license, contact details and more.

## 3.17.0-RELEASE
* Add `letterContactBlock` to the `Template` model.
- We've added `letter_contact_block` to our API responses when calling`getTemplateById`, `getTemplateVersion` and `getAllTemplates`. This release updates our `Template` model to include this new property.

## 3.16.0-RELEASE
* Add support for an optional `isCsv` parameter in the `prepareUpload()` function. This fixes a bug when sending a CSV file by email. This ensures that the file is downloaded as a CSV rather than a TXT file.

## 3.15.3-RELEASE
* Fixes issue #171 with null pointer exception on reading the errorstream

## 3.15.2-RELEASE
* Change error messages to refer to file, not document.

## 3.15.1-RELEASE
* Added 400 has a default value for the httpResult value of a NotificationClientException.
 - there are some cases when the exception is thrown from the client and not from the API, even though a httpResult. All the exceptions raised can be classified as 400, therefore using 400 as a default.

## 3.15.0-RELEASE
* Added `NotificationClient.getPdfForLetter` function
  - accepts `String notificationId`
  - returns a `byte[]` containing the final printable PDF for a precompiled or templated letter

## 3.14.2-RELEASE
* Updated old dependencies
* Updated code to bring in linting standards.

## 3.14.1-RELEASE
* Updated project properties to use UTF-8
* Updated GET and POST to use UTF-8 readers and writers

## 3.14.0-RELEASE
* Added `postage` argument to `NotificationClient.sendPrecompiledLetter` and `NotificationClient.sendPrecompiledLetterWithInputStream`
* Added `postage` to `LetterResponse`
* Added `postage` to `Notification`
* Added `html` to `TemplatePreview`

## 3.13.0-RELEASE
* Allow passing of `List`s into the personalisation Map to display as a bulleted list in the message.

## 3.12.0-RELEASE
* Added `NotificationClient.prepareUpload` method that can be used if you want to upload a document and send a link to that docuemnt by email.
  - Takes a byte[] of document contents
  - You then add the returned `JSONObject` to the personalisation map.
  - NOTE: the personalisation map for this call needs to be HashMap<String, Object>
    - which is why the `sendEmail` no uses a wildcard for the generic definition of the `personalisation` `HashMap`

## 3.11.0-RELEASE
* Updated `Template` to have `name`, the name of the template as set in Notify.

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
