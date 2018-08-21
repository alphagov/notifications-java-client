# Java client documentation

This documentation is for developers interested in using the GOV.UK Notify Java client to send emails, text messages or letters.

# Set up the client

## Install the client

The `notifications-java-client` deploys to Bintray.

Go to the [GOV.UK Notify Java client page on Bintray](https://bintray.com/gov-uk-notify/maven/notifications-java-client) [external link]:

1. Select __Set me up!__ and use the appropriate download instructions.
1. Go to the Maven build settings section of the page and copy the appropriate dependency code snippet.

Refer to the [client changelog](https://github.com/alphagov/notifications-java-client/blob/master/CHANGELOG.md) for the version number and the latest updates.

## Create a new instance of the client

Add this code to your application:

```java
import uk.gov.service.notify.NotificationClient;
NotificationClient client = new NotificationClient(apiKey);
```

To get an API key, [sign in to GOV.UK Notify](https://www.notifications.service.gov.uk/) and go to the __API integration__ page. You can find more information in the [API keys](#api-keys) section of the documentation.

# Send a message

You can use GOV.UK Notify to send text messages, emails and letters.

## Send a text message

### Method

```java
SendSmsResponse response = client.sendSms(
    templateId,
    phoneNumber,
    personalisation,
    reference,
    smsSenderId
);
```

### Arguments

#### templateId (required)

Sign in to [GOV.UK Notify](https://www.notifications.service.gov.uk/) and go to the __Templates__ page to find the template ID.

```
String templateId="f33517ff-2a88-4f6e-b855-c550268ce08a";
```

#### phoneNumber (required)

The phone number of the recipient of the text message. This number can be a UK or international number.

```
String phoneNumber="+447900900123";
```

#### personalisation (required)

If a template has placeholder fields for personalised information such as name or reference number, you must provide their values in a map. For example:

```java
Map<String, String> personalisation = new HashMap<>();
personalisation.put("first_name", "Amala");
personalisation.put("application_date", "2018-01-01");
```

If a template does not have any placeholder fields for personalised information, you must pass in an empty map or `null`.

#### reference (required)

A unique identifier you create. This reference identifies a single unique notification or a batch of notifications. If you do not have a reference, you must pass in an empty string or `null`.

```
String reference='STRING';
```

#### smsSenderId (optional)

A unique identifier of the sender of the text message notification. To find this information, go to the __Text Message sender__ settings screen:

1. Sign in to your GOV.UK Notify account.
1. Go to __Settings__.
1. If you need to change to another service, select __Switch service__ in the top right corner of the screen and select the correct one.
1. Go to the __Text Messages__ section and select __Manage__ on the __Text Message sender__ row.

In this screen, you can then either:

  - copy the sender ID that you want to use and paste it into the method
  - select __Change__ to change the default sender that the service will use, and select __Save__

```
String smsSenderId='8e222534-7f05-4972-86e3-17c5d9f894e2'
```


If you do not have an `smsSenderId`, you can leave out this argument.

### Response

If the request to the client is successful, the client returns a `SendSmsResponse`:

```java
UUID notificationId;
Optional<String> reference;
UUID templateId;
int templateVersion;
String templateUri;
String body;
Optional<String> fromNumber;
```

If you are using the [test API key](#test), all your messages come back with a `delivered` status.

All messages sent using the [team and whitelist](#team-and-whitelist) or [live](#live) keys appear on your dashboard.

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|Message|How to fix|
|:---|:---|:---|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Can't send to this recipient using a team-only API key"`<br>`]}`|Use the correct type of [API key](#api-keys)|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Can't send to this recipient when service is in trial mode - see https://www.notifications.service.gov.uk/trial-mode"`<br>`}]`|Your service cannot send this notification in [trial mode](https://www.notifications.service.gov.uk/features/using-notify#trial-mode)|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|
|`429`|`[{`<br>`"error": "RateLimitError",`<br>`"message": "Exceeded rate limit for key type TEAM/TEST/LIVE of 3000 requests per 60 seconds"`<br>`}]`|Refer to [API rate limits](#api-rate-limits) for more information|
|`429`|`[{`<br>`"error": "TooManyRequestsError",`<br>`"message": "Exceeded send limits (LIMIT NUMBER) for today"`<br>`}]`|Refer to [service limits](#service-limits) for the limit number|
|`500`|`[{`<br>`"error": "Exception",`<br>`"message": "Internal server error"`<br>`}]`|Notify was unable to process the request, resend your notification|

## Send an email

### Method

```java
SendEmailResponse response = client.sendEmail(
    templateId,
    emailAddress,
    personalisation,
    reference,
    emailReplyToId
);
```

### Arguments


#### templateId (required)

Sign in to [GOV.UK Notify](https://www.notifications.service.gov.uk/) and go to the __Templates__ page to find the template ID.

```
String templateId="f33517ff-2a88-4f6e-b855-c550268ce08a";
```

#### emailAddress (required)

The email address of the recipient.

```
String emailAddress='sender@something.com';
```

#### personalisation (required)

If a template has placeholder fields for personalised information such as name or application date, you must provide their values in a map. For example:

```java
Map<String, String> personalisation = new HashMap<>();
personalisation.put("first_name", "Amala");
personalisation.put("application_date", "2018-01-01");
```
If a template does not have any placeholder fields for personalised information, you must pass in an empty map or `null`.

#### reference (required)

A unique identifier you create. This reference identifies a single unique notification or a batch of notifications. If you do not have a reference, you must pass in an empty string or `null`.

```
String reference='STRING';
```

#### emailReplyToId (optional)

This is an email reply-to address specified by you to receive replies from your users. Your service cannot go live until you set up at least one of these email addresses. To set up:

1. Sign into your GOV.UK Notify account.
1. Go to __Settings__.
1. If you need to change to another service, select __Switch service__ in the top right corner of the screen and select the correct one.
1. Go to the __Email__ section and select __Manage__ on the __Email reply to addresses__ row.
1. Select __Change__ to specify the email address to receive replies, and select __Save__.

```
String emailReplyToId='8e222534-7f05-4972-86e3-17c5d9f894e2'
```

If you do not have an `emailReplyToId`, you can leave out this argument.

### Response

If the request to the client is successful, the client returns a `SendEmailResponse`:

```java
UUID notificationId;
Optional<String> reference;
UUID templateId;
int templateVersion;
String templateUri;
String body;
String subject;
Optional<String> fromEmail;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|Message|How to fix|
|:--- |:---|:---|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Can't send to this recipient using a team-only API key"`<br>`]}`|Use the correct type of [API key](#api-keys)|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Can't send to this recipient when service is in trial mode - see https://www.notifications.service.gov.uk/trial-mode"`<br>`}]`|Your service cannot send this notification in [trial mode](https://www.notifications.service.gov.uk/features/using-notify#trial-mode)|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|
|`429`|`[{`<br>`"error": "RateLimitError",`<br>`"message": "Exceeded rate limit for key type TEAM/TEST/LIVE of 3000 requests per 60 seconds"`<br>`}]`|Refer to [API rate limits](#api-rate-limits) for more information|
|`429`|`[{`<br>`"error": "TooManyRequestsError",`<br>`"message": "Exceeded send limits (LIMIT NUMBER) for today"`<br>`}]`|Refer to [service limits](#service-limits) for the limit number|
|`500`|`[{`<br>`"error": "Exception",`<br>`"message": "Internal server error"`<br>`}]`|Notify was unable to process the request, resend your notification|

## Send a letter

When your service first signs up to GOV.UK Notify, you’ll start in trial mode. You can only send letters in live mode. You must ask GOV.UK Notify to make your service live.

1. Sign in to [GOV.UK Notify](https://www.notifications.service.gov.uk/).
1. Select __Using Notify__.
1. Select __requesting to go live__.

### Method

```java
SendLetterResponse response = client.sendLetter(
    templateId,
    personalisation,
    reference
);
```

### Arguments

#### templateId (required)

Sign in to [GOV.UK Notify](https://www.notifications.service.gov.uk/) and go to the __Templates__ page to find the template ID.

```
String templateId = "f33517ff-2a88-4f6e-b855-c550268ce08a";
```

#### personalisation (required)

The personalisation argument always contains the following parameters for the letter recipient's address:

- `address_line_1`
- `address_line_2`
- `postcode`

Any other placeholder fields included in the letter template also count as required parameters. You must provide their values in a map. For example:

```java
HashMap<String, String> personalisation = new HashMap<>();
personalisation.put("address_line_1", "The Occupier"); // mandatory address field
personalisation.put("address_line_2", "Flat 2"); // mandatory address field
personalisation.put("postcode", "SW14 6BH"); // mandatory address field
personalisation.put("first_name", "Amala"); // field from template
personalisation.put("application_date", "2018-01-01"); // field from template
```

If a template does not have any placeholder fields for personalised information, you must pass in an empty map or `null`.

#### personalisation (required)

The following parameters in the letter recipient's address are optional:

```java
personalisation.put("address_line_3", "123 High Street"); // optional address field
personalisation.put("address_line_4", "Richmond upon Thames"); // optional address field
personalisation.put("address_line_5", "London"); // optional address field
personalisation.put("address_line_6", "Middlesex"); // optional address field
```

#### reference (required)

A unique identifier you create. This reference identifies a single unique notification or a batch of notifications. If you do not have a reference, you must pass in an empty string or `null`.

```
String reference='STRING';
```

### Response

If the request to the client is successful, the client returns a `SendLetterResponse`:

```java
UUID notificationId;
Optional<String> reference;
UUID templateId;
int templateVersion;
String templateUri;
String body;
String subject;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|Message|How to fix|
|:--- |:---|:---|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Cannot send letters with a team api key"`<br>`]}`|Use the correct type of [API key](#api-keys)|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Cannot send letters when service is in trial mode - see https://www.notifications.service.gov.uk/trial-mode"`<br>`}]`|Your service cannot send this notification in [trial mode](https://www.notifications.service.gov.uk/features/using-notify#trial-mode)|
|`400`|`[{`<br>`"error": "ValidationError",`<br>`"message": "personalisation address_line_1 is a required property"`<br>`}]`|Ensure that your template has a field for the first line of the address, refer to [personalisation](#send-a-letter-arguments-personalisation-required) for more information|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|
|`429`|`[{`<br>`"error": "RateLimitError",`<br>`"message": "Exceeded rate limit for key type TEAM/TEST/LIVE of 3000 requests per 60 seconds"`<br>`}]`|Refer to [API rate limits](#api-rate-limits) for more information|
|`429`|`[{`<br>`"error": "TooManyRequestsError",`<br>`"message": "Exceeded send limits (LIMIT NUMBER) for today"`<br>`}]`|Refer to [service limits](#service-limits) for the limit number|
|`500`|`[{`<br>`"error": "Exception",`<br>`"message": "Internal server error"`<br>`}]`|Notify was unable to process the request, resend your notification|


## Send a precompiled letter

This is an invitation-only feature. Contact the GOV.UK Notify team on the [support page](https://www.notifications.service.gov.uk/support) or through the [Slack channel](https://ukgovernmentdigital.slack.com/messages/govuk-notify) for more information.

### Method

```java
LetterResponse response = client.sendPrecompiledLetter(
    reference,
    precompiledPDFAsFile
    );
```

```java
LetterResponse response = client.sendPrecompiledLetterWithInputStream(
    reference,
    precompiledPDFAsInputStream
    );
```

### Arguments

#### reference (required)

A unique identifier you create. This reference identifies a single unique notification or a batch of notifications. It must not contain any personal information such as name or postal address.

```
String reference="STRING";
```

#### precompiledPDFAsFile (required for the sendPrecompiledLetter method)

The precompiled letter must be a PDF file. This argument adds the precompiled letter PDF file to a Java file object. The method sends this Java file object to GOV.UK Notify.

```java
File precompiledPDF = new File("<PDF file path>");
```

#### precompiledPDFAsInputStream (required for the sendPrecompiledLetterWithInputStream method)

The precompiled letter must be an InputStream. This argument adds the precompiled letter PDF content to a Java InputStream object. The method sends this InputStream to GOV.UK Notify.

```java
InputStream precompiledPDFAsInputStream = new FileInputStream(pdfContent);
```

### Response

If the request to the client is successful, the client returns a `LetterResponse`:

```java
UUID notificationId;
<String> reference;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|Message|How to fix|
|:--- |:---|:---|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Cannot send letters with a team api key"`<br>`]}`|Use the correct type of [API key](#api-keys)|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Cannot send letters when service is in trial mode - see https://www.notifications.service.gov.uk/trial-mode"`<br>`}]`|Your service cannot send this notification in [trial mode](https://www.notifications.service.gov.uk/features/using-notify#trial-mode)|
|`400`|`[{`<br>`"error": "ValidationError",`<br>`"message": "personalisation address_line_1 is a required property"`<br>`}]`|Send a valid PDF file|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|
|`429`|`[{`<br>`"error": "RateLimitError",`<br>`"message": "Exceeded rate limit for key type TEAM/TEST/LIVE of 3000 requests per 60 seconds"`<br>`}]`|Refer to [API rate limits](#api-rate-limits) for more information|
|`429`|`[{`<br>`"error": "TooManyRequestsError",`<br>`"message": "Exceeded send limits (LIMIT NUMBER) for today"`<br>`}]`|Refer to [service limits](#service-limits) for the limit number|
|N/A|`"message":"precompiledPDF must be a valid PDF file"`|Send a valid PDF file|
|N/A|`"message":"reference cannot be null or empty"`|Populate the reference parameter|
|N/A|`"message":"precompiledPDF cannot be null or empty"`|Send a PDF file with data in it|

# Get message status

Message status depends on the type of message that you have sent.

You can only get the status of messages that are 7 days old or less.

## Status - text and email

|Status|Information|
|:---|:---|
|Created|The message is queued to be sent to the provider. The notification usually remains in this state for a few seconds.|
|Sending|The message is queued to be sent by the provider to the recipient, and GOV.UK Notify is waiting for delivery information.|
|Delivered|The message was successfully delivered.|
|Failed|This covers all failure statuses:<br>- `permanent-failure` - "The provider was unable to deliver message, email or phone number does not exist; remove this recipient from your list"<br>- `temporary-failure` - "The provider was unable to deliver message, email inbox was full or phone was turned off; you can try to send the message again"<br>- `technical-failure` - "Notify had a technical failure; you can try to send the message again"|

## Status - text only

|Status|Information|
|:---|:---|
|Pending|GOV.UK Notify received a callback from the provider but the device has not yet responded. Another callback from the provider determines the final status of the notification.|
|Sent|The text message was delivered internationally. This only applies to text messages sent to non-UK phone numbers. GOV.UK Notify may not receive additional status updates depending on the recipient's country and telecoms provider.|

## Status - letter

|Status|information|
|:---|:---|
|Failed|The only failure status that applies to letters is `technical-failure`. GOV.UK Notify had an unexpected error while sending to our printing provider.|
|Accepted|GOV.UK Notify is printing and posting the letter.|
|Received|The provider has received the letter to deliver.|

## Status - pre-compiled letter

|Status|information|
|:---|:---|
|Pending virus scan|GOV.UK Notify virus scan of the pre-compiled letter file is not yet complete.|
|Virus scan failed|GOV.UK Notify virus scan has identified a potential virus in the pre-compiled letter file.|

## Get the status of one message

### Method

```java
Notification notification = client.getNotificationById(notificationId);
```

### Arguments

#### notificationId (required)

The ID of the notification. You can find the notification ID in the response to the [original notification method call](/java.html#response).

You can also find it in your [GOV.UK Notify Dashboard](https://www.notifications.service.gov.uk).

1. Sign into GOV.UK Notify and select __Dashboard__.
1. Select either __emails sent__, __text messages sent__, or __letters sent__.
1. Select the relevant notification.
1. Copy the notification ID from the end of the page URL, for example `https://www.notifications.service.gov.uk/services/af90d4cb-ae88-4a7c-a197-5c30c7db423b/notification/ID`.

### Response

If the request to the client is successful, the client returns a `Notification`:

```java
UUID id;
Optional<String> reference;
Optional<String> emailAddress;
Optional<String> phoneNumber;
Optional<String> line1;
Optional<String> line2;
Optional<String> line3;
Optional<String> line4;
Optional<String> line5;
Optional<String> line6;
Optional<String> postcode;
String notificationType;
String status;
UUID templateId;
int templateVersion;
String templateUri;
String body;
Optional<String subject;
DateTime createdAt;
Optional<DateTime> sentAt;
Optional<DateTime> completedAt;
Optional<DateTime> estimatedDelivery;
Optional<String> createdByName;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|Message|How to fix|
|:---|:---|:---|
|`400`|`[{`<br>`"error": "ValidationError",`<br>`"message": "id is not a valid UUID"`<br>`}]`|Check the notification ID|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|
|`404`|`[{`<br>`"error": "NoResultFound",`<br>`"message": "No result found"`<br>`}]`|Check the notification ID|


## Get the status of multiple messages

This API call returns one page of up to 250 messages and statuses. You can get either the most recent messages, or get older messages by specifying a particular notification ID in the [`olderThanId`](#olderthanid) argument.

You can only get the status of messages that are 7 days old or less.

### Method

```java
NotificationList notification = client.getNotifications(
    status,
    notificationType,
    reference,
    olderThanId
);
```

To get the most recent messages, you must pass in an empty `olderThanId` argument or `null`.

To get older messages, pass the ID of an older notification into the `olderThanId` argument. This returns the next oldest messages from the specified notification ID.

### Arguments

You can pass in empty arguments or `null` to ignore these filters.

#### status (optional)

| status | description | text | email | letter |
|:--- |:--- |:--- |:--- |:--- |
|`created` |The message is queued to be sent to the provider|Yes|Yes||
|`sending` |The message is queued to be sent by the provider to the recipient|Yes|Yes||
|`delivered`|The message was successfully delivered|Yes|Yes||
|`sent`|The text message was delivered internationally|Yes|Yes||
|`failed`|This returns all failure statuses:<br>- `permanent-failure`<br>- `temporary-failure`<br>- `technical-failure`|Yes|Yes||
|`permanent-failure`|The provider was unable to deliver message, email or phone number does not exist; remove this recipient from your list|Yes|Yes||
|`temporary-failure`|The provider was unable to deliver message, email inbox was full or phone was turned off; you can try to send the message again|Yes|Yes||
|`technical-failure`|Email / Text: Notify had a technical failure; you can try to send the message again. <br><br>Letter: Notify had an unexpected error while sending to our printing provider. <br><br>You can leave out this argument to ignore this filter.|Yes|Yes||
|`accepted`|Notify is printing and posting the letter|||Yes|
|`received`|The provider has received the letter to deliver|||Yes|

#### notificationType (optional)

You can filter by:

* `email`
* `sms`
* `letter`

#### reference (optional)

A unique identifier you create if necessary. This reference identifies a single unique notification or a batch of notifications.

```
String reference='STRING';
```

#### olderThanId (optional)

Input the ID of a notification into this argument. If you use this argument, the client returns the next 250 received notifications older than the given ID.

```
String olderThanId='8e222534-7f05-4972-86e3-17c5d9f894e2'
```

If you pass in an empty argument or `null`, the client returns the most recent 250 notifications.

### Response

If the request to the client is successful, the client returns a `NotificationList`:

```java
List<Notification> notifications;
String currentPageLink;
Optional<String> nextPageLink;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|Message||
|:---|:---|:---|
|`400`|`[{`<br>`"error": "ValidationError",`<br>`"message": "bad status is not one of [created, sending, sent, delivered, pending, failed, technical-failure, temporary-failure, permanent-failure, accepted, received]"`<br>`}]`|Contact the Notify team|
|`400`|`[{`<br>`"error": "ValidationError",`<br>`"message": "Applet is not one of [sms, email, letter]"`<br>`}]`|Contact the Notify team|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|


# Get a template

## Get a template by ID

### Method

This returns the latest version of the template.

```java
Template template = client.getTemplateById(templateId);
```

### Arguments

#### templateId (required)

Sign in to [GOV.UK Notify](https://www.notifications.service.gov.uk/) and go to the __Templates__ page to find the template ID.

```
String templateId='f33517ff-2a88-4f6e-b855-c550268ce08a';
```

### Response

If the request to the client is successful, the client returns a `Template`:

```java
UUID id;
String templateType;
DateTime createdAt;
Optional<DateTime> updatedAt;
String createdBy;
int version;
String body;
Optional<String> subject;
Optional<Map<String, Object>> personalisation;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|Message|How to fix|
|:---|:---|:---|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|
|`404`|`[{`<br>`"error": "NoResultFound",`<br>`"message": "No Result Found"`<br>`}]`|Check your [template ID](#arguments-template-id-required)|


## Get a template by ID and version

### Method

This returns the latest version of the template.

```java
Template template = client.getTemplateVersion(templateId, version);
```

### Arguments

#### templateId (required)

Sign in to [GOV.UK Notify](https://www.notifications.service.gov.uk/) and go to the __Templates__ page to find the template ID.

```
String templateId='f33517ff-2a88-4f6e-b855-c550268ce08a';
```

#### version (required)

The version number of the template.

### Response

If the request to the client is successful, the client returns a `Template`:

```Java
UUID id;
String templateType;
DateTime createdAt;
Optional<DateTime> updatedAt;
String createdBy;
int version;
String body;
Optional<String> subject;
Optional<Map<String, Object>> personalisation;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|message|How to fix|
|:---|:---|:---|
|`400`|`[{`<br>`"error": "ValidationError",`<br>`"message": "id is not a valid UUID"`<br>`}]`|Check the notification ID|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|
|`404`|`[{`<br>`"error": "NoResultFound",`<br>`"message": "No Result Found"`<br>`}]`|Check your [template ID](#get-a-template-by-id-and-version-arguments-template-id-required) and [version](#version)|


## Get all templates

### Method

This returns the latest version of all templates.

```java
TemplateList templates = client.getAllTemplates(templateType);
```

### Arguments

#### templateType (optional)

If you donot use `templateType`, the client returns all templates. Otherwise you can filter by:

- `email`
- `sms`
- `letter`

### Response

If the request to the client is successful, the client returns a `TemplateList`:

```java
List<Template> templates;
```

If no templates exist for a template type or there no templates for a service, the templates list is empty.

## Generate a preview template

### Method

This generates a preview version of a template.

```java
TemplatePreview templatePreview = client.getTemplatePreview(
    templateId,
    personalisation
);
```

The parameters in the personalisation argument must match the placeholder fields in the actual template. The API notification client ignores any extra fields in the method.

### Arguments

#### templateId (required)

Sign in to [GOV.UK Notify](https://www.notifications.service.gov.uk/) and go to the __Templates__ page to find the template ID.

```
String templateId='f33517ff-2a88-4f6e-b855-c550268ce08a';
```

#### personalisation (required)

If a template has placeholder fields for personalised information such as name or application date, you must provide their values in a map. For example:

```java
Map<String, String> personalisation = new HashMap<>();
personalisation.put("first_name", "Amala");
personalisation.put("application_date", "2018-01-01");
```
If a template does not have any placeholder fields for personalised information, you must pass in an empty map or `null`.

### Response

If the request to the client is successful, the client returns a `TemplatePreview`:

```java
UUID id;
String templateType;
int version;
String body;
Optional<String> subject;
```

### Error codes

If the request is not successful, the client returns a `NotificationClientException` containing the relevant error code:

|httpResult|message|How to fix|
|:---|:---|:---|
|`400`|`[{`<br>`"error": "BadRequestError",`<br>`"message": "Missing personalisation: [PERSONALISATION FIELD]"`<br>`}]`|Check that the personalisation arguments in the method match the placeholder fields in the template|
|`400`|`[{`<br>`"error": "NoResultFound",`<br>`"message": "No result found"`<br>`}]`|Check the [template ID](#generate-a-preview-template-required-arguments-template-id)|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Error: Your system clock must be accurate to within 30 seconds"`<br>`}]`|Check your system clock|
|`403`|`[{`<br>`"error": "AuthError",`<br>`"message": "Invalid token: signature, api token not found"`<br>`}]`|Use the correct API key. Refer to [API keys](#api-keys) for more information|


# Get received text messages

This API call returns one page of up to 250 received text messages. You can get either the most recent messages, or get older messages by specifying a particular notification ID in the [`olderThanId`](#olderThanId) argument.

You can only get messages that are 7 days old or less.

### Method

```java
ReceivedTextMessageList response = client.getReceivedTextMessages(olderThanId);
```

To get the most recent messages, you must pass in an empty argument or `null`.

To get older messages, pass the ID of an older notification into the `olderThanId` argument. This returns the next oldest messages from the specified notification ID.

### Arguments

#### olderThanId (optional)

Input the ID of a received text message into this argument. If you use this argument, the client returns the next 250 received text messages older than the given ID.

```
String olderThanId='8e222534-7f05-4972-86e3-17c5d9f894e2'
```

If you pass in an empty argument or `null`, the client returns the most recent 250 text messages.

### Response

If the request to the client is successful, the client returns a `ReceivedTextMessageList` that returns all received texts.

```java
private List<ReceivedTextMessage> receivedTextMessages;
private String currentPageLink;
private String nextPageLink;
```
The `ReceivedTextMessageList` has the following properties:

```java
private UUID id;
private String notifyNumber;
private String userNumber;
private UUID serviceId;
private String content;
private DateTime createdAt;
```
If the notification specified in the `olderThanId` argument is older than 7 days, the client returns an empty response.
