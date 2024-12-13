package ai.migrate.service;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import ai.migrate.dao.UploadFileDao;
import ai.migrate.db.Conn;
import ai.common.pojo.UploadFile;

public class UploadFileService {
    private UploadFileDao uploadFileDao = new UploadFileDao();

    public int addUploadFile(UploadFile entity) throws SQLException {
        return uploadFileDao.addUploadFile(entity);
    }

    public int addUploadMeetingFile(UploadFile entity) throws SQLException {
        return uploadFileDao.addUploadMeetingFile(entity);
    }

    public boolean getAddCount(String fileId) throws SQLException {
        return uploadFileDao.getAddCount(fileId);
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

     public int deleteCategoryFile (List<String> idList,String category) {
          int result = -1;
        Conn conn = new Conn();
        try {
            conn.setAutoCommit(false);
            if (category == null){
               for (String fileId : idList) {
                result = uploadFileDao.deleteUploadMeetingFile(fileId, conn);
               }
            }else {
                for (String fileId : idList) {
                result = uploadFileDao.deleteUploadMeetingFile(fileId,category, conn);
               }
            }

            conn.commit();
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
        return result;
     }

    public List<UploadFile> getUploadFileList(int pageNumber, int pageSize, String category) throws SQLException {
        if (category == null) {
            return uploadFileDao.getUploadFileList(pageNumber, pageSize);
        }
        return uploadFileDao.getUploadFileList(pageNumber, pageSize, category);
    }

    public List<UploadFile> getMeetingUploadFileList(int pageNumber, int pageSize, String category) throws SQLException {
        if (category == null) {
            return uploadFileDao.getMeetingUploadFileList(pageNumber, pageSize);
        }
        return uploadFileDao.getMeetingUploadFileList(pageNumber, pageSize, category);
    }
    
    public int getTotalRow(String category) throws SQLException {
        if (category == null) {
            return uploadFileDao.getTotalRow();
        }
        return uploadFileDao.getTotalRow(category);
    }
    public int getMeetingTotalRow(String category) throws SQLException {
        if (category == null) {
            return uploadFileDao.getMeetingTotalRow();
        }
        return uploadFileDao.getMeetingTotalRow(category);
    }
    public int getMeetingFileIdTotalRow(String category,String file_id) throws SQLException {
        return uploadFileDao.getMeetingTotalRow(category,file_id);
    }
     public List<String> getMeetingPermissions(String file_id){
        List<String> categories = null;
        try {
            categories = uploadFileDao.getMeetingPermissions(file_id);
        }catch (SQLException e){
            e.printStackTrace();
        }
       return categories;
    }
}
