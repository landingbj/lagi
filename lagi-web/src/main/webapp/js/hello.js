let topTile = 'Linkmind';
let finishedLoadTitle = false;

let activeModel = 0;

let models = [
    // {name:"GPT-3.5"}, {name:"GPT-4"}
]

let introTable = [
    [{title:"测试1", content:"内容1"},{title:"测试2", content:"内容2"},],
    [{title:"测试3", content:"内容3"},{title:"测试4", content:"内容4"},],
]

let agent_title = '我的智能体';
let agent_tools = [
    {name:'社交圈', bind_func: 'socialCircles', available:true},
    {name:'视频流', bind_func: 'notifyAvailable', available:false},
    {name:'语音流', bind_func: 'notifyAvailable', available:false},
    {name:'传感器', bind_func: 'notifyAvailable', available:false},
    {name:'工控线', bind_func: 'notifyAvailable', available:false},
]


document.title = HTML_TITLE;


function initHelloPage() {
    
    initModelSlide();
    initIntroduces();
    loadCorners();
    // initAgentTool();
    $('#model-prefences').hide();
}


function showHelloContent() {
    // $('#item-content').show();
    $('#introduces').show();
    $('#modelChoices').hide();
    $('#topTitle').show();
}


function hideHelloContent() {
    $('#introduces').hide();
    $('#modelChoices').hide();
    $('#topTitle').hide();
    $('#item-content').empty();
    hideBallDiv(); // 隐藏球形 div
}

function loadIntroduces() {
    $.ajax({
        type: "GET",
        contentType: "application/json;charset=utf-8",
        url: "info/getPrompts",
        async: false,
        data: {size:4},
        success: function(reponse) {
            if(reponse.code != 0) {
                introTable = [];
                return ;
            }
            introTable = [];
            introTable.push(reponse.data.slice(0, 2)) ;
            introTable.push(reponse.data.slice(2, 4)) ;
        },
        error: function(){
            // alert("返回失败");

        }
    
    });
}

function initIntroduces() {
    loadIntroduces()
    let html = '';  
    for (let i = 0; i < introTable.length; i++) {
        const row = introTable[i];
        html += `
            <div class="flex flex-col gap-3">${addTr(row)}</div>
            `;
    }
    $('#introduces .grid').empty();
    $('#introduces .grid').append(html);
    scroll();
}


function fillQueryText(That) {
    let content = $(That).data('content');
    let agent = $(That).data('agent');
    $('#queryContent').val(content);
    $('#queryContent').data("agent", agent);
    currentAppId = agent;
}

function addTr(ls) {
    let html = '';
    for (let i = 0; i < ls.length; i++) {
        const col = ls[i];
        // html += `
        // <div data-projection-id="1" style="opacity: 1; transform: none;">
        // <button type="button" data-content="${col.content}" data-agent ="${col.agentId ? col.agentId: ""}"   onclick="fillQueryText(this)" class="btn relative btn-neutral group w-full whitespace-nowrap rounded-xl text-left text-gray-700 shadow-[0px_1px_6px_0px_rgba(0,0,0,0.02)] dark:text-gray-300 md:whitespace-normal" as="button">
        //         <div class="flex w-full gap-2 items-center justify-center">
        //             <div class="flex w-full items-center justify-between">
        //                 <div class="flex flex-col overflow-hidden">
        //                     <div class="truncate font-semibold">
        //                         ${col.title}</div>
        //                     <div class="truncate opacity-50 item-column">
        //                         <p>to ${col.content}</p>
        //                     </div>
        //                 </div>
        //                 <div class="absolute bottom-0 right-0 top-0 flex items-center rounded-xl bg-gradient-to-l from-gray-100 from-[60%] pl-6 pr-3 text-gray-700 opacity-0 group-hover:opacity-100 dark:from-gray-700 dark:text-gray-200">
        //                     <span class="" data-state="closed"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="none" class="icon-sm">
        //                             <path d="M.5 1.163A1 1 0 0 1 1.97.28l12.868 6.837a1 1 0 0 1 0 1.766L1.969 15.72A1 1 0 0 1 .5 14.836V10.33a1 1 0 0 1 .816-.983L8.5 8 1.316 6.653A1 1 0 0 1 .5 5.67V1.163Z" fill="currentColor">
        //                             </path>
        //                         </svg></span></div>
        //             </div>
        //         </div>
        //     </button>
        // </div>
        // `
        html += `
        <div data-projection-id="1" style="opacity: 1; transform: none;">
        <button type="button" data-content="${col.content}" data-agent ="${col.agentId ? col.agentId: ""}"   onclick="fillQueryText(this)" class="btn relative btn-neutral group w-full whitespace-nowrap rounded-xl text-left text-gray-700 shadow-[0px_1px_6px_0px_rgba(0,0,0,0.02)] dark:text-gray-300 md:whitespace-normal" as="button">
                <div class="flex w-full gap-2 items-center justify-center">
                    <div class="flex w-full items-center justify-between">
                        <div class="flex flex-col overflow-hidden">
                            <div class="truncate  item-column">
                                <p style="display: flex;" >
                                    <span style="display: inline !important;white-space: nowrap;width: 14px;" ><svg t="1752916211531" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5079" width="14" height="20" data-spm-anchor-id="a313x.search_index.0.i12.40f03a81CmvggW"><path d="M512 512m-170.666667 0a170.666667 170.666667 0 1 0 341.333334 0 170.666667 170.666667 0 1 0-341.333334 0Z" fill="#2196F3" p-id="5080"></path></svg></span>
                                    <span style="display: inline !important;white-space: nowrap; padding-left: 21px"> ${col.content}</span>
                                </p>
                            </div>
                        </div>
                        <div class="absolute bottom-0 right-0 top-0 flex items-center rounded-xl bg-gradient-to-l from-gray-100 from-[60%] pl-6 pr-3 text-gray-700 opacity-0 group-hover:opacity-100 dark:from-gray-700 dark:text-gray-200">
                            <span class="" data-state="closed"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="none" class="icon-sm">
                                    <path d="M.5 1.163A1 1 0 0 1 1.97.28l12.868 6.837a1 1 0 0 1 0 1.766L1.969 15.72A1 1 0 0 1 .5 14.836V10.33a1 1 0 0 1 .816-.983L8.5 8 1.316 6.653A1 1 0 0 1 .5 5.67V1.163Z" fill="currentColor">
                                    </path>
                                </svg></span></div>
                    </div>
                </div>
            </button>
        </div>
        `
    }
    return html;
}

