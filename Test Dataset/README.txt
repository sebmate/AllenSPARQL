Using the minimal test dataset with AllenSPARQL
===============================================

This document describes how to use AllenSPARQ with our test dataset. There are
three methods, depending on what software components you have available.


Method 1: With an i2b2 instance based on i2b2 Wizard (Oracle)
-------------------------------------------------------------

Our toolset is designed to work with i2b2 installations that have been created with
i2b2 Wizard (https://github.com/sebmate/i2b2Wizard). A possible difference to other
i2b2 installations is that all tables for an i2b2 are stored in one database schema
(and not three). If the latter is the case for your i2b2 installation, we suggest
to follow either method 2 or 3 below.

Data upload:

1. Create a new i2b2 project "Allen Test", and assign an i2b2 user to it.
2. On the database schema, run "Insert i2b2 test data.sql".
3. Open the i2b2 project in the i2b2 webclient and and query for "A" and "B".

Preparing D2RQ and Fuseki:

4. Modify the database connection in "mapping.ttl" and start D2RQ with this mapping
   file. It should point to your i2b2 project in your Oracle database. It might also
   be necessary to rename the schema names.
5. Start the Fuseki 2 server.
6. Access http://localhost:3030/manage.html and create a new RDF dataset. Call it 
   "i2b2" and use the persistent type.
7. Modify the database connection in "DBConnection.properties" of AllenSPARQL. It 
   should also point to your i2b2 project in the Oracle database.
8. Start AllenSPARQL. It should be able to connect your i2b2 project and display
   the i2b2 queries in the upper left corner. It should also report a triple count
   of zero.
9. On the right side, select "Complex aggregation", then click on "Run ETL". After
   running all tasks, it should report 1200 triples.

Continue with "Querying data" below.


Method 2: No i2b2 instance but Oracle database available
--------------------------------------------------------

Data upload:

1. Create a new Oracle database schema "I2B2_17_ALLENTEST".
2. On the database schema, run "i2b2 project Oracle schema dump.sql".
3. Continue the steps 4-9 from method 1 (see above).

Continue with "Querying data" below.
   

Method 3: No D2RQ, i2b2 and Oracle database
-------------------------------------------

For using this method, you do not need D2RQ, i2b2 or an Oracle database.

1. Start the Fuseki 2 server.
2. Access http://localhost:3030/manage.html and create a new RDF dataset. Call it 
   "i2b2" and use the persistent type.
3. Upload the file "i2b2 D2RQ dump.rdf" into this RDF dataset. It should report
   363 triples uploaded into the RDF store.
4. Start AllenSPARQL. It will show an error messages and tell you that it can't
   access the i2b2 databse. Ignore this, you should be able to continue. The tool
   should also report 636 triples.
5. On the right side, select "Complex aggregation". Since AllenSPARQL cannot access
   an i2b2 database to get the information about the the concepts from a query, we 
   have to enter this manually. On the right side, click on the "+" button twice.
   This will create two entries in the table. In the second row, rename the
   original name to "B" and the new name to "B Agg". The first line should contain
   the same, except with "A" instead of "B".
6. On the left side, make sure that the following ETL tasks are selected:

    [ ] Create view with the query's data for D2RQ   (or deavtivated)
	[ ] Upload these data to Fuseki via D2RQ         (or deavtivated)
	[X] Duplicate renamed intervals
	[X] Link near intervals
	[X] Aggregate near intervals into episodes
	[X] Link episodes to intervals, count subintervals, compute average values
	[X] Create Allen relations in source database
	[ ] Add simplified Allen relations to source data

7. Click on "Run ETL". The tool will now perform the aggregation and annotation with
   Allen's relations. The triple count after that should be 1200 triples.
   
Continue with "Querying data" below.


Querying data
-------------

To automatically run queries in AllenSPARQL, select "Automatically run SPARQL queries" 
on the left bottom.

To query the data, start AllenGUI from within AllenSPARQL's root directory. Create two new intervals, or rename the existing ones to "A Agg" and "B Agg". Using AllenGUI is simple:
   
   - To create a new intervall or connector: Left-click and drag the left mouse button
   - To delete an interval: Right-click on the interval
   - To rename an interval: Middle-click on the interval

Feel free to model all 13 Allen relations. AllenGUI should report a distinct patient
number for each Allen relation (1-13).

Also feel free to experiment with the variable temporal aggregation. Unfortunately,
when you started with the RDF dump, you will have to delete the i2b2 dataset in
Fuseki first and upload it again afterwards.


