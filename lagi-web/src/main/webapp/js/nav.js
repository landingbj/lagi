let promptNavs = [
    {id:1, title: '智能问答',exampleImgSrc:'../images/znwd.png',
        exampleVedioSrc:'../video/znwd.mp4',
        prompt:'该功能可以针对用户需求帮助用户快速获取信息、解决问题，提高工作效率和便捷性。可用于对话沟通、智能营销、智能客服、情感沟通等需要沟通对话的场景',
        operation:'在输入框内输入您的需求（如“请告诉我康熙皇帝在位几年？”），并点击右侧Logo发送需求，Lagi将会对您作出响应。'},
    {id:2, title: '文本生成',exampleImgSrc:'../images/wbsc.png',
        exampleVedioSrc:'../video/wbsc.mp4',
        prompt:'该功能可以根据用户的需求，生成精准匹配的创作文本。',
        operation:'在输入框内输入您的需求（如“写一份关于唐朝的故事”），并点击右侧Logo发送需求，Lagi将会对您作出响应。'},
    {id:3, title: '语音识别',exampleImgSrc:'../images/yysb.png',
        exampleVedioSrc:'../video/yysb.mp4',
        prompt:'该功能可使得大模型与用户进行语音交互、用语音识别代替手写或打字转输入。',
        operation:'长按输入框最左侧的话筒按钮，同时开始说话，按钮松手后会自动识别文字到输入框。'},
    {id:4, title: '千人千声',exampleImgSrc:'../images/qrqs.png',
        exampleVedioSrc:'../video/qrqs.mp4',
        prompt:'该功能的语音回答可采用不同情绪音色，可以为个人用户提供更加便捷、高效的交互方式和更加生动形象的语音体验，为企业提供更优质的服务质量和更高效的工作流程。',
        operation:'在Lagi对您的输入内容作出回应的最右侧，点击“默认”按钮，即可看到多种可供选择的情绪音色。选中其中一个音色后，点击旁边的竖着的三个点，即可选择播放及播放倍速。'},
    {id:5, title: '看图说话',exampleImgSrc:'../images/ktsh.png',
        exampleVedioSrc:'../video/ktsh.mp4',
        status:0,
        prompt:'该功能可自动提取上传图片的信息，并生成对图片的描述，帮助用户理解图片内容。',
        operation:`点击输入框最右侧的文件夹图标，选择图片并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
         已经收到您上传的图片。如果您想生成视频，请输入"视频生成"。如果您想增强图片，请输入"图像增强"。如果您想使用AI描述图片，请输入"看图说话"。
         此时请在输入框内输入“看图说话”，Lagi将会对您的请求作出响应。`},
    {id:6, title: '画质增强',exampleImgSrc:'../images/txzq.png',
        exampleVedioSrc:'../video/txzq.mp4',
        status:0,
        prompt:'该功能可以提升图像清晰度、色彩表现、对比度，并减少噪声和杂点，从而增强图像的视觉效果和可读性',
        operation:`点击输入框最右侧的文件夹图标，选择图片并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
        已经收到您上传的图片。如果您想生成视频，请输入"视频生成"。如果您想增强图片，请输入"图像增强"。如果您想使用AI描述图片，请输入"看图说话"。
        此时请在输入框内输入“画质增强”，Lagi将会对您的请求作出响应。`},
    {id:7, title: '图片生成',exampleImgSrc:'../images/tpsc.png',
        exampleVedioSrc:'../video/tpsc.mp4',
        status:0,
        prompt:'该功能可根据用户的需求，生成精准匹配的图片，为用户提供配图',
        operation:'在输入框内输入您的需求（如“生成一张风景图”），并点击右侧Logo发送需求，Lagi将会对您作出响应。'},

    {id:8, title: '视频追踪',exampleImgSrc:'../images/spzz.png',
        exampleVedioSrc:'../video/spzz.mp4',
        status:0,
        prompt:'该功能可对上传视频的内容进行搜索、编辑和创作视频。跟踪人物进行轨迹绘制，框选等操作。',
        operation:'点击输入框最右侧的文件夹图标，选择视频并点击“打开”，即可上传。Lagi将会自动您的请求做出响应。'},
    {id:9, title: '视频增强',exampleImgSrc:'../images/spzq.png',
        exampleVedioSrc:'../video/spzq.mp4',
        status:0,
        prompt:'该功能可以显著提升视频的质量和观感体验，让观众享受更加清晰、生动、流畅的画面效果。这些技术在影视制作、视频修复、在线视频流等领域具有广泛的应用前景。',
        operation:`点击输入框最右侧的文件夹图标，选择视频并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
            已经收到您上传的视频。如果您想视频追踪，请输入“视频追踪”。如果您想视频增强，请输入“视频增强”。
            此时请在输入框内输入“视频增强”，Lagi将会对您的请求作出响应。`},
    {id:10, title: '视频生成',exampleImgSrc:'../images/spsc.png',
        exampleVedioSrc:'../video/spsc.mp4',
        status:0,
        prompt:'该功能可对根据上传的图像，自动生成与之相关的视频。这有助于提高视频的创新性和生产效率，为影视制作、游戏开发、广告创意等领域提供更多的可能性。',
        operation:`点击输入框最右侧的文件夹图标，选择图片并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
         已经收到您上传的图片。如果您想生成视频，请输入"视频生成"。如果您想增强图片，请输入"图像增强"。如果您想使用AI描述图片，请输入"看图说话"。
         此时请在输入框内输入“生成视频”，Lagi将会对您的请求作出响应。`},
    {id:11, title: '快捷私训',exampleImgSrc:'../images/kjsx.png',
        exampleVedioSrc:'../video/kjsx.mp4',
        prompt:'该功能可对用户进行个性化推荐、训练某行业或领域的专业翻译、解决冷启动问题、保护数据隐私等，用户可根据需求和偏好投喂数据，使其能够提供更加个性化和定制化的服务。',
        operation:`点击输入框最右侧的文件夹图标，选择文件并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
         已经收到您的资料文档，您可以在新的会话中，询问与资料中内容相关的问题。
         此时请在输入框内输入您的询问内容，Lagi将会对您的请求作出响应。`},
    {id:12, title: '指令生成',exampleImgSrc:'../images/sczl.png',
        exampleVedioSrc:'../video/sczl.mp4',
        prompt:'该功能是指，当用户提供一篇文档时，大模型能够自动分析文档内容，理解其结构和语义，然后生成与之相关的指令集。这些指令集可以是一系列操作步骤、代码片段、或者是针对特定任务的指导说明。',
        operation:`点击输入框最右侧的文件夹图标，选择文件并点击“打开”，即可上传。Lagi将会根据上载的内容对您作出如下提示：
			已经收到您的资料文档，您可以在新的会话中，询问与资料中内容相关的问题。如果您想生成指令集，请输入"帮我生成指令集”。
			此时请在输入框内输入“帮我生成指令集”，Lagi将会对您的请求作出响应。`},

    {id:13, title: '图文混排',exampleImgSrc:'../images/twhp.png',
        exampleVedioSrc:'../video/twhp.mp4',
        prompt:'该功能可根据用户提出的问题或需求，以图文并茂的方式为用户提供更加直观、形象和生动的信息和服务，在提高信息传达效果的同时，还能增加用户的阅读体验的，提高人们的工作效率和生活品质。',
        operation:'在输入框内输入您的需求（如“知识图谱的概念”），并点击右侧Logo发送需求，Lagi将会对您作出响应。',
        group : 2
    },
    // {id:14, title: '数据服务',exampleImgSrc:'../images/sjfw.png',
    //        exampleVedioSrc:'../video/sjfw.mp4',
    //        prompt:'该功能可根据用户提出的问题或需求，以图文并茂的方式为用户提供更加直观、形象和生动的信息和服务，在提高信息传达效果的同时，还能增加用户的阅读体验的，提高人们的工作效率和生活品质。',
    //       operation:'在输入框内输入您的需求（如“知识图谱的概念”），并点击右侧Logo发送需求，Lagi将会对您作出响应。',
    //       group : 2 },
]


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
    if(!promptNavs) {
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
        let group = nav.group == undefined ? 1 : nav.group;
        let style = group == g ? '' :  'style="margin-top:3em"';
        if (g != group) {
            g = group;
        }
        let cls = '';
        let func = `getPromptDialog(${nav.id})`;
        if(nav.status === 0) {
            cls = 'tooltip text-gray-500';
            func='maintenance()';
        }
        
        html += `
        <li class="relative z-[15]" ${style} data-projection-id="7" style="opacity: 1; height: auto;">
            <a  onclick=${func} data-tooltip="维护中" class="${cls} flex py-2 px-2 items-center gap-3 relative rounded-md hover:bg-default-100 dark:hover:bg-[#2A2B32] cursor-pointer break-all bg-default-50 hover:pr-4 dark:bg-default-900 group">
                <svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" class="icon-sm" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z">
                    </path>
                </svg>
                <div class="flex-1 text-ellipsis max-h-5 overflow-hidden break-all relative">
                    ${nav.title}<div class="absolute inset-y-0 right-0 w-8 z-10 bg-gradient-to-l dark:from-default-900 from-gray-50 group-hover:from-gray-100 dark:group-hover:from-[#2A2B32]">
                    </div>
                </div>
            </a>
            
        </li>
        `;
    }
    return html;
}


