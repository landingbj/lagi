// 示例知识库数据
let corpuses = [
    // { id: 1, name: "默认知识库", description: "系统默认创建的知识库", fileCount: 128, createTime: "2023-05-15", isActive: true },
    // { id: 2, name: "项目文档", description: "项目相关的文档和资料", fileCount: 42, createTime: "2023-06-20", isActive: false },
    // { id: 3, name: "技术资料", description: "各种技术文章和教程", fileCount: 76, createTime: "2023-07-10", isActive: false }
];

// 初始化页面
document.addEventListener('DOMContentLoaded', function() {
    // 绑定创建知识库按钮事件
    document.getElementById('addCorpus').addEventListener('click', openCreateModal);
    
    // 加载知识库列表
    loadCorpusList();
});

// 加载知识库列表
function loadCorpusList() {
    const container = document.querySelector('.corpus-list');
    // 清空现有列表，保留创建按钮
    const addButton = container.querySelector('#addCorpus');
    container.innerHTML = '';
    if (addButton) container.appendChild(addButton);
    
    // 添加知识库卡片
    corpuses.forEach(corpus => {
        const card = document.createElement('div');
        card.className = `corpus-card ${corpus.isActive ? 'corpus-activate' : ''}`;
        card.innerHTML = `
            <div class="corpus-card-title">${corpus.name}</div>
            <div class="corpus-card-info">
                <span><i class="fa fa-file-text-o"></i> 文件数: ${corpus.fileCount}</span>
                <span><i class="fa fa-calendar-o"></i> 创建时间: ${corpus.createTime}</span>
            </div>
            <div class="corpus-operation">
                <button class="edit-btn" onclick="enterCorpus(${corpus.id}, this)" title="进入知识库">
                    进入
                </button>
                <button class="delete-btn" onclick="deleteCorpus(${corpus.id})" title="删除知识库">
                    删除
                </button>
            </div>
        `;
        container.appendChild(card);
    });
}

// 打开创建知识库模态框
function openCreateModal() {
    document.getElementById('modalTitle').textContent = '创建知识库';
    document.getElementById('corpusForm').reset();
    document.getElementById('corpusId').value = '';
    document.getElementById('corpusModal').classList.add('active');
}

// 关闭模态框
function closeCorpusModal() {
    document.getElementById('corpusModal').classList.remove('active');
}

// 打开删除确认模态框
function deleteCorpus(id) {
    document.getElementById('deleteCorpusId').value = id;
    document.getElementById('deleteModal').classList.add('active');
}

// 关闭删除确认模态框
function closeDeleteModal() {
    document.getElementById('deleteModal').classList.remove('active');
}

// 确认删除
function confirmDelete() {
    const id = parseInt(document.getElementById('deleteCorpusId').value);
    corpuses = corpuses.filter(corpus => corpus.id !== id);
    loadCorpusList();
    closeDeleteModal();
    showNotification('success', '知识库删除成功');
}

// 进入知识库
function enterCorpus(id, element) {
    // 移除所有激活状态
    document.querySelectorAll('.corpus-card').forEach(card => {
        card.classList.remove('corpus-activate');
    });
    
    // 设置当前知识库为激活状态
    const card = element.closest('.corpus-card');
    card.classList.add('corpus-activate');
    
    // 更新数据
    corpuses.forEach(corpus => {
        corpus.isActive = corpus.id === id;
    });
    hideCorpusList();
    showNotification('info', `已进入 "${corpuses.find(c => c.id === id).name}" 知识库`);
    const detail = loadCorpusDetail(id);
    renderCorpusDetail(detail);
}

function hideCorpusList() {
    const corpuses = document.querySelector('.corpus-list');
    $(corpuses).hide();
}

function showCorpusList() {
    const corpuses = document.querySelector('.corpus-list');
    $(corpuses).show();
}


function loadCorpusDetail(id) {
    return {'name': '知识库名字', 'createTime': '1111', 'category': category, 'setting': {}, 'uploads': {data:[]}};
}

