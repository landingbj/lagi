
let searchResults = [];

// 渲染搜索结果
function renderResults(results) {
    const resultsContainer = $('#results');
    resultsContainer.empty(); // 清空现有结果
    results.forEach(result => {
        const resultItem = $('<div class="result-item"></div>');
        resultItem.html(`
                    <h3 style="font-size: 18px; margin-bottom: 10px; color: #004f99;">${result.metadata.filename}</h3>
                    <p style="font-size: 14px; line-height: 1.6; color: #555; margin-bottom: 10px;">${result.document.slice(0, 200)}...</p>
                    <p class="distance" style="font-size: 14px; color: #888;">距离：${result.distance}</p>
                    </br>
                    <p class="more" style="color: #007bff; font-size: 14px; cursor: pointer;" onclick="showDetails('${result.id}')">查看详情</p>
                `);
        resultsContainer.append(resultItem);
    });
}

// 打开模态框显示详情
function showDetails(id) {
    const result = searchResults.find(r => r.id === id);
    $('#modalTitle').text(result.metadata.filename);

    let htmlContent = result.document;
    htmlContent = htmlContent.replace(/\\n/g, '</br>');
    let content = marked.parse(htmlContent);
    $('#modalDocument').html(content);
    
    $('#modalId').text(result.id);
    $('#modalFilename').text(result.metadata.filename);
    $('#modalFilepath').text(result.metadata.filepath);
    $('#modalCategory').text(result.metadata.category);
    $('#modalDistance').text(result.distance);
    $('#modalSettings').text(result.metadata.settingList);
    $('#myModal').fadeIn();
}

// 关闭模态框
function closeVectorDetailModal() {
    $('#myModal').fadeOut();
}

// 模拟搜索过程
function search() {
    let searchText = $('#searchText').val().trim();
    let vectorMaxTop = $('#vector-max-top').val().trim() || 30;
    // 如果输入框为空，提示用户输入内容
    if (!searchText) {
        alert('请输入查询内容');
        return;
    }

    const requestData = {
        text: searchText,
        n: vectorMaxTop,
        where: {},
        category: "lagi"
    };

    $.ajax({
        url: '/v1/vector/query',
        type: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify(requestData),
        success: function(response) {
            searchResults = response.data;
            renderResults(response.data);  // 处理返回的数据并更新页面内容
        },
        error: function(xhr, status, error) {
            console.error('请求错误:', error);
        }
    });
}

// 关闭模态框点击事件
$('#close_vector_detail').click(closeVectorDetailModal);

// 点击窗口外部区域关闭模态框
$(window).click(function(event) {
    // if (!$(event.target).is('#myModal')) {
    //     closeVectorDetailModal();
    // }
});

$(document).ready(function () {
    const $dropArea = $('#dropArea');
    // 阻止浏览器默认的拖拽行为
    $dropArea.on('dragenter dragover dragleave drop', function (e) {
        e.preventDefault();
        e.stopPropagation();
    });

    // 当文件被拖入时添加高亮样式
    $dropArea.on('dragenter dragover', function () {
        $dropArea.addClass('highlight');
    });

    // 当文件离开拖拽区域时移除高亮样式
    $dropArea.on('dragleave drop', function () {
        $dropArea.removeClass('highlight');
    });
});