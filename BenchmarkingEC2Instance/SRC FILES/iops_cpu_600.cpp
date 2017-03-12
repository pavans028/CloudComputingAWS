#include <iostream>
#include <fstream>
#include <cstdlib>
#include <pthread.h>
#include <unistd.h>

using namespace std;

#define NUM_THREADS 4
long iops_count;

void* iops_benchmarking(void *) //function for integer operations
{
  int v=2, w=8, x=7, y=3, z=6, result;
  for(iops_count=0;;iops_count++){
    ((y*v*w*x*(y+z)*x*z)+((v*x*w*(z+w))+(z*w*y*(z+x)))+(w*(z+w*(v*x*(z*w*y*(z+x)))))); 
  }
  pthread_exit(NULL);
}

void* start_iops_loop(void *) //function for integer operations
{
  long previous_loop_count = iops_count;
  long total_no_of_loops;
  ofstream out_file("output_iops.txt");
  ostream &out = out_file;
  out_file << "IOPS using 4 threads as a separate experiment"<< endl;
  for(int i=0; i<610;i++){
    total_no_of_loops = iops_count-previous_loop_count;
    out_file << total_no_of_loops*30*4 <<endl;
    previous_loop_count = iops_count;
    sleep(1);
  }  
}


int main ()
{
  int rc;
  int i;
  pthread_t threads[NUM_THREADS];
  pthread_attr_t attr;
  void *status;

  // Initialize and set thread joinable
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
  
  pthread_create (threads, NULL, start_iops_loop, NULL); 
  cout << "IOPS using 4 threads as a separate experiment and will run for 10 mins"<< endl;
  for( i=0; i < NUM_THREADS; i++ ){
    rc = pthread_create(&threads[i], NULL, iops_benchmarking, NULL );
    if (rc){
      cout << "Error:unable to create thread," << rc << endl;
      exit(-1);
    }
  }
  pthread_attr_destroy(&attr);
  for( i=0; i < NUM_THREADS; i++ ){
      rc = pthread_join(threads[i], &status);
      if (rc){
         cout << "Error:unable to join," << rc << endl;
         exit(-1);
      }
    }
  pthread_exit(NULL);
  cout << "END OF CPU IOPS BENCHMARKING." << endl << endl;
}