function renderCorpusDetail(detail) {
    // <div class="corpus-detail corpus-container" style="display:none">
    // <h3>${detail.name}</h3>
    // <div class="drop-area" id="dropArea">
    //     <p>将文件拖放到这里或点击选择文件</p>
    //     <input type="file" id="fileInput" />
    //     <button class="button" onclick="document.getElementById('fileInput').click()">选择文件</button>
    // </div>
    // <div id="loadingSpinner" class="hidden"></div> <!-- 加载圈圈 -->
    // <div id="results_file" class="my-table">
    //     <table id="upload-file-list1" class="" border="1">
    //     <thead>
    //     <tr>
    //         <th>文件名称</th>
    //         <th>文件</th>
    //         <th>更新时间</th>
    //         <th>操作</th>
    //     </tr>
    //     </thead>
    //     <tbody>
    //     <!-- 动态生成已上传文件数据 -->
    //     </tbody>
    //     </table>
    // </div>
    // <div class="pagination" id="file-upload-pagination">
    //     <!-- 分页按钮将动态添加到这里 -->
    // </div>
    // </div>
    const detailEl = $($('.corpus-detail')[0]);
    detailEl.show();
    detailEl.find('h4').html(detail.name);
}

function hideCorpusDetail() {
    const detailEl = $($('.corpus-detail')[0]);
    detailEl.hide();
}


// 保存知识库
function saveCorpus() {
    const name = document.getElementById('corpusName').value.trim();
    const desc = document.getElementById('corpusDesc').value.trim();
    const id = document.getElementById('corpusId').value;
    
    if (!name) {
        showNotification('error', '知识库名称不能为空');
        return;
    }
    
    if (id) {
        // 更新现有知识库
        const index = corpuses.findIndex(c => c.id === parseInt(id));
        if (index !== -1) {
            corpuses[index].name = name;
            corpuses[index].description = desc;
            showNotification('success', '知识库更新成功');
        }
    } else {
        // 创建新知识库
        const newId = corpuses.length > 0 ? Math.max(...corpuses.map(c => c.id)) + 1 : 1;
        const now = new Date();
        const dateString = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
        
        corpuses.push({
            id: newId,
            name: name,
            description: desc,
            fileCount: 0,
            createTime: dateString,
            isActive: false
        });
        
        showNotification('success', '知识库创建成功');
    }
    
    loadCorpusList();
    closeCorpusModal();
}

// 显示通知消息
function showNotification(type, message) {
    const notification = document.getElementById('notification');
    const notificationIcon = document.getElementById('notificationIcon');
    const notificationMessage = document.getElementById('notificationMessage');
    
    // 设置通知类型
    notification.className = `notification active ${type}`;
    
    // 设置图标
    if (type === 'success') {
        notificationIcon.className = 'fa fa-check-circle';
    } else if (type === 'error') {
        notificationIcon.className = 'fa fa-exclamation-circle';
    } else if (type === 'info') {
        notificationIcon.className = 'fa fa-info-circle';
    }
    
    // 设置消息
    notificationMessage.textContent = message;
    
    // 显示通知
    notification.classList.add('active');
    
    // 3秒后隐藏通知
    setTimeout(() => {
        notification.classList.remove('active');
    }, 3000);
}



const CorpusApi = {
    // 添加知识库
    addKnowledge(userId) {
      return $.ajax({
        url: '/api/add',
        method: 'GET',
        data: { userId },
        dataType: 'json'
      });
    },
  
    // 删除知识库
    deleteKnowledge(userId, corpusId) {
      return $.ajax({
        url: '/api/delete',
        method: 'POST',
        data: JSON.stringify({ userId, corpusId }),
        contentType: 'application/json',
        dataType: 'json'
      });
    },
  
    // 获取知识库列表
    getKnowledgeList(userId) {
      return $.ajax({
        url: '/api/getList',
        method: 'GET',
        data: { userId },
        dataType: 'json'
      });
    },
  
    // 获取单个知识库
    getKnowledgeBase(userId, knowledgeId) {
      return $.ajax({
        url: '/api/getOne',
        method: 'GET',
        data: { userId, knowledgeId },
        dataType: 'json'
      });
    }
  };
  
  // 使用示例
//   CorpusApi.getKnowledgeList('user123')
//     .done(response => {
//         console.log('获取知识库列表成功:', response);
//     })
//     .fail(error => {
//         console.error('获取知识库列表失败:', error);
//     });