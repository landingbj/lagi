window.category = "";

window.finger = null;



window.onload = function () {
    window.category = getCookie("category");
    var categoryTmp = window.category;
    if (categoryTmp === "" || categoryTmp === undefined) {
        getCategory("");
    } else {
        getCategory(categoryTmp);
    }

    initHelloPage();
    loadTheme();
    // showPromptNav();
    loadNavBar();
    Fingerprint2.get(function(components) {
        const values = components.map(function(component,index) {
            if (index === 0) { //把微信浏览器里UA的wifi或4G等网络替换成空,不然切换网络会ID不一样
                return component.value.replace(/\bNetType\/\w+\b/, '')
            }
            return component.value
        })
        // 生成最终id murmur
        // console.log(values)  //使用的浏览器信息
        const murmur = Fingerprint2.x64hash128(values.join(''), 31)
        // console.log(murmur) //生成的标识码
        window.finger = murmur;
    })
}




function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toGMTString();
    document.cookie = `${cname}=${cvalue}; ${expires}`;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i].trim();
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return "";
}

function getCategory(currentCategory) {
    $.ajax({
        type: "GET",
        url: "user/getRandomCategory?currentCategory=" + currentCategory,
        success: function (res) {
            var category = 'temp';
            if (res.status === 'success') {
                category = res.data.category;
                setCookie("category", category, 180);
            } else {
                setCookie("category", category, 1);
            }
            window.category = category;
        },
        error: function (res) {
            var category = 'temp';
            setCookie("category", category, 1);
            window.category = category;
        }
    });
}

function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0,
            v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}



// *******************************事件绑定 结束*****************************************************

// *******************************全局变量 开始*****************************************************
// chat 窗口索引
var chatIndex = -1;
var chatList = [];
// 是否正在查询
var querying = false;
// var theme = localStorage.getItem("theme") ? localStorage.getItem("theme") : "light" ;
var themes = ["light", "dark"];
var themeIndex = 0;
var themeHtmls = [
    `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round"
    stroke-linejoin="round" class="h-4 w-4" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
  </svg>
  Dark mode`,
    `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round"
    stroke-linejoin="round" class="h-4 w-4" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
    <circle cx="12" cy="12" r="5"></circle>
    <line x1="12" y1="1" x2="12" y2="3"></line>
    <line x1="12" y1="21" x2="12" y2="23"></line>
    <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
    <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
    <line x1="1" y1="12" x2="3" y2="12"></line>
    <line x1="21" y1="12" x2="23" y2="12"></line>
    <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
    <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
  </svg>
  Light mode`
]

var queryUrl = "search/questionAnswer";

var channelId = 437;


// *******************************全局变量 结束*****************************************************

// *******************************工具函数 开始*****************************************************

// *******************************工具函数 结束*****************************************************


function loadTheme() {
    themeIndex = localStorage.getItem("theme") == 1 ? 1 : 0;
    $('#themeChange').prop('data-theme', themeIndex);
    chooseTheme();
}

function changeTheme(el) {
    let that = $(el);
    let th = that.prop('data-theme');
    themeIndex = th == 0 ? 1 : 0;
    chooseTheme();
    // dom 层
    that.prop('data-theme', themeIndex);
    that.empty();
    that.append(themeHtmls[themeIndex]);
}


function chooseTheme() {
    var html = document.getElementsByTagName("html")[0]
    html.classList.remove("light", "dark");
    html.classList.add(themes[themeIndex]);
    html.style["color-scheme"] = themes[themeIndex];
    localStorage.setItem("theme", themeIndex);
}


// 查询
function query() {
    // 获取文档内容
    let queryString = $('#queryContent').val();
    $('#queryContent').val('');
    if (isBlank(queryString)) {
        return;
    }
    if (querying) {
        console.log("正在查询");
        return;
    }
    querying = true;
    // 执行访问远程访问，调用 qury 访问
    if (chatIndex == -1) {
        newChat();
    }
    // 修改按钮 内容为加载中
    disableQueryBtn();
    // 添加chat
    let answerJq = addOneChat({"question": queryString});
    // 测试
    sleep(200).then(() => {
        // 调用接口拿到结果
        let answer = getAnswer(queryString);
        // 跟新答案
        fillAnswer(answerJq, answer)
        enableQueryBtn();
        querying = false;
    })

}

