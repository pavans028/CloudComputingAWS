** Copy the Spark folder into the instance, 
scp -i spark_node.pem /spark_folder ubuntu@spark_node_public_dns

** connect to the instance, 
ssh spark_node_public_dns

** Add the pem file
eval `ssh-agent -s`
chmod 600 SparkPshetty4pem.pem
ssh-add SparkPshetty4pem 

** Copy the SortTheKeySingleNode.py file to the bin.

** Using gensort, create 10GB of unsorted data,

./gensort -a 100000000 10GBUnsorted

** Submit the python file using spark-submit.
ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin$ ./spark-submit SortTheKeySingleNode.py

** Once the job is completed, traverse till the output file convert it to dos format.
ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin$ cd 10GBSorted/
ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin/10GBSorted$ unix2dos part-00000
ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin/10GBSorted$ ~/64/valsort part-00000

ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin$ cd 10GBSorted/
ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin/10GBSorted$ unix2dos part-00297
ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin/10GBSorted$ ~/64/valsort part-00297

** Finally get the first 10 and last 10 lines of the output file

ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin/10GBSorted$ head 10 part-00000

ubuntu@ip-172-31-3-205:~/spark-1.6.0-bin-hadoop2.6/bin/10GBSorted$ tail 10 part-00000
