#include <iostream>
#include <fstream>
#include <cstdlib>
#include <pthread.h>
#include <unistd.h>

using namespace std;

#define NUM_THREADS 4
long flops_count;

void* flops_benchmarking(void *) //function for integer operations
{
  double v=2.4434, w=8.2233, x=7.3543, y=3.5546, z=6.4435, result;
  for(flops_count=0;;flops_count++){
    result = ((y*v*w*x*(y+z)*x*z)+((v*x*w*(z+w))+(z*w*y*(z+x)))+(w*(z+w*(v*x*(z*w*y*(z+x))))));
  }
  pthread_exit(NULL);
}

void* start_flops_loop(void *) //function for integer operations
{
  long previous_loop_count = flops_count;
  long total_no_of_loops;
  ofstream out_file("output_flops.txt");
  ostream &out = out_file;
  out_file << "FLOPS using 4 threads as a separate experiment"<< endl;
  for(int i=0; i<610;i++){
    total_no_of_loops = flops_count-previous_loop_count;
    out_file << total_no_of_loops*31*4 <<endl;
    previous_loop_count = flops_count;
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

/***************************************** FLOPS BENCHMARKING *******************************************/

  pthread_create (threads, NULL, start_flops_loop, NULL);
  cout << "FLOPS using 4 threads as a separate experiment and will run for 10 mins"<< endl;
  for( i=0; i < NUM_THREADS; i++ ){
    rc = pthread_create(&threads[i], NULL, flops_benchmarking, NULL );
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
  cout << "END OF CPU FLOPS BENCHMARKING." << endl <<endl;
}