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


const MODEL_NAV_ID = 1;
const AGENT_NAV_ID = 11;
const ORCHESTRATION_NAV_ID = 12;
const FEATURE_NAV_ID = 14;
const MINE_NAV_ID = 15;

let MODEL_NAV = null;
let AGENT_NAV = null;
let ORCHESTRATION_NAV = null;
let FEATURE_NAV = null;
let MINE_NAV = null;

let promptNavs = [
    {
        id: MODEL_NAV_ID,
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
                "id": 1010102,
                "agentId": "stock",
                "title": "股票助手",
                "templateIssues": "请简要分析一下今天上证指数的情况?"
            },
            {
                "id": 1010103,
                "agentId": "exchangeRate",
                "title": "汇率助手",
                "templateIssues": "当前美元对人民币汇率是多少?"
            },
            // {
            //     "id": 1010105,
            //     "agentId": "yiYan",
            //     "title": "文心助手",
            //     "templateIssues": "请帮我写一篇关于科技创新的文章"
            // },
            {
                "id": 1010106,
                "agentId": "yuanQi",
                "title": "元器助手",
                "templateIssues": "Dota2推荐一个适合新手玩的英雄"
            },
            // {
            //     "id": 1010107,
            //     "agentId": "xiaohongshu",
            //     "title": "红书优选",
            //     "templateIssues": "帮我为 女式瑜伽裤 写一份营销文案"
            // },
            {
                "id": 1010108,
                "agentId": "weather",
                "title": "天气助手",
                "templateIssues": "今天北京天气如何?"
            },
            {
                "id": 1010109,
                "agentId": "oil",
                "title": "油价助手",
                "templateIssues": "今天的北京油价是多少?"
            },
            {
                "id": 1010110,
                "agentId": "bmi",
                "title": "体重指数",
                "templateIssues": "我身高175cm，体重70kg，计算一下我的BMI值"
            },
            // {
            //     "id": 1010111,
            //     "agentId": "calorie",
            //     "title": "健康饮食",
            //     "templateIssues": "一份鸡胸肉的卡路里是多少?"
            // },
            {
                "id": 1010112,
                "agentId": "dishonest",
                "title": "失信查询",
                "templateIssues": "请帮我查询一下这个人的失信记录"
            },
            {
                "id": 1010113,
                "agentId": "ticket",
                "title": "高铁助手",
                "templateIssues": "从北京到上海的高铁票价是多少?"
            },
            {
                "id": 1010114,
                "agentId": "history",
                "title": "历史今日",
                "templateIssues": "今天在历史上发生了哪些重大事件?"
            },
            {
                "id": 1010115,
                "agentId": "youdao",
                "title": "有道翻译",
                "templateIssues": "请翻译‘Hello, how are you?’到中文"
            },
            {
                "id": 1010116,
                "agentId": "image",
                "title": "图像生成",
                "templateIssues": "帮我生成一副海滩风景"
            },
            // {
            //     "id": 1010117,
            //     "agentId": "KFC_text_generate",
            //     "title": "疯狂星期",
            //     "templateIssues": "帮我生成一个疯狂星期四的文案"
            // },
            {
                "id": 1010118,
                "agentId": "ip_address_lookup_agent",
                "title": "ip查询",
                "templateIssues": "帮我查一下ip为127.0.0.1的归属地在那"
            },
            // {
            //     "id": 1010119,
            //     "agentId": "anime_pictures",
            //     "title": "动漫图片",
            //     "templateIssues": "帮我搜索一张动漫图片"
            // },
            {
                "id": 1010120,
                "agentId": "constellation_luck",
                "title": "今日运势",
                "templateIssues": "帮我查查今天白羊座运势怎么样"
            },
            {
                "id": 1010121,
                "agentId": "sogou_search_pictures",
                "title": "搜狗搜图",
                "templateIssues": "帮我搜索一下刘亦菲的图片"
            },
            {
                "id": 1010122,
                "agentId": "belle_pictures",
                "title": "头像生成",
                "templateIssues": "帮我生成一张头像"
            },
            {
                "id": 1010123,
                "agentId": "baidu_search_pictures",
                "title": "百度搜图",
                "templateIssues": "帮我搜索一下关于小米SU7的图片"
            },
            {
                "id": 1010124,
                "agentId": "gold_today",
                "title": "今日金价",
                "templateIssues": "今天国内金价是多少"
            },
            // {
            //     "id": 1010125,
            //     "agentId": "jokes_generation",
            //     "title": "段子生成",
            //     "templateIssues": "帮我生成一篇段子"
            // },
            {
                "id": 1010126,
                "agentId": "meal_suggestion",
                "title": "美食推荐",
                "templateIssues": "帮我推荐今天的午餐是什么？"
            },
            {
                "id": 1010127,
                "agentId": "countdown_day",
                "title": "倒数计时",
                "templateIssues": "帮我查询下最近的倒数日是什么？"
            },
            {
                "id": 1010128,
                "agentId": "city_travel_route",
                "title": "出行路线",
                "templateIssues": "从武汉到北京的出行路线是什么？"
            },
            {
                "id": 1010129,
                "agentId": "car_query",
                "title": "查询车辆",
                "templateIssues": "帮我查询小米SU7的车况信息"
            },
            {
                "id": 1010130,
                "agentId": "surname_rank",
                "title": "姓氏排名",
                "templateIssues": "王姓在百家姓中的排名是多少？"
            },
            {
                "id": 1010131,
                "agentId": "investment_income",
                "title": "收益计算",
                "templateIssues": "我投资了2432032元，年化收益率3.425%，投资期限5年，按天计算收益是多少？"
            },
            {
                "id": 1010132,
                "agentId": "driving_license_search",
                "title": "驾考题库",
                "templateIssues": "驾驶员在高速公路上行驶时，车辆左前轮突然爆胎，须第一时间紧握转向盘，然后轻踏制动踏板进行减速，并将车停靠在紧急停车带上。这样做的原因是什么？"
            },
            {
                "id": 1010133,
                "agentId": "blood_type_calculation",
                "title": "血型预测",
                "templateIssues": "父母血型是B和AB，子代可能是什么血型？"
            },
            {
                "id": 1010134,
                "agentId": "bing_search",
                "title": "Bing搜索",
                "templateIssues": "帮我搜索关于'PearNo'的信息"
            },
            {
                "id": 1010135,
                "agentId": "lottery_results",
                "title": "彩票查询",
                "templateIssues": "查询双色球最近的开奖信息"
            },
            // {
            //     "id": 1010136,
            //     "agentId": "text_corrector",
            //     "title": "文本纠错",
            //     "templateIssues": "帮我检查并纠正以下文本：我一经吃了很多药了，可是病还不好"
            // },
            // {
            //     "id": 1010137,
            //     "agentId": "text_difference",
            //     "title": "文本对比",
            //     "templateIssues": "帮我比较两个文本的差异：奔赴新的远征 vs 我愿意开启新的征程"
            // },
            {
                "id": 1010138,
                "agentId": "place_search",
                "title": "地点搜索",
                "templateIssues": "帮我查找关于罗浮山的信息"
            },
            {
                "id": 1010139,
                "agentId": "chip_query",
                "title": "芯片查询",
                "templateIssues": "查询ESP8266的芯片参数有哪些？"
            },
            // {
            //     "id": 1010140,
            //     "agentId": "spark_dialog",
            //     "title": "星火助手",
            //     "templateIssues": "你好，能帮我做些什么？"
            // },
            {
                "id": 1010141,
                "agentId": "hot_news",
                "title": "热点新闻",
                "templateIssues": "今天的热点新闻有哪些？"
            },
            // {
            //     "id": 1010142,
            //     "agentId": "article_rewrite",
            //     "title": "文章续写",
            //     "templateIssues": "帮我续写这段文字：我爱你"
            // },
            {
                "id": 1010143,
                "agentId": "recipe_query",
                "title": "菜谱查询",
                "templateIssues": "帮我查询家常红烧鱼块的菜谱"
            },
            // {
            //     "id": 1010144,
            //     "agentId": "answer_book",
            //     "title": "答案之书",
            //     "templateIssues": "我现在应该去钓鱼吗？"
            // },
            {
                "id": 1010145,
                "agentId": "website_ping",
                "title": "测速工具",
                "templateIssues": "测试百度的Ping延迟"
            },
            // {
            //     "id": 1010146,
            //     "agentId": "text_conversion",
            //     "title": "文本转换",
            //     "templateIssues": "将`随机繁体测试内容`转为繁体"
            // },
            {
                "id": 1010147,
                "agentId": "population_data",
                "title": "人口数据",
                "templateIssues": "最新的世界人口数量是多少？"
            },
            // {
            //     "id": 1010148,
            //     "agentId": "meaning_search",
            //     "title": "诗词名言",
            //     "templateIssues": "如何树立远大志向？"
            // },
            // {
            //     "id": 1010149,
            //     "agentId": "deepseek_chat",
            //     "title": "深度问答",
            //     "templateIssues": "你好啊？"
            // },
            {
                "id": 1010150,
                "agentId": "trademark_info",
                "title": "商标查询",
                "templateIssues": "帮我查询商标 '哇哈哈' 的信息"
            },
            {
                "id": 1010151,
                "agentId": "movie_box_office",
                "title": "票房榜单",
                "templateIssues": "当前票房榜单如何？"
            },
            // {
            //     "id": 1010152,
            //     "agentId": "historical_figure_info",
            //     "title": "历史人物",
            //     "templateIssues": "帮我查询曹操的详细信息"
            // },
            {
                "id": 1010153,
                "agentId": "daily_rumor_refutation",
                "title": "辟谣前线",
                "templateIssues": "帮我查看今天的辟谣新闻"
            },
            {
                "id": 1010154,
                "agentId": "google_translate",
                "title": "谷歌翻译",
                "templateIssues": "帮我把HelloWorld翻译成中文"
            },
            // {
            //     "id": 1010155,
            //     "agentId": "couplet_generation",
            //     "title": "对联生成",
            //     "templateIssues": "天增岁月人增寿,帮我生成下联"
            // }
        ]
    },
    {
        id: ORCHESTRATION_NAV_ID,
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
                // group: 2
            },
            {
                "id": -666,
                "title": "智能生成",
            }
        ]
    },
    {
        id: FEATURE_NAV_ID,
        key: 'feature',
        icon: 'feature',
        title: '特色',
        subNavs: [
            {
                id: 14, key: SOCIAL_NAV_KEY, title: '社交接入', exampleImgSrc: '',
                exampleVedioSrc: '../video/sjjr.mp4',
                prompt: '该功能通过接入社交软件，通过RPA和大模型技术自动化社交软件的相关操作。',
                operation: '在输入框内输入您的需求（机器人、定时器、烽火台、引流涨粉），然后按照提示完成相关操作。',
                // group: 2
            }
        ]
    },
    {
        id: MINE_NAV_ID,
        key: 'mine',
        icon: 'mine',
        title: '我的',
        subNavs: [
            {
                id: 666,
                title: '我的发布',
            },
            {
                id: 667,
                title: '我的订阅',
            },
            {
                id: 668,
                title: '我的语料',
            },
            {
                id: 669,
                title: '我的模型',
            }
        ]
    }
];


