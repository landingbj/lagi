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