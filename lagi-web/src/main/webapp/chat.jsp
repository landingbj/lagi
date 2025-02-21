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
  <link rel="stylesheet" href="css/chat.css?ver=${initParam.version}">
  <link rel="stylesheet" href="css/login.css?ver=${initParam.version}">
  <link rel="stylesheet" href="css/agent.css?ver=${initParam.version}">
  <link rel="stylesheet" href="css/material.css?ver=${initParam.version}">
  <link rel="stylesheet" href="css/model.css?ver=${initParam.version}">
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

    <div id="alert-box" class="absolute w-full h-full  left-0 top-0 z-50 hidden" style="background-color: rgba(0, 0, 0, 0.5);">
    </div>
    <div id="confirm-box" class="absolute w-full h-full  left-0 top-0 z-50 hidden" style="background-color: rgba(0, 0, 0, 0.5);">
    </div>
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
                   class="flex px-3 min-h-[44px] border py-1 items-center gap-3 transition-colors duration-200 dark:text-white cursor-pointer text-sm rounded-md dark:border-white/20 hover:bg-gray-500/10 h-11 bg-white dark:bg-transparent flex-grow overflow-hidden">
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
                <span class="mb-1 flex flex-row gap-2 rounded-md border dark:text-white dark:border-white/20 dark:bg-transparent" data-state="closed">
                      <a
                              class="flex px-3 min-h-[44px] py-1 gap-3 transition-colors duration-200 dark:text-white cursor-pointer text-sm rounded-md dark:border-white/20 hover:bg-gray-500/10 h-11 w-11 flex-shrink-0 items-center justify-center bg-white dark:bg-transparent"><svg
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
        <div id="top-nav" class="sticky top-0 z-10 flex items-center border-b border-white/20 bg-gray-800 pl-1 pt-1 text-gray-200 sm:pl-3 md:hidden "
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
          <!-- <div  class="w-full h-full absolute">
          </div> -->
          <div>
            <div id="model-prefences" class="w-full h-16 pl-10 absolute left-0 top-0 ">
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
                  <path d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z"
                        p-id="7346" data-spm-anchor-id="a313x.search_index.0.i2.13b03a81EFSaRX"
                        class="selected" fill="#1296db"></path>
                </svg>
              </div>
              <textarea class="w-full h-full" id="textareaCopy"></textarea>
            </div>

            <div id="hello-page" class="flex-1 h-full">
              <div id="mytab" class="hidden">
                <!-- 我的发布列表开始 -->
              <div class="agent-list-container tab" class="w-full" id="agent-list-container">

              <!--                        <button onclick="openAgentModal()">新增智能体</button> -->
              <!-- <span class="close-btn" onclick="closeAgentList()">&times;</span> -->
            <div>
                <a onclick="backToChat()">
                    <svg class="back-button" xmlns="http://www.w3.org/2000/svg" width="25" height="25" viewBox="0 0 100 100" style="cursor: pointer;">
                        <circle cx="50" cy="50" r="40" stroke="#000" stroke-width="10" fill="none"/>
                        <line x1="65" y1="50" x2="35" y2="50" stroke="#000" stroke-width="10" class="line"/>
                        <polyline points="45,35 35,50 45,65" stroke="#000" stroke-width="10" fill="none" class="arrow"/>
                    </svg>
                </a>
            </div>

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
                <!-- 动态生成的智能体数据行 -->
                </tbody>
              </table>

              <div class="pagination" id="pagination">
                <!-- 分页按钮将动态添加到这里 -->
              </div>
              </div>
              <!-- 我的发布列表结束 -->
              <!-- 我的订阅列表开始 -->
              <div class="paid-agent-list-container w-full tab" id="paid-agent-list-container">
                <div>
                    <a onclick="backToChat()">
                        <svg class="back-button" xmlns="http://www.w3.org/2000/svg" width="25" height="25" viewBox="0 0 100 100" style="cursor: pointer;">
                            <circle cx="50" cy="50" r="40" stroke="#000" stroke-width="10" fill="none"/>
                            <line x1="65" y1="50" x2="35" y2="50" stroke="#000" stroke-width="10" class="line"/>
                            <polyline points="45,35 35,50 45,65" stroke="#000" stroke-width="10" fill="none" class="arrow"/>
                        </svg>
                    </a>
                </div>
                <table id="paid-agent-list" class="paid-agent-list " border="1">
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

              <div class="user-material tab w-full"
                id="user-upload-file-container">
                <div class="user-material-head">
                  <span>我的语料</span>
                </div>
                <div class="material-nav  w-full p-1">
                  <button class="material-title material-nav-active" id="link1" onclick="changeMaterialPage(this)" >
                    我的语料
                  </button>
                  <button  class="material-title material-nav-not-active " id="link2"  onclick="changeMaterialPage(this)">
                    文本切片
                  </button>
                  <button  class="material-title material-nav-not-active" id="link3" onclick="changeMaterialPage(this)">
                    向量数据库
                  </button>
                  <a onclick="backToChat()">
                    <svg t="1740019605102" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9950" xmlns:xlink="http://www.w3.org/1999/xlink" width="24" height="24"><path d="M555.6 692.8c-5.5 0-10.9-2.1-15.1-6.2L425 571c-25-25-25-65.7 0-90.7l115.5-115.5c8.3-8.3 21.8-8.3 30.2 0s8.3 21.8 0 30.2L455.2 510.5c-8.4 8.4-8.4 22 0 30.4l115.5 115.5c8.3 8.3 8.3 21.8 0 30.2-4.2 4.1-9.6 6.2-15.1 6.2z" p-id="9951"></path><path d="M512 42.7c258.8 0 469.3 210.5 469.3 469.3S770.8 981.3 512 981.3 42.7 770.8 42.7 512 253.2 42.7 512 42.7M512 0C229.2 0 0 229.2 0 512s229.2 512 512 512 512-229.2 512-512S794.8 0 512 0z" p-id="9952"></path></svg>
                  </a>
                </div>
                
                <div id="upload-file-list">
                  <div id = "my-corpus"  class="material-item" >
                    <div class="drop-area" id="dropArea">
                      <p>将文件拖放到这里或点击选择文件</p>
                      <input type="file" id="fileInput" />
                      <button class="button" onclick="document.getElementById('fileInput').click()">选择文件</button>
                    </div>
                    <table id="upload-file-list1"  class="" border="1">
                      <thead>
                      <tr>
                        <th>文件名称</th>
                        <th>文件</th>
                        <th>更新时间</th>
                        <th>操作</th>
                      </tr>
                      </thead>
                      <tbody>
                      <!-- 动态生成已上传文件数据 -->
                      </tbody>
                    </table>
                    <div class="pagination" id="file-upload-pagination">
                      <!-- 分页按钮将动态添加到这里 -->
                    </div>
                  </div>
                  <div id = "chat-settings" class="material-item chat-settings" style="display: none;">
                      <!-- LLM 温度 -->
                      <div class="chat-setting-item">
                        <h2 >LLM 温度</h2>
                        <div class="temperature-intro">
                          <p >此设置控制您的 LLM 回答的“创意”程度。</p>
                          <p >数字越高越有创意。对于某些模型，如果设置得太高，可能会导致响应不一致。</p>
                          <p >模型不同，此 LLM温度 的有效值范围也不同。</p>
                        </div>
                        <div class="temperature-control">
                          <label for="textBlockSize" style=""><img src="images/thermometer.png" alt=""> 温度(temperature)：</label>
                          <input type="number" id="wendu_type" value="0.8" style="width: 80px; padding: 8px; font-size: 1.1em; margin-right: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;" />
                          <button onclick="submitSettings('wendu_type')" class="setting-btn setting-submit-btn">提交</button>
                          &nbsp;&nbsp;
                          <button onclick="resetSlice('wendu_type')" class="setting-btn setting-reset-btn">重置</button>
                        </div>
                      </div>
                      
                      <!-- 文本切片设置 -->
                      <div class="chat-setting-item">
                        <h2>文本切片设置</h2>
                        <div class="split-intro">
                          <p >在这里，您可以针对不同语料文件，调整文本分段的理想长度。</p>
                          <!-- <p style="">温馨提示：修改此设置前，请确保您熟悉文本分割的逻辑及其可能带来的影响。若您对此不太确定，建议保持默认设置或咨询专业人员。</p> -->
                        </div>
                        
                        <!-- <hr style="margin: 30px 0; border: 1px solid #eee;" /> -->

                        <div class="split-arg-item">
                          <label for="textBlockSize" ><strong> 文本类-文本块大小：</strong></label>
                          <input type="number" id="wenben_type" value="512" style="width: 80px; padding: 8px; font-size: 1.1em; margin-right: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;" />
                          <button onclick="submitSettings('wenben_type')" class="setting-btn setting-submit-btn">提交</button>
                          &nbsp;&nbsp;
                          <button onclick="resetSlice('wenben_type')" class="setting-btn setting-reset-btn">重置</button>
                        </div>

                        <div class="split-arg-item">
                          <label for="textBlockOverlap" ><strong>表格类-文本块大小：</strong></label>
                          <input type="number" id="biaoge_type" value="512" style="width: 80px; padding: 8px; font-size: 1.1em; margin-right: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;" />
                          <button onclick="submitSettings('biaoge_type')" class="setting-btn setting-submit-btn">提交</button>
                          &nbsp;&nbsp;
                          <button onclick="resetSlice('biaoge_type')" class="setting-btn setting-reset-btn">重置</button>
                        </div>

                        <div class="split-arg-item">
                          <label for="textBlockOverlap" ><strong>图文类-文本块大小：</strong></label>
                          <input type="number" id="tuwen_type" value="512" style="width: 80px; padding: 8px; font-size: 1.1em; margin-right: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;" />
                          <button onclick="submitSettings('tuwen_type')" class="setting-btn setting-submit-btn">提交</button>
                          &nbsp;&nbsp;
                          <button onclick="resetSlice('tuwen_type')" class="setting-btn setting-reset-btn">重置</button>
                        </div>
                        <p class="split-bottom">*温馨提示：修改此设置前，请确保您熟悉文本分割的逻辑及其可能带来的影响。若您对此不太确定，建议保持默认设置或咨询专业人员。</p> 
                      </div>
                  </div>
                  <div id="vector-database" class="material-item chat-settings" style="display: none;">
                    <!-- 文档相似度阈值 -->
                    <div class="chat-setting-item">
                      </br>
                      <h2 style="font-size: 1.8em; margin-bottom: 20px; color: #023f63;">文档相似度阈值</h2>
                      <div class="chat-setting-item-intro">
                        <p >此设置控制您的向量数据库的搜索精度。</p>
                        <p >它定义了文档与聊天内容相关联所设定的最大距离。数值越高，来源范围越广。</p>
                      </div>
                      
                      <div class="w-full mt-10">
                        <div class="w-full" style="font-size: 16px;">
                          <strong>相似度阈值(范围：0-1)：</strong>
                        </div>
                        <div class="chat-setting-item-control">
                          <input type="range" id="distance" min="0" max="1" step="0.01" value="0.8" style="width: 200px; margin-right: 10px;" />
                          <span id="distance_value" style="font-size: 1.1em; width: 60px;">0.8</span>
        
                          <button onclick="submitSettings('distance')" class="setting-btn setting-submit-btn">提交</button>
                          &nbsp;&nbsp;
                          <button onclick="resetSlice('distance')" class="setting-btn setting-reset-btn">重置</button>
                        </div>
                      </div>
                    </div>
                    <!-- 文档相似度阈值 -->
                    <div class="chat-setting-item">
                      <h2 >向量上下文最大条数</h2>
                      <div class="chat-setting-item-intro text-center">
                        <p >此设置控制您在召回时的上下文条数。</p>
                      </div>
  
                    <div style="margin-top: 20px; display: flex; align-items: center;">
                      <label for="vector-max-top" style="font-size: 1.1em; margin-right: 10px; width: 100px;">最大条数</label>
                      <input type="number" id="vector-max-top" value="30" style="width: 80px; padding: 8px; font-size: 1.1em; margin-right: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;" />
                      <button onclick="submitSettings('vector-max-top')" class="setting-btn setting-submit-btn">提交</button>
                      &nbsp;&nbsp;
                      <button onclick="resetSlice('vector-max-top')" class="setting-btn setting-reset-btn">重置</button>
                    </div>
                    </br></br>
                    <div>
                      <h2 >向量相似度搜索</h2>
                      <div class="relative">
                        <input type="text" id="searchText" placeholder="请输入查询内容..." style="width: 100%; max-width: 800px; padding: 12px; font-size: 16px; border-radius: 8px; border: 1px solid #ccc !important;">
                        <button onclick="search()" class="vector-search"></button>
                      </div>
                    </div>
                    </div>
                    <div id="results" style="margin-top: 40px; padding: 20px;">
                      <!-- 结果项将动态加载到这里 -->
                    </div>
                    <!-- Modal -->
                    <div id="myModal" style="display: none; position: fixed; z-index: 1; left: 0; top: 0; width: 100%; height: 100%; overflow: auto; background-color: rgba(0,0,0,0.4); padding-top: 60px;">
                      <div style="background-color: #fefefe; margin: 5% auto; padding: 20px; border: 1px solid #888; width: 80%; max-width: 800px; border-radius: 8px;">
                        <span id="close_vector_detail" style="color: #aaa; float: right; font-size: 28px; font-weight: bold; cursor: pointer;">&times;</span>
                        <h3 id="modalTitle_x" style="color: #004f99;"></h3>
                        <p><strong>文件ID:</strong> <span id="modalId"></span></p>
                        <p><strong>文件名:</strong> <span id="modalFilename"></span></p>
                        <p><strong>文件路径:</strong> <span id="modalFilepath"></span></p>
                        <p><strong>类别:</strong> <span id="modalCategory"></span></p>
                        <p><strong>距离:</strong> <span id="modalDistance"></span></p>
                        <p> <strong>内容:</strong></p>
                        <p id="modalDocument" style="font-size: 14px; color: #555;"></p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="user-model tab w-full model-modules-box" id="user-model">
                <div class="user-model-head">
                  <span>我的模型</span>
                </div>
                <div class="model-modules-nav w-full p-1">
                  <button class="model-modules-title model-modules-nav-active" onclick="loadUserModule(this, 'user-finetune', 'loadFinetuneData')" >
                    模型微调
                  </button>
                  <button class="model-modules-title model-modules-nav-not-active " onclick="loadUserModule(this, 'user-develop', 'loadDevelopData')">
                      模型部署
                  </button>
                  <button class="model-modules-title model-modules-nav-not-active" onclick="loadUserModule(this, 'user-manager', 'loadDevelopManagerData')">
                      模型管理
                  </button>

                  <a onclick="backToChat()">
                    <svg t="1740019605102" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9950" xmlns:xlink="http://www.w3.org/1999/xlink" width="24" height="24"><path d="M555.6 692.8c-5.5 0-10.9-2.1-15.1-6.2L425 571c-25-25-25-65.7 0-90.7l115.5-115.5c8.3-8.3 21.8-8.3 30.2 0s8.3 21.8 0 30.2L455.2 510.5c-8.4 8.4-8.4 22 0 30.4l115.5 115.5c8.3 8.3 8.3 21.8 0 30.2-4.2 4.1-9.6 6.2-15.1 6.2z" p-id="9951"></path><path d="M512 42.7c258.8 0 469.3 210.5 469.3 469.3S770.8 981.3 512 981.3 42.7 770.8 42.7 512 253.2 42.7 512 42.7M512 0C229.2 0 0 229.2 0 512s229.2 512 512 512 512-229.2 512-512S794.8 0 512 0z" p-id="9952"></path></svg>
                  </a>
                </div>
                <div class="model-modules w-full p-1">
                  <div class="model-module finetune w-full hidden" id="user-finetune">
                    <div class="train-form" id="train-form">
                      <div class="train-datasets">
                        <div class="train-dataset-upload">
                          <label for="sel-model"><strong>选择模型</strong></label>
                          <select name="sel-model" id="sel-model">
                            <option value="Qwen1.5-7B">Qwen1.5-7B</option>
                            <option value="DeepSeek-R1-Distill-Qwen-1.5B">DeepSeek-R1-Distill-Qwen-1.5B</option>
                          </select>
                          <label for="up-datasets"><strong>上传数据集</strong></label>
                          <input name="up-datasets" type="file" id="up-datasets">
                          <button class="train-dataset-upload-btn" onclick="uploadUserDatasets()">
                            上传
                            </button> 
                          <span id="up-datasets-status" ></span>
                        </div>
                        <div class="train-dataset-sel">
                          <label for="sel-datasets"><strong>选择数据集</strong></label>
                          <sapn id="sel-datasets">
                          </sapn>
                          <button onclick="deleteUserDatasets()">
                            <img src="images/delete.png" alt="删除">
                            <span>删除</span>
                          </button>
                        </div>
                      </div>
                      <div class="train-args">
                        <div class="train-args-tile">
                          <strong>模型参数:</strong>
                        </div>
                        <div class="train-args-list">
                          <div>
                            <label for="quantization-bit">量化等级</label>
                            <select name="quantization-bit"> 
                              <option value="none" selected>none</option>
                              <option value="8">8</option>
                              <option value="4">4</option>
                            </select>
                            <label for="quantization-method">量化方法</label>
                            <select name="quantization-method">
                              <option value="none" selected>bitsandbytes</option>
                              <option value="hqq">hqq</option>
                              <option value="eetq">eetq</option>
                            </select>
                            <label for="template">对话模板</label>
                            <input name="template" id="template" value="" type="text">
                            <label for="RoPE-scaling">RoPE 插值方法</label>
                            <select name="RoPE-scaling">
                              <option value="none" selected>none</option>
                              <option value="linear">linear</option>
                              <option value="dynamic">dynamic</option>
                              <option value="yarn">yarn</option>
                              <option value="llama3">llama3</option>
                            </select>
                            <label for="booster">加速方式</label>
                            <select name="booster" type="text">
                              <option value="auto">auto</option>
                              <option value="flashattn2">flashattn2</option>
                              <option value="unsloth">unsloth</option>
                              <option value="liger_kernel">liger_kernel</option>
                            </select>
                          </div>
                          <div>
                            <label for="learning-rate">学习率</label>
                            <input name="learning-rate" type="text" value="0.0001">
                            <label for="epochs">训练轮数</label>
                            <input name="epochs" type="text" value="3">
                            <label for="maximum-gradient-norm">最大梯度范数</label>
                            <input name="maximum-gradient-norm" type="text" value="1">
                            <label for="max-samples">最大样本数</label>
                            <input name="max-samples" type="text" value="100000">
                            <label for="compute-type">计算类型</label>
                            <select name="compute-type" >
                              <option value="bf16">bf16</option>
                              <option value="fp16">fp16</option>
                              <option value="fp32">fp32</option>
                              <option value="pure_bf16">pure_bf16</option>
                            </select>
                          </div>
                          <div>
                            <label for="cutoff-length">截断长度</label>
                            <input name="cutoff-length" type="text" value="1024">
                            <label for="batch-size">批处理大小</label>
                            <input name="batch-size" type="text" value="1">
                            <label for="gradient-accumulation">梯度累积</label>
                            <input name="gradient-accumulation" type="text" value="2">
                            <label for="val-size">验证集比例</label>
                            <input name="val-size" type="text" value="0.1">
                            <label for="LR-scheduler">学习率调节器</label>
                            <select name="LR-scheduler" type="text">
                              <option value="linear">linear</option>
                              <option value="cosine" selected>cosine</option>
                              <option value="cosine_with_restarts">cosine_with_restarts</option>
                              <option value="polynomial">polynomial</option>
                              <option value="constant">constant</option>
                              <option value="constant_with_warmup">constant_with_warmup</option>
                              <option value="inverse_sqrt">inverse_sqrt</option>
                              <option value="reduce_lr_on_plateau">reduce_lr_on_plateau</option>
                              <option value="cosine_with_min_lr">cosine_with_min_lr</option>
                              <option value="warmup_stable_decay">warmup_stable_decay</option>
                            </select>
                          </div>
                          <!-- <div>
                            <label for="datasets">检查点路径</label>
                            <input name="model" type="text">
                          </div> -->
                          <div class="train-btn-box">
                            <p class="train-btn-box-mid">
                              <label for="output-dir"><strong>保存地址</strong></label>
                              <input name="output-dir" type="text" value="">
                              <button onclick="doTrain(this)"> <image
                                src="images/train.png"
                              />训练</button>
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="train-view" id="train-view">
                      <div class="train-view-title"><strong>训练日志</strong> </div>
                      <div class="train-view-content" id="train-view-content"></div>
                    </div>
                  </div>
                  <div class="model-module user-develop hidden" id="user-develop">
                    <div class="develop-model-container">
                      <button id="addModeldevelop-btn" class="develop-btn">
                        <img src="images/add.png" alt="addDevelop">
                        <strong>部署模型</strong>
                      </button>
                      <div id="modelList" class="model-list">
                      </div>
                    </div>

                    <!-- 模态框 -->
                    <div id="myDevelop" class="develop">
                      <div class="develop-content">
                          <div class="develop-content-title">
                            <span class="close">&times;</span>
                            <span>部署模型</span>
                          </div>
                          
                          <form id="modelForm">
                              <label for="modelPath"><strong>模型路径:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</strong></label>
                              <input type="text" id="modelPath" name="modelPath" placeholder="请输入" required>
                              <br>
                              <label for="adapterPath"><strong>Adapter路径:</strong> </label>
                              <input type="text" id="adapterPath" name="adapterPath" placeholder="请输入">
                              <button type="submit" class="develop-btn">发布模型</button>
                          </form>
                      </div>
                    </div>
                  </div>
                  <div class="model-module manager hidden" id="user-manager">
                    <div class="modal-container">
                      <button id="addModelBtn" class="develop-btn">
                        <img src="images/add.png" alt="addDevelop">
                        <strong>添加模型</strong>
                      </button>
                      <table id="modelTable">
                          <thead>
                              <tr>
                                  <th>模型名</th>
                                  <th>是否线上模型</th>
                                  <th>API Key</th>
                                  <th>模型类型</th>
                                  <th>模型终端地址</th>
                                  <th>状态</th>
                                  <th>操作</th>
                              </tr>
                          </thead>
                          <tbody>
                              <!-- 模型行将在这里动态添加 -->
                          </tbody>
                      </table>
                    </div>
              
                    <div id="modelModal" class="modal-manager">
                        <div class="modal-manager-content">
                            <!-- <span class="close">&times;</span>
                            <h2 id="modalTitle">添加模型</h2> -->
                            <div class="modal-content-title">
                              <span class="close">&times;</span>
                              <span>添加模型</span>
                            </div>
                            <form id="modeManagerlForm">
                               <div class="modal-manager-row">
                                  <label for="modelName"><strong>模型名:</strong></label>
                                  <input type="text" id="modelName" name="modelName" placeholder="请输入" required>
                               </div>
                               <div class="modal-manager-row">
                                  <label for="isOnline"><strong>是否线上模型:</strong></label>
                                  <input type="checkbox" id="isOnline" name="isOnline">
                                </div>
                                <div id="onlineFields" style="display: none;">
                                  <div class="modal-manager-row">
                                    <label for="apiKey"><strong>API Key:</strong></label>
                                    <input type="text" id="apiKey" name="apiKey" placeholder="请输入">
                                  </div>
                                  <div class="modal-manager-row">
                                    <label for="modelType"><strong>模型类型:</strong></label>
                                    <select id="modelType" name="modelType">
                                    </select>
                                  </div>
                                </div>
                                <div class="modal-manager-row" id="offlineFields">
                                    <label for="modelEndpoint"><strong>模型终端地址:</strong></label>
                                    <input type="text" id="modelEndpoint" name="modelEndpoint" placeholder="请输入">
                                </div>
                                <div class="modal-manager-row" >
                                  <button type="submit">保存修改</button>
                                </div>
                            </form>
                        </div>
                    </div>
                  </div>
                </div>
              </div>
              </div>
              <div id="conTab" class="react-scroll-to-bottom--css-dlyqs-79elbk h-full dark:bg-gray-800">
                <div class="react-scroll-to-bottom--css-dlyqs-1n7m0yu h-full">
                  <div class="flex flex-col text-sm dark:bg-gray-800 h-full">
                    <div class="flex h-full flex-col items-center justify-between">
                       
                      <!-- 我的订阅列表结束 -->
                      <div id="item-content" class="w-full" style="overflow: auto;">
                        <div id="topTitle" class="relative align-center flex w-full md:flex-col justify-center self-center ">
                          <div class="absolute right-2 top-2 text-gray-600 text-sm sm:none">
                            内容由AI协助
                          </div>
                          <div id="centerTitleBox" class=""  style="margin-left: auto; margin-right: auto;">
                            <canvas id = "title-canvas"  style="width: 300px; height: 120px; margin-top: 10px;"></canvas>
                          </div>
                        </div>
                        <div class="relative">
                          <div class="ball-mask" style="width: min(47em, 90%); height: 0px; position: relative; margin-left: auto; margin-right: auto;" >
                            <div class="ball-corner-container" style=" z-index: 5; width: 100%;position: absolute; margin-left: auto; margin-right: auto;">
                              <div class="ball-left-top absolute ball-corner">
                                <ul>
                                </ul>
                              </div>
                              <div class="ball-right-top absolute ball-corner">
                                 <ul>
                                 </ul>
                              </div>
                            </div>
                          </div>
                          <div id="ball-div" >
                            
                          </div>
                        </div>
                        
                        <!-- ***********************输入框前form******************************** -->
                        <div id="introduces" class="relative">
                          <div class="h-full flex gap-0 md:gap-2 justify-center">
                            <div class="grow">
                              <div class="absolute left-0 mb-4 flex w-full grow gap-2  sm:pb-0 md:static md:mb-0 md:max-w-none">
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
                 class="absolute bottom-0 left-0  border-t md:border-t-0 dark:border-white/20 md:border-transparent md:dark:border-transparent md:bg-vert-light-gradient bg-white dark:bg-gray-800 md:!bg-transparent dark:md:bg-vert-dark-gradient pt-2 md:w-[calc(100%-.5rem)]">
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
                          <li class=" pl-5  not-available " onclick="openCreateAgent()">创建智能体</li>
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
              <div id="footer-info" class="relative">
                <div class="relative pb-3 pt-2 text-center md:text-xs text-[10px] text-gray-600 dark:text-gray-300 md:px-[60px] md:pb-6 md:pt-3" style="max-height: 50px;">
                  <div class="lg:mx-auto lg:max-w-2xl xl:max-w-3xl" style="max-width: 60rem; max-height: 50px;">
                    <a href="http://beian.miit.gov.cn/" target="_blank" style="display:inline-block">京ICP备&nbsp;2020046697号</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    <img style="width: 20px; height: 20px; display: inline-block;" src="images/beian.png" alt="">
                    <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=11010802033940" target="_blank" style="display:inline-block">京公网按备号&nbsp;11010802033940</a>
                    &nbsp;&nbsp;&nbsp;&nbsp; 联系邮箱:
                    <a href="mailto:service@landingbj.com" style="text-decoration: none;">service@landingbj.com</a>
                    &nbsp;&nbsp;&nbsp;&nbsp;联系电话: 027-87659116
                    &nbsp;&nbsp;&nbsp;&nbsp;<div>Powered By <a style="color: rgb(35 142 252);" href="https://github.com/landingbj/lagi">Lag[i]</a>&nbsp;一种通用人工智能的实现验证</div>
                  </div>
                </div>
                
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
    
              </div>
              <!-- *********************** 底部end******************************** -->
            </div>
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
        <option value="ai.agent.customer.GeneralAgent">联动北方</option>
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
    <button class="closePayBtn" onclick="cancelPayment()">&times;</button>
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
    <button id="h5PrepayBtn" class="paymentBtn" onclick="h5Prepay()">微信支付</button>
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

