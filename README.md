# GOV.UK Notify Java client

## Installation

### Maven

The notifications-java-client is deployed to [Bintray](https://bintray.com/gov-uk-notify/maven/notifications-java-client). Add this snippet to your Maven `settings.xml` file.
```xml
<?xml version='1.0' encoding='UTF-8'?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd' xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
<profiles>
	<profile>
		<repositories>
			<repository>
				<snapshots>
					<enabled>false</enabled>
				</snapshots>
				<id>bintray-gov-uk-notify-maven</id>
				<name>bintray</name>
				<url>http://dl.bintray.com/gov-uk-notify/maven</url>
			</repository>
		</repositories>
		<pluginRepositories>
			<pluginRepository>
				<snapshots>
					<enabled>false</enabled>
				</snapshots>
				<id>bintray-gov-uk-notify-maven</id>
				<name>bintray-plugins</name>
				<url>http://dl.bintray.com/gov-uk-notify/maven</url>
			</pluginRepository>
		</pluginRepositories>
		<id>bintray</id>
	</profile>
</profiles>
<activeProfiles>
	<activeProfile>bintray</activeProfile>
</activeProfiles>
</settings>
```
Then add the Maven dependency to your project.
```xml
    <dependency>
        <groupId>uk.gov.service.notify</groupId>
        <artifactId>notifications-java-client</artifactId>
        <version>2.2.0-RELEASE</version>
    </dependency>

```

### Gradle
```
repositories {
    mavenCentral()
    maven {
        url  "http://dl.bintray.com/gov-uk-notify/maven"
    }
}

dependencies {
    compile('uk.gov.service.notify:notifications-java-client:2.2.0-RELEASE')
}
```

### Artifactory or Nexus

Click 'set me up!' on https://bintray.com/gov-uk-notify/maven/notifications-java-client for instructions.

## Getting started


```java
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationList;
import uk.gov.service.notify.NotificationResponse;

NotificationClient client = new NotificationClient(apiKey);
```

Generate an API key by signing in to
[GOV.UK Notify](https://www.notifications.service.gov.uk) and going to
the **API integration** page.

## Send a message

Text message:

```java
NotificationResponse response = client.sendSms(templateId, mobileNumber);
```

Email:

```java
NotificationResponse response = client.sendEmail(templateId, emailAddress);
```

Find `templateId` by clicking **API info** for the template you want to send.

If a template has placeholders, you need to provide their values in `personalisation`, for example:

```java
HashMap<String, String> personalisation = new HashMap<>();
personalisation.put("name", "Jo");
personalisation.put("reference_number", "13566");
NotificationResponse response = client.sendEmail(emailTemplateId,
        emailAddress, personalisation);
```

## Get the status of one message

```java
Notification notification = client.getNotificationById(notificationId);
```
 
## Get the status of all messages

```java
Notification notification = client.getNotification(status, notificationType);
```

Optional `status` can be one of:

* `null` (returns all messages)
* `created`
* `sending`
* `delivered`
* `permanent-failure`
* `temporary-failure`
* `technical-failure`

Optional `notificationType` can be one of:

* `null` (returns all messages)
* `email`
* `sms`

## Testing

Still testing......