for (const nav of promptNavs) {
    if(nav.id === MODEL_NAV_ID) {
        MODEL_NAV = nav;
    } else if(nav.id === AGENT_NAV_ID) {
        AGENT_NAV = nav;
    } else if(nav.id === ORCHESTRATION_NAV_ID) {
        ORCHESTRATION_NAV = nav;
    } else if(nav.id === FEATURE_NAV_ID) {
        FEATURE_NAV = nav;
    } else if(nav.id === MINE_NAV_ID) {
        MINE_NAV = nav;
    }
}


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
    let p = nav.prompt;
    let o = nav.operation;
    let html = `&nbsp;&nbsp;&nbsp;&nbsp;功能介绍：${p}<br/>&nbsp;&nbsp;&nbsp;&nbsp;操作方式: ${o}`
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
            ${genSubNavItems(nav)}  <!-- 二级菜单在这里渲染 -->
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

function genSubNavItems(nav) {
    const subNavs = nav.subNavs;
    if (!subNavs) {
        return '';
    }

    if(nav.id == MODEL_NAV_ID) {
        return genModelSubNav(subNavs);
    } else if(nav.id == AGENT_NAV_ID) {
        return genAgentsNav(subNavs);
    }else if(nav.id == ORCHESTRATION_NAV_ID) {
        return getOrchestration(subNavs);
    } else if(nav.id == FEATURE_NAV_ID) {
        return genFeatureNav(subNavs);
    } else if(nav.id == MINE_NAV_ID) {
        return genMineNav(subNavs);
    }
    return '';
}

