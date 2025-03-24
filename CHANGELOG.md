## 3.0.0
* Removed the optional "email_reply_to_id" attribute of the request data for the endpoint /v2/notifications/email.  The server no longer accepts this attribute.

## 2.0.3
* Upgraded the jose4j and gson dependency versions

## 2.0.2
* Renamed default branch from 'master' to 'main'

## 2.0.0
* Added GSON library as dependency 
* Refactored all JSON serialization and deserialization to use GSON

### Breaking changes
* Removed deprecated `sendEmail` and `sendSms` methods.

  You have to use `new EmailRequest.Builder()` and `new SmsRequest.Builder()` to fluently construct your requests starting with version 2.

## 1.2.0
* Added support for sending notification requests with recipient identifiers supported by Master Person Index.
Identifiers will be used to lookup contact information (email, phone number) as well as contact preferences in VA Profile.
  
  Currently supported identifiers include:
  - `ICN` 
  - `VA Profile Id`
  - `Participant Id`
  - `Birls Id (FileNumber)`
  - `EDIPI`
  
You can send requests that contain only recipient identifier, for example:
```java
SendEmailResponse response = client.sendEmail(new EmailRequest.Builder()
        .withTemplateId("aTemplateId")
        .withRecipientIdentifier(new Identifier(IdentifierType.VAPROFILEID, "some-id");
        .build()
      );
```

Or you can specify both email / phone number and identifier. In that case identifier will be only used to lookup preferences.
```java
SendEmailResponse response = client.sendEmail(new EmailRequest.Builder()
        .withTemplateId("aTemplateId")
        .withEmailAddress("some@email.com")
        .withRecipientIdentifier(new Identifier(IdentifierType.VAPROFILEID, "some-id");
        .build()
      );
```

* Moved all tests to Junit5.

## 1.1.0
* Added EmailRequest with a fluent builder to avoid constructors with lots of same type optional arguments.
  You can use it as follows:
  ```
  SendEmailResponse response = client.sendEmail(new EmailRequest.Builder()
                .withTemplateId("aTemplateId")
                .withEmailAddress("some@email.com")
                .withPersonalisation(emptyMap())
                .withReference("aReference")
                .withBillingCode("aBillingCode")
                .withEmailReplyToId("aEmailReplyToId")
                .build()
        );
  ```
* Deprecated following NotificationClientApi sendEmail methods:
  ```
  @Deprecated
  SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation, String reference, String billingCode) throws NotificationClientException;
  
  @Deprecated
  SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation, String reference, String billingCode, String emailReplyToId) throws NotificationClientException;
  ```
* Added SmsRequest with a fluent builder to avoid constructors with lots of same type optional arguments.
  You can use it as follows:
  ```
  client.sendSms(new SmsRequest.Builder()
                .withTemplateId("aTemplateId")
                .withPhoneNumber("aPhoneNumber")
                .withPersonalisation(emptyMap())
                .withReference("aReference")
                .withBillingCode("aBillingCode")
                .withSmsSenderId("aSmsSenderId")
                .build()
        );
  ```
* Deprecated following NotificationClientApi sendSms methods:
  ```
  @Deprecated
  SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference, String billingCode) throws NotificationClientException;
  
  @Deprecated
  SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference, String billingCode, String smsSenderId) throws NotificationClientException;
  ```


## 1.0.0
* Forked client for U.S. Department of Veterans Affairs
* Renamed project to vanotify-java-client. Updated repositories, links, urls. etc
* Added support for `billing_code` in Email and SMS requests.
