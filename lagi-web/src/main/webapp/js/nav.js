let SOCIAL_NAV_KEY = 'sjjr';

let MODEL_TYPE_LLM = "llm";
let MODEL_TYPE_ASR = "asr";
let MODEL_TYPE_TTS = "tts";
let MODEL_TYPE_IMG2TEXT = "img2Text";
let MODEL_TYPE_IMGENHANCE = "imgEnhance";
let MODEL_TYPE_IMGGEN = "imgGen";
let MODEL_TYPE_VIDEOTRACK = "videoTrack";
let MODEL_TYPE_VIDEOENHANCE = "videoEnhance";
let MODEL_TYPE_TEXT2VIDEO = "text2Video";

let MODEL_TYPES = [MODEL_TYPE_LLM, MODEL_TYPE_ASR, MODEL_TYPE_TTS, MODEL_TYPE_IMG2TEXT,
    MODEL_TYPE_IMGENHANCE, MODEL_TYPE_IMGGEN, MODEL_TYPE_VIDEOTRACK,
    MODEL_TYPE_VIDEOENHANCE, MODEL_TYPE_TEXT2VIDEO];

let currentAppId = null;

let timer = 0;
let currentPromptDialog;
let currentNav = null;


let promptNavs = [
    {
        id: 1,
        key: 'model',
        icon: 'model',
        title: '大模型',
        subNavs: [
            {
                id: 1, key: 'znwd', title: '智能问答', exampleImgSrc: '../images/znwd.png',
                models: ["llm"],
                exampleVedioSrc: '../video/znwd.mp4',
                prompt: '该功能可以针对用户需求帮助用户快速获取信息、解决问题，提高工作效率和便捷性。可用于对话沟通、智能营销、智能客服、情感沟通等需要沟通对话的场景',
                operation: '在输入框内输入您的需求（如“请告诉我康熙皇帝在位几年？”），并点击右侧Logo发送需求，Lagi将会对您作出响应。'
            },
            {
                id: 2, key: 'wbsc', title: '文本生成', exampleImgSrc: '../images/wbsc.png',
                models: ["llm"],
                exampleVedioSrc: '../video/wbsc.mp4',
                prompt: '该功能可以根据用户的需求，生成精准匹配的创作文本。',
                operation: '在输入框内输入您的需求（如“写一份关于唐朝的故事”），并点击右侧Logo发送需求，Lagi将会对您作出响应。'
            },
            {
                id: 3, key: 'yysb', title: '语音识别', exampleImgSrc: '../images/yysb.png',
                models: ["asr"],
                exampleVedioSrc: '../video/yysb.mp4',
                prompt: '该功能可使得大模型与用户进行语音交互、用语音识别代替手写或打字转输入。',
                operation: '长按输入框最左侧的话筒按钮，同时开始说话，按钮松手后会自动识别文字到输入框。'
            },
            {
                id: 4, key: 'qrqs', title: '千人千声', exampleImgSrc: '../images/qrqs.png',
                models: ["tts"],
                exampleVedioSrc: '../video/qrqs.mp4',
                prompt: '该功能的语音回答可采用不同情绪音色，可以为个人用户提供更加便捷、高效的交互方式和更加生动形象的语音体验，为企业提供更优质的服务质量和更高效的工作流程。',
                operation: '在Lagi对您的输入内容作出回应的最右侧，点击“默认”按钮，即可看到多种可供选择的情绪音色。选中其中一个音色后，点击旁边的竖着的三个点，即可选择播放及播放倍速。'
            },
            {
                id: 5, key: 'ktsh', title: '看图说话', exampleImgSrc: '../images/ktsh.png',
                models: ["img2Text"],
                exampleVedioSrc: '../video/ktsh.mp4',
                status: 1,
                prompt: '该功能可自动提取上传图片的信息，并生成对图片的描述，帮助用户理解图片内容。',
                operation: `点击输入框最右侧的文件夹图标，选择图片并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
         已经收到您上传的图片。如果您想生成视频，请输入"视频生成"。如果您想增强图片，请输入"图像增强"。如果您想使用AI描述图片，请输入"看图说话"。
         此时请在输入框内输入“看图说话”，Lagi将会对您的请求作出响应。`
            },
            {
                id: 6, key: 'hzzq', title: '画质增强', exampleImgSrc: '../images/txzq.png',
                models: ["imgEnhance"],
                exampleVedioSrc: '../video/txzq.mp4',
                status: 1,
                prompt: '该功能可以提升图像清晰度、色彩表现、对比度，并减少噪声和杂点，从而增强图像的视觉效果和可读性',
                operation: `点击输入框最右侧的文件夹图标，选择图片并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
        已经收到您上传的图片。如果您想生成视频，请输入"视频生成"。如果您想增强图片，请输入"图像增强"。如果您想使用AI描述图片，请输入"看图说话"。
        此时请在输入框内输入“画质增强”，Lagi将会对您的请求作出响应。`
            },
            {
                id: 7, key: 'tpsc', title: '图片生成', exampleImgSrc: '../images/tpsc.png',
                models: ["imgGen"],
                exampleVedioSrc: '../video/tpsc.mp4',
                status: 1,
                prompt: '该功能可根据用户的需求，生成精准匹配的图片，为用户提供配图',
                operation: '在输入框内输入您的需求（如“生成一张风景图”），并点击右侧Logo发送需求，Lagi将会对您作出响应。'
            },

            {
                id: 8, key: 'spzz', title: '视频追踪', exampleImgSrc: '../images/spzz.png',
                models: ["videoTrack"],
                exampleVedioSrc: '../video/spzz.mp4',
                status: 1,
                prompt: '该功能可对上传视频的内容进行搜索、编辑和创作视频。跟踪人物进行轨迹绘制，框选等操作。',
                operation: '点击输入框最右侧的文件夹图标，选择视频并点击“打开”，即可上传。Lagi将会自动您的请求做出响应。'
            },
            {
                id: 9, key: 'spzq', title: '视频增强', exampleImgSrc: '../images/spzq.png',
                models: ["videoEnhance"],
                exampleVedioSrc: '../video/spzq.mp4',
                status: 1,
                prompt: '该功能可以显著提升视频的质量和观感体验，让观众享受更加清晰、生动、流畅的画面效果。这些技术在影视制作、视频修复、在线视频流等领域具有广泛的应用前景。',
                operation: `点击输入框最右侧的文件夹图标，选择视频并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
            已经收到您上传的视频。如果您想视频追踪，请输入“视频追踪”。如果您想视频增强，请输入“视频增强”。
            此时请在输入框内输入“视频增强”，Lagi将会对您的请求作出响应。`
            },
            {
                id: 10, key: 'spsc', title: '视频生成', exampleImgSrc: '../images/spsc.png',
                models: ["text2Video"],
                exampleVedioSrc: '../video/spsc.mp4',
                status: 1,
                prompt: '该功能可对根据上传的图像，自动生成与之相关的视频。这有助于提高视频的创新性和生产效率，为影视制作、游戏开发、广告创意等领域提供更多的可能性。',
                operation: `点击输入框最右侧的文件夹图标，选择图片并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
         已经收到您上传的图片。如果您想生成视频，请输入"视频生成"。如果您想增强图片，请输入"图像增强"。如果您想使用AI描述图片，请输入"看图说话"。
         此时请在输入框内输入“生成视频”，Lagi将会对您的请求作出响应。`
            }
        ]
    },
    {
        id: 11,
        key: 'intelligentAgent',
        icon: 'intelligentAgent',
        title: '智能体',
        subNavs: [
            {
                "id": 102,
                "agentId": "exchangeRate",
                "title": "汇率助手",
                "templateIssues": "当前美元对人民币汇率是多少?"
            },
            {
                "id": 103,
                "agentId": "yiYan",
                "title": "文心助手",
                "templateIssues": "请帮我写一篇关于科技创新的文章"
            },
            {
                "id": 104,
                "agentId": "yuanQi",
                "title": "元器助手",
                "templateIssues": "这款电子元器件的规格是什么?"
            },
            {
                "id": 105,
                "agentId": "xiaohongshu",
                "title": "红书优选",
                "templateIssues": "推荐一些适合冬季穿的羽绒服"
            },
            {
                "id": 106,
                "agentId": "weather",
                "title": "天气助手",
                "templateIssues": "今天北京天气如何?"
            },
            {
                "id": 110,
                "agentId": "dishonest",
                "title": "失信查询",
                "templateIssues": "请帮我查询一下这个人的失信记录"
            },
            {
                "id": 111,
                "agentId": "ticket",
                "title": "高铁助手",
                "templateIssues": "从北京到上海的高铁票价是多少?"
            },
            {
                "id": 113,
                "agentId": "youdao",
                "title": "有道翻译",
                "templateIssues": "请翻译‘Hello, how are you?’到中文"
            },
            {
                "id": 119,
                "agentId": "sogou_search_pictures",
                "title": "搜狗搜图",
                "templateIssues": "帮我搜索一下刘亦菲的图片"
            },
            {
                "id": 222,
                "agentId": "hot_news",
                "title": "热点新闻",
                "templateIssues": "今天的热点新闻有哪些？"
            },
            {
                "id": 333,
                "agentId": "city_travel_route",
                "title": "出行路线",
                "templateIssues": "从武汉到北京的出行路线是什么？"
            }
        ]
    },
    {
        id: 12,
        key: 'orchestration',
        icon: 'orchestration',
        title: '编排',
        subNavs: [
            {
                id: 11, key: 'kjsx', title: '快捷私训', exampleImgSrc: '../images/kjsx.png',
                exampleVedioSrc: '../video/kjsx.mp4',
                prompt: '该功能可对用户进行个性化推荐、训练某行业或领域的专业翻译、解决冷启动问题、保护数据隐私等，用户可根据需求和偏好投喂数据，使其能够提供更加个性化和定制化的服务。',
                operation: `点击输入框最右侧的文件夹图标，选择文件并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
                            已经收到您的资料文档，您可以在新的会话中，询问与资料中内容相关的问题。
                            此时请在输入框内输入您的询问内容，Lagi将会对您的请求作出响应。`
            },
            {
                id: 12, key: 'zlsc', title: '指令生成', exampleImgSrc: '../images/sczl.png',
                exampleVedioSrc: '../video/sczl.mp4',
                prompt: '该功能是指，当用户提供一篇文档时，大模型能够自动分析文档内容，理解其结构和语义，然后生成与之相关的指令集。这些指令集可以是一系列操作步骤、代码片段、或者是针对特定任务的指导说明。',
                operation: `点击输入框最右侧的文件夹图标，选择文件并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
			已经收到您的资料文档，您可以在新的会话中，询问与资料中内容相关的问题。如果您想生成指令集，请输入"帮我生成指令集”。
			此时请在输入框内输入“帮我生成指令集”，Lagi将会对您的请求作出响应。`
            },
            {
                id: 13, key: 'twhp', title: '图文混排', exampleImgSrc: '../images/twhp.png',
                exampleVedioSrc: '../video/twhp.mp4',
                prompt: '该功能可根据用户提出的问题或需求，以图文并茂的方式为用户提供更加直观、形象和生动的信息和服务，在提高信息传达效果的同时，还能增加用户的阅读体验的，提高人们的工作效率和生活品质。',
                operation: '在输入框内输入您的需求（如“知识图谱的概念”），并点击右侧Logo发送需求，Lagi将会对您作出响应。',
                group: 2
            }
        ]
    },
    {
        id: 14,
        key: 'feature',
        icon: 'feature',
        title: '特色',
        subNavs: [
            {
                id: 14, key: SOCIAL_NAV_KEY, title: '社交接入', exampleImgSrc: '',
                exampleVedioSrc: '../video/sjjr.mp4',
                prompt: '该功能通过接入社交软件，通过RPA和大模型技术自动化社交软件的相关操作。',
                operation: '在输入框内输入您的需求（机器人、定时器、烽火台、引流涨粉），然后按照提示完成相关操作。',
                group: 2
            }
        ]
    },
    {
        id: 15,
        key: 'interfaceTest',
        icon: 'interfaceTest',
        title: '高级功能中心',
        subNavs: [
            {
                id: 15.1,
                key: 'padCoordinate',
                title: '焊盘坐标表生成',
                exampleImgSrc: '../images/padCoordinate.png',
                exampleVedioSrc: '../video/padCoordinate.mp4',
                prompt: '上传 TXT 文件以生成焊盘坐标表。文件大小限制为 10MB，若文件较大或数据行数过多，处理可能需要较长时间。',
                operation: '选择一个 TXT 文件，点击提交按钮，接口将生成并下载焊盘坐标表（DOCX 格式）。'
            },
            {
                id: 15.2,
                key: 'hsrSummary',
                title: '安全规范摘要提取',
                exampleImgSrc: '../images/hsrSummary.png',
                exampleVedioSrc: '../video/hsrSummary.mp4',
                prompt: '上传 PDF 文件以生成硬件安全规范摘要。文件大小限制为 10MB，若文件页数过多或内容复杂，处理可能需要较长时间。',
                operation: '选择一个 PDF 文件，点击提交按钮，接口将生成并下载摘要文件（DOCX 格式）。'
            },
            {
                id: 15.3,
                key: 'analyzeImage',
                title: '图片内容结构化分析',
                exampleImgSrc: '../images/analyzeImage.png',
                exampleVedioSrc: '../video/analyzeImage.mp4',
                prompt: '上传 PNG 或 JPEG 图片文件，接口将返回图像内容的结构化分析文本。',
                operation: '选择一张图片文件，点击提交按钮，接口将返回分析结果并显示在页面上。'
            },
            {
                id: 15.4,
                key: 'pinDiagram',
                title: '引脚图生成与PPT导出',
                exampleImgSrc: '', // 可替换为实际图片路径
                exampleVedioSrc: '', // 可替换为实际视频路径
                prompt: '上传 PDF 文件及Architecture (Block Diagram)和Package & Pad图片以生成引脚图或导出 PPT。支持查询特定关键词的引脚图，文件大小限制为 10MB。',
                operation: '选择一个 PDF 文件和两张图片，点击“下载完整 PPT”生成包含所有内容的 PPT，或点击“下载部分 PPT”生成仅包含 Product Overview 和 Architecture 的 PPT，或输入关键词点击“查询”查看引脚图。'
            },
            {
                id: 15.5,
                key: 'dxAnalyzeImage',
                title: 'DX图像结构化分析',
                exampleImgSrc: '../images/dxAnalyzeImage.png', // 可替换为实际图片路径
                exampleVedioSrc: '../video/dxAnalyzeImage.mp4', // 可替换为实际视频路径
                prompt: '上传 PNG 或 JPEG 图片文件，接口将返回DX图像内容的结构化分析文本。',
                operation: '选择一张图片文件，点击提交按钮，接口将返回分析结果并显示在页面上。'
            },
            {
                id: 15.6,
                key: 'ruleTxtToExcel',
                title: '规则TXT转Excel',
                exampleImgSrc: '../images/ruleTxtToExcel.png',
                exampleVedioSrc: '../video/ruleTxtToExcel.mp4',
                prompt: '上传 TXT 文件以生成规则汇总 Excel 文件。文件大小限制为 10MB，若文件较大或数据行数过多，处理可能需要较长时间。',
                operation: '选择一个 TXT 文件，点击提交按钮，接口将生成并下载 Excel 文件（XLSX 格式）。'
            },
            {
                id: 15.7,
                key: 'safetyMechanism',
                title: '安全机制表生成',
                exampleImgSrc: '../images/safetyMechanism.png',
                exampleVedioSrc: '../video/safetyMechanism.mp4',
                prompt: '上传 PNG 或 JPEG 图片文件，接口将分析图像内容并生成安全机制表（DOCX 格式）。文件大小限制为 10MB。',
                operation: '选择一张图片文件，点击提交按钮，接口将生成并下载安全机制表（DOCX 格式）。'
            }
        ]
    }

];

