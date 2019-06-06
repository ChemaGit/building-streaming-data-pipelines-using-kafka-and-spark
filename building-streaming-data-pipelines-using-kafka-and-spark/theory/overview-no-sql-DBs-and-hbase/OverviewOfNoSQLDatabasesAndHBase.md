# Overview of NoSQL Databases and HBase

	- While we can use any type of database to persist the output of streaming pipelines, NoSQL is the most popular choice. Here are the list of market leading NoSQL databases.

    		- Overview of NoSQL Databases
        		- HBase – comes as part of Hadoop eco system with most of the distributions such as Cloudera, Hortonworks, MapR etc.
        		- Cassandra – most popular NoSQL database which can work with Big Data technologies such as Hadoop, Spark etc.
        		- MongoDB – simple, easy to use NoSQL database with rich querying capabilities and works seamlessly with Big Data technologies such as Hadoop, Spark etc.
        		- DynamoDB – popular NoSQL database with in amazon eco system
        		- MapR DB – MapR version of HBase.
    		- Understanding HBase concepts
        		- CRUD Operations
        		- Schema in HBase
        		- Understanding HBase Shell


# NoSQL Concepts/Features

	- All NoSQL databases comes with these capabilities
    		- Key and Value – each row will have a key and value. Value typically contain attributes and respective values. It is very close to XML or JSON.
    		- Indexed – Data will be typically sorted and indexed based on row key.
    		- Partitioned – Data will be partitioned by row key column. It also known as sharded/sharding.
    		- Replication – There will be multiple copies of data
    		- Commit log – For restore and recovery of the data
    		- CAP algorithm – Consistency, Accessibility and Partition Tolerance
    		- Minor and major compaction – periodic merging of files with in each partition so that we will not end up having too many small files
    		- Tombstones – soft delete of data
    		- Vacuum Cleaning – hard delete of data
    		- Consistency Level – Commit point. 
			- As we have multiple copies of data consistency level determines whether data is considered to be committed when one copy is updated or all the copies are updated. 
			- It is determined depending up on the criticality of data and desired performance for the application.

		- While indexing and partitioning serves the purpose of scalability in terms of performance, replication serves the purpose of reliability of database. 
			- Even though we do not cover these terms extensively, it is good to understand all these terms in detail.

$ hbase shell
hbase> list
hbase> scan 'nyse:stock_data', {LIMIT => 10}
hbase> scan 'training:demo'
hbase> put 'training:demo', 4, 'cf1:column1', 'val1'
hbase> delete 'training:demo', 4, 'cf1:column1', 'val1'


# CRUD Operations

	- Let us explore details about CRUD.
	- As part of this course we will be covering CRUD operations on HBase or MapR-DB database.

	- CRUD stands for
    		- Create (insert)
    		- Read
    		- Update
    		- Delete

	- Here are some of the points to remember with respect to CRUD operations
    		- All the databases support all operations. But when it come to DML (CUD), performance varies depending up on the consistency level.
    		- All databases support basic operations
        		- Selecting all the data
        		- Selecting range of columns
        		- Retrieve row value by passing key
        		- Apply filter on row values
    		- But databases such as MongoDB also have rich aggregation framework

# Schema in HBase/MapR-DB

	- Let us go through quick overview of HBase/MapR-DB schema.
    	- A table contains column family (group of columns)
    	- We do not specify columns while creating the table
    	- Data is inserted/updated using put
    	- Data can be read using scan or get. We need to pass row key to get.
    	- Data can be deleted using delete
    	- With each put we only insert/update one row with column within a column family
    	- Combination of row key and one column name and value are also known as cell
    	- Data is automatically sorted and partitioned on row key
    	- Within each row key all the cells (column name and value) are sorted based on the key of cell (column name)
    	- They support several filters (partial scan and filters on top of cells as part of get)

