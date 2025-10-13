package gov.va.vanotify;

import java.util.Map;

public abstract class NotificationPushRequest {
    protected final MobileAppType mobileApp;
    protected final String callbackUrl;
    protected final String templateId;
    protected final Map<String, ?> personalisation;

    protected NotificationPushRequest(Builder builder) {
        this.mobileApp = builder.mobileApp;
        this.callbackUrl = builder.callbackUrl;
        this.templateId = builder.templateId;
        this.personalisation = builder.personalisation;

        if (this.templateId == null || this.templateId.isEmpty()) {
            throw new IllegalStateException("Missing templateId");
        }
    }

    public MobileAppType getMobileApp() {
        return mobileApp;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Map<String, ?> getPersonalisation() {
        return personalisation;
    }

    public abstract static class Builder<T extends NotificationPushRequest, B extends Builder> {        
        protected MobileAppType mobileApp = MobileAppType.VA_FLAGSHIP_APP;
        protected String callbackUrl;
        protected String templateId;
        protected Map<String, ?> personalisation;

        protected abstract B getInstance();

        /**
         * Sets <b>optional</b> mobileApp.
         * Default value is {@link MobileAppType#VA_FLAGSHIP_APP}
         * @param mobileApp name of the PUSH mobile app
         * @return reference to itself (builder)
         */
        public B withMobileApp(MobileAppType mobileApp) {
            this.mobileApp = mobileApp;
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
         * Sets <b>required</b> templateId.
         * @param templateId    The VEText template id.
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
         * Abstract method for building a NotificationPushRequest
         * Use one of concrete implementations:
         * {@link PushRequest.Builder}
         *
         * @return a fully constructed NotificationPushRequest
         * @throws IllegalStateException if any required fields are missing
         */
        public abstract T build();
    }
}
