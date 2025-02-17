function loadUploadFileList(pageNumber) {
    fetch(`/uploadFile/getUploadFileList?lagiUserId=${globalUserId}&pageNumber=${pageNumber}&pageSize=10`)
    .then(response => {
        if(response.status == 200) {
            return response.json();
        } else {
            return {totalPage:100, pageNumber: 1, data:[{'filename':'name', 'filepath':'path', 'fileId':'1'}]} 
        }
    })
    .then(data => {
        let tbody = document.querySelector("#upload-file-list tbody");
        tbody.innerHTML = ''; // 清空表格内容
        data.data.forEach(uploadFile => {
            // 获取 driver 的翻译
            let row = document.createElement('tr');

            row.innerHTML = `
                <td>${uploadFile.filename}</td>
                <td><a href="/uploadFile/downloadFile?filePath=${uploadFile.filepath}&fileName=${uploadFile.filename}">${uploadFile.filename}</a></td>
                <td>${formatDate(uploadFile.createTime)}</td>
                <td><button onclick="deleteFile('${uploadFile.filename}', '${uploadFile.fileId}')">删除</button></td>
            `;
            tbody.appendChild(row);
        });

        renderFileUploadPagination(data.totalPage, pageNumber);  // 渲染分页
    });
        // 初始化接口调用
        $.ajax({
            url: `/v1/vector/getTextBlockSize?lagiUserId=${globalUserId}&category=${window.category}`,
            method: 'GET',
            success: function(response) {
                if (response.status === 'success') {
                    // 遍历返回的数据，根据 fileType 回填相应的值
                    response.data.forEach(function(item) {
                        if (item.fileType === 'wenben_type') {
                            $('#wenben_type').val(item.chunkSize);
                        } else if (item.fileType === 'biaoge_type') {
                            $('#biaoge_type').val(item.chunkSize);
                        } else if (item.fileType === 'tuwen_type') {
                            $('#tuwen_type').val(item.chunkSize);
                        } else if (item.fileType === 'wendu_type') {
                            $('#wendu_type').val(item.temperature);
                            window.myTemperature = item.temperature;
                        }
                    });
                }
            },
            error: function() {
                alert('初始化失败，请重试');
            }
        });
}


function formatDate(timestamp) {
    // 创建一个 Date 对象
    let date = new Date(timestamp);

    // 获取年、月、日、小时、分钟、秒
    let year = date.getFullYear();
    let month = String(date.getMonth() + 1).padStart(2, '0'); // 月份从 0 开始，需要加 1
    let day = String(date.getDate()).padStart(2, '0');
    let hours = String(date.getHours()).padStart(2, '0');
    let minutes = String(date.getMinutes()).padStart(2, '0');
    let seconds = String(date.getSeconds()).padStart(2, '0');

    // 格式化日期字符串
    let formattedDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

    console.log(formattedDate); // 输出: 2023-01-01 00:00:00
    return formattedDate;
}


function renderFileUploadPagination(totalPage, currentPage) {
    let paginationContainer = document.getElementById("file-upload-pagination");
    paginationContainer.innerHTML = '';  // 清空分页容器

    if (totalPage <= 5) {
        // 如果总页数小于等于5，直接显示所有页码
        for (let i = 1; i <= totalPage; i++) {
            createPageButton(i, currentPage, paginationContainer);
        }
    } else {
        // 总页数大于 5 的分页逻辑
        // 始终显示第一页
        createPageButton(1, currentPage, paginationContainer);

        if (currentPage <= 3) {
            // 当前页在前 3 页，显示 2 到 4 页
            for (let i = 2; i <= 4; i++) {
                createPageButton(i, currentPage, paginationContainer);
            }
            // 显示省略号和最后一页
            let ellipsis = document.createElement("span");
            ellipsis.textContent = "...";
            paginationContainer.appendChild(ellipsis);
            createPageButton(totalPage, currentPage, paginationContainer);
        } else if (currentPage >= totalPage - 2) {
            // 当前页在后 3 页，显示倒数第 4 页到倒数第 2 页
            let ellipsis = document.createElement("span");
            ellipsis.textContent = "...";
            paginationContainer.appendChild(ellipsis);
            for (let i = totalPage - 3; i < totalPage; i++) {
                createPageButton(i, currentPage, paginationContainer);
            }
            createPageButton(totalPage, currentPage, paginationContainer);
        } else {
            // 当前页在中间部分，显示前后两页及省略号
            let ellipsis1 = document.createElement("span");
            ellipsis1.textContent = "...";
            paginationContainer.appendChild(ellipsis1);
            for (let i = currentPage - 1; i <= currentPage + 1; i++) {
                createPageButton(i, currentPage, paginationContainer);
            }
            let ellipsis2 = document.createElement("span");
            ellipsis2.textContent = "...";
            paginationContainer.appendChild(ellipsis2);
            createPageButton(totalPage, currentPage, paginationContainer);
        }
    }
}

