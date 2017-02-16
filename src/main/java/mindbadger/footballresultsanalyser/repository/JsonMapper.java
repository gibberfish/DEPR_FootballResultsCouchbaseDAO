package mindbadger.footballresultsanalyser.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.couchbase.client.java.document.json.JsonObject;

import mindbadger.footballresultsanalyser.dao.FootballResultsAnalyserDAO;
import mindbadger.footballresultsanalyser.domain.Division;
import mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import mindbadger.footballresultsanalyser.domain.Fixture;
import mindbadger.footballresultsanalyser.domain.Season;
import mindbadger.footballresultsanalyser.domain.Team;

@Component
public class JsonMapper {
	@Autowired
	DomainObjectFactory domainObjectFactory;
	
	@Autowired
	FootballResultsAnalyserDAO dao;
	
	public Season mapJsonToSeason(JsonObject jsonSeason) {
		return domainObjectFactory.createSeason(jsonSeason.getInt("seasonNumber"));
	}

	public Division mapJsonToDivision (JsonObject jsonDivision) {
		Division divisionObject = domainObjectFactory.createDivision(jsonDivision.getString("divisionName"));
		Long divisionId = jsonDivision.getLong("divisionId");
		divisionObject.setDivisionId(divisionId.toString());
		return divisionObject;
	}

	public Team mapJsonToTeam (JsonObject jsonDivision) {
		Team teamObject = domainObjectFactory.createTeam(jsonDivision.getString("teamName"));
		Long teamId = jsonDivision.getLong("teamId");
		teamObject.setTeamId(teamId.toString());
		return teamObject;
	}

	public Fixture mapJsonToFixture(JsonObject jsonFixture) {
		Season season = dao.getSeason(jsonFixture.getInt("seasonNumber"));
		Division division = dao.getDivision(jsonFixture.getString("divisionId"));
		Team homeTeam = dao.getTeam(jsonFixture.getString("homeTeamId"));
		Team awayTeam = dao.getTeam(jsonFixture.getString("awayTeamId"));
		Integer fixtureId = jsonFixture.getInt("fixtureId");
		
		Fixture fixtureObject = domainObjectFactory.createFixture(season, homeTeam, awayTeam);
		fixtureObject.setFixtureId(fixtureId.toString());
		fixtureObject.setDivision(division);
		
		String fixtureDateString = jsonFixture.getString("fixtureDate");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat alternativeDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		if (fixtureDateString != null) {
			Calendar fixtureDate = Calendar.getInstance();
			try {
				fixtureDate.setTime(dateFormat.parse(fixtureDateString));
				fixtureObject.setFixtureDate(fixtureDate);
			} catch (ParseException e1) {
				try {
					fixtureDate.setTime(alternativeDateFormat.parse(fixtureDateString));
					fixtureObject.setFixtureDate(fixtureDate);
				}  catch (ParseException e) {
					e.printStackTrace();
					throw new RuntimeException (e);
				}
			}
		}
		
		Integer awayGoals = jsonFixture.getInt("awayGoals");
		Integer homeGoals = jsonFixture.getInt("homeGoals");
		
		if (homeGoals != null) {
			fixtureObject.setHomeGoals(homeGoals);
		}

		if (awayGoals != null) {
			fixtureObject.setAwayGoals(awayGoals);
		}

		return fixtureObject;
	}

	public void setDao(FootballResultsAnalyserDAO dao) {
		this.dao = dao;
	}

	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

}
