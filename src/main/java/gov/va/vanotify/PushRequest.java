package gov.va.vanotify;

/**
 * Represents Push notification request
 * To create an instance use the static builder with fluent API
 * {@link PushRequest.Builder}
 * {@link NotificationPushRequest.Builder}
 */
public class PushRequest extends NotificationPushRequest {
    private final Identifier recipientIdentifier;

    private PushRequest(Builder builder) {
        super(builder);
        this.recipientIdentifier = builder.recipientIdentifier;
        if (this.recipientIdentifier == null || !this.recipientIdentifier.getIdentifierType().equals(IdentifierType.ICN)) {
            throw new IllegalStateException("recipientIdentifier must be of type ICN");
        }
    }

    public Identifier getRecipientIdentifier() {
        return recipientIdentifier;
    }

    /**
     * Fluent API Builder for PushRequest
     * Please see {@link NotificationPushRequest.Builder} for builder methods shared between all push notification requests
     */
    public static class Builder extends NotificationPushRequest.Builder<PushRequest, Builder>{
        private Identifier recipientIdentifier;

        @Override
        protected Builder getInstance() {
            return this;
        }

        /**
         * Sets <b>required</b> recipientIdentifier.
         * @param recipientIdentifier  The mobile phone number
         * @return reference to itself (builder)
         */
        public Builder withRecipientIdentifier(Identifier recipientIdentifier) {
            this.recipientIdentifier = recipientIdentifier;
            return this;
        }

        /**
         * Builds {@link PushRequest}
         * @return <code>SmsRequest</code>
         * @throws IllegalStateException if any required fields are missing
         */
        @Override
        public PushRequest build() {
            return new PushRequest(this);
        }
    }
}
