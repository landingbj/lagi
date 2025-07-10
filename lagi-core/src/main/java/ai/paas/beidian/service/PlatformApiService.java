package ai.paas.beidian.service;


import ai.common.exception.RRException;
import ai.paas.beidian.pojo.InferenceServiceItem;
import ai.paas.beidian.pojo.InferenceUpdate;
import ai.paas.beidian.pojo.PoliciesItem;
import ai.paas.beidian.pojo.request.*;
import ai.paas.beidian.pojo.response.*;
import ai.paas.beidian.utils.BaseHttpRequestUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformApiService {

    private final Map<String, String> headers;
    private final String baseUrl;
    private final Gson gson = new Gson();

    public PlatformApiService(String baseUrl, String traceId, String spaceId, String authorization, String acceptLanguage) {
        this.baseUrl = baseUrl;
        this.headers = BaseHttpRequestUtil.toHeader(traceId, spaceId, authorization, acceptLanguage);
    }

    // auth
    public String PASSWORD_EDIT = "/user/password/edit";
    public String CLOUD_REGISTER = "/cloud/register";
    public String PASSWORD_LOGIN = "/cloud/passwordLogin";


    public Response<RegisterData> register(RegisterRequest request){
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + CLOUD_REGISTER, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<RegisterData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<Object> passwordEdit(PasswordEditRequest request){
        try {
            String post = BaseHttpRequestUtil.put(baseUrl + PASSWORD_EDIT, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<RegisterData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<PasswordLoginData> passwordLogin(PasswordLoginRequest request){
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + PASSWORD_LOGIN, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<PasswordLoginData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    // image
    public String IMAGE_REPOSITORY_LIST = "/user/imageRepository/list";
    public String IMAGE_REPOSITORY_INFO_UPDATE = "/user/imageRepository/info/update/{imageId}";
    public String IMAGE_REPOSITORY_DETAIL_INFO = "/user/imageRepository/detailInfo/{imageId}";
    public String IMAGE_REPOSITORY_SAVE = "/user/imageRepository/image/save/{jobenvId}";

    public String ADD_IMAGE_BUILD = "/user/image/addImageBuild";
    public String GET_IMAGE_BUILD_INFO_LIST = "/user/image/getImageBuildInfoList";
    public String GET_IMAGE = "/user/image/getImage";
    public String GET_IMAGE_INSTALL_PKG = "/user/image/getImageInstalledPkg";
    public String GET_IMAGE_BUILD_LOG = "/user/image/getImageBuildLog";
    public String IMAGE_INTELLIGENT_GEN = "/user/image/intelligentGen";
    public String DELETE_IMAGE_BUILD_INFO = "/user/image/deleteImageBuildInfo";
    public String UPDATE_IMAGE_BASE_INFO = "/user/image/updateImageBaseInfo";


    public Response<ImageRepositoryListData> getImageRepositoryList(Integer accessType, String keyWords, List<String> labelElementIds, Integer pageNum,
                                                                Integer pageSize, Integer isOfficial, Integer reduce, Integer repositoryType){
        try {
            Map<String, String> query = new HashMap<>();
            query.put("accessType", accessType.toString());
            query.put("keyWords", keyWords);
            query.put("labelElementIds", String.join(",", labelElementIds));
            query.put("pageNum", pageNum.toString());
            query.put("pageSize", pageSize.toString());
            query.put("isOfficial", isOfficial.toString());
            query.put("reduce", reduce.toString());
            query.put("repositoryType", repositoryType.toString());
            String get = BaseHttpRequestUtil.get(baseUrl + IMAGE_REPOSITORY_LIST, query, headers);
            return gson.fromJson(get, new TypeToken<Response<ImageRepositoryListData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<Object> updateImageRepositoryInfo(String imageId, ImageRepositoryInfoUpdateRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("imageId", imageId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(IMAGE_REPOSITORY_INFO_UPDATE, pathParams);
            String post = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<ImageRepositoryData> getImageDetailInfo(String imageId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("imageId", imageId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(IMAGE_REPOSITORY_DETAIL_INFO, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<ImageRepositoryData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<Object> saveImageRepository(String jobenvId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobenvId", jobenvId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(IMAGE_REPOSITORY_SAVE, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }


    public Response<Object> addImageBuild(ImageBuildRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + ADD_IMAGE_BUILD, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }


    public Response<ImageBuildInfoListData> getImageBuildInfoList(Integer pageNum, Integer pageSize, Integer status, String name) {
        try {
            Map<String, String> query = new HashMap<>();
            query.put("pageNum", pageNum.toString());
            query.put("pageSize", pageSize.toString());
            query.put("status", status.toString());
            query.put("name", name);
            String post = BaseHttpRequestUtil.get(baseUrl + GET_IMAGE_BUILD_INFO_LIST, query, headers);
            return gson.fromJson(post, new TypeToken<Response<ImageBuildInfoListData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }


    public Response<ImageInfoData> getImage(Integer imageId, Integer jobEnvId, Integer fromImageId) {
        try {
            Map<String, String> query = new HashMap<>();
            query.put("imageId", imageId.toString());
            query.put("jobEnvId", jobEnvId.toString());
            query.put("fromImageId", fromImageId.toString());
            String post = BaseHttpRequestUtil.get(baseUrl + GET_IMAGE, query, headers);
            return gson.fromJson(post, new TypeToken<Response<ImageInfoData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<ImageInstallPkgData> getImageInstallPkg(Integer imageId, Integer jobEnvId, Integer fromImageId, String jobId) {

        try {
            Map<String, String> query = new HashMap<>();
            query.put("imageId", imageId.toString());
            query.put("jobEnvId", jobEnvId.toString());
            query.put("fromImageId", fromImageId.toString());
            query.put("jobId", jobId);
            String post = BaseHttpRequestUtil.get(baseUrl + GET_IMAGE_INSTALL_PKG, query, headers);
            return gson.fromJson(post, new TypeToken<Response<ImageInstallPkgData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<ImageBuildLog> getImageBuildLog(Integer imageId) {
        try {
            Map<String, String> query = new HashMap<>();
            query.put("imageId", imageId.toString());
            String post = BaseHttpRequestUtil.get(baseUrl + GET_IMAGE_BUILD_LOG, query, headers);
            return gson.fromJson(post, new TypeToken<Response<ImageBuildLog>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<IntelligentGenData> intelligentGen(IntelligentGenRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + IMAGE_INTELLIGENT_GEN, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<IntelligentGenData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<Object> deleteImageBuildInfo(DeleteImageBuildInfoRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + DELETE_IMAGE_BUILD_INFO, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public Response<Object> updateImageBaseInfo(UpdateImageBaseInfoRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + UPDATE_IMAGE_BASE_INFO, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    // 模型
    public String MODEL_DETAIL = "/user/model/detail/{modelId}";

    public Response<ModelInfoData> modelDetail(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DETAIL, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<ModelInfoData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_LIST = "/user/model/list";

    public Response<Object> modelList(Integer accessType, String keyWords, List<String> labelElementIds,
                                      Integer pageNum, Integer pageSize, String sortOrder, String sortField,
                                      Integer reduce) {
        try {
            Map<String, String> query =new HashMap<>();
            query.put("accessType", accessType.toString());
            query.put("keyWords", keyWords);
            query.put("labelElementIds", String.join(",", labelElementIds));
            query.put("pageNum", pageNum.toString());
            query.put("pageSize", pageSize.toString());
            query.put("sortOrder", sortOrder);
            query.put("sortField", sortField);
            query.put("reduce", reduce.toString());
            String get = BaseHttpRequestUtil.get(baseUrl + MODEL_LIST, query, headers);
            return gson.fromJson(get, new TypeToken<Response<ModelInfoData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_VERSION_LIST = "/user/model/listVersions/{modelId}";

    public Response<ModelListVersionsData> modelListVersions(String modelId, String reduce) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_VERSION_LIST, pathParams);
            Map<String, String> query =new HashMap<>();
            pathParams.put("reduce", reduce);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, query, headers);
            return gson.fromJson(get, new TypeToken<Response<ModelListVersionsData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }


    public String MODEL_ADD = "/user/model/add";

    public Response<ModelAddData> modelAdd(ModelAddRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + MODEL_ADD, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<ModelAddData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_SAVE_DRAFT = "/user/model/saveDraft/{modelId}";

    public Response<Object> modelSaveDraft(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_SAVE_DRAFT, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_UPLOAD = "/user/model/upload/{modelId}";
    public Response<ModelUploadData> modelUpload(String modelId, ModelUploadRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_UPLOAD, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<ModelUploadData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }


    public String MODEL_DELETE_DRAFT = "/user/model/deleteDraft/{modelId}";

    public Response<Object> modelDeleteDraft(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DELETE_DRAFT, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, null, headers);
            return gson.fromJson(delete, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_UPDATE = "/user/model/update/{modelId}";
    public Response<Object> modelUpdate(String modelId, ModelUpdateRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_UPDATE, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_ADD_VERSION = "/user/model/addVersion/{modelId}";
    public Response<Object> modelAddVersion(String modelId, ModelAddVersionRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_ADD_VERSION, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }
    public String MODEL_UNZIP = "/user/model/unzip/{modelId}";
    public Response<Object> modelUnzip(String modelId, ModelUnzipRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_UNZIP, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_ADD_DRAFT = "/user/model/addDraft/{modelId}";
    public Response<Object> modelAddDraft(String modelId, ModelAddDraftRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_ADD_DRAFT, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e){
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_UNZIP_CANCEL = "/user/model/unzipCancel/{modelId}";
    public Response<Object> modelUnzipCancel(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_UNZIP_CANCEL, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_DELETE_VERSION = "/user/model/deleteVersion/{modelId}";
    public Response<Object> modelDeleteVersion(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DELETE_VERSION, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, null, headers);
            return gson.fromJson(delete, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_CHANGE_ACCESS = "/user/model/changeAccess/{modelId}";

    public Response<Object> modelChangeAccess(String modelId, ModelChangeAccessRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_CHANGE_ACCESS, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MOREL_RENAME = "/user/model/rename/{modelId}";
    public Response<Object> modelRename(String modelId, ModelRenameRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MOREL_RENAME, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_DELETE = "/user/model/delete/{modelId}";
    public Response<Object> modelDelete(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DELETE, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, null, headers);
            return gson.fromJson(delete, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_SFT_CONFIRM = "/user/model/upload/sftpConfirm/{channelId}";

    public ModelSftConfirmResponse modelSftConfirm(String channelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("channelId", channelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_SFT_CONFIRM, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(put, ModelSftConfirmResponse.class);
        } catch (Exception e){
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_FILE_LIST = "/user/model/fileList/{modelId}";
    public Response<ModelFileListData> modelFileList(String modelId, String filePath, String versionId,
                                                         Integer pageNum, Integer pageSize) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            Map<String, String> queryParams =new HashMap<>();
            queryParams.put("filePath", filePath);
            queryParams.put("versionId", versionId);
            queryParams.put("pageNum", pageNum.toString());
            queryParams.put("pageSize", pageSize.toString());
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_FILE_LIST, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, queryParams, headers);
            return gson.fromJson(get, new TypeToken<Response<ModelFileListData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_SFT_CANCEL = "/user/model/upload/sftpCancel/{channelId}";
    public Response<Object> modelSftCancel(String channelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("channelId", channelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_SFT_CANCEL, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_UPDATE_AND_GET_SFTP_INFO = "/user/model/update/getSftpInfo/{modelId}";
    public Response<SftpInfoData> modelUpdateAndGetSftpInfo(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_UPDATE_AND_GET_SFTP_INFO, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<SftpInfoData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_DOWNLOAD = "/user/model/download/full/getUrl/{modelId}";
    public Response<ModelUploadData> modelDownload(String modelId, String versionId, String filePath, String fileName) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            Map<String, String> queryParams =new HashMap<>();
            queryParams.put("versionId", versionId);
            queryParams.put("filePath", filePath);
            queryParams.put("fileName", fileName);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DOWNLOAD, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, queryParams, headers);
            return gson.fromJson(get, new TypeToken<Response<ModelUploadData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_UPDATE_AND_GET_SFTP_UP_INFO = "/user/model/update/getSftpInfoUp/{modelId}";
    public Response<SftpInfoData> modelUpdateAndGetSftpUpInfo(String modelId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_UPDATE_AND_GET_SFTP_UP_INFO, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<SftpInfoData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_DOWNLOAD_FILE = "/user/model/download/file/getUrl/{modelId}";
    public Response<ModelUploadData> modelDownloadFile(String modelId, String versionId, String filePath, String fileName) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            Map<String, String> queryParams =new HashMap<>();
            queryParams.put("versionId", versionId);
            queryParams.put("filePath", filePath);
            queryParams.put("fileName", fileName);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DOWNLOAD_FILE, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, queryParams, headers);
            return gson.fromJson(get, new TypeToken<Response<ModelUploadData>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_DELETE_FILE = "/user/model/deleteFile/{modelId}";
    public Response<ModelUploadData> modelDeleteFile(String modelId, String filePath, String fileName) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            Map<String, String> queryParams =new HashMap<>();
            queryParams.put("filePath", filePath);
            queryParams.put("fileName", fileName);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DELETE_FILE, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, queryParams, headers);
            return gson.fromJson(delete, new TypeToken<Response<ModelUploadData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_DELETE_DIR = "/user/model/deleteDir/{modelId}";
    public Response<ModelUploadData> modelDeleteDir(String modelId, String filePath) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            Map<String, String> queryParams =new HashMap<>();
            queryParams.put("filePath", filePath);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_DELETE_DIR, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, queryParams, headers);
            return gson.fromJson(delete, new TypeToken<Response<ModelUploadData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_RENAME_FILE = "/user/model/renameFile/{modelId}";
    public Response<Object> modelRenameFile(String modelId, String filePath, String fileName, String newFileName) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            Map<String, String> queryParams =new HashMap<>();
            queryParams.put("filePath", filePath);
            queryParams.put("fileName", fileName);
            queryParams.put("newFileName", newFileName);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_RENAME_FILE, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, queryParams, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String MODEL_CREATE_DIR = "/user/model/createDir/{modelId}";
    public Response<Object> modelCreateDir(String modelId, ModelDirRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("modelId", modelId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(MODEL_CREATE_DIR, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_EMERGENCY = "/user/job/emergency/{jobId}";

    public Response<Object> jobEmergency(String jobId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_EMERGENCY, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(Maps.newHashMap()));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }


    public String JOB_UPDATE = "/user/job/update/{jobId}";

    public Response<Object> jobUpdate(String jobId, JobUpdateRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_UPDATE, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_DELETE = "/user/job/delete/{jobId}";
    public Response<Object> jobDelete(String jobId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_DELETE, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, null, headers);
            return gson.fromJson(delete, new TypeToken<Response<Object>>(){}.getType());
        }catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_CLONE = "/user/job/clone/{jobId}";
    public Response<JobData> jobClone(String jobId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_CLONE, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<JobData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_STATUS = "/user/job/status/{jobId}";

    public Response<JobStatusData> jobStatus(String jobId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_STATUS, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<JobStatusData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_DETAIL = "/user/job/detail/{jobId}";
    public Response<JobDetailData> jobDetail(String jobId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_DETAIL, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<JobDetailData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_LIST = "/user/job/list";

    public Response<Map<String, Object>> jobList(String source, String keyWords,String jobStatus,Integer pageNum, Integer pageSize) {
        try {
            Map<String, String> queryParams =new HashMap<>();
            queryParams.put("source", source);
            queryParams.put("keyWords", keyWords);
            queryParams.put("jobStatus", jobStatus);
            queryParams.put("pageNum", pageNum.toString());
            queryParams.put("pageSize", pageSize.toString());
            String get = BaseHttpRequestUtil.get(baseUrl + JOB_LIST, queryParams, headers);
            return gson.fromJson(get, new TypeToken<Response<Map<String, Object>>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_RESOURCE_STOCK = "/user/job/jobResourceStock";
    public Response<JobResourceStockData> jobResourceStock(JobResourceStockRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + JOB_RESOURCE_STOCK, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<JobResourceStockData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_NEW = "/user/job/new";

    public Response<Object> jobNew(JobNewRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + JOB_NEW, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_UPDATE_MAX_RUN_HOUR = "/user/job/updateMaxRunHour/{jobId}";
    public Response<Object> jobUpdateMaxRunHour(String jobId, JobUpdateMaxRunHourRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_UPDATE_MAX_RUN_HOUR, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String JOB_CANCEL = "/user/job/cancel/{jobId}";
    public Response<Object> jobCancel(String jobId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("jobId", jobId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(JOB_CANCEL, pathParams);
            String put = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, null);
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }


    public String INFERENCE_UPGRADE = "/user/inference/upgrade/{inferenceId}";

    // TODO 2025/7/10 接口请求 示例和 Schema 不一致
    public Response<InferenceUpgradeData> inferenceUpgrade(String inferenceId, InferenceUpgradeRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_UPGRADE, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<InferenceUpgradeData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_DELETE = "/user/inference/{inferenceId}/services/{serviceId}";

    public Response<Object> inferenceDelete(String inferenceId, String serviceId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            pathParams.put("serviceId", serviceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_DELETE, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, null, headers);
            return gson.fromJson(delete, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_UPDATE = "/user/inference/{inferenceId}/services/{serviceId}";
    public Response<InferenceUpdate> inferenceUpdate(String inferenceId, String serviceId, InferenceUpdate request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            pathParams.put("serviceId", serviceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_UPDATE, pathParams);
            String patch = BaseHttpRequestUtil.patch(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(patch, new TypeToken<Response<InferenceUpdate>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_GET_POLICIES = "/user/inference/{inferenceId}/policies";

    public Response<InferencePoliciesData> inferenceGetPolicies(String inferenceId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_GET_POLICIES, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<InferencePoliciesData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_NEW_POLICIES = "/user/inference/{inferenceId}/policies";
    public Response<PoliciesItem> inferenceNewPolicies(String inferenceId, PoliciesItem request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_NEW_POLICIES, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<PoliciesItem>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_UPDATE_POLICIES = "/user/inference/{inferenceId}/policies/{policyId}";

    public Response<PoliciesItem> inferenceUpdatePolicies(String inferenceId, String policyId, PoliciesItem request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            pathParams.put("policyId", policyId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_UPDATE_POLICIES, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<PoliciesItem>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_DELETE_POLICIES = "/user/inference/{inferenceId}/policies/{policyId}";
    public Response<Object> inferenceDeletePolicies(String inferenceId, String policyId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            pathParams.put("policyId", policyId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_DELETE_POLICIES, pathParams);
            String delete = BaseHttpRequestUtil.delete(baseUrl + formatUrl, null, headers);
            return gson.fromJson(delete, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_UPGRADE_CHECK = "/user/inference/upgradeCheck/{inferenceId}";
    public Response<InferenceUpgradeCheckData> inferenceUpgradeCheck(String inferenceId, InferenceUpgradeRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_UPGRADE_CHECK, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<InferenceUpgradeCheckData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_UPDATE_INFO = "/user/inference/update/{inferenceId}";
    public Response<Object> inferenceUpdateInfo(String inferenceId, InferenceUpdateInfoRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_UPDATE_INFO, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_CANCEL = "/user/inference/cancel/{inferenceId}";
    public Response<Object> inferenceCancel(String inferenceId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_CANCEL, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, "{}");
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_START = "/user/inference/start/{inferenceId}";
    public Response<Object> inferenceStart(String inferenceId, InferenceUpgradeRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_START, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_DETAIL = "/user/inference/detail/{inferenceId}";
    public Response<InferenceDetailData> inferenceDetail(String inferenceId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_DETAIL, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<InferenceDetailData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_LIST_VERSIONS = "/user/inference/listVersions/{inferenceId}";

    public Response<List<InferenceVersionData>> inferenceListVersions(String inferenceId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_LIST_VERSIONS, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<List<InferenceVersionData>>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_UPDATE_REPLICAS = "/user/inference/updateReplicas/{inferenceId}";
    public Response<Object> inferenceUpdateReplicas(String inferenceId, InferenceUpdateReplicasRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_UPDATE_REPLICAS, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_DETAILS = "/user/inference/updateDesc/{inferenceId}";

    public Response<Object> inferenceDetails(String inferenceId, InferenceUpdateDescRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_DETAILS, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }
    public String INFERENCE_ROLLBACK = "/user/inference/rollback/{inferenceId}";
    public Response<Object> inferenceRollback(String inferenceId, InferenceRollbackRequest request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_ROLLBACK, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<Object>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_SERVICES_LIST = "/user/inference/{inferenceId}/services";
    public Response<InferenceServiceListData> inferenceServicesList(String inferenceId) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_SERVICES_LIST, pathParams);
            String get = BaseHttpRequestUtil.get(baseUrl + formatUrl, null, headers);
            return gson.fromJson(get, new TypeToken<Response<InferenceServiceListData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_SERVICES_ADD = "/user/inference/{inferenceId}/services";
    public Response<InferenceServiceItem> inferenceServicesAdd(String inferenceId, InferenceServiceListData  request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_SERVICES_ADD, pathParams);
            String post = BaseHttpRequestUtil.post(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<InferenceServiceItem>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_SERVICES_UPDATE = "/user/inference/{inferenceId}/services";

    public Response<InferenceServiceItem> inferenceServicesUpdate(String inferenceId, InferenceServiceListData request) {
        try {
            Map<String, String> pathParams =new HashMap<>();
            pathParams.put("inferenceId", inferenceId);
            String formatUrl = BaseHttpRequestUtil.pathUrl(INFERENCE_SERVICES_UPDATE, pathParams);
            String put = BaseHttpRequestUtil.put(baseUrl + formatUrl, null, headers, gson.toJson(request));
            return gson.fromJson(put, new TypeToken<Response<InferenceServiceItem>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }

    public String INFERENCE_NEW = "/user/inference/new";

    public Response<InferenceNewData> inferenceNew(InferenceUpgradeRequest request) {
        try {
            String post = BaseHttpRequestUtil.post(baseUrl + INFERENCE_NEW, null, headers, gson.toJson(request));
            return gson.fromJson(post, new TypeToken<Response<InferenceNewData>>(){}.getType());
        } catch (Exception e) {
            throw new RRException(500, e.getMessage());
        }
    }




    public static void main(String[] args) {
        String path = "/user/imageRepository/info/update/{imageId}";
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("imageId", "1");
        String format = StrUtil.format(path, stringStringHashMap);
        System.out.println( format);
    }



}
