package uk.co.mindbadger.footballresultsanalyser.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;

public class FootballResultsAnalyserCouchbaseDAOSeasonTest {

	private static final Integer SEASON_1 = 2001;
	private static final Integer SEASON_2 = 2002;
	private FootballResultsAnalyserCouchbaseDAO dao;
	private DomainObjectFactory domainObjectFactory;
	private static CouchbaseUtilities cbUtils = new CouchbaseUtilities ();
	
	private static final String TEST_BUCKET_NAME = "footballTest";
	
	@BeforeClass
	public static void ensureWeAreCleanBeforeWeStart () {
		cbUtils.flushBucket(TEST_BUCKET_NAME);
	}
	
	@Before
	public void setup () {
		dao = new FootballResultsAnalyserCouchbaseDAO ();
		domainObjectFactory = new DomainObjectFactoryImpl();
		
		dao.setDomainObjectFactory(domainObjectFactory);
		dao.setBucketName("footballTest");
		
		dao.startSession();
	}

	@After
	public void teardown () {
		dao.closeSession();
		cbUtils.tearDownSeasons(new Integer[] {SEASON_1, SEASON_2}, TEST_BUCKET_NAME);
	}
	
	@Test
	public void shouldBeNoSeasonsToStartWith () {
		// When
		List<Season> seasons = dao.getSeasons();
		
		// Then
		assertEquals (0, seasons.size());		
	}

	@Test
	public void shouldThrowAnExceptionWhenTryingToGetANonExistentSeason () {
		// Given

		try {
			// When
			dao.getSeason(1766);
			fail ("An exception should be thrown if a season does not exist");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Season 1766 does not exist", e.getMessage());
		}
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
	public void shouldUpdateAnExistingSeason() {
		// When
		dao.addSeason(SEASON_1);
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
}
