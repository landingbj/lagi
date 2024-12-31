<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<!DOCTYPE HTML>
<html class="light" style="color-scheme: light;">

<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
  <title>chatai</title>
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
  <link rel="stylesheet" href="css/chat.css?ver=${initParam.version}">
  <link rel="stylesheet" href="css/login.css?ver=${initParam.version}">
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
    <!-- 左边导航条 -->
    <!-- scrollbar-trigger flex h-full w-full flex-1 items-start border-white/20 -->
    <!-- dark hidden bg-gray-900 md:fixed md:inset-y-0 md:flex md:w-[260px] md:flex-col -->
    <div id="navigation_bar"
         class="flex-shrink-0 overflow-x-hidden dark hidden bg-gray-900 md:fixed md:inset-y-0 md:flex md:w-[260px]  md:flex-col"
         style="background-color: #023f63;">
      <div class="h-full w-[260px]">
        <div class="flex h-full min-h-0 flex-col ">
          <div class="scrollbar-trigger relative h-full w-full flex-1 items-start border-white/20">
            <h2
                    style="position: absolute; border: 0px; width: 1px; height: 1px; padding: 0px; margin: -1px; overflow: hidden; clip: rect(0px, 0px, 0px, 0px); white-space: nowrap; overflow-wrap: normal;">
              历史对话</h2>
            <nav class="flex h-full w-full flex-col p-2" aria-label="Chat history">
              <div class="mb-1 flex flex-row gap-2">
                <a onclick="backToHello()"
                   class="flex px-3 min-h-[44px] py-1 items-center gap-3 transition-colors duration-200 dark:text-white cursor-pointer text-sm rounded-md border dark:border-white/20 hover:bg-gray-500/10 h-11 bg-white dark:bg-transparent flex-grow overflow-hidden">
                  <!-- <svg
                    stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round"
                    stroke-linejoin="round" class="icon-sm shrink-0" height="1em" width="1em"
                    xmlns="http://www.w3.org/2000/svg">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                  </svg> -->
                  <svg t="1697600043545" class="icon" viewBox="0 0 1024 1024" version="1.1"
                       xmlns="http://www.w3.org/2000/svg" p-id="4283" width="20" height="20">
                    <path d="M927.999436 531.028522a31.998984 31.998984 0 0 0-31.998984 31.998984c0 51.852948-10.147341 102.138098-30.163865 149.461048a385.47252 385.47252 0 0 1-204.377345 204.377345c-47.32295 20.016524-97.6081 30.163865-149.461048 30.163865s-102.138098-10.147341-149.461048-30.163865a385.47252 385.47252 0 0 1-204.377345-204.377345c-20.016524-47.32295-30.163865-97.6081-30.163865-149.461048s10.147341-102.138098 30.163865-149.461048a385.47252 385.47252 0 0 1 204.377345-204.377345c47.32295-20.016524 97.6081-30.163865 149.461048-30.163865a387.379888 387.379888 0 0 1 59.193424 4.533611l-56.538282 22.035878A31.998984 31.998984 0 1 0 537.892156 265.232491l137.041483-53.402685a31.998984 31.998984 0 0 0 18.195855-41.434674L639.723197 33.357261a31.998984 31.998984 0 1 0-59.630529 23.23882l26.695923 68.502679a449.969005 449.969005 0 0 0-94.786785-10.060642c-60.465003 0-119.138236 11.8488-174.390489 35.217667a449.214005 449.214005 0 0 0-238.388457 238.388457c-23.361643 55.252253-35.22128 113.925486-35.22128 174.390489s11.8488 119.138236 35.217668 174.390489a449.214005 449.214005 0 0 0 238.388457 238.388457c55.252253 23.368867 113.925486 35.217667 174.390489 35.217667s119.138236-11.8488 174.390489-35.217667A449.210393 449.210393 0 0 0 924.784365 737.42522c23.368867-55.270316 35.217667-113.925486 35.217667-174.390489a31.998984 31.998984 0 0 0-32.002596-32.006209z"
                          fill="#e6e6e6" p-id="4284"></path>
                  </svg>
                  <span class="truncate">返回欢迎页</span>
                </a>
                <span class="" data-state="closed">
                      <a
                              class="flex px-3 min-h-[44px] py-1 gap-3 transition-colors duration-200 dark:text-white cursor-pointer text-sm rounded-md border dark:border-white/20 hover:bg-gray-500/10 h-11 w-11 flex-shrink-0 items-center justify-center bg-white dark:bg-transparent"><svg
                              stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24"
                              stroke-linecap="round"
                              stroke-linejoin="round" class="icon-sm" height="1em" width="1em"
                              xmlns="http://www.w3.org/2000/svg">
                          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                          <line x1="9" y1="3" x2="9" y2="21"></line>
                        </svg><span
                              style="position: absolute; border: 0px; width: 1px; height: 1px; padding: 0px; margin: -1px; overflow: hidden; clip: rect(0px, 0px, 0px, 0px); white-space: nowrap; overflow-wrap: normal;">Close
                          sidebar</span></a></span></div>
              <div
                      class="absolute left-0 top-14 z-20 overflow-hidden transition-all duration-500 invisible max-h-0">
                <div class="bg-gray-900 px-4 py-3">
                  <div class="p-1 text-sm text-gray-100">Chat History is off for this browser.
                  </div>
                  <div class="p-1 text-xs text-gray-500">When history is turned off, new chats on
                    this browser won't appear in your history on any of your devices, be used to
                    train our models, or stored for longer than 30 days. <strong>This setting
                      does not sync across browsers or devices.</strong> <a
                            href="https://help.openai.com/en/articles/7730893" target="_blank"
                            class="underline"
                            rel="noreferrer">Learn more</a></div>
                  <button class="btn relative btn-primary mt-4 w-full">
                    <div class="flex w-full gap-2 items-center justify-center">
                      <svg stroke="currentColor"
                           fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round"
                           stroke-linejoin="round" class="icon-sm" height="1em" width="1em"
                           xmlns="http://www.w3.org/2000/svg">
                        <path d="M18.36 6.64a9 9 0 1 1-12.73 0"></path>
                        <line x1="12" y1="2" x2="12" y2="12"></line>
                      </svg>
                      Enable chat history
                    </div>
                  </button>
                </div>
                <div class="h-24 bg-gradient-to-t from-gray-900/0 to-gray-900"></div>
              </div>
              <div class="flex-col flex-1 transition-opacity duration-500 -mr-2 pr-2 overflow-y-auto">
                <div id="conversationsNav"
                     class="flex flex-col gap-2 pb-2 dark:text-gray-100 text-gray-800 text-sm">
                </div>
              </div>
              <div class="border-t border-black/20 pt-2 empty:hidden dark:border-white/20">
                <a id="forwardButton"
                   class="flex px-3 min-h-[44px] py-1 items-center gap-3 transition-colors duration-200 dark:text-white cursor-pointer text-sm hover:bg-gray-100 dark:hover:bg-gray-800 rounded-md"><span
                        class="flex w-full flex-row flex-wrap-reverse justify-between"><span
                        class="gold-new-button flex items-center gap-3"><svg stroke="currentColor"
                                                                             fill="none"
                                                                             stroke-width="2"
                                                                             viewBox="0 0 24 24"
                                                                             stroke-linecap="round"
                                                                             stroke-linejoin="round"
                                                                             class="icon-sm shrink-0"
                                                                             height="1em" width="1em"
                                                                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                            <circle cx="12" cy="7" r="4"></circle>
                          </svg>分享给朋友</span>
                  <!-- bg-yellow-200 -->
                        <div class="rounded-md  px-1.5 py-0.5 text-xs font-medium uppercase text-gray-800 relative">
                          <div
                                  class=" absolute inset-y-0 right-0 w-8 z-10 bg-gradient-to-l dark:from-default-900 from-gray-50 group-hover:from-gray-100 dark:group-hover:from-[#2A2B32]">
                            <img src="images/forward.png" style="width: 100%; height: 100%;" alt="">
                          </div>
                        </div>
                      </span>
                </a>

                <div class="group relative" data-headlessui-state="">
                  <div id="userMenu"
                       class="absolute right-0 bottom-full mb-2 w-48 bg-white login-hidden border border-gray-200 rounded-md shadow-lg">
                    <ul>
                      <li class="px-4 py-2 hover:bg-gray-100 cursor-pointer" onclick="logout()">
                        退出登录
                      </li>
                    </ul>
                  </div>
                  <button
                          onclick="toggleUserMenu(event)"
                          class="flex w-full items-center gap-3 rounded-md px-3 py-3 text-sm transition-colors duration-200 hover:bg-gray-100 group-ui-open:bg-gray-100 dark:hover:bg-gray-800 dark:group-ui-open:bg-gray-800"
                          type="button" aria-haspopup="true" aria-expanded="false" data-state="closed"
                          id="headlessui-menu-button-:rc:" data-headlessui-state="">
                    <div class="flex-shrink-0">
                      <div class="flex items-center justify-center rounded">
                        <div class="relative flex">
                          <img alt="User" loading="lazy" width="36" height="36"
                               decoding="async" data-nimg="1"
                               class="rounded-sm" srcset="" src="images/rj.png"
                               style="color: transparent;">
                        </div>
                      </div>
                    </div>
                    <div
                            class="grow overflow-hidden text-ellipsis whitespace-nowrap text-left text-gray-700 dark:text-white">
                      <!-- <div class="font-semibold">游客</div> -->
                      <div id="user_box" class="font-semibold">
                        登录
                      </div>
                      <div class="text-xs text-gray-500"></div>
                    </div>
                    <svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24"
                         stroke-linecap="round" stroke-linejoin="round"
                         class="icon-sm flex-shrink-0 text-gray-500"
                         height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                      <circle cx="12" cy="12" r="1"></circle>
                      <circle cx="19" cy="12" r="1"></circle>
                      <circle cx="5" cy="12" r="1"></circle>
                    </svg>
                  </button>
                </div>
              </div>
            </nav>
          </div>
        </div>
      </div>
    </div>
    <!-- 左边导航条 -->
    <div class="relative flex h-full max-w-full flex-1 overflow-hidden">
      <div class="flex h-full max-w-full flex-1 flex-col">
        <!-- 上部导航条 -->
        <div class="sticky top-0 z-10 flex items-center border-b border-white/20 bg-gray-800 pl-1 pt-1 text-gray-200 sm:pl-3 md:hidden "
             style="background-color: #023f63;">
          <div>
            <button type="button" onclick="toggleUserMenu()"
                    class="-ml-0.5 -mt-0.5 inline-flex h-10 w-10 items-center justify-center rounded-md hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white dark:hover:text-white">
              <span class="sr-only">Open sidebar</span>
              <svg stroke="currentColor" fill="none" stroke-width="1.5" viewBox="0 0 24 24"
                   stroke-linecap="round" stroke-linejoin="round" height="1em" width="1em"
                   xmlns="http://www.w3.org/2000/svg" class="h-6 w-6">
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
              </svg>
            </button>
          </div>
          <h1 class="flex-1 text-center text-base font-normal">新建对话</h1>
          <button type="button" class="px-3">
            <svg stroke="currentColor" fill="none" stroke-width="1.5" viewBox="0 0 24 24"
                 stroke-linecap="round" stroke-linejoin="round" height="1em" width="1em"
                 xmlns="http://www.w3.org/2000/svg" class="h-6 w-6">
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
          <div  class="w-full h-full absolute">
            <div id="alert-box" class="w-full h-full relative left-0 top-0 z-50 hidden" style="background-color: rgba(0, 0, 0, 0.5);">
            </div>
          </div>
          <div>
            <div id="model-prefences" class="w-full h-16 pl-10">
              <div id="model-selects" class="inline-block model-selects  float-left">
              </div>
              <div class="model-btns w-20 flex-1 inline-block">
                <button id="modelClearBtn" class="inline-block" onclick="clearPreference()">重置所有
                </button>
                <button id="modelSaveBtn" class="inline-block" onclick="savePerference()">保存</button>
                <%--                    <button id = "test" class="inline-block" onclick="matchingAgents()">匹配智能体</button>--%>
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
                  <path d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z"
                        p-id="7346" data-spm-anchor-id="a313x.search_index.0.i2.13b03a81EFSaRX"
                        class="selected" fill="#1296db"></path>
                </svg>
              </div>
              <textarea class="w-full h-full" id="textareaCopy"></textarea>
            </div>

            <div id="hello-page" class="flex-1 h-full overflow-hidden">
              <div class="react-scroll-to-bottom--css-dlyqs-79elbk h-full dark:bg-gray-800">
                <div class="react-scroll-to-bottom--css-dlyqs-1n7m0yu h-full">
                  <div class="flex flex-col text-sm dark:bg-gray-800 h-full">
                    <div class="flex h-full flex-col items-center justify-between">
                      <div id="modelChoices"
                           class="px-2 w-full flex flex-col py-2 md:py-6 sticky top-0">
                        <div class="relative flex flex-col items-stretch justify-center gap-2 sm:items-center">
                          <div class="relative flex rounded-xl bg-gray-100 p-1 text-gray-900 dark:bg-gray-900">
                            <ul class="flex w-full list-none gap-1 sm:w-auto">
                            </ul>
                          </div>
                        </div>
                      </div>
                      <!-- 我的发布列表开始 -->
                      <div class="agent-list-container" class="w-full" id="agent-list-container">

                        <%--                        <button onclick="openAgentModal()">新增智能体</button>--%>
                        <%-- <span class="close-btn" onclick="closeAgentList()">&times;</span>--%>
                        <table id="agent-list" border="1">
                          <thead>
                          <tr>
                            <th>智能体名称</th>
                            <th>智能体平台</th>
                            <th>智能体Token</th>
                            <th>智能体App ID</th>
                            <th>是否收费</th>
                            <th>每次请求收费(元)</th>
                            <th>发布状态</th>
                            <th>收益(元)</th>
                            <th>调用次数</th>
                            <th>订阅者人数</th>
                            <th>操作</th>
                          </tr>
                          </thead>
                          <tbody>
                          <%-- 动态生成的智能体数据行 --%>
                          </tbody>
                        </table>

                        <div class="pagination" id="pagination">
                          <!-- 分页按钮将动态添加到这里 -->
                        </div>
                      </div>
                      <!-- 我的发布列表结束 -->
                      <!-- 我的订阅列表开始 -->
                      <div class="paid-agent-list-container w-full"
                           id="paid-agent-list-container">
                        <table id="paid-agent-list" border="1">
                          <thead>
                          <tr>
                            <th>智能体名称</th>
                            <th>智能体平台</th>
                            <th>每次请求收费(元)</th>
                            <th>余额</th>
                          </tr>
                          </thead>
                          <tbody>
                          <!-- 动态生成的智能体数据行 -->
                          </tbody>
                        </table>
                        <div class="pagination" id="paid-agent-pagination">
                          <!-- 分页按钮将动态添加到这里 -->
                        </div>
                      </div>
                      <!-- 我的订阅列表结束 -->

                      <div id="item-content" class="w-full" style="overflow: auto;">
                        <div id="topTitle" class="align-center flex w-full flex-col justify-center self-center px-2 pb-2 md:pb-[4vh]">
                          <div class="absolute right-2 top-2 text-gray-600 text-sm sm:none">
                            仅限邀请内测
                          </div>
                          <div id="centerTitleBox" style="margin-top: 1rem; margin-left: auto; margin-right: auto; width: 18rem; height: 80rem; ">
                            <div class="top-title-box" style="position: relative; height: 2rem;  width: 100%; font-size: 1rem;">
                              <div style="color: black; width: 1rem; margin-left: calc(70% - 3rem); font-weight: bold; font-family: 'fangsong', SimSun, serif;">问</div>
                            </div>
                            <div class="mid-title-box" style="width: 80%;  height:  4rem;  margin-left: auto; margin-right: auto;">
                              <div style="display: flex; height: 4rem;">
                                <h1 id="systemName"
                                    style="color: #838383; width: calc(100% - 4rem)">
                                  <span></span>
                                </h1>
                                <img style="width: 4rem;  height: calc(100%-0.5rem);" src="images/bdw.png">
                              </div>
                            </div>
                            <div class="foot-title-box" style="font-weight: bold; color: #838383;position: relative;display: flex; width: 80%; font-size: 1.0rem; margin-left: auto; margin-right: auto;">
                              <!--                              不倒翁-->
                              <div style="width: 70%; display: flex; ">
                                <div style="width: 60%; padding-left: 5px">不</div>
                                <div style="width: 40%;">倒</div>
                              </div>
                              <div style="width: 30%;height: 1rem;" class="text-center">
                                翁
                              </div>
                            </div>
                          </div>

                        </div>
                        <div id="ball-div">
                        </div>
                        <!-- ***********************输入框前form******************************** -->
                        <div id="introduces">
                          <div class="h-full flex ml-1 md:w-full md:m-auto md:mb-4 gap-0 md:gap-2 justify-center">
                            <div class="grow">
                              <div class="absolute  left-0 mb-4 flex w-full grow gap-2 px-1 pb-1 sm:px-2 sm:pb-0 md:static md:mb-0 md:max-w-none">
                                <div class="grid w-full grid-flow-row grid-cols-2 gap-3"></div>
                              </div>
                            </div>
                          </div>
                        </div>
                        <!-- ***********************输入框前form end******************************** -->
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div id="not-content"
                 class="absolute bottom-0 left-0  border-t md:border-t-0 dark:border-white/20 md:border-transparent md:dark:border-transparent md:bg-vert-light-gradient bg-white dark:bg-gray-800 md:!bg-transparent dark:md:bg-vert-dark-gradient pt-2 md:pl-2 md:w-[calc(100%-.5rem)]">
              <form
                      class="stretch mx-2 flex flex-row gap-3 last:mb-2 md:mx-4 md:last:mb-6 lg:mx-auto lg:max-w-2xl xl:max-w-3xl">
                <div class="relative flex h-full flex-1 items-stretch md:flex-col">
                  <!-- ***********************输入框******************************** -->
                  <div class="flex w-full " id="queryBox">
                    <div id="agent-container"
                         class="absolute flex w-full flex-1 items-stretch md:flex-col hidden">
                      <div id="agentList" class="absolute right-1 z-50 agent-pannel w-32 ">
                        <div id="agent-head" class="agent-head  pt-2 pb-1 text-center">智能体
                        </div>
                        <!-- <ul id = "agent-tools" class="pb-2" style="max-height: 100px; overflow: auto"> -->
                        <ul id="agent-tools" class="pb-2">
                          <li class=" pl-5  not-available " onclick="openAgentModal(event)">发布智能体</li>
                          <%--                                                    <li class=" pl-5  not-available " onclick="showQrCode(688841, 4, 0.01)">测试收款码</li>--%>
                          <%--                                                    <li class="pl-5 not-available" onclick="openRechargeModal('recharge-modal')">测试弹框</li>--%>
                        </ul>
                      </div>
                    </div>

                    <div class="flex-col-reverse w-10 m-0 relative">

                      <div class="absolute top-0  ml-2" style="display: none;"
                           id="textareaScretch" onclick="showTextareaMask()">
                        <svg style="width: 24px;height: 24px;" t="1703232823101" class="icon"
                             viewBox="0 0 1024 1024" version="1.1"
                             xmlns="http://www.w3.org/2000/svg" p-id="6337" width="200"
                             height="200">
                          <path d="M838.116 732.779 877.7 693.195 511.979 327.549 146.3 693.195 185.883 732.779 512.003 406.652Z"
                                p-id="6338" fill="#1296db"></path>
                        </svg>
                        <!-- 向下 -->
                        <!-- <svg t="1703233241892" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7345" width="200" height="200"><path d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z" p-id="7346" data-spm-anchor-id="a313x.search_index.0.i2.13b03a81EFSaRX" class="selected" fill="#1296db"></path></svg> -->
                      </div>
                      <div id="voiceIcon" class="absolute bottom-0 ml-2">
                        <svg id="voiceSvg"
                             style="width: 24px;height: 24px; margin-bottom: 5px;"
                             t="1694870288752" class="icon" viewBox="0 0 1024 1024"
                             version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4046"
                             width="200" height="200">
                          <path
                                  d="M446.2 705.9c-99.1 0-180.2-81.1-180.2-180.2V245.4c0-99.1 81.1-180.2 180.2-180.2s180.2 81.1 180.2 180.2v280.3c0 99.1-81.1 180.2-180.2 180.2z"
                                  fill="#D6F3FA" p-id="4047"></path>
                          <path
                                  d="M443.7 832.1c-3 12.8-16.3 20.2-28.9 16.4-132.1-41-224.8-158-234.6-296.2-0.9-13.1 9.3-24.4 22.5-24.4 11.7 0 21.5 8.9 22.3 20.6 8.3 120.1 88.9 221.8 203.8 257.2 11.1 3.4 17.6 15 14.9 26.4zM820.8 528c-11.6 0-21.5 8.9-22.3 20.5-10.4 148.1-132.3 265.7-282 269.8-0.5 0-1-0.3-1.5-0.3-0.9 0-1.7 0.4-2.5 0.5-1.3 0-2.6 0.2-3.9 0.2v0.6c-9.2 2.8-16 11.1-16 21.2v98.4c0 12.4 10.1 22.5 22.5 22.5s22.5-10.1 22.5-22.5v-76.6c163.5-14 294-145.7 305.8-309.8 0.8-13.2-9.5-24.5-22.6-24.5z m-503.3 35.3v-267C317.5 187.5 406 99 514.8 99s197.3 88.5 197.3 197.3v267c0 108.8-88.5 197.3-197.3 197.3s-197.3-88.5-197.3-197.3z m51.5 0c0 80.4 65.4 145.8 145.8 145.8s145.8-65.4 145.8-145.8v-267c0-80.4-65.4-145.8-145.8-145.8S369 215.9 369 296.3v267z m45.6-157.7h75.2c12.4 0 22.5-10.1 22.5-22.5s-10.1-22.5-22.5-22.5h-75.2c-12.4 0-22.5 10.1-22.5 22.5 0.1 12.4 10.2 22.5 22.5 22.5z m75.2 68.6c12.4 0 22.5-10.1 22.5-22.5s-10.1-22.5-22.5-22.5h-75.2c-12.4 0-22.5 10.1-22.5 22.5s10.1 22.5 22.5 22.5h75.2z"
                                  fill="#18BAE5" p-id="4048"></path>
                        </svg>
                      </div>

                    </div>

                    <div
                            class="flex flex-col w-full py-2 flex-grow md:py-3 md:pl-4 relative border border-black/10 bg-white dark:border-gray-900/50 dark:text-white dark:bg-gray-700 rounded-md shadow-[0_0_10px_rgba(0,0,0,0.10)] dark:shadow-[0_0_15px_rgba(0,0,0,0.10)]">
                          <textarea id="queryContent" data-agent="" tabindex="0" data-id="root" rows="1"
                                    placeholder="请输入文字..."
                                    class=" m-0 w-full resize-none border-0 bg-transparent p-0 pl-2 pr-7 focus:ring-0 focus-visible:ring-0 dark:bg-transparent md:pl-0"
                                    style="max-height: 200px; height: 24px;  width: 98%;float: left;"></textarea>

                      <!-- <svg style="width:30px;height:24px" t="1694769456924" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8678" width="200" height="200"><path d="M801.171 483.589H544V226.418c0-17.673-14.327-32-32-32s-32 14.327-32 32v257.171H222.83c-17.673 0-32 14.327-32 32s14.327 32 32 32H480v257.17c0 17.673 14.327 32 32 32s32-14.327 32-32v-257.17h257.171c17.673 0 32-14.327 32-32s-14.327-32-32-32z" fill="" p-id="8679"></path></svg> -->
                      <!-- ******************发送按钮开始 ***************************** -->
                      <!-- bg-gray-900 -->
                      <button onclick="textQuery()" type="button" id="queryBtn"
                              class="absolute p-1 rounded-md text-gray-500 bottom-1.5 right-1 md:bottom-2.5 md:right-2 hover:bg-gray-100 dark:hover:text-gray-400 dark:hover:bg-ldbj-900 disabled:hover:bg-transparent dark:disabled:hover:bg-transparent">
                        <img style="position: relative;height: 25px;top: 1px;"
                             src="images/rj.png">
                      </button>
                      <!-- ******************发送按钮关闭***************************** -->
                    </div>

                    <div id="agentButton" class=" flex-col-reverse m-2 flex-bottom">
                      <img style="width: 28px;height: 28px; float: left;right: 100px; object-fit: contain; margin-bottom: 0px;"
                           t="1694871462493" class=" icon"
                           alt="agent"
                           src="images/agent.png"
                      />
                    </div>

                    <div id="addButton" class=" flex-col-reverse m-2 flex-bottom">
                      <img style="width: 28px;height: 28px; float: left;right: 100px; object-fit: contain; margin-bottom: 0px;"
                           t="1694871462493" class=" icon"
                           alt="agent"
                           src="images/wjsc.png"
                      />
                    </div>
                  </div>
                  <!-- ***********************输入框end******************************** -->
                </div>
              </form>

              <!-- *********************** 底部******************************** -->
              <div id="footer-info" class="relative pb-3 pt-2 text-center md:text-xs text-[10px] text-gray-600 dark:text-gray-300 md:px-[60px] md:pb-6 md:pt-3 ">
                <div class="lg:mx-auto lg:max-w-2xl xl:max-w-3xl" style="max-width: 60rem;">
                  <a href="http://beian.miit.gov.cn/" target="_blank" style="display:inline-block">京ICP备&nbsp;2020046697号</a>&nbsp;&nbsp;&nbsp;&nbsp;
                  <img style="width: 20px; height: 20px; display: inline-block;" src="images/beian.png" alt="">
                  <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=11010802033940" target="_blank" style="display:inline-block">京公网按备号&nbsp;11010802033940</a>
                  &nbsp;&nbsp;&nbsp;&nbsp; 联系邮箱:
                  <a href="mailto:service@landingbj.com" style="text-decoration: none;">service@landingbj.com</a>
                  &nbsp;&nbsp;&nbsp;&nbsp;联系电话: 027-87659116
                  &nbsp;&nbsp;&nbsp;&nbsp;<div>内容由AI生成&nbsp;一种通用人工智能的实现验证</div>
                </div>
              </div>

              <!-- *********************** 底部end******************************** -->
            </div>
          </div>

          <!-- ************************************底部 ？ == help *********************************** -->
          <div id="help-button" class="group fixed bottom-5 right-4 z-10 flex flex-row items-center gap-3">
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
          <!-- ************************************底部 help end *********************************** -->

        </main>
      </div>
    </div>
  </div>
  <div class="absolute left-0 right-0 top-0 z-[2]"></div>
