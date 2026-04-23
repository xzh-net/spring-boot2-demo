package net.xzh.minio;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.http.Method;

/**
 * minio预签名生成服务
 * @author xzh
 *
 */
public class MinioPresignedService {
    
    private final MinioClient minioClient;
    private final String defaultBucket;
    
    public MinioPresignedService(String endpoint, String accessKey, 
                                        String secretKey, String defaultBucket) {
        this.defaultBucket = defaultBucket;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
    
    /**
     * 生成临时上传 URL
     * @param objectName 对象存储路径（如 "images/avatar.jpg"）
     * @param expirySeconds 有效期（秒），建议 300~3600
     * @return 预签名上传 URL
     */
    public String getUploadUrl(String objectName, int expirySeconds) throws Exception {
        return getUploadUrl(defaultBucket, objectName, expirySeconds);
    }
    
    /**
     * 生成临时上传 URL
     * @param bucketName 存储桶名称
     * @param objectName 对象存储路径
     * @param expirySeconds 有效期（秒），建议 300~3600
     * @return 预签名上传 URL
     */
    public String getUploadUrl(String bucketName, String objectName, int expirySeconds) throws Exception {
        // 确保存储桶存在
        ensureBucketExists(bucketName);
        
        // 生成预签名 PUT URL
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expirySeconds)
                .build()
        );
    }
    
    
    /**
     * 生成临时下载 URL
     * @param objectName 对象存储路径（如 "images/avatar.jpg"）
     * @param expirySeconds 有效期（秒），建议 300~3600
     * @return 预签名下载 URL
     */
    public String getDownloadUrl(String objectName, int expirySeconds) throws Exception {
        return getDownloadUrl(defaultBucket, objectName, expirySeconds);
    }
    
    /**
     * 生成临时下载 URL
     * @param bucketName 存储桶名称
     * @param objectName 对象存储路径
     * @param expirySeconds 有效期（秒），建议 300~3600
     * @return 预签名下载 URL
     */
    public String getDownloadUrl(String bucketName, String objectName, int expirySeconds) throws Exception {
        // 确保存储桶存在
        ensureBucketExists(bucketName);
        
        // 生成预签名 GET URL
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expirySeconds)
                .build()
        );
    }
    
    
    private void ensureBucketExists(String bucketName) throws Exception {
        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!bucketExists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
        }
    }
}