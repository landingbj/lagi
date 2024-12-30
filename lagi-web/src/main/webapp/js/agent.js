// 页面加载时获取并渲染智能体列表
document.addEventListener('DOMContentLoaded', () => {
    // loadAgentMenu(currentPage); // 初始加载第一页的智能体
    const agentToolsElement = document.getElementById('agent-tools');
    agentToolsElement.addEventListener('scroll', handleScroll); // 监听滚动事件
});


// 获取用户ID
let globalUserId = getCookie('userId');

let currentAgentId = null; // 设定一个全局变量来标记当前的 agentId

let selectedAgentId = null;  // 全局变量，用来存储选择的 agentId

// 创建 driver 映射关系
const driverMap = {
    "ai.agent.chat.qianfan.XiaoxinAgent": "千帆",
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
function openAgentModal() {
    if (!getCookie('userId')) {
        openModal();
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
function togglePublishStatus(agentId, currentPublishStatus) {
    // debugger
    // 获取确认信息
    const action = currentPublishStatus ? "发布" : "取消发布";
    const confirmMessage = `确定要${action}该智能体吗？`;

    // 弹出二次确认框
    if (window.confirm(confirmMessage)) {
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
function deleteAgent(agentId) {
    if (confirm("确定要删除这个智能体吗？")) {
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
                if (agentData.balance < 0) {
                    alert("余额不足，请充值！");
                    openRechargeModal(agentName, pricePerReq);
                } else {
                    // 使用 await 等待 appointTextQuery 异步调用完成
                    await appointTextQuery(userQuestion, selectedAgentId);
                }
            } else {
                // 如果没有订阅该智能体，弹出提示
                alert(`该智能体 (${agentName}) 需要订阅才能使用！`);
                openRechargeModal(agentName, pricePerReq);
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
    callCountInput.addEventListener('input', function() {
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
                alert(res.error);
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
        data: {'outTradeNo': outTradeNo},
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
