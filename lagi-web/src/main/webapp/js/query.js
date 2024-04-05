let queryLock = false;
var PromptDialog = 0;
// 绑定页面回车事件
$('#queryContent').keydown(function (event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        textQuery();
    }
});

function textQuery() {
    if (queryLock) {
        alert("有对话正在进行请耐心等待");
        return;
    }
    queryLock = true;
    disableQueryBtn();
    let question = $('#queryBox textarea').val();
    if (isBlank(question)) {
        alert("请输入有效字符串！！！");
        $('#queryBox textarea').val('');
        enableQueryBtn();
        querying = false;
        queryLock = false;
        return;
    }

    // 隐藏非对话内容
    hideHelloContent();
    $('#queryBox textarea').val('');
    let conversation = {user: {question: question}, robot: {answer: ''}}
    let robootAnswerJq = newConversation(conversation);
    sleep(200).then(() => {
        getTextResult(question.trim(), robootAnswerJq, conversation);
    })
}

const CONVERSATION_CONTEXT = [];

function getTextResult(question, robootAnswerJq, conversation) {
    var result = '';
    var paras = {
        "category": window.category,
        "messages": CONVERSATION_CONTEXT.concat([
            {"role": "user", "content": question}
        ]),
        "temperature": 0.8,
        "max_tokens": 1024,
        "stream": true
    };

    var queryUrl = "search/detectIntent";
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: queryUrl,
        data: JSON.stringify(paras),
        success: function (res) {
            let answer = '';
            if (res != null && res.status === "success") {
                // 判断文生图
                if (res.result !== undefined) {
                    result = `
                        <img src='${res.result}' alt='Image' style="width: 320px;">
                    `
                    robootAnswerJq.html(result);
                    answer = result;
                    let p = robootAnswerJq.parent().parent().parent();
                    p.children('.idx').children('.appendVoice').children('audio').hide();
                    p.children('.idx').children('.appendVoice').children('select').hide();
                }
                // 判断生成指令集
                else if (res.instructions != null) {
                    var instructions = JSON.stringify(res.instructions, null, 2);
                    result = syntaxHighlight(instructions);
                    robootAnswerJq.html("<pre>" + result + "</pre>");
                    answer = result;
                }
                // 判断图生文
                else if (res.samUrl != null) {
                    result = "您所上传的图片的意思是：<br><b>类别</b>：" + res.classification + "<br><b>描述</b>：" + res.caption + "<br>" +
                        "<b>分割后的图片</b>：  <img src='" + res.samUrl + "' alt='Image'><br>";
                    robootAnswerJq.html(result);
                    let p = robootAnswerJq.parent().parent().parent();
                    p.children('.idx').children('.appendVoice').children('audio').hide();
                    p.children('.idx').children('.appendVoice').children('select').hide();
                    answer = result;
                } else if (res.enhanceImageUrl != null) {
                    result = "加强后的图片如下：<br>" + "<img src='" + res.enhanceImageUrl + "' alt='Image'><br>";
                    robootAnswerJq.html(result);
                    answer = result;
                } else if (res.svdVideoUrl != null) {
                    result = "<video id='media' src='" + res.svdVideoUrl + "' controls width='400px' height='400px'></video>";
                    robootAnswerJq.html(result);
                    answer = result;
                } else if (res.type != null && res.type === 'mot') {
                     result = "<video id='media' src='" + res.data + "' controls width='400px' height='400px'></video>";
                    robootAnswerJq.html(result);
                    answer = result;
                } else if (res.type != null && res.type === 'mmediting') {
                     result = "<video id='media' src='" + res.data + "' controls width='400px' height='400px'></video>";
                    robootAnswerJq.html(result);
                    answer = result;
                } else {
                    if (paras["stream"]) {
                        streamOutput(paras, question, robootAnswerJq);
                    } else {
                        generalOutput(paras, question, robootAnswerJq);
                    }
                }
            } else {
                robootAnswerJq.html("调用失败！");
                answer = '调用失败! ';
            }
            $('#queryBox textarea').val('');
            queryLock = false;
            conversation.robot.answer = answer;
            addConv(conversation);
        },
        error: function () {
            $('#queryBox textarea').val('');
            queryLock = false;
            robootAnswerJq.html("调用失败！");
            conversation.robot.answer = "调用失败！";
            addConv(conversation);
        }

    });
    return result;
}

function generalOutput(paras, question, robootAnswerJq) {
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "v1/chat/completions",
        data: JSON.stringify(paras),
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

function streamOutput(paras, question, robootAnswerJq) {
    async function generateStream(paras) {
        const response = await fetch('v1/chat/completions', {
            method: "POST",
            cache: "no-cache",
            keepalive: true,
            headers: {
                "Content-Type": "application/json",
                "Accept": "text/event-stream",
            },
            body: JSON.stringify(paras),
        });

        const reader = response.body.getReader();
        let fullText = '';
        let flag = true;
        while (flag) {
            const {value, done} = await reader.read();
            let chunkStr = new TextDecoder().decode(value).replaceAll('data: ', '').trim();
            const chunkArray = chunkStr.split("\n\n");
            for (let i = 0; i < chunkArray.length; i++) {
                let chunk = chunkArray[i];
                if (chunk === "[DONE]") {
                    CONVERSATION_CONTEXT.push({"role": "user", "content": question});
                    CONVERSATION_CONTEXT.push({"role": "assistant", "content": fullText});
                    flag = false;
                    break;
                }
                var json = JSON.parse(chunk);
                if (json.choices === undefined || json.choices.length === 0) {
                    queryLock = false;
                    robootAnswerJq.html("调用失败！");
                    break
                }
                var chatMessage = json.choices[0].message;
                var a = '<a style="color: #666;text-decoration: none;" ' +
                    'href="uploadFile/downloadFile?filePath=' + chatMessage.filepath + '&fileName=' +
                    chatMessage.filename + '">' + chatMessage.filename + '</a>';

                if (chatMessage.content === undefined) {
                    continue;
                }
                var t = chatMessage.content;
                t = t.replaceAll("\n", "<br>");
                fullText += t;
                result = `
                        ${fullText} <br>
                        ${chatMessage.imageList !== undefined && chatMessage.imageList.length > 0 ? `<img src='${chatMessage.imageList[0]}' alt='Image'>` : ""}
                        ${chatMessage.filename !== undefined ? `附件:${a}` : ""}<br>
                        `
                robootAnswerJq.html(result);
            }
        }
    }

    generateStream(paras).then(r => {
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
}


function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}
