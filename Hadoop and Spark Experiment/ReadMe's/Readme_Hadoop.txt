Open up the c3.large instance.

Send the files, "HadoopPshetty4New.pem" file, 64 folder(which has Gensort and Valsort) and connect to it and follow the following steps to setup and run the sort program on hadoop. 

// add the pem file to the instance
eval `ssh-agent -s`
chmod 600 HadoopPshetty4New.pem
ssh-add HadoopPshetty4New.pem


:1,$d // to delete all lines in Vi editor.


// Create a jar for your mapper reducer sorting java file.

vim HadoopSortSingleNode.java
./hadoop com.sun.tools.javac.Main HadoopSortSingleNode.java
jar cf HadoopSortSingleNode.jar HadoopSortSingleNode*.class

// Change the configuration files in ~/hadoop-2.7.2/etc/hadoop$

1. vi core-site.xml

// Add the below configuration in this file.

<configuration>
        <property>
                <name>fs.defaultFS</name>
                <value>hdfs://ec2-54-175-61-81.compute-1.amazonaws.com:9000</value>
        </property>
		<property>
                <name>hadoop.tmp.dir</name>
                <value>/mnt/raid</value>
        </property> <!-- Comment this if not using the raid -->
</configuration>


2. vi slaves

// Add the localhost in the file, if not already added.

localhost


3. Vi hadoop-env.sh

// uncomment the java path and configure the java path.

# The java implementation to use.
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64

4. vi hdfs-site.xml

// Make the replication factor as 1, to get the most performance out of the system and also to reduce the data over the instances.

<configuration>
        <property>
                <name>dfs.replication</name>
                <value>1</value>
        </property>
        <property>
                <name>dfs.permissions</name>
                <value>false</value>
        </property>
</configuration>

5. vi mapred-site.xml

// Set the mappers and reducers to 4 as c3.large has 2 virtual cores.

<configuration>
<property>
	<name>mapred.job.tracker</name>
	<value>hdfs://ec2-54-175-61-81.compute-1.amazonaws.com:8021</value>
</property>

<!-- Increase the per task JVM memory of mapper and reducer, if heap error comes in between -->
<property>
	<name>mapreduce.map.java.opts</name>
	<value>-Xmx2342m</value>
</property> 
<property>
	<name>mapreduce.reduce.java.opts</name>
	<value>-Xmx4684m</value>
</property>

<!-- Increase the memory of mapper and reducer, if memory not sufficient error comes in between -->
<property>
	<name>mapreduce.map.memory.mb</name>
	<value>2928</value>
</property>
<property>
	<name>mapreduce.reduce.memory.mb</name>
	<value>5856</value>
</property>

<!-- Set the maximum limit of mapper and reducer -->
<property>
    <name>mapred.tasktracker.map.tasks.maximum</name>
    <value>4</value>
</property>
<property>
    <name>mapred.tasktracker.reduce.tasks.maximum</name>
    <value>4</value>
</property>

<!-- Set the number of mapper and reducer to be used in the program-->
<property>
	<name>mapred.map.tasks</name>
	<value>4</value>
</property>
<property>
	<name>mapred.reduce.tasks</name>
	<value>4</value>
</property>
</configuration>

<!-- OPTIONAL 

// Used to turn off the safe mode of namenode.
./hdfs dfsadmin -safemode leave

// To format the namenode
sudo bin/hadoop namenode -format

-->

// To delete temp files
rm -Rf /tmp/hadoop-ubuntu/

// To grant permissions
sudo chmod 777 /mnt/raid/

// To format the namenode
cd ../bin/
sudo ./hdfs namenode -format

// start the dfs
cd ../sbin/
~/hadoop-2.7.2/sbin$ ./start-dfs.sh
jps

// Make a director on HDFS
cd ../bin/
~/hadoop-2.7.2/bin$ ./hdfs dfs -mkdir -p /user/pshetty4/inputfolder


// Creating RAID on disk

sudo apt-get install mdadm

sudo umount -l /mnt

sudo mdadm --create --force --verbose /dev/md0 --level=0 --name=MY_RAID --raid-devices=3 /dev/xvdb /dev/xvdc /dev/xvdd

// Optional if mdadm says Device busy
sudo mdadm --stop /dev/md0

sudo mkfs.ext4 -L MY_RAID /dev/md0

sudo mkdir -p /mnt/raid

sudo mount LABEL=MY_RAID /mnt/raid

// Now check the mounting status using lsblk command.

lsblk

// Create 10GBUnsorted data
sudo ~/64/./gensort -a 100000000 /mnt/raid/10GBUnsorted
			OR
		// Without raid
sudo ~/64/./gensort -a 100000000 ~/10GBUnsorted 

// PUT THE 10GBUnsorted on HDFS
~/hadoop-2.7.2/bin$ ./hadoop fs -put /mnt/raid/10GBUnsorted /user/pshetty4/inputfolder/
			OR
		// Without raid
~/hadoop-2.7.2/bin$ ./hadoop fs -put ~/10GBUnsorted /user/pshetty4/inputfolder/

//Remove the 10GB from raid locally
~/hadoop-2.7.2/bin$ rm -r /mnt/raid/10GBUnsorted
			OR
		// Without raid
~/hadoop-2.7.2/bin$ rm -r ~/10GBUnsorted			
			
// Run the job on HDFS 
~/hadoop-2.7.2/bin$ ./hadoop jar HadoopSortSingleNode.jar HadoopSortSingleNode /user/pshetty4/inputfolder 10GBSorted	


// Check the file existence on HDFS
./hadoop fs -ls

// Get the sorted file to raid locally
./hadoop fs -get 10GBSorted /mnt/raid/
			OR
		// Without raid
./hadoop fs -get 10GBSorted ~/


// Convert it to dos to run valsort
unix2dos part-r-00000

// run the valsort on file
~/64/./valsort part-r-00000

// Get the first ten records
head -10 part-r-00000

// Get the last ten records
tail -10 part-r-00000


