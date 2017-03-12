SPARK - Multi Node
--------------------------------------------
*** Use the instance which has spark installed and open the ec2 folder of spark directory.

ubuntu@ip-172-31-49-133:~/spark-1.6.0-bin-hadoop2.6$ cd spark/ec2

*** Export the AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY to the ec2 folder.

ubuntu@ip-172-31-49-133:~/spark-1.6.0-bin-hadoop2.6/ec2$ export AWS_ACCESS_KEY_ID=*********************
ubuntu@ip-172-31-49-133:~/spark-1.6.0-bin-hadoop2.6/ec2$ export AWS_SECRET_ACCESS_KEY=******************


*** Change the mode for SSH to work between master and workers

ubuntu@ip-172-31-49-133:~/spark-1.6.0-bin-hadoop2.6/ec2$ chmod 400 SparkPshetty416Nodes.pem

*** Using the AMI which has spark and hadoop installed, Launch a 16 nodes cluster using "spark-ec2" command.
*********** ./spark-ec2 -k <keypair> 
	-i <key-file> -s <num-slaves> 
	launch <cluster-name>,
	where <keypair> is the name of your EC2 key pair,
	<key-file> is the private key file for your key pair, 
	<num-slaves> is the number of slave nodes to launch 
	<cluster-name> is the name to give to your cluster. **************

ubuntu@ip-172-31-49-133:~/spark-1.6.0-bin-hadoop2.6/ec2$ ./spark-ec2 -k SparkPshetty416Nodes -i SparkPshetty416Nodes.pem -s 16 --instance-type=c3.large --region=us-east-1 --ami=ami-7b0fe51b --spot-price=0.035 --hadoop-major-version=yarn launch 16NodesSpark100GB

					OR
					
./spark-ec2 -k SparkPshetty416West -i SparkPshetty416West.pem -s 16 --instance-type=c3.large --region=us-west-2 --ami=ami-940ee4f4 --spot-price=0.035 --hadoop-major-version=yarn launch 16NodesSpark100GB
					

****************************************************************************************************************************************

*** Now connect to the master node using the pem file and with root as an user, as it is amazon AMI. 
	Before this copy the gensort and valsort to the master instance.

My Linux Terminal$ scp -i ~/SparkPshetty416Nodes.pem -r 64/ root@ec2-54-152-185-65.compute-1.amazonaws.com:~
My Linux Terminal$ ssh -i ~/SparkPshetty416Nodes.pem root@ec2-54-152-185-65.compute-1.amazonaws.com

*** Create the 100 GB unsorted file using gensort

root@ip-172-31-37-82$ ~/64/gensort -a 1000000000 100GBUnsorted

*** Go to hadoop directory and put the unsorted file into HDFS.

root@ip-172-31-37-82 ~]$ cd ephemeral-hdfs/
root@ip-172-31-37-82 ephemeral-hdfs]$ ./hadoop fs -put ~/100GBUnsorted /

*** Submit the job using SortTheKeySingleNode.py file.

root@ip-172-31-37-82 bin]$ vim ~/SortTheKeySingleNode.py
root@ip-172-31-37-82 bin]$ ./spark-submit ~/SortTheKeySingleNode.py

*** TO stop the cluster use destroy.
./spark-ec2 destroy 16NodesSpark100GB

