package uk.gov.service.notify;

public class NotificationClientException extends Exception
{
    private static final long serialVersionUID = 3l;
    private int httpResult;
    private String httpResponse;

    public NotificationClientException(Exception ex)
    {
        super(ex);
    }

    public NotificationClientException(String message)
    {
        super(message);
    }

    public NotificationClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    NotificationClientException(int httpResult, String httpResponse)
    {
        super("Status code: " + httpResult + " " + httpResponse);
        this.httpResult = httpResult;
        this.httpResponse = httpResponse;
    }

    public int getHttpResult()
    {
        return this.httpResult;
    }

    public String getHttpResponse()
    {
        return this.httpResponse;
    }
}
