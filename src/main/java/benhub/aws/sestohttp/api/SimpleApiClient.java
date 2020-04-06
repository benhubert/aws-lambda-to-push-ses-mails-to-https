package benhub.aws.sestohttp.api;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.commons.text.StringSubstitutor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleApiClient implements ApiClient {

    private final String apiUrlTemplate;
    private final Map<String, String> httpHeaders;
    private final Integer expectedResponseCode;
    private final Integer rejectedResponseCode;
    private final HttpClient httpClient;
    private final Duration sendTimeout;
    private final Context context;

    public SimpleApiClient(String apiUrlTemplate,
                           Map<String, String> httpHeaders,
                           Integer expectedResponseCode,
                           Integer rejectedResponseCode,
                           Duration connectTimeout,
                           Duration sendTimeout,
                           Context context) {
        this.apiUrlTemplate = apiUrlTemplate;
        this.httpHeaders = httpHeaders;
        this.expectedResponseCode = expectedResponseCode;
        this.rejectedResponseCode = rejectedResponseCode;
        this.httpClient = createHttpClient(connectTimeout);
        this.sendTimeout = sendTimeout;
        this.context = context;
    }

    @Override
    public void pushMailToApi(String to, String body) {
        String url = buildUrl(to);
        log("Pushing mail to " + url + ".");
        HttpRequest request = buildHttpRequest(url, body);

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (expectedResponseCode.equals(response.statusCode())) {
                log("API return HTTP " + response.statusCode() + ". Done.");
            } else if (rejectedResponseCode.equals(response.statusCode())) {
                log("API returned HTTP " + response.statusCode() + " which is means that this mail is rejected permanently. Will drop it.");
            } else {
                log("API returned unexpected HTTP " + response.statusCode() + ". Will fail now and retry later.");
                throw new IllegalStateException("API endpoint returned unexpected response code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to push mail to API.", e);
        }

    }

    private HttpClient createHttpClient(Duration connectTimeout) {
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }

    private String buildUrl(String to) {
        Pattern mailAddressPattern = Pattern.compile("(.*)@(.*)");
        Matcher matcher = mailAddressPattern.matcher(to);
        String toLocalPart = matcher.matches() ? matcher.group(1) : "";
        String toDomain = matcher.matches() ? matcher.group(2) : "";

        HashMap<String, String> values = new HashMap<>();
        values.put("to.localpart", toLocalPart);
        values.put("to.domain", toDomain);

        StringSubstitutor stringSubstitutor = new StringSubstitutor(values);
        return stringSubstitutor.replace(apiUrlTemplate);
    }

    private HttpRequest buildHttpRequest(String url, String body) {
        URI uri = URI.create(url);
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("User-Agent", "aws-lambda-to-push-ses-mails-to-https/latest")
                .timeout(sendTimeout)
                .uri(uri);
        httpHeaders.forEach(request::header);
        return request.build();
    }

    private void log(String line) {
        if (this.context != null) {
            this.context.getLogger().log(line);
        }
    }

}
