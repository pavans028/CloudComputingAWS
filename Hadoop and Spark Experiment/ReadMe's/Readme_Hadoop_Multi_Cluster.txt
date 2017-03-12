--------------------SETTING UP MULTI_NODE CLUSTER---------------------

Open all instancecs with hadoop installed AMI's and place the public dns and private ipaddress to configure the Multi-Node Cluster
----------------------------------------------------------------------------------------
Change the permissions of the pem key for SSH to datanodes.

my Linux terminal$ sudo chmod 600 ~/.ssh/HadoopPshetty4New.pem

Now let's SSH into the machine with the following SSH command template:

my Linux terminal$ ssh -i ~/.ssh/HadoopPshetty4New.pem ubuntu@ec2_instance_public_dns
----------------------------------------------------------------------------------------------------
Setting up a config file in the ~/.ssh directory to ease of connecting to instance.

my Linux terminal$ ~/.ssh/config file. 

//Create one, if it does not exist. Config file is also submitted along with this submission.
---------------------------------------------------------------------------------------------------------------
my Linux terminal$ scp /home/ubuntu/.ssh/HadoopPshetty4New.pem  ~/.ssh/config namenode:~/.ssh

The authenticity of host 'ec2-52-91-102-135.compute-1.amazonaws.com (172.31.4.185)' can't be established.
ECDSA key fingerprint is 30:c0:45:72:05:df:8c:3e:1f:46:14:89:44:f9:2c:7d.
Are you sure you want to continue connecting (yes/no)? yes
Warning: Permanently added 'ec2-52-91-102-135.compute-1.amazonaws.com,172.31.4.185' (ECDSA) to the list of known hosts.
HadoopPshetty4New.pem                                                                                                                                      100% 1692     1.7KB/s   00:00
config                                                                                                                                                     100% 2095     2.1KB/s   00:00
-----------------------------------------------------------------------------------------------------------------------------

my Linux terminal$ ssh namenode

// connects to the master node.
------------------------------------------------------------------------------
On the NameNode, create the public/private rsa keypair using ~/.ssh/id_rsa.pub, 
and add it to it's authorized_keys

namenode$ ssh-keygen -f ~/.ssh/id_rsa -t rsa -P ""
namenode$ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
---------------------------------------------------------------------------------------

Copy the public/private rsa keypair to each DataNode's ~/.ssh/authorized_keys 
to enable the passwordless SSH capabilities from the NameNode to any DataNode.

namenode$ cat ~/.ssh/id_rsa.pub | ssh datanode1 'cat >> ~/.ssh/authorized_keys'
namenode$ cat ~/.ssh/id_rsa.pub | ssh datanode2 'cat >> ~/.ssh/authorized_keys'
--------------------------------------------------------------------------------------
Try doing ssh to datanode,

namenode$ ssh datanode1
----------------------------------------------------------------------------------------------------


%%%%%%%%%%%%%%%%%%%%%%%%%   NameNode and DataNode COMMON configurations %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

Open /home/ubuntu/hadoop-2.7.2/etc/hadoop for all the configuration files.

$HADOOP_CONF_DIR :- /home/ubuntu/hadoop-2.7.2/etc/hadoop

********************************************
sudo vim $HADOOP_CONF_DIR/hadoop-env.sh

// Uncomment and add this below line

# The java implementation to use.
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
*****************************************************

********************************************
sudo vim $HADOOP_CONF_DIR/core-site.xml

// change the localhost to namenode_public_dns in all datanodes

<configuration>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://namenode_public_dns:9000</value>
  </property>
</configuration>
*****************************************************

******************************************************
sudo vim $HADOOP_CONF_DIR/yarn-site.xml

// add these configuration lines in it

<configuration>
<!-- Site specific YARN configuration properties -->
  <property>
    <name>yarn.nodemanager.aux-services</name>
    <value>mapreduce_shuffle</value>
  </property> 
  <property>
    <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
    <value>org.apache.hadoop.mapred.ShuffleHandler</value>
  </property>
  <property>
    <name>yarn.resourcemanager.hostname</name>
    <value>namenode_public_dns</value>
  </property>
