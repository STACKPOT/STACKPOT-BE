package stackpot.stackpot.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AmazonConfig {

    private AWSCredentials awsCredentials;

    @org.springframework.beans.factory.annotation.Value("${spring.cloud.aws.credentials.accessKey}")
    private String accessKey;

    @org.springframework.beans.factory.annotation.Value("${spring.cloud.aws.credentials.secretKey}")
    private String secretKey;

    @org.springframework.beans.factory.annotation.Value("${spring.cloud.aws.region.static}")
    private String region;

    @org.springframework.beans.factory.annotation.Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @org.springframework.beans.factory.annotation.Value("${spring.cloud.aws.s3.path.FeedFile}")
    private String FeedFilePath;

    @PostConstruct
    public void init() {
        this.awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
    }

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        return new AWSStaticCredentialsProvider(awsCredentials);
    }
}
