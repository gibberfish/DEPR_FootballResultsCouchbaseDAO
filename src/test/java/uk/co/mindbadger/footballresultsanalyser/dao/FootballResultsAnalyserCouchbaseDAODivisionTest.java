package uk.co.mindbadger.footballresultsanalyser.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;

public class FootballResultsAnalyserCouchbaseDAODivisionTest {
	private FootballResultsAnalyserCouchbaseDAO dao;

	private static CouchbaseUtilities cbUtils = new CouchbaseUtilities ();

	private static final String DIV_NAME_1 = "Premier";
	private static final String DIV_NAME_2 = "Championship";
	private static final String TEST_BUCKET_NAME = "footballTest";
	
	private DomainObjectFactory domainObjectFactory;
	private String divisionId1;
	private String divisionId2;
	
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
		cbUtils.tearDownDivisions(new String[] {divisionId1, divisionId2}, TEST_BUCKET_NAME);
	}
	
	@Test
	public void shouldBeNoDivisionsToStartWith () {
		// When
		Map<String, Division> divisions = dao.getAllDivisions();
		
		// Then
		assertEquals (0, divisions.size());		
	}

	@Test
	public void shouldAddADivision() {
		// When
		Division division = dao.addDivision(DIV_NAME_1);
		Map<String, Division> divisions = dao.getAllDivisions();
		
		// Then (check return value)
		assertEquals (DIV_NAME_1, division.getDivisionName());
		divisionId1 = division.getDivisionId();
		
		// Then (get it from the list)
		assertEquals (1, divisions.size());
		assertEquals (DIV_NAME_1, divisions.get(divisionId1).getDivisionName());
		assertEquals (divisionId1, divisions.get(divisionId1).getDivisionId());
		
		// Then (get it by key)
		assertEquals (DIV_NAME_1, dao.getDivision(divisionId1).getDivisionName());
	}

	@Test
	public void shouldAddTwoDivisions () {
		// When
		Division division1 = dao.addDivision(DIV_NAME_1);
		Division division2 = dao.addDivision(DIV_NAME_2);
		Map<String, Division> divisions = dao.getAllDivisions();
		
		// Then (check return value)
		assertEquals (DIV_NAME_1, division1.getDivisionName());
		divisionId1 = division1.getDivisionId();
		assertEquals (DIV_NAME_2, division2.getDivisionName());
		divisionId2 = division2.getDivisionId();
		
		// Then (get it from the list)
		assertEquals (2, divisions.size());
		assertEquals (DIV_NAME_1, divisions.get(divisionId1).getDivisionName());
		assertEquals (divisionId1, divisions.get(divisionId1).getDivisionId());
		assertEquals (DIV_NAME_2, divisions.get(divisionId2).getDivisionName());
		assertEquals (divisionId2, divisions.get(divisionId2).getDivisionId());
		
		// Then (get it by key)
		assertEquals (DIV_NAME_1, dao.getDivision(divisionId1).getDivisionName());
		assertEquals (DIV_NAME_2, dao.getDivision(divisionId2).getDivisionName());
	}

	@Test
	public void shouldUpdateAnExistingDivision() {
		// When
		dao.addDivision(DIV_NAME_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Map<String, Division> divisions = dao.getAllDivisions();
		
		// Then (check return value)
		assertEquals (DIV_NAME_1, division.getDivisionName());
		divisionId1 = division.getDivisionId();
		
		// Then (get it from the list)
		assertEquals (1, divisions.size());
		assertEquals (DIV_NAME_1, divisions.get(divisionId1).getDivisionName());
		assertEquals (divisionId1, divisions.get(divisionId1).getDivisionId());
		
		// Then (get it by key)
		assertEquals (DIV_NAME_1, dao.getDivision(divisionId1).getDivisionName());
	}

	@Test
	public void shouldThrowAnExceptionWhenTryingToGetANonExistentSeason () {
		// Given

		try {
			// When
			dao.getDivision(divisionId1);
			fail ("An exception should be thrown if a division does not exist");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Division "+divisionId1+" does not exist", e.getMessage());
		}
	}	
}
