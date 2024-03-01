package ai.migrate.pojo;

import java.util.Date;

public class TermWord {
	private String id; // 名词ID
	private String databaseId; // 数据库ID
	private String bookId; // 书ID，0表示不存在
	private String subjectId; // 学科ID
	private String orderNumber; // 序号
	private String cn; // 规范用词
	private String en; // 英文JSON数组[]
	private String tw; // 台湾名
	private String fullName; // 全称JSONfull_name
	private String shortName; // 简称JSON short_name
	private String onceName; // 曾称once_name
	private String alsoName; // 又称 also_name
	private String commName; // 俗称comm_name
	private String otherLanguage; // 其他语言other_language
	private String bbh; // 版本号version
	private Integer publishYear; // 公布时间年publish_year
	private String definition; // 定义definition
	private String py; // 拼音py
	private String pyShort; // 拼音简拼py_short
	private String source; // 来源source
	private Integer pno; // 页码
	private Integer hasExt; // 是否有延伸阅读
	private Integer version; // 版本号预留
	private Integer status; // 名词状态（0表示启用，1表示禁用）
	private String crtUser; // 录入人员
	private Date crtTime; // 录入时间
	private String updUser; // 更新人员
	private Date updTime; // 更新时间
	private String subjectNum; // 学科代码
	private String subjectName; // 学科名称
	private String xh1; // 序号预留
	private String oid; // 原ID
	private String enExt; // 英文延伸阅读
	private String qtExt; // 其他语言延伸阅读
	private Integer hasIns; // 是否有术语示例
	private String zcYl; // 曾称预留
	private String scYl; // 俗称预留
	private String videoPath; // 视频路径
	private String spzId; // 生僻字ID
	private String bz;
	private Boolean originalText; // 是否有原文
	private String subjectArea; // 领域id
	private String subjectPath; // 学科路径id
	private String areaName; // 领域名称

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(String databaseId) {
		this.databaseId = databaseId;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public String getTw() {
		return tw;
	}

	public void setTw(String tw) {
		this.tw = tw;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getOnceName() {
		return onceName;
	}

	public void setOnceName(String onceName) {
		this.onceName = onceName;
	}

	public String getAlsoName() {
		return alsoName;
	}

	public void setAlsoName(String alsoName) {
		this.alsoName = alsoName;
	}

	public String getCommName() {
		return commName;
	}

	public void setCommName(String commName) {
		this.commName = commName;
	}

	public String getOtherLanguage() {
		return otherLanguage;
	}

	public void setOtherLanguage(String otherLanguage) {
		this.otherLanguage = otherLanguage;
	}

	public String getBbh() {
		return bbh;
	}

	public void setBbh(String bbh) {
		this.bbh = bbh;
	}

	public Integer getPublishYear() {
		return publishYear;
	}

	public void setPublishYear(Integer publishYear) {
		this.publishYear = publishYear;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getPy() {
		return py;
	}

	public void setPy(String py) {
		this.py = py;
	}

	public String getPyShort() {
		return pyShort;
	}

	public void setPyShort(String pyShort) {
		this.pyShort = pyShort;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Integer getPno() {
		return pno;
	}

	public void setPno(Integer pno) {
		this.pno = pno;
	}

	public Integer getHasExt() {
		return hasExt;
	}

	public void setHasExt(Integer hasExt) {
		this.hasExt = hasExt;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCrtUser() {
		return crtUser;
	}

	public void setCrtUser(String crtUser) {
		this.crtUser = crtUser;
	}

	public Date getCrtTime() {
		return crtTime;
	}

	public void setCrtTime(Date crtTime) {
		this.crtTime = crtTime;
	}

	public String getUpdUser() {
		return updUser;
	}

	public void setUpdUser(String updUser) {
		this.updUser = updUser;
	}

	public Date getUpdTime() {
		return updTime;
	}

	public void setUpdTime(Date updTime) {
		this.updTime = updTime;
	}

	public String getSubjectNum() {
		return subjectNum;
	}

	public void setSubjectNum(String subjectNum) {
		this.subjectNum = subjectNum;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getXh1() {
		return xh1;
	}

	public void setXh1(String xh1) {
		this.xh1 = xh1;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getEnExt() {
		return enExt;
	}

	public void setEnExt(String enExt) {
		this.enExt = enExt;
	}

	public String getQtExt() {
		return qtExt;
	}

	public void setQtExt(String qtExt) {
		this.qtExt = qtExt;
	}

	public Integer getHasIns() {
		return hasIns;
	}

	public void setHasIns(Integer hasIns) {
		this.hasIns = hasIns;
	}

	public String getZcYl() {
		return zcYl;
	}

	public void setZcYl(String zcYl) {
		this.zcYl = zcYl;
	}

	public String getScYl() {
		return scYl;
	}

	public void setScYl(String scYl) {
		this.scYl = scYl;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public String getSpzId() {
		return spzId;
	}

	public void setSpzId(String spzId) {
		this.spzId = spzId;
	}

	public String getBz() {
		return bz;
	}

	public void setBz(String bz) {
		this.bz = bz;
	}

	public Boolean getOriginalText() {
		return originalText;
	}

	public void setOriginalText(Boolean originalText) {
		this.originalText = originalText;
	}

	public String getSubjectArea() {
		return subjectArea;
	}

	public void setSubjectArea(String subjectArea) {
		this.subjectArea = subjectArea;
	}

	public String getSubjectPath() {
		return subjectPath;
	}

	public void setSubjectPath(String subjectPath) {
		this.subjectPath = subjectPath;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	@Override
	public String toString() {
		return "TermWord [id=" + id + ", databaseId=" + databaseId
				+ ", bookId=" + bookId + ", subjectId=" + subjectId
				+ ", orderNumber=" + orderNumber + ", cn=" + cn + ", en=" + en
				+ ", tw=" + tw + ", fullName=" + fullName + ", shortName="
				+ shortName + ", onceName=" + onceName + ", alsoName="
				+ alsoName + ", commName=" + commName + ", otherLanguage="
				+ otherLanguage + ", bbh=" + bbh + ", publishYear="
				+ publishYear + ", definition=" + definition + ", py=" + py
				+ ", pyShort=" + pyShort + ", source=" + source + ", pno="
				+ pno + ", hasExt=" + hasExt + ", version=" + version
				+ ", status=" + status + ", crtUser=" + crtUser + ", crtTime="
				+ crtTime + ", updUser=" + updUser + ", updTime=" + updTime
				+ ", subjectNum=" + subjectNum + ", subjectName=" + subjectName
				+ ", xh1=" + xh1 + ", oid=" + oid + ", enExt=" + enExt
				+ ", qtExt=" + qtExt + ", hasIns=" + hasIns + ", zcYl=" + zcYl
				+ ", scYl=" + scYl + ", videoPath=" + videoPath + ", spzId="
				+ spzId + ", bz=" + bz + ", originalText=" + originalText
				+ ", subjectArea=" + subjectArea + ", subjectPath="
				+ subjectPath + ", areaName=" + areaName + "]";
	}
}
