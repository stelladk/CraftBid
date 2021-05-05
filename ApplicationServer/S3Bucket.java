import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.auth.AWSStaticCredentialsProvider;

public class S3Bucket {
	
	/**
	 * Connects IAM user to AWS S3
	 * @return S3 client
	 */
	public static AmazonS3 connectToBucket() {
		
		// credentials to connent to S3 bucket
		AWSCredentials credentials = new BasicAWSCredentials(Constants.ACCESS_KEY_ID, Constants.ACCESS_SEC_KEY);
		
		// create client connection 
		return AmazonS3ClientBuilder
				.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.US_EAST_2)
				.build();
	}
	
	/**
	 * Creates folder in S3 bucket
	 * @param folderName the name of the folder to be created
	 * @param s3Connection the connection to specific S3 bucket
	 */
	public static void createFolder(String folderName, AmazonS3 s3Connection) {
		// folder's metadata with 0 length
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		
		// create folder's empty content
		InputStream content = new ByteArrayInputStream(new byte[0]);
		
		// request to create folder
		PutObjectRequest request = new PutObjectRequest(Constants.BUCKET_NAME, folderName+"/", content, metadata);
		
		s3Connection.putObject(request);
	}
	
	/**
	 * Deletes folder from S3 bucket
	 * @param folderName the name of the folder
	 * @param s3Connection the connection between IAM user and S3 bucket
	 */
	public static void deleteFolder(String folderName, AmazonS3 s3Connection) {
		// folder's files
		List files = s3Connection.listObjects(Constants.BUCKET_NAME, folderName).getObjectSummaries();
		
		// delete all files in folder
		for (Object f: files) {
			S3ObjectSummary file = (S3ObjectSummary) f;
			s3Connection.deleteObject(Constants.BUCKET_NAME, file.getKey());
		}
		// delete folder
		s3Connection.deleteObject(Constants.BUCKET_NAME, folderName);
	}
}
