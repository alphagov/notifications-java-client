import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;
import uk.gov.service.notify.NotificationResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class TestNotificationClient {

    /**
     *  Args: secret, issuer, baseUrl
      * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        NotificationClient client = new NotificationClient(args[0], args[1], args[2]);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Select an option from the following options: \n 1 - create \n 2 - fetch \n 3 - fetch-all");
        String requestType = reader.readLine();
        System.out.println("requestType:" + requestType);
        switch(requestType){
            case "1":
                createPostRequest(client);
                break;
            case "2":
                createFetchNotificationByIdRequest(client);
                break;
            case "3":
                createFetchAllRequest(client);
                break;
            default:
                System.out.println("Invalid selection, please enter 1, 2 or 3");
                System.exit(1);
                break;
        }

    }

    private static void createFetchAllRequest(NotificationClient client) throws IOException, NotificationClientException {
        System.out.println("Select status type from following: \n 0 - all \n 1 - delivered \n 2 - failed \n 3 - sending");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String statusInt = reader.readLine();
        String status = null;
        switch(statusInt){
            case "0":
                status = null;
                break;
            case "1":
                status = "delivered";
                break;
            case "2":
                status = "failed";
                break;
            case "3":
                status = "sending";
                break;
            default:
                System.out.println("Invalid selection, please enter 0, 1, 2 or 3");
                System.exit(1);
                break;
        }
        System.out.println("Select notification type from following: \n 0 - both \n 1 - sms \n 2 - email");
        String typeInt = reader.readLine();
        String notificationType = null;
        switch(typeInt){
            case "0":
                notificationType = null;
                break;
            case "1":
                notificationType = "sms";
                break;
            case "2":
                notificationType = "email";
                break;
            default:
                System.out.println("Invalid selection, please enter 0, 1, 2");
                System.exit(1);
                break;
        }
        NotificationList notificationList = client.getNotifications(status, notificationType);
    }

    private static void createFetchNotificationByIdRequest(NotificationClient client) throws IOException, NotificationClientException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the notification id to fetch: ");
        String notificationId = reader.readLine();
        Notification notification = client.getNotificationById(notificationId);
    }

    private static void createPostRequest(NotificationClient client) throws IOException, NotificationClientException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter type of message (sms|email): ");
        String messageType = reader.readLine();

        String to = null;
        switch(messageType){
            case "sms":
                System.out.println("Enter the mobile number: ");
                to = reader.readLine();
                break;
            case "email":
                System.out.println("Enter the email address: ");
                to = reader.readLine();
                break;
            default:
                System.out.println("Please enter email or sms for messageType");
                System.exit(1);
                break;
        }

        System.out.println("Enter the template id: ");
        String templateId = reader.readLine();
        if(templateId == null || templateId.isEmpty()){
            System.out.println("Template id must be the uuid of the template and can not be empty.");
            System.exit(1);
        }
        System.out.println("Enter the personalisation (key:value, key:value): ");
        String personalisation = reader.readLine();
        HashMap<String, String> properties = new HashMap<>();
        if (personalisation != null && !personalisation.isEmpty()){
            String[] pairs = personalisation.split(",");
            for(String pair : pairs){
                String[] keyValue = pair.split(":");
                properties.put(keyValue[0], keyValue[1]);
            }
        }
        if (messageType.equals("sms")){
            NotificationResponse response = client.sendSms(templateId, to, properties);
        }
        else{
            NotificationResponse response = client.sendEmail(templateId, to, properties);
        }
    }
}