function genModelSubNav(subNavs) {
    let subHtml = '<ul class="sub-nav hidden">';
    for (const subNav of subNavs) {
        subHtml += `
        <li class="sub-nav-item">
            <a onclick="getPromptDialog(${MODEL_NAV_ID}, ${subNav.id},event)" class="sub-nav-link flex py-2 px-2 items-center gap-3 rounded-md bg-default-50 hover:bg-default-100 dark:bg-default-900 dark:hover:bg-[#2A2B32]" data-nav-id="${subNav.id}" data-app-id="${subNav.agentId}">
                ${subNav.title}
            </a>
        </li>
        `;
    }
    subHtml += '</ul>';
    return subHtml;
}

function genAgentsNav(subNavs) {
    let subHtml = '<ul class="sub-nav hidden">';
    for (const subNav of subNavs) {
        subHtml += `
        <li class="sub-nav-item">
            <a onclick="updateSelectAgent(${subNav.id})" class="sub-nav-link flex py-2 px-2 items-center gap-3 rounded-md bg-default-50 hover:bg-default-100 dark:bg-default-900 dark:hover:bg-[#2A2B32]" data-nav-id="${subNav.id}" data-app-id="${subNav.agentId}">
                ${subNav.title}
            </a>
        </li>
        `;
    }
    subHtml += '</ul>';
    return subHtml;
}

