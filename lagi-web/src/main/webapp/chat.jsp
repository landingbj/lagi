<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<!DOCTYPE HTML>
<html class="light" style="color-scheme: light;">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
    <title>不倒翁</title>
    <meta name="next-head-count" content="3">
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="icon" type="image/png" sizes="32x32" href="images/rj.png">
    <link rel="icon" type="image/png" sizes="16x16" href="images/rj.png">
    <meta name="description" content="A conversational AI system that listens, learns, and challenges">
    <meta property="og:title" content="联动北方">
    <meta property="og:description" content="A conversational AI system that listens, learns, and challenges">
    <meta name="react-scroll-to-bottom:version" content="4.2.0">
    <style data-emotion="react-scroll-to-bottom--css-ncqif" data-s=""></style>
    <link rel="stylesheet" href="css/pagev2.css?ver=${initParam.version}">
    <link rel="stylesheet" href="css/folder.css?ver=${initParam.version}">
    <link rel="stylesheet" href="css/chat.css?ver=${initParam.version}">
</head>
<noscript>
    <strong>We're sorry but chatai doesn't work properly without JavaScript enabled. Please enable it to
        continue.
    </strong>
</noscript>
<body class="antialiased">
<div id="pdfMask" class="pdf-mask">
    <div class="pdf-box">
        <img src="" style="width: 100%; height: auto"/>
    </div>