function scroll() {
    var itemColumns = document.getElementsByClassName("item-column");
    for (let i = 0; i < itemColumns.length; i++) {
        var column = itemColumns[i];
        if (column.scrollWidth > column.clientWidth) {
            var scrollColum = column;
            var d = column.scrollWidth - column.clientWidth;
            $(column).find("p").addClass("scrollDiv");
        }
    }   
}


function initModelSlide() {

    $('#modelChoices ul').empty();
    if(models.length <= 0 ){
        // 隐藏模型
        $('#modelChoices').hide();
        return ;
    }
    for (let i = 0; i < models.length; i++) {
        const model = models[i];
        let html = '';
        if(activeModel == i) {
            html += `
            <li class="group/toggle w-full" data-testid="text-davinci-002-render-sha">
                <button type="button" onclick=chooseModel(${i}) id="radix-:ri:" aria-haspopup="menu" aria-expanded="false" data-state="closed" class="w-full cursor-pointer">
                    <div class="group/button relative flex w-full items-center justify-center gap-1 rounded-lg border py-3 outline-none transition-opacity duration-100 sm:w-auto sm:min-w-[148px] md:gap-2 md:py-2.5 border-black/10 bg-white text-gray-900 shadow-[0_1px_7px_0px_rgba(0,0,0,0.06)] hover:!opacity-100 dark:border-[#4E4F60] dark:bg-gray-700 dark:text-gray-100">
                        <span class="max-[370px]:hidden relative">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="none" class="icon-sm transition-colors text-brand-green" width="16" height="16">
                                <path d="M9.586 1.526A.6.6 0 0 0 8.553 1l-6.8 7.6a.6.6 0 0 0 .447 1h5.258l-1.044 4.874A.6.6 0 0 0 7.447 15l6.8-7.6a.6.6 0 0 0-.447-1H8.542l1.044-4.874Z" fill="currentColor"></path>
                            </svg>
                        </span><span class="truncate text-sm font-medium md:pr-1.5 pr-1.5">${model.name}</span>
                    </div>
                </button>
            </li>
            `;
        } else {
            html += `
            <li class="group/toggle w-full" data-testid="gpt-4-upsell">
                <button type="button" onclick=chooseModel(${i}) id="radix-:rk:" aria-haspopup="menu" aria-expanded="false" data-state="closed" class="w-full cursor-pointer">
                <div class="group/button relative flex w-full items-center justify-center gap-1 rounded-lg border py-3 outline-none transition-opacity duration-100 sm:w-auto sm:min-w-[148px] md:gap-2 md:py-2.5 border-transparent text-gray-500 hover:text-gray-800 hover:dark:text-gray-100">
                    <span class="max-[370px]:hidden relative">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="none" class="icon-sm transition-colors group-hover/button:text-brand-purple" width="16" height="16">
                            <path d="M12.784 1.442a.8.8 0 0 0-1.569 0l-.191.953a.8.8 0 0 1-.628.628l-.953.19a.8.8 0 0 0 0 1.57l.953.19a.8.8 0 0 1 .628.629l.19.953a.8.8 0 0 0 1.57 0l.19-.953a.8.8 0 0 1 .629-.628l.953-.19a.8.8 0 0 0 0-1.57l-.953-.19a.8.8 0 0 1-.628-.629l-.19-.953h-.002ZM5.559 4.546a.8.8 0 0 0-1.519 0l-.546 1.64a.8.8 0 0 1-.507.507l-1.64.546a.8.8 0 0 0 0 1.519l1.64.547a.8.8 0 0 1 .507.505l.546 1.641a.8.8 0 0 0 1.519 0l.546-1.64a.8.8 0 0 1 .506-.507l1.641-.546a.8.8 0 0 0 0-1.519l-1.64-.546a.8.8 0 0 1-.507-.506L5.56 4.546Zm5.6 6.4a.8.8 0 0 0-1.519 0l-.147.44a.8.8 0 0 1-.505.507l-.441.146a.8.8 0 0 0 0 1.519l.44.146a.8.8 0 0 1 .507.506l.146.441a.8.8 0 0 0 1.519 0l.147-.44a.8.8 0 0 1 .506-.507l.44-.146a.8.8 0 0 0 0-1.519l-.44-.147a.8.8 0 0 1-.507-.505l-.146-.441Z" fill="currentColor"></path>
                        </svg>
                    </span>
                    <span class="truncate text-sm font-medium md:pr-1.5 pr-1.5">${model.name}</span>
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true" class="icon-sm ml-0.5 transition-colors sm:ml-0 group-hover/options:text-gray-500 !text-gray-500 -ml-2 group-hover/button:text-brand-purple">
                        <path fill-rule="evenodd" d="M12 1.5a5.25 5.25 0 00-5.25 5.25v3a3 3 0 00-3 3v6.75a3 3 0 003 3h10.5a3 3 0 003-3v-6.75a3 3 0 00-3-3v-3c0-2.9-2.35-5.25-5.25-5.25zm3.75 8.25v-3a3.75 3.75 0 10-7.5 0v3h7.5z" clip-rule="evenodd"></path>
                    </svg>
                </div>
                </button>
            </li>
            `;
        }
        $('#modelChoices ul').append(html);
    }
}

