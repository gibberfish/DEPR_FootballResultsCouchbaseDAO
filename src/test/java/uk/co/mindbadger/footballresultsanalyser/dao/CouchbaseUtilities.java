package uk.co.mindbadger.footballresultsanalyser.dao;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;

public class CouchbaseUtilities {
	public void flushBucket (String bucketName) {
		System.out.println("Flushing Bucket");
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(bucketName);
		bucket.bucketManager().flush();
		cluster.disconnect();		
	}
	
	public void tearDownSeasons (Integer[] seasons, String bucketName) {
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(bucketName);
		
		for (Integer season : seasons) {
			if (bucket.get("ssn_"+season) != null) bucket.remove("ssn_"+season);
		}
		cluster.disconnect();		
	}

	public void tearDownDivisions(String[] divisions, String bucketName) {
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(bucketName);
		
		for (String divisionId : divisions) {
			if (bucket.get("div_"+divisionId) != null) bucket.remove("div_"+divisionId);
		}
		cluster.disconnect();		
	}

}
