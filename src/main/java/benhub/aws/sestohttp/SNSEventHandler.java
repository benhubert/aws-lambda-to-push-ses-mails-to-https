package benhub.aws.sestohttp;

import benhub.aws.sestohttp.api.ApiClient;
import benhub.aws.sestohttp.dto.EventContent;
import benhub.aws.sestohttp.dto.Header;
import benhub.aws.sestohttp.dto.serialize.DtoSerializer;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;

public class SNSEventHandler {

    private final ApiClient apiClient;
    private final AmazonS3 s3Client;
    private final Context context;
    private final DtoSerializer serializer;

    public SNSEventHandler(ApiClient apiClient,
                           AmazonS3 s3Client,
                           Context context) {
        this.apiClient = apiClient;
        this.s3Client = s3Client;
        this.context = context;
        this.serializer = new DtoSerializer();
    }

    public void handleEvent(String jsonContent) {
        log("Handling received SNS event.");
        EventContent eventContent = serializer.deserialize(jsonContent);
        String bucketName = eventContent.getReceipt().getAction().getBucketName();
        String objectKey = eventContent.getReceipt().getAction().getObjectKey();

        log("Fetching mail from S3: " + bucketName + ", " + objectKey + ".");
        String mailContent = s3Client.getObjectAsString(bucketName, objectKey);

        log("Pushing mail to API.");
        handleMail(eventContent, mailContent);

        log("Deleting mail from S3: " + bucketName + ", " + objectKey + ".");
        s3Client.deleteObject(bucketName, objectKey);
    }

    private void handleMail(EventContent eventContent, String mailContent) {
        String to = eventContent.getMail().getHeader("To").map(Header::getValue).orElse("");
        apiClient.pushMailToApi(to, mailContent);
    }

    private void log(String line) {
        if (this.context != null) {
            this.context.getLogger().log(line);
        }
    }

}
