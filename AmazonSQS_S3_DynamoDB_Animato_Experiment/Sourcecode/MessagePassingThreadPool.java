/**
 * This program implements a CloudKon locally.
 */
package cloud.asst3.queue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class name: MessagePassingThreadPool
 * @author Pavankumar Bhujanga Shetty
 * @version 1.0
 *  
 * This is the class used to implement the publisher/Consumer implementation together
 * It creates the ThreadPool
 * It accepts 2 inline parameters while running args[0]- no of threads and args[1] - filename
 * 
 * Integer noOfThreads - 1,2,4,8,16
 * String fileName - Name of the file from where tasks are read from.
 * 
 */


public class MessagePassingThreadPool {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		ConcurrentLinkedQueue<TaskToQueueThreadPool> requestQueue = new ConcurrentLinkedQueue<TaskToQueueThreadPool>();
		ConcurrentLinkedQueue<TaskToQueueThreadPool> responseQueue = new ConcurrentLinkedQueue<TaskToQueueThreadPool>();
		double startTime, endTime, totalTime;
		
		//String fileName = "C:/Cloud/Assignment/LocalQueueAsst3/src/cloud/asst3/queue/1000Sleep10Tasks.txt";
		//int noOfThreads = 16;
		
		String fileName = args[6];
		int noOfThreads = Integer.parseInt(args[4]);
		String localOrRemote = args[2];
		String clientOrWorker = args[0];
		
		startTime = System.nanoTime(); //Returns the current value of the running Java Virtual Machine's high-resolution time source, in nanoseconds
		ExecutorService executorPublisher = Executors.newFixedThreadPool(1);//creating a pool of 1 thread  
		Runnable workerPublisher = new PublisherThreadPool(requestQueue, fileName);
		executorPublisher.execute(workerPublisher);
		executorPublisher.shutdown(); //Initiates an orderly shutdown 
		while (!executorPublisher.isTerminated()) {   //Returns true if all tasks have completed following shut down
     	   
        }
		ExecutorService executorConsumer = Executors.newFixedThreadPool(noOfThreads);//creating a pool of 16 thread
		for(int i=0;i<noOfThreads;i++) {  
			Runnable workerConsumer = new ConsumerThreadPool(requestQueue,responseQueue);
			executorConsumer.execute(workerConsumer); // Executes the given command at some time in the future. 
        }	
        executorConsumer.shutdown();  
        while (!executorConsumer.isTerminated()) {   
        	
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime)/1000000000.0;	  
        System.out.println(clientOrWorker+" running on "+localOrRemote+".");
        System.out.println("Total time taken for "+noOfThreads+" threads : "+totalTime);	
        boolean flagForResult = true;
        for(TaskToQueueThreadPool task: responseQueue){
        	if(!task.isStatus()){
        		flagForResult = false;
        		break;
        	}
        }
        if(!flagForResult){
        	System.out.println("Some jobs have failed!!!");
        }

	}
}

/**
 * Class name: PublisherThreadPool
 * @author Pavankumar Bhujanga Shetty
 * @version 1.0
 *  
 * This is the class used to implement the publisher/Client implementation
 * It will read the contents from the file
 * And puts into Queue
 * 
 * @param ConcurrentLinkedQueue<TaskToQueueThreadPool> requestQueue - Queue used by publisher to out all tasks/ Messages.
 * @param String fileName - Name of the file from where tasks are read from.
 * 
 */

class PublisherThreadPool implements Runnable {

	ConcurrentLinkedQueue<TaskToQueueThreadPool> requestQueue;
	TaskToQueueThreadPool task;
	int noOfThreads;
	String fileName;
	
	public PublisherThreadPool(ConcurrentLinkedQueue<TaskToQueueThreadPool> requestQueue, String fileName) {
		this.requestQueue = requestQueue;
		this.fileName = fileName;
	}

	public void run() {
				
		FileReader fileReader;
		BufferedReader bufferedReader = null;
		int taskid = 1;
		try {
			fileReader = new FileReader(fileName); // read filename and create a file reader.
			bufferedReader = new BufferedReader(fileReader);
			String eachTaskLine = null;
            while((eachTaskLine = bufferedReader.readLine()) != null) {   
            	task = new TaskToQueueThreadPool();
            	task.setStatus(false);
            	task.setTaskId(taskid++);
            	task.setTaskToSend(eachTaskLine);
            	requestQueue.offer(task); // internally calls add method
	        }
            bufferedReader.close();      
	            
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/**
 * Class name: ConsumerThreadPool
 * @author Pavankumar Bhujanga Shetty
 * @version 1.0
 *  
 * This is the class used to implement the worker implementation
 * 
 * @param ConcurrentLinkedQueue<TaskToQueueThreadPool> requestQueue - Queue used by publisher to out all tasks/ Messages.
 * @param ConcurrentLinkedQueue<TaskToQueueThreadPool> responseQueue - Queue used by Consumer to send back the task executed to publisher.
 * 
 */

class ConsumerThreadPool implements Runnable {

	ConcurrentLinkedQueue<TaskToQueueThreadPool> requestQueue;
	ConcurrentLinkedQueue<TaskToQueueThreadPool> responseQueue;

	public ConsumerThreadPool(ConcurrentLinkedQueue<TaskToQueueThreadPool> requestQueue,ConcurrentLinkedQueue<TaskToQueueThreadPool> responseQueue) {
		this.requestQueue = requestQueue;
		this.responseQueue = responseQueue;
	}

	public void run() {
		
		try {
			TaskToQueueThreadPool task;
			while (!requestQueue.isEmpty()) {
				task = requestQueue.poll(); // Retrieves and removes the head of this queue, or returns null if this queue is empty.
				task.setStatus(true);
				Thread.currentThread();
				Thread.sleep(Long.parseLong(task.getTaskToSend().toString().split(" ")[1]));
				responseQueue.offer(task); //Inserts the specified element at the tail of this queue. As the queue is unbounded, this method will never return false.
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

/**
 * Class name: TaskToQueueThreadPool
 * @author Pavankumar Bhujanga Shetty
 * @version 1.0
 *  
 * This is the object,
 * which is sent to the Queue by Producer
 * and the same is processed by consumer
 * 
 * @param Integer taskId - To keep track of Messages.
 * @param String taskToSend - Content of task to be processed by consumer.
 * @param Boolean status - To change the status once it is processed.
 */

class TaskToQueueThreadPool {

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}


	private int taskId;
    public String getTaskToSend() {
		return taskToSend;
	}

	public void setTaskToSend(String taskToSend) {
		this.taskToSend = taskToSend;
	}
	private boolean status;
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}


	private String taskToSend;

    @Override
    public String toString() {
        return "Message{ task Id:" + taskId + ",message: " + taskToSend + "}";
    }
}
