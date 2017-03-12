import java.io.*;

class DiskBenchmarkThread implements Runnable{
	
	Thread tClient; // Thread instance
	int blockSize[] = {1, 1024, 1024*1024};
	
	// Constructor to initialize and make it as a separate thread
	public DiskBenchmarkThread() {
		tClient = new Thread(this);
		tClient.start();
	}
		
	@Override
	public void run() {
		
		//************ RANDOM WRITE OPERTION ***********//
		String fileNameToWriteRand = "RANDOM_WRITE.txt";
		//File fileWriteRand = new File(fileNameToWriteRand);
		System.out.println("-------RANDOM WRITE FUNCTION BENCHMARKING------");
		for(int i=0;i<blockSize.length;i++){
			randomWrite(fileNameToWriteRand, blockSize[i]);
		}
		//System.exit(0);
		
		//************ RANDOM READ OPERTION ***********//
		String fileNameToBeReadRand = "RANDOM_WRITE.txt";
		//File fileReadRand = new File(fileNameToBeReadRand);
		System.out.println("-------RANDOM READ FUNCTION BENCHMARKING------");
		for(int i=0;i<blockSize.length;i++){
			randomRead(fileNameToBeReadRand, blockSize[i]);
		}
		
		//************ SEQUENTIAL WRITE OPERTION ***********//
		String fileNameToWrite = "SEQUENTIAL_WRITE.txt";
		//File fileWrite = new File(fileNameToWrite);
		System.out.println("-------SEQUENTIAL WRITE FUNCTION BENCHMARKING------");
		for(int i=0;i<blockSize.length;i++){
			sequentialWrite(fileNameToWrite, blockSize[i]);
		}
		
		//************ SEQUENTIAL READ OPERTION ***********//
		String fileNameToBeRead = "SEQUENTIAL_WRITE.txt";
		//File fileRead = new File(fileNameToBeRead);
		System.out.println("-------SEQUENTIAL READ FUNCTION BENCHMARKING------");
		for(int i=0;i<blockSize.length;i++){
			sequentialRead(fileNameToBeRead, blockSize[i]);
		}	
		
	}
	
	///************* RANDOM WRITE OPERATION ************//
	public void randomWrite(String file, int blockSize) {
		double startTime, endTime, totalTime;
		try { 
			RandomAccessFile raf = new RandomAccessFile(file, "rw"); 
			byte[] byteArray = new byte[blockSize];
			startTime = System.nanoTime();
			for(int i=0; i<100; i++){
				raf.seek((2*i+file.length())%31);
				raf.write(byteArray);	
			}			
			endTime = System.nanoTime();
	        totalTime = (endTime - startTime)/1000000000.0;	  
	        System.out.println("Latency for block size randomWrite, "+blockSize+" B : "+(totalTime/(blockSize*100))*1000+"msecs");	            
	        System.out.println("Throughput for block size randomWrite, "+blockSize+" B : "+(100*blockSize)/(totalTime*1024*1024)+" MB/SEC");
			raf.close(); 
		} catch (Exception e) {
			System.out.println("This is RANDOM WRITE EXCEPTION");
			e.printStackTrace();
		}
	}
	
	///************* RANDOM READ OPERATION ************//
	public void randomRead(String file, int blockSize) {
		double startTime, endTime, totalTime;
		try { 
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			byte[] byteArray = new byte[blockSize];
			startTime = System.nanoTime();
			for(int i=0; i<100; i++){	
				raf.seek((2*i+file.length())%31);
				raf.read(byteArray);	            	
			}	
			endTime = System.nanoTime();
	        totalTime = (endTime - startTime)/1000000000.0;	 
	        System.out.println("Latency for block size randomRead, "+blockSize+" B : "+(totalTime/(blockSize*100))*1000+"msecs");	            
	        System.out.println("Throughput for block size randomRead, "+blockSize+" B : "+(100*blockSize)/(totalTime*1024*1024)+" MB/SEC");			
	        			
			raf.close(); 
		} catch (Exception e) {
			System.out.println("This is RANDOM READ EXCEPTION");
			e.printStackTrace(); 
		}  
	} 
	
	///************* SEQUENTIAL READ OPERATION ************//
	public static void sequentialRead(String fileToBeRead, int blockSize) {
		double startTime, endTime, totalTime;
		try { 
            FileInputStream fis = new FileInputStream(fileToBeRead);  
            byte[] byteArray = new byte[blockSize];
			startTime = System.nanoTime();
			for(int i=0; i<100; i++){					
				fis.read(byteArray);		            	
			}	
			endTime = System.nanoTime();
	        totalTime = (endTime - startTime)/1000000000.0;	            
	        System.out.println("Latency for block size sequentialRead, "+blockSize+" B : "+(totalTime/(blockSize*100))*1000+"msecs");	            
	        System.out.println("Throughput for block size sequentialRead, "+blockSize+" B : "+(100*blockSize)/(totalTime*1024*1024)+" MB/SEC");			
	        
            fis.close();     
 
		} catch (Exception e) {
			System.out.println("This is SEQUENTIAL READ EXCEPTION");
			e.printStackTrace(); 
		} 
	} 	

	
	///************* SEQUENTIAL READ OPERATION ************//
	public static void sequentialWrite(String fileToWrite, int blockSize) {
		double startTime, endTime, totalTime;
		try { 
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileToWrite));
			byte[] byteArray = new byte[blockSize]; 
            
            startTime = System.nanoTime();
			for(int i=0; i<100; i++){					
		           dos.write(byteArray);		            	
			}	
			endTime = System.nanoTime();
	        totalTime = (endTime - startTime)/1000000000.0;	            
	        System.out.println("Latency for block size sequentialWrite, "+blockSize+" B : "+(totalTime/(blockSize*100))*1000+"msecs");	            
	        System.out.println("Throughput for block size sequentialWrite, "+blockSize+" B : "+(100*blockSize)/(totalTime*1024*1024)+" MB/SEC");			
	        	
            dos.close();     
 
		} catch (Exception e) {
			System.out.println("This is SEQUENTIAL WRITE EXCEPTION");
			e.printStackTrace(); 
		} 
	}

}


/** THis is the mian function which instantiates the thread and run the disk benchmark.*/
public class DiskBenchmarking {
   public static void main(String args[]) throws InterruptedException {
	  
	  for(int i=1; i<3; i=i*2){
		  System.out.println("\nNo of threads running: "+i);
		  for(int j=0;j<i; j++){
			  //System.out.println(i);
			  new DiskBenchmarkThread(); 
		  }
		  Thread.sleep(5000); /* program wil sleep for 5 seconds after the thread count 1 experiement is done and later */
	  } 
   }   
}
