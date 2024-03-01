package ai.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ai.learning.trigger.ArticleTrigger;
import ai.learning.trigger.ITrigger;
import ai.migrate.pojo.AddDocsCustomResponse;
import ai.migrate.pojo.Document;
import ai.migrate.pojo.ExtractContentResponse;
import ai.migrate.pojo.Response;
import ai.migrate.service.FileService;
import ai.migrate.service.VectorDbService;
import ai.utils.AiGlobal;
import ai.utils.HadoopUtil;
import ai.utils.MigrateGlobal;
import ai.utils.pdf.PdfUtil;
import ai.utils.word.WordUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

public class UploadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final int FILE_MAX_BYTE_SIZE   = AiGlobal.FILE_PARTITION_CHUNK_SIZE;
	private static final boolean _APPROACH_HADOOP = AiGlobal.IS_HADOOP;
	
	private Gson gson = new Gson();
	private ITrigger acTrigger = new ArticleTrigger();
	private FileService fileService = new FileService();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setHeader("Content-Type", "text/html;charset=utf-8");

		String url = req.getRequestURI();
		String method = url.substring(url.lastIndexOf("/") + 1);

		if (method.equals("uploadLearningFile")) {
			this.uploadLearningFile(req, resp);
		} else if(method.equals("downloadFile")){
			this.downloadFile(req, resp);
    	} else if(method.equals("uploadImageFile")) {
    	    this.uploadImageFile(req, resp);
    	}else if(method.equals("uploadVideoFile")) {
    	    this.uploadVideoFile(req, resp);
    	}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	// 下载文件
	private void downloadFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		
		String filePath = req.getParameter("filePath");
        String fileName = req.getParameter("fileName");
        // 设置响应内容类型
        resp.setContentType("application/pdf");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        
        // 读取文件并写入响应流
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            OutputStream outputStream = resp.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
    private void uploadVideoFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("status", "failed");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
        String filePath = getServletContext().getRealPath("/upload");
        if (!new File(filePath).isDirectory()) {
            new File(filePath).mkdirs();
        }
        
        String lastFilePath = "";
        
        try {
            // 存储文件
            List<?> fileItems = upload.parseRequest(req);
            Iterator<?> it = fileItems.iterator();
            
            while (it.hasNext()) {
                FileItem fi = (FileItem) it.next();
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file = null;
                    String newName = null;
                    do {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
                        file = new File(filePath + File.separator + newName);
                        lastFilePath = filePath + File.separator + newName;
                        session.setAttribute("last_video_file", lastFilePath);
                        jsonResult.addProperty("status", "success");
                    } while (file.exists());
                    fi.write(file);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }
	
    private void uploadImageFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("status", "failed");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
        String filePath = getServletContext().getRealPath("/upload");
        if (!new File(filePath).isDirectory()) {
            new File(filePath).mkdirs();
        }
        
        String lastFilePath = "";
        
        try {
            // 存储文件
            List<?> fileItems = upload.parseRequest(req);
            Iterator<?> it = fileItems.iterator();
            
            while (it.hasNext()) {
                FileItem fi = (FileItem) it.next();
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file = null;
                    String newName = null;
                    do {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
                        file = new File(filePath + File.separator + newName);
                        lastFilePath = filePath + File.separator + newName;
                        session.setAttribute("last_image_file", lastFilePath);
                        jsonResult.addProperty("status", "success");
                    } while (file.exists());
                    fi.write(file);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }
	
	private void uploadLearningFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        session.setAttribute("uploadProgress", 0);
        
        String category = req.getParameter("category");
        
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("result", false);
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        String filePath = getServletContext().getRealPath("/upload");
        if (!new File(filePath).isDirectory()) {
            new File(filePath).mkdirs();
        }
        
        List<File> files = new ArrayList<>();
        Map<String, String> realNameMap = new HashMap<String, String>();
        
        try {
            // 存储文件
            List<?> fileItems = upload.parseRequest(req);
            Iterator<?> it = fileItems.iterator();
            
            while (it.hasNext()) {
                FileItem fi = (FileItem) it.next();
                            
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file = null;
                    String newName = null;
                    do {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
                        String lastFilePath = filePath + File.separator + newName;
                        file = new File(lastFilePath);
                        session.setAttribute(newName, file.toString());
                        session.setAttribute("lastFilePath",lastFilePath);
                    } while (file.exists());
                    
                    if (_APPROACH_HADOOP) {
                        HadoopUtil.createFileByInputStream(fi.getInputStream(), AiGlobal.KGRAPH_HADOOP_DIR + newName);
                    } else {
                        fi.write(file);
                    }
                    files.add(file);
                    realNameMap.put(file.getName(), fileName);
                }
            }

            // 解析文件

        } catch (Exception ex) {
            jsonResult.put("msg", "解析文件出现错误");
            ex.printStackTrace();
        } 
        
        session.setAttribute("uploadProgress", 20);
        List<String> jsonArray = new ArrayList<>();
        
        if (files.size() != 0) {
            String content = "";
            
            for (File file : files) {
                String extString = file.getName().substring(file.getName().lastIndexOf("."));
                
                InputStream in = null;
                
                if (_APPROACH_HADOOP) {
                    in = HadoopUtil.getInputStream(AiGlobal.KGRAPH_HADOOP_DIR + file.getName());
                } else {
                    in = new FileInputStream(file);
                }
                
                if (".doc".equals(extString) || ".docx".equals(extString)) {
                    content = WordUtils.getContentsByWord(in, extString);
                } else if (".txt".equals(extString)) {
                    content = getString(in);
                } else if (".pdf".equals(extString)) {
                    content = PdfUtil.webPdfParse(in).replaceAll("[\r\n?|\n]", "");
                } else {
                    jsonResult.put("msg", "请选择Word/PDF/Txt文件");
                }
                in.close();
                
                if (!StringUtils.isEmpty(content)) {
                    String fileName = file.getName().replace(extString, "");
                    List<String> fileList = splitFile(fileName, content, FILE_MAX_BYTE_SIZE);
                    jsonArray.addAll(fileList);
                    String filename = realNameMap.get(file.getName());
                    new AddDocIndex(file, extString, category, filename, content).start();
                }
                session.setAttribute("uploadProgress", 40);
            }
            if (jsonArray.size() > 0) {
                jsonResult.put("keywords", jsonArray);
                jsonResult.put("result", true);
            }
        }
        
        session.setAttribute("uploadFiles", new LinkedList<String>(jsonArray));
        if (!jsonResult.containsKey("msg")) {
            jsonResult.put("file", jsonArray.get(0));
            jsonResult.put("result", true);
            jsonResult.put("status", true);
        }
        
        PrintWriter out = resp.getWriter();
        out.write(jsonResult.toJSONString());
        out.flush();
        out.close();
    }
    
    
    public class AddDocIndex extends Thread {
        private List<Document> docs;
        private VectorDbService vectorDbService = new VectorDbService();
        private String extString;
        private File file;
        private String category;
        private String content;
        private String filename;
        private String extractFilepath;
        
        public AddDocIndex(File file, String extString, String category, String filename, String content) {
            this.file = file;
            this.category = category;
            this.filename = filename;
            this.content = content;
            this.extString = extString;
        }
        
        public void run() {
            docs = new ArrayList<>();
            if (".pdf".equals(extString) || ".doc".equals(extString) || ".docx".equals(extString)) {
                try {
                    ExtractContentResponse response = fileService.extractContentWithoutImage(file);
                    docs = response.getData();
                    extractFilepath = response.getFilepath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Document doc : docs) {
                    doc.setCategory(category);
                    doc.setFilename(filename);
                    doc.setFilepath(file.getAbsolutePath());
                }
            } else {
                Document doc = new Document();
                doc.setCategory(category);
                doc.setFilename(filename);
                doc.setFilepath(file.getAbsolutePath());
                doc.setText(content);
                docs.add(doc);
            }
            try {
                addDocIndex(docs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private boolean addDocIndex(List<Document> docs) throws IOException {
            Map<String, String> header = new HashMap<String, String>();
            header.put("Content-type", "application/json");
            String result = null;
            
            if (".pdf".equals(extString) || ".doc".equals(extString) || ".docx".equals(extString)) {
                for (int i = 0; i < docs.size(); i ++) {
                    docs.get(i).setDoc_id(i);
                }
                AddDocsCustomResponse addDocsCustomResponse = vectorDbService.addDocsCustom(docs);
                List<Document> docList = addDocsCustomResponse.getData();
                Map<Integer, String> docIdMap = new HashMap<>();
                for (Document doc : docList) {
                    docIdMap.put(doc.getDoc_id(), doc.getId());
                }
                ExtractContentResponse response = fileService.extractContent(extractFilepath);
                List<Document> updateDocList = response.getData();
                for (int i = 0; i < updateDocList.size(); i ++) {
                    updateDocList.get(i).setId(docIdMap.get(i));
                }
                result = vectorDbService.updateDocImage(updateDocList);
            } else {
                result = vectorDbService.addDocs(docs);
            }
            
            if (result == null) {
                return false;
            }
            Response response = gson.fromJson(result, Response.class);
            if (response.getStatus().equals("success")) {
                return true;
            }
            return false;
        }
    }
	
	private List<String> splitFile(String fileName, String fileContent, int maxByteSize) throws IOException {
		String destDir = this.getServletContext().getRealPath(AiGlobal.DIR_TEMP);
		File tmpFile = new File(destDir);
		if (!tmpFile.exists()) {
			tmpFile.mkdir();
		}
		
		List<String> result = new ArrayList<>();
		String regx = "[。？！.；;!\\?]";
		String[] strs = fileContent.split(regx);

		int fileIdx = 1;

		StringBuilder chunk = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			chunk.append(strs[i]).append(";");
			if (chunk.toString().getBytes(StandardCharsets.UTF_8).length > FILE_MAX_BYTE_SIZE) {
				String name = fileName + "_" + fileIdx++ + ".txt";
				if (_APPROACH_HADOOP) {
					InputStream in =  IOUtils.toInputStream(chunk.toString(), StandardCharsets.UTF_8);
					HadoopUtil.createFileByInputStream(in, AiGlobal.KGRAPH_HADOOP_DIR + name);
				} else {
					FileUtils.writeStringToFile(new File(destDir + "/" + name), chunk.toString(), StandardCharsets.UTF_8);
				}
				result.add(name);
				chunk = new StringBuilder();
			}
		}

		if (chunk.length() > 0) {
			String name = fileName + "_" + fileIdx + ".txt";
			FileUtils.writeStringToFile(new File(destDir + "/" + name), chunk.toString(), StandardCharsets.UTF_8);
			result.add(name);
		}
		return result;
	}
	
	private String getString(InputStream in) {
		String str = "";
		try {
			BufferedInputStream bis = new BufferedInputStream(in);
			CharsetDetector cd = new CharsetDetector();
			cd.setText(bis);
			CharsetMatch cm = cd.detect();
			if (cm != null) {
				Reader reader = cm.getReader();
				str = IOUtils.toString(reader);
			} else {
				str = IOUtils.toString(in, StandardCharsets.UTF_8.name());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return str;
	}
}