<!-- 创建智能体弹窗开始 -->
<div id="createLagiAgent" class="create_LagiAgent">
  <div class="createLagiAgent-content">
    <button class="close-btn" onclick="closeCreateAgent()">×</button>
    <h2>创建智能体</h2>
    <form id="createLagiAgentForm">
      <label for="LagiAgentName">名称 <span style="color: red">*</span></label>
      <input type="text" id="LagiAgentName" placeholder="请输入智能体名称">

      <label for="LagiAgentDescribe">设定 <span style="color: red">*</span></label>
      <textarea id="LagiAgentDescribe" placeholder="你是一个健身教练AI助手，主要职责是根据用户的需求制定个性化的健身计划和饮食建议。你需要熟练掌握相关领域的知识，并根据用户需求提供准确、专业、权威的解答。"></textarea>

      <div class="create_LagiAgentButtons">
        <button type="button" id="createLagiAgentButtons" onclick="createLagiAgent()" disabled>立即创建</button>
      </div>
    </form>
  </div>
</div>

<!-- 创建智能体弹窗结束 -->


<!-- mobile debug 插件 -->
<!-- <script src="https://unpkg.com/vconsole@latest/dist/vconsole.min.js"></script> -->
<script src="libs/jquery-3.1.1.min.js"></script>
<script src="js/fingerprint2.min.js"></script>
<script src="libs/sse.js?ver=${initParam.version}"></script>
<script src="js/config.js?ver=${initParam.version}"></script>
<script src="js/common.js?ver=${initParam.version}"></script>
<script src="js/file.js?ver=${initParam.version}"></script>
<script src="js/hello.js?ver=${initParam.version}"></script>
<script src="js/chat.js?ver=${initParam.version}"></script>
<script src="js/conversations.js?ver=${initParam.version}"></script>
<script src="js/nav.js?ver=${initParam.version}"></script>
<script src="js/index.js?ver=${initParam.version}"></script>
<script src="js/self.js?ver=${initParam.version}"></script>
<script src="js/query.js?ver=${initParam.version}"></script>
<script src="js/ball.js?ver=${initParam.version}"></script>
<script src="js/agent.js?ver=${initParam.version}"></script>
<script src="js/login.js?ver=${initParam.version}"></script>
<script src="js/model.js?ver=${initParam.version}"></script>
<script src="js/vector_settings.js?ver=${initParam.version}"></script>
</body>
</html>