function updateSelectAgent(agentId) {
    backToChat();
    currentAppId = agentId;
    const nav = AGENT_NAV.subNavs.find(n => n.id === agentId);
    currentNav = nav;
    if (!(nav.prompt && nav.operation)) {
        $('#queryContent').val(nav.templateIssues);
        resetBallState();
        highlightWord(nav.title);
    }
}


function getOrchestration(subNavs) {
    let subHtml = '<ul class="sub-nav hidden">';
    for (const subNav of subNavs) {
        if(subNav.id == -666) {
            console.log('subNav', subNav);
            subHtml += `
            <li class="sub-nav-item">
                <a onclick="openCreateAgent()" class="sub-nav-link flex py-2 px-2 items-center gap-3 rounded-md bg-default-50 hover:bg-default-100 dark:bg-default-900 dark:hover:bg-[#2A2B32]" data-nav-id="${subNav.id}" data-app-id="${subNav.agentId}">
                    ${subNav.title}
                </a>
            </li>
            `;
            continue;
        }
        subHtml += `
        <li class="sub-nav-item">
            <a onclick="getPromptDialog(${ORCHESTRATION_NAV_ID}, ${subNav.id},event)" class="sub-nav-link flex py-2 px-2 items-center gap-3 rounded-md bg-default-50 hover:bg-default-100 dark:bg-default-900 dark:hover:bg-[#2A2B32]" data-nav-id="${subNav.id}" data-app-id="${subNav.agentId}">
                ${subNav.title}
            </a>
        </li>
        `;
    }
    subHtml += '</ul>';
    return subHtml;
}


function genFeatureNav(subNavs) {
    let subHtml = '<ul class="sub-nav hidden">';
    for (const subNav of subNavs) {
        subHtml += `
        <li class="sub-nav-item">
            <a onclick="getPromptDialog(${FEATURE_NAV_ID}, ${subNav.id},event)" class="sub-nav-link flex py-2 px-2 items-center gap-3 rounded-md bg-default-50 hover:bg-default-100 dark:bg-default-900 dark:hover:bg-[#2A2B32]" data-nav-id="${subNav.id}" data-app-id="${subNav.agentId}">
                ${subNav.title}
            </a>
        </li>
        `;
    }
    subHtml += '</ul>';
    return subHtml;
}