function createPageButton(pageNumber, currentPage, container) {
    let button = document.createElement("button");
    button.textContent = pageNumber;
    button.onclick = () => loadUploadFileList(pageNumber);
    if (pageNumber === currentPage) {
        button.style.fontWeight = 'bold';
    }
    container.appendChild(button);
}


async function deleteFile(filename, fileId) {
    let flag = await confirm(`确定要删除文件:${filename}`);
    if(!flag) {
        return ;
    }
    fetch(`/uploadFile/deleteFile`,  
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify([fileId])
        }
    )
    .then(response => {
        if(response.status == 200) {
            return response.json();
        } else {
            throw new Error("删除失败");
        }
    })
    .then(data => {
        if(data.status == 'success') {
            loadUploadFileList(1);
        } else {
            throw new Error("删除失败");
        }
    })
    .catch((error)=>{
        alert("删除失败");
        console.error('Error:', error);
    })
    ;
}

$(document).ready(function() {
    // 设置默认选中的链接并显示 wdy1，隐藏 wdy2
    $('#link1').css('background-color', '#055a7a').css('font-weight', 'bold');
    $('#my-corpus').show();
    $('#chat-settings').hide();

    // 点击事件处理
    $('.navbar a').click(function() {
        // 恢复所有链接的样式
        $('.navbar a').css('background-color', '').css('font-weight', '');

        // 设置点击的链接的样式
        $(this).css('background-color', '#055a7a').css('font-weight', 'bold');

        // 根据点击的链接显示对应的内容
        if ($(this).attr('id') === 'link1') {
            $('#my-corpus').show();
            $('#chat-settings').hide();
            $('#vector-database').hide();
        } else if ($(this).attr('id') === 'link2') {
            $('#my-corpus').hide();
            $('#vector-database').hide();
            $('#chat-settings').show();
        } else if ($(this).attr('id') === 'link3') {
            $('#my-corpus').hide();
            $('#chat-settings').hide();
            $('#vector-database').show();
        }
    });
});
$(document).ready(function() {
    // 默认选择聊天按钮
    $('#chatButton').css('background-color', '#333');
    $('#queryButton').css('background-color', '#055a7a');

    // 监听聊天按钮点击事件
    $('#chatButton').click(function() {
        $(this).css('background-color', '#333');
        $('#queryButton').css('background-color', '#055a7a');
        $('#contentText').text("当前模式为： 聊天 将提供 LLM 的一般知识 和 找到的文档上下文的答案。");
    });

    // 监听查询按钮点击事件
    $('#queryButton').click(function() {
        $(this).css('background-color', '#333');
        $('#chatButton').css('background-color', '#055a7a');
        $('#contentText').text("当前模式为： 查询 将 仅 提供找到的文档上下文的答案。");
    });
});

function submitSettings(type) {
    let chunkSizeValue;
    let temperature;

    // 获取相应输入框中的值
    if (type === 'wenben_type') {
        chunkSizeValue = $('#wenben_type').val();
    } else if (type === 'biaoge_type') {
        chunkSizeValue = $('#biaoge_type').val();
    } else if (type === 'tuwen_type') {
        chunkSizeValue = $('#tuwen_type').val();
    } else if (type === 'wendu_type') {
        temperature = $('#wendu_type').val();
    }

    $.ajax({
        url: '/v1/vector/updateTextBlockSize',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            "userId": String(globalUserId),
            "fileType": type,  // 传递相应的type
            "category": window.category,
            "chunkSize": chunkSizeValue,  // 传递相应的chunkSize
            "temperature": temperature
        }),
        success: function(response) {
            if (response.status === 'success') {
                alert('请求成功');
            } else {
                alert('请求失败，返回状态不正确');
            }
        },
        error: function() {
            alert('请求失败，请重试');
        }
    });

}

function resetSlice(type) {
    let chunkSizeValue;
    let temperature;

    // 获取相应输入框中的值
    if (type === 'wenben_type') {
        chunkSizeValue = $('#wenben_type').val();
    } else if (type === 'biaoge_type') {
        chunkSizeValue = $('#biaoge_type').val();
    } else if (type === 'tuwen_type') {
        chunkSizeValue = $('#tuwen_type').val();
    } else if (type === 'wendu_type') {
        temperature = $('#wendu_type').val();
        window.myTemperature = temperature;
    }

    $.ajax({
        url: 'v1/vector/resetBlockSize',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            "userId": String(globalUserId),
            "fileType": type,  // 传递相应的type
            "category": window.category,
            "chunkSize": chunkSizeValue,  // 传递相应的chunkSize
            "temperature": temperature
        }),
        success: function(response) {
            if (response.status === 'success') {
                alert('请求成功');
            } else {
                alert('请求失败，返回状态不正确');
            }
        },
        error: function() {
            alert('请求失败，请重试');
        }
    });
    // 为刷新按钮添加点击事件
        location.reload();  // 刷新页面

}

// 使用 jQuery 监听滑动条变化，更新显示的值
$(document).ready(function() {
    $('#distance').on('input', function() {
        $('#distance_value').text($(this).val());
    });
});
