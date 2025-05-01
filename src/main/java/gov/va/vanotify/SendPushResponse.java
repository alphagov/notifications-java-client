package gov.va.vanotify;

public class SendPushResponse {
    private final String result;

    public SendPushResponse(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "SendPushResponse{" +
                "result=" + result +
                '}';
    }
}

