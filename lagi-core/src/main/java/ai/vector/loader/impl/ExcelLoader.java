package ai.vector.loader.impl;

import ai.common.pojo.FileChunkResponse;
import ai.utils.EasyExcelUtil;
import ai.utils.ExcelSqlUtil;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.pojo.SplitConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ExcelLoader implements DocumentLoader {
    @Override
    public List<List<FileChunkResponse.Document>> load(String path, SplitConfig splitConfig) {
        return excelLoad(path, splitConfig);
    }

    public List<List<FileChunkResponse.Document>> excelLoad(String path, SplitConfig splitConfig) {
        File file = new File(path);
        if (ExcelSqlUtil.isSql(file.getPath())) {
            if (ExcelSqlUtil.isSqlietConnect() || ExcelSqlUtil.isConnect()) {
                try {
                    ExcelSqlUtil.uploadSql(file.getPath(), (String) splitConfig.getExtra().get("filename"), (String) splitConfig.getExtra().get("file_id"));
                } catch (Exception e) {
                    log.error("excel: upload sql error", e);
                }
            }
        } else {
            try {
                return EasyExcelUtil.getChunkDocumentExcel(file, splitConfig.getChunkSizeForText());
            } catch (Exception e) {
                log.error("load excel file error", e);
            }
        }
        return Collections.emptyList();
    }
}