# Understanding hbase shell

	- Let us see understand more about hbase shell by going through some of the commands
	- On the gateway node of the hbase cluster run hbase shell
	- help command provides list of commands in different categories
	- Namespace – group of tables (similar to schema or database) 
		- create – create_namespace 'training'
    		- list – list_namespace 'training'
    		- list tables – list_namespace_tables 'training'

	- Table – group of rows which have keys and values 
    		- While creating the table we need to specify table name and at least one column family
    		- Column family will have cells. A cell is nothing but, a name and value pair
    		- e.g.: create 'training:hbasedemo', 'cf1'
    		- list – list 'training:.*'
    		- describe – describe 'training:hbasedemo'
    		- truncate – truncate 'training:hbasedemo'
    		- Dropping is 2 step process – disable and drop
    		-	disable – disable 'training:hbasedemo'
    		- drop – drop 'training:hbasedemo'
	- Inserting/updating data
		- put 'training:hbasedemo', 1, 'cf1:column1', 'value1'
		- put 'training:hbasedemo', 1, 'cf1:column2', 'value2'
		- put 'training:hbasedemo', 4, 'cf1:column1', 'value1'
		- put 'training:hbasedemo', 4, 'cf1:column3', 'value3'


		- $ hbase shell
		- hbase> list
		- hbase> scan 'nyse:stock_data', {LIMIT => 10}
		- hbase> scan 'training:demo'
		- hbase> put 'training:demo', 4, 'cf1:column1', 'val1'
		- hbase> delete 'training:demo', 4, 'cf1:column1', 'val1'
		- hbase> create 'demo', 'cf1'
		- hbase> create 'demo', 'cf1'
		- hbase> list_namespace
		- hbase> create_namespace 'training'
		- hbase> create 'training:demo','cf1'
		- hbase> put 'training:demo', 1, 'cf1:c1', 'v1'
		- hbase> scan 'training:demo'
		- hbase> put 'training:demo', 1, 'cf1:c2', 'v2'
		- hbase> put 'training:demo', 1, 'cf1:c4', 'v3'
		- hbase> put 'training:demo', 1, 'cf1:c3', 'v3'
		- hbase> scan 'training:demo'
		- hbase> put 'training:demo', 3, 'cf1:x1', 'xv1'
		- hbase> put 'training:demo', 3, 'cf1:x2', 'xv2'
		- hbase> put 'training:demo', 2, 'cf1:y1', 'yv1'
		- hbase> scan 'training:demo'
		- update
		- hbase> put 'training:demo', 1, 'cf1:c1', 'cv1'
		- hbase> scan 'training:demo'
		- hbase> get 'training:demo',1
		- hbase> get 'training:demo',1, {COLUMN => 'cf1:c1', VERSIONS => 2}
		- hbase> describe 'training:demo'
		- hbase> scan 'training:demo',{VERSIONS => 3}
		- delete
		- hbase> delete 'training:demo',1,'cf1:c1'
		- hbase> scan 'training:demo'
		- hbase> create 'training:endorsements', 't'
		- hbase> put 'training:endorsements','email@email.com','t:oracle','x,y,z'
		- hbase> scan 'training:endorsements'
		- hbase> disable 'training:endorsements'
		- hbase> drop 'training:endorsements'
		- hbase> disable 'training:endorsements'
		- hbase> create 'training:endorsements', 'raw', 'agg'
		- hbase> put 'training:endorsements','email@email.com','raw:oracle','x,y,z'
		- hbase> put 'training:endorsements','email@email.com','agg:oracle', 3
		- hbase> put 'training:endorsements','email@email.com','raw:oracle:x', 1
		- hbase> put 'training:endorsements','email@email.com:oracle','raw:endorsers', 'x,y,z'
		- hbase> show_filters
		- hbase> scan 'training:endorsements', {LIMIT => 10}
		- hbase> get 'training:endorsements', 'email@email.com:oracle'
		- hbase> get 'training:endorsements', 'email@email.com:oracle:agg'
		- hbase> scan 'training:endorsements', {STARTROW => 'email@email.com', LIMIT => 10}
		- hbase> scan 'training:endorsements', {STARTROW => 'email@email.com:oracle', LIMIT => 10}
		- hbase> scan 'training:endorsements', {STARTROW => 'email@email.com',STOPROW => 'email@email.com:oracle' , LIMIT => 10}
		- hbase> scan 'training:endorsements', {STARTROW => 'email@email.com',STOPROW => 'email@email.com:oracle' ,COLUMNS => ['agg:oracle'], LIMIT => 10}
		- hbase> scan 'training:endorsements', {FILTER => "ColumnPrefixFilter('or')", LIMIT => 10}
		- hbase> get 'training:endorsements', {FILTER => "ColumnPrefixFilter('or')", LIMIT => 10}
