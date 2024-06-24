package ai.migrate.dao;

import ai.dto.ModelPreferenceDto;
import ai.migrate.db.Conn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserModelPreferenceDao {

    private final Logger log = LoggerFactory.getLogger(UserModelPreferenceDao.class);

    public ModelPreferenceDto getUserModelPreference(String finger)  {
        String sqlCheck = "SELECT finger, llm, tts, asr, img2Text, imgGen, imgEnhance, img2Video, text2Video, videoEnhance, videoTrack  FROM lagi_user_preference WHERE finger = ?";
        Conn conn = null;
        try {
            // 获取数据库连接
            conn = new Conn();
            PreparedStatement ps = conn.prepareStatement(sqlCheck);
            ps.setString(1, finger);
            ResultSet rs = ps.executeQuery();
            ModelPreferenceDto modelPreferenceDto = new ModelPreferenceDto();
            modelPreferenceDto.setFinger(rs.getString(1));
            modelPreferenceDto.setLlm(rs.getString(2));
            modelPreferenceDto.setTts(rs.getString(3));
            modelPreferenceDto.setAsr(rs.getString(4));
            modelPreferenceDto.setImg2Text(rs.getString(5));
            modelPreferenceDto.setImgGen(rs.getString(6));
            modelPreferenceDto.setImgEnhance(rs.getString(7));
            modelPreferenceDto.setImg2Video(rs.getString(8));
            modelPreferenceDto.setText2Video(rs.getString(9));
            modelPreferenceDto.setVideoEnhance(rs.getString(10));
            modelPreferenceDto.setVideoTrack(rs.getString(11));
            return modelPreferenceDto;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.close();
        }
        return null;
    }


    public int saveOrUpdate(ModelPreferenceDto modelPreferenceDto) {

        String sqlCheck = "SELECT * FROM lagi_user_preference WHERE finger = ?";
        String sqlInsert = "INSERT INTO lagi_user_preference (finger, user_id, llm, tts, asr, img2Text, imgGen, imgEnhance, img2Video, text2Video,\n" +
                "                                  videoEnhance, videoTrack)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        Conn conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;
        int res = 0;
        try {
            // 获取数据库连接
            conn = new Conn();
            // 禁用自动提交，开启事务
            conn.setAutoCommit(false);
            // 检查记录是否存在
            pstmtCheck = conn.prepareStatement(sqlCheck);
            System.out.println(modelPreferenceDto.getFinger());
            pstmtCheck.setString(1, modelPreferenceDto.getFinger());
            rs = pstmtCheck.executeQuery();
            if (rs.next()) { // 如果记录存在，则更新
                res = updatePreference(conn, modelPreferenceDto);
            } else { // 如果记录不存在，则插入
                System.out.println(modelPreferenceDto);
                pstmtInsert = conn.prepareStatement(sqlInsert);
                pstmtInsert.setString(1, modelPreferenceDto.getFinger());
                pstmtInsert.setString(2, modelPreferenceDto.getLlm());
                pstmtInsert.setString(3, modelPreferenceDto.getTts());
                pstmtInsert.setString(4, modelPreferenceDto.getAsr());
                pstmtInsert.setString(5, modelPreferenceDto.getImg2Text());
                pstmtInsert.setString(6, modelPreferenceDto.getImgGen());
                pstmtInsert.setString(7, modelPreferenceDto.getImgEnhance());
                pstmtInsert.setString(8, modelPreferenceDto.getImg2Video());
                pstmtInsert.setString(9, modelPreferenceDto.getText2Video());
                pstmtInsert.setString(10, modelPreferenceDto.getVideoEnhance());
                pstmtInsert.setString(11, modelPreferenceDto.getVideoTrack());
                res =  pstmtInsert.executeUpdate();
                System.out.println(res);
            }
            // 提交事务
            conn.commit();
            log.info("数据库更新成功");

        } catch (SQLException e) {
            // 如果出现异常，回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error(ex.getMessage());
                }
            }
            log.error(e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (pstmtCheck != null) pstmtCheck.close();
                if (pstmtUpdate != null) pstmtUpdate.close();
                if (pstmtInsert != null) pstmtInsert.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return res;
    }


    private int updatePreference(Conn conn, ModelPreferenceDto modelPreferenceDto) throws SQLException {
        StringBuilder updateBuilder = new StringBuilder();
        List<String> fields = new ArrayList<>();
        List<String> params = new ArrayList<>();
        updateBuilder.append("UPDATE lagi_user_preference SET ");
        if(modelPreferenceDto.getLlm() != null) {
            fields.add("llm");
            params.add(modelPreferenceDto.getLlm());
        }
        if(modelPreferenceDto.getTts() != null) {
            fields.add("tts");
            params.add(modelPreferenceDto.getTts());
        }
        if (modelPreferenceDto.getAsr() != null) {
            fields.add("asr");
            params.add(modelPreferenceDto.getAsr());
        }
        if (modelPreferenceDto.getImg2Text() != null) {
            fields.add("img2Text");
            params.add(modelPreferenceDto.getImg2Text());
        }
        if (modelPreferenceDto.getImgGen() != null) {
            fields.add("imgGen");
            params.add(modelPreferenceDto.getImgGen());
        }
        if (modelPreferenceDto.getImgEnhance() != null) {
            fields.add("imgEnhance");
            params.add(modelPreferenceDto.getImgEnhance());
        }
        if (modelPreferenceDto.getImg2Video() != null) {
            fields.add("img2Video");
            params.add(modelPreferenceDto.getImg2Video());
        }
        if (modelPreferenceDto.getText2Video() != null) {
            fields.add("text2Video");
            params.add(modelPreferenceDto.getText2Video());
        }
        if (modelPreferenceDto.getVideoEnhance() != null) {
            fields.add("videoEnhance");
            params.add(modelPreferenceDto.getVideoEnhance());
        }
        if (modelPreferenceDto.getVideoTrack() != null) {
            fields.add("videoTrack");
            params.add(modelPreferenceDto.getVideoTrack());
        }
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            updateBuilder.append(field).append("=").append("? ");
            if(i != fields.size() -1) {
                updateBuilder.append(", ");
            }
        }
        updateBuilder.append(" where finger = ?;");
        PreparedStatement preparedStatement = conn.prepareStatement(updateBuilder.toString());
        for (int i = 1; i <= params.size(); i++) {
            preparedStatement.setString(i , params.get(0));
        }
        preparedStatement.setString(params.size() + 1, modelPreferenceDto.getFinger());
        int res = preparedStatement.executeUpdate();
        preparedStatement.close();
        return res;
    }

    public Integer remove(String finger) {
        String sql = "DELETE\n" +
                "FROM lagi_user_preference\n" +
                "WHERE finger = ?;";
        try (Conn conn = new Conn()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, finger);
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return -1;
    }

    public static void main(String[] args) {
        ModelPreferenceDto dto = ModelPreferenceDto.builder().finger("aaa").llm("gpt").asr("asr").build();
        UserModelPreferenceDao userModelPreferenceDao = new UserModelPreferenceDao();
        userModelPreferenceDao.saveOrUpdate(dto);
    }
}
