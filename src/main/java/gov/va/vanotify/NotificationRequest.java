package gov.va.vanotify;

import java.util.Map;

public abstract class NotificationRequest {
    protected final String templateId;
    protected final Map<String, ?> personalisation;
    protected final String reference;
    protected final String billingCode;
    protected final Identifier recipientIdentifier;
    protected final String callbackUrl;

    protected NotificationRequest(Builder builder) {
        this.templateId = builder.templateId;
        this.personalisation = builder.personalisation;
        this.reference = builder.reference;
        this.billingCode = builder.billingCode;
        this.recipientIdentifier = builder.recipientIdentifier;
        this.callbackUrl = builder.callbackUrl;

        if (this.templateId == null || this.templateId.isEmpty()) {
            throw new IllegalStateException("Missing templateId");
        }
    }

    public String getTemplateId() {
        return templateId;
    }

    public Map<String, ?> getPersonalisation() {
        return personalisation;
    }

    public String getReference() {
        return reference;
    }

    public String getBillingCode() {
        return billingCode;
    }

    public Identifier getRecipientIdentifier() {
        return recipientIdentifier;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public abstract static class Builder<T extends NotificationRequest, B extends Builder> {
        protected String templateId;
        protected String recipient;
        protected Map<String, ?> personalisation;
        protected String reference;
        protected String billingCode;
        protected Identifier recipientIdentifier;
        protected String callbackUrl;

        protected abstract B getInstance();

        /**
         * Sets <b>required</b> recipient.
         * @param recipient  The email address or phone number
         * @return reference to itself (builder)
         */
        protected B withRecipient(String recipient) {
            this.recipient = recipient;
            return this.getInstance();
        }

        /**
         * Sets <b>required</b> templateId.
         * @param templateId    The template id is visible on the template page in the application.
         * @return reference to itself (builder)
         */
        public B withTemplateId(String templateId) {
            this.templateId = templateId;
            return this.getInstance();
        }

        /**
         * Sets <b>optional</b> personalisations.
         * @param personalisation   Map representing the placeholders for the template if any. For example, key=name value=Bob
         * @return reference to itself (builder)
         */
        public B withPersonalisation(Map<String, ?> personalisation) {
            this.personalisation = personalisation;
            return this.getInstance();
        }

        /**
         * Sets <b>optional</b> reference
         * @param reference A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
         *                  This reference can be unique or used used to refer to a batch of notifications.
         * @return reference to itself (builder)
         */
        public B withReference(String reference) {
            this.reference = reference;
            return this.getInstance();
        }

        /**
         * Sets <b>optional</b> billing code.
         *
         * @param billingCode   A billing code specified by the service for the notification.
         *                      Used to group notifications for billing and reporting.
         * @return reference to itself (builder)
         */
        public B withBillingCode(String billingCode) {
            this.billingCode = billingCode;
            return this.getInstance();
        }

        /**
         * Sets <b>optional</b> callback URL.
         *
         * @param callbackUrl   A URL to which delivery status updates are sent.
         *                      Must be a valid URI with a maximum length of 255 characters.
         * @return reference to itself (builder)
         */
        public B withCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this.getInstance();
        }

        /**
         * Sets VA identifier that is supported by Master Person Index
         *
         * @param recipientIdentifier   An object representing recipient identifier supported by MPI (Master Person Index) such as ICN, Participant Id, etc.
         *                              This identifier will be used to query contact information and communication permissions (preferences) in VA Profile.
         *                              Either recipientIdentifier or email address / phone number must be provided
         * @return reference to itself (builder)
         */
        public B withRecipientIdentifier(Identifier recipientIdentifier) {
            this.recipientIdentifier = recipientIdentifier;
            return this.getInstance();
        }

        /**
         * Abstract method for building a NotificationRequest
         * Use one of concrete implementations:
         * {@link SmsRequest.Builder}
         * {@link EmailRequest.Builder}
         *
         * @return a fully constructed NotificationRequest
         * @throws IllegalStateException if any required fields are missing
         */
        public abstract T build();
    }
}