function chooseModel(index) {
    activeModel = index;
    initModelSlide();
}


function initTopTile() {
    if(!finishedLoadTitle) {
        $.ajax({
            type : "GET",
            url : "user/getDefaultTitle",
            success : function(res) {
                if (res.status === 'success') {
                    $('#centerTitleBox h1').html(res.data);
                    // $('#topTitle h1 span').html(res.data);
                    // const a = parseInt($('#title-canvas')[0].style.width);
                    // const b = parseInt($('#title-canvas')[0].style.height);
                    // topTile = res.data;
                    // drawTitle('title-canvas', a, b, 16, 64, 16, topTile);
                    // finishedLoadTitle = true;
                }
            },
            error: function(res) {
                $('#centerTitleBox h1').html(topTile);
                // $('#topTitle h1 span').html(topTile);
                // const a = parseInt($('#title-canvas')[0].style.width);
                // const b = parseInt($('#title-canvas')[0].style.height);
                // drawTitle('title-canvas', a, b, 16, 64, 16, topTile);
                // finishedLoadTitle = true;
            }
        });
    } else {
        $('#centerTitleBox h1').html(topTile);
        // const a = parseInt($('#title-canvas')[0].style.width);
        // const b = parseInt($('#title-canvas')[0].style.height);
        // drawTitle('title-canvas', a, b, 16, 64, 16, topTile);
    }
}

function setupCanvas(canvas) {
    // 获取设备的像素比
    const dpr = window.devicePixelRatio || 1;

    // 获取 Canvas 的 CSS 尺寸
    const rect = canvas.getBoundingClientRect();
    const width = rect.width;
    const height = rect.height;

    // 设置 Canvas 的实际尺寸为 CSS 尺寸乘以 DPR
    canvas.width = width * dpr;
    canvas.height = height * dpr;

    // 缩放绘图上下文以适应高分辨率屏幕
    const ctx = canvas.getContext('2d');
    ctx.scale(dpr, dpr);

    return ctx;
}