</div>
<div portal-container=""><span
        class="pointer-events-none fixed inset-0 z-[60] mx-auto my-2 flex max-w-[560px] flex-col items-stretch justify-start md:pb-5"></span>
</div>

<!-- 登录注册弹框开始 -->
<div id="overlay" class="overlay">
  <div class="modal" id="login-form">
    <div class="close-modal-container" style="position: absolute; right: 10px; z-index: 100;">
      <button class="close-modal" onclick="closeModal()"
              style="padding: 10px 15px; font-size: 24px; background-color: transparent; border: none; color: #333; cursor: pointer; transition: color 0.3s;">
        &times;
      </button>
    </div>
    <h2 style="display: flex; align-items: center; position: relative; width: 100%; justify-content: center;">
      <img src="images/Small_logo.png" alt="公司 Logo" class="modal-Small_logo" style="max-height: 50px;">
    </h2>
    <div style="display: flex; flex-direction: column; align-items: center; width: 80%; max-width: 350px; margin: 0 auto;">
      <input type="text" placeholder="用户名" id="login-username"
             style="width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; box-sizing: border-box;">
      <input type="password" placeholder="密码" id="login-password"
             style="width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; box-sizing: border-box;">
      <div class="captcha-container" style="display: flex; align-items: center; width: 100%;">
        <input type="text" placeholder="验证码" id="login-captcha"
               style="flex: 1; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; box-sizing: border-box;">
        <img alt="验证码" class="login-captcha-image" onclick="updateCaptcha(this)"
             style="width: 30%; margin-left: 10px; cursor: pointer;">
      </div>
    </div>
    <div id="login-error" class="error-message">登录失败，请检查输入信息</div>
    <div style="display: flex; justify-content: center; gap: 15px; width: 80%; max-width: 350px; margin: 0 auto; margin-top: 20px; align-items: center;">
      <button onclick="submitLogin()"
              style="padding: 10px 20px; background-color: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; width: 100%;">
        登录
      </button>
      <button onclick="showRegisterPage()"
              style="padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; width: 100%;">
        注册
      </button>
    </div>
  </div>
  <div class="modal login-hidden" id="register-form">
    <div class="close-modal-container" style="position: absolute; right: 10px; z-index: 100;">
      <button class="close-modal" onclick="closeModal()"
              style="padding: 10px 15px; font-size: 24px; background-color: transparent; border: none; color: #333; cursor: pointer; transition: color 0.3s;">
        &times;
      </button>
    </div>
    <h2 style="display: flex; align-items: center; position: relative; width: 100%; justify-content: center;">
      <img src="images/Small_logo.png" alt="公司 Logo" class="modal-Small_logo" style="max-height: 50px;">
    </h2>
    <div style="display: flex; flex-direction: column; align-items: center; width: 80%; max-width: 350px; margin: 0 auto;">
      <input type="text" placeholder="用户名" id="register-username"
             style="width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; box-sizing: border-box;">
      <input type="password" placeholder="密码" id="register-password"
             style="width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; box-sizing: border-box;">
      <input type="password" placeholder="确认密码" id="register-confirm-password"
             style="width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; box-sizing: border-box;">
      <div class="captcha-container" style="display: flex; align-items: center; width: 100%;">
        <input type="text" placeholder="验证码" id="register-captcha"
               style="flex: 1; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; box-sizing: border-box;">
        <img alt="验证码" class="register-captcha-image" onclick="updateCaptcha(this)"
             style="width: 30%; margin-left: 10px; cursor: pointer;">
      </div>
    </div>
    <div id="register-error" class="error-message">注册失败，请检查输入信息</div>
    <div style="display: flex; justify-content: center; gap: 15px; width: 80%; max-width: 350px; margin: 0 auto; margin-top: 20px; align-items: center;">
      <button onclick="submitRegister()"
              style="padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; width: 100%;">
        注册
      </button>
      <button onclick="showLoginPage()"
              style="padding: 10px 20px; background-color: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; width: 100%;">
        返回登录
      </button>
    </div>
  </div>