function loadNavStatus() {
    $.ajax({
        type: "GET",
        contentType: "application/json;charset=utf-8",
        url: "info/getNavStatus",
        async: false,
        success: function (reponse) {
            if (reponse.code !== 0) {
                return;
            }
            let status = reponse.data
            promptNavs.forEach(function (nav) {
                let ns = status[nav.key];
                nav.status = ns;
            })
        },
        error: function () {
            // alert("返回失败");
        }

    });
}

function buildPromptDialogContent(nav) {
    p = nav.prompt;
    o = nav.operation;
    html = `&nbsp;&nbsp;&nbsp;&nbsp;功能介绍：${p}<br/>&nbsp;&nbsp;&nbsp;&nbsp;操作方式: ${o}`
    // return `${pre} ${ exampleImgSrc ? `<img src='${exampleImgSrc}' alt='example' style="width: 100%;"></img>`: '' }`;
    return html;
}

function showPromptNav() {
    let html = '';
    $('#conversationsNav').empty();
    if (!promptNavs) {
        return;
    }

    html += `
    <div class="relative" data-projection-id="5" style="height: auto; opacity: 1;">
        <div class="sticky top-0 z-[16]" data-projection-id="6" style="opacity: 1;">
            <h3 class="h-9 pb-2 pt-3 px-3 text-xs text-gray-500 font-medium text-ellipsis overflow-hidden break-all bg-default-50 dark:bg-default-900">功能大全</h3>
        </div>
        <ol>
            ${genNavItems(promptNavs)}
        </ol>
    </div>
    `;
    $('#conversationsNav').append(html);
}

