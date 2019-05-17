# Understanding Kafka Multi Broker Cluster setup

	- Let us see the steps involved in Kafka multi broker setup. We will be using simulated environment with 7 virtual machines.
		- Pre-requisites for multi broker setup
    		- Setup Kafka and Zookeeper binaries
    		- Configure Zookeeper
    		- Zookeeper Commands
    		- Configure Kafka Brokers
    		- Validate Kafka

* Pre-requisites

	- Kafka multi broker clusters might have tens of servers managed by zookeeper. Zookeeper will be setup on either 3 or 5 or 7 nodes.
    	- Typically gateways, zookeeper and Kafka brokers will be isolated
        	- Gateways – one or two gateways with management tools to take care of the cluster.
        	- Zookeeper – 3 nodes. This will make sure that Kafka brokers are up and running all the time
        	- Brokers – Many (depending up on the requirement)
    	- In our case we have 7 servers. One will be gateway and 3 will have zookeeper and rest will have kafka broker on each of them (which is not the case for production configuration)
    	- Provision servers
        	- kdbroker00.itversity.com – gateway
        	- kdbroker01.itversity.com – zookeeper
        	- kdbroker02.itversity.com – zookeeper
        	- kdbroker03.itversity.com – zookeeper
        	- kdbroker04.itversity.com – kafka broker
        	- kdbroker05.itversity.com – kafka broker
        	- kdbroker06.itversity.com – kafka broker
    	- Update /etc/hosts with IP address and aliases.
    	- Make sure all the nodes have private IP address assigned and aliases allocated via DNS. As we do not have centralized DNS server we are giving aliases using /etc/hosts on all the virtual machines.
    	- Make sure to have a functional user on all the nodes (kafka in our case). It is already created while provisioning virtual machines.
    	- Make sure to mount storage to store files related to kafka topics and zookeeper managed services (/data1 and /data2 are base directories)
    	- Make sure owner of /data1 and /data2 is kafka.

127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4

::1         localhost localhost.localdomain localhost6 localhost6.localdomain6

192.168.100.210   kdbroker00.itversity.com

192.168.100.211   kdbroker01.itversity.com

192.168.100.212   kdbroker02.itversity.com

192.168.100.213   kdbroker03.itversity.com

192.168.100.214   kdbroker04.itversity.com

192.168.100.215   kdbroker05.itversity.com

192.168.100.216 kdbroker06.itversity.com

# copy public key on from control node to all nodes in kafka cluster
sshpass -f <(printf '%s\n' itversity) \
  ssh-copy-id -o StrictHostKeyChecking=no kafka@kdbroker06.itversity.com
sshpass -f <(printf '%s\n' itversity) \
  ssh-copy-id -o StrictHostKeyChecking=no kafka@kdbroker05.itversity.com
sshpass -f <(printf '%s\n' itversity) \
  ssh-copy-id -o StrictHostKeyChecking=no kafka@kdbroker04.itversity.com
sshpass -f <(printf '%s\n' itversity) \
  ssh-copy-id -o StrictHostKeyChecking=no kafka@kdbroker03.itversity.com
sshpass -f <(printf '%s\n' itversity) \
  ssh-copy-id -o StrictHostKeyChecking=no kafka@kdbroker02.itversity.com
sshpass -f <(printf '%s\n' itversity) \
  ssh-copy-id -o StrictHostKeyChecking=no kafka@kdbroker01.itversity.com
sshpass -f <(printf '%s\n' itversity) \
ssh-copy-id -o StrictHostKeyChecking=no kafka@kdbroker00.itversity.com

echo -e "o\nn\np\n1\n\n\nw" | sudo fdisk /dev/vdb

echo -e "o\nn\np\n1\n\n\nw" | sudo fdisk /dev/vdc

sudo mkfs.ext4 /dev/vdb; sudo mkfs.ext4 /dev/vdc

sudo mkdir /data1; sudo mkdir /data2

sudo sh -c "echo -e \"/dev/vdb   /data1    ext4    defaults        0 0\" >> /etc/fstab"

