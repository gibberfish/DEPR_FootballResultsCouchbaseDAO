package mindbadger.footballresultsanalyser.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mindbadger.footballresultsanalyser.dao.FootballResultsAnalyserCouchbaseDAO;
import mindbadger.footballresultsanalyser.domain.Season;

@Component
public class SeasonRepositoryImpl implements SeasonRepository {

	@Autowired
	private FootballResultsAnalyserCouchbaseDAO dao;

	@Override
	public void delete(Season arg0) {
		throw new UnsupportedOperationException("delete season not yet supported");
	}

	@Override
	public Iterable<Season> findAll() {
		return dao.getSeasons();
	}

	@Override
	public Season findOne(Integer seasonNumber) {
		return dao.getSeason(seasonNumber);
	}

	@Override
	public Season save(Season season) {
		Season retrievedSeason = dao.getSeason(season.getSeasonNumber());
		if (retrievedSeason == null) {
			retrievedSeason = dao.addSeason(season.getSeasonNumber());
			retrievedSeason.setSeasonDivisions(season.getSeasonDivisions());
		}
		
		return retrievedSeason;
	}
}