function drawTitle(canvasId='title-canvas',  width = 300, height = 120, line1height = 16, line2height = 64, line3height = 16, text2='Lag[i]') {
    const dpr = window.devicePixelRatio || 1;
    
    const canvas = document.getElementById(canvasId);
    
    const rect = canvas.getBoundingClientRect();
    const rwidth = rect.width;
    const rheight = rect.height;
    
    canvas.width = rwidth * dpr;
    canvas.height = rheight * dpr;
    
    const ctx = canvas.getContext('2d');

    if(width == 300 && height == 120) {
        line1height = 16;
        line2height = 32;
        line3height = 16;
    }else if(width == 150 && height == 60) {
        line1height = 8;
        line2height = 32;
        line3height = 8;
    }
    

    
    // 定义文本内容
    const text1 = '问';
    // const text2 = 'Lag[i]';
    const text3 = SUB_SYSTEM_TITLE;

    
    const color1 = '#808080'; // 黑色
    const color2 = '#808080'; // 灰色
    const color3 = '#808080'; // 灰色

    line1height = line1height * dpr;
    line2height = line2height * dpr;
    line3height = line3height * dpr;    

    // 定义颜色和字体大小
    const fontSize1 = line1height + 'px';
    let fontSize2 = line2height + 'px';
    const fontSize3 = line3height + 'px';

    // 设置第二行字体和颜色
    ctx.font = `bold ${fontSize2} sans-serif`;
    ctx.fillStyle = color2;

    // 测量第二行文本的宽度
    let text2Width = ctx.measureText(text2).width;
    
    if(text2Width > width * dpr) {
        line2height = width * dpr / (text2.length + 2);
        fontSize2 = line2height + 'px';
        ctx.font = `bold ${fontSize2} sans-serif`;
        ctx.fillStyle = color2;
        text2Width = ctx.measureText(text2).width;
    }


    const row1Top = line1height;
    const row2Top = line1height + line2height * 0.85;
    const row3Top = row2Top + line3height * 2;


    // 设置基准字体，以便 rem 单位能够正确计算
    // 这里假设基准字体大小为 16px
    const baseFontSize = 16;
    ctx.font = `italic ${fontSize1} sans-serif`;

    // 测量第一行文本的宽度
    const text1Width = ctx.measureText(text1).width;

    // 加载 Logo 图片
    const logo = new Image();
    logo.src = 'images/bdw.png'; // 请将此处替换为您的 Logo 图片路径

    logo.onload = () => {
        // 计算 Logo 图片的宽度与字体大小相同
        const logoWidth = parseInt(fontSize2) * 3 / 5;
        const logoHeight = parseInt(fontSize2) * 3 / 4; // 保持比例

        // 计算第二行总宽度（文本 + Logo）
        const totalWidth = text2Width + logoWidth;

        // 计算起始 X 坐标，使内容居中
        const startX = (canvas.width - totalWidth) / 2;

        let startXLogo =  startX + text2Width;
        // 绘制第二行文本
        // ctx.fillText(text2, startX, row2Top); // 调整 Y 坐标以适应字体大小
        startXLogo = startX + reduceSpecialSymbol(ctx, startX, row2Top, text2, line2height, color2);
        // 绘制 Logo 图片
        ctx.drawImage(logo, startXLogo, row1Top  + (logoHeight *2 / 7) , logoWidth, logoHeight); // 调整 Y 坐标以对齐
        
        var imageData = ctx.getImageData(startXLogo, row1Top  + (logoHeight *2 / 7) , logoWidth, logoHeight);
        var data = imageData.data;
        // for (var i = 0; i< data.length; i += 4) {
        //     let red =  data[i];
        //     let green = data[i + 1];
        //     let blue =  data[i+2];
        //     if (blue > red) {
        //         // console.log(data[i], data[i+1], data[i+2])
        //         data[i] = 255;
        //         // data[i+1] =  70;
        //         data[i+1] = data[i+1] * 0.9;
        //         data[i+2] =  0;
        //     }
        // }
        ctx.putImageData(imageData, startXLogo, row1Top  + (logoHeight *2 / 7));

        // 设置第三行字体和颜色
        ctx.font = `${fontSize3} sans-serif`;
        ctx.fillStyle = color3;

        // 绘制第三行文本
        // ctx.fillText(text3, text3StartX,  row3Top); // 调整 Y 坐标以适应字体大小
        tiling(ctx, startX, row3Top,  text2Width + logoWidth, text3)
        // 绘制第一行文本
        ctx.font = `${fontSize1} sans-serif`;
        ctx.fillStyle = color1;

        const text1StartX = (canvas.width - text1Width) / 2;
        ctx.font = `bold italic ${fontSize1} sans-serif`;
        ctx.fillText(text1, startXLogo - line1height * 2.1, row1Top); // 调整 Y 坐标以适应字体大小
        // const dpr = window.devicePixelRatio || 1;
        // ctx.scale( 1, 1);
    };

    logo.onerror = () => {
        console.error('无法加载 Logo 图片');
    };
}

