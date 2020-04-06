package benhub.aws.sestohttp.api;

public interface ApiClient {

    void pushMailToApi(String to, String body);

}
