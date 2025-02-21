// 页面加载时获取并渲染智能体列表
document.addEventListener('DOMContentLoaded', () => {
    // loadAgentMenu(currentPage); // 初始加载第一页的智能体
    const agentToolsElement = document.getElementById('agent-tools');
    // agentToolsElement.addEventListener('scroll', handleScroll); // 监听滚动事件
});


// 获取用户ID
let globalUserId = getCookie('userId');

let currentAgentId = null; // 设定一个全局变量来标记当前的 agentId

let selectedAgentId = null;  // 全局变量，用来存储选择的 agentId

// 创建 driver 映射关系
const driverMap = {
    "ai.agent.chat.qianfan.XiaoxinAgent": "千帆",
    "ai.agent.customer.GeneralAgent": "联动北方",
    "ai.agent.chat.coze.CozeAgent": "扣子",
    "ai.agent.chat.tencent.YuanQiAgent": "元器",
    "ai.agent.chat.zhipu.ZhipuAgent": "智谱"
};


// 显示或隐藏收费输入框
function handleFeeRequirementChange() {
    const isFeeRequiredYes = document.getElementById("isFeeRequiredYes").checked;
    const pricePerReqContainer = document.getElementById("pricePerReqContainer");

    if (isFeeRequiredYes) {
        pricePerReqContainer.style.display = "block";  // 显示收费金额输入框
    } else {
        pricePerReqContainer.style.display = "none";   // 隐藏收费金额输入框
    }
}

// 初始化事件监听
document.getElementById("isFeeRequiredYes").addEventListener("change", handleFeeRequirementChange);
document.getElementById("isFeeRequiredNo").addEventListener("change", handleFeeRequirementChange);
handleFeeRequirementChange();

// 清空弹框中的表单内容
function resetAgentForm() {
    var form = document.getElementById('agent-form');
    form.reset();
    document.getElementById('pricePerReqContainer').style.display = 'none';
    document.getElementById('isFeeRequiredNo').checked = true;
    document.getElementById('pricePerReq').value = '';
}

// 打开弹框的方法
function openAgentModal(e) {
    if (!getCookie('userId')) {
        openModal(e);
        return;
    }
    var modal = document.getElementById('agent-form-modal');
    modal.style.display = 'block';
    document.getElementById("agent-list-container").classList.add("blurred");
    document.body.classList.add("no-select");
}

// 关闭弹框并清空表单内容
function closeAgentModal() {
    var modal = document.getElementById('agent-form-modal');
    modal.style.display = 'none';
    document.getElementById("agent-list-container").classList.remove("blurred");
    document.body.classList.remove("no-select");
    resetAgentForm();
}

// 打开智能体列表
function openAgentList() {
    document.getElementById("agent-list-container").style.display = 'block';
}

// 关闭智能体列表
function closeAgentList() {
    document.getElementById("agent-list-container").style.display = 'none';
}

// 打开智能体列表
function openPaidAgentList() {
    document.getElementById("paid-agent-list-container").style.display = 'block';
}

// 关闭智能体列表
function closePaidAgentList() {
    document.getElementById("paid-agent-list-container").style.display = 'none';
}

// 保存智能体信息
function saveAgent() {
    let agentConfig = {
        id: currentAgentId,
        name: document.getElementById("agent-name").value,
        driver: document.getElementById("platform").value,
        token: document.getElementById("token").value,
        appId: document.getElementById("app-id").value,
        isFeeRequired: document.querySelector('input[name="isFeeRequired"]:checked').value === 'true',
        pricePerReq: document.getElementById("pricePerReq").value.trim() === "" ? 0 : parseFloat(document.getElementById("pricePerReq").value.trim()),
        lagiUserId: globalUserId,
        publishStatus: true
    };

    let url = currentAgentId ? '/updateLagiAgent' : '/addLagiAgent';
    fetch('/agent' + url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(agentConfig)
    }).then(response => response.json())
        .then(data => {
            closeAgentModal();
            loadAgentList(1);  // 刷新列表

            // 在保存成功后弹出提示框
            alert('发布成功！');
        })
        .catch(error => {
            // 处理可能的错误情况
            alert('发布失败，请重试！');
        });
}


