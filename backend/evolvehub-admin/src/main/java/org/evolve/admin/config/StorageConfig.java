package org.evolve.admin.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储配置（MinIO + Milvus）
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Configuration
public class StorageConfig {

    /**
     * MinIO 客户端
     */
    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    /**
     * Milvus 客户端
     */
    @Bean
    public MilvusServiceClient milvusClient(MilvusProperties properties) {
        return new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(properties.getHost())
                        .withPort(properties.getPort())
                        .build()
        );
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "storage.minio")
    public static class MinioProperties {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucketName = "evolvehub-kb";
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "storage.milvus")
    public static class MilvusProperties {
        private String host = "localhost";
        private Integer port = 19530;
        private String collectionName = "kb_chunks";
    }
}
