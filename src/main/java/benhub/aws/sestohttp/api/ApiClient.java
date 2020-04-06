package benhub.aws.sestohttp.api;

public interface ApiClient {

    /**
     * Pushes the given mail message to the API.
     * @param to   The content of the To: header.
     * @param body The message body.
     * @return True, if the push was successful and the message can be safely
     * deleted from the storage.
     * @throws IllegalStateException if the push should be retried later.
     */
    boolean pushMailToApi(String to, String body);

}