function genNavItems(navs) {
    let html = '';
    let g = 1;
    for (let index = 0; index < navs.length; index++) {
        const nav = navs[index];
        console.log(nav);

        let group = nav.group == undefined ? 1 : nav.group;
        let style = group == g ? '' : 'style="margin-top:1em"';
        if (g != group) {
            g = group;
        }

        let cls = '';
        let func = `toggleSubNav(${nav.id})`;  // 修改为点击时只触发展开二级菜单

        if (nav.status === 0) {
            cls = 'tooltip text-gray-500';
            func = 'maintenance()';
        }
        // 根据 nav.icon 动态选择图标
        let iconSvg = getIconSvg(nav.icon);  // 动态获取图标SVG

        html += ` 
        <li class="relative z-[15]" ${style} data-nav-id="${nav.id}" style="opacity: 1; height: auto;">
            <a onclick="${func}" data-tooltip="维护中" class="${cls} flex py-2 px-2 items-center gap-3 relative rounded-md hover:bg-default-100 dark:hover:bg-[#2A2B32] cursor-pointer break-all bg-default-50 hover:pr-4 dark:bg-default-900 group">
                ${iconSvg}  <!-- 动态插入图标 -->
                <div class="flex-1 text-ellipsis max-h-5 overflow-hidden break-all relative">
                    ${nav.title}
                </div>
                <!-- 小箭头图标 -->
                <span class="arrow ml-2">&gt;</span>
            </a>
            ${genSubNavItems(nav.subNavs)}  <!-- 二级菜单在这里渲染 -->
        </li>
        `;
    }
    return html;
}

