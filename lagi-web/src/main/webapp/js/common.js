function sleep (time) {
    return new Promise((resolve) => setTimeout(resolve, time));
}

function isBlank(value){      
    return !value || !value.toString().trim() || /^[\s\b\0]+$/.test(value.toString());
 }

 function getUuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (Math.random() * 16) | 0,
        v = c == 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

function storageJson(uuid,json){
	jsonStr=JSON.stringify(json);
	sessionStorage.setItem(uuid, jsonStr);
}


// 函数防抖
function debounce(fun,wait=1500){
    let timeout = null
    return function(){
        if(timeout){//如果存在定时器就清空
            clearTimeout(timeout)
        }
        timeout=setTimeout(()=>{
            fun.apply(this,arguments)
        },wait)
    }
 
}

function alert(e){
    $("#msg").remove();
    $("#alert-box").show();
    $("#alert-box").append('<div id="msg" class="msg"><div id="msg_top" class="msg_top">信息<span class="msg_close">×</span></div><div id="msg_cont" class="msg_cont">'+e+'</div><div class="msg_clear" id="msg_clear">确定</div></div>');
    $(".msg_close").click(function (){
        $("#msg").remove();
        $("#alert-box").hide();
    });

    $(".msg_clear").click(function (){
        $("#msg").remove();
        $("#alert-box").hide();
    });
}


function confirm(e){
    $("#confirm-msg").remove();
    $("#confirm-box").show();
    $("#confirm-box").append('<div id="confirm-msg" class="msg msg-container" ><div id="msg_top"  class="msg_top" >信息<span class="msg_close">×</span></div><div id="msg_cont" class="msg_cont" >'+e+'</div><div id="msg_cancel" class="msg_cancel left" >取消</div><div class="msg_close right" id="msg_sure">确定</div></div>');

    const confirmBox = document.getElementById('confirm-box');
    const customConfirm = document.getElementById('confirm-msg');
    const closeBtn = document.getElementsByClassName('msg_close')[0];
    const confirmYes = document.getElementById('msg_sure');
    const confirmNo = document.getElementById('msg_cancel');
    // console.log("confirm", customConfirm, closeBtn, confirmYes, confirmNo);
    return new Promise((resolve) => {
        // 处理确定按钮点击
        confirmYes.onclick = function() {
            $("#confirm-msg").remove();
            $("#confirm-box").hide();
            resolve(true);
        }

        // 处理取消按钮点击
        confirmNo.onclick = function() {
            $("#confirm-msg").remove();
            $("#confirm-box").hide();
            resolve(false);
        }

        // 处理关闭按钮点击
        closeBtn.onclick = function() {
            $("#confirm-msg").remove();
            $("#confirm-box").hide();
            resolve(false);
        }

        // 点击模态背景关闭对话框
        window.onclick = function(event) {
            if (event.target == customConfirm) {
                $("#confirm-msg").remove();
                $("#confirm-box").hide();
                resolve(false);
            }
        }
    });

}


async function test() {
    let a =  await confirm("确定");
    console.log(a);
}

// let a = confirm('你好');

// const result = await confirm('你确定要执行此操作吗？');
// console.log(result);