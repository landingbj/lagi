package ai.migrate.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import ai.migrate.dao.UserDao;
import ai.common.pojo.UserEntity;
import ai.servlet.dto.LoginRequest;
import ai.servlet.dto.LoginResponse;
import ai.servlet.dto.RegisterRequest;
import ai.servlet.dto.RegisterResponse;
import ai.utils.OkHttpUtil;
import com.google.gson.Gson;

public class UserService {
    private final UserDao userDao = new UserDao();
    private final Gson gson = new Gson();
    private static final String SAAS_BASE_URL = "https://saas.landingbj.com";

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
        if (count > 0) {
            return category;
        }
        return null;
    }

    public LoginResponse login(LoginRequest loginRequest) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/loginByName", gson.toJson(loginRequest));
        LoginResponse loginResponse = gson.fromJson(resultJson, LoginResponse.class);
        return loginResponse;
    }

    public RegisterResponse register(RegisterRequest registerRequest) throws IOException {
        registerRequest.setDomainName(registerRequest.getUsername());
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/registerChannel", gson.toJson(registerRequest));
        RegisterResponse registerResponse = gson.fromJson(resultJson, RegisterResponse.class);
        if ("failed".equals(registerResponse.getStatus())) {
            if (registerResponse.getMsg().contains("存在该域名")) {
                registerResponse.setMsg("用户名已存在");
            }
        }
        return registerResponse;
    }
}
