package ai.migrate.service;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import ai.common.pojo.UserRagSetting;
import ai.migrate.dao.UploadFileDao;
import ai.migrate.db.Conn;
import ai.common.pojo.UploadFile;

public class UploadFileService {
    private UploadFileDao uploadFileDao = new UploadFileDao();

    public int addUploadFile(UploadFile entity) throws SQLException {
        return uploadFileDao.addUploadFile(entity);
    }

    public int deleteUploadFile(String category)  {
        try {
            return uploadFileDao.deleteUploadFile(category);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int deleteUploadFile(List<String> idList) {
        int result = -1;
        Conn conn = new Conn();
        UploadFile uploadFile = null;
        try {
            conn.setAutoCommit(false);
            for (String fileId : idList) {
                uploadFile = uploadFileDao.getUploadFileList(fileId, conn);
                uploadFileDao.deleteUploadFile(fileId, conn);
            }
            conn.commit();
            result = idList.size();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            conn.close();
        }

        if (uploadFile != null) {
            File file = new File(uploadFile.getFilepath());
            if (file.exists()) {
                file.delete();
            }
        }
        return result;
    }

    public List<UploadFile> getUploadFileList(int pageNumber, int pageSize, String category, String userId) throws SQLException {
        if (category == null && userId == null) {
            return uploadFileDao.getUploadFileList(pageNumber, pageSize);
        }
        return uploadFileDao.getUploadFileList(pageNumber, pageSize, category, userId);
    }

    public int getTotalRow(String category, String userId) throws SQLException {
        if (category == null && userId == null) {
            return uploadFileDao.getTotalRow();
        }
        return uploadFileDao.getTotalRow(category, userId);
    }
    public List<UserRagSetting> getTextBlockSize(String category, String userId) throws SQLException {
        return uploadFileDao.getTextBlockSize(category, userId);
    }

    public boolean updateTextBlockSize(UserRagSetting userRagSetting) throws SQLException {
        return uploadFileDao.updateTextBlockSize(userRagSetting) >0;
    }
    public boolean deleteTextBlockSize(UserRagSetting userRagSetting) throws SQLException {
        return uploadFileDao.deleteTextBlockSize(userRagSetting) >0;
    }
}