// 加载智能体列表
function loadAgentList(pageNumber) {
    fetch(`/agent/getLagiAgentList?pageNumber=${pageNumber}&pageSize=10&lagiUserId=${globalUserId}`)
        .then(response => response.json())
        .then(data => {
            let tbody = document.querySelector("#agent-list tbody");
            tbody.innerHTML = ''; // 清空表格内容

            data.data.forEach(agent => {
                // 获取 driver 的翻译
                const driverName = driverMap[agent.driver] || agent.driver; // 如果没有找到映射，则显示原值

                let row = document.createElement('tr');

                // 根据 publishStatus 决定按钮
                let publishButton = agent.publishStatus
                    ? `<button onclick="togglePublishStatus(${agent.id}, false)">取消发布</button>`
                    : `<button onclick="togglePublishStatus(${agent.id}, true)">发布</button>`;

                row.innerHTML = `
                    <td>${agent.name}</td>
                    <td>${driverName}</td>
                    <td>${agent.token}</td>
                    <td>${agent.appId}</td>
                    <td>${agent.isFeeRequired ? '是' : '否'}</td>
                    <td>${agent.isFeeRequired ? agent.pricePerReq : '0'}</td>
                    <td>${agent.publishStatus ? '已发布' : '未发布'}</td>
                    <td>${agent.income.toFixed(2)}</td> 
                    <td>${agent.reqNum}</td> 
                    <td>${agent.subscriberNum}</td> 
                    <td>
                        <button onclick="editAgent(${agent.id})">编辑</button>
                        <button onclick="orchestrationAgent(${agent.id})">编排</button>
                        <button onclick="deleteAgent(${agent.id})">删除</button>
                        ${publishButton} <!-- 根据发布状态显示按钮 -->
                    </td>
                `;
                tbody.appendChild(row);
            });

            renderPagination(data.totalPage, pageNumber);  // 渲染分页
        });
}


// 渲染分页
function renderPagination(totalPages, currentPage) {
    let paginationContainer = document.getElementById('pagination');
    paginationContainer.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
        let button = document.createElement('button');
        button.textContent = i;
        button.onclick = () => loadAgentList(i);
        if (i === currentPage) {
            button.style.fontWeight = 'bold';
        }
        paginationContainer.appendChild(button);
    }
}

