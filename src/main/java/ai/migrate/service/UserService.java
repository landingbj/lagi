package ai.migrate.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import ai.migrate.dao.UserDao;
import ai.migrate.pojo.UserEntity;

public class UserService {
	private UserDao userDao = new UserDao();
	
	public String getRandomCategory() {
	    String category = UUID.randomUUID().toString().replace("-", "");
	    UserEntity entity = new UserEntity();
	    entity.setCategory(category);
	    entity.setCategoryCreateTime(new Date());
	    int count = 0;
        try {
            count = userDao.addTempCategory(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	    if(count > 0) {
	        return category;
	    }
	    return null;
	}
}
