package ai.migrate.mr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ai.learn.questionAnswer.KShingle;
import ai.migrate.dao.SearchDao;
import ai.migrate.pojo.TermWord;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.utils.MigrateGlobal;



public class SearchMapper extends BaseMapper implements IMapper {
	private KShingle kShingle = new KShingle();
	private SearchDao searchDao = new SearchDao();
	protected int priority;

	@Override
	public List<?> myMapping() {
		String phrase = (String) this.getParameters().get(MigrateGlobal.SEARCH_PHRASE);
		
		List<Object> result = new ArrayList<>();
		Set<String> shingleSet =  kShingle.getShingles(phrase, 2, phrase.length() - 1).keySet();
		String searchStr = shingleToStr(shingleSet);
		try {
			List<TermWord> searchList = searchDao.searchTerm(searchStr);
			List<TermWord> resultList = new ArrayList<>();
			for(TermWord termWord : searchList) {
				if (phrase.indexOf(termWord.getCn().trim()) > -1) {
					resultList.add(termWord);
				}
			}
			result.add(resultList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		result.add(getPriority());
		return result;
	}

	private String shingleToStr(Set<String> shingleSet) {
		StringBuilder sb = new StringBuilder();
		for (String s: shingleSet) {
			sb.append(s).append(" ");
		}
		return sb.toString();
	}
	
	/*
	 * @see mr.IMapper#setPriority(int)
	 */
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/*
	 * @see mr.IMapper#getPriority()
	 */
	@Override
	public int getPriority() {
		return priority;
	}
}
