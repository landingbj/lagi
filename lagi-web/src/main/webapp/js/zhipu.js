const EMPHASIS_QUESTION = '本篇文章的侧重点是什么？'
const FAIL_PROMPT = '操作失败'
const CHAPTER_DONE = 6

let currentFileList = [];

function uploadPaperFiles(files) {
    var formData = new FormData();

    for (var i = 0; i < files.length; i++) {
        console.log(files[i]);
        formData.append('files[]', files[i]);
    }

    addRobotDialog("文件正在上传...");

    $.ajax({
        url: '/v1/medicine/uploadTextFiles?category=' + window.category,
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function (response) {
            if (response.status === 'success') {
                setLastRobotAnswer("文件已经上传成功");
                currentFileList = response.data;
                addRobotDialog(EMPHASIS_QUESTION);
                unlockInput();
                enableQueryBtn();
            }
        },
        error: function (jqXHR, textStatus, errorMessage) {
            console.log('uploadFiles', errorMessage);
        }
    });


}

function generateEssay(input) {
    let question = getLastRobotAnswer();
    if (question.trim() !== EMPHASIS_QUESTION) {
        unlockInput();
        enableQueryBtn();
        querying = false;
        return;
    }
    console.log('generateEssay question', question);
    console.log('generateEssay input', input);
    addUserDialog(input);
    prepareForGenerateEssay(input, currentFileList);

    var statusInterval = null;

    function prepareForGenerateEssay(emphasize, uploadFileList) {
        var data = {'uploadFileList': uploadFileList, 'emphasize': emphasize};
        $.ajax({
            url: "/v1/medicine/prepareForGenerateEssay",
            type: "POST",
            contentType: "application/json;charset=utf-8",
            data: JSON.stringify(data),
            success: function (res) {
                if (res.status === 'success') {
                    addRobotDialog("开始撰写文章...");
                    statusInterval = setInterval(getGenerateEssayProgress, 1000, res.taskId);
                }
            },
            error: function (res) {
                setLastRobotAnswer(FAIL_PROMPT);
                unlockInput();
                enableQueryBtn();
            }
        });
    }

    function getGenerateEssayProgress(taskId) {
        var data = {'taskId': taskId};
        $.ajax({
            url: "/v1/medicine/getGenerateEssayProgress",
            type: "GET",
            contentType: "application/json;charset=utf-8",
            data: data,
            success: function (res) {
                if (res.status === 'success') {
                    let msg = res.data.message
                    if (res.data.status === CHAPTER_DONE) {
                        clearInterval(statusInterval);
                        addRobotDialog("以下是根据文献内容撰写的文章：");
                        generateEssay(taskId);
                    } else if (res.data.status > 0) {
                        let question = getLastRobotAnswer();
                        if (question.trim() === msg) {
                            return;
                        }
                        addRobotDialog(msg);
                    } else {
                        clearInterval(statusInterval);
                        addRobotDialog(msg);
                    }
                }
            },
            error: function (res) {
                setLastRobotAnswer(FAIL_PROMPT);
                unlockInput();
                enableQueryBtn();
            }
        });
    }


    function generateEssay(taskId) {
        let question = "";
        let conversation = {user: {question: question}, robot: {answer: ''}}
        let robotAnswerJq = newConversation(conversation, false, true);
        streamOutput({"taskId": taskId}, question, robotAnswerJq);
    }

    function streamOutput(paras, question, robootAnswerJq) {
        async function generateStream(paras) {
            const response = await fetch('/v1/medicine/generateEssay', {
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
                let res = new TextDecoder().decode(value);
                if (res.startsWith("error:")) {
                    robootAnswerJq.html(res.replaceAll('error:', ''));
                    return;
                }
                let chunkStr = new TextDecoder().decode(value).replaceAll('data: ', '').trim();
                const chunkArray = chunkStr.split("\n\n");
                for (let i = 0; i < chunkArray.length; i++) {
                    let chunk = chunkArray[i];
                    if (chunk === "[DONE]") {
                        CONVERSATION_CONTEXT.push({"role": "user", "content": question});
                        CONVERSATION_CONTEXT.push({"role": "assistant", "content": fullText});
                        flag = false;
                        result = `
                        ${fullText}
                        `
                        robootAnswerJq.html(result);
                        break;
                    }
                    var json = JSON.parse(chunk);
                    if (json.choices === undefined) {
                        queryLock = false;
                        robootAnswerJq.html("调用失败！");
                        break
                    }
                    if (json.choices.length === 0) {
                        continue;
                    }
                    var chatMessage = json.choices[0].message;
                    if (chatMessage.content === undefined) {
                        continue;
                    }
                    var t = chatMessage.content;
                    t = t.replaceAll("\n", "<br>");
                    fullText += t;
                    result = `
                        ${fullText}
                        `
                    robootAnswerJq.html(result + '<p style="display: inline-block"></p>');
                }
            }
        }

        generateStream(paras).then(r => {
            unlockInput();
            enableQueryBtn();
            querying = false;
            console.log("generateStream done");
        }).catch((err) => {
            console.error(err);
            unlockInput();
            enableQueryBtn();
            querying = false;
            if (!robootAnswerJq.text) {
                robootAnswerJq.html("调用失败！");
            }
        });
    }
}

