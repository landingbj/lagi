let queryLock = false;
var PromptDialog = 0;
// 绑定页面回车事件
$(document).keyup(function (event) {
    if (event.keyCode == 13) {
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

// 判断是否生成指令集

function getTextResult(question, robootAnswerJq, conversation) {

    var category = window.category;
    var result = '';
    var paras = {
        "category": window.category,
        // "category": "dgmeta",
        "messages": CONVERSATION_CONTEXT.concat([
            {"role": "user", "content": question}
        ]),
        "channelId": channelId
        // 这里最后不能写死
    };

    console.log("paras", paras);
    var queryUrl = "search/questionAnswer";
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: queryUrl,
        data: JSON.stringify(paras),
        success: function (res) {
            let answer = '';
            if (res != null && res.status == "success") {
                // 判断文生图
                if (res.result != undefined) {
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
                else if(res.instructions!=null){
                    var instructions = JSON.stringify(res.instructions, null, 2);
                    result = syntaxHighlight(instructions);
                    robootAnswerJq.html("<pre>" + result + "</pre>");
                    answer = result;
                }
                // 判断图生文
                else if(res.samUrl != null){
                    var result = "您所上传的图片的意思是：<br><b>类别</b>：" + res.classification + "<br><b>描述</b>：" + res.caption + "<br>" +
                    		"<b>分割后的图片</b>：  <img src='" + res.samUrl + "' alt='Image'><br>";
                    robootAnswerJq.html(result);
                    let p = robootAnswerJq.parent().parent().parent();
                    p.children('.idx').children('.appendVoice').children('audio').hide();
                    p.children('.idx').children('.appendVoice').children('select').hide();
                    answer = result;
                }
                
                else if(res.enhanceImageUrl != null){
                    var result = "加强后的图片如下：<br>" + "<img src='" + res.enhanceImageUrl + "' alt='Image'><br>";
                    robootAnswerJq.html(result);
                    answer = result;
                }
                
                else if(res.svdVideoUrl != null){
                    var result = "<video id='media' src='" + res.svdVideoUrl + "' controls width='400px' heigt='400px'></video>";
                    robootAnswerJq.html(result);
                    answer = result;
                }
                
                else if(res.type != null && res.type === 'mot'){
                    var result = "<video id='media' src='" + res.data + "' controls width='400px' heigt='400px'></video>";
                    robootAnswerJq.html(result);
                    answer = result;
                }
                
                else if(res.type != null && res.type === 'mmediting'){
                    var result = "<video id='media' src='" + res.data + "' controls width='400px' heigt='400px'></video>";
                    robootAnswerJq.html(result);
                    answer = result;
                }
                
                // 图文混排,普通的对话
                else {
                    var json = res.data[0];
                    var a = `
                    <a style="color: #666;text-decoration: none;" href="uploadFile/downloadFile?filePath=${json.filepath}&fileName=${json.filename}">${json.filename}</a>
                    `
                    // console.log("图片为"+json.imageList);
                    var t = json.text;
                    t = t.replaceAll("\n", "<br>");
                    result = `
                            ${t} <br>
                            ${json.imageList != undefined ? `<img src='${json.imageList[0]}' alt='Image'>` : ""}
                            ${json.filename != undefined ? `附件:${a}` : ""}<br>
                            
                    `
                    console.log("result", result);
                    
                    CONVERSATION_CONTEXT.push({"role": "user", "content": question});
                    CONVERSATION_CONTEXT.push({"role": "assistant", "content": json.text});
                    
                    answer = result;

                    txtTovoice(json.text, "default");

                    // answer=result;
                    // // answer+= voice;
                    robootAnswerJq.html(answer);
                    answer = result;
                    // 替换掉图片等标签
                    // answer =answer.replace(/<img[^>]*>/g, "替换的内容");


                    enableQueryBtn();
                    querying = false;
                }
            } else {
//                alert("调用失败");
            	robootAnswerJq.html("调用失败！");
                answer = '调用失败! ';
            }
            $('#queryBox textarea').val('');
            queryLock = false;
            conversation.robot.answer = answer;
            addConv(conversation);
        },
        error: function () {
//            alert("返回失败");
            $('#queryBox textarea').val('');
            queryLock = false;
            robootAnswerJq.html("调用失败！");
            conversation.robot.answer = "调用失败！";
            addConv(conversation);
        }

    });
    return result;
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

// function (filename){
//     if(filename==undefined){
//         return ""
//     }
//     else{

//     }
// }