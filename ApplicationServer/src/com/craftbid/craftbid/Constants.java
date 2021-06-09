package com.craftbid.craftbid;

/**
 * Contains keys and other constants used in database and S3 connection.  
 * */

//TODO fill in the credentials before running Server
public class Constants {
	/** the host of the database */
	public static final String HOST = "...";
	
	/** the port the database's host uses */
	public static final String PORT = "...";
	
	/** the database's name */
	public static final String DATABASE = "...";
	
	/** the administrator's username */
	public static final String USER = "...";
	
	/** the administrator's password */
	public static final String PASSWORD = "...";

	/** access key ID for IAM user with limited rights connection to S3 bucket */
	public static final String ACCESS_KEY_ID = "...";
	
	/** access secret key for IAM user with limited rights connection to S3 bucket */
	public static final String ACCESS_SEC_KEY = "...";
	
	/** the S3 bucket name */
	public static final String BUCKET_NAME = "...";
}