function reduceSpecialSymbol(ctx, startX, startY, text, fontSize, color) {
    // let size = [];
    let texts = [];
    let start = 0;
    for (let i = 0; i < text.length; i++) {
        if(text[i] == '[' || text[i] == ']') {
            let a = text.slice(start, i);
            texts.push(a);
            texts.push(text[i]);
            start = i + 1;
        }
    }
    
    if(texts.length == 0) {
        ctx.font = `bold ${fontSize + 'px'} sans-serif`;
        ctx.fillText(text, startX, startY);
        return ctx.measureText(text).width;
    }
    
    let startXX = startX;
    let width = 0;
    ctx.fillStyle = color;
    for (let i = 0; i < texts.length; i++) {
        let size = texts[i] == '[' || texts[i] == ']' ? (fontSize * 3 / 4) : fontSize;
        ctx.font = `bold ${size + 'px'} sans-serif`;
        ctx.fillText(texts[i], startXX, startY);
        startXX += ctx.measureText(texts[i]).width;
        width += ctx.measureText(texts[i]).width;
    }
    return width;
    // ctx.fillText(text, startX, startY); // 调整 Y 坐标以适应字体大小
}

function tiling(ctx, startX, startY, width, text) {
    const textWidth = ctx.measureText(text).width / 2;
    const remainingSpace = Math.floor(width / text.length);
    for (let i = 0; i < text.length; i++) {
        ctx.fillText(text[i], startX  + (i * remainingSpace) + textWidth, startY);
    }
}


function initAgentTool() {
    $('#agent-head').html(agent_title);

    /*$('#agent-head').on('click', function() {
        loadAgentList(1);
        openAgentList();
    });*/

    $('#agent-tools').empty();
    for (let index = 0; index < agent_tools.length; index++) {
        const agent = agent_tools[index];
        let html = `<li class=" pl-5  ${agent.available ? '' : 'not-available'}" onclick="${agent.bind_func}()" >> ${agent.name}</li>`;
        $('#agent-tools').append(html);
    }
}


function socialCircles() {
    for(const element of promptNavs) {
        let nav = element;
        if(nav.id == FEATURE_NAV_ID) {
            for(const element of nav.subNavs) {
                let subNav = element;
                if(subNav.key == SOCIAL_NAV_KEY) {
                    getPromptDialog(FEATURE_NAV_ID, subNav.id);
                }
            }
        }
    }
}

function notifyAvailable() {
    // alert('功能不可用');
}





// 定义一个函数来设置 div 的大小
function setDivSize() {
    replaceConversationAttached();
    // 获取 div 元素
    const itemContent = document.getElementById('item-content');
    const no_content = document.getElementById('not-content');
    const intro = document.getElementById('introduces');
    // todo margin-top + 1个字符大小
    const title = document.getElementById('centerTitleBox');
    const titleBox = document.getElementById('topTitle');
    const ball = document.getElementById('ball-div');
    const left_nav_bar = document.getElementById('navigation_bar');
    // const top_nav_bar = document.getElementById('top-nav');
    
    // 获取当前窗口的宽度和高度
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;
    

    const item_widht= windowWidth - left_nav_bar.offsetWidth;
    // ball
    // let ballRadius = Math.min(windowWidth * 0.8, 512); 
    const intro_h =  intro.offsetHeight;
    let title_h =  titleBox.offsetHeight;
    const content_h =  itemContent.offsetHeight;
    // let top_nav_h =  top_nav_bar.offsetHeight;

    const computedStyle = window.getComputedStyle(ball);
    const ball_m_t =  parseInt(computedStyle.marginTop);
    let ball_m_b =  parseInt(computedStyle.marginBottom);
    // console.log(top_nav_h, ball_m_b)
    // radius 最大 512
    // top_nav_h =  20;
    let d1 =  Math.min(windowWidth * 0.8, 400) ;
    let d2 = content_h - intro_h - title_h  - ball_m_t - ball_m_b;
    
    let marginDelta = left_nav_bar.offsetWidth > 0 ? 39 : 12;
    if(d2 < 100) {
        // title / 2
        title.style.height = '60px';
        title.style.width = '150px';
        title.width = 150;
        title.height = 60;
        titleBox.style.height = '70px';
        title_h =  titleBox.offsetHeight;
    } else {
        title.style.height = '120px';
        title.style.width = '300px';
        title.width = 300;
        title.height = 120;
        titleBox.style.height = '130px';
        title_h =  titleBox.offsetHeight;
        // alert(title_h);
    }
    let temp = content_h - intro_h - title_h  - ball_m_t;
    let margin = 20;
    d2 = temp - margin;
    if (d2 > d1) {
        d2 = d1;
        margin = temp - d2;
    }
    document.documentElement.style.setProperty('--ball-m-bottom', margin + 'px');
    ball_m_b = parseInt(computedStyle.marginBottom);

    // d2 = content_h - intro_h - title_h  - ball_m_t - ball_m_b;
    let ball_d = 0;
    // alert([d2, content_h , intro_h , title_h , ball_m_t , ball_m_b].join(","));
    
    if(d2 < 0) {
        ball_d = 0;
    } else {
        ball_d = d2;
    }

    ball_d -= 10;
    // console.log("ball_d", ball_d);
    ball.style.width = ball_d + 'px';
    ball.style.height = ball_d + 'px';
    ball.style.marginLeft = (item_widht - ball_d) / 2  + marginDelta+ 'px';

    // alert(content_h  - title_h - intro_h -ball_m_t - ball_m_b - ball_d);
    initTopTile();

}


