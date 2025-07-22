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
});

// 全局变量，供其他JS文件使用
window.currentKbId = null;

// 全局知识库对象映射，以ID为key
window.knowledgeBaseMap = {};

// 加载知识库选择下拉框
function loadKnowledgeBaseSelect() {
    let userId = globalUserId;
    const select = document.getElementById('knowledge-base-select');
    if (!select) return;
    
    // 清空现有选项，保留默认选项
    select.innerHTML = '<option value="">请选择知识库</option>';
    
    // 调用API获取知识库列表
    KnowledgeBaseAPI.getKnowledgeList(userId, region).then(data => {
        if (data.code !== 0) {
            console.error('获取知识库列表失败:', data.message);
            return;
        }
        
        let knowledgeBases = data.data;
        knowledgeBases.forEach(kb => {
            const option = document.createElement('option');
            option.value = kb.id;
            option.textContent = kb.name;
            select.appendChild(option);
        });
    }).catch(error => {
        console.error('加载知识库列表失败:', error);
    });
}

// 加载知识库列表
function loadCorpusList() {
    let userId = globalUserId
    const container = document.querySelector('.corpus-list');
    // 清空现有列表，保留创建按钮
    const addButton = container.querySelector('#addCorpus');
    container.innerHTML = '';
    if (addButton) container.appendChild(addButton);
    
    // 添加知识库卡片
    KnowledgeBaseAPI.getKnowledgeList(userId, region).then(data=>{
        if(data.code !== 0) {
            throw new Error('读取知识库列表失败');
        }
        let ls =  data.data;
        
        // 清空并重新构建全局知识库映射
        window.knowledgeBaseMap = {};
        
        ls.forEach(corpus => {
            const card = document.createElement('div');
            card.className = `corpus-card`;
            const now = new Date(corpus.createTime);
            const dateString = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
            
            // 根据isPublic字段设置默认状态
            const isDefault = corpus.isPublic;
            const defaultBtnText = isDefault ? '取消默认' : '设为默认';
            const defaultBtnTitle = isDefault ? '取消默认' : '设为默认';
            
            card.innerHTML = `
                <div class="default-badge">默认</div>
                <div class="corpus-card-title">${corpus.name}</div>
                <div class="corpus-card-info">
                    <span><i class="fa fa-file-text-o"></i> 文件数: ${corpus.fileCount}</span>
                    <span><i class="fa fa-calendar-o"></i> 创建时间: ${dateString}</span>
                </div>
                <div class="corpus-operation">
                    <button class="edit-btn" onclick="enterCorpus(${corpus.id}, this)" title="进入知识库">
                        进入
                    </button>
                    <button class="delete-btn" onclick="deleteCorpus(${corpus.id})" title="删除知识库">
                        删除
                    </button>
                    <button class="default-btn" onclick="setAsDefault(${corpus.id}, this)" title="${defaultBtnTitle}">
                        ${defaultBtnText}
                    </button>
                </div>
            `;
            
            // 如果是默认知识库，添加is-default类
            if (isDefault) {
                card.classList.add('is-default');
            }
            
            container.appendChild(card);
            
            // 将知识库对象添加到全局映射中
            window.knowledgeBaseMap[corpus.id] = corpus;
        });
        
        // 同时更新知识库选择下拉框
        loadKnowledgeBaseSelect();
    })
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
    
    // 调用真实的删除接口
    KnowledgeBaseAPI.deleteKnowledge(id).then(data => {
        if (data.code !== 0) {
            showNotification('error', '删除知识库失败: ' + (data.message || '未知错误'));
            return;
        }
        
        // 删除成功，重新加载知识库列表
        loadCorpusList();
        closeDeleteModal();
        showNotification('success', '知识库删除成功');
        
        // 如果删除的是当前选中的知识库，清空当前选择
        if (window.currentKbId === id) {
            window.currentKbId = null;
            hideCorpusDetail();
        }
    }).catch(error => {
        console.error('删除知识库失败:', error);
        showNotification('error', '删除知识库失败，请重试');
    });
}