function disableQueryBtn() {
    // let html = `
    // <div class="text-2xl" style="line-height: 1.3rem;">
    // <span class="load_dot1">·</span><span class="load_dot2">·</span><span class="load_dot3">·</span>
    // </div>`;
    // $('#queryBtn').html(html);
    $('#queryBtn').prop("disabled", true);

}

function enableQueryBtn() {
    // let html = `
    // <svg
    //     stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24"
    //     stroke-linecap="round" stroke-linejoin="round" height="1em" width="1em"
    //     xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1">
    //     <line x1="22" y1="2" x2="11" y2="13"></line>
    //     <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
    // </svg>`;
    // let html = ' <img  src="images/search.png">'
    // $('#queryBtn').html(html);
    $('#queryBtn').prop("disabled", false);

}

function getAnswer(question) {
    var result = '';
    var paras = {
        "messages": [
            {"role": "user", "content": question}
        ],
        "category": window.category,
        "channelId": channelId
        // 这里最后不能写死
    };
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: queryUrl,
        async: false,
        data: JSON.stringify(paras),
//        async: false,
        success: function (json) {
//        	defer.resolve(json);
            var res = $.parseJSON(JSON.stringify(json));    //这里需要修改解析方式
            if (res != null && res.status == "success") {
                // 将json对象与uuid一起写入到session中
                var uuid = getUuid();
                storageJson(uuid, json);
                // 获取回答的个数
                var length = res.data.length;
                var textArr = new Array();
                var refFileArr = new Array();
                var refPathArr = new Array();
                for (var i = 0; i < length; i++) {
                    textArr.push(res.data[i].text);
                    refFileArr.push(res.data[i].filename);
                    refPathArr.push(res.data[i].filepath);
                }
                // 修改内容
                result = textArr[0];
            }
        },
        error: function () {
            alert("返回失败");

        }

    });
    return result;
}


function newChat() {
    var d = {}
    chatList.push(d);
    chatIndex = chatList.length - 1;
    changeChat(chatList[chatIndex]);
}


function showStopBox() {
    $('#stopChat').prop("display", true);
}

let inputMaxHeight = 0;
let inputMinHeight = 0;

$("#queryBox textarea").on("input", function () {
    let el = $(this);
    if (document.body.clientWidth >= 900) {
        if (inputMinHeight != 0) {
            el.height(inputMinHeight);
        }
        return;
    }
    flexibleTextarea(el);
});

function flexibleTextarea(el) {
    if (inputMaxHeight == 0) {
        inputMaxHeight = el.height() * 5;
        inputMinHeight = el.height();
    }

    // 缩小
    hideStretch();
    el.height(inputMinHeight);
    let curHeight = el.prop('scrollHeight');
    if (inputMaxHeight >= curHeight) {
        el.height(curHeight);
    } else {
        // 扩展到最大
        el.height(inputMaxHeight);
    }
    if (el.height() > (inputMinHeight * 2)) {
        showStretch();
    }

}

function showStretch() {
    $('#textareaScretch').show();
}

function hideStretch() {
    $('#textareaScretch').hide();
}

function showTextareaMask() {
    $('#textareaMask').show();
    let y = $('#queryBox textarea').offset().top;
    let x = $('#queryBox textarea').offset().left;
    // 同步输入
    $('#textareaCopy').val($("#queryBox textarea").val());
    // 动画
    $('#textareaMask').offset({top: y, let: x});
    $('#textareaMask').animate({top: '0'});

}

function hideTextareaMask() {
    // 隐藏动画
    // let y = $('#queryBox textarea').offset().top;
    // let x = $('#queryBox textarea').offset().left;
    // $('#textareaMask').animate({top:y});
    $('#textareaMask').hide();
    $('#textareaCopy').val('');
    flexibleTextarea($('#queryBox textarea'));
}

$("#textareaCopy").on("input", function () {
    $("#queryBox textarea").val($(this).val());
});


window.addEventListener('resize', function () {
    let el = $("#queryBox textarea");
    if (document.body.clientWidth >= 900) {
        if (inputMinHeight != 0) {
            el.height(inputMinHeight);
        }
        return;
    }
    flexibleTextarea(el);
});