function toggleSubNav(navId) {
    const subNav = document.querySelector(`[data-nav-id="${navId}"] .sub-nav`);
    const arrow = document.querySelector(`[data-nav-id="${navId}"] .arrow`);
    const navItem = document.querySelector(`[data-nav-id="${navId}"] a`);  // 当前一级菜单项

    // 收起所有已展开的二级菜单
    const allSubNavs = document.querySelectorAll('.sub-nav');
    allSubNavs.forEach(subNavItem => {
        if (!subNavItem.classList.contains('hidden') && subNavItem !== subNav) {
            subNavItem.classList.add('hidden');  // 隐藏所有展开的二级菜单
        }
    });

    // 收起所有箭头，恢复到右箭头状态
    const allArrows = document.querySelectorAll('.arrow');
    allArrows.forEach(arrowItem => {
        arrowItem.classList.remove('down');
    });

    // 清除所有一级菜单的 'active' 类
    const allNavItems = document.querySelectorAll('a');
    allNavItems.forEach(item => {
        item.classList.remove('active');
    });

    // 添加 'active' 类到当前被点击的一级菜单项
    navItem.classList.add('active');

    // 如果当前的二级菜单已经展开，则收回它，箭头恢复为右箭头
    if (subNav) {
        if (subNav.classList.contains('hidden')) {
            subNav.classList.remove('hidden');  // 显示当前二级菜单
            // subNav.classList.add('bg-darker');  // 展开时加深背景颜色
            if (arrow) {
                arrow.classList.add('down');  // 展开时箭头朝下
            }
        } else {
            subNav.classList.add('hidden');  // 收回当前二级菜单
            // subNav.classList.remove('bg-darker'); // 恢复背景颜色
            if (arrow) {
                arrow.classList.remove('down');  // 收回时箭头恢复为右箭头
            }
        }
    }
}

function genSubNavItems(subNavs) {
    if (!subNavs) {
        return '';
    }

    let subHtml = '<ul class="sub-nav hidden">';  // 初始时隐藏二级菜单
    for (let i = 0; i < subNavs.length; i++) {
        const subNav = subNavs[i];
        console.log("subNav.id:" + subNav.id);

        // 如果子菜单项包含 agentId，我们将其存储在 data-app-id 中
        subHtml += `
        <li class="sub-nav-item">
            <a onclick="getPromptDialog(${subNav.id})" class="sub-nav-link flex py-2 px-2 items-center gap-3 rounded-md bg-default-50 hover:bg-default-100 dark:bg-default-900 dark:hover:bg-[#2A2B32]" data-nav-id="${subNav.id}" data-app-id="${subNav.agentId}">
                ${subNav.title}
            </a>
        </li>
        `;
    }
    subHtml += '</ul>';
    return subHtml;
}

// 添加点击激活事件
document.addEventListener('click', function (e) {
    // 检查点击是否在二级菜单项上
    if (e.target && e.target.classList.contains('sub-nav-link')) {
        // 获取所有二级菜单项
        const allSubNavLinks = document.querySelectorAll('.sub-nav-link');

        // 移除所有其他二级菜单的激活样式
        allSubNavLinks.forEach(link => {
            link.classList.remove('active');  // 移除激活样式
        });

        // 给当前点击的菜单项添加激活样式
        e.target.classList.add('active');
    }
});

// 根据 icon 类型返回对应的 SVG 图标
function getIconSvg(icon) {
    let svg = '';
    switch (icon) {
        case 'model':
            svg = `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" class="icon-sm" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                    </svg>`;
            break;
        case 'intelligentAgent':
            svg = `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" class="icon-sm" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 1C7.03 1 3 4.03 3 8c0 1.17.29 2.27.8 3.19.2-.01.39-.02.6-.02C6.87 7.34 9.74 5 12 5s5.13 2.34 7.6 6.17c.21 0 .4.01.6.02.51-.92.8-2.02.8-3.19 0-3.97-4.03-7-9-7zM2 14v5h2v-5h1c.03-.4.07-.79.1-1.19.03-.4.07-.79.1-1.19.03.4.07.79.1 1.19H6v5h2v-5h1c.03-.4.07-.79.1-1.19.03-.4.07-.79.1-1.19.03.4.07.79.1 1.19H10v5h2v-5h1c.03-.4.07-.79.1-1.19.03-.4.07-.79.1-1.19.03.4.07.79.1 1.19H14v5h2v-5H18c-.03-.4-.07-.79-.1-1.19-.03-.4-.07-.79-.1-1.19-.03.4-.07.79-.1 1.19H16v5h2v-5h2V14h-2V9h-2v5h-2v-5h-2v5h-2v-5h-2v5h-2v-5h-2V9H2v5h2z"></path>
                    </svg>`;
            break;
        case 'orchestration':
            svg = `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" class="icon-sm" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                        <path d="M19 7h-6V5h6v2zm0 4h-6v-2h6v2zm0 4h-6v-2h6v2zM5 5h6v2H5V5zm0 4h6v2H5V9zm0 4h6v2H5v-2z"></path>
                    </svg>`;
            break;
        case 'feature':
            svg = `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" class="icon-sm" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 19V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2zM12 3h6v4h-6zm-2 0H6v4h4zm6 14H6v-4h10z"></path>
                    </svg>`;
            break;
        default:
            svg = `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" class="icon-sm" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                        <path d="M0 0h24v24H0z" fill="none"></path>
                    </svg>`;
            break;
    }
    return svg;
}

