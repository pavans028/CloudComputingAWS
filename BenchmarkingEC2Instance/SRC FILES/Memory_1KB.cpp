#include <iostream>
#include <fstream>
#include <cstdlib>
#include <pthread.h>
#include <unistd.h>
#include <memory.h>
#include <sys/time.h>

using namespace std;

#define NUM_THREADS 30
#define BUFFER_SIZE 1024

long loop_count = (25*1024*1024);
char *src_chunk, *dest_chunk;
struct timeval time_start, time_end; 
double total_duration;

void *sequential_memory(void*)
{
    //cout <<"In sequential read+write function and loopcount is " << loop_count <<endl;
    for(long sm = 0; sm < loop_count; sm += BUFFER_SIZE){
      memcpy(&dest_chunk[sm], &src_chunk[sm], BUFFER_SIZE);
    }
    pthread_exit(NULL);
}

void *random_memory(void*)
{
    long random_number;
    //cout <<"In random read+write function and loopcount is " << loop_count <<endl;
    for(long rm = 0; rm < loop_count; rm += BUFFER_SIZE){
      random_number = (loop_count*(rm+1)) % 961;
      memcpy(&dest_chunk[random_number], &src_chunk[random_number], BUFFER_SIZE);
    }
    pthread_exit(NULL);        
}

int main ()
{
  int rc;
  pthread_t threads[NUM_THREADS];
  pthread_attr_t attr;
  void *status;
  double start_time, end_time;
  src_chunk = new char[loop_count];
  dest_chunk = new char[loop_count];
  // Initialize source buffer
  for(int i=0;i<loop_count;i++){
    *(src_chunk+i) = 'x';
  }
  // Initialize and set thread joinable
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

  cout << "****************   1 KB BLOCK SIZE    **************" << endl << endl;

  cout << "****************   SEQUENTIAL MEMORY ACCESS    **************" << endl << endl;
  for(int i=1;i<3;i=i*2)
  {    
    gettimeofday(&time_start, NULL);
    start_time = time_start.tv_sec+(time_start.tv_usec/1000000.0);
    for(int j=0; j<i; j++){
      //cout << "\nCreating Thread: " << (i+1) << endl;
      rc = pthread_create(&threads[j],NULL, sequential_memory, NULL);
      if (rc){
         cout << "Error:unable to create thread," << rc << endl;
         exit(-1);
      }
      //sleep(1);
    } 
    for(int j=0; j<i; j++){
      rc = pthread_join(threads[j], &status);
      if (rc){
         cout << "Error:unable to join," << rc << endl;
         exit(-1);
      }
    }
    gettimeofday(&time_end, NULL);
    end_time = time_end.tv_sec+(time_end.tv_usec/1000000.0);
    total_duration = end_time - start_time;
    cout << "Thread count #"<< i << endl;
    cout << "Latency:" << (total_duration*1000)/(loop_count/BUFFER_SIZE) << "msecs"<< endl;
    cout << "Throughput for sequential read+write = " << (loop_count/(total_duration*1024*1024)) << " mbps" << endl << endl;
  }
  

  cout << "****************   RANDOM MEMORY ACCESS    **************" << endl << endl;
  for(int i=1;i<3;i=i*2)
  {    
    gettimeofday(&time_start, NULL);
    start_time = time_start.tv_sec+(time_start.tv_usec/1000000.0);
    for(int j=0; j<i; j++){
      //cout << "\nCreating Thread: " << (i+1) << endl;
      rc = pthread_create(&threads[j],NULL, random_memory, NULL);
      if (rc){
         cout << "Error:unable to create thread," << rc << endl;
         exit(-1);
      }
      //sleep(1);
    } 
    for(int j=0; j<i; j++){
      rc = pthread_join(threads[j], &status);
      if (rc){
         cout << "Error:unable to join," << rc << endl;
         exit(-1);
      }
    }
    gettimeofday(&time_end, NULL);
    end_time = time_end.tv_sec+(time_end.tv_usec/1000000.0);
    total_duration = end_time - start_time;
    cout << "Thread count #"<< i << endl;
    cout << "Latency:" << (total_duration*1000)/(loop_count/BUFFER_SIZE) << "msecs"<< endl;
    cout << "Throughput for random read+write = " << (loop_count/(total_duration*1024*1024)) << " mbps" << endl << endl;
  }  
  cout << "END OF MEMORY BENCHMARKING." << endl <<endl;
  pthread_exit(NULL);  
}