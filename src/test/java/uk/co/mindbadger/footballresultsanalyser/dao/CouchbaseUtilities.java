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

	public void tearDownTeams(String[] teams, String bucketName) {
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(bucketName);
		
		for (String teamId : teams) {
			if (bucket.get("team_"+teamId) != null) bucket.remove("team_"+teamId);
		}
		cluster.disconnect();		
	}

	public void tearDownFixtures(String[] fixtures, String bucketName) {
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(bucketName);
		
		for (String fixtureId : fixtures) {
			if (bucket.get("fix_"+fixtureId) != null) bucket.remove("fix_"+fixtureId);
		}
		cluster.disconnect();		
	}
}
