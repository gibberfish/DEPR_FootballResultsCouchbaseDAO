package uk.co.mindbadger.footballresultsanalyser.dao;

import com.couchbase.client.core.CouchbaseCore;
import com.couchbase.client.java.bucket.BucketFlusher;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivision;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionTeam;
import uk.co.mindbadger.footballresultsanalyser.domain.Team;

public class DAOTest {
	public static void main(String[] args) {
		FootballResultsAnalyserCouchbaseDAO dao = new FootballResultsAnalyserCouchbaseDAO ();
		DomainObjectFactory domainObjectFactory = new DomainObjectFactoryImpl();
		dao.setDomainObjectFactory(domainObjectFactory);
		dao.setBucketName("footballTest");

		BucketFlusher.flush(new CouchbaseCore (), "footballTest", "");
		
		dao.startSession();
		
		Season season = dao.addSeason(2001);
		Division division = dao.addDivision("Premier");
		Team team = dao.addTeam("Portsmouth");
		
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 2);
		
		SeasonDivisionTeam seasonDivisionTeam = dao.addSeasonDivisionTeam(seasonDivision, team);
		
		dao.closeSession();
	}
}
