<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
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
</head>
<noscript>
  <strong>We're sorry but chatai doesn't work properly without JavaScript enabled. Please enable it to
    continue.
  </strong>
</noscript>


  <body class="antialiased">
    <div id="pdfMask" class="pdf-mask" >
      <div class="pdf-box">
        <img src="" style="width: 100%; height: auto"/>
      </div>
    </div>
    <div id="__next">
      <script>!function () { try { var d = document.documentElement, c = d.classList; c.remove('light', 'dark'); var e = localStorage.getItem('theme'); if ('system' === e || (!e && true)) { var t = '(prefers-color-scheme: dark)', m = window.matchMedia(t); if (m.media !== t || m.matches) { d.style.colorScheme = 'dark'; c.add('dark') } else { d.style.colorScheme = 'light'; c.add('light') } } else if (e) { c.add(e || '') } if (e === 'light' || e === 'dark') d.style.colorScheme = e } catch (e) { } }()</script>
      <div class="overflow-hidden w-full h-full relative flex z-0">
        <!-- 左边导航条 -->
        <!-- scrollbar-trigger flex h-full w-full flex-1 items-start border-white/20 -->
        <!-- dark hidden bg-gray-900 md:fixed md:inset-y-0 md:flex md:w-[260px] md:flex-col -->
        <div id="navigation_bar" class="flex-shrink-0 overflow-x-hidden dark hidden bg-gray-900 md:fixed md:inset-y-0 md:flex md:w-[260px]  md:flex-col" style="background-color: #023f63;">
          <div class="h-full w-[260px]">
            <div class="flex h-full min-h-0 flex-col ">
              <div class="scrollbar-trigger relative h-full w-full flex-1 items-start border-white/20">
                <h2
                  style="position: absolute; border: 0px; width: 1px; height: 1px; padding: 0px; margin: -1px; overflow: hidden; clip: rect(0px, 0px, 0px, 0px); white-space: nowrap; overflow-wrap: normal;">
                  历史对话</h2>
                <nav class="flex h-full w-full flex-col p-2" aria-label="Chat history">
                  <div class="mb-1 flex flex-row gap-2">
                    <a onclick = "backToHello()" class="flex px-3 min-h-[44px] py-1 items-center gap-3 transition-colors duration-200 dark:text-white cursor-pointer text-sm rounded-md border dark:border-white/20 hover:bg-gray-500/10 h-11 bg-white dark:bg-transparent flex-grow overflow-hidden">
                      <!-- <svg
                        stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round"
                        stroke-linejoin="round" class="icon-sm shrink-0" height="1em" width="1em"
                        xmlns="http://www.w3.org/2000/svg">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg> -->
                      <svg t="1697600043545" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4283" width="20" height="20"><path d="M927.999436 531.028522a31.998984 31.998984 0 0 0-31.998984 31.998984c0 51.852948-10.147341 102.138098-30.163865 149.461048a385.47252 385.47252 0 0 1-204.377345 204.377345c-47.32295 20.016524-97.6081 30.163865-149.461048 30.163865s-102.138098-10.147341-149.461048-30.163865a385.47252 385.47252 0 0 1-204.377345-204.377345c-20.016524-47.32295-30.163865-97.6081-30.163865-149.461048s10.147341-102.138098 30.163865-149.461048a385.47252 385.47252 0 0 1 204.377345-204.377345c47.32295-20.016524 97.6081-30.163865 149.461048-30.163865a387.379888 387.379888 0 0 1 59.193424 4.533611l-56.538282 22.035878A31.998984 31.998984 0 1 0 537.892156 265.232491l137.041483-53.402685a31.998984 31.998984 0 0 0 18.195855-41.434674L639.723197 33.357261a31.998984 31.998984 0 1 0-59.630529 23.23882l26.695923 68.502679a449.969005 449.969005 0 0 0-94.786785-10.060642c-60.465003 0-119.138236 11.8488-174.390489 35.217667a449.214005 449.214005 0 0 0-238.388457 238.388457c-23.361643 55.252253-35.22128 113.925486-35.22128 174.390489s11.8488 119.138236 35.217668 174.390489a449.214005 449.214005 0 0 0 238.388457 238.388457c55.252253 23.368867 113.925486 35.217667 174.390489 35.217667s119.138236-11.8488 174.390489-35.217667A449.210393 449.210393 0 0 0 924.784365 737.42522c23.368867-55.270316 35.217667-113.925486 35.217667-174.390489a31.998984 31.998984 0 0 0-32.002596-32.006209z" fill="#e6e6e6" p-id="4284"></path></svg>
                      <span class="truncate">返回欢迎页</span>
                    </a>
                    <span class="" data-state="closed">
                      <a
                        class="flex px-3 min-h-[44px] py-1 gap-3 transition-colors duration-200 dark:text-white cursor-pointer text-sm rounded-md border dark:border-white/20 hover:bg-gray-500/10 h-11 w-11 flex-shrink-0 items-center justify-center bg-white dark:bg-transparent"><svg
                          stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round"
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
                          href="https://help.openai.com/en/articles/7730893" target="_blank" class="underline"
                          rel="noreferrer">Learn more</a></div><button class="btn relative btn-primary mt-4 w-full">
                        <div class="flex w-full gap-2 items-center justify-center"><svg stroke="currentColor"
                            fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round"
                            stroke-linejoin="round" class="icon-sm" height="1em" width="1em"
                            xmlns="http://www.w3.org/2000/svg">
                            <path d="M18.36 6.64a9 9 0 1 1-12.73 0"></path>
                            <line x1="12" y1="2" x2="12" y2="12"></line>
                          </svg>Enable chat history</div>
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
                          class="gold-new-button flex items-center gap-3"><svg stroke="currentColor" fill="none"
                            stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round"
                            class="icon-sm shrink-0" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
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
                      <button
                        onclick="window.open('https://saas.landingbj.com', '_blank');"
                        class="flex w-full items-center gap-3 rounded-md px-3 py-3 text-sm transition-colors duration-200 hover:bg-gray-100 group-ui-open:bg-gray-100 dark:hover:bg-gray-800 dark:group-ui-open:bg-gray-800"
                        type="button" aria-haspopup="true" aria-expanded="false" data-state="closed"
                        id="headlessui-menu-button-:rc:" data-headlessui-state="">
                        <div class="flex-shrink-0">
                          <div class="flex items-center justify-center rounded">
                            <div class="relative flex">
                              <img alt="User" loading="lazy" width="36" height="36" decoding="async" data-nimg="1"
                                class="rounded-sm" srcset="" src="images/rj.png" style="color: transparent;">
                            </div>
                          </div>
                        </div>
                        <div
                          class="grow overflow-hidden text-ellipsis whitespace-nowrap text-left text-gray-700 dark:text-white">
                          <!-- <div class="font-semibold">游客</div> -->
                          <div id="user_box"class="font-semibold">
                            登录
                          </div>
                          <div class="text-xs text-gray-500"></div>
                        </div><svg stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24"
                          stroke-linecap="round" stroke-linejoin="round" class="icon-sm flex-shrink-0 text-gray-500"
                          height="1em" width="1em" xmlns="http://www.w3.org/2000/svg">
                          <circle cx="12" cy="12" r="1"></circle>
                          <circle cx="19" cy="12" r="1"></circle>
                          <circle cx="5" cy="12" r="1"></circle>
                        </svg>
                      </button></div>
                  </div>
                </nav>
              </div>
            </div>
          </div>
        </div>
        <!-- 左边导航条 -->
        <div class="relative flex h-full max-w-full flex-1 overflow-hidden">
          <div class="flex h-full max-w-full flex-1 flex-col" >
            <!-- 上部导航条 -->
            <div class="sticky top-0 z-10 flex items-center border-b border-white/20 bg-gray-800 pl-1 pt-1 text-gray-200 sm:pl-3 md:hidden " style="background-color: #023f63;">
              <div>
                <button type="button" class="-ml-0.5 -mt-0.5 inline-flex h-10 w-10 items-center justify-center rounded-md hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white dark:hover:text-white">
                  <span class="sr-only">Open sidebar</span>
                  <svg stroke="currentColor" fill="none" stroke-width="1.5" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6">
                          <line x1="3" y1="12" x2="21" y2="12"></line>
                          <line x1="3" y1="6" x2="21" y2="6"></line>
                          <line x1="3" y1="18" x2="21" y2="18"></line>
                  </svg>
                </button>
              </div>
              <h1 class="flex-1 text-center text-base font-normal">新建对话</h1><button type="button" class="px-3">
                <svg stroke="currentColor" fill="none" stroke-width="1.5" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6">
                      <line x1="12" y1="5" x2="12" y2="19"></line>
                      <line x1="5" y1="12" x2="19" y2="12"></line>
                  </svg></button>
            </div>
            <!-- 上部导航条 -->
            <!-- *************************************************主要结构********************************* -->
            <main class="relative h-full w-full transition-width overflow-auto flex-1">
              <div>
                <div id="model-prefences" class="w-full h-16 pl-10">
                  <div id="model-selects" class="inline-block model-selects  float-left">
                  </div>
                  <div class="model-btns w-20 flex-1 inline-block">
                    <button id = "modelClearBtn" class="inline-block" onclick="clearPreference()">重置所有</button>
                    <button id = "modelSaveBtn" class="inline-block" onclick="savePerference()">保存</button>
                  </div>
                </div>
              </div>
              <div role="presentation" class="flex h-full">
                <!-- 隐藏文本遮罩 -->
                <div  class="w-full h-full absolute top-14 z-10 bg-white " id="textareaMask" style="display: none;">
                  <div class="w-8 h-8 bg-gray-100" style="border-radius: 24px;" onclick="hideTextareaMask()">
                    <svg class="w-8 h-8 icon" t="1703233241892" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7345" width="200" height="200"><path d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z" p-id="7346" data-spm-anchor-id="a313x.search_index.0.i2.13b03a81EFSaRX" class="selected" fill="#1296db"></path></svg>
                  </div>
                  <textarea class="w-full h-full"  id = "textareaCopy"></textarea>
                </div>

                <div id="hello-page" class="flex-1 overflow-hidden">
                  <div class="react-scroll-to-bottom--css-dlyqs-79elbk h-full dark:bg-gray-800">
                    <div class="react-scroll-to-bottom--css-dlyqs-1n7m0yu">
                      <div class="flex flex-col text-sm dark:bg-gray-800 h-full">
                        <div class="flex h-full flex-col items-center justify-between pb-64">
                          <!-- ***********************************头部  slide**************************************-->
                          <div id="modelChoices" class="px-2 w-full flex flex-col py-2 md:py-6 sticky top-0">
                            <div class="relative flex flex-col items-stretch justify-center gap-2 sm:items-center">
                              <div class="relative flex rounded-xl bg-gray-100 p-1 text-gray-900 dark:bg-gray-900">
                                <ul class="flex w-full list-none gap-1 sm:w-auto">
                                </ul>
                              </div>
                            </div>
                          </div>
                          <!-- ************************************************中部 标题**************************** -->
                          <div id="topTitle"
                            class="align-center flex h-full w-full flex-col justify-center self-center px-2 pb-2 md:pb-[8vh]">
                            <div class="absolute right-2 top-2 text-gray-600 text-sm  sm:none">
                              仅限邀请内测
                            </div>
                            <h1
                              class="text-6xl font-semibold text-center t ml-auto mr-auto mb-10 sm:mb-16 flex gap-2 items-center justify-center flex-grow"
                              style="color: #838383;"
                              >
                              <!-- style="color: #51B4EF;" -->
                              <span></span>
                            </h1>
                          </div>
                          <!-- ************************************************中部 标题end**************************** -->
                          <div id="item-content" class="flex h-full w-full flex-col  px-2 pb-32 md:pb-[8vh]"
                            style="overflow: auto;">

                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div
                  class="absolute bottom-0 left-0 w-full border-t md:border-t-0 dark:border-white/20 md:border-transparent md:dark:border-transparent md:bg-vert-light-gradient bg-white dark:bg-gray-800 md:!bg-transparent dark:md:bg-vert-dark-gradient pt-2 md:pl-2 md:w-[calc(100%-.5rem)]">
                  <form
                    class="stretch mx-2 flex flex-row gap-3 last:mb-2 md:mx-4 md:last:mb-6 lg:mx-auto lg:max-w-2xl xl:max-w-3xl">
                    <div class="relative flex h-full flex-1 items-stretch md:flex-col">
                      <!-- ***********************输入框前form******************************** -->
                      <div id="introduces">
                        <div class="h-full flex ml-1 md:w-full md:m-auto md:mb-4 gap-0 md:gap-2 justify-center">
                          <div class="grow">
                            <div
                              class="absolute bottom-full left-0 mb-4 flex w-full grow gap-2 px-1 pb-1 sm:px-2 sm:pb-0 md:static md:mb-0 md:max-w-none">
                              <div
                                class="grid w-full grid-flow-row grid-cols-[repeat(auto-fit,minmax(250px,1fr))] gap-3">

                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <!-- ***********************输入框前form end******************************** -->

                     

                      <!-- ***********************输入框******************************** -->
                      
                      <div class="flex w-full " id="queryBox">

                        <div id = "agent-container" class="absolute flex w-full flex-1 items-stretch md:flex-col hidden">
                          <div id="agentList" class="absolute right-1 z-50 agent-pannel w-32 "  >
                            <div id = "agent-head" class="agent-head  pt-2 pb-1 text-center"></div>
                            <ul id = "agent-tools" class="pb-2">
                              <!-- <li class=" pl-5  " >社交圈</li>
                              <li class=" pl-5  not-available" >视频流</li>
                              <li class=" pl-5  not-available" >语音流</li>
                              <li class=" pl-5  not-available">传感器</li>
                              <li class=" pl-5  not-available">工控线</li> -->
                              <!-- <li class=" pl-5 pb-2">工控线</li> -->
                            </ul>
                          </div>
                          </div>

                        <div class="flex-col-reverse w-10 m-0 relative">
                          
                          <div  class="absolute top-0  ml-2" style="display: none;" id = "textareaScretch"  onclick="showTextareaMask()">
                            <svg  style="width: 24px;height: 24px;" t="1703232823101" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6337" width="200" height="200"><path d="M838.116 732.779 877.7 693.195 511.979 327.549 146.3 693.195 185.883 732.779 512.003 406.652Z" p-id="6338" fill="#1296db"></path></svg>
                            <!-- 向下 -->
                            <!-- <svg t="1703233241892" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7345" width="200" height="200"><path d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z" p-id="7346" data-spm-anchor-id="a313x.search_index.0.i2.13b03a81EFSaRX" class="selected" fill="#1296db"></path></svg> -->
                          </div>
                          <div id="voiceIcon" class="absolute bottom-0 ml-2">
                            <svg id="voiceSvg"
                              style="width: 24px;height: 24px; margin-bottom: 5px;" t="1694870288752" class="icon" viewBox="0 0 1024 1024"
                              version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4046" width="200" height="200">
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
                          <textarea id="queryContent" tabindex="0" data-id="root" rows="1" placeholder="请输入文字..."
                            class=" m-0 w-full resize-none border-0 bg-transparent p-0 pl-2 pr-7 focus:ring-0 focus-visible:ring-0 dark:bg-transparent md:pl-0"
                            style="max-height: 200px; height: 24px;  width: 98%;float: left;"></textarea>

                          <!-- <svg style="width:30px;height:24px" t="1694769456924" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8678" width="200" height="200"><path d="M801.171 483.589H544V226.418c0-17.673-14.327-32-32-32s-32 14.327-32 32v257.171H222.83c-17.673 0-32 14.327-32 32s14.327 32 32 32H480v257.17c0 17.673 14.327 32 32 32s32-14.327 32-32v-257.17h257.171c17.673 0 32-14.327 32-32s-14.327-32-32-32z" fill="" p-id="8679"></path></svg> -->
                          <!-- ******************发送按钮开始 ***************************** -->
                          <!-- bg-gray-900 -->
                          <button onclick="textQuery()" type="button" id="queryBtn"
                            class="absolute p-1 rounded-md text-gray-500 bottom-1.5 right-1 md:bottom-2.5 md:right-2 hover:bg-gray-100 dark:hover:text-gray-400 dark:hover:bg-ldbj-900 disabled:hover:bg-transparent dark:disabled:hover:bg-transparent">
                            <img style="height:24px" src="images/rj.png">
                          </button>
                          <!-- ******************发送按钮关闭***************************** -->
                        </div>
                        
                        <div id="agentButton" class=" flex-col-reverse m-2 flex-bottom">
                          <img style="width: 28px;height: 28px; float: left;right: 100px; object-fit: contain; margin-bottom: 1px;" t="1694871462493" class=" icon"
                          alt="agent"
                          src="images/agent.png"
                          />
                        </div>

                        <div id="addButton" class=" flex-col-reverse m-2 flex-bottom"><svg id="addSvg"
                            style="width: 26px;height: 26px; float: left;right: 100px; margin-bottom: 0px;" t="1694871462493" class=" icon"
                            viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6730"
                            width="200" height="200">
                            <path
                              d="M888.640638 178.095187 344.424912 178.095187c0 0-37.257555-65.74333-75.9109-65.74333L93.247242 112.351857c-15.407921 0-27.907607 15.117302-27.907607 33.675984l0 50.736545c0 18.508539 12.499685 33.624818 27.907607 33.624818l796.417725 0c15.408945 0 26.220175-7.514137 26.220175-26.022677l0.067538 0.38374C915.95268 186.241728 904.049582 178.095187 888.640638 178.095187z"
                              fill="#2DAAD2" p-id="6731"></path>
                            <path
                              d="M958.413747 833.260841c0 18.508539-12.500709 33.624818-27.90863 33.624818L93.247242 866.88566c-15.407921 0-27.907607-15.116279-27.907607-33.624818L65.339636 294.90559c0-18.508539 12.499685-33.625842 27.907607-33.625842l837.257875 0c15.408945 0 27.90863 15.117302 27.90863 33.625842L958.413747 833.260841z"
                              fill="#3399CC" p-id="6732"></path>
                            <path d="M351.692433 523.843746l320.318376 0 0 80.529081-320.318376 0 0-80.529081Z"
                              fill="#FFFFFF" p-id="6733"></path>
                            <path d="M471.659735 403.923516l80.479963 0 0 320.319399-80.479963 0 0-320.319399Z"
                              fill="#FFFFFF" p-id="6734"></path>
                          </svg>
                        </div>
                      </div>
                      <!-- ***********************输入框end******************************** -->
                    </div>
                  </form>
                  
                  <!-- *********************** 底部******************************** -->
                  <div
                    class="relative pb-3 pt-2 text-center md:text-xs text-[10px] text-gray-600 dark:text-gray-300 md:px-[60px] md:pb-6 md:pt-3 ">

                    <div class="lg:mx-auto lg:max-w-2xl xl:max-w-3xl " style="max-width: 60rem;" >
                      <a  href="http://beian.miit.gov.cn/" target="_blank" 
                          style="display:inline-block">京ICP备&nbsp;2020046697号</a>&nbsp;&nbsp;&nbsp;&nbsp;
                      <img
                          style="width: 20px; height: 20px; display: inline-block;" src="images/beian.png" alt="">
                      <a
                          href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=11010802033940"
                          target="_blank"
                          style="display:inline-block">京公网按备号&nbsp;11010802033940</a>
                      &nbsp;&nbsp;&nbsp;&nbsp;
                      联系邮箱: 
                      <a href="mailto:service@landingbj.com" style="text-decoration: none;">service@landingbj.com</a>
                      &nbsp;&nbsp;&nbsp;&nbsp;联系电话: 027-87659116 &nbsp;&nbsp;&nbsp;&nbsp;<div>内容由AI生成 &nbsp 一种通用人工智能的实现验证</div>
                    </div>
                  </div>
                  <!-- *********************** 底部end******************************** -->
                </div>
              </div>

              <!-- ************************************底部 ？ == help *********************************** -->
              <div class="group fixed bottom-5 right-4 z-10 flex flex-row items-center gap-3">
                <div class="hidden md:block">
                  <div class="group relative" data-headlessui-state=""><button
                      class="flex items-center justify-center rounded-full border border-gray-200 bg-gray-50 text-gray-600 dark:border-white/10 dark:bg-white/10 dark:text-gray-200"
                      id="headlessui-menu-button-:rh:" type="button" aria-haspopup="true" aria-expanded="false"
                      data-headlessui-state="">
                      <div class="h-6 w-6">?</div>
                    </button></div>
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
  </body>
</html>