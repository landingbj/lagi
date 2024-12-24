// 页面加载时获取并渲染智能体列表
document.addEventListener('DOMContentLoaded', () => {
    loadAgentMenu(currentPage); // 初始加载第一页的智能体
    const agentToolsElement = document.getElementById('agent-tools');
    agentToolsElement.addEventListener('scroll', handleScroll); // 监听滚动事件
});


// 获取用户ID
let globalUserId = getCookie('userId');

let lastActiveLi = "";  // 用于保存上次激活的 li 元素

let currentAgentId = null; // 设定一个全局变量来标记当前的 agentId

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
        lagiUserId: globalUserId // 将 userId 添加到请求中
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
                    <td>${agent.publishStatus ? '已发布' : '未发布'}</td> <!-- 发布状态 -->
                    <td>
                        <button onclick="editAgent(${agent.id})">编辑</button>
                        <button onclick="deleteAgent(${agent.id})">删除</button>
                        ${publishButton} <!-- 根据发布状态显示按钮 -->
                    </td>
                `;
                tbody.appendChild(row);
            });

            console.log("data.totalPage:" + data.totalPage);
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
        console.log('操作已取消');
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

// 定义当前的页码和每页的大小
let currentPage = 1;
const pageSize = 10;

// 获取智能体列表并动态渲染到页面
async function loadAgentMenu(pageNumber = 1) {
    try {
        const response = await fetch(`/agent/getLagiAgentList?pageNumber=${pageNumber}&pageSize=${pageSize}&publishStatus=true`);
        const data = await response.json();

        if (data.status === 'success' && data.data.length > 0) {
            const agentList = data.data;
            const agentToolsElement = document.getElementById('agent-tools');

            // 遍历智能体列表，生成菜单项
            agentList.forEach(agent => {
                const li = document.createElement('li');
                li.classList.add('pl-5');
                li.textContent = agent.name;

                // 判断是否收费，给不同颜色
                if (agent.isFeeRequired) {
                    li.style.color = 'red';  // 收费的智能体显示为红色
                } else {
                    li.style.color = 'green';  // 免费的智能体显示为绿色
                }

                // 使用 dataset 存储多个自定义属性
                li.dataset.id = agent.id;
                li.dataset.name = agent.name;
                li.dataset.appId = agent.appId;
                li.dataset.token = agent.token;
                li.dataset.driver = agent.driver;
                li.dataset.isFeeRequired = agent.isFeeRequired;
                li.dataset.pricePerReq = agent.pricePerReq;

                // 点击事件：如果是收费的，弹出支付提示
                li.addEventListener('click', function () {
                    const isFeeRequired = agent.isFeeRequired;
                    const price = agent.pricePerReq;
                    const agentId = agent.id;
                    const lagiUserId = getCookie('userId');
                    // 清除所有其他 li 元素的背景色
                    const allLis = agentToolsElement.querySelectorAll('li');
                    allLis.forEach(item => {
                        item.classList.remove('active-agent'); // 移除其他 li 的激活背景
                    });
                    // 保存当前点击的 li 的 agent.id (不在点击时保存，只有确认支付后保存)
                    // lastActiveLi = null; // 先清空上次激活的记录
                    // 如果是收费的，弹框提示用户
                    if (isFeeRequired) {
                        if (!lagiUserId) {
                            openModal();
                            return;
                        }
                        // 判断是否是当前登录用户发布的智能体
                        if (lagiUserId !== agent.lagiUserId) {
                            const userConfirmed = confirm(`该智能体是由其他用户发布的，每次请求费用为 ¥${price} 元。是否继续？`);
                            if (userConfirmed) {
                                showQrCode(lagiUserId, agentId, price, li, agent.appId); // 传递 li 元素
                            } else {
                                // 取消支付时，恢复到上一次激活的 li
                                restoreLastActiveLi(agentToolsElement);
                            }
                        } else {
                            // 激活当前点击的 li 背景色
                            li.classList.add('active-agent'); // 为当前点击的 li 添加激活样式
                            currentAppId = agent.appId;
                            lastActiveLi = agent.id;  // 保存最后激活的 agent.id
                        }
                    } else {
                        // 激活当前点击的 li 背景色
                        li.classList.add('active-agent'); // 为当前点击的 li 添加激活样式
                        currentAppId = agent.appId;
                        lastActiveLi = agent.id;  // 保存最后激活的 agent.id
                    }
                });

                // 将新的 <li> 添加到 <ul> 中
                agentToolsElement.appendChild(li);
            });
        } else {
            console.error('获取智能体列表失败或没有数据');
        }
    } catch (error) {
        console.error('请求出错', error);
    }
}

function restoreLastActiveLi(agentToolsElement) {
    // 如果 lastActiveLi 有值，恢复上一次激活的 li 背景色
    if (lastActiveLi) {
        const allLis = agentToolsElement.querySelectorAll('li');
        allLis.forEach(item => {
            if (Number(item.dataset.id) === lastActiveLi) {  // 将 item.dataset.id 转换为数字
                item.classList.add('active-agent');
            }
        });
    }
}


function showQrCode(lagiUserId, agentId, fee, li, appId) {
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
                // 支付成功后，通知取消支付逻辑来处理激活背景色
                currentAppId = appId;  // 支付成功后，保存当前 appId
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

    // 清除当前激活的 li 背景色
    const agentToolsElement = document.getElementById('agent-tools');

    // 如果 lastActiveLi 有值，恢复上一次激活的 li 背景色
    if (lastActiveLi) {
        const allLis = agentToolsElement.querySelectorAll('li');
        allLis.forEach(item => {
            if (Number(item.dataset.id) === lastActiveLi) {  // 将 item.dataset.id 转换为数字
                item.classList.add('active-agent');
            }
        });
    }
}


// 监听页面滚动事件，触发懒加载
let isLoading = false;  // 控制是否正在加载数据
function handleScroll() {
    const agentToolsElement = document.getElementById('agent-tools');

    // 如果已经在加载数据，直接返回
    if (isLoading) return;

    // 检查容器滚动是否到达底部
    if (agentToolsElement.scrollHeight - agentToolsElement.scrollTop <= agentToolsElement.clientHeight) {
        isLoading = true; // 标记为正在加载

        // 加载下一页数据
        currentPage++;
        loadAgentMenu(currentPage).finally(() => {
            isLoading = false; // 加载完成后恢复标记
        });
    }
}




