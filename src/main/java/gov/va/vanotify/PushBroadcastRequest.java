package gov.va.vanotify;

/**
 * Represents Push notification request
 * To create an instance use the static builder with fluent API
 * {@link PushBroadcastRequest.Builder}
 * {@link NotificationPushRequest.Builder}
 */
public class PushBroadcastRequest extends NotificationPushRequest {
    private final String topic_sid;

    private PushBroadcastRequest(Builder builder) {
        super(builder);
        this.topic_sid = builder.topic_sid;
        if (this.topic_sid == null || this.topic_sid.isEmpty()) {
            throw new IllegalStateException("missing ICN recipientIdentifier");
        }
    }

    public String getTopicSID() {
        return topic_sid;
    }

    /**
     * Fluent API Builder for PushBroadcastRequest
     * Please see {@link NotificationPushRequest.Builder} for builder methods shared between all push notification requests
     */
    public static class Builder extends NotificationPushRequest.Builder<PushBroadcastRequest, Builder>{
        private String topic_sid;

        @Override
        protected Builder getInstance() {
            return this;
        }

        /**
         * Sets <b>required</b> topic_sid.
         * @param topic_sid  The mobile phone number
         * @return reference to itself (builder)
         */
        public Builder withTopicSID(String topic_sid) {
            this.topic_sid = topic_sid;
            return this;
        }

        /**
         * Builds {@link PushBroadcastRequest}
         * @return <code>SmsRequest</code>
         * @throws IllegalStateException if any required fields are missing
         */
        @Override
        public PushBroadcastRequest build() {
            return new PushBroadcastRequest(this);
        }
    }
}