function replaceConversationAttached() {
    // if($('.robot-return').length > 0) {
    //     console.log("replaceConversationAttached");
    //     let text_width =  $('.text-area')[0].offsetWidth;
    //     let robot_width =  $('.robot-return')[0].offsetWidth;
    //     let convs_width = 0;
    //     for(let i = 0; i < $('.conv-attached').length; i ++) {
    //         let dom = $('.conv-attached')[i];
    //         convs_width = $('.conv-attached')[i].offsetWidth;
    //         if(robot_width >= text_width + convs_width) {
    //             dom.classList.remove('justify-end');
    //             dom.classList.add('justify-between');
    //             $(dom).find('.appendVoice')[0].classList.add( "self-end", "lg:self-center", "justify-center", "mt-2", "gap-3", "md:gap-4", "lg:gap-1", "lg:absolute", "lg:top-0", "lg:translate-x-full", "lg:right-0", "lg:mt-0", "lg:pl-2", "visible");
    //         } else {
    //             dom.classList.remove('justify-between');
    //             dom.classList.add('justify-end');
    //             $(dom).find('.appendVoice')[0].classList.remove( "self-end", "lg:self-center", "justify-center", "mt-2", "gap-3", "md:gap-4", "lg:gap-1", "lg:absolute", "lg:top-0", "lg:translate-x-full", "lg:right-0", "lg:mt-0", "lg:pl-2", "visible");
    //         }
    //     }
    // }
    
    // let convs =  $('.conv-attached');
}

function debounce(func, delay) {
    let timeout;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), delay);
    };
}

// 页面加载时设置一次大小
window.addEventListener('load', function(){
    setDivSize();
    loadBall();
});

const debouncedHandleResize = function() {
    // setDivSize();
    debounce(setDivSize, 50)();
};

// 监听窗口大小变化事件，并重新设置 div 的大小
window.addEventListener('resize', debouncedHandleResize);

function generateColorGradient(steps, count) {
    if (steps < 2) {
        throw new Error("Steps must be at least 2 to create a gradient.");
    }
    // Define the key colors in the gradient
    const colors = [
        {r: 35, g: 142, b: 252}, // Light blue
        {r: 0, g: 0, b: 139},     // Dark blue
        {r: 255, g: 198, b: 38}, // Light yellow
        {r: 255, g: 165, b: 0},   // Orange
        {r: 255, g: 30, b: 30}, // Light red
        {r: 139, g: 0, b: 0}      // Dark red
    ];
    const gradient = [];
    // Helper function to interpolate between two colors
    function interpolateColor(color1, color2, factor) {
        return {
            r: Math.round(color1.r + (color2.r - color1.r) * factor),
            g: Math.round(color1.g + (color2.g - color1.g) * factor),
            b: Math.round(color1.b + (color2.b - color1.b) * factor)
        };
    }
    // Generate the gradient
    const segments = colors.length - 1;
    const stepsPerSegment = Math.floor(steps / segments);
    const remainder = steps % segments;

    for (let i = 0; i < segments; i++) {
        const startColor = colors[i];
        const endColor = colors[i + 1];
        const segmentSteps = stepsPerSegment + (i < remainder ? 1 : 0);
        for (let step = 0; step < segmentSteps; step++) {
            const factor = step / (segmentSteps - 1);
            const interpolatedColor = interpolateColor(startColor, endColor, factor);
            gradient.push(`rgb(${interpolatedColor.r}, ${interpolatedColor.g}, ${interpolatedColor.b})`);
        }
    }

    // Ensure the first and last colors are preserved
    gradient[0] = `rgb(${colors[0].r}, ${colors[0].g}, ${colors[0].b})`;
    gradient[gradient.length - 1] = `rgb(${colors[colors.length - 1].r}, ${colors[colors.length - 1].g}, ${colors[colors.length - 1].b})`;

    let result = gradient;

    // Reduce gradient to the desired count
    if (count && count < gradient.length) {
        const reducedGradient = [];
        const interval = (gradient.length - 1) / (count - 1);
        for (let i = 0; i < count; i++) {
            reducedGradient.push(gradient[Math.round(i * interval)]);
        }
        result = reducedGradient;
    }

    return result.reverse();
}