// 保存检索设置
function saveSearchSettings() {
    if (!window.currentKbId) {
        showNotification('error', '请先选择一个知识库');
        return;
    }
    
    // 获取表单数据
    const similarityThreshold = document.getElementById('similarity-threshold').value;
    const similarityTopK = document.getElementById('similarity-topk').value;
    const fullTextSearch = document.getElementById('full-text-search').checked;
    const llmAssistant = document.getElementById('llm-assistant').checked;
    
    // 验证数据
    if (similarityThreshold < 0 || similarityThreshold > 1) {
        showNotification('error', '文档相似度阈值必须在0-1之间');
        return;
    }
    
    if (similarityTopK < 1 || similarityTopK > 100) {
        showNotification('error', '文档相似度TopK必须在1-100之间');
        return;
    }
    
    // 先获取当前知识库信息
    KnowledgeBaseAPI.getKnowledgeBase(currentKbId).then(data => {
        if (data.code !== 0) {
            showNotification('error', '获取知识库信息失败');
            return;
        }
        
        const kb = data.data;
        
        // 更新知识库设置
        kb.similarityCutoff = parseFloat(similarityThreshold);
        kb.similarityTopK = parseInt(similarityTopK);
        kb.enableFulltext = fullTextSearch;
        kb.enableText2qa = llmAssistant;
        
        // 调用更新接口
        KnowledgeBaseAPI.updateKnowledge(kb).then(updateData => {
            if (updateData.code !== 0) {
                showNotification('error', '保存设置失败');
                return;
            }
            
            showNotification('success', '检索设置保存成功');
        }).catch(err => {
            console.error('更新失败：', err);
            showNotification('error', '保存设置失败');
        });
    }).catch(err => {
        console.error('获取知识库信息失败：', err);
        showNotification('error', '获取知识库信息失败');
    });
}

// 设为默认知识库
function setAsDefault(id, element) {
    const card = element.closest('.corpus-card');
    const isCurrentlyDefault = card.classList.contains('is-default');
    
    if (isCurrentlyDefault) {
        // 如果当前已经是默认，则取消默认
        // 先获取知识库详情，然后更新isPublic为false
        KnowledgeBaseAPI.getKnowledgeBase(id).then(data => {
            if (data.code !== 0) {
                showNotification('error', '获取知识库信息失败');
                return;
            }
            
            const kb = data.data;
            kb.isPublic = false;
            
            KnowledgeBaseAPI.updateKnowledge(kb).then(updateData => {
                if (updateData.code !== 0) {
                    showNotification('error', '取消默认状态失败');
                    return;
                }
                
                card.classList.remove('is-default');
                element.textContent = '设为默认';
                element.title = '设为默认';
                let name = $(card).find('.corpus-card-title').html();
                showNotification('info', `已取消 "${name}" 的默认状态`);
            }).catch(err => {
                console.error('更新失败：', err);
                showNotification('error', '取消默认状态失败');
            });
        }).catch(err => {
            console.error('获取知识库信息失败：', err);
            showNotification('error', '获取知识库信息失败');
        });
    } else {
        // 如果当前不是默认，则设为默认
        // 先获取知识库详情，然后更新isPublic为true
        KnowledgeBaseAPI.getKnowledgeBase(id).then(data => {
            if (data.code !== 0) {
                showNotification('error', '获取知识库信息失败');
                return;
            }
            
            const kb = data.data;
            kb.isPublic = true;
            
            KnowledgeBaseAPI.updateKnowledge(kb).then(updateData => {
                if (updateData.code !== 0) {
                    showNotification('error', '设置默认状态失败');
                    return;
                }
                
                // 移除所有卡片的默认状态
                document.querySelectorAll('.corpus-card').forEach(card => {
                    card.classList.remove('is-default');
                });
                
                // 移除所有按钮的"取消默认"状态
                document.querySelectorAll('.default-btn').forEach(btn => {
                    btn.textContent = '设为默认';
                    btn.title = '设为默认';
                });
                
                // 设置当前卡片为默认状态
                card.classList.add('is-default');
                element.textContent = '取消默认';
                element.title = '取消默认';
                
                let name = $(card).find('.corpus-card-title').html();
                showNotification('success', `已将 "${name}" 设为默认知识库`);
            }).catch(err => {
                console.error('更新失败：', err);
                showNotification('error', '设置默认状态失败');
            });
        }).catch(err => {
            console.error('获取知识库信息失败：', err);
            showNotification('error', '获取知识库信息失败');
        });
    }
}

