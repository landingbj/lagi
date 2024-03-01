/*
* @Author: 朝夕熊
* @Date:   2017-11-05 10:17:45
* @Last Modified by:   zhaoxixiong
* @Last Modified time: 2017-11-12 14:12:14
*/
function Circle(obj) {
    var This = this;
     this.objDefault = {
    		value: '联动北方 智能运维',
    		fontSize: 128,
    		fontSpace: 4,
    		circleSize: 1,
    		circleColor: 'blue',
    		backType: 'back',
     		backDynamics: 'spring',
     		circleOrigin: null,
     		circleSpeed: 1,
     		complete: null
    }
    this.param = $.extend(this.objDefault, obj);
    this.init();
    this.getUserText();
    //this.generalRandomParam();
    this.drawCircles();
    this.ballAnimate();
    setTimeout(function(){
    	This.animateToText();
    }, 2000);

    

    // 窗口改变大小后，生计算并获取画面
    window.onresize = function(){
        This.stateW = document.body.offsetWidth;
        This.stateH = document.body.offsetHeight;
        This.iCanvasW = This.iCanvas.width = This.stateW;
        This.iCanvasH = This.iCanvas.height = This.stateH;
        This.ctx = This.iCanvas.getContext("2d");
    }
}

// 初始化
Circle.prototype.init = function(){
    //父元素宽高
    this.stateW = document.body.offsetWidth;
    this.stateH = document.body.offsetHeight;
    this.iCanvas = document.createElement("canvas");

    // 设置Canvas 与父元素同宽高
    this.iCanvasW = this.iCanvas.width = this.stateW;
    this.iCanvasH = this.iCanvas.height = this.stateH;
    this.iCanvas.style.position = 'absolute';
    this.iCanvas.style.zIndex = 100;
    // 获取 2d 绘画环境
    this.ctx = this.iCanvas.getContext("2d");
    // 插入到 body 元素中
    document.body.appendChild(this.iCanvas);

    this.iCanvasCalculate = document.createElement("canvas");
    // 用于保存计算文字宽度的画布
    this.mCtx =  this.iCanvasCalculate.getContext("2d");
    this.mCtx.font = this.param.font +  "px arial";

    this.fontCanvas = document.createElement("canvas");
    // 用于保存计算文字宽度的画布
    this.fCtx =  this.fontCanvas.getContext("2d");
    this.fCtx.font = this.param.font +  "px arial";

    this.iCanvasPixel = document.createElement("canvas");
    this.iCanvasPixel.setAttribute("style","position:absolute;top:0;left:0;");
    this.pCtx = null; // 用于绘画文字的画布

    // 随机生成圆的数量
    //this.ballNumber = ramdomNumber(1000, 2000);
    this.ballNumber = trimAll(this.param.value).length * (this.param.fontSize / 128 * 500);
    // 保存所有小球的数组
    this.balls = [];
    // 保存动画中最后一个停止运动的小球
    this.animte = null;
    this.imageData = null;

    this.textWidth = 0; // 保存生成文字的宽度
    this.textHeight = 150; // 保存生成文字的高度
    this.inputText = this.param.value; // 保存用户输入的内容

    this.actionCount = 0;

    this.ballActor = []; // 保存生成文字的粒子
    this.actorNumber = 0; // 保存生成文字的粒子数量

    this.backType = "back"; // 归位
    this.backDynamics = ""; // 动画效果

    this.isPlay = false; // 标识（在生成文字过程中，不能再生成）

}
// 渲染出所有圆
Circle.prototype.drawCircles = function () {
    for(var i=0;i<this.ballNumber;i++){
        this.renderBall(this.balls[0]);
    }
}

// 获取用户输入文字
Circle.prototype.getUserText = function(){
            this.inputText = this.param.value;
            this.getAnimateType();
            this.getTextPixel();
            this.isPlay = true;
}

// 计算文字的宽
Circle.prototype.calculateTextWidth = function () {
    this.textWidth = this.inputText.length * this.param.fontSize / 128 * 128;
}