function maintenance() {

}

function getPromptDialog(id) {
    let nav = null;
    for (let index = 0; index < promptNavs.length; index++) {
        if(id == promptNavs[index].id) {
            nav = promptNavs[index];
        }
    }
    if(nav == null) {
        alert("找不到对应的信息");
    }
    // 隐藏 首页
    hideHelloContent();
    let answer = buildPromptDialogContent(nav);
    let answerJq =  newRobotStartDialog('');
    let vedioHtml = `
    <video controls width="100%" style = "border:10px solid #238efc; border-radius:15px">
        <source src="${nav.exampleVedioSrc}" type="video/mp4" />
    </video>
    `;
    clearTimeout(timer);
    typing(0, answer, answerJq, addRobotDialog, vedioHtml);
}

let timer = 0;

function typing (i, str, jq, callback, ...args) {
    str += '';
    if (i <= str.length) {
        let temp = str.substring(i, i+6);
        if(temp == '&nbsp;') {
            i += 6;
        } else {
            jq.html(str.slice(0, i++) + '<p style="display: inline-block"></p>');
        }
        timer = setTimeout(()=>{
            typing(i, str, jq, callback, args)
        } , 30)
    }
    else {
        jq.html(str);//结束打字,移除 _ 光标
        clearTimeout(timer);
        callback(args);
    }
}

function backToHello() {
    $('#item-content').empty();
    showHelloContent();
}
