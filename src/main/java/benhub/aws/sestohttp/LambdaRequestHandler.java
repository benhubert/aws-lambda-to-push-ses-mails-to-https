package benhub.aws.sestohttp;

import benhub.aws.sestohttp.api.ApiClient;
import benhub.aws.sestohttp.api.SimpleApiClient;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LambdaRequestHandler implements RequestHandler<SNSEvent, Void> {

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        SNSEventHandler snsEventHandler = initializeEventHandler(context);
        event.getRecords().forEach(record -> Optional.of(record)
                .map(SNSEvent.SNSRecord::getSNS)
                .map(SNSEvent.SNS::getMessage)
                .ifPresent(snsEventHandler::handleEvent));
        return null;
    }

    private SNSEventHandler initializeEventHandler(Context context) {
        // initialize AWS S3 client
        String s3Region = System.getenv("AWS_S3_REGION");
        String s3AccessKey = System.getenv("AWS_S3_ACCESS_KEY");
        String s3SecretKey = System.getenv("AWS_S3_SECRET_KEY");
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(
                s3AccessKey,
                s3SecretKey
        );
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(s3Region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        // initialize API client
        String apiUrlTemplate = System.getenv("API_URL");
        Integer apiResponseExpected = Integer.parseInt(System.getenv("API_RESPONSE_EXPECTED"));
        Integer apiResponseRejected = Integer.parseInt(System.getenv("API_RESPONSE_REJECTED"));
        Map<String, String> headers = System.getenv().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("API_HEADER_"))
                .collect(Collectors.toMap(
                        e -> e.getKey()
                                .replaceFirst("^API_HEADER_", "")
                                .replaceAll("_", "-"),
                        e -> e.getValue()
                ));
        Duration connectTimeout = Duration.ofMillis(Long.parseLong(System.getenv("API_CLIENT_CONNECT_TIMEOUT_MILLIS")));
        Duration sendTimeout = Duration.ofMillis(Long.parseLong(System.getenv("API_CLIENT_SEND_TIMEOUT_MILLIS")));
        ApiClient apiClient = new SimpleApiClient(
                apiUrlTemplate,
                headers,
                apiResponseExpected,
                apiResponseRejected,
                connectTimeout,
                sendTimeout,
                context);

        // initialize event handler
        return new SNSEventHandler(apiClient, s3Client, context);
    }

}