sudo sh -c "echo -e \"/dev/vdc   /data2    ext4    defaults        0 0\" >> /etc/fstab"

sudo mount -a

# Make sure contents of format_disk.sh is copied into a file called format_disk.sh
# Run these commands via ssh
# Do not run these commands on same node more than once
ssh kafka@kdbroker00.itversity.com 'bash -s' < format_disk.sh 

ssh kafka@kdbroker01.itversity.com 'bash -s' < format_disk.sh 

ssh kafka@kdbroker02.itversity.com 'bash -s' < format_disk.sh 

ssh kafka@kdbroker03.itversity.com 'bash -s' < format_disk.sh 

ssh kafka@kdbroker04.itversity.com 'bash -s' < format_disk.sh 

ssh kafka@kdbroker05.itversity.com 'bash -s' < format_disk.sh 

ssh kafka@kdbroker06.itversity.com 'bash -s' < format_disk.sh 

ssh kafka@kdbroker00.itversity.com "sudo chown kafka:kafka /data1 /data2" 

ssh kafka@kdbroker01.itversity.com "sudo chown kafka:kafka /data1 /data2"

ssh kafka@kdbroker02.itversity.com "sudo chown kafka:kafka /data1 /data2"

ssh kafka@kdbroker03.itversity.com "sudo chown kafka:kafka /data1 /data2" 

ssh kafka@kdbroker04.itversity.com "sudo chown kafka:kafka /data1 /data2"

ssh kafka@kdbroker05.itversity.com "sudo chown kafka:kafka /data1 /data2" 

ssh kafka@kdbroker06.itversity.com "sudo chown kafka:kafka /data1 /data2"

* Setup Kafka and Zookeeper binaries

	- Now let us see how we can setup binaries so that we can have Kafka multinode cluster along with zookeeper.

    	- Download kafka on all servers
    	- Untar kafka on all servers
    	- Move untarred kafka folder to /opt on all servers
    	- Create soft link by name kafka. This comes handy to maintain different versions of Kafka
    	- Even though we download Kafka on all nodes, we will not configure any service on kdbroker00.itversity.com. It will act as gateway to the main cluster. We will create topics, list topics, delete topics, validate topics from this server.
    	- In typical production environment non designated support staff are allowed to login to those nodes where services are configured to run. They will be given access to gateway node.
    	- Kafka binaries have zookeeper as well. We can leverage that and set up zookeeper as Kafka require zookeeper to manage the Kafka brokers. We will explore concepts behind zookeeper later.

	ssh kafka@kdbroker00.itversity.com "wget http://redrockdigimark.com/apachemirror/kafka/1.1.0/kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker01.itversity.com "wget http://redrockdigimark.com/apachemirror/kafka/1.1.0/kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker02.itversity.com "wget http://redrockdigimark.com/apachemirror/kafka/1.1.0/kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker03.itversity.com "wget http://redrockdigimark.com/apachemirror/kafka/1.1.0/kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker04.itversity.com "wget http://redrockdigimark.com/apachemirror/kafka/1.1.0/kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker05.itversity.com "wget http://redrockdigimark.com/apachemirror/kafka/1.1.0/kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker06.itversity.com "wget http://redrockdigimark.com/apachemirror/kafka/1.1.0/kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker00.itversity.com "tar xzf kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker01.itversity.com "tar xzf kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker02.itversity.com "tar xzf kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker03.itversity.com "tar xzf kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker04.itversity.com "tar xzf kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker05.itversity.com "tar xzf kafka_2.11-1.1.0.tgz"

	ssh kafka@kdbroker06.itversity.com "tar xzf kafka_2.11-1.1.0.tgz"


	ssh -tt kafka@kdbroker00.itversity.com "sudo mv -f kafka_2.11-1.1.0 /opt/."

	ssh -tt kafka@kdbroker01.itversity.com "sudo mv -f kafka_2.11-1.1.0 /opt/."

	ssh -tt kafka@kdbroker02.itversity.com "sudo mv -f kafka_2.11-1.1.0 /opt/."

	ssh -tt kafka@kdbroker03.itversity.com "sudo mv -f kafka_2.11-1.1.0 /opt/."

	ssh -tt kafka@kdbroker04.itversity.com "sudo mv -f kafka_2.11-1.1.0 /opt/."

	ssh -tt kafka@kdbroker05.itversity.com "sudo mv -f kafka_2.11-1.1.0 /opt/."

	ssh -tt kafka@kdbroker06.itversity.com "sudo mv -f kafka_2.11-1.1.0 /opt/."


	ssh -tt kafka@kdbroker00.itversity.com "sudo ln -s /opt/kafka_2.11-1.1.0 /opt/kafka"

	ssh -tt kafka@kdbroker01.itversity.com "sudo ln -s /opt/kafka_2.11-1.1.0 /opt/kafka"

	ssh -tt kafka@kdbroker02.itversity.com "sudo ln -s /opt/kafka_2.11-1.1.0 /opt/kafka"

	ssh -tt kafka@kdbroker03.itversity.com "sudo ln -s /opt/kafka_2.11-1.1.0 /opt/kafka"

	ssh -tt kafka@kdbroker04.itversity.com "sudo ln -s /opt/kafka_2.11-1.1.0 /opt/kafka"

	ssh -tt kafka@kdbroker05.itversity.com "sudo ln -s /opt/kafka_2.11-1.1.0 /opt/kafka"

	ssh -tt kafka@kdbroker06.itversity.com "sudo ln -s /opt/kafka_2.11-1.1.0 /opt/kafka"


