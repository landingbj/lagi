// document.addEventListener('DOMContentLoaded', loadBall);

var longestCommonSubsequence = function(text1, text2) {
    const m = text1.length, n = text2.length;
    const dp = new Array(m + 1).fill(0).map(() => new Array(n + 1).fill(0));
    for (let i = 1; i <= m; i++) {
        const c1 = text1[i - 1];
        for (let j = 1; j <= n; j++) {
            const c2 = text2[j - 1];
            if (c1 === c2) {
                dp[i][j] = dp[i - 1][j - 1] + 1;
            } else {
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
    }
    return dp[m][n];
};

window.highlightWordIndex = -1;
window.stopRotation = false;
window.maxScale = -1;




function loadBall() {
    // 检测设备类型：是否为手机
    const isMobile = window.innerWidth <= 900;

    // 根据是否为手机端调整 words 数组的长度
    const words = isMobile ? ["股票助手", "汇率助手", "文心助手", "元器助手", "红书优选",
        "天气助手", "油价助手", "体重指数", "健康饮食", "失信查询",
        "高铁助手", "历史今日", "有道翻译", "图像生成", "疯狂星期", "ip查询",
        "动漫图片", "今日运势", "搜狗搜图", "头像生成", "百度搜图", "今日金价",
        "段子生成", "美食推荐", "倒数计时", "出行路线", "查询车辆", "姓氏排名",
        "收益计算", "驾考题库", "血型预测", "Bing搜索", "彩票查询", "文本纠错"]
         :
        ["股票助手", "汇率助手", "文心助手", "元器助手", "红书优选",
        "天气助手", "油价助手", "体重指数", "健康饮食", "失信查询",
        "高铁助手", "历史今日", "有道翻译", "图像生成", "疯狂星期", "ip查询",
        "动漫图片", "今日运势", "搜狗搜图", "头像生成", "百度搜图", "今日金价",
        "段子生成", "美食推荐", "倒数计时", "出行路线", "查询车辆", "姓氏排名",
        "收益计算", "驾考题库", "血型预测", "Bing搜索", "彩票查询", "文本纠错",
        "文本对比", "地点搜索", "芯片查询", "星火助手", "热点新闻", "文章续写",
        "菜谱查询", "答案之书", "文本转换", "人口数据", "诗词名言", "深度问答",
        "商标查询", "票房榜单", "历史人物", "辟谣前线", "谷歌翻译",
        "对联生成", "编程教学", "语言学习", "语法检查", "面试准备", "写作助手",
        "论文查重", "考试复习", "定制化学习", "儿童教育", "旅游翻译",
        "语音翻译", "多语言沟通", "实时翻译", "新闻追踪", "事件提醒",
        "个人助理", "学习路径", "职业规划", "求职简历", "招聘筛选",
        "游戏攻略", "竞技分析", "运动战术", "健身计划", "减脂",
        "心率监控", "血压监控", "睡眠分析", "营养摄入", "减压助手",
        "会议记录", "在线课堂", "绘画教学", "智能合同助手", "法律顾问",
        "税务助手", "智能财务", "危机预测", "客户服务", "自然灾害预警",
        "环保数据", "气候变化", "星座运势", "心理测试", "名人信息"]
    ;

    // const words = isMobile ? ["股票助手", "汇率助手", "文心助手", "元器助手"]
    //      :
    //     ["股票助手", "汇率助手", "文心助手", "元器助手"]
    // ;

    let radius = 0;
    const dtr = Math.PI / 180;
    const d = 300;
    const defaultSpeed = 0.5; // 默认旋转速度
    const size = 250;
    let lasta = defaultSpeed; // 默认 X 方向旋转速度
    let lastb = defaultSpeed; // 默认 Y 方向旋转速度
    let mcList = [];
    let aA = null;
    let oDiv = null;
    let isDragging = false;
    let lastMouseX = 0;
    let lastMouseY = 0;
    let shouldRotate = true;  // 控制是否需要自转
    // let stopRotation = false; // 控制是否完全停止旋转
    let previousHighlightedIndex = -1; // 记录上次高亮的词索引
    let copyHighTag=null;
    let xRotateFinish = false;
    let yRotateFinish = false;

    // let currentHighlightIndex = -1;

    oDiv = document.getElementById('ball-div');

    // 动态添加 <span> 标签
    words.forEach(word => {
        const spanTag = document.createElement('span');
        spanTag.innerText = word;
        oDiv.appendChild(spanTag);
    });

    aA = oDiv.getElementsByTagName('span');  // 现在获取的是 <span> 标签

    for (let i = 0; i < aA.length; i++) {
        const oTag = {};
        oTag.offsetWidth = aA[i].offsetWidth;
        oTag.offsetHeight = aA[i].offsetHeight;
        mcList.push(oTag);
    }

    sineCosine(0, 0, 0);
    positionAll();

    // 默认旋转逻辑
    setInterval(update, 30);

    // 鼠标拖拽逻辑
    oDiv.addEventListener('mousedown', function (ev) {
        isDragging = true;
        shouldRotate = false;  // 拖动时停止自动旋转
        window.stopRotation = false;
        lastMouseX = ev.clientX;
        lastMouseY = ev.clientY;

    });

    document.addEventListener('mousemove', function (ev) {
        if (isDragging) {
            const deltaX = ev.clientX - lastMouseX;
            const deltaY = ev.clientY - lastMouseY;
            // 通过拖拽动态调整旋转速度
            lasta += deltaY * 0.01; // Y 方向拖拽影响 X 轴旋转速度
            lastb -= deltaX * 0.01; // X 方向拖拽影响 Y 轴旋转速度
            
            lastMouseX = ev.clientX;
            lastMouseY = ev.clientY;
        }
    });

    document.addEventListener('mouseup', function () {
        isDragging = false;
        lasta = defaultSpeed; // 默认 X 方向旋转速度
        lastb = defaultSpeed; // 默认 Y 方向旋转速度
        if (!window.stopRotation) shouldRotate = true;  // 鼠标松开时恢复自动旋转
    });

    let highlightWord =  function (word) {
        const targetIndex = words.indexOf(word);
        if (targetIndex === -1) return;


        window.highlightWordIndex = targetIndex;
        window.maxScale = -1;


        // 停止自转
        shouldRotate = false;
        window.stopRotation = false; // 启动加速旋转逻辑

        // 如果有之前高亮的词，清除高亮highlightWordIndex
        if (previousHighlightedIndex !== -1) {
            aA[previousHighlightedIndex].style.color = '';
            aA[previousHighlightedIndex].style.fontWeight = '';
        }
        
    }

    let getHighWord =  function (word) {
       let highWord = "";
       let maxLenth = 0;
        for(let i = 0; i < words.length ; i++) {
            let con =  longestCommonSubsequence(word,  words[i]);
            if(con >= 2 && con > maxLenth) {
                highWord = words[i];
                maxLenth = con.length;
            }
        }
       return highWord;
    }


    function update() {
        if (window.stopRotation) {
            return; // 如果设置为完全停止旋转，不执行旋转逻辑
        }

        let a = lasta;
        let b = lastb;

        let highTag = mcList[window.highlightWordIndex];
        
        let radio = 0.01;
        if (shouldRotate) {
            // 自动旋转逻辑
            if (Math.abs(lasta) < defaultSpeed) lasta = defaultSpeed;
            if (Math.abs(lastb) < defaultSpeed) lastb = defaultSpeed;

            // 模拟缓慢衰减
            lasta *= 0.98;
            lastb *= 0.98;
        } else {

            if(highTag) {
                let p =  1000 / 30;
                let dstX  =  highTag.offsetWidth / 2 + highTag.cx;
                let dstY = - highTag.offsetHeight/2 +  highTag.cy;
                let flagX = false;
                let flagY = false;
                if(copyHighTag) {
                    // is flag chage ?
                    let midX1 =  highTag.cx + highTag.offsetWidth / 2;
                    let midX2 =  copyHighTag.cx - copyHighTag.offsetWidth / 2;
                    let midY1 =  highTag.cy - highTag.offsetHeight / 2;
                    let midY2 =  copyHighTag.cy - copyHighTag.offsetHeight / 2;
                    flagX = ((midX1 * midX2 < 0) || highTag.cx * (highTag.cx + highTag.offsetWidth*0.5) < 0) ? true : false;
                    flagY = ((midY1 * midY2 < 0) || highTag.cy * (highTag.cy + highTag.offsetHeight*0.5) < 0) ? true : false;
                    // console.log(midX1, midX2, midY1, midY2, flagX, flagY);
                }
                if(Math.abs(dstX) <= radius * radio || flagX) {
                    // console.log(dstX, flagX, radius * radio);
                    xRotateFinish = true;
                }
            
                
                
                // 未超过区间 动 或者符号没变
                if(!xRotateFinish) {
                    let deltax = dstX  / p;
                    lasta = 0 ;
                    lastb = deltax ;
                    
                } else {
                    let deltay = dstY / p;
                    lasta = -deltay ;
                    lastb = 0 ;
                    if(Math.abs(dstY) <= radius * radio || flagY) {
                        // console.log(dstY, radius * radio);
                        yRotateFinish = true;
                    }
                    
                }
                // console.log(xRotateFinish, yRotateFinish);
                copyHighTag = JSON.parse(JSON.stringify(highTag));
            }
        }

        

        sineCosine(a, b, 0);
        mcList.forEach((tag, i) => {
            
            const rx1 = tag.cx;
            const ry1 = tag.cy * ca + tag.cz * -sa;
            const rz1 = tag.cy * sa + tag.cz * ca;

            const rx2 = rx1 * cb + rz1 * sb;
            const ry2 = ry1;
            const rz2 = rx1 * -sb + rz1 * cb;

            const rx3 = rx2 * cc + ry2 * -sc;
            const ry3 = rx2 * sc + ry2 * cc;
            const rz3 = rz2;

            tag.cx = rx3;
            tag.cy = ry3;
            tag.cz = rz3;

            const per = d / (d + rz3);
            tag.x = rx3 * per;
            tag.y = ry3 * per;
            tag.scale = per;
            tag.alpha = per;

            
            tag.alpha = (tag.alpha - 0.6) * (10 / 6);
            if(window.highlightWordIndex == i) {
                aA[i].style.color = '#00bdfe';  // 设置目标词的颜色为蓝色
                aA[i].style.fontWeight = 'bold';  // 加粗目标词
            } else {
                aA[i].style.color = '';
                aA[i].style.fontWeight = '';
            }
        });
        doPosition();
        if (!shouldRotate) {
            if(highTag) {
                if(xRotateFinish && yRotateFinish) {
                    window.stopRotation = true;
                    xRotateFinish = false;
                    yRotateFinish = false;
                    copyHighTag = null;
                    // console.log(stopRotation);
                }
            }
        }
        
    }

    function positionAll() {
        const containerWidth = oDiv.offsetWidth;
        const containerHeight = oDiv.offsetHeight;
        radius = Math.min(containerWidth, containerHeight) * 0.40;
        console.log("radius: ", radius);
        restBallCover(radius);
        mcList.forEach((tag, i) => {
            const phi = Math.acos(-1 + (2 * (i + 1) - 1) / mcList.length);
            const theta = Math.sqrt(mcList.length * Math.PI) * phi;
            tag.cx = radius * Math.cos(theta) * Math.sin(phi);
            tag.cy = radius * Math.sin(theta) * Math.sin(phi);
            tag.cz = radius * Math.cos(phi);

            aA[i].style.left = tag.cx + 'px';
            aA[i].style.top = tag.cy + 'px';
        });
    }

    function doPosition() {
        const l = oDiv.offsetWidth * 0.42;
        const t = oDiv.offsetHeight * 0.43;
        for(let i = 0; i < mcList.length; i++) {
            let tag = mcList[i];
            aA[i].style.fontSize = Math.ceil(12 * tag.scale / 2) + 8 + 'px';
            aA[i].style.left = tag.cx + l + 'px';
            aA[i].style.top = tag.cy + t + 'px';
            aA[i].style.opacity = tag.alpha;
        }
    }

    function sineCosine(a, b, c) {
        sa = Math.sin(a * dtr);
        ca = Math.cos(a * dtr);
        sb = Math.sin(b * dtr);
        cb = Math.cos(b * dtr);
        sc = Math.sin(c * dtr);
        cc = Math.cos(c * dtr);
    }

    window.resetBallState = function () {
        // 恢复自转
        shouldRotate = true;
        lasta = defaultSpeed; // 重置 X 轴旋转速度
        lastb = defaultSpeed; // 重置 Y 轴旋转速度

        // 清除所有词的高亮
        mcList.forEach((tag, i) => {
            aA[i].style.color = ''; // 恢复默认颜色
            aA[i].style.fontWeight = ''; // 恢复默认字体粗细
        });
    };


    // Expose the highlightWord function to the global scope
    window.highlightWord = highlightWord;

    window.getHighWord = getHighWord;
}

function restBallCover(radius) {
    const element1 = document.getElementById('ball-corner-container');
    const element2 = document.getElementById('ball-container');
    element1.style.setProperty('--ball-radius',  `${radius}px`);
    element2.style.setProperty('--ball-radius',  `${radius}px`); 
}