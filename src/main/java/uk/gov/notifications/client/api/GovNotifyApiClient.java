package uk.gov.notifications.client.api;

import uk.gov.notifications.client.handler.EmailRequestHandler;
import uk.gov.notifications.client.handler.SmsRequestHandler;
import uk.gov.notifications.client.handler.StatusRequestHandler;
import uk.gov.notifications.client.http.ApacheGovNotifyHttpClient;
import uk.gov.notifications.client.http.GovNotifyHttpClient;
import uk.gov.notifications.client.model.EmailRequest;
import uk.gov.notifications.client.model.NotificationCreatedResponse;
import uk.gov.notifications.client.model.SmsRequest;
import uk.gov.notifications.client.model.StatusRequest;
import uk.gov.notifications.client.model.StatusResponse;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class GovNotifyApiClient implements GovNotifyApi {

    private ClientConfiguration configuration;
    private GovNotifyHttpClient httpClient;


    /**
     * Creates API client with specific configuration.
     * @param configuration configuration object
     */
    public GovNotifyApiClient(ClientConfiguration configuration) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this(configuration, new ApacheGovNotifyHttpClient());
    }

    /**
     * Creates API client with specific configuration and custom http client.
     * @param configuration configuration object
     * @param client custom http client
     */
    public GovNotifyApiClient(ClientConfiguration configuration, GovNotifyHttpClient client) {
        this.configuration = configuration;
        this.httpClient = client;
    }

    @Override
    public NotificationCreatedResponse sendEmail(EmailRequest request) throws
            GovNotifyClientException {
        return new EmailRequestHandler(configuration, httpClient).handle(request);
    }

    @Override
    public NotificationCreatedResponse sendEmail(String email, String templateId)
            throws GovNotifyClientException {
        return sendEmail(EmailRequest.builder().email(email).templateId(templateId).build());
    }

    @Override
    public NotificationCreatedResponse sendSms(SmsRequest request) throws GovNotifyClientException {
        return new SmsRequestHandler(configuration, httpClient).handle(request);
    }

    @Override
    public NotificationCreatedResponse sendSms(String phoneNumber, String templateId)
            throws GovNotifyClientException {
        return sendSms(
                SmsRequest.builder()
                        .phoneNumber(phoneNumber)
                        .templateId(templateId)
                        .build()
        );
    }

    @Override
    public StatusResponse checkStatus(StatusRequest request) throws GovNotifyClientException {
        return new StatusRequestHandler(configuration, httpClient).handle(request);
    }

    @Override
    public StatusResponse checkStatus(String notificationId) throws GovNotifyClientException {
        return checkStatus(StatusRequest.builder().notificationId(notificationId).build());
    }
}
