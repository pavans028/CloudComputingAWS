#include <iostream>
#include <fstream>
#include <cstdlib>
#include <pthread.h>
#include <unistd.h>
#include <sys/time.h>
#include <limits.h>

using namespace std;

#define NUM_THREADS 30
long loop_count = 1000000000; // no of times the loop has run
struct timeval time_start, time_end; // timeval struct variables to pass a refernece for gettimeofday 

void* iops_benchmarking(void *) //function for input/output operations
{
	int v=233, w=222, x=755, y=563, z=676, result;
	for(long i=0;i<loop_count;i++){
		result = ((y*v*w*x*(y+z)*x*z)+((v*x*w*(z+w))+(z*w*y*(z+x)))+(w*(z+w*(v*x*(z*w*y*(z+x))))));
	}
	pthread_exit(NULL);
}

void* flops_benchmarking(void *) //function for floating point operations
{
  double v=2.4434, w=8.2233, x=7.3543, y=3.5546, z=6.4435, result;
  for(long i=0;i<loop_count;i++){
    result= ((y*v*w*x*(y+z)*x*z)+((v*x*w*(z+w))+(z*w*y*(z+x)))+(w*(z+w*(v*x*(z*w*y*(z+x))))));
  }
  pthread_exit(NULL);
}

int main ()
{
  int rc;
  pthread_t threads[NUM_THREADS]; //pthread library for thread creation
  pthread_attr_t attr;
  void *status; // status of the thread
  double total_duration, start_time, end_time;

  // Initialize and set thread joinable
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

/***************************************** IOPS BENCHMARKING *******************************************/
  // IOPS with 1, 2, 4 thread

  for(int i=1;i<5;i=i*2){
      
    cout << "IOPS using thread #"<< i << endl;
    gettimeofday(&time_start, NULL); // gettimeofday to get wall clock time for accurate benchmarking.
    start_time = time_start.tv_sec+(time_start.tv_usec/1000000.0);
    for(int j=0; j<i; j++){
      rc = pthread_create(&threads[j], NULL, iops_benchmarking, NULL ); //create threads
      if (rc){
        cout << "Error:unable to create thread," << rc << endl;
        exit(-1);
      }  
    }
    for(int j=0; j<i; j++){
      rc = pthread_join(threads[j], &status); //join threads
      if (rc){
        cout << "Error:unable to join," << rc << endl;
        exit(-1);
      }  
    }
    gettimeofday(&time_end, NULL);
    end_time = time_end.tv_sec+(time_end.tv_usec/1000000.0);
    total_duration = (double)end_time-start_time;
    cout<<"IOPS, thread count "<< i <<" = "<<(double)(((loop_count/(total_duration))*31* i )/1e9)<<" GIGA IOPS"<<endl; // 31 is the number of oerations
  }

  cout << "DONE WITH IOPS BENCHMARKING." << endl <<endl;

/***************************************** FLOPS BENCHMARKING *******************************************/
  // FLOPS with 1, 2, 4 thread

  for(int i=1;i<5;i=i*2){
      
    cout << "FLOPS using thread #"<< i << endl;
    gettimeofday(&time_start, NULL);
    start_time = time_start.tv_sec+(time_start.tv_usec/1000000.0);
    for(int j=0; j<i; j++){
      rc = pthread_create(&threads[j], NULL, flops_benchmarking, NULL );
      if (rc){
        cout << "Error:unable to create thread," << rc << endl;
        exit(-1);
      }  
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
    total_duration = (double)end_time-start_time;
    cout<<"FLOPS, thread count "<< i <<" = "<<(double)(loop_count*31* i /(total_duration))/1e9<<" GIGA FLOPS"<<endl;
  }
  cout << "DONE WITH FLOPS BENCHMARKING." << endl <<endl;
  
  cout << "END OF CPU BENCHMARKING." << endl <<endl;
  pthread_exit(NULL);
}