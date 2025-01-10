let topTile = 'Lag[i]';

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
    // $('#introduces').hide();
    // $('#modelChoices').hide();
    // $('#topTitle').hide();
    // $('#item-content').empty();
    // hideBallDiv(); // 隐藏球形 div
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
        html += `
        <div data-projection-id="1" style="opacity: 1; transform: none;">
        <button type="button" data-content="${col.content}" data-agent ="${col.agentId ? col.agentId: ""}"   onclick="fillQueryText(this)" class="btn relative btn-neutral group w-full whitespace-nowrap rounded-xl text-left text-gray-700 shadow-[0px_1px_6px_0px_rgba(0,0,0,0.02)] dark:text-gray-300 md:whitespace-normal" as="button">
                <div class="flex w-full gap-2 items-center justify-center">
                    <div class="flex w-full items-center justify-between">
                        <div class="flex flex-col overflow-hidden">
                            <div class="truncate font-semibold">
                                ${col.title}</div>
                            <div class="truncate opacity-50 item-column">
                                <p>to ${col.content}</p>
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
	$.ajax({
		type : "GET",
		url : "user/getDefaultTitle",
		success : function(res) {
			if (res.status === 'success') {
				// $('#topTitle h1 span').html(res.data);
                const a = parseInt($('#title-canvas')[0].style.width);
                const b = parseInt($('#title-canvas')[0].style.height);
                drawTitle('title-canvas', a, b, 16, 64, 16, res.data);
			}
		},
		error: function(res) {
            $('#topTitle h1 span').html(topTile);
            const a = parseInt($('#title-canvas')[0].style.width);
            const b = parseInt($('#title-canvas')[0].style.height);
            drawTitle('title-canvas', a, b, 16, 64, 16, topTile);
		}
	});
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
        line2height = 64;
        line3height = 16;
    }else if(width == 150 && height == 60) {
        line1height = 8;
        line2height = 32;
        line3height = 8;
    }
    

    
    // 定义文本内容
    const text1 = '问';
    // const text2 = 'Lag[i]';
    const text3 = '不倒翁';

    
    const color1 = '#000000'; // 黑色
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
    
    console.log("text2Width", text2Width);
    console.log(text2Width, width * dpr);
    if(text2Width > width * dpr) {
        line2height = width * dpr / (text2.length + 2);
        fontSize2 = line2height + 'px';
        ctx.font = `bold ${fontSize2} sans-serif`;
        ctx.fillStyle = color2;
        text2Width = ctx.measureText(text2).width;
        console.log(line2height, fontSize2, text2Width);
    }
    console.log("text2Width", text2Width);


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
        ctx.fillText(text1, startXLogo - line1height * 2.2, row1Top); // 调整 Y 坐标以适应字体大小
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
    // 社交接入
    for(let i = 0; i < promptNavs.length; i++) {
        let nav = promptNavs[i];
        if(nav.key == SOCIAL_NAV_KEY) {
            getPromptDialog(nav.id);
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
    const title = document.getElementById('title-canvas');
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
    let d1 =  Math.min(windowWidth * 0.8, 512) ;
    let d2 = content_h - intro_h - title_h  - ball_m_t - ball_m_b;
    
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
    ball.style.marginLeft = (item_widht - ball_d) / 2  + 12+ 'px';

    // alert(content_h  - title_h - intro_h -ball_m_t - ball_m_b - ball_d);
    initTopTile();

}


function replaceConversationAttached() {
    if($('.robot-return').length > 0) {
        console.log("replaceConversationAttached");
        let text_width =  $('.text-area')[0].offsetWidth;
        let robot_width =  $('.robot-return')[0].offsetWidth;
        let convs_width = 0;
        for(let i = 0; i < $('.conv-attached').length; i ++) {
            let dom = $('.conv-attached')[i];
            convs_width = $('.conv-attached')[i].offsetWidth;
            if(robot_width >= text_width + convs_width) {
                dom.classList.remove('justify-end');
                dom.classList.add('justify-between');
                $(dom).find('.appendVoice')[0].classList.add( "self-end", "lg:self-center", "justify-center", "mt-2", "gap-3", "md:gap-4", "lg:gap-1", "lg:absolute", "lg:top-0", "lg:translate-x-full", "lg:right-0", "lg:mt-0", "lg:pl-2", "visible");
            } else {
                dom.classList.remove('justify-between');
                dom.classList.add('justify-end');
                $(dom).find('.appendVoice')[0].classList.remove( "self-end", "lg:self-center", "justify-center", "mt-2", "gap-3", "md:gap-4", "lg:gap-1", "lg:absolute", "lg:top-0", "lg:translate-x-full", "lg:right-0", "lg:mt-0", "lg:pl-2", "visible");
            }
        }
    }
    
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



function gentRankLi(el) {
    return `<li class="ball-describe-item"> ${el.name}  <a class="hot-tag">${el.count}</a></li>`;
}


function freshRankDom(ulJq, list) {
    ulJq.empty();
    for(let i = 0; i < list.length; i++) {
        let el = list[i];
        let html =  gentRankLi(el);
        ulJq.append(html);
    }
}

function freshLeftRankDom(list) {
    freshRankDom($('.ball-left-top ul'), list);
}


function freshRightRankDom(list) {
    freshRankDom($('.ball-right-top ul'), list);
}


function loadLeftRank() {
    fetch(`/rank/llm?pageSize=3`)
    .then(response => {
        return response.json();
    })
    .then(data => {
        freshLeftRankDom(data.data);
    })
    .catch((error)=>{
        freshLeftRankDom([{name:'通义千问', count: 99}, {name:'文心一言', count: 55}, {name:'智谱清言', count: 54}]);
        console.log("loadLeftRank error:", error);
    });
}


function loadRightRank() {
    fetch(`/rank/agent?top=3`)
    .then(response => {
        return response.json();
    })
    .then(data => {
        freshRightRankDom(data.data);
    }).catch((error)=>{
        freshRightRankDom([{name:'天气助手', count: 90}, {name:'油价助手', count: 72}, {name:'高铁助手', count: 11}]);
        console.log("loadRightRank error:", error);
    });;
}



function loadCorners() {
    loadLeftRank();
    loadRightRank();
}

