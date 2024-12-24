// 页面加载时获取并渲染智能体列表
document.addEventListener('DOMContentLoaded', () => {
    loadAgentMenu(currentPage); // 初始加载第一页的智能体
    const agentToolsElement = document.getElementById('agent-tools');
    agentToolsElement.addEventListener('scroll', handleScroll); // 监听滚动事件
});


// 获取用户ID
let globalUserId = getCookie('userId');

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
                row.innerHTML = `
                    <td>${agent.name}</td>
                    <td>${driverName}</td>  <!-- 显示翻译后的 driver -->
                    <td>${agent.token}</td>
                    <td>${agent.appId}</td>
                    <td>${agent.isFeeRequired ? '是' : '否'}</td>
                    <td>${agent.isFeeRequired ? agent.pricePerReq : '0'}</td>
                    <td>
                        <button onclick="editAgent(${agent.id})">编辑</button>
                        <button onclick="deleteAgent(${agent.id})">删除</button>
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
        // 发起请求，获取智能体数据
        const response = await fetch(`/agent/getLagiAgentList?pageNumber=${pageNumber}&pageSize=${pageSize}`);
        const data = await response.json();

        if (data.status === 'success' && data.data.length > 0) {
            const agentList = data.data;
            const agentToolsElement = document.getElementById('agent-tools');

            // 遍历智能体列表，生成菜单项
            agentList.forEach(agent => {
                const li = document.createElement('li');
                li.classList.add('pl-5');
                li.textContent = agent.name; // 显示智能体名称

                // 使用 dataset 存储多个自定义属性
                li.dataset.id = agent.id;
                li.dataset.name = agent.name;
                li.dataset.appId = agent.appId;
                li.dataset.token = agent.token;
                li.dataset.driver = agent.driver;
                li.dataset.isFeeRequired = agent.isFeeRequired;
                li.dataset.pricePerReq = agent.pricePerReq;
                li.dataset.lagiUserId = agent.lagiUserId;  // 将 lagiUserId 存储在 dataset 中

                // 根据 isFeeRequired 设置颜色（内联样式或类）
                if (agent.isFeeRequired) {
                    li.style.color = 'red';  // 收费的智能体使用红色
                } else {
                    li.style.color = 'green';  // 免费的智能体使用绿色
                }

                // 添加点击事件，激活背景色
                li.addEventListener('click', function () {
                    // 清除所有其他 li 元素的背景色
                    const allLis = agentToolsElement.querySelectorAll('li');
                    allLis.forEach(item => {
                        item.classList.remove('active-agent'); // 移除其他 li 的激活背景
                    });

                    // 激活当前点击的 li 背景色
                    li.classList.add('active-agent'); // 为当前点击的 li 添加激活样式

                    // 检查是否为当前登录用户的智能体
                    if (agent.lagiUserId !== globalUserId) {
                        // 如果是收费的智能体，弹框提醒
                        if (agent.isFeeRequired) {
                            const price = agent.pricePerReq.toFixed(2); // 格式化为2位小数
                            const userConfirmed = confirm(`该智能体是由其他用户发布的收费智能体，每次请求费用为 ¥${price} 元。是否继续使用？`);
                            // 如果用户点击 "确认"，可以执行相关操作
                            if (userConfirmed) {
                                console.log("用户确认继续使用收费智能体");
                                // 执行你的相关操作，比如向后台发送请求等
                            } else {
                                console.log("用户取消了收费智能体的使用");
                            }
                        }
                    } else {
                        // 如果是当前用户的智能体，可以直接处理
                        console.log("这是当前用户的智能体，无需收费提示");
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



