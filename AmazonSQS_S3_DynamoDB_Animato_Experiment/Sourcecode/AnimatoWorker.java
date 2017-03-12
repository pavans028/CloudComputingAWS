
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudsearchdomain.model.Bucket;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;


/**
 * Class name: AnimatoWorker
 * @author Pavankumar Bhujanga Shetty
 * @version 1.0
 *  
 * 
 * AmazonS3 s3 - Holds the s3 client to upload the file into S3
 * AmazonDynamoDBClient dynamoDB - Holds DynamoDB Client
 * AmazonSQS sqs - Holds SQS Client
 * 
 */


public class AnimatoWorker {

	static AmazonSQS sqs;
	static AmazonDynamoDBClient dynamoDB;
	static AmazonS3 s3;

	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed
	 * automatically. Client parameters, such as proxies, can be specified in an
	 * optional ClientConfiguration object when constructing a client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.ProfilesConfigFile
	 * @see com.amazonaws.ClientConfiguration
	 */

	private static void initSQSandDynamoDB() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * (C:\\Users\\****\\.aws\\credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (C:\\Users\\****\\.aws\\credentials), and is in valid format.", e);
		}
		dynamoDB = new AmazonDynamoDBClient(credentials);
		sqs = new AmazonSQSClient(credentials);
		s3 = new AmazonS3Client(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		dynamoDB.setRegion(usWest2);
		sqs.setRegion(usWest2);
		s3.setRegion(usWest2);

	}

	// Create a new item for database entry
	private static Map<String, AttributeValue> newItem(String taskID) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("taskID", new AttributeValue(taskID));
		return item;
	}

	public static void main(String[] args) throws Exception {

		initSQSandDynamoDB();
		try {

			String tableName = "Check-Task-Duplicacy" + args[2];
			// Create table if it does not exist yet
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// Create a table with a primary hash key named 'name', which
				// holds a string
				CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
						.withKeySchema(new KeySchemaElement().withAttributeName("taskID").withKeyType(KeyType.HASH))
						.withAttributeDefinitions(new AttributeDefinition().withAttributeName("taskID")
								.withAttributeType(ScalarAttributeType.S))
						.withProvisionedThroughput(
								new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
				TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest)
						.getTableDescription();
				System.out.println("Created Table: " + createdTableDescription);

				// Wait for it to become active
				System.out.println("Waiting for " + tableName + " to become ACTIVE...");
				Tables.awaitTableToBecomeActive(dynamoDB, tableName);
			}

			String noOfWorkers = args[4];
			String requestQueueName = "MyRequestQueue" + args[2]; //This is the request queue to put task in to workers.
			String responseQueueName = "MyResponseQueue" + args[2];
			String clientOrWorker = args[0];

			String bucketName = "animoto" + UUID.randomUUID();// Created a bucket for the assignment

			String key = "MyVideoFile";
			System.out.println("Accessing bucket " + bucketName + "\n");
			s3.createBucket(bucketName);

			// Create a queue
			// System.out.println("Accessing SQS queue: "+requestQueueName);
			String myRequestQueueUrl = sqs.createQueue(requestQueueName).getQueueUrl();

			// System.out.println("Creating a new SQS queue called
			// MyResponseQueue.\n");
			String myResponseQueueUrl = sqs.createQueue(responseQueueName).getQueueUrl();

			// Receive the messages
			try {
				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myRequestQueueUrl);
				receiveMessageRequest.setVisibilityTimeout(900);
				receiveMessageRequest.setWaitTimeSeconds(20);
				while (true) {
					List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
					// Throw exception when queue gets empty
					if (messages.isEmpty()) {
						break;
					}
					for (Message message : messages) {
						try {
							String[] splitTask = message.getBody().toString().split("~");
							// DynamoDB Duplicacy checks
							Map<String, AttributeValue> item = newItem(splitTask[0]);
							PutItemRequest putItemRequest = new PutItemRequest(tableName, item)
									.withConditionExpression("attribute_not_exists(taskID)");
							dynamoDB.putItem(putItemRequest);
							// System.out.println("splitTask[1]-->"+splitTask[1]);
							try {
								String urlList[] = splitTask[1].split(" ");
								Process fetchImageFromURL;
								String url = null;
								for (int i = 0; i < urlList.length; i++) {
									// url = "wget -O
									// /home/ubuntu/pics/pshetty4pic"+i+".png
									// http://goo.gl/uoBq8r";
									// wget will download the images from the url to the locall directory
									url = "wget -O /home/ubuntu/pics/pshetty4pic" + i + ".png " + urlList[i];
									// System.out.println("URL each
									// worker::>>"+url);
									fetchImageFromURL = Runtime.getRuntime().exec(url);
									fetchImageFromURL.waitFor();
								}

							} catch (Exception e) {

							}
							// delete from request queue once processed.
							String messageReceiptHandle = message.getReceiptHandle();
							sqs.deleteMessage(new DeleteMessageRequest(myRequestQueueUrl, messageReceiptHandle));
							// send the response back to client
							sqs.sendMessage(new SendMessageRequest(myResponseQueueUrl, splitTask[0]+"~"));

						} catch (ConditionalCheckFailedException e) {

						}
					}
				}
				try {
					// Used to create video out of all the images downloaded from the urls
					Process downloadVideo = Runtime.getRuntime().exec(
							"ffmpeg -t 60 -r 1/5 -i /home/ubuntu/pics/pshetty4pic%d.png -c:v libx264 -vf fps=25 -pix_fmt yuv420p /home/ubuntu/pshetty4.mp4");
					downloadVideo.waitFor();
					// System.err.println("Video is made");
				} catch (Exception e) {
					// System.err.println("Exception in creating video.");
					// e.printStackTrace();
				}
				try {
					// System.err.println("Before creating");
					// Create a file object needed to put in the amazon s3
					File fileToBeSent = new File("/home/ubuntu/pshetty4.mp4");
					s3.putObject(new PutObjectRequest(bucketName, key, fileToBeSent));
					// System.err.println("after creating and putting it");

					// Url is constructed in this way and can be accessed in the web console.
					String urlOfS3 = "http://" + bucketName + ".s3-website-us-west-2.amazonaws.com/" + key;
					// System.out.println(urlOfS3);
					// adding 17000 just a value to split the url from rest of the task ids
					sqs.sendMessage(new SendMessageRequest(myResponseQueueUrl, ("17000" + "~" + urlOfS3)));

				} catch (AmazonServiceException ase) {
					System.out.println("Caught an AmazonServiceException, which means your request made it "
							+ "to Amazon S3, but was rejected with an error response for some reason.");
					System.out.println("Error Message:    " + ase.getMessage());
					System.out.println("HTTP Status Code: " + ase.getStatusCode());
					System.out.println("AWS Error Code:   " + ase.getErrorCode());
					System.out.println("Error Type:       " + ase.getErrorType());
					System.out.println("Request ID:       " + ase.getRequestId());
				} catch (AmazonClientException ace) {
					System.out.println("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with S3, "
							+ "such as not being able to access the network.");
					System.out.println("Error Message: " + ace.getMessage());
				}

			} catch (Exception ex) {
				// ex.printStackTrace();
				// System.out.println("Caught after while true");
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}

	}
}
