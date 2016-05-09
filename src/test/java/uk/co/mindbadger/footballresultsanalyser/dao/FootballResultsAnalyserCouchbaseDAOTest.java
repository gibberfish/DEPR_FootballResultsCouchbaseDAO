package uk.co.mindbadger.footballresultsanalyser.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

	private static final Integer SEASON_1 = 2001;
	private static final Integer SEASON_2 = 2002;
	private FootballResultsAnalyserCouchbaseDAO dao;
	private DomainObjectFactory domainObjectFactory;
	
	private static final String TEST_BUCKET_NAME = "footballTest";
	
	@BeforeClass
	public static void ensureWeAreCleanBeforeWeStart () {
		flushBucket();
	}
	
	@AfterClass
	public static void cleanUpAtEnd () {
		//flushBucket();
	}
	
	@Before
	public void setup () {
		//flushBucket();
		
		dao = new FootballResultsAnalyserCouchbaseDAO ();
		domainObjectFactory = new DomainObjectFactoryImpl();
		
		dao.setDomainObjectFactory(domainObjectFactory);
		dao.setBucketName("footballTest");
		
		dao.startSession();
	}

	@After
	public void teardown () {
		dao.closeSession();

		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(TEST_BUCKET_NAME);
		
		if (bucket.get("ssn_"+SEASON_1) != null) bucket.remove("ssn_"+SEASON_1);
		if (bucket.get("ssn_"+SEASON_2) != null) bucket.remove("ssn_"+SEASON_2);
		
		cluster.disconnect();		
	}
	
	@Test
	public void shouldBeNoSeasonsToStartWith () {
		// When
		List<Season> seasons = dao.getSeasons();
		
		// Then
		assertEquals (0, seasons.size());		
	}

	@Test
	public void shouldAddASeason() {
		// When
		Season season = dao.addSeason(SEASON_1);
		List<Season> seasons = dao.getSeasons();
		
		// Then (check return value)
		assertEquals (SEASON_1, season.getSeasonNumber());
		
		// Then (get it from the list)
		assertEquals (1, seasons.size());
		assertEquals (SEASON_1, seasons.get(0).getSeasonNumber());
		
		// Then (get it by key)
		assertEquals (SEASON_1, dao.getSeason(SEASON_1).getSeasonNumber());
	}

	@Test
	public void shouldAddTwoSeasons () {
		// When
		Season season1 = dao.addSeason(SEASON_1);
		Season season2 = dao.addSeason(SEASON_2);
		
		// The (check return values)
		assertEquals (SEASON_1, season1.getSeasonNumber());
		assertEquals (SEASON_2, season2.getSeasonNumber());
		
		// Then (check them in the list)
		List<Season> seasons = dao.getSeasons();
		assertEquals (2, seasons.size());
		assertEquals (SEASON_1, seasons.get(0).getSeasonNumber());
		assertEquals (SEASON_2, seasons.get(1).getSeasonNumber());
				
		// Then (get them by keys)
		assertEquals (SEASON_1, dao.getSeason(SEASON_1).getSeasonNumber());
		assertEquals (SEASON_2, dao.getSeason(SEASON_2).getSeasonNumber());
	}

	@Test
	public void getShouldReturnNullIfNoSeasonExists () {
		// When
		Season season = dao.getSeason(SEASON_1); 
		
		// Then
		assertNull (season);		
	}

	@Ignore
	@Test
	public void testDivisions () {
		Division division = dao.addDivision("Premier");
	}

	@Ignore
	@Test
	public void testTeams () {
		Team team = dao.addTeam("Portsmouth");
	}

	@Ignore
	@Test
	public void testSeasonDivisions () {
		Season season = dao.addSeason(2001);
		Division division = dao.addDivision("Premier");
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 2);
	}
	
	@Ignore
	@Test
	public void testDivisionTeams () {
		Season season = dao.addSeason(2001);
		Division division = dao.addDivision("Premier");
		Team team = dao.addTeam("Portsmouth");
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 2);
		
		SeasonDivisionTeam seasonDivisionTeam = dao.addSeasonDivisionTeam(seasonDivision, team);
	}
	
	private static void flushBucket () {
		System.out.println("Flushing Bucket");
		Cluster cluster = CouchbaseCluster.create();
		Bucket bucket = cluster.openBucket(TEST_BUCKET_NAME);
		bucket.bucketManager().flush();
		cluster.disconnect();		
	}
}