// 获取文字像素点
Circle.prototype.getTextPixel = function () {
    if(this.pCtx){
        this.pCtx.clearRect(0,0,this.textWidth,this.textHeight);
    }
    this.calculateTextWidth(this.inputText);
    this.iCanvasPixel.width = this.textWidth;
    this.iCanvasPixel.height = this.textHeight;
    this.pCtx =  this.iCanvasPixel.getContext("2d");
    this.pCtx.font = this.param.fontSize + "px arial";
    this.pCtx.fillStyle = "white";
    this.pCtx.textBaseline = "middle";
    this.pCtx.textAlign = 'center';
    this.pCtx.fillText(this.inputText,this.textWidth/2,this.textHeight/2);
    this.imageData = this.pCtx.getImageData(0,0,this.textWidth,this.textHeight).data;
    this.getTextPixelPosition(this.textWidth,this.textHeight);
}

// 获取文字粒子像素点位置
Circle.prototype.getTextPixelPosition = function (width,height) {
    var left = (this.iCanvasW - width)/2;
    var top = (this.iCanvasH - height)/2;
    var space = this.param.fontSpace;
    this.actionCount = 0;
    for(var i=0;i<this.textHeight;i+=space){
        for(var j=0;j<this.textWidth;j+=space){
            var index = j*space+i*this.textWidth*4;
            if(this.imageData[index] == 255){
                //if(this.actionCount<this.ballNumber){
            		this.generalRandomParamOne();
                    this.balls[this.actionCount].status = 0;
                    this.balls[this.actionCount].targetX = left+j;
                    this.balls[this.actionCount].targetY = top+i;
                    this.balls[this.actionCount].backX = this.balls[this.actionCount].x;
                    this.balls[this.actionCount].backY = this.balls[this.actionCount].y;
                    this.ballActor.push(this.balls[this.actionCount]);
                    this.actionCount++;
                //}
            }
        }
        this.ballNumber = this.actionCount;
        this.actorNumber = this.ballActor.length;
    }
    //this.animateToText();
}

// 粒子运动到指定位置
Circle.prototype.animateToText = function(){
	var This = this;
	for(var i=0;i<This.actorNumber;i++){
		var ball = This.ballActor[i];
		ball.status = 1;
	}
	   if(this.isPlay == true){
	        this.isPlay = false;
	    }
    for(var i=0;i<This.actorNumber;i++){
        dynamics.animate(This.ballActor[i], {
          x: this.ballActor[i].targetX,
          y: this.ballActor[i].targetY
        },{
            type: dynamics.easeIn,
            duration: 500
        });
    }
    setTimeout(function(){
        This.ballbackType();
    },1500);
}

// 粒子原路返回
Circle.prototype.ballBackPosition = function(){
	var This = this;
    for(var i=0;i<This.actorNumber;i++){
        var ball = This.ballActor[i];
        this.changeStatus(ball);
        dynamics.animate(ball, {
          x: ball.backX,
          y: ball.backY
        },{
            type: dynamics[this.backDynamics],
            duration: 500,
            complete: dynamics.animate(this.iCanvas,{
            	opacity: 0
            },{
            	type: dynamics[this.backDynamics],
            	duration: 500,
            	complate: setTimeout(function(){
            		This.iCanvas.style.display = 'none';
            		This.param.complete && This.param.complete();
            	}, 500)
            })
        });
    }
}

// 获取类型|动画效果
Circle.prototype.getAnimateType = function() {
    /*this.backType = selectType.options[selectType.options.selectedIndex].value;
    this.backDynamics = selectDynamics.options[selectDynamics.options.selectedIndex].value;*/
	this.backType = this.param.backType;
    this.backDynamics = this.param.backDynamics;
}

// 复位散开
Circle.prototype.ballbackType = function(){
    if(this.backType == "back"){
        this.ballBackPosition();
    }else{
        this.ballAutoPosition();
    }
    this.ballActor = [];
}

// 随机散开
Circle.prototype.ballAutoPosition = function(ball){
    for(var i=0;i<this.actorNumber;i++){
        this.changeStatus(this.ballActor[i])
    }
}

// 更改小球状态
Circle.prototype.changeStatus = function(ball){
    ball.status = 0;
    if(this.isPlay == true){
        this.isPlay = false;
    }


}