function maintenance() {

}

function loadModelSelect(nav) {
    if (nav.models !== undefined && Array.isArray(nav.models)) {
        $('#model-selects').empty();
        $('#model-prefences').show();
        for (let i = 0; i < nav.models.length; i++) {
            let selectHtml = '';
            let modelType = nav.models[i];
            let modelInfos = getModeList(modelType);
            selectHtml = `
            <select class = "model-select" name="${modelType}">
                ${genModelOptions(modelInfos)}
            </select>
            `;
            $('#model-selects').append(selectHtml);
        }
    } else {
        $('#model-selects').empty();
        $('#model-prefences').hide();
    }
}

function genModelOptions(modelInfos) {
    let res = ``;
    if (modelInfos !== undefined && Array.isArray(modelInfos)) {
        for (let j = 0; j < modelInfos.length; j++) {
            const modelInfo = modelInfos[j];
            if (modelInfo.enabled) {
                res += `
                <option value="${modelInfo.model}"  ${modelInfo.activate ? 'selected' : ''}>${modelInfo.model}</option>
                `;
            } else {
                res += `
                <option value="${modelInfo.model}"  disabled>${modelInfo.model}</option>
                `;
            }

        }
    }
    return res;
}

function getModeList(type) {
    let res;
    let params = {
        'type': type,
        'userId': window.finger
    };
    $.ajax({
        type: "GET",
        contentType: "application/json;charset=utf-8",
        url: "preference/getModels",
        async: false,
        data: params,
        success: function (reponse) {
            if (reponse.code !== 0) {
                return;
            }
            res = reponse.data;
        },
        error: function () {
        }
    });
    return res;
}

// 通用函数：更新状态信息（从demo中复用）
function updateStatus(elementId, message, type = 'info') {
    const statusElement = document.getElementById(elementId);
    statusElement.textContent = message;
    statusElement.className = 'status ' + (type === 'error' ? 'error' : type === 'success' ? 'success' : type === 'warning' ? 'warning' : '');
}

// 通用函数：禁用/启用按钮（从demo中复用）
function toggleButton(buttonId, disabled) {
    const button = document.getElementById(buttonId);
    button.disabled = disabled;
}

// 通用函数：检查文件大小（从demo中复用）
function checkFileSize(file, maxSizeMB, statusId) {
    const fileSizeMB = file.size / (1024 * 1024);
    if (fileSizeMB > maxSizeMB) {
        updateStatus(statusId, `文件大小 (${fileSizeMB.toFixed(2)} MB) 超过 ${maxSizeMB} MB，处理可能需要较长时间，请耐心等待。`, 'warning');
        return true;
    } else if (fileSizeMB > maxSizeMB / 2) {
        updateStatus(statusId, `文件较大 (${fileSizeMB.toFixed(2)} MB)，处理可能稍慢，请稍候。`, 'warning');
        return true;
    }
    return false;
}

// 处理焊盘坐标表生成
async function submitPadFile(fileInputId, statusId, buttonId) {
    const fileInput = document.getElementById(fileInputId);
    const file = fileInput.files[0];
    if (!file) {
        updateStatus(statusId, '请选择一个 TXT 文件', 'error');
        return;
    }

    const isLargeFile = checkFileSize(file, 10, statusId);
    toggleButton(buttonId, true);
    updateStatus(statusId, isLargeFile ? '正在上传并处理大文件，请耐心等待...' : '正在上传并处理文件...', 'info');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/pad/generatePadTable', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.msg || '请求失败');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'PadCoordinate.docx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        updateStatus(statusId, '文件生成成功，已下载', 'success');
    } catch (error) {
        updateStatus(statusId, '错误：' + error.message, 'error');
    } finally {
        toggleButton(buttonId, false);
    }
}

// 处理安全规范摘要提取
async function submitHsrFile(fileInputId, statusId, buttonId) {
    const fileInput = document.getElementById(fileInputId);
    const file = fileInput.files[0];
    if (!file) {
        updateStatus(statusId, '请选择一个 PDF 文件', 'error');
        return;
    }

    const isLargeFile = checkFileSize(file, 10, statusId);
    toggleButton(buttonId, true);
    updateStatus(statusId, isLargeFile ? '正在上传并处理大文件，请耐心等待...' : '正在上传并处理文件...', 'info');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/hsrsummary/generateHsrSummary', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.msg || '请求失败');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'HsrSummary.docx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        updateStatus(statusId, '文件生成成功，已下载', 'success');
    } catch (error) {
        updateStatus(statusId, '错误：' + error.message, 'error');
    } finally {
        toggleButton(buttonId, false);
    }
}

// 处理图片内容结构化分析
async function submitImage(fileInputId, statusId, buttonId, resultId) {
    const fileInput = document.getElementById(fileInputId);
    const file = fileInput.files[0];
    if (!file) {
        updateStatus(statusId, '请选择一张图片文件', 'error');
        return;
    }

    toggleButton(buttonId, true);
    updateStatus(statusId, '正在上传并分析图像，请稍候...', 'info');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const apiUrl = fileInputId.includes('dxAnalyzeImage') ?
            '/dxAnalyzeImage/dxAnalyzeImage' :
            '/analyzeImage';
        const response = await fetch(apiUrl, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('请求失败，状态码: ' + response.status);
        }

        const result = await response.json();
        if (result.status !== 'success') {
            throw new Error(result.msg || '分析失败');
        }

        const resultBox = document.getElementById(resultId);
        resultBox.textContent = `状态: ${result.status}\n\n${result.data}`;
        updateStatus(statusId, '分析成功', 'success');
    } catch (error) {
        updateStatus(statusId, '错误：' + error.message, 'error');
    } finally {
        toggleButton(buttonId, false);
    }
}

