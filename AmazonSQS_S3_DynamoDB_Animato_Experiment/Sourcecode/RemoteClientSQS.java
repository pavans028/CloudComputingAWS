
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * This sample demonstrates how to make basic requests to Amazon SQS using the
 * AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use Amazon SQS. For more information on Amazon
 * SQS, see http://aws.amazon.com/sqs.
 * <p>
 * Fill in your AWS access credentials in the provided credentials file
 * template, and be sure to move the file to the default location
 * (C:\\Users\\HP\\.aws\\credentials) where the sample code will load the
 * credentials from.
 * <p>
 * <b>WARNING:</b> To avoid accidental leakage of your credentials, DO NOT keep
 * the credentials file in your source directory.
 */

/**
 * Class name: RemoteClientSQS
 * @author Pavankumar Bhujanga Shetty
 * @version 1.0
 *  
 * AmazonS3 s3 - Holds the s3 client to upload the file into S3
 * AmazonDynamoDBClient dynamoDB - Holds DynamoDB Client
 * AmazonSQS sqs - Holds SQS Client
 * 
 */


public class RemoteClientSQS {

	static ConcurrentHashMap<Integer, String> mySubmittedTasks;

	static AmazonDynamoDBClient dynamoDB;
	static AmazonSQS sqs;

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
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		dynamoDB.setRegion(usWest2);
		sqs.setRegion(usWest2);
	}

	public static void main(String[] args) throws Exception {

		initSQSandDynamoDB(); // initializes both SQS and DYNAMODB

		double startTime, endTime, totalTime;

		// String fileName =
		// "C:/Cloud/Assignment/LocalQueueAsst3/src/cloud/asst3/queue/10KSleepTasks.txt";
		// int noOfThreads = 16 ;
		// String localOrRemote = "REMOTE";
		// String clientOrWorker = "CLIENT";

		String fileName = args[4];
		String requestQueueName = "MyRequestQueue" + args[2]; // Request Queue to put tasks in.
		String responseQueueName = "MyResponseQueue" + args[2]; // Response Queue to put tasks in.
		String clientOrWorker = args[0];

		System.out.println("===========================================");
		System.out.println("Lets get started with Amazon SQS");
		System.out.println("===========================================\n");

		try {
			// Create a queue
			System.out.println("Creating a new SQS queue called MyRequestQueue.\n");
			CreateQueueRequest createRequestQueue = new CreateQueueRequest(requestQueueName);
			String myRequestQueueUrl = sqs.createQueue(createRequestQueue).getQueueUrl();

			System.out.println("Creating a new SQS queue called MyResponseQueue.\n");
			CreateQueueRequest createResponseQueue = new CreateQueueRequest(responseQueueName);
			String myResponseQueueUrl = sqs.createQueue(createResponseQueue).getQueueUrl();

			// List queues
			System.out.println("Listing all queues in your account.\n");
			for (String queueUrl : sqs.listQueues().getQueueUrls()) {
				System.out.println("  QueueUrl: " + queueUrl);
			}

			// Send a message
			System.out.println("Sending a message to " + requestQueueName);

			int taskID = 1;
			FileReader fileReader;
			BufferedReader bufferedReader = null;
			startTime = System.nanoTime();
			try {
				fileReader = new FileReader(fileName);
				bufferedReader = new BufferedReader(fileReader);
				String eachTaskLine = null;
				mySubmittedTasks = new ConcurrentHashMap<Integer, String>(); // ConcurrentHashMap is used to store the task after sending to verify later with response queue.

				while ((eachTaskLine = bufferedReader.readLine()) != null) {
					sqs.sendMessage(new SendMessageRequest(myRequestQueueUrl, taskID + " " + eachTaskLine));// a space character is used to distinguish between taskId and task description.
					mySubmittedTasks.put(taskID, eachTaskLine);
					taskID++;
				}
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Sent all the messages to " + requestQueueName);

			try {
				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myResponseQueueUrl);// used to put requests in the queue
				receiveMessageRequest.setVisibilityTimeout(900); // This is to override the default visibility timeout of the messages in queue.
				receiveMessageRequest.setWaitTimeSeconds(20); // This is to inceare the wait time for messages/tasks.

				while (!mySubmittedTasks.isEmpty()) {
					List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
					for (Message message : messages) {
						if (mySubmittedTasks.containsKey(Integer.parseInt(message.getBody().toString()))) {
							mySubmittedTasks.remove(Integer.parseInt(message.getBody().toString()));
							String messageReceiptHandle = message.getReceiptHandle();
							sqs.deleteMessage(new DeleteMessageRequest(myResponseQueueUrl, messageReceiptHandle));
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (!mySubmittedTasks.isEmpty()) {
				System.out.println("Some tasks have failed");
			}
			endTime = System.nanoTime();
			totalTime = (endTime - startTime) / 1000000000.0;
			System.out.println("This is " + clientOrWorker.toUpperCase() + " running with workers.");
			System.out.println("Total time taken: " + totalTime);

			System.out.println("Received all the messages from MyResponseQueue.\n");

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
}
