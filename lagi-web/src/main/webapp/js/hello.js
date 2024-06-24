let topTile = 'Lag<small>[</small>i<small>]</small>';

let activeModel = 0;

let models = [
    // {name:"GPT-3.5"}, {name:"GPT-4"}
]

let introTable = [
    [{title:"测试1", content:"内容1"},{title:"测试2", content:"内容2"},],
    [{title:"测试3", content:"内容3"},{title:"测试4", content:"内容4"},],
]

let agent_title = 'AI智能体';
let agent_tools = [
    {name:'社交圈', bind_func: 'socialCircles', available:true},
    {name:'视频流', bind_func: 'notifyAvailable', available:false},
    {name:'语音流', bind_func: 'notifyAvailable', available:false},
    {name:'传感器', bind_func: 'notifyAvailable', available:false},
    {name:'工控线', bind_func: 'notifyAvailable', available:false},
]

function initHelloPage() {
    initTopTile();
    initModelSlide();
    initIntroduces();
    initAgentTool();
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
    // $('#item-content').empty();

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
    $('#queryContent').val(content);
}

function addTr(ls) {
    let html = '';
    for (let i = 0; i < ls.length; i++) {
        const col = ls[i];
        html += `
        <div data-projection-id="1" style="opacity: 1; transform: none;">
        <button type="button" data-content="${col.content}" onclick="fillQueryText(this)" class="btn relative btn-neutral group w-full whitespace-nowrap rounded-xl text-left text-gray-700 shadow-[0px_1px_6px_0px_rgba(0,0,0,0.02)] dark:text-gray-300 md:whitespace-normal" as="button">
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
				$('#topTitle h1 span').html(res.data);
			}
		},
		error: function(res) {
			$('#topTitle h1 span').html(topTile);
		}
	});
}


function initAgentTool() {
    $('#agent-head').html(agent_title);
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