</div>
<!-- 登录注册弹框结束 -->

<!-- 新增/编辑智能体弹窗开始 -->
<div id="agent-form-modal" class="modal">
  <div class="modal-content">
    <span class="close-btn" onclick="closeAgentModal()">&times;</span>
    <form id="agent-form">
      <label for="agent-name">智能体名称：</label>
      <input type="text" id="agent-name" required>
      <label for="platform">智能体平台：</label>
      <select id="platform" required>
        <option value="ai.agent.chat.qianfan.XiaoxinAgent">千帆</option>
        <option value="ai.agent.chat.coze.CozeAgent">扣子</option>
        <option value="ai.agent.chat.tencent.YuanQiAgent">元器</option>
        <option value="ai.agent.chat.zhipu.ZhipuAgent">智谱</option>
      </select>
      <label for="token">智能体Token：</label>
      <input type="text" id="token" required>
      <label for="app-id">智能体App ID：</label>
      <input type="text" id="app-id" required>
      <div style="display: flex; align-items: center;">
        <label>是否收费：</label>
        <label for="isFeeRequiredYes" style="margin-right: 5px;">是</label>
        <input type="radio" id="isFeeRequiredYes" name="isFeeRequired" value="true"
               style="width: 1px; margin-bottom: -0.4em;">

        <label for="isFeeRequiredNo" style="margin-right: 5px; padding-left: 10px;">否</label>
        <input type="radio" id="isFeeRequiredNo" name="isFeeRequired" value="false"
               style="width: 1px; margin-bottom: -0.4em;" checked>
      </div>
      <div id="pricePerReqContainer" style="display: none;">
        <label for="pricePerReq">每次请求收费(元)：</label>
        <input type="number" id="pricePerReq" step="0.01" min="0">
      </div>
      <button type="button" onclick="saveAgent()">发布</button>
    </form>
  </div>
