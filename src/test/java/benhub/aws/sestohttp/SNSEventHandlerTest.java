package benhub.aws.sestohttp;

import benhub.aws.sestohttp.api.ApiClient;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SNSEventHandlerTest {

    @Test
    public void test_handleEvent_pushesExtractedDataToAPI() {
        String event = Samples.open("event-1");
        ApiClient apiClient = Mockito.mock(ApiClient.class);
        AmazonS3 s3Client = Mockito.mock(AmazonS3.class);
        Mockito.when(s3Client.getObjectAsString(Mockito.any(String.class), Mockito.any(String.class))).thenReturn("some text");

        SNSEventHandler requestHandler = new SNSEventHandler(apiClient, s3Client, null);

        requestHandler.handleEvent(event);

        Mockito.verify(apiClient, Mockito.atLeastOnce())
                .pushMailToApi("c292df2c-d2f2-47c2-9ca8-c84b03389282@any.domain.example.com", "some text");
    }

}