* Configure Zookeeper

	- Zookeeper provides the capabilities of load balancing, fault tolerance and failover of kafka brokers, topics and partitions. 
	- It is mandatory to have Zookeeper to run Kafka brokers. 
	- If you have Zookeeper cluster already, you can skip these steps and configure Kafka brokers to plug into existing Zookeeper cluster.


    	- Define myid file under zookeeper dataDir (/data1/zookeeper)
    	- Zookeeper is typically set up on multiple nodes and it manages it self. myid file come handy in leader election for Zookeeper itself.

	- ssh kafka@kdbroker01.itversity.com "mkdir -p /data1/zookeeper"
	- ssh kafka@kdbroker02.itversity.com "mkdir -p /data1/zookeeper"
	- ssh kafka@kdbroker03.itversity.com "mkdir -p /data1/zookeeper"

	- Update zookeeper.properties with relevant values


#Licensed to the Apache Software Foundation (ASF) under one or more
#contributor license agreements.  See the NOTICE file distributed with
#this work for additional information regarding copyright ownership.
#The ASF licenses this file to You under the Apache License, Version 2.0
#(the "License"); you may not use this file except in compliance with
#the License.  You may obtain a copy of the License at
# 
#http://www.apache.org/licenses/LICENSE-2.0
# 
#Unless required by applicable law or agreed to in writing, software
#distributed under the License is distributed on an "AS IS" BASIS,
#WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#See the License for the specific language governing permissions and
#limitations under the License.
#the directory where the snapshot is stored.

dataDir=/data1/zookeeper

#the port at which the clients will connect

clientPort=2181

#disable the per-ip limit on the number of connections since this is a non-production config

maxClientCnxns=0

tickTime=2000

initLimit=5

syncLimit=2

server.1=kdbroker01.itversity.com:2888:3888

server.2=kdbroker02.itversity.com:2888:3888

server.3=kdbroker03.itversity.com:2888:3888


	- Start zookeeper on designated nodes	

	- ssh kafka@kdbroker01.itversity.com "/opt/kafka/bin/zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties"
	- ssh kafka@kdbroker02.itversity.com "/opt/kafka/bin/zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties"
	- ssh kafka@kdbroker03.itversity.com "/opt/kafka/bin/zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties"