</div>
<!-- 新增/编辑智能体弹窗结束 -->

<!-- 微信支付弹窗开始 -->
<div id="wechat_pay_qr" style="display: none;">
  <div class="qrCodeDiv">
    <!-- 添加公司logo -->
    <div class="companyLogoDiv">
      <img class="company_logo" src="images/company_logo.png" alt="公司 Logo"/>
    </div>
    <div class="qrDiv">
      <img id="qrCode" src=""/>
    </div>
    <div id="wechatTitle">
      <img class="wechat_logo" src="images/wechat_logo.png"/>
      <div class="wechat_label">
        微信支付 <span id="payAmount" class="orangeFont"></span> 元
      </div>
    </div>
    <!-- 添加取消支付按钮 -->
    <button id="cancelPaymentBtn" onclick="cancelPayment()">取消支付</button>
  </div>
</div>
<!-- 微信支付弹窗结束 -->

<!-- 查看收费智能体弹窗开始 -->
<div class="recharge-overlay" id="recharge-modal">
  <div class="recharge-modal">
    <button class="recharge-close-btn" id="recharge-close-btn" onclick="closeRechargeModal()">&times;</button>

    <div class="recharge-modal-header">
      <h2 class="recharge-agent-name">智能体名称：智能体A</h2>
    </div>

    <div class="recharge-modal-body">
      <div class="recharge-left">
        <p>单次调用价格</p>
        <span class="recharge-price">¥ 0.01</span>
      </div>

      <div class="recharge-right">
        <label for="recharge-call-count">可用次数</label>
        <input type="number" id="recharge-call-count" value="1" min="1" step="1">
      </div>
    </div>

    <div class="recharge-footer">
      <button class="recharge-subscribe-btn" id="recharge-subscribe-btn" onclick="subscription()">
        <span id="recharge-total-amount-footer">¥ 0.01</span> 订阅
      </button>
    </div>
  </div>
</div>
<!-- 查看收费智能体弹窗结束 -->


<!-- mobile debug 插件 -->
<!-- <script src="https://unpkg.com/vconsole@latest/dist/vconsole.min.js"></script> -->
<script src="libs/jquery-3.1.1.min.js"></script>
<script src="js/fingerprint2.min.js"></script>
<script src="libs/sse.js?ver=${initParam.version}"></script>
<script src="js/common.js?ver=${initParam.version}"></script>
<script src="js/hello.js?ver=${initParam.version}"></script>
<script src="js/chat.js?ver=${initParam.version}"></script>
<script src="js/conversations.js?ver=${initParam.version}"></script>
<script src="js/nav.js?ver=${initParam.version}"></script>
<script src="js/index.js?ver=${initParam.version}"></script>
<script src="js/self.js?ver=${initParam.version}"></script>
<script src="js/query.js?ver=${initParam.version}"></script>
<script src="js/ball.js"></script>
<script src="js/agent.js"></script>
<script src="js/login.js?ver=${initParam.version}"></script>
</body>
</html>