// 随机生成每个圆的相关参数
Circle.prototype.generalRandomParam = function(){
    for(var i=0;i<this.ballNumber;i++){
        var ball = {};
        ball.size = this.param.circleSize; // 随机生成圆半径
        // 随机生成圆心 x 坐标
        if(this.param.circleOrigin === null){
        	ball.x = ramdomNumber(0+ball.size, this.iCanvasW-ball.size);
            ball.y = ramdomNumber(0+ball.size, this.iCanvasH-ball.size);
        }else{
        	ball.x = ramdomNumber(this.param.circleOrigin[0], this.param.circleOrigin[1]);
            ball.y = ramdomNumber(this.param.circleOrigin[2], this.param.circleOrigin[3]);
        }
        ball.speedX = ramdomNumber(-this.param.circleSpeed, this.param.circleSpeed);
        ball.speedY = ramdomNumber(-this.param.circleSpeed, this.param.circleSpeed);
        this.balls.push(ball);
        ball.status = 0;
        ball.targetX = 0;
        ball.targetY = 0;
        ball.backX = 0;
        ball.backY = 0;
    }
}

Circle.prototype.generalRandomParamOne = function(){
        var ball = {};
        ball.size = this.param.circleSize; // 随机生成圆半径
        // 随机生成圆心 x 坐标
        if(this.param.circleOrigin === null){
        	ball.x = ramdomNumber(0+ball.size, this.iCanvasW-ball.size);
            ball.y = ramdomNumber(0+ball.size, this.iCanvasH-ball.size);
        }else{
        	ball.x = ramdomNumber(this.param.circleOrigin[0], this.param.circleOrigin[1]);
            ball.y = ramdomNumber(this.param.circleOrigin[2], this.param.circleOrigin[3]);
        }
        ball.speedX = ramdomNumber(-this.param.circleSpeed, this.param.circleSpeed);
        ball.speedY = ramdomNumber(-this.param.circleSpeed, this.param.circleSpeed);
        this.balls.push(ball);
        ball.status = 0;
        ball.targetX = 0;
        ball.targetY = 0;
        ball.backX = 0;
        ball.backY = 0;
}
// 改变圆的位置
Circle.prototype.changeposition = function(){
    for(var i=0;i<this.ballNumber;i++){
        if( this.balls[i].status == 0){
            this.balls[i].x += this.balls[i].speedX;
            this.balls[i].y += this.balls[i].speedY;
        }
    }
}

// 画圆
Circle.prototype.renderBall = function(ball){
    this.ctx.fillStyle = this.param.circleColor;
    this.ctx.beginPath(); // 这个一定要加
    this.ctx.arc(ball.x, ball.y, ball.size, 0, 2 * Math.PI);
    this.ctx.closePath(); // 这个一定要加
    this.ctx.fill();
}

// 小球碰撞判断
Circle.prototype.collision = function(ball){
    for(var i=0;i<this.ballNumber;i++){
       if(ball.x>this.iCanvasW-ball.size || ball.x<ball.size){
            if(ball.x>this.iCanvasW-ball.size){
                ball.x = this.iCanvasW-ball.size;
            }else{
                ball.x = ball.size;
            }
            ball.speedX = - ball.speedX;
       }
       if(ball.y>this.iCanvasH-ball.size || ball.y<ball.size){
            if(ball.y>this.iCanvasH-ball.size){
                ball.y = this.iCanvasH-ball.size;
            }else{
                ball.y = ball.size;
            }
            ball.speedY = - ball.speedY;
       }
    }
}


// 开始动画
Circle.prototype.ballAnimate = function(){
    var This = this;
    var animateFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.msRequestAnimationFrame;
    (function move(){
        animte = animateFrame(move);
        This.ctx.clearRect(0, 0, This.iCanvasW, This.iCanvasH);
        This.changeposition();
        for(var i=0;i<This.ballNumber;i++){
           This.collision(This.balls[i]);
           This.renderBall(This.balls[i]);
        }

    })();
}

// 生成一个随机数
function ramdomNumber(min, max) {
    return Math.random() * (max - min) + min;
}
// 去除所有的空格
function trimAll(str)
{
    var result;
    result = str.replace(/(^\s+)|(\s+$)/g,"");
    result = result.replace(/\s/g,"");
    return result;
}