async function submitSafetyMechanismFile(fileInputId, statusId, buttonId) {
    const fileInput = document.getElementById(fileInputId);
    const file = fileInput.files[0];
    if (!file) {
        updateStatus(statusId, '请选择一张图片文件', 'error');
        return;
    }

    const isLargeFile = checkFileSize(file, 10, statusId);
    toggleButton(buttonId, true);
    updateStatus(statusId, isLargeFile ? '正在上传并处理大文件，请稍候...' : '正在上传并处理文件...', 'info');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/safetymechanism/generateSafetyMechanismTable', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.msg || '请求失败');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'SafetyMechanism.docx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        updateStatus(statusId, '文件生成成功，已下载', 'success');
    } catch (error) {
        updateStatus(statusId, '错误：' + error.message, 'error');
    } finally {
        toggleButton(buttonId, false);
    }
}

// 处理引脚图生成与PPT导出
async function submitPinDiagramFile(fileInputId, architectureInputId, packagePadInputId, statusId, buttonId, pptType) {
    const fileInput = document.getElementById(fileInputId);
    const architectureInput = document.getElementById(architectureInputId);
    const packagePadInput = document.getElementById(packagePadInputId);
    const pdfFile = fileInput.files[0];
    const architectureFile = architectureInput.files[0];
    const packagePadFile = packagePadInput.files[0];

    if (!pdfFile) {
        updateStatus(statusId, '请选择一个 PDF 文件', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('pdfFile', pdfFile);
    if (architectureFile) {
        formData.append('architectureImage', architectureFile);
    }
    if (packagePadFile) {
        formData.append('packagePadImage', packagePadFile);
    }
    formData.append('pptType', pptType);

    const isLargeFile = checkFileSize(pdfFile, 10, statusId);
    toggleButton(buttonId, true);
    updateStatus(statusId, isLargeFile ? '正在上传并生成 PPT，请耐心等待...' : '正在上传并生成 PPT...', 'info');

    try {
        const response = await fetch('/pinDiagramGenerate', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.msg || '请求失败');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = pptType === 'full' ? 'FullPinDiagrams.pptx' : 'PartialPinDiagrams.pptx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        updateStatus(statusId, 'PPT 生成成功，已下载', 'success');
    } catch (error) {
        updateStatus(statusId, '错误：' + error.message, 'error');
    } finally {
        toggleButton(buttonId, false);
    }
}

// 处理引脚图查询
async function queryPinDiagramImage(fileInputId, keywordInputId, statusId, buttonId, imageId) {
    const fileInput = document.getElementById(fileInputId);
    const keywordInput = document.getElementById(keywordInputId);
    const file = fileInput.files[0];
    const keyword = keywordInput.value.trim();

    if (!file) {
        updateStatus(statusId, '请选择一个 PDF 文件', 'error');
        return;
    }
    if (!keyword) {
        updateStatus(statusId, '请输入关键词', 'error');
        return;
    }

    toggleButton(buttonId, true);
    updateStatus(statusId, '正在查询引脚图，请稍候...', 'info');

    const formData = new FormData();
    formData.append('file', file);
    formData.append('keyword', keyword);

    try {
        const response = await fetch('/pinDiagramGenerate', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.msg || '请求失败');
        }

        const result = await response.json();
        if (result.status !== 'success') {
            throw new Error(result.msg || '查询失败');
        }

        const imageElement = document.getElementById(imageId);
        imageElement.src = `data:image/png;base64,${result.data}`;
        imageElement.style.display = 'block';
        updateStatus(statusId, '引脚图查询成功', 'success');
    } catch (error) {
        updateStatus(statusId, '错误：' + error.message, 'error');
    } finally {
        toggleButton(buttonId, false);
    }
}

// 规则TXT转Excel
async function submitRuleTxtToExcel(fileInputId, statusId, buttonId) {
    const fileInput = document.getElementById(fileInputId);
    const file = fileInput.files[0];
    if (!file) {
        updateStatus(statusId, '请选择一个 TXT 文件', 'error');
        return;
    }

    const isLargeFile = checkFileSize(file, 10, statusId);
    toggleButton(buttonId, true);
    updateStatus(statusId, isLargeFile ? '正在上传并处理大文件，请耐心等待...' : '正在上传并处理文件...', 'info');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/ruleTxtToExcel', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.msg || '请求失败');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'RuleSummary.xlsx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        updateStatus(statusId, 'Excel 文件生成成功，已下载', 'success');
    } catch (error) {
        updateStatus(statusId, '错误：' + error.message, 'error');
    } finally {
        toggleButton(buttonId, false);
    }
}

