let currentMeetingInfo;
const meetingUserId = "a56fef1b48ab4afaa134ddda7ea774e";

function addMeetingConversation(question) {
    let questionHtml = '<div>' + question + '</div>';
    addUserDialog(questionHtml);
    addMeetingPrompt(question);
}

function addMeetingPrompt(message) {
    if (message === '确定') {
        let html = '<div>' + '好的，已帮您预定了：' + '</div></br>';
        html += '<div>预定人id：' + JSON.stringify(currentMeetingInfo.userId) + '</div></br>';
        html += '<div>会议地点：' + JSON.stringify(currentMeetingInfo.meetingAddress) + '</div></br>';
        html += '<div>会议开始日期：' + JSON.stringify(currentMeetingInfo.date) + '</div></br>';
        html += '<div>会议开始时间：' + JSON.stringify(currentMeetingInfo.startTime) + '</div></br>';
        html += '<div>会议人数：' + JSON.stringify(currentMeetingInfo.attendance) + '</div></br>';
        html += '<div>会议时长：' + JSON.stringify(currentMeetingInfo.duration) + '</div></br>';
        addRobotDialog(html);
        unlockInput();
        currentMeetingInfo = {};
        return;
    }
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "/chat/extractAddMeetingInfo",
        data: JSON.stringify({
            "message": message,
            "meetingInfo": currentMeetingInfo
        }),
        success: function (res) {
            if (res.meetingInfo) {
                currentMeetingInfo = res.meetingInfo;
                if (currentMeetingInfo === undefined) {
                    addRobotDialog('<div>您可以对我说：“帮我预定明天下午3点的东四会议室，会议时长为一个小时，会议有5个人。”' + '</br>' + '这样我就可以帮您预定会议了！！！</div>');
                    unlockInput();
                } else if (currentMeetingInfo.attendance === undefined || currentMeetingInfo.attendance <= 0) {
                    addRobotDialog('请问您的会议参会人数是多少呢？（您可以告诉我：会议有1人，会议有2人，会议有3人...）</br>');
                    unlockInput();
                } else if (currentMeetingInfo.meetingAddress === undefined) {
                    addRobotDialog('请问您的会议地点是哪里呢？（您可以告诉我：‘东四’、‘西单’、‘酒仙桥’、‘洋桥’、‘大郊亭’...）</br>');
                    unlockInput();
                } else if (currentMeetingInfo.date === undefined) {
                    addRobotDialog('请问您的会议是什么时候呢？（您可以告诉我：明天，后天，下周三...）</br>');
                    unlockInput();
                } else if (currentMeetingInfo.startTime === undefined) {
                    addRobotDialog('请问您的会议是几点呢？（您可以告诉我：明天上午10点...）</br>');
                    unlockInput();
                } else if (currentMeetingInfo.duration === undefined) {
                    addRobotDialog('请问您会议打算开多久呢？（会议时长不超过2小时，您可以告诉我：半个小时，一个小时，两个小时...）</br>');
                    unlockInput();
                } else if (currentMeetingInfo.duration > 120) {
                    addRobotDialog('抱歉，您预定的会议时间过长，会议时长不超过2小时（您可以告诉我：修改时长为半个小时，一个小时，两个小时...）</br>');
                    unlockInput();
                } else {
                    currentMeetingInfo.userId = meetingUserId;
                    let html = '<div>' + '您的会议信息如下：' + '</div></br>';
                    html += '<div>会议地点：' + JSON.stringify(currentMeetingInfo.meetingAddress) + '</div></br>';
                    html += '<div>会议开始日期：' + JSON.stringify(currentMeetingInfo.date) + '</div></br>';
                    html += '<div>会议开始时间：' + JSON.stringify(currentMeetingInfo.startTime) + '</div></br>';
                    html += '<div>会议时长：' + JSON.stringify(currentMeetingInfo.duration) + '</div></br>';
                    html += '<div>会议人数：' + JSON.stringify(currentMeetingInfo.attendance) + '</div></br>';
                    html += '<div>预定人：' + JSON.stringify(currentMeetingInfo.userId) + '</div></br>';
                    html += '<div>' + '您确认预定吗？（您可以告诉我：‘确定’ 或 ‘修改预定...’）' + '</div></br>';
                    addRobotDialog(html);
                    unlockInput();
                }
            } else {
                let prompt = '<div>您可以对我说：“帮我预定明天下午3点的东四会议室，会议时长为一个小时，会议有5个人。”' + '</br>' + '这样我就可以帮您预定会议了！！！</div>';
                addRobotDialog(prompt);
                unlockInput();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.error("Error occurred: ", textStatus, errorThrown);
            returnFailedResponse();
        }
    });
}