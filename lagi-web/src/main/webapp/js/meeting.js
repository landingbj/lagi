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

function addMeetingMinutes(question){
    let questionHtml = '<div>' + question + '</div>';
    addUserDialog(questionHtml);
    //addMeetingMinutesPrompt(question);
        var MeetingMinutesparas = {
        "rag": rag,
        "category": window.category,
        "messages": CONVERSATION_CONTEXT.concat([
            {"role": "user", "content": question}
        ]),
        "temperature": 0.8,
        "max_tokens": 1024,
        "stream": true
    };

    MeetingMinutesStream(MeetingMinutesparas);

}

  async function MeetingMinutesStream(paras) {
        const response = await fetch('/chat/meetingMinutes', {
            method: "POST",
            cache: "no-cache",
            keepalive: true,
            headers: {
                "Content-Type": "application/json",
                "Accept": "text/event-stream",
            },
            body: JSON.stringify(paras),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const reader = response.body.getReader();

        let fullText = '';
        let flag = true;
        let lastChunkPart = '';

        while (flag) {
            const {value, done} = await reader.read();
            let res =  new TextDecoder().decode(value);
            if(res.startsWith("error:")) {
                robootAnswerJq.html(res.replaceAll('error:', ''));
                return;
            }
            let chunkStr = lastChunkPart + new TextDecoder().decode(value).replaceAll('data: ', '').trim();
            const chunkArray = chunkStr.split("\n\n");

            let lastChunk = chunkArray[chunkArray.length - 1];
            if (!isJsonString(chunkArray[chunkArray.length - 1]) && lastChunk !== "[DONE]" ) {
                lastChunkPart = chunkArray.pop();
            } else {
                lastChunkPart = '';
            }

            for (let i = 0; i < chunkArray.length; i++) {
                let chunk = chunkArray[i];
                if (chunk === "[DONE]") {
                    CONVERSATION_CONTEXT.push({"role": "user", "content": question});
                    CONVERSATION_CONTEXT.push({"role": "assistant", "content": fullText});
                    flag = false;
                    break;
                }

                let json = JSON.parse(chunk);
                if (json.choices === undefined) {
                    queryLock = false;
                    robootAnswerJq.html("调用失败！");
                    break
                }
                if (json.choices.length === 0) {
                    continue;
                }
                let chatMessage = json.choices[0].message;
                let a = '';
                if (chatMessage.filename === undefined){

                }else{
                    //var a = '<ul style="list-style:none;padding-left:5px;">';
                    a = '';
                    let isFirst = true; // 标记是否是第一个文件名

                    for (let i = 0; i < chatMessage.filename.length; i++) {
                        let marginLeft = isFirst ? '0' : '50px';
                        a += `<a class="filename" style="list-style:none;color: #666;text-decoration: none;display: inline-block; " href="uploadFile/downloadFile?filePath=${chatMessage.filepath[i]}&fileName=${chatMessage.filename[i]}">${chatMessage.filename[i]}</a>`;
                        isFirst = false;
                        //console.log("这里的路径是："+chatMessage.filepath[i]);
                        //a+=`<a style="color: #666;text-decoration: none;" href="uploadFile/downloadFile?filePath=${chatMessage.filepath[i]}&fileName=${chatMessage.filename[i]}">${chatMessage.filename[i]}</a><br>`;
                    }
                    //a +='</ul>'
                }

                if (chatMessage.content === undefined) {
                    continue;
                }
                let t = chatMessage.content;
                t = t.replaceAll("\n", "<br>");
                fullText += t;
                result = `
                        ${fullText} <br>
                        ${chatMessage.imageList !== undefined && chatMessage.imageList.length > 0 ? `<img src='${chatMessage.imageList[0]}' alt='Image'>` : ""}
                        ${chatMessage.filename !== undefined ? `<div style="display: flex;"><div style="width:50px;flex:1">附件:</div><div style="width:600px;flex:17 padding-left:5px">${a}</div></div>` : ""}<br>
                        ${chatMessage.context || chatMessage.contextChunkIds ?  `<div class="context-box"><div class="loading-box">正在索引文档&nbsp;&nbsp;<span></span></div><a style="float: right; cursor: pointer; color:cornflowerblue" onClick="retry(${CONVERSATION_CONTEXT.length + 1})">更多通用回答</a></div>` : ""}<br>`
                        // `;
                if(chatMessage.contextChunkIds) {
                    if(chatMessage.contextChunkIds instanceof Array) {
                        // filterChunk(chatMessage.filename, chatMessage.filepath, chatMessage.contextChunkIds, fullText, robootAnswerJq);
                        // result += `<div class="loading-box">正在索引文档&nbsp;&nbsp;<span></span></div><br>`;
                        getCropRect(chatMessage.contextChunkIds, fullText, robootAnswerJq);
                    }
                }
                robootAnswerJq.html(result);
            }
        }
    }

     MeetingMinutesStream(paras).then(r => {
        let lastAnswer = CONVERSATION_CONTEXT[CONVERSATION_CONTEXT.length - 1]["content"]
        txtTovoice(lastAnswer, "default");
        enableQueryBtn();
        querying = false;
    }).catch((err) => {
        console.error(err);
        enableQueryBtn();
        querying = false;
        queryLock = false;
        robootAnswerJq.html("调用失败！");
    });
function addMeetingMinutesPrompt(question){
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "/chat/meetingMinutes",
        data: JSON.stringify(question),
        success: function (res) {
            if (res === null || res.status === "failed") {
                robootAnswerJq.html("调用失败！");
                return;
            }
            var json = res.data[0];
            var a = `
                    <a style="color: #666;text-decoration: none;" href="uploadFile/downloadFile?filePath=${json.filepath}&fileName=${json.filename}">${json.filename}</a>
                    `
            var t = json.text;
            t = t.replaceAll("\n", "<br>");
            result = `
                            ${t} <br>
                            ${json.imageList != undefined ? `<img src='${json.imageList[0]}' alt='Image'>` : ""}
                            ${json.filename != undefined ? `附件:${a}` : ""}<br>
                    `
            CONVERSATION_CONTEXT.push({"role": "user", "content": question});
            CONVERSATION_CONTEXT.push({"role": "assistant", "content": json.text});

            txtTovoice(json.text, "default");
            robootAnswerJq.html(result);
            enableQueryBtn();
            querying = false;
        }
    });
}