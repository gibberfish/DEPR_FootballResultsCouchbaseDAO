package uk.co.mindbadger.footballresultsanalyser.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivision;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionTeam;
import uk.co.mindbadger.footballresultsanalyser.domain.Team;

public class FootballResultsAnalyserCouchbaseDAOTest {

	private FootballResultsAnalyserCouchbaseDAO dao;
	private DomainObjectFactory domainObjectFactory;
	
	private static final String TEST_BUCKET_NAME = "footballTest";
	
	@Before
	public void setup () {
		flushBucket();
		
		dao = new FootballResultsAnalyserCouchbaseDAO ();
		domainObjectFactory = new DomainObjectFactoryImpl();
		
		dao.setDomainObjectFactory(domainObjectFactory);
		dao.setBucketName("footballTest");
		
		dao.startSession();
	}

	@After
	public void teardown () {
		dao.closeSession();
	}

	@BeforeClass
	public static void ensureWeAreCleanBeforeWeStart () {
		//flushBucket();
	}

	@AfterClass
	public static void cleanUpAtEnd () {
		//flushBucket();
	}

	private static void flushBucket () {
		System.out.println("Flushing Bucket");
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(TEST_BUCKET_NAME);
		bucket.bucketManager().flush();
		cluster.disconnect();		
	}
	
	@Test
	public void testSeasons () {
		Season season = dao.addSeason(2001);
	}
	
	@Test
	public void testDivisions () {
		Division division = dao.addDivision("Premier");
	}

	@Test
	public void testTeams () {
		Team team = dao.addTeam("Portsmouth");
	}

	@Test
	public void testSeasonDivisions () {
		Season season = dao.addSeason(2001);
		Division division = dao.addDivision("Premier");
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 2);
	}
	
	@Test
	public void testDivisionTeams () {
		Season season = dao.addSeason(2001);
		Division division = dao.addDivision("Premier");
		Team team = dao.addTeam("Portsmouth");
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 2);
		
		SeasonDivisionTeam seasonDivisionTeam = dao.addSeasonDivisionTeam(seasonDivision, team);
	}
	
}
