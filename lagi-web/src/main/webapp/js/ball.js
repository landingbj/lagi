document.addEventListener('DOMContentLoaded', function () {
    const words = [
        "股票", "汇率", "文心", "腾讯元器", "小红书", "天气", "油价", "体重指数",
        "食物卡路里", "失信人员查询", "高铁票查询", "历史的今天", "有道翻译", "图像生成"
    ];


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
    let stopRotation = false; // 控制是否完全停止旋转
    let previousHighlightedIndex = -1; // 记录上次高亮的词索引

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
        if (!stopRotation) shouldRotate = true;  // 鼠标松开时恢复自动旋转
    });

    function highlightWord(word) {
        const targetIndex = words.indexOf(word);
        if (targetIndex === -1) return;

        const target = mcList[targetIndex];

        // 如果目标词已经在圆心位置，不需要做任何旋转
        if (target.cx === 0 && target.cy === 0 && target.cz === radius) {
            // 高亮目标词，其他词恢复正常
            mcList.forEach((tag, i) => {
                if (i === targetIndex) {
                    aA[i].style.color = '#00bdfe';  // 设置目标词的颜色为蓝色
                    aA[i].style.fontWeight = 'bold';  // 加粗目标词
                }
            });
            previousHighlightedIndex = targetIndex; // 更新上次高亮的词索引
            return;
        }

        // 计算目标词的方向角度，目的是使其对准屏幕圆心
        const targetAngleX = Math.atan2(target.cy, target.cz);  // 计算目标词的X轴角度
        const targetAngleY = Math.atan2(target.cx, target.cz);  // 计算目标词的Y轴角度

        // 停止自转
        shouldRotate = false;
        stopRotation = false; // 启动加速旋转逻辑

        // 如果有之前高亮的词，清除高亮
        if (previousHighlightedIndex !== -1) {
            aA[previousHighlightedIndex].style.color = '';
            aA[previousHighlightedIndex].style.fontWeight = '';
        }

        // 启动加速旋转
        const highlightInterval = setInterval(() => {
            // 通过调整当前的旋转角度向目标角度过渡，使用一个平滑的比例系数 0.1
            lasta += (targetAngleX - lasta) * 0.1;
            lastb += (targetAngleY - lastb) * 0.1;
        }, 30);

        // 2秒后停止旋转并高亮目标词
        setTimeout(() => {
            clearInterval(highlightInterval); // 停止加速旋转
            stopRotation = true; // 停止自转逻辑

            // 高亮目标词，其他词恢复正常
            mcList.forEach((tag, i) => {
                if (i === targetIndex) {
                    aA[i].style.color = '#00bdfe';  // 设置目标词的颜色为蓝色
                    aA[i].style.fontWeight = 'bold';  // 加粗目标词
                }
            });

            // 更新上次高亮的词索引
            previousHighlightedIndex = targetIndex;
        }, 2000);  // 延迟 2 秒，旋转完成后高亮词汇
    }



    function update() {
        if (stopRotation) {
            return; // 如果设置为完全停止旋转，不执行旋转逻辑
        }

        let a = lasta;
        let b = lastb;

        if (shouldRotate) {
            // 自动旋转逻辑
            if (Math.abs(lasta) < defaultSpeed) lasta = defaultSpeed;
            if (Math.abs(lastb) < defaultSpeed) lastb = defaultSpeed;

            // 模拟缓慢衰减
            lasta *= 0.98;
            lastb *= 0.98;
        }

        sineCosine(a, b, 0);
        mcList.forEach(tag => {
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
        });

        doPosition();
    }

    function positionAll() {
        const containerWidth = oDiv.offsetWidth;
        const containerHeight = oDiv.offsetHeight;
        radius = Math.min(containerWidth, containerHeight) / 2;

        mcList.forEach((tag, i) => {
            const phi = Math.acos(-1 + (2 * (i + 1) - 1) / mcList.length);
            const theta = Math.sqrt(mcList.length * Math.PI) * phi;

            tag.cx = radius * Math.cos(theta) * Math.sin(phi);
            tag.cy = radius * Math.sin(theta) * Math.sin(phi);
            tag.cz = radius * Math.cos(phi);

            aA[i].style.left = tag.cx + containerWidth / 2 - tag.offsetWidth / 2 + 'px';
            aA[i].style.top = tag.cy + containerHeight / 2 - tag.offsetHeight / 2 + 'px';
        });
    }

    function doPosition() {
        const l = oDiv.offsetWidth / 2;
        const t = oDiv.offsetHeight / 2;
        mcList.forEach((tag, i) => {
            aA[i].style.left = tag.cx + l - tag.offsetWidth / 2 + 'px';
            aA[i].style.top = tag.cy + t - tag.offsetHeight / 2 + 'px';
            aA[i].style.fontSize = Math.ceil(12 * tag.scale / 2) + 8 + 'px';
            aA[i].style.opacity = tag.alpha;
        });
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
});