function gentRankLi(el) {
    return `<li class="ball-describe-item"> <span>${el.icon}</span> <span class="scroll-text" style="display:inline-block; overflow:hidden; width:72px; white-space: nowrap;" > ${el.name} </span> <a class="hot-tag">(${el.count})</a></li>`;
}


function freshRankDom(ulJq, list, colors) {
    // ulJq.empty();
    // let lastHeight = 1.3;
    // let lastFontSize = 0.9;
    // let bottomMargin = 0.5;
    // for(let i = 0; i < list.length; i++) {
    //     let el = list[i];
    //     let html =  gentRankLi(el);
    //     ulJq.append(html);
    //     let li = ulJq.find('li').eq(i);
    //     let hotTag = li.find('.hot-tag');
    //     li.css('background-color', colors[i]);
    //     if (i > 0) {
    //         lastFontSize = lastFontSize * 0.85;
    //         bottomMargin = bottomMargin * 0.8;
    //     }
    //     li.css('height', `${lastHeight}em`);
    //     li.css('line-height', `${lastHeight}em`);
    //     li.css('font-size', `${lastFontSize}em`);
    //     li.css('margin-bottom', `${bottomMargin}em`);
    // }

    ulJq.empty();

    for(let i = 0; i < list.length; i++) {
        let el = list[i];
        let html =  gentRankLi(el);
        ulJq.append(html);
        let li = ulJq.find('li').eq(i);
        li.css('background-color', colors[i]);
    }
}

function freshLeftRankDom(list) {
    // const colors = generateColorGradient(100, list.length);
    const colors = [
        '#b29aff',
        '#d4e9ff',
        '#ffa7a7',
        '#d5d6d8',
        '#a0b5ff',
    ];
    freshRankDom($('.ball-left-top ul'), list, colors);
}


function freshRightRankDom(list) {
    // const colors = generateColorGradient(100, list.length);
    const colors = [
        '#9adaff',
        '#d4ffea',
        '#ffdfa7',
        '#a8ff9a',
        '#ffbda0',
    ];
    freshRankDom($('.ball-right-top ul'), list, colors);
}

