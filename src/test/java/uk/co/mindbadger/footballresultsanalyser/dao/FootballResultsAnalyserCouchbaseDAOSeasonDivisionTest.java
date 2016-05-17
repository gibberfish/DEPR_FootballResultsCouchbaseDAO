package uk.co.mindbadger.footballresultsanalyser.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivision;

public class FootballResultsAnalyserCouchbaseDAOSeasonDivisionTest {

	private static CouchbaseUtilities cbUtils = new CouchbaseUtilities ();

	private static final Integer SEASON_1 = 2001;
	private static final Integer SEASON_2 = 2002;
	private static final String DIV_NAME_1 = "Premier";
	private static final String DIV_NAME_2 = "Championship";

	private FootballResultsAnalyserCouchbaseDAO dao;
	private String divisionId1;
	private String divisionId2;

	private DomainObjectFactory domainObjectFactory;
	
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
		cbUtils.tearDownDivisions(new String[] {divisionId1, divisionId2}, TEST_BUCKET_NAME);
	}
	
	@Test
	public void shouldBeNoDivisionsForANewSeason () {
		// When
		Season season = dao.addSeason(SEASON_1);
		List<SeasonDivision> seasonDivisions = dao.getDivisionsForSeason(season);
		
		// Then
		assertEquals (0, seasonDivisions.size());		
	}

	@Test
	public void shouldAddADivisionToASeason() {
		// When
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 3);
		List<SeasonDivision> seasonDivisions = dao.getDivisionsForSeason(season);
		
		// Then (check return value)
		assertEquals (SEASON_1, seasonDivision.getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), seasonDivision.getDivision().getDivisionId());
		assertEquals (3, seasonDivision.getDivisionPosition());
		
		// Then (get it from the list)
		assertEquals (1, seasonDivisions.size());
		SeasonDivision returnedSeasonDivision = (SeasonDivision)(seasonDivisions.toArray())[0];
		assertEquals (SEASON_1, returnedSeasonDivision.getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), returnedSeasonDivision.getDivision().getDivisionId());
		assertEquals (3, returnedSeasonDivision.getDivisionPosition());
	}

	@Test
	public void shouldUpdateAnExistingDivisionInASeason() {
		// When
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		
		dao.addSeasonDivision(season, division, 1);
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 3);
		List<SeasonDivision> seasonDivisions = dao.getDivisionsForSeason(season);
		
		// Then (check return value)
		assertEquals (SEASON_1, seasonDivision.getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), seasonDivision.getDivision().getDivisionId());
		assertEquals (3, seasonDivision.getDivisionPosition());
		
		// Then (get it from the list)
		assertEquals (1, seasonDivisions.size());
		SeasonDivision returnedSeasonDivision = (SeasonDivision)(seasonDivisions.toArray())[0];
		assertEquals (SEASON_1, returnedSeasonDivision.getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), returnedSeasonDivision.getDivision().getDivisionId());
		assertEquals (3, returnedSeasonDivision.getDivisionPosition());
	}

	@Test
	public void shouldAddTwoDivisionsToASeason () {
		// When
		Season season = dao.addSeason(SEASON_1);
		Division division1 = dao.addDivision(DIV_NAME_1);
		Division division2 = dao.addDivision(DIV_NAME_2);
		
		SeasonDivision seasonDivision1 = dao.addSeasonDivision(season, division1, 3);
		SeasonDivision seasonDivision2 = dao.addSeasonDivision(season, division2, 2);
		List<SeasonDivision> seasonDivisions = dao.getDivisionsForSeason(season);
		
		// Then (check return value)
		assertEquals (SEASON_1, seasonDivision1.getSeason().getSeasonNumber());
		assertEquals (SEASON_1, seasonDivision2.getSeason().getSeasonNumber());
		assertEquals (division1.getDivisionId(), seasonDivision1.getDivision().getDivisionId());
		assertEquals (division2.getDivisionId(), seasonDivision2.getDivision().getDivisionId());
		assertEquals (3, seasonDivision1.getDivisionPosition());
		assertEquals (2, seasonDivision2.getDivisionPosition());
		
		// Then (get it from the list)
		assertEquals (2, seasonDivisions.size());
		SeasonDivision returnedSeasonDivision1 = (SeasonDivision)(seasonDivisions.toArray())[0];
		assertEquals (SEASON_1, returnedSeasonDivision1.getSeason().getSeasonNumber());
		assertEquals (division1.getDivisionId(), returnedSeasonDivision1.getDivision().getDivisionId());
		assertEquals (3, returnedSeasonDivision1.getDivisionPosition());
		
		SeasonDivision returnedSeasonDivision2 = (SeasonDivision)(seasonDivisions.toArray())[1];
		assertEquals (SEASON_1, returnedSeasonDivision2.getSeason().getSeasonNumber());
		assertEquals (division2.getDivisionId(), returnedSeasonDivision2.getDivision().getDivisionId());
		assertEquals (2, returnedSeasonDivision2.getDivisionPosition());
	}
	
	//TODO Need to add some tests for cases where we pass in incomplete objects to the creates
}
