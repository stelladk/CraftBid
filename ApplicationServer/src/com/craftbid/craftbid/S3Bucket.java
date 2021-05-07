package com.craftbid.craftbid;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
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
	
	/**
	 * Uploads file to specific folder in S3 bucket
	 * @param folderName the name of the folder in S3 bucket
	 * @param fileName the name of the file shown in S3 bucket
	 * @param file File object to be uploaded
	 * @param s3Connection
	 */
	public static void addToFolder(String folderName, String fileName, File file, AmazonS3 s3Connection) {
		PutObjectRequest request = new PutObjectRequest(Constants.BUCKET_NAME, folderName+"/"+fileName, file);
		s3Connection.putObject(request);
	}
	
	/**
	 * Gets file from specific folder in S3 bucket and stores it locally
	 * @param folderName the name of the folder in S3 bucket
	 * @param fileName the file to be retrieved and stored locally
	 * @param s3Connection
	 */
	public static void getFromFolder(String folderName, String fileName, AmazonS3 s3Connection) {
		S3Object image = s3Connection.getObject(Constants.BUCKET_NAME, folderName+"/"+fileName);
		S3ObjectInputStream in = image.getObjectContent();
		
		// where the images will be stored locally (to be changed for Android)
		String outPath = "src/"+fileName;
		
		try {
			Files.copy(in, Paths.get(outPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes file in specific folder in S3 bucket
	 * @param folderName folder in S3 bucket
	 * @param fileName file's name to be deleted
	 * @param s3Connection
	 */
	public static void deleteFile(String folderName, String fileName, AmazonS3 s3Connection) {
		s3Connection.deleteObject(Constants.BUCKET_NAME, folderName+"/"+fileName);
	}
}