const defaultModelIcon= '<svg t="1752984421534" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="16726" width="26" height="26"><path d="M79.11424 270.67904l116.2496 66.9952a97.1776 97.1776 0 0 0-4.87424 30.57664v268.7232a97.53088 97.53088 0 0 0 47.39584 83.72736l219.42784 131.31264a97.28 97.28 0 0 0 15.4112 7.45984v153.84576a97.6384 97.6384 0 0 1-11.06944-5.70368L120.32 803.34848a97.51552 97.51552 0 0 1-47.44704-83.62496v-414.72c0-11.89376 2.19648-23.5008 6.2464-34.32448z m871.4752 34.32448v414.67392a97.52576 97.52576 0 0 1-47.44704 83.67104L561.8176 1007.6672c-3.6096 2.0992-7.31648 3.99872-11.06944 5.6576v-157.6448c2.28864-1.1264 4.5312-2.34496 6.72768-3.6608l219.42784-131.31264a97.53088 97.53088 0 0 0 47.44192-83.72736V368.25088a97.07008 97.07008 0 0 0-1.36192-16.384l124.3904-71.68c2.0992 7.99744 3.2256 16.3328 3.2256 24.81664zM561.8176 17.06496l336.59904 201.39008-118.2464 68.11648c-1.0752-0.70144-2.16576-1.3824-3.26656-2.048l-219.42784-131.31264a97.52576 97.52576 0 0 0-100.15744 0L248.32 278.28224l-113.76128-65.536L461.65504 17.06496a97.52576 97.52576 0 0 1 100.15744 0z" p-id="16727" fill="#1296db"></path><path d="M264.60672 365.22496L483.84 491.0336v275.98848a97.34144 97.34144 0 0 1-31.7952-12.09344L304.7936 667.1616a97.52576 97.52576 0 0 1-47.54432-83.77344V402.3808c0-12.96896 2.5856-25.5488 7.31136-37.15584h0.0512z m482.10944 37.15584v181.00736c0 34.3552-18.07872 66.18112-47.5904 83.77344l-147.21536 87.76704c-2.58048 1.51552-5.21728 2.92864-7.90016 4.2496V499.6608l201.04704-115.27168c1.11104 5.9392 1.664 11.9552 1.65888 17.99168z m-194.80576-171.54048l147.26144 87.77216a97.536 97.536 0 0 1 17.26464 13.16352l-193.39264 110.83776h-3.21536L304.29696 318.90432l0.49152-0.34304L452.0448 230.78912a97.52576 97.52576 0 0 1 99.86048 0v0.0512z" p-id="16728" fill="#1296db"></path></svg>';
const defaultAgentIcon= '<svg t="1752986414845" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="18795" width="26" height="26"><path d="M880.896 328.448c15.274667 32.768 26.197333 65.536 30.549333 109.226667 39.253333 8.789333 69.888 43.690667 69.888 85.333333v43.605333c0 41.557333-30.549333 76.544-69.973333 85.248-4.266667 43.690667-13.056 76.458667-30.464 109.226667-21.845333 41.557333-54.613333 76.544-98.304 98.389333-37.12 19.626667-76.458667 28.416-131.072 32.768a86.698667 86.698667 0 0 1-85.162667 67.754667H478.976c-41.472 0-76.458667-28.416-85.162667-67.754667-54.613333-2.218667-93.866667-13.141333-131.072-32.768A225.024 225.024 0 0 1 164.437333 761.173333a320.512 320.512 0 0 1-30.549333-109.312A88.746667 88.746667 0 0 1 64 566.613333v-43.690666c0-41.557333 30.549333-76.458667 72.106667-85.248 4.266667-43.690667 13.056-76.458667 30.549333-109.226667 21.845333-41.557333 54.613333-76.544 98.304-98.389333 45.824-26.197333 96.085333-34.986667 176.896-34.986667h37.12V107.776c0-24.064 19.626667-43.690667 43.690667-43.690667 24.064 0 43.690667 19.626667 43.690666 43.690667v87.466667h37.12c80.810667 0 131.072 8.704 179.114667 34.986666 41.472 21.76 76.458667 54.613333 98.304 98.304z m-30.634667 253.44h-4.352v-74.24c0-78.677333-10.922667-115.797333-26.197333-146.432q-26.197333-45.909333-72.106667-72.106667c-30.549333-15.36-67.669333-26.197333-146.346666-26.197333H439.637333c-78.592 0-115.712 10.922667-146.346666 26.197333q-45.824 26.197333-72.021334 72.106667c-15.36 30.634667-26.197333 67.754667-26.197333 146.432v74.24c0 80.896 10.922667 115.882667 28.330667 148.650667q26.282667 45.909333 72.106666 72.106666c30.549333 15.36 67.669333 26.197333 146.346667 26.197334h161.621333c80.810667 0 115.712-10.922667 148.48-28.330667 30.634667-17.493333 54.613333-41.557333 72.106667-72.192 15.36-30.549333 26.197333-67.669333 26.197333-146.346667zM347.904 457.386667c0-23.893333 19.626667-43.605333 43.690667-43.605334 24.064 0 43.690667 19.626667 43.690666 43.690667v174.848c0 23.978667-19.626667 43.690667-43.690666 43.690667a43.861333 43.861333 0 0 1-43.690667-43.690667v-174.933333z m262.144 0c0-23.893333 19.626667-43.605333 43.690667-43.605334 23.978667 0 43.690667 19.626667 43.690666 43.690667v174.848c0 23.978667-19.626667 43.690667-43.690666 43.690667a43.861333 43.861333 0 0 1-43.690667-43.690667v-174.933333z" p-id="18796" fill="#1296db"></path></svg>';

const defaultLeftRank = [
    {name:'通义千问', count: 90, icon: defaultModelIcon},
    {name:'文心一言', count: 55, icon: defaultModelIcon},
    {name:'智谱清言', count: 54, icon: defaultModelIcon},
    {name:'Moonshot', count: 11, icon: defaultModelIcon},
    {name:'星火', count: 50, icon: defaultModelIcon}
];

const defaultRightRank = [
    {name:'天气助手', count: 90, icon: defaultAgentIcon},
    {name:'油价助手', count: 72, icon: defaultAgentIcon},
    {name:'高铁助手', count: 11, icon: defaultAgentIcon},
    {name:'翻译助手', count: 11, icon: defaultAgentIcon},
    {name:'历史今日', count: 9, icon: defaultAgentIcon}
]

function loadLeftRank() {
    fetch(`/rank/llmHotRanking?limit=5`)
    .then(response => {
        return response.json();
    })
    .then(data => {
        if (data.status === 'success') {
            for(let el of data.data) {
                if(!el.icon) {
                    el.icon = defaultModelIcon;
                }
            }
            freshLeftRankDom(data.data);
        }
    })
    .catch((error)=>{
        freshLeftRankDom(defaultLeftRank);
        console.log("loadLeftRank error:", error);
    });
}


function loadRightRank() {
    fetch(`/rank/agentHotRanking?limit=5`)
    .then(response => {
        return response.json();
    })
    .then(data => {
        if (data.status === 'success') {
            for(let el of data.data) {
                if(!el.icon) {
                    el.icon = defaultAgentIcon;
                }
            }
            freshRightRankDom(data.data);
        }
    }).catch((error)=>{
        freshRightRankDom(defaultRightRank);
        console.log("loadRightRank error:", error);
    });
}



function loadCorners() {
    loadLeftRank();
    loadRightRank();
}