</div>
<div id="__next">
    <script>!function () {
        try {
            var d = document.documentElement, c = d.classList;
            c.remove('light', 'dark');
            var e = localStorage.getItem('theme');
            if ('system' === e || (!e && true)) {
                var t = '(prefers-color-scheme: dark)', m = window.matchMedia(t);
                if (m.media !== t || m.matches) {
                    d.style.colorScheme = 'dark';
                    c.add('dark')
                } else {
                    d.style.colorScheme = 'light';
                    c.add('light')
                }
            } else if (e) {
                c.add(e || '')
            }
            if (e === 'light' || e === 'dark') d.style.colorScheme = e
        } catch (e) {
        }
    }()</script>
    <div class="overflow-hidden w-full h-full relative flex z-0">

        <div id="alert-box" class="absolute w-full h-full  left-0 top-0 z-50 hidden"
             style="background-color: rgba(0, 0, 0, 0.5);">
        </div>
        <div id="confirm-box" class="absolute w-full h-full  left-0 top-0 z-50 hidden"
             style="background-color: rgba(0, 0, 0, 0.5);">
        </div>
        <!-- 左边导航条 -->
        <!-- scrollbar-trigger flex h-full w-full flex-1 items-start border-white/20 -->
        <!-- dark hidden bg-gray-900 md:fixed md:inset-y-0 md:flex md:w-[260px] md:flex-col -->
        <div id="navigation_bar"
             class="navigation_bar flex-shrink-0 overflow-x-hidden dark hidden  md:fixed md:inset-y-0 md:flex md:w-[260px]  md:flex-col">
            <div class="nav_header w-full">
                <div class="logo_container" onclick="backToHello()">
                </div>
            </div>
            <div id="nav_body" class="nav_body w-full decorated-heading">
            </div>
            <div class="nav_foot w-full decorated-heading">
                <div class="user-box">
                    <div id="forwardButton" class="share">
                        <span class='share-text'>分享</span>
                        <span><img src="images/share.png"></span>
                    </div>
                    <div class="user-info" onclick="window.open('https://saas.landingbj.com', '_blank');">
                        <div class="user-left avatar-container" id="user-img">
                            <img src="images/rj.png" alt="用户头像" class="avatar">
                            <span class="status-indicator" style="visibility: hidden;"></span>
                        </div>
                        <div class="user-middle" id="user_box">登录</div>
                        <div class="user-right relative" id="user-more">
                            <svg t="1752903400975" class="icon" viewBox="0 0 1024 1024" version="1.1"
                                 xmlns="http://www.w3.org/2000/svg" p-id="3997" width="24" height="24">
                                <path d="M512 298.6496a85.3504 85.3504 0 1 0 0-170.6496 85.3504 85.3504 0 0 0 0 170.6496z"
                                      fill="#5A5A68" p-id="3998"></path>
                                <path d="M512 512m-85.3504 0a85.3504 85.3504 0 1 0 170.7008 0 85.3504 85.3504 0 1 0-170.7008 0Z"
                                      fill="#5A5A68" p-id="3999"></path>
                                <path d="M512 896a85.3504 85.3504 0 1 0 0-170.7008 85.3504 85.3504 0 0 0 0 170.7008z"
                                      fill="#5A5A68" p-id="4000"></path>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 左边导航条 -->
        <div class="relative flex h-full max-w-full flex-1 overflow-hidden">
            <div class="flex h-full max-w-full flex-1 flex-col">
                <!-- 上部导航条 -->
                <div id="top-nav"
                     class="sticky top-0 z-10 flex items-center border-b border-white/20 bg-gray-800 pl-1 pt-1 text-gray-200 sm:pl-3 md:hidden "
                     style="background-color: #023f63;">
                    <div>
                        <button type="button" onclick="toggleUserMenu()"
                                class="-ml-0.5 -mt-0.5 inline-flex h-10 w-10 items-center justify-center rounded-md hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white dark:hover:text-white">
                            <span class="sr-only">Open sidebar</span>
                            <svg stroke="currentColor" fill="none" stroke-width="1.5" viewBox="0 0 24 24"
                                 stroke-linecap="round"
                                 stroke-linejoin="round" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg"
                                 class="h-6 w-6">
                                <line x1="3" y1="12" x2="21" y2="12"></line>
                                <line x1="3" y1="6" x2="21" y2="6"></line>
                                <line x1="3" y1="18" x2="21" y2="18"></line>
                            </svg>
                        </button>
                    </div>
                    <h1 class="flex-1 text-center text-base font-normal">新建对话</h1>
                    <button type="button" class="px-3">
                        <svg stroke="currentColor" fill="none" stroke-width="1.5" viewBox="0 0 24 24"
                             stroke-linecap="round"
                             stroke-linejoin="round" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg"
                             class="h-6 w-6">
                            <line x1="12" y1="5" x2="12" y2="19"></line>
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                        </svg>
                    </button>
                    <div id="userMenu-sm"
                         class="absolute left-0 bottom-full login-hidden mb-2 w-48 top-11 border border-gray-200 rounded-md shadow-lg">
                        <ul style="background-color: #023f63;">
                            <li class="px-4 py-2 hover:bg-gray-100 cursor-pointer" onclick="logout()">退出登录</li>
                        </ul>
                    </div>
                </div>
                <!-- 上部导航条 -->
                <!-- *************************************************主要结构********************************* -->
                <main class="relative h-full w-full transition-width overflow-hidden flex-1">
                    <!-- <div  class="w-full h-full absolute">
                </div> -->
                    <div>
                        <div id="model-prefences" class="w-full h-16 pl-10 absolute left-0 top-0" style="z-index:1000">
                            <div id="model-selects" class="inline-block model-selects float-left">
                            </div>
                            <div class="model-btns w-20 flex-1 inline-block">
                                <button id="modelClearBtn" class="inline-block" onclick="clearPreference()">重置所有
                                </button>
                                <button id="modelSaveBtn" class="inline-block" onclick="savePerference()">保存</button>
                            </div>
                        </div>
                    </div>
                    <div role="presentation" class="flex h-full">
                        <!-- 隐藏文本遮罩 -->
                        <div class="w-full h-full absolute top-14 z-10 bg-white " id="textareaMask"
                             style="display: none;">
                            <div class="w-8 h-8 bg-gray-100" style="border-radius: 24px;" onclick="hideTextareaMask()">
                                <svg class="w-8 h-8 icon" t="1703233241892" viewBox="0 0 1024 1024" version="1.1"
                                     xmlns="http://www.w3.org/2000/svg" p-id="7345" width="200" height="200">
                                    <path
                                            d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z"
                                            p-id="7346" data-spm-anchor-id="a313x.search_index.0.i2.13b03a81EFSaRX"
                                            class="selected"
                                            fill="#1296db"></path>
                                </svg>
                            </div>
                            <textarea class="w-full h-full" id="textareaCopy"></textarea>
                        </div>
                        <div id="hello-page" class="flex-1 h-full">
                            <div id="conTab" class="react-scroll-to-bottom--css-dlyqs-79elbk h-full dark:bg-gray-800">
                                <div class="react-scroll-to-bottom--css-dlyqs-1n7m0yu h-full">
                                    <div class="flex flex-col text-sm dark:bg-gray-800 h-full">
                                        <div class="flex h-full flex-col items-center justify-between">
                                            <div id="item-content" class="w-full" style="overflow: auto;">
                                                <div id="topTitle"
                                                     class="relative align-center flex w-full md:flex-col justify-center self-center ">
                                                    <div class="absolute right-2 top-2 text-gray-600 text-sm sm:none">
                                                        内容由AI协助
                                                    </div>
                                                    <div id="centerTitleBox" class="center-title-box"
                                                         style="margin-left: auto; margin-right: auto;">
                                                        <h1 class="w-full">
                                                        </h1>
                                                    </div>
                                                </div>
                                                <div id="ball-container" class="relative ball-container">
                                                    <div class="ball-mask"
                                                         style="width: min(47em, 90%); height: 0px; position: relative; margin-left: auto; margin-right: auto;">
                                                        <div class="ball-corner-container" id="ball-corner-container"
                                                             style=" z-index: 5; width: 100%;position: absolute; margin-left: auto; margin-right: auto;">
                                                        </div>
                                                    </div>
                                                    <div id="ball-div">
                                                    </div>
                                                </div>
                                                <!-- ***********************介绍框 start******************************** -->
                                                <div id="introduces" class="relative">
                                                    <div class="h-full flex gap-0 md:gap-2 justify-center">
                                                        <div class="grow">
                                                            <div
                                                                    class="absolute left-0 mb-4 flex w-full grow gap-2  sm:pb-0 md:static md:mb-0 md:max-w-none">
                                                                <div class="grid w-full grid-flow-row grid-cols-2 gap-3"></div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <!-- ***********************介绍框 end******************************** -->
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="not-content"
                         class="absolute bottom-0 left-0  border-t md:border-t-0 dark:border-white/20 md:border-transparent md:dark:border-transparent md:bg-vert-light-gradient bg-white dark:bg-gray-800 md:!bg-transparent dark:md:bg-vert-dark-gradient pt-2 md:w-[calc(100%-.5rem)]">
                        <form
                                class="stretch mx-2 flex flex-row gap-3 last:mb-2 md:mx-4 md:last:mb-6 lg:mx-auto lg:max-w-2xl xl:max-w-3xl">
                            <div class="relative flex h-full flex-1 items-stretch md:flex-col">
                                <!-- ***********************输入框******************************** -->
                                <div class="flex w-full " id="queryBox" class="border border-black/10">
                                    <!-- 智能体功能列表 -->
                                    <div
                                            class="flex flex-col w-full py-2 flex-grow md:py-3 md:pl-4 relative  bg-white dark:border-gray-900/50 dark:text-white dark:bg-gray-700 rounded-md shadow-[0_0_10px_rgba(0,0,0,0.10)] dark:shadow-[0_0_15px_rgba(0,0,0,0.10)]">
                          <textarea id="queryContent" data-agent="" tabindex="0" data-id="root" rows="1"
                                    placeholder="请输入文字..."
                                    class=" m-0 w-full resize-none border-0 bg-transparent p-0 pl-2 pr-7 focus:ring-0 focus-visible:ring-0 dark:bg-transparent md:pl-0"
                                    style="max-height: 200px; height: 170px;  width: 98%;float: left;"></textarea>

                                        <!-- <svg style="width:30px;height:24px" t="1694769456924" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8678" width="200" height="200"><path d="M801.171 483.589H544V226.418c0-17.673-14.327-32-32-32s-32 14.327-32 32v257.171H222.83c-17.673 0-32 14.327-32 32s14.327 32 32 32H480v257.17c0 17.673 14.327 32 32 32s32-14.327 32-32v-257.17h257.171c17.673 0 32-14.327 32-32s-14.327-32-32-32z" fill="" p-id="8679"></path></svg> -->
                                        <!-- ******************发送按钮开始 ***************************** -->
                                        <!-- bg-gray-900 -->
                                        <button type="button" id="addButton" style="left: 0.75rem;"
                                                class="absolute p-1 rounded-md text-gray-500   hover:bg-gray-100 dark:hover:text-gray-400 dark:hover:bg-ldbj-900 disabled:hover:bg-transparent dark:disabled:hover:bg-transparent">
                                            <img style="position: relative;" src="images/upload.png">
                                        </button>
                                        <button type="button" id="agentButton" style="left: 3.25rem;"
                                                class="absolute p-1 rounded-md text-gray-500   hover:bg-gray-100 dark:hover:text-gray-400 dark:hover:bg-ldbj-900 disabled:hover:bg-transparent dark:disabled:hover:bg-transparent">
                                            <div id="agent-container"
                                                 class="absolute flex w-full flex-1 items-stretch md:flex-col hidden">
                                              <div id="agentList" class="absolute right-1 z-50 agent-pannel w-32 "  >
                                                <div id = "agent-head" class="agent-head  pt-2 pb-1 text-center"></div>
                                                <ul id = "agent-tools" class="pb-2">
                                                  <li class=" pl-5  " >社交圈</li>
                                                  <li class=" pl-5  not-available" >视频流</li>
                                                  <li class=" pl-5  not-available" >语音流</li>
                                                  <li class=" pl-5  not-available">传感器</li>
                                                  <li class=" pl-5  not-available">工控线</li>
                                                </ul>
                                              </div>
                                            </div>
                                            <img style="position: relative;" src="images/agent_btn.png">
                                        </button>
                                        <!-- <button  type="button" id="agentButton"
                                          class="absolute p-1 rounded-md text-gray-500 bottom-1.5 right-1 md:bottom-2.5  hover:bg-gray-100 dark:hover:text-gray-400 dark:hover:bg-ldbj-900 disabled:hover:bg-transparent dark:disabled:hover:bg-transparent">
                                        <img style="position: relative;width: 25px;height: 25px;" src="images/rj.png">
                                        </button> -->
                                        <button type="button" id="voiceIcon"
                                                class="absolute p-1 rounded-md text-gray-500  right-16  hover:bg-gray-100 dark:hover:text-gray-400 dark:hover:bg-ldbj-900 disabled:hover:bg-transparent dark:disabled:hover:bg-transparent">
                                            <img style="position: relative;" src="images/vioce.png">
                                        </button>

                                        <button onclick="textQuery()" type="button" id="queryBtn"
                                                class="absolute p-1 rounded-md text-gray-500  right-1  hover:bg-gray-100 dark:hover:text-gray-400 dark:hover:bg-ldbj-900 disabled:hover:bg-transparent dark:disabled:hover:bg-transparent">
                                            <img style="position: relative;" src="images/send.png">
                                        </button>
                                        <!-- ******************发送按钮关闭***************************** -->
                                    </div>
                                </div>
                                <!-- ***********************输入框end******************************** -->
                            </div>
                        </form>
                        <div id="footer-info" class="relative">
                            <div
                                    class="relative pb-3 pt-2 text-center md:text-xs text-[10px] text-gray-600 dark:text-gray-300 md:px-[60px] md:pb-6 md:pt-3"
                                    style="max-height: 50px;">
                                <div class="lg:mx-auto lg:max-w-2xl xl:max-w-3xl"
                                     style="max-width: 60rem; max-height: 50px;">
                                    <a href="http://beian.miit.gov.cn/" target="_blank"
                                       style="display:inline-block">京ICP备&nbsp;2020046697号</a>&nbsp;&nbsp;&nbsp;&nbsp;
                                    <img style="width: 20px; height: 20px; display: inline-block;"
                                         src="images/beian.png" alt="">
                                    <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=11010802033940"
                                       target="_blank" style="display:inline-block">京公网按备号&nbsp;11010802033940</a>
                                    &nbsp;&nbsp;&nbsp;&nbsp; 联系邮箱:
                                    <a href="mailto:service@landingbj.com" style="text-decoration: none;">service@landingbj.com</a>
                                    &nbsp;&nbsp;&nbsp;&nbsp;联系电话: 027-87659116
                                    &nbsp;&nbsp;&nbsp;&nbsp;<div>Powered By <a style="color: rgb(35 142 252);"
                                                                               href="https://github.com/landingbj/lagi">Lag[i]</a>&nbsp;一种通用人工智能的实现验证
                                </div>
                                </div>
                            </div>

                            <div id="help-button"
                                 class="group fixed bottom-5 right-4 z-10 flex flex-row items-center gap-3">
                                <div class="hidden md:block">
                                    <div class="group relative" data-headlessui-state="">
                                        <button
                                                class="flex items-center justify-center rounded-full border border-gray-200 bg-gray-50 text-gray-600 dark:border-white/10 dark:bg-white/10 dark:text-gray-200"
                                                id="headlessui-menu-button-:rh:" type="button" aria-haspopup="true"
                                                aria-expanded="false"
                                                data-headlessui-state="">
                                            <div class="h-6 w-6">?</div>
                                        </button>
                                    </div>
                                </div>
                            </div>

                        </div>
                        <!-- *********************** 底部end******************************** -->
                    </div>
            </main>
        </div>
    </div>
</div>
<div class="absolute left-0 right-0 top-0 z-[2]"></div>
</div>
<div portal-container=""><span
        class="pointer-events-none fixed inset-0 z-[60] mx-auto my-2 flex max-w-[560px] flex-col items-stretch justify-start md:pb-5"></span>
</div>


<script src="libs/jquery-3.1.1.min.js"></script>
<script src="libs/fingerprint2.min.js"></script>
<script src="libs/marked.min.js"></script>
<script src="libs/echart.min.js"></script>
<script src="libs/sse.js?ver=${initParam.version}"></script>
<script src="js/common.js?ver=${initParam.version}"></script>
<script src="js/hello.js?ver=${initParam.version}"></script>
<script src="js/chat.js?ver=${initParam.version}"></script>
<script src="js/conversations.js?ver=${initParam.version}"></script>
<script src="js/nav.js?ver=${initParam.version}"></script>
<script src="js/index.js?ver=${initParam.version}"></script>
<script src="js/self.js?ver=${initParam.version}"></script>
<script src="js/query.js?ver=${initParam.version}"></script>
<script src="js/ball.js?ver=${initParam.version}"></script>
</body>
</html>