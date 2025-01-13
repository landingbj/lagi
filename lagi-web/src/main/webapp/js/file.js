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
                <td><a href="${uploadFile.filepath}">${uploadFile.filename}</a></td>
                <td>2025/1/10 12:00</td>
                <td><button onclick="deleteFile('${uploadFile.filename}', '${uploadFile.fileId}')">删除</button></td>
            `;
            tbody.appendChild(row);
        });

        renderFileUploadPagination(data.totalPage, pageNumber);  // 渲染分页
    });
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
        button.style.backgroundColor = '#238efc'
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