</configuration>
*********************************************************************

***********************************************************************************
sudo cp $HADOOP_CONF_DIR/mapred-site.xml.template $HADOOP_CONF_DIR/mapred-site.xml

sudo vim mapred-site.xml

<configuration>
  <property>
    <name>mapreduce.jobtracker.address</name>
    <value>namenode_public_dns:54311</value>
  </property>
  <property>
    <name>mapreduce.framework.name</name>
    <value>yarn</value>
  </property>
</configuration>
*******************************************************


%%%%%%%%%%%%%%%%%%%%%%%%%   NameNode configurations %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

*************************************************************
// adding hosts to /etc/hosts

sudo vi /etc/hosts

ec2-52-91-102-135.compute-1.amazonaws.com ip-172.31.4.185
ec2-52-87-151-144.compute-1.amazonaws.com ip-172.31.1.99
*************************************************************

*************************************************************
// Open up the hdfs-site.xml
sudo vim $HADOOP_CONF_DIR/hdfs-site.xml

	<property>
		<name>dfs.namenode.name.dir</name>
		<value>file:///home/ubuntu/hadoop-2.7.2/hadoop_data/hdfs/namenode</value>
	</property>

*************************************************************

************************************************************************
Add a masters file to config directory and add namenode hostname to it.

sudo touch $HADOOP_CONF_DIR/masters

sudo vim $HADOOP_CONF_DIR/masters

************************************************************

************************************************************************
Open up the slave file and add all the datanodes hostname to it.

sudo vim $HADOOP_CONF_DIR/slaves

************************************************************


%%%%%%%%%%%%%%%%%%%%%%%%%   DataNode configurations %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

*************************************************************
// Open up the hdfs-site.xml
sudo vim $HADOOP_CONF_DIR/hdfs-site.xml

	<property>
		<name>dfs.namenode.name.dir</name>
		<value>file:///home/ubuntu/hadoop-2.7.2/hadoop_data/hdfs/datanode</value>
	</property>

*************************************************************

%%%%%%%%%%%%%%%%%%%%%%%%%   Starting the cluster %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

Format the namenode first and then launch all dfs and yarn, either separately using start-dfs.sh and start-yarn.sh

namenode$ ~/hadoop-2.7.2/bin/hdfs namenode -format
namenode$ ~/hadoop-2.7.2/sbin/start-dfs.sh
namenode$ ~/hadoop-2.7.2/sbin/start-yarn.sh
			OR
namenode$ ~/hadoop-2.7.2/bin/hdfs namenode -format
namenode$ ~/hadoop-2.7.2/sbin/start-all.sh

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% RUNNING THE APPLICATION %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

****************************************************************
// Generate 100GB unsorted data using gensort
sudo ~/64/./gensort -a 1000 ~/hadoop-2.7.2/bin/100GBUnsorted
****************************************************************

****************************************************************
// make your own directory in HDFS
./hdfs dfs -mkdir -p /user/pshetty4/inputfolder
****************************************************************

****************************************************************
// Put the data in HDFS
./hadoop fs -put 100GBUnsorted /user/pshetty4/inputfolder/
****************************************************************

****************************************************************
// Remove the local 100GBUnsorted file
rm 100GBUnsorted
****************************************************************

****************************************************************
// Run the jar 
./hadoop jar HadoopSortSingleNode.jar HadoopSortSingleNode /user/pshetty4/inputfolder 100GBSorted
****************************************************************

****************************************************************
// Check the hdfs for file 
./hadoop fs -ls
****************************************************************

****************************************************************
// get the sorted file from  hdfs locally 
./hadoop fs -get 100GBSorted .
****************************************************************

****************************************************************
// Traverse till the output file and convert the file to dos
 to run valsort

 cd 100GBSorted/
unix2dos part-r-00000
~/64/./valsort part-r-00000
****************************************************************

****************************************************************
// Get the top ten sorted keys
head -10 part-r-00000

// Get the last ten sorted keys
tail -10 part-r-00000
****************************************************************
