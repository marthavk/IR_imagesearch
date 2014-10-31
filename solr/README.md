irproject
=========

1. Download Apache Solr version 4.7.2 from http://lucene.apache.org/solr/.
2. Copy the core "images", inside the core directory, into *your_solr_dir*/example/solr/
3. Start your absolute favourite terminal application, move into the Solr example directory and run "java -jar start.jar"
4. Open Google Chrome and navigate to http://localhost:8983/solr/#/~cores/
5. Click "Add Core" and enter: name -> images, instanceDir -> images, dataDir -> data
6. Submit the form
7. Open the Java project, make sure there is nothing crazy going on within the SolrConnector (URL to Solr etc.)
8. You're done!
