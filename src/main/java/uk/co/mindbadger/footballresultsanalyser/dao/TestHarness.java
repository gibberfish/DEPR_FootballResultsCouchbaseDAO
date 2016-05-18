package uk.co.mindbadger.footballresultsanalyser.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.Stale;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.couchbase.client.protocol.views.ComplexKey;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Fixture;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivision;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionTeam;
import uk.co.mindbadger.footballresultsanalyser.domain.Team;

public class TestHarness {


	public static void main(String[] args) {
		DomainObjectFactory domainObjectFactory = new DomainObjectFactoryImpl();
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket("footballTest");
		
//		bucket.bucketManager().flush();
//		
//		FootballResultsAnalyserCouchbaseDAO dao = new FootballResultsAnalyserCouchbaseDAO ();
//		dao.setDomainObjectFactory(domainObjectFactory);
//		dao.setBucketName("footballTest");
//		
//		Season season = dao.addSeason(2001);
//		Division division = dao.addDivision("Premier");
//		Team homeTeam = dao.addTeam("Portsmouth");
//		Team awayTeam = dao.addTeam("Arsenal");
//		Calendar fixtureDate = Calendar.getInstance();
//		
//		// When
//		dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, null, null);
//		Fixture fixture = dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, 5, 4);
//		List<Fixture> fixtures = dao.getFixtures();
//		
//		// Then
//		String fixtureId1 = fixture.getFixtureId();
//		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());
		
		String uniqueKey = "[2001,\"0\",\"5\",\"1\"]";
		
		System.out.println("Looking for matching fixture with key: " + uniqueKey);
		
		ViewQuery query = ViewQuery.from("fixture", "unique");
		query.stale(Stale.FALSE);
		JsonArray jsonArrayKey = JsonArray.empty();
		jsonArrayKey.add(2001);
		jsonArrayKey.add("0");
		jsonArrayKey.add("5");
		jsonArrayKey.add("1");
		//query.key(uniqueKey);
		query.key(jsonArrayKey);
		ViewResult result = bucket.query(query);
		
		
		for (ViewRow row : result.allRows()) {
			System.out.println("Got a matching fixture with key " + row.key());
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject jsonFixture = doc.content();
			
			
			Integer seasonNumber = jsonFixture.getInt("seasonNumber");
			String divisionId = jsonFixture.getString("divisionId");
			String homeTeamId = jsonFixture.getString("homeTeamId");
			String awayTeamId = jsonFixture.getString("awayTeamId");
			Integer fixtureId = jsonFixture.getInt("fixtureId");
			String fixtureDateString = jsonFixture.getString("fixtureDate");
			Integer homeGoals = jsonFixture.getInt("homeGoals");
			Integer awayGoals = jsonFixture.getInt("awayGoals");
			
			System.out.println("...ssn:"+seasonNumber+",div:"+divisionId+",hmtm:"+homeTeamId+",awtm:"+awayTeamId+",dt:"+fixtureDateString+",hmgl:"+homeGoals+",awgl:"+awayGoals+",id:"+fixtureId);
		}
		
		cluster.disconnect();
	}
}
