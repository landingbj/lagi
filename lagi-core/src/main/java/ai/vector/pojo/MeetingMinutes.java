package ai.vector.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class MeetingMinutes {
    private String category;
    private String level;

    private String id;                  // ID/fileId;
    private String number;              // 编号
    private String meetingCategory;     // 会议类别
    private String title;               // 标题
    private String drafter;             // 拟稿人
    private String meetingPlace;        // 会议地点
    private String hostingUnit;         // 会议承办单位
    private Date meetingTime;           // 会议时间
    private String phone;               // 电话
    private String confidentiality;     // 密级
    private String copyNumber;          // 份号
    private String distribution;        // 分送
    private Date issueDate;             // 印发日期
    private String urgency;             // 缓急
    private String content;             // 原文路径
    private String authorizer;          // 授权人
}