// 更新 createFileUploadUI，支持 pinDiagram
function createFileUploadUI(nav, containerId) {
    injectDemoStyles();
    const container = document.getElementById(containerId);
    container.innerHTML = '';

    const sectionId = `section-${nav.key}`;
    const fileInputId = `${nav.key}-fileInput`;
    const submitButtonId = `${nav.key}-submitBtn`;
    const statusId = `${nav.key}-status`;
    const resultId = `${nav.key}-result`;

    let html = '';
    let acceptType = '';

    if (nav.key === 'padCoordinate') {
        acceptType = '.txt';
        html = `
            <div class="section" id="${sectionId}">
                <h2>${nav.title}</h2>
                <div class="description">${nav.prompt}</div>
                <input type="file" id="${fileInputId}" accept="${acceptType}">
                <button id="${submitButtonId}">提交</button>
                <div id="${statusId}" class="status"></div>
            </div>
        `;
    } else if (nav.key === 'hsrSummary') {
        acceptType = '.pdf';
        html = `
            <div class="section" id="${sectionId}">
                <h2>${nav.title}</h2>
                <div class="description">${nav.prompt}</div>
                <input type="file" id="${fileInputId}" accept="${acceptType}">
                <button id="${submitButtonId}">提交</button>
                <div id="${statusId}" class="status"></div>
            </div>
        `;
    } else if (nav.key === 'analyzeImage' || nav.key === 'dxAnalyzeImage') {
        acceptType = 'image/png, image/jpeg';
        html = `
            <div class="section" id="${sectionId}">
                <h2>${nav.title}</h2>
                <div class="description">${nav.prompt}</div>
                <input type="file" id="${fileInputId}" accept="${acceptType}">
                <button id="${submitButtonId}">提交</button>
                <div id="${statusId}" class="status"></div>
                <pre id="${resultId}"></pre>
            </div>
        `;
    } else if (nav.key === 'pinDiagram') {
        acceptType = '.pdf';
        const keywordInputId = `${nav.key}-keywordInput`;
        const queryButtonId = `${nav.key}-queryBtn`;
        const imageId = `${nav.key}-image`;
        const partialButtonId = `${nav.key}-partialBtn`;
        const architectureInputId = `${nav.key}-architectureInput`;
        const packagePadInputId = `${nav.key}-packagePadInput`;
        html = `
            <div class="section" id="${sectionId}">
                <h2>${nav.title}</h2>
                <div class="description">${nav.prompt}</div>
                <label>PDF 文件:</label>
                <input type="file" id="${fileInputId}" accept="${acceptType}">
                <label>Architecture (Block Diagram) 图片:</label>
                <input type="file" id="${architectureInputId}" accept="image/png,image/jpeg">
                <label>Package & Pad 图片:</label>
                <input type="file" id="${packagePadInputId}" accept="image/png,image/jpeg">
                <input type="text" id="${keywordInputId}" placeholder="请输入关键词（如 Pin Configuration）">
                <button id="${queryButtonId}">查询</button>
                <button id="${submitButtonId}">下载完整 PPT</button>
                <button id="${partialButtonId}">下载部分 PPT</button>
                <div id="${statusId}" class="status"></div>
                <img id="${imageId}" style="display: none; max-width: 100%; margin-top: 20px; border-radius: 8px;" alt="引脚图">
            </div>
        `;
    } else if (nav.key === 'ruleTxtToExcel') {
        acceptType = '.txt';
        html = `
            <div class="section" id="${sectionId}">
                <h2>${nav.title}</h2>
                <div class="description">${nav.prompt}</div>
                <input type="file" id="${fileInputId}" accept="${acceptType}">
                <button id="${submitButtonId}">提交</button>
                <div id="${statusId}" class="status"></div>
            </div>
        `;
    }else if (nav.key === 'safetyMechanism') {
        acceptType = 'image/png, image/jpeg';
        html = `
            <div class="section" id="${sectionId}">
                <h2>${nav.title}</h2>
                <div class="description">${nav.prompt}</div>
                <input type="file" id="${fileInputId}" accept="${acceptType}">
                <button id="${submitButtonId}">提交</button>
                <div id="${statusId}" class="status"></div>
            </div>
        `;
        }

    container.innerHTML = html;

    const submitButton = document.getElementById(submitButtonId);
    if (submitButton) {
        submitButton.addEventListener('click', () => {
            if (nav.key === 'padCoordinate') {
                submitPadFile(fileInputId, statusId, submitButtonId);
            } else if (nav.key === 'hsrSummary') {
                submitHsrFile(fileInputId, statusId, submitButtonId);
            } else if (nav.key === 'analyzeImage' || nav.key === 'dxAnalyzeImage') {
                submitImage(fileInputId, statusId, submitButtonId, resultId);
            } else if (nav.key === 'pinDiagram') {
                submitPinDiagramFile(fileInputId, `${nav.key}-architectureInput`, `${nav.key}-packagePadInput`, statusId, submitButtonId, 'full');
            }else if (nav.key === 'ruleTxtToExcel') {
                submitRuleTxtToExcel(fileInputId, statusId, submitButtonId);
            }else if (nav.key === 'safetyMechanism') {
                submitSafetyMechanismFile(fileInputId, statusId, submitButtonId);
            }
        });
    }

    if (nav.key === 'pinDiagram') {
        const queryButton = document.getElementById(`${nav.key}-queryBtn`);
        if (queryButton) {
            queryButton.addEventListener('click', () => {
                queryPinDiagramImage(fileInputId, `${nav.key}-keywordInput`, statusId, `${nav.key}-queryBtn`, `${nav.key}-image`);
            });
        }
        const partialButton = document.getElementById(`${nav.key}-partialBtn`);
        if (partialButton) {
            partialButton.addEventListener('click', () => {
                submitPinDiagramFile(fileInputId, `${nav.key}-architectureInput`, `${nav.key}-packagePadInput`, statusId, `${nav.key}-partialBtn`, 'partial');
            });
        }
    }
}

// 更新 injectDemoStyles，添加关键词输入框和图片样式
function injectDemoStyles() {
    const style = document.createElement('style');
    style.textContent = `
        .section {
            margin-bottom: 40px;
            padding: 24px;
            background: #ffffff;
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .section:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
        }
        h2 {
            color: #1a1a1a;
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 16px;
            line-height: 1.4;
        }
        .description {
            font-size: 0.95rem;
            color: #5c5c5c;
            margin-bottom: 20px;
            line-height: 1.6;
        }
        input[type="file"] {
            display: block;
            margin-bottom: 16px;
            padding: 10px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            background: #f9f9f9;
            font-size: 0.9rem;
            cursor: pointer;
            transition: border-color 0.2s ease;
        }
        input[type="file"]:hover {
            border-color: #007bff;
        }
        input[type="text"] {
            display: block;
            margin-bottom: 16px;
            padding: 10px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            background: #f9f9f9;
            font-size: 0.9rem;
            width: 100%;
            box-sizing: border-box;
            transition: border-color 0.2s ease;
        }
        input[type="text"]:hover,
        input[type="text"]:focus {
            border-color: #007bff;
            outline: none;
        }
        button {
            padding: 12px 24px;
            background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
            color: #ffffff;
            font-size: 0.95rem;
            font-weight: 500;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: background 0.3s ease, transform 0.2s ease;
            box-shadow: 0 2px 6px rgba(0, 123, 255, 0.3);
            margin-right: 10px;
        }
        button:hover:not(:disabled) {
            background: linear-gradient(135deg, #0056b3 0%, #003d82 100%);
            transform: translateY(-1px);
            box-shadow: 0 4px 10px rgba(0, 123, 255, 0.4);
        }
        button:active:not(:disabled) {
            transform: translateY(0);
            box-shadow: 0 2px 6px rgba(0, 123, 255, 0.3);
        }
        button:disabled {
            background: #d0d0d0;
            color: #a0a0a0;
            cursor: not-allowed;
            box-shadow: none;
        }
        .status {
            margin-top: 12px;
            font-size: 0.9rem;
            font-weight: 500;
            line-height: 1.5;
            transition: opacity 0.3s ease;
        }
        .error {
            color: #d32f2f;
            background: #fce7e7;
            padding: 8px 12px;
            border-radius: 6px;
        }
        .success {
            color: #2e7d32;
            background: #e8f5e9;
            padding: 8px 12px;
            border-radius: 6px;
        }
        .warning {
            color: #f57c00;
            background: #fff3e0;
            padding: 8px 12px;
            border-radius: 6px;
        }
        pre {
            background: #f5f5f5;
            padding: 16px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            font-size: 0.9rem;
            line-height: 1.6;
            white-space: pre-wrap;
            max-height: 400px;
            overflow-y: auto;
            color: #333;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-size: 0.95rem;
            color: #333;
            font-weight: 500;
        }
        @media (max-width: 600px) {
            .section {
                padding: 16px;
                margin-bottom: 24px;
            }
            h2 {
                font-size: 1.25rem;
            }
            .description {
                font-size: 0.85rem;
            }
            button {
                padding: 10px 20px;
                font-size: 0.9rem;
            }
            pre {
                font-size: 0.85rem;
            }
        }
    `;
    document.head.appendChild(style);
}

