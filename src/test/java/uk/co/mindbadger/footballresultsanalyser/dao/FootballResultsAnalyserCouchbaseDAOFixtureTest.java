package uk.co.mindbadger.footballresultsanalyser.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Fixture;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;
import uk.co.mindbadger.footballresultsanalyser.domain.Team;

public class FootballResultsAnalyserCouchbaseDAOFixtureTest {

	private static CouchbaseUtilities cbUtils = new CouchbaseUtilities ();

	private static final Integer SEASON_1 = 2001;
	private static final Integer SEASON_2 = 2002;
	private static final String DIV_NAME_1 = "Premier";
	private static final String TEAM_NAME_1 = "Porstmouth";
	private static final String TEAM_NAME_2 = "Arsenal";
	private static final String TEAM_NAME_3 = "Walsall";
	private static final String TEAM_NAME_4 = "Bristol City";

	private FootballResultsAnalyserCouchbaseDAO dao;
	
	private String divisionId1;
	private String divisionId2;
	private String teamId1;
	private String teamId2;
	private String teamId3;
	private String teamId4;
	private String fixtureId1;
	private String fixtureId2;
	private String fixtureId3;
	private String fixtureId4;
	private String fixtureId5;
	private String fixtureId6;
	private String fixtureId7;

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
		cbUtils.tearDownTeams(new String[] {teamId1, teamId2, teamId3, teamId4}, TEST_BUCKET_NAME);
		cbUtils.tearDownFixtures(new String[] {fixtureId1, fixtureId2, fixtureId3, fixtureId4, fixtureId5, fixtureId6, fixtureId7}, TEST_BUCKET_NAME);
	}
	
	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutASeason () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(null, fixtureDate, division, homeTeam, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a season");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a season when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutADivision () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, fixtureDate, null, homeTeam, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a division");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a division when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutAHomeTeam () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, fixtureDate, division, null, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a home team");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a home team when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutAnAwayTeam () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, fixtureDate, division, homeTeam, null, 1, 2);
			fail ("An exception should be thrown when we don't supply a away team");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply an away team when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAPlayedFixtureWithoutAFixtureDate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, null, division, homeTeam, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a date for a played fixture");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a fixture date team when creating a played fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldAddAnUnplayedFixtureWithoutADate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		Fixture fixture = dao.addFixture(season, null, division, homeTeam, awayTeam, null, null);
		fixtureId1 = fixture.getFixtureId();
		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		assertTrue (retrievedFixture != null);
		assertEquals (1, fixtures.size());
		assertEquals (SEASON_1, retrievedFixture.getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), retrievedFixture.getDivision().getDivisionId());
		assertEquals (homeTeam.getTeamId(), retrievedFixture.getHomeTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), retrievedFixture.getAwayTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), retrievedFixture.getAwayTeam().getTeamId());
		assertNull (retrievedFixture.getFixtureDate());
		assertNull (retrievedFixture.getHomeGoals());
		assertNull (retrievedFixture.getAwayGoals());
	}
	
	@Test
	public void shouldAddAnUnplayedFixtureWithADate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		Fixture fixture = dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, null, null);
		fixtureId1 = fixture.getFixtureId();
		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		assertTrue (retrievedFixture != null);
		assertEquals (1, fixtures.size());
		assertEquals (SEASON_1, retrievedFixture.getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), retrievedFixture.getDivision().getDivisionId());
		assertEquals (homeTeam.getTeamId(), retrievedFixture.getHomeTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), retrievedFixture.getAwayTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), retrievedFixture.getAwayTeam().getTeamId());
		assertTrue (areDatesTheSame(fixtureDate, retrievedFixture.getFixtureDate()));
		assertNull (retrievedFixture.getHomeGoals());
		assertNull (retrievedFixture.getAwayGoals());
	}
	
	@Test
	public void shouldAddAPlayedFixture () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		Fixture fixture = dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, 2, 1);
		fixtureId1 = fixture.getFixtureId();
		
		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		assertTrue (retrievedFixture != null);
		assertEquals (1, fixtures.size());
		assertEquals (SEASON_1, retrievedFixture.getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), retrievedFixture.getDivision().getDivisionId());
		assertEquals (homeTeam.getTeamId(), retrievedFixture.getHomeTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), retrievedFixture.getAwayTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), retrievedFixture.getAwayTeam().getTeamId());
		assertTrue (areDatesTheSame(fixtureDate, retrievedFixture.getFixtureDate()));
		assertEquals (new Integer(2), retrievedFixture.getHomeGoals());
		assertEquals (new Integer(1), retrievedFixture.getAwayGoals());
	}
	
	@Test
	public void shouldGetFixturesWithNoFixtureDate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team team1 = dao.addTeam(TEAM_NAME_1);
		teamId1 = team1.getTeamId();
		Team team2 = dao.addTeam(TEAM_NAME_2);
		teamId2 = team1.getTeamId();
		Team team3 = dao.addTeam(TEAM_NAME_3);
		teamId3 = team1.getTeamId();
		Team team4 = dao.addTeam(TEAM_NAME_4);
		teamId4 = team1.getTeamId();
		Calendar fixtureDate = Calendar.getInstance();
		Fixture fixture1 = dao.addFixture(season, fixtureDate, division, team1, team2, 2, 1);
		fixtureId1 = fixture1.getFixtureId();
		Fixture fixture2 = dao.addFixture(season, fixtureDate, division, team3, team4, 1, 3);
		fixtureId2 = fixture2.getFixtureId();
		Fixture fixture3 = dao.addFixture(season, null, division, team2, team1, null, null);
		fixtureId3 = fixture3.getFixtureId();
		Fixture fixture4 = dao.addFixture(season, null, division, team4, team3, null, null);
		fixtureId4 = fixture4.getFixtureId();
		
		// When
		List<Fixture> fixtures = dao.getFixturesWithNoFixtureDate();
		
		// Then
		assertEquals (2, fixtures.size());
		assertEquals (fixture3.getFixtureId(), fixtures.get(0).getFixtureId());
		assertEquals (fixture4.getFixtureId(), fixtures.get(1).getFixtureId());
	}
	
	@Test
	public void shouldGetUnplayedFixturesWithFixtureDate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team team1 = dao.addTeam(TEAM_NAME_1);
		teamId1 = team1.getTeamId();
		Team team2 = dao.addTeam(TEAM_NAME_2);
		teamId2 = team1.getTeamId();
		Team team3 = dao.addTeam(TEAM_NAME_3);
		teamId3 = team1.getTeamId();
		Team team4 = dao.addTeam(TEAM_NAME_4);
		teamId4 = team1.getTeamId();
		Calendar fixtureDate1 = Calendar.getInstance();
		fixtureDate1.set(Calendar.YEAR, 2003);
		fixtureDate1.set(Calendar.MONTH, 4);
		fixtureDate1.set(Calendar.DAY_OF_MONTH, 15);
		Calendar fixtureDate2 = Calendar.getInstance();
		fixtureDate2.set(Calendar.YEAR, 2003);
		fixtureDate2.set(Calendar.MONTH, 4);
		fixtureDate2.set(Calendar.DAY_OF_MONTH, 21);
		Calendar fixtureDate3 = Calendar.getInstance();
		fixtureDate3.set(Calendar.YEAR, 2070);
		fixtureDate3.set(Calendar.MONTH, 4);
		fixtureDate3 .set(Calendar.DAY_OF_MONTH, 21);
		Fixture fixture1 = dao.addFixture(season, fixtureDate1, division, team1, team2, 2, 1);
		fixtureId1 = fixture1.getFixtureId();
		Fixture fixture2 = dao.addFixture(season, fixtureDate1, division, team3, team4, null, null);
		fixtureId2 = fixture2.getFixtureId();
		Fixture fixture3 = dao.addFixture(season, null, division, team2, team1, null, null);
		fixtureId3 = fixture3.getFixtureId();
		Fixture fixture4 = dao.addFixture(season, null, division, team4, team3, null, null);
		fixtureId4 = fixture4.getFixtureId();
		Fixture fixture5 = dao.addFixture(season, null, division, team2, team3, null, null);
		fixtureId5 = fixture5.getFixtureId();
		Fixture fixture6 = dao.addFixture(season, fixtureDate2, division, team1, team4, null, null);
		fixtureId6 = fixture6.getFixtureId();
		Fixture fixture7 = dao.addFixture(season, fixtureDate3, division, team2, team4, null, null);
		fixtureId7 = fixture7.getFixtureId();
		
		// When
		List<Fixture> fixtures = dao.getUnplayedFixturesBeforeToday();
		
		// Then
		assertEquals (2, fixtures.size());
		assertEquals (fixtureId2, fixtures.get(0).getFixtureId());
		assertEquals (fixtureId6, fixtures.get(1).getFixtureId());
	}

	@Test
	public void shouldGetAllFixtures () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team team1 = dao.addTeam(TEAM_NAME_1);
		teamId1 = team1.getTeamId();
		Team team2 = dao.addTeam(TEAM_NAME_2);
		teamId2 = team1.getTeamId();
		Team team3 = dao.addTeam(TEAM_NAME_3);
		teamId3 = team1.getTeamId();
		Team team4 = dao.addTeam(TEAM_NAME_4);
		teamId4 = team1.getTeamId();
		Calendar fixtureDate1 = Calendar.getInstance();
		fixtureDate1.set(Calendar.YEAR, 2003);
		fixtureDate1.set(Calendar.MONTH, 4);
		fixtureDate1.set(Calendar.DAY_OF_MONTH, 15);
		Calendar fixtureDate2 = Calendar.getInstance();
		fixtureDate2.set(Calendar.YEAR, 2003);
		fixtureDate2.set(Calendar.MONTH, 4);
		fixtureDate2.set(Calendar.DAY_OF_MONTH, 21);
		Calendar fixtureDate3 = Calendar.getInstance();
		fixtureDate3.set(Calendar.YEAR, 2070);
		fixtureDate3.set(Calendar.MONTH, 4);
		fixtureDate3 .set(Calendar.DAY_OF_MONTH, 21);
		Fixture fixture1 = dao.addFixture(season, fixtureDate1, division, team1, team2, 2, 1);
		fixtureId1 = fixture1.getFixtureId();
		Fixture fixture2 = dao.addFixture(season, fixtureDate1, division, team3, team4, null, null);
		fixtureId2 = fixture2.getFixtureId();
		Fixture fixture3 = dao.addFixture(season, null, division, team2, team1, null, null);
		fixtureId3 = fixture3.getFixtureId();
		Fixture fixture4 = dao.addFixture(season, null, division, team4, team3, null, null);
		fixtureId4 = fixture4.getFixtureId();
		Fixture fixture5 = dao.addFixture(season, null, division, team2, team3, null, null);
		fixtureId5 = fixture5.getFixtureId();
		Fixture fixture6 = dao.addFixture(season, fixtureDate2, division, team1, team4, null, null);
		fixtureId6 = fixture6.getFixtureId();
		Fixture fixture7 = dao.addFixture(season, fixtureDate3, division, team2, team4, null, null);
		fixtureId7 = fixture7.getFixtureId();
		
		// When
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		assertEquals (7, fixtures.size());
	}
	
	@Test
	public void shouldUpdateAnUnscheduledFixtureWithADate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		dao.addFixture(season, null, division, homeTeam, awayTeam, null, null);
		Fixture fixture = dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, null, null);
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		fixtureId1 = fixture.getFixtureId();
		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());

		assertTrue (retrievedFixture != null);
		assertEquals (1, fixtures.size());
		assertEquals (SEASON_1, fixtures.get(0).getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), fixtures.get(0).getDivision().getDivisionId());
		assertEquals (homeTeam.getTeamId(), fixtures.get(0).getHomeTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertTrue (areDatesTheSame(fixtureDate, fixtures.get(0).getFixtureDate()));
		assertNull (fixtures.get(0).getHomeGoals());
		assertNull (fixtures.get(0).getAwayGoals());
	}
	
	@Test
	public void shouldUpdateAScheduledFixtureWithAScore () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, null, null);
		Fixture fixture = dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, 5, 4);
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		fixtureId1 = fixture.getFixtureId();
		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());

		assertTrue (retrievedFixture != null);
		assertEquals (1, fixtures.size());
		assertEquals (SEASON_1, fixtures.get(0).getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), fixtures.get(0).getDivision().getDivisionId());
		assertEquals (homeTeam.getTeamId(), fixtures.get(0).getHomeTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertTrue (areDatesTheSame(fixtureDate, fixtures.get(0).getFixtureDate()));
		assertEquals (new Integer(5), retrievedFixture.getHomeGoals());
		assertEquals (new Integer(4), retrievedFixture.getAwayGoals());
	}

	@Test
	public void shouldUpdateAPlayedFixtureWithAnEarlierDate () {
		// This is the scenario where a playoff fixture is saved first and then the original is saved afterwards.
		
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar firstFixtureDate = Calendar.getInstance();
		firstFixtureDate.set(Calendar.YEAR, 2006);
		firstFixtureDate.set(Calendar.MONTH, 5);
		firstFixtureDate.set(Calendar.DAY_OF_MONTH, 6);
		Calendar secondFixtureDate = Calendar.getInstance();
		secondFixtureDate.set(Calendar.YEAR, 2005);
		secondFixtureDate.set(Calendar.MONTH, 10);
		secondFixtureDate.set(Calendar.DAY_OF_MONTH, 21);
		dao.addFixture(season, firstFixtureDate, division, homeTeam, awayTeam, 2, 1);
		
		// When
		dao.addFixture(season, secondFixtureDate, division, homeTeam, awayTeam, 3, 3);
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		assertEquals (1, fixtures.size());
		assertEquals (SEASON_1, fixtures.get(0).getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), fixtures.get(0).getDivision().getDivisionId());
		assertEquals (homeTeam.getTeamId(), fixtures.get(0).getHomeTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertTrue (areDatesTheSame(secondFixtureDate, fixtures.get(0).getFixtureDate()));
		assertEquals (new Integer(3), fixtures.get(0).getHomeGoals());
		assertEquals (new Integer(3), fixtures.get(0).getAwayGoals());
	}

	@Test
	public void shouldThrowAnExceptionWhenUpdatingAPlayedFixtureWithALaterDate () {
		// This is the scenario for playoffs.
		
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar firstFixtureDate = Calendar.getInstance();
		firstFixtureDate.set(Calendar.YEAR, 2005);
		firstFixtureDate.set(Calendar.MONTH, 10);
		firstFixtureDate.set(Calendar.DAY_OF_MONTH, 21);
		Calendar secondFixtureDate = Calendar.getInstance();
		secondFixtureDate.set(Calendar.YEAR, 2006);
		secondFixtureDate.set(Calendar.MONTH, 5);
		secondFixtureDate.set(Calendar.DAY_OF_MONTH, 6);
		dao.addFixture(season, firstFixtureDate, division, homeTeam, awayTeam, 2, 1);

		// When
		try {
			dao.addFixture(season, secondFixtureDate, division, homeTeam, awayTeam, 5, 4);
			fail("Should throw an exception when trying to save a playoff over a regular match");
		} catch (ChangeScoreException e) {
			assertEquals ("Can't save a playoff result over a regular game", e.getMessage());
		}
		
		List<Fixture> fixtures = dao.getFixtures();
		
		// Then
		assertEquals (1, fixtures.size());
		assertEquals (SEASON_1, fixtures.get(0).getSeason().getSeasonNumber());
		assertEquals (division.getDivisionId(), fixtures.get(0).getDivision().getDivisionId());
		assertEquals (homeTeam.getTeamId(), fixtures.get(0).getHomeTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertEquals (awayTeam.getTeamId(), fixtures.get(0).getAwayTeam().getTeamId());
		assertTrue (areDatesTheSame(firstFixtureDate, fixtures.get(0).getFixtureDate()));
		assertEquals (new Integer(2), fixtures.get(0).getHomeGoals());
		assertEquals (new Integer(1), fixtures.get(0).getAwayGoals());
	}

	private boolean areDatesTheSame (Calendar date1, Calendar date2) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date1String = sdf.format(date1.getTime());
		String date2String = sdf.format(date2.getTime());
		return date1String.equals(date2String);
	}
}