function genMineNav(subNavs) {
    let subHtml = '<ul class="sub-nav hidden">';
    for (const subNav of subNavs) {
        subHtml += `
        <li class="sub-nav-item">
            <a onclick="goToUserTab(${subNav.id},event)" class="sub-nav-link flex py-2 px-2 items-center gap-3 rounded-md bg-default-50 hover:bg-default-100 dark:bg-default-900 dark:hover:bg-[#2A2B32]" data-nav-id="${subNav.id}" data-app-id="${subNav.agentId}">
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
        case 'mine':
            svg = `<svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" class="icon-sm" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
    <path d="M12 21s5-3.5 8-6c2.5-2.5 4-5 4-8A4 4 0 0 0 12 3a4 4 0 0 0-8 4c0 3 1.5 5.5 4 8 3 2.5 8 6 8 6z"></path>
</svg>
`;
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

let currentPromptDialog;

function loadModelSelect(nav) {
    if (nav &&  nav.models !== undefined && Array.isArray(nav.models)) {
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

let currentNav = null;


function showBallDiv() {
    const ballDiv = document.getElementById("ball-div");
    if (ballDiv) {
        ballDiv.style.display = "block";
    }
}

function backToChat() {
    $('#conTab').show();
    $('#mytab').hide();
    $('#queryBox').show();
    $('#introduces').show();
    $('#topTitle').show();
    $('#item-content').show();
    $('#not-content form').show();
    const activeListItems = document.querySelectorAll('#conversationsNav a.active');
    console.log(activeListItems);
    activeListItems.forEach(a => {
        console.log(a);
        a.classList.remove('active');
    });
    showBallDiv();
    $('#model-selects').empty();
    $('#model-prefences').hide();
}

function hideBallDiv() {
    const ballDiv = document.getElementById("ball-div");
    if (ballDiv) {
        ballDiv.style.display = "none";
    }
}

function getPromptDialog(id, subId) {
    // debugger
    let nav = null;
    let parentNav = null
    // 查找包含指定ID的一级导航项
    for (const pNav of promptNavs) {
        if(pNav.id != id) {
            continue
        }
        parentNav = pNav;
        for (const sNav of parentNav.subNavs) {
            if (sNav.id == subId) {
                nav = sNav;
                currentNav = nav;  // 更新当前导航为选中的子导航
                break;
            }
        }
    }
    // 如果未找到指定ID的导航项
    if (nav == null) {
        alert("找不到对应的信息");
        return;
    }

    backToChat();
    
    $('#queryContent').val('');

    loadModelSelect(nav);

    let answer = buildPromptDialogContent(nav);
    let answerJq = newRobotStartDialog('');

    // 播放视频（如果存在）
    let vedioHtml = '';
    if (nav.exampleVedioSrc) {
        vedioHtml = `
            <video controls width="100%" style="border:10px solid #238efc; border-radius:15px">
                <source src="${nav.exampleVedioSrc}" type="video/mp4" />
            </video>
        `;
    }

    // 重置计时器
    clearTimeout(timer);

    // 更新当前的提示框
    currentPromptDialog = nav;
    console.log("nav", nav)
    // 开始打字效果
    typing(0, answer, answerJq, addRobotDialog, vedioHtml);
}


function goToUserTab(id, e) {
    if (id === 666 || id == 667 || id == 668 || id == 669) {
        if (!getCookie('userId')) {
            openModal(e);
            return;
        }
    }
    $('#conTab').hide();
    $('#mytab').show();
    $('#not-content form').hide();
    let subNavs = promptNavs[4].subNavs;
    let tabs = $('#mytab .tab');
    for (let i = 0; i < subNavs.length; i++) {
        let tab = tabs[i];
        if (id == subNavs[i].id) {
            tab.style.display = 'block';
        } else {
            tab.style.display = 'none';
        }
    }
    if (id === 666) {
        loadAgentList(1);
        return;
    }

    if (id === 667) {
        loadPaidAgentList(1);
        return;
    }

    if (id == 668) {
        loadUploadFileList(1);
        return;
    }

    if (id == 669) {
        loadUserModule($('.model-modules-title')[0], 'user-finetune', 'loadFinetuneData')
        return;
    }
}


function savePerference() {
    if (!window.finger) {
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


let timer = 0;

function typing(i, str, jq, callback, ...args) {
    str += '';
    if (i <= str.length) {
        let temp = str.substring(i, i + 6);
        if (temp == '&nbsp;') {
            i += 6;
        } else {
            jq.html(str.slice(0, i++) + '<p style="display: inline-block"></p>');
            $('#item-content').scrollTop($('#item-content').prop('scrollHeight'));
        }
        timer = setTimeout(() => {
            typing(i, str, jq, callback, args)
        }, 3)
    } else {
        jq.html(str);//结束打字,移除 _ 光标
        $('#item-content').scrollTop($('#item-content').prop('scrollHeight'));
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