// 进入知识库
function enterCorpus(id, element) {
    currentKbId = id;
    // 设置当前知识库为激活状态
    const card = element.closest('.corpus-card');
    let name =  $(card).find('.corpus-card-title').html();
    hideCorpusList();
    showNotification('info', `已进入 "${name}" 知识库`);
    const detail = loadCorpusDetail(id);
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
    $('.corpus-detail').show();
    // load settings
    const detailEl = $($('.corpus-detail')[0]);
    detailEl.show();
    KnowledgeBaseAPI.getKnowledgeBase(id).then(data=>{
        if(data.code != 0) {
            throw new Error("加载知识库详情失败");
        }
        const kb = data.data;
        console.log(kb)
        detailEl.find('span').html(kb.name);
        renderSettings(kb);
        loadSearchSettings(kb);
        loadUploadFileList(1, kb.category);
    });
    // return {'name': '知识库名字', 'createTime': '1111', 'category': category, 'setting': {}, 'uploads': {data:[]}};
}


function renderSettings(kb) {
    console.log('渲染设置');
}

// 加载检索设置到表单
function loadSearchSettings(kb) {
    const similarityCutoff = kb.similarityCutoff || 0.8;
    document.getElementById('similarity-threshold').value = similarityCutoff;
    const similarityTopK = kb.similarityTopK || 10;
    document.getElementById('similarity-topk').value = similarityTopK;
    const enableFulltext = kb.enableFulltext || false;
    document.getElementById('full-text-search').checked = enableFulltext;
    const enableText2qa = kb.enableText2qa || false;
    document.getElementById('llm-assistant').checked = enableText2qa;
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
        // const newId = corpuses.length > 0 ? Math.max(...corpuses.map(c => c.id)) + 1 : 1;
        // const now = new Date();
        // const dateString = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
        
        let corpuse = {
            // id: newId,
            name: name,
            description: desc,
            region: region,
            userId: globalUserId,
            // fileCount: 0,
            // createTime: dateString,
            // isActive: false
        };
        KnowledgeBaseAPI.addKnowledge(corpuse).then(data=>{
            if(data.code != 0 || !data.data) {
                showNotification('error', '创建知识库失败');
                return;
            }
            showNotification('success', '创建知识库成功');
            loadCorpusList();
        })
    }
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


const KnowledgeBaseAPI = {
    // 获取区域信息
    getRegion() {
        return $.ajax({
            url: '/knowledge/region',
            method: 'GET',
        });
    },

    // 添加知识库条目
    addKnowledge(knowledgeBase) {
        return $.ajax({
            url: '/knowledge/add',
            method: 'POST',
            data: JSON.stringify(knowledgeBase),
            contentType: 'application/json',
            dataType: 'json'
        });
    },

    // 删除知识库条目
    deleteKnowledge(knowledgeBaseId) {
        return $.ajax({
            url: '/knowledge/delete',
            method: 'POST',
            data: JSON.stringify({ id : knowledgeBaseId }),
            contentType: 'application/json',
            dataType: 'json'
        });
    },

    // 更新知识库条目
    updateKnowledge(knowledgeBase) {
        return $.ajax({
            url: '/knowledge/update',
            method: 'POST',
            data: JSON.stringify(knowledgeBase),
            contentType: 'application/json',
            dataType: 'json'
        });
    },

    // 获取知识库列表
    getKnowledgeList(userId, region) {
        return $.ajax({
            url: `/knowledge/getList?userId=${userId}&region=${region}`,
            method: 'GET',
        });
    },

    // 获取单个知识库条目
    getKnowledgeBase(knowledgeId) {
        return $.ajax({
            url: `/knowledge/getOne?knowledgeId=${knowledgeId}`,
            method: 'GET',
        });
    }
};


let region = null;

$(document).ready(function() {
    // 示例：调用getRegion
    KnowledgeBaseAPI.getRegion()
        .done(data => {
            region = data.data;
            console.log('区域信息：', region);
        })
        .fail(err => console.error('请求失败：', err));
});
