package ai.migrate.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.migrate.dao.SearchDao;
import ai.migrate.mr.SearchContainer;
import ai.migrate.mr.SearchMapper;
import ai.migrate.mr.SearchReducer;
import ai.migrate.pojo.TermWord;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.utils.MigrateGlobal;
import ai.utils.StringUtils;

public class SearchService {
	private SearchDao searchDao = new SearchDao();

	public List<TermWord> searchTerm(String term) throws SQLException {
		return searchDao.searchTerm(term);
	}
	
	public List<TermWord> searchParaTerm(String para) throws SQLException {
		List<String> phraseList =  StringUtils.toShortPhrase(para);
		List<TermWord> result = new ArrayList<>();
		try(IRContainer contain = new SearchContainer()) {
			for (String phrase: phraseList) {
				IMapper mapper = new SearchMapper();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put(MigrateGlobal.SEARCH_PHRASE, phrase);
				mapper.setParameters(params);
				contain.registerMapper(mapper);
			}
			IReducer reducer = new SearchReducer();
			contain.registerReducer(reducer);
			result.addAll((List<TermWord>) contain.Init().running());
		}	
		return result;
	}
}
