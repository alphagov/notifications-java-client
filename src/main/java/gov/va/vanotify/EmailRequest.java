package gov.va.vanotify;

/**
 * Represents email notification request
 * To create an instance use the static builder with fluent API
 * {@link EmailRequest.Builder}
 * {@link NotificationRequest.Builder}
 */
public class EmailRequest extends NotificationRequest {
    private final String emailAddress;

    private EmailRequest(Builder builder) {
        super(builder);
        this.emailAddress = builder.recipient;
        if (this.missingRecipient() && this.recipientIdentifier == null) throw new IllegalStateException("Missing at least one of emailAddress and recipientIdentifier");
    }

    private boolean missingRecipient() {
        return this.emailAddress == null || this.emailAddress.isEmpty();
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    /**
     * Fluent API Builder for EmailRequest
     * Please see {@link NotificationRequest.Builder} for builder methods shared between all notification requests
     */
    public static class Builder extends NotificationRequest.Builder<EmailRequest, Builder> {
        @Override
        protected Builder getInstance() {
            return this;
        }

        /**
         * Sets <b>required</b> emailAddress.
         * @param emailAddress  The email address
         * @return reference to itself (builder)
         */
        public Builder withEmailAddress(String emailAddress) {
            this.withRecipient(emailAddress);
            return this;
        }

        /**
         * Builds {@link EmailRequest}
         * @return <code>EmailRequest</code>
         * @throws IllegalStateException if any required fields are missing
         */
        @Override
        public EmailRequest build() {
            return new EmailRequest(this);
        }
    }
}