// 编辑智能体
function editAgent(agentId) {
    currentAgentId = agentId;

    // 构造请求 URL，直接将参数附加到 URL 中
    const url = `/agent/getLagiAgent?agentId=${agentId}&lagiUserId=${globalUserId}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            // 填充数据到表单
            document.getElementById("agent-name").value = data.data.name;
            document.getElementById("platform").value = data.data.driver;
            document.getElementById("token").value = data.data.token;
            document.getElementById("app-id").value = data.data.appId;
            document.getElementById("isFeeRequiredYes").checked = data.data.isFeeRequired;
            document.getElementById("isFeeRequiredNo").checked = !data.data.isFeeRequired;
            document.getElementById("pricePerReq").value = data.data.pricePerReq;

            // 根据是否收费，显示/隐藏收费金额输入框
            const pricePerReqContainer = document.getElementById("pricePerReqContainer");
            if (data.data.isFeeRequired) {
                pricePerReqContainer.style.display = "block";  // 显示收费金额输入框
            } else {
                pricePerReqContainer.style.display = "none";  // 隐藏收费金额输入框
            }

            // 打开模态框
            openAgentModal();
        })
        .catch(error => {
            console.error('Error:', error);  // 捕获并处理错误
        });
}

// 切换发布状态（POST 请求）
async function togglePublishStatus(agentId, currentPublishStatus) {
    // debugger
    // 获取确认信息
    const action = currentPublishStatus ? "发布" : "取消发布";
    const confirmMessage = `确定要${action}该智能体吗？`;
    const flag = await confirm(confirmMessage);
    // 弹出二次确认框
    if (flag) {
        // 构造请求数据
        const requestData = {
            id: agentId,
            publishStatus: currentPublishStatus  // 切换发布状态
        };

        // 发送 POST 请求到 /agent/updateLagiAgent 接口
        fetch('/agent/updateLagiAgent', {
            method: 'POST', // 使用 POST 请求
            headers: {
                'Content-Type': 'application/json' // 请求头设置为 JSON 格式
            },
            body: JSON.stringify(requestData) // 将请求数据转换为 JSON 字符串
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    // 如果更新成功，刷新智能体列表
                    loadAgentList(1);  // 重新加载列表，传入当前页码 1 或者你想要的页码
                } else {
                    alert('发布状态更新失败');
                }
            })
            .catch(error => {
                console.error('发布状态更新失败', error);
            });
    } else {
    }
}


// 删除智能体
async function deleteAgent(agentId) {
    const flag = await confirm('确定要删除这个智能体吗？');
    if (flag) {
        fetch(`/agent/deleteLagiAgentById`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify([agentId])
        }).then(response => response.json())
            .then(data => {
                loadAgentList(1); // 刷新列表
            });
    }
}

// 加载订阅智能体列表
function loadPaidAgentList(pageNumber) {
    fetch(`/agent/getPaidAgentByUser?lagiUserId=${globalUserId}&pageNumber=${pageNumber}&pageSize=10`)
        // fetch(`/agent/getPaidAgentByUser?lagiUserId=${60}&pageNumber=${pageNumber}&pageSize=10`)
        .then(response => response.json())
        .then(data => {
            let tbody = document.querySelector("#paid-agent-list tbody");
            tbody.innerHTML = ''; // 清空表格内容

            data.data.forEach(agent => {
                // 获取 driver 的翻译
                const driverName = driverMap[agent.driver] || agent.driver; // 如果没有找到映射，则显示原值

                let row = document.createElement('tr');

                row.innerHTML = `
                    <td>${agent.name}</td>
                    <td>${driverName}</td>
                    <td>${agent.isFeeRequired ? agent.pricePerReq : '0'}</td>
                    <td>${agent.balance}</td>
                `;
                tbody.appendChild(row);
            });

            renderPaidPagination(data.totalPage, pageNumber);  // 渲染分页
        });
}

// 渲染分页按钮
function renderPaidPagination(totalPage, currentPage) {
    let paginationContainer = document.getElementById("paid-agent-pagination");
    paginationContainer.innerHTML = '';  // 清空分页容器

    for (let i = 1; i <= totalPage; i++) {
        let button = document.createElement("button");
        button.textContent = i;
        button.onclick = () => loadPaidAgentList(i);
        if (i === currentPage) {
            button.style.fontWeight = 'bold';
        }
        paginationContainer.appendChild(button);
    }
}

// 显示 div 的函数
function showHelpButton() {
    document.getElementById("help-button").style.display = "flex";
}

// 隐藏 div 的函数
function hideHelpButton() {
    document.getElementById("help-button").style.display = "none";
}

// 显示 div 的函数
function showFooterInfo() {
    document.getElementById("footer-info").style.display = "block";
}

// 隐藏 div 的函数
function hideFooterInfo() {
    document.getElementById("footer-info").style.display = "none";
}

//========= 查看收费智能体js=============

async function handleSelect(selectedItem, userQuestion) {
    if (!getCookie('userId')) {
        openModal();
        return;
    }
    const selectedValue = selectedItem.value;
    const selectedOption = selectedItem.options[selectedItem.selectedIndex];
    const pricePerReq = selectedOption.getAttribute('data-priceperreq');
    const agentName = selectedOption.textContent.trim();
    if (selectedValue === 'default') {
        return;
    }
    selectedAgentId = selectedValue;
    // 判断是否是用户自己发布的智能体
    try {
        const isUserAgent = await isAgentBelongsToUser(globalUserId, selectedAgentId);
        if (isUserAgent) {
            // 如果是用户发布的智能体，可以跳过订阅检查，直接进行下一步
            await appointTextQuery(userQuestion, selectedAgentId);
        } else {
            // 如果不是用户发布的智能体，继续判断是否订阅
            const agentData = await checkSubscription(selectedAgentId);
            if (agentData) {
                // 如果余额小于零，弹出提示并打开收费框
                if (agentData.balance <= 0) {
                    let flag = await confirm('余额不足，请充值！');
                    if (flag) {
                        openRechargeModal(agentName, pricePerReq);
                    }
                } else {
                    // 使用 await 等待 appointTextQuery 异步调用完成
                    await appointTextQuery(userQuestion, selectedAgentId);
                }
            } else {
                // 如果没有订阅该智能体，弹出提示
                let flag = await confirm(`该智能体 (${agentName}) 需要订阅才能使用！`);
                if (flag) {
                    openRechargeModal(agentName, pricePerReq);
                }
            }
        }
    } catch (error) {
        console.error("操作失败:", error);
        alert("操作失败，请稍后再试！");
    }
}




function openRechargeModal(agentName, pricePerReq) {
    // 获取弹框元素
    const overlay = document.getElementById('recharge-modal');
    const agentNameElement = document.querySelector('.recharge-agent-name');
    const priceElement = document.querySelector('.recharge-price');

    // 更新智能体名称和单次调用价格
    agentNameElement.textContent = `智能体名称：${agentName}`;
    priceElement.textContent = `¥ ${pricePerReq}`;

    // 更新底部的总金额
    const totalAmountFooterElement = document.getElementById('recharge-total-amount-footer');
    totalAmountFooterElement.textContent = `¥ ${pricePerReq}`;

    // 显示弹框
    if (overlay) {
        overlay.style.display = 'flex';
    }


    // 更新单次调用价格
    const callCountInput = document.getElementById('recharge-call-count');
    callCountInput.addEventListener('input', function () {
        updateTotalAmount(pricePerReq);
    });

    // 初始化金额
    updateTotalAmount(pricePerReq);
}

// 计算充值金额的函数
function updateTotalAmount(pricePerReq) {
    const callCountInput = document.getElementById('recharge-call-count');
    const totalAmountFooterElement = document.getElementById('recharge-total-amount-footer');
    const callCount = parseInt(callCountInput.value, 10);
    // 如果输入无效，设置默认值为 1
    if (isNaN(callCount) || callCount < 1) {
        callCountInput.value = 1;
        return;
    }
    // 计算总金额
    const totalAmount = pricePerReq * callCount;
    // 使用 toFixed() 保留两位小数
    const formattedTotalAmount = totalAmount.toFixed(2);
    // 更新总金额显示
    totalAmountFooterElement.textContent = `¥ ${formattedTotalAmount}`;
}



// 关闭弹框
function closeRechargeModal() {
    const overlay = document.getElementById('recharge-modal');
    const callCountInput = document.getElementById('recharge-call-count');
    callCountInput.value = 1;
    overlay.style.display = 'none';
}

// 订阅按钮点击事件
function subscription() {
    const totalAmountFooterElement = document.getElementById('recharge-total-amount-footer');
    const totalAmount = totalAmountFooterElement.textContent.trim().replace('¥', '').trim();
    // 检查 agentId 是否存在
    if (!selectedAgentId) {
        alert('请选择一个智能体');
        return;
    }
    // 调用 showQrCode 函数
    showQrCode(globalUserId, selectedAgentId, totalAmount);
    closeRechargeModal();
}

function showQrCode(lagiUserId, agentId, fee) {
    if (isMobileDevice()) {
        $('#h5PrepayBtn').show();
    } else {
        $('#h5PrepayBtn').hide();
    }
    $.ajax({
        url: "/agent/prepay",
        contentType: "application/json;charset=utf-8",
        type: "post",
        data: JSON.stringify({
            "lagiUserId": lagiUserId,
            "agentId": agentId,
            "fee": fee,
        }),
        success: function (res) {
            if (res.result === '1') {
                document.getElementById("qrCode").src = "data:image/png;base64," + res.qrCode;
                $('#payAmount').text(res.totalFee);
                $('#wechat_pay_qr').show();
                interval = setInterval(function () {
                    getAgentChargeDetail(res.outTradeNo);
                }, 1000);
            } else {
                alert("获取收款码失败");
            }
        }
    });
}

// 查询支付状态
function getAgentChargeDetail(outTradeNo) {
    $.ajax({
        url: "/agent/getAgentChargeDetail",
        contentType: "application/json;charset=utf-8",
        type: "get",
        data: { 'outTradeNo': outTradeNo },
        success: function (res) {
            if (res.status === 1) {
                clearInterval(interval);
                alert("支付成功");
                $('#wechat_pay_qr').hide();
            }
        }
    });
}

function cancelPayment() {
    // 隐藏支付弹框
    $('#wechat_pay_qr').hide();
    // 清除二维码和支付信息
    document.getElementById("qrCode").src = "";
    $('#payAmount').text('');
    // 清除支付查询的定时器
    clearInterval(interval);
}

function isMobileDevice() {
    const userAgent = navigator.userAgent || navigator.vendor || window.opera;
    const mobileRegex = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i;
    return mobileRegex.test(userAgent);
}

function h5Prepay() {
    selectedAgentId = 27;
    const totalAmountFooterElement = document.getElementById('recharge-total-amount-footer');
    const totalAmount = totalAmountFooterElement.textContent.trim().replace('¥', '').trim();
    if (!selectedAgentId) {
        alert('请选择一个智能体');
        return;
    }
    $.ajax({
        url: "/agent/h5Prepay",
        contentType: "application/json;charset=utf-8",
        type: "post",
        data: JSON.stringify({
            "lagiUserId": globalUserId,
            "agentId": selectedAgentId,
            "fee": totalAmount,
        }),
        success: function (res) {
            if (res.result === '1') {
                window.location.href = res.mWebUrl;
                $('#h5PrepayBtn').text('支付中...').css('disabled', true);
            } else {
                alert(res.error);
            }
        }
    });
}

function checkSubscription(agentId) {
    return new Promise((resolve, reject) => {
        fetch(`/agent/getPaidAgentByUser?lagiUserId=${globalUserId}&pageNumber=1&pageSize=1000`)
            .then(response => response.json())
            .then(data => {
                if (data.status === "success") {
                    const agent = data.data.find(agent => agent.id === parseInt(agentId));
                    resolve(agent || null);  // 如果找到该ID的智能体，返回数据，否则返回null
                } else {
                    reject(new Error("接口返回状态异常"));
                }
            })
            .catch(err => {
                reject(err);
            });
    });
}

// 判断智能体是否是用户发布的
async function isAgentBelongsToUser(lagiUserId, agentId) {
    try {
        const response = await fetch(`/agent/getLagiAgent?agentId=${agentId}`);
        if (!response.ok) {
            throw new Error('请求失败，无法获取数据');
        }
        const data = await response.json();
        if (data.status === 'success' && data.data) {
            const agent = data.data;

            if (agent.lagiUserId === lagiUserId) {
                return true;
            }
        }
        return false;
    } catch (error) {
        console.error('请求数据时发生错误:', error);
        return false;
    }
}


// ========= 创建智能体js=============

function openCreateAgent() {
    if (!getCookie('userId')) {
        openModal();
        return;
    }
    document.getElementById("createLagiAgent").style.visibility = 'visible';

    document.getElementById("LagiAgentName").value = '';
    document.getElementById("LagiAgentDescribe").value = '';

    document.getElementById("createLagiAgentButtons").disabled = true;
}

function closeCreateAgent() {
    document.getElementById("createLagiAgent").style.visibility = 'hidden';

    document.getElementById("LagiAgentName").value = '';
    document.getElementById("LagiAgentDescribe").value = '';

    document.getElementById("createLagiAgentButtons").disabled = true;
}


function validateForm() {
    const lagiagentName = document.getElementById("LagiAgentName").value.trim();
    const describeValue = document.getElementById("LagiAgentDescribe").value.trim();
    const createButton = document.getElementById("createLagiAgentButtons");

    if (describeValue === "" || lagiagentName === "") {
        createButton.disabled = true;
    } else {
        createButton.disabled = false;
    }
}

function createLagiAgent() {
    let agentConfig = {
        id: currentAgentId,
        name: document.getElementById("LagiAgentName").value,
        describe: document.getElementById("LagiAgentDescribe").value,
        lagiUserId: globalUserId,
        isFeeRequired: false,
        pricePerReq: 0,
        publishStatus: true
    };
    document.getElementById("createLagiAgentButtons").disabled = true;
    fetch('/agent/createLagiAgent', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(agentConfig),
    })
        .then(response => response.json())
        .then(data => {
            closeCreateAgent();
            alert('智能体创建成功！');
            document.getElementById("createLagiAgentButtons").disabled = false;
        })
        .catch(error => {
            closeCreateAgent();
            alert('创建失败，请重试！');
            document.getElementById("createLagiAgentButtons").disabled = false;
        });
}

document.getElementById("LagiAgentDescribe").addEventListener("input", validateForm);
document.getElementById("LagiAgentName").addEventListener("input", validateForm);

// ========= 编排智能体js开始=============
let canvas = document.getElementById("orchestration-canvas");
let xmlns = "http://www.w3.org/2000/svg";
let scaleFactor = 1;
let offsetX = 0, offsetY = 0;

// Expanded Node Types
const NODE_TYPES = {
    START: 'start',
    END: 'end',
    PROCESS: 'process',
    DECISION: 'decision',
    DOCUMENT: 'document',
    SUBPROCESS: 'subprocess',
    DATABASE: 'database',
    NOTE: 'note',
    ONPAGE_REF: 'onpage_ref'
};

// Structure Types
const STRUCTURE_TYPES = {
    SEQUENCE: 'sequence',
    BRANCH: 'branch',
    LOOP_WHILE: 'loop_while',
    LOOP_UNTIL: 'loop_until'
};

// Node Data Model
let nodes = [];
let connections = [];

// Open Orchestration Editor
function orchestrationAgent(agentId) {
    document.getElementById("orchestration-modal-container").style.display = "flex";
    document.getElementById("orchestration-modal").setAttribute("data-agent-id", agentId);
    nodes = [];
    connections = [];
    updateCanvas();
}

// Close Orchestration Editor
function closeOrchestrationAgent() {
    document.getElementById("orchestration-modal-container").style.display = "none";
    let tbody = document.querySelector("#orchestration-table tbody");
    tbody.innerHTML = "";
    updateCanvas();
}

// Add New Row
function addRow() {
    let tbody = document.querySelector("#orchestration-table tbody");
    let row = document.createElement("tr");
    row.innerHTML = `
        <td class="orchestration-task">
            <input type="text" placeholder="任务名称" oninput="updateCanvas()">
        </td>
        <td class="orchestration-logic">
            <input type="text" placeholder="执行逻辑" oninput="updateCanvas()">
        </td>
        <td class="orchestration-node-type">
            <select onchange="updateCanvas()">
                <option value="${NODE_TYPES.START}">开始</option>
                <option value="${NODE_TYPES.END}">结束</option>
                <option value="${NODE_TYPES.PROCESS}">流程</option>
                <option value="${NODE_TYPES.DECISION}">判定</option>
                <option value="${NODE_TYPES.DOCUMENT}">文档</option>
                <option value="${NODE_TYPES.SUBPROCESS}">子流程</option>
                <option value="${NODE_TYPES.DATABASE}">数据库</option>
                <option value="${NODE_TYPES.NOTE}">注释</option>
                <option value="${NODE_TYPES.ONPAGE_REF}">页面内引用</option>
            </select>
        </td>
        <td>
            <select onchange="updateCanvas()" class="structure-type">
                <option value="${STRUCTURE_TYPES.SEQUENCE}">顺序</option>
                <option value="${STRUCTURE_TYPES.BRANCH}">分支</option>
                <option value="${STRUCTURE_TYPES.LOOP_WHILE}">循环（条件为真时）</option>
                <option value="${STRUCTURE_TYPES.LOOP_UNTIL}">循环（直到条件为真）</option>
            </select>
        </td>
        <td>
            <button class="orchestration-delete-btn" onclick="deleteRow(this)">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M10 11v6M14 11v6" />
                </svg>
            </button>
        </td>
    `;
    tbody.appendChild(row);
    updateCanvas();
}


// Delete Row and Update Canvas
function deleteRow(button) {
    let row = button.parentElement.parentElement;
    row.remove();
    updateCanvas();
}

// Update Canvas
function updateCanvas() {
    canvas.innerHTML = "";
    nodes = [];
    connections = [];

    // Parse Table Data
    const rows = Array.from(document.querySelectorAll("#orchestration-table tbody tr"));
    let prevNode = null;
    let branchStack = []; // Track active branches

    rows.forEach((row, index) => {
        const task = row.querySelector(".orchestration-task input").value || `步骤${index + 1}`;
        const logic = row.querySelector(".orchestration-logic input").value;
        const nodeType = row.querySelector(".orchestration-node-type select").value;
        const structureType = row.querySelector(".structure-type").value;

        // Create Node
        const node = {
            id: `node_${index}`,
            type: nodeType,
            structure: structureType,
            text: task,
            logic: logic,
            x: 0,
            y: 0,
            children: [],
            parents: []
        };
        nodes.push(node);

        // Handle Connections
        if (prevNode && !branchStack.length) {
            connections.push({ from: prevNode.id, to: node.id, type: 'normal' });
            prevNode.children.push(node.id);
            node.parents.push(prevNode.id);
        }

        // Handle Branch Structure
        if (structureType === STRUCTURE_TYPES.BRANCH && nodeType === NODE_TYPES.DECISION) {
            const trueNode = {
                id: `node_${index}_true`,
                type: NODE_TYPES.PROCESS,
                structure: STRUCTURE_TYPES.SEQUENCE,
                text: "是路径",
                logic: "",
                x: 0,
                y: 0,
                children: [],
                parents: [node.id]
            };
            const falseNode = {
                id: `node_${index}_false`,
                type: NODE_TYPES.PROCESS,
                structure: STRUCTURE_TYPES.SEQUENCE,
                text: "否路径",
                logic: "",
                x: 0,
                y: 0,
                children: [],
                parents: [node.id]
            };
            nodes.push(trueNode, falseNode);
            connections.push({ from: node.id, to: trueNode.id, type: 'condition', label: '是' });
            connections.push({ from: node.id, to: falseNode.id, type: 'condition', label: '否' });
            node.children.push(trueNode.id, falseNode.id);
            branchStack.push({ trueNode, falseNode, decision: node });
            prevNode = null; // Pause main flow until branch merges
        } else if (branchStack.length > 0) {
            // Connect within branch
            const lastBranch = branchStack[branchStack.length - 1];
            if (!lastBranch.trueConnected) {
                connections.push({ from: lastBranch.trueNode.id, to: node.id, type: 'normal' });
                lastBranch.trueNode.children.push(node.id);
                node.parents.push(lastBranch.trueNode.id);
                lastBranch.trueConnected = true;
            } else if (!lastBranch.falseConnected) {
                connections.push({ from: lastBranch.falseNode.id, to: node.id, type: 'normal' });
                lastBranch.falseNode.children.push(node.id);
                node.parents.push(lastBranch.falseNode.id);
                lastBranch.falseConnected = true;
                branchStack.pop(); // Merge branches here
                prevNode = node; // Resume main flow
            }
        } else {
            prevNode = node;
        }
    });

    // Auto Layout
    autoLayout(nodes, connections);

    // Render
    renderNodes(nodes);
    renderConnections(connections);
}

// Improved Auto Layout Algorithm
function autoLayout(nodes, connections) {
    const BASE_X = 400;
    const BASE_Y = 100;
    const H_SPACING = 300; // Increased for horizontal branching
    const V_SPACING = 150;
    const BRANCH_OFFSET = 0; // Align branches horizontally

    const nodeMap = new Map();
    nodes.forEach(node => nodeMap.set(node.id, node));

    // Build Relationships
    nodes.forEach(node => {
        node.children = [];
        node.parents = [];
    });
    connections.forEach(conn => {
        const parent = nodeMap.get(conn.from);
        const child = nodeMap.get(conn.to);
        if (parent && child) {
            parent.children.push(child);
            child.parents.push(parent);
        }
    });

    // Recursive Layout
    function layoutNode(node, x, y, visited = new Set(), branchLevel = 0) {
        if (visited.has(node.id)) return;
        visited.add(node.id);

        node.x = x + (branchLevel * H_SPACING);
        node.y = y;

        if (node.structure === STRUCTURE_TYPES.BRANCH && node.type === NODE_TYPES.DECISION) {
            const trueChild = node.children.find(c => c.id.includes('true'));
            const falseChild = node.children.find(c => c.id.includes('false'));
            if (trueChild) layoutNode(trueChild, x + H_SPACING / 2, y + V_SPACING, visited, 0);
            if (falseChild) layoutNode(falseChild, x - H_SPACING / 2, y + V_SPACING, visited, 0);
        } else if (node.structure.startsWith('loop')) {
            node.children.forEach((child, idx) => {
                layoutNode(child, x, y + V_SPACING * (idx + 1), visited, branchLevel);
                connections.push({ from: child.id, to: node.id, type: 'loop' });
            });
        } else {
            node.children.forEach((child, idx) => {
                layoutNode(child, x, y + V_SPACING * (idx + 1), visited, branchLevel);
            });
        }
    }

    const rootNode = nodes.find(n => n.parents.length === 0);
    if (rootNode) layoutNode(rootNode, BASE_X, BASE_Y);
}

// Render Nodes
function renderNodes(nodes) {
    const defs = document.createElementNS(xmlns, "defs");
    const marker = document.createElementNS(xmlns, "marker");
    marker.id = "arrowhead";
    marker.setAttribute("viewBox", "0 0 10 10");
    marker.setAttribute("refX", "9");
    marker.setAttribute("refY", "3");
    marker.setAttribute("markerWidth", "6");
    marker.setAttribute("markerHeight", "6");
    marker.setAttribute("orient", "auto");
    const arrow = document.createElementNS(xmlns, "path");
    arrow.setAttribute("d", "M0,0 L0,6 L9,3 z");
    arrow.setAttribute("fill", "#666");
    marker.appendChild(arrow);
    defs.appendChild(marker);
    canvas.appendChild(defs);

    nodes.forEach(node => {
        let shape;
        const width = 120;
        const height = 60;
        const radius = 30;

        switch (node.type) {
            case NODE_TYPES.START:
            case NODE_TYPES.END:
                shape = document.createElementNS(xmlns, "circle");
                shape.setAttribute("cx", node.x);
                shape.setAttribute("cy", node.y);
                shape.setAttribute("r", radius);
                shape.setAttribute("fill", node.type === NODE_TYPES.START ? "#4CAF50" : "#F44336");
                shape.setAttribute("stroke-width", node.type === NODE_TYPES.END ? "4" : "2");
                break;

            case NODE_TYPES.PROCESS:
                shape = document.createElementNS(xmlns, "rect");
                shape.setAttribute("x", node.x - width / 2);
                shape.setAttribute("y", node.y - height / 2);
                shape.setAttribute("width", width);
                shape.setAttribute("height", height);
                shape.setAttribute("rx", 5);
                shape.setAttribute("fill", "#4CAF50"); // Green for process nodes
                break;

            case NODE_TYPES.DECISION:
                shape = document.createElementNS(xmlns, "path");
                const d = `M ${node.x} ${node.y - height / 2} L ${node.x + width / 2} ${node.y} L ${node.x} ${node.y + height / 2} L ${node.x - width / 2} ${node.y} Z`;
                shape.setAttribute("d", d);
                shape.setAttribute("fill", "#FFCA28"); // Yellow for decision
                break;

            case NODE_TYPES.DOCUMENT:
                shape = document.createElementNS(xmlns, "path");
                const docPath = `M ${node.x - width / 2} ${node.y - height / 2} H ${node.x + width / 2} V ${node.y + height / 2 - 10} Q ${node.x + width / 4} ${node.y + height / 2 + 10}, ${node.x - width / 2} ${node.y + height / 2 - 10} Z`;
                shape.setAttribute("d", docPath);
                shape.setAttribute("fill", "#9C27B0");
                break;

            case NODE_TYPES.SUBPROCESS:
                shape = document.createElementNS(xmlns, "rect");
                shape.setAttribute("x", node.x - width / 2);
                shape.setAttribute("y", node.y - height / 2);
                shape.setAttribute("width", width);
                shape.setAttribute("height", height);
                shape.setAttribute("rx", 5);
                shape.setAttribute("fill", "#673AB7");
                const innerRect = document.createElementNS(xmlns, "rect");
                innerRect.setAttribute("x", node.x - width / 2 + 5);
                innerRect.setAttribute("y", node.y - height / 2 + 5);
                innerRect.setAttribute("width", width - 10);
                innerRect.setAttribute("height", height - 10);
                innerRect.setAttribute("fill", "none");
                innerRect.setAttribute("stroke", "#fff");
                innerRect.setAttribute("stroke-width", "2");
                canvas.appendChild(innerRect);
                break;

            case NODE_TYPES.DATABASE:
                shape = document.createElementNS(xmlns, "path");
                const dbPath = `M ${node.x - width / 2} ${node.y - height / 2} A ${width / 2} 10 0 0 1 ${node.x + width / 2} ${node.y - height / 2} V ${node.y + height / 2} A ${width / 2} 10 0 0 1 ${node.x - width / 2} ${node.y + height / 2} Z`;
                shape.setAttribute("d", dbPath);
                shape.setAttribute("fill", "#FF5722");
                break;

            case NODE_TYPES.NOTE:
                shape = document.createElementNS(xmlns, "path");
                const notePath = `M ${node.x - width / 2} ${node.y - height / 2} H ${node.x + width / 2 - 10} L ${node.x + width / 2} ${node.y - height / 2 + 10} V ${node.y + height / 2} H ${node.x - width / 2} Z`;
                shape.setAttribute("d", notePath);
                shape.setAttribute("fill", "#8BC34A");
                break;

            case NODE_TYPES.ONPAGE_REF:
                shape = document.createElementNS(xmlns, "circle");
                shape.setAttribute("cx", node.x);
                shape.setAttribute("cy", node.y);
                shape.setAttribute("r", radius);
                shape.setAttribute("fill", "#607D8B");
                shape.setAttribute("stroke-dasharray", "5,5");
                break;
        }

        shape.setAttribute("stroke", "#333");
        shape.setAttribute("stroke-width", "2");
        canvas.appendChild(shape);

        // Add Text
        addText(node.x, node.y - 5, node.text, 12, "bold");
        if (node.logic) addText(node.x, node.y + 15, node.logic, 10);
    });
}

// Render Connections
function renderConnections(connections) {
    connections.forEach(conn => {
        const fromNode = nodes.find(n => n.id === conn.from);
        const toNode = nodes.find(n => n.id === conn.to);
        if (!fromNode || !toNode) return;

        let path;
        if (conn.type === 'loop') {
            path = document.createElementNS(xmlns, "path");
            const controlX = fromNode.x - 150;
            const controlY = (fromNode.y + toNode.y) / 2;
            path.setAttribute("d", `M ${fromNode.x} ${fromNode.y} C ${controlX} ${fromNode.y}, ${controlX} ${toNode.y}, ${toNode.x} ${toNode.y}`);
            path.setAttribute("stroke-dasharray", "5,5");
        } else if (fromNode.type === NODE_TYPES.DECISION) {
            path = document.createElementNS(xmlns, "line");
            path.setAttribute("x1", fromNode.x);
            path.setAttribute("y1", fromNode.y);
            path.setAttribute("x2", toNode.x);
            path.setAttribute("y2", toNode.y);
        } else {
            path = document.createElementNS(xmlns, "line");
            path.setAttribute("x1", fromNode.x);
            path.setAttribute("y1", fromNode.y);
            path.setAttribute("x2", toNode.x);
            path.setAttribute("y2", toNode.y);
        }

        path.setAttribute("stroke", "#333");
        path.setAttribute("fill", "none");
        path.setAttribute("stroke-width", "2");
        path.setAttribute("marker-end", "url(#arrowhead)");
        canvas.appendChild(path);

        if (conn.label) {
            const labelX = (fromNode.x + toNode.x) / 2;
            const labelY = (fromNode.y + toNode.y) / 2 - 20; // Position above the line
            addText(labelX, labelY, conn.label, 12, "bold");
        }
    });
}

// Helper Function: Add Text
function addText(x, y, text, size, weight = "normal") {
    const textElem = document.createElementNS(xmlns, "text");
    textElem.setAttribute("x", x);
    textElem.setAttribute("y", y);
    textElem.setAttribute("text-anchor", "middle");
    textElem.setAttribute("font-size", size);
    textElem.setAttribute("font-weight", weight);
    textElem.setAttribute("fill", "#000"); // Black text for visibility
    textElem.textContent = text;
    canvas.appendChild(textElem);
}

// Enable Canvas Drag
function enableCanvasDrag() {
    let canvasContainer = document.getElementById("orchestration-canvas-container");
    let isDragging = false;
    let startX, startY;

    canvasContainer.addEventListener("mousedown", (e) => {
        isDragging = true;
        startX = e.clientX;
        startY = e.clientY;
    });

    canvasContainer.addEventListener("mousemove", (e) => {
        if (isDragging) {
            let offsetX = e.clientX - startX;
            let offsetY = e.clientY - startY;
            canvasContainer.scrollLeft -= offsetX;
            canvasContainer.scrollTop -= offsetY;
            startX = e.clientX;
            startY = e.clientY;
        }
    });

    canvasContainer.addEventListener("mouseup", () => {
        isDragging = false;
    });
}

// Enable Canvas Zoom
function enableCanvasZoom() {
    let canvasContainer = document.getElementById("orchestration-canvas-container");
    canvasContainer.addEventListener("wheel", (e) => {
        e.preventDefault();
        let zoom = e.deltaY > 0 ? 0.9 : 1.1;
        scaleFactor *= zoom;
        scaleFactor = Math.max(0.5, Math.min(scaleFactor, 3));
        canvas.style.transform = `scale(${scaleFactor})`;
        const rect = canvasContainer.getBoundingClientRect();
        const offsetX = e.clientX - rect.left;
        const offsetY = e.clientY - rect.top;
        canvasContainer.scrollLeft = (canvasContainer.scrollLeft + offsetX) * zoom - offsetX;
        canvasContainer.scrollTop = (canvasContainer.scrollTop + offsetY) * zoom - offsetY;
    });
}

enableCanvasDrag();
enableCanvasZoom();

// Placeholder Save Function
function saveOrchestration() {
    console.log("Saving orchestration:", { nodes, connections });
}
// ========= 编排智能体js结束=============