* Zookeeper Commands

	- Zookeeper have bunch of commands to get the information about the services managed by Zookeeper as well as Zookeeper itself.

	- zkCli.sh or zookeeper-shell.sh – It is primarily to get details about services
    		- Here are some sample commands. Be careful while running rmr or any del commands.

			- /opt/kafka/bin/zookeeper-shell.sh kdbroker01.itversity.com:2181,kdbroker02.itversity.com:2181,kdbroker03.itversity.com:2181 help
  
			- /opt/kafka/bin/zookeeper-shell.sh kdbroker01.itversity.com:2181,kdbroker02.itversity.com:2181,kdbroker03.itversity.com:2181 ls /
  
			- /opt/kafka/bin/zookeeper-shell.sh kdbroker01.itversity.com:2181,kdbroker02.itversity.com:2181,kdbroker03.itversity.com:2181 get /zookeeper

	- Four letter commands – to get details about Zookeeper cluster: https://zookeeper.apache.org/doc/r3.4.8/zookeeperAdmin.html#sc_zkCommands

    		- Sample Command – echo conf | nc kdbroker01.itversity.com 2181

	- We will also see how to troubleshoot Zookeeper related issues by going through logs. Default location for Zookeeper logs as part of Kafka is /opt/kafka/logs/zookeeper.out

* Configure Kafka Brokers

	- Now let us see how we can configure Kafka brokers on the 3 designated nodes and start them.


    	- Assign broker.id – it has to be unique on each of the nodes. If you remove this entry or set to -1, unique id will be assigned when brokers are started.
    	- Configure listeners – using default port number. You can change if you want
    	- Configure log.dirs – log.dirs=/data1/kafka-logs,/data2/kafka-logs
    	- Configure zookeeper.connect using zookeeper host and port combination

broker.id=1

listeners=PLAINTEXT://:9092

log.dirs=/data1/kafka-logs,/data2/kafka-logs

zookeeper.connect=kdbroker01.itversity.com:2181,kdbroker02.itversity.com:2181,kdbroker03.itversity.com:2181


broker.id=2

listeners=PLAINTEXT://:9092

log.dirs=/data1/kafka-logs,/data2/kafka-logs

zookeeper.connect=kdbroker01.itversity.com:2181,kdbroker02.itversity.com:2181,kdbroker03.itversity.com:2181


broker.id=3

listeners=PLAINTEXT://:9092

log.dirs=/data1/kafka-logs,/data2/kafka-logs

zookeeper.connect=kdbroker01.itversity.com:2181,kdbroker02.itversity.com:2181,kdbroker03.itversity.com:2181


	- Start all the brokers

		- ssh kafka@kdbroker04.itversity.com "/opt/kafka/bin/kafka-server-start.sh -daemon /opt/kafka/config/server.properties"
		- ssh kafka@kdbroker05.itversity.com "/opt/kafka/bin/kafka-server-start.sh -daemon /opt/kafka/config/server.properties"
		- ssh kafka@kdbroker06.itversity.com "/opt/kafka/bin/kafka-server-start.sh -daemon /opt/kafka/config/server.properties"

* Validate Kafka

	- As Kafka is successfully setup let us go ahead and validate by creating topic and then by publishing and consuming messages using the topic.

	- Validate from host kdbroker00.itversity.com

    		- Download Kafka
    		- Untar and copy it to /opt
    		- Create soft link
    		- Run these commands and validate by passing some test messages to Kafka topic


		/opt/kafka/bin/kafka-topics.sh \
  			--create \
  			--zookeeper kdbroker01.itversity.com:2181,kdbroker02.itversity.com:2181,kdbroker03.itversity.com:2181 \
  			--replication-factor 1 \
  			--partitions 1 \
  			--topic test

		/opt/kafka/bin/kafka-console-producer.sh \
  			--broker-list kdbroker04.itversity.com:9092,kdbroker05.itversity.com:9092 \
  			--topic test

		/opt/kafka/bin/kafka-console-consumer.sh \
  			--bootstrap-server kdbroker04.itversity.com:9092,kdbroker05.itversity.com:9092 \
  			--topic test \
			--from-beginning

		- Setup gen_logs on kdbroker00.itversity.com