function getPromptDialog(id) {
    let nav = null;

    // 查找包含指定ID的子导航项
    for (let index = 0; index < promptNavs.length; index++) {
        const category = promptNavs[index];
        for (let j = 0; j < category.subNavs.length; j++) {
            const subNav = category.subNavs[j];
            if (subNav.id == id) {
                nav = subNav;
                currentNav = nav;
                break;
            }
        }
        if (nav) break;
    }

    if (nav == null) {
        alert("找不到对应的信息");
        return;
    }
    currentAppId = nav.agentId;

    // 如果是高级功能中心的子菜单，直接触发文件上传和接口调用
    if (['padCoordinate', 'hsrSummary', 'analyzeImage', 'pinDiagram', 'dxAnalyzeImage', 'ruleTxtToExcel','safetyMechanism'].includes(nav.key)) {
        // 隐藏首页内容
        hideHelloContent();

        // 清空并显示文件上传界面
        createFileUploadUI(nav, 'item-content');

        // 应用demo中的CSS样式（确保页面已包含相关样式）
        // 如果CSS未包含，可在此处动态注入（见下方说明）
    } else {
        // 保留原有逻辑
        if (!(nav.prompt && nav.operation)) {
            $('#queryContent').val(nav.templateIssues);
            resetBallState();
            highlightWord(nav.title);
            return;
        }

        $('#queryContent').val('');
        loadModelSelect(nav);
        hideHelloContent();

        let answer = buildPromptDialogContent(nav);
        let answerJq = newRobotStartDialog('');

        let vedioHtml = '';
        if (nav.exampleVedioSrc) {
            vedioHtml = `
                <video controls width="100%" style="border:10px solid #238efc; border-radius:15px">
                    <source src="${nav.exampleVedioSrc}" type="video/mp4" />
                </video>
            `;
        }

        clearTimeout(timer);
        currentPromptDialog = nav;
        typing(0, answer, answerJq, addRobotDialog, vedioHtml);
    }
}

function savePerference() {
    if (!window.finger) {
        console.log("未识别到身份");
        return;
    }
    let params = {
        'finger': window.finger
    }
    for (let i = 0; i < MODEL_TYPES.length; i++) {
        const modelType = MODEL_TYPES[i];
        if ($(`.model-select[name=${modelType}]`).length > 0) {
            params[modelType] = $(`.model-select[name=${modelType}]`).val();
        }
    }

    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "preference/savePreference",
        async: false,
        data: JSON.stringify(params),
        success: function (res) {
            if (res.code !== 0) {
                return;
            }
            if (res.data <= 0) {
                alert("保存失败");
            }
            alert("保持成功!!！");
        },
        error: function () {
            alert("返回失败");

        }

    });
}

function clearPreference() {
    if (!window.finger) {
        return;
    }
    let params = {
        'userId': window.finger
    }
    $.ajax({
        type: "GET",
        contentType: "application/json;charset=utf-8",
        url: "preference/clearPreference",
        async: false,
        data: params,
        success: function (res) {
            if (res.code !== 0) {
                return;
            }
            if (res.data <= 0) {
                alert("清除失败");
            }
            alert("重置成功!!!");
            loadModelSelect(currentNav);
        },
        error: function () {
            alert("返回失败");

        }

    });
}

function typing(i, str, jq, callback, ...args) {
    str += '';
    if (i <= str.length) {
        let temp = str.substring(i, i + 6);
        if (temp == '&nbsp;') {
            i += 6;
        } else {
            jq.html(str.slice(0, i++) + '<p style="display: inline-block"></p>');
        }
        timer = setTimeout(() => {
            typing(i, str, jq, callback, args)
        }, 3)
    } else {
        jq.html(str);//结束打字,移除 _ 光标
        clearTimeout(timer);
        callback(args);

        if (currentPromptDialog !== undefined && currentPromptDialog.key === SOCIAL_NAV_KEY) {
            resetSocialPromptStep();
            getAppListHtml();
        }
    }
}

function backToHello() {
    /*$('#item-content').empty();
    showHelloContent();
    currentPromptDialog = undefined;
    $('#model-prefences').hide();*/
    location.reload();
}

function getAppListHtml() {
    let html = '';
    $.ajax({
        type: "GET",
        contentType: "application/json;charset=utf-8",
        url: "/v1/rpa/getAppList",
        success: function (res) {
            res.data.forEach((app) => {
                let appName = app.appName;
                let appIcon = app.appIcon;
                html += '<div class="appType"><img src="' + appIcon + '" alt="' + appName + '"><div class="appTypeName">' + appName + '</div></div>';
                SOCIAL_APP_MAP.set(app.appId.toString(), app.appName);
            });
            let prompt = '<div>请问您想接入哪款社交软件</div>';
            addRobotDialog(prompt + html + '</br>');
        },
        error: function () {
            html = '返回失败';
        }
    });
}

