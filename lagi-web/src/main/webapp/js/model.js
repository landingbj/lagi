
let model_templte_map = {
    "Qwen1.5-7B": {
        "name":"Qwen1.5-7B",
        "path":"qwen/Qwen1.5-7B",
        "template":"qwen"
    },
    "DeepSeek-R1-Distill-Qwen-1.5B": {
        "name":"DeepSeek-R1-Distill-Qwen-1.5B",
        "path":"deepseek/DeepSeek-R1-Distill-Qwen-1.5B",
        "template":"deepseek3"
    }
}


let develop = $('#myDevelop');

let manager_models = [];

let currentModelIndex = -1;

let currentModelId = -1;

$(document).ready(function(){
    $('#sel-model').on('change', function(){
        const selectedValue = $(this).val();
        $('#train-form input[name="template"]').val(model_templte_map[selectedValue]["template"]);
        $('#train-form input[name="output-dir"]').val(selectedValue);
    });
    // 模态框的显示与隐藏
    
    var develop_btn = $('#addModeldevelop-btn');
    var span = $('.develop-content .close');

    develop_btn.on('click', function() {
        develop.show();
    });

    span.on('click', function() {
        develop.hide();
    });

    $(window).on('click', function(event) {
        if ($(event.target).is(develop)) {
            develop.hide();
        }
    });

    // 表单提交处理
    $('#modelForm').on('submit', function(event) {
        event.preventDefault();
        addDevelop();
    });

    // 复制模型ID按钮
    $('#modelList').on('click', '.copy-develop-btn', function() {
        copyDevelopedAddress(this);
    });

    // 开启/停止按钮
    $('#modelList').on('click', '.toggle-develop-btn', function() {
        var status = $(this).closest('.model-card').find('.model-status').text().split(': ')[1];
        if (status === '停止') {
            runningDevelop(this);
        } else {
            stopDevelop(this);
        }
    });

    // 删除按钮
    $('#modelList').on('click', '.delete-develop-btn', function() {
        delDevelop(this);
    });


    /************************************** */

    // 显示模态框
    function showModal(title, isEdit) {
        $('#modalTitle').text(title);
        if (isEdit) {
            const model = manager_models[currentModelIndex];
            $('#modelName').val(model.modelName);
            $('#isOnline').prop('checked', model.isOnline == 1 ? true: false);
            if (model.isOnline == 1) {
                $('#onlineFields').show();
                $('#apiKey').val(model.apiKey);
                $('#modelType').val(model.modelType);
                $('#offlineFields').hide();
            } else {
                $('#offlineFields').show();
                $('#modelEndpoint').val(model.endpoint);
                $('#onlineFields').hide();
            }
        } else {
            $('#modeManagerlForm')[0].reset();
            $('#onlineFields').hide();
            $('#offlineFields').show();
        }
        $('#modelModal').show();
    }



    // 切换线上/线下模型字段显示
    $('#isOnline').change(function() {
        if ($(this).is(':checked')) {
            $('#onlineFields').show();
            $('#offlineFields').hide(); 
        } else {
            $('#offlineFields').show();
            $('#onlineFields').hide();
        }
    });

    // 添加/编辑模型
    $('#modeManagerlForm').submit(function(event) {
        event.preventDefault();
        if(currentModelId == -1) {
            addManagerModel();
        } else {
            let modelData = {
                modelName :  $('#modelName').val(),
                isOnline : $('#isOnline').is(':checked') ? 1 : 0,
                apiKey : $('#isOnline').is(':checked') ? $('#apiKey').val() : '',
                modelType: $('#isOnline').is(':checked') ? $('#modelType').val() : '',
                modelEndpoint : $('#isOnline').is(':checked') ? '' : $('#modelEndpoint').val(),
                status: 0,
            }
            updateManagerModel(currentModelId, modelData, true);
        }
    });



    // 添加模型按钮点击事件
    $('#addModelBtn').click(function() {
        currentModelIndex = -1;
        currentModelId = -1;
        showModal('添加模型', false);
    });

    // 编辑模型按钮点击事件
    $(document).on('click', '.editBtn', function() {
        currentModelIndex = $(this).data('index');
        currentModelId = $(this).data('id');
        showModal('编辑模型', true);
    });

    // 删除模型按钮点击事件
    $(document).on('click', '.deleteBtn', function() {
        // const index = $(this).data('index');
        // manager_models.splice(index, 1);
        // renderTable();
        delManagerModel(this);
    });

    // 开启/关闭模型按钮点击事件
    $(document).on('click', '.toggleBtn', function() {
        const index = $(this).data('index');
        //  反轉 01
        manager_models[index].status = manager_models[index].status == 1 ?  0 : 1;
        renderTable();
        updateManagerModel($(this).data('id'), manager_models[index], false);
    });

    // 关闭模态框
    $('.modal-manager .close').click(hideModal);
    $('#modelModal').click(function(event) {
        if ($(event.target).is('#modelModal')) {
            hideModal();
        }
    });
});

function showActivateModule(el) {
    el.removeClass('model-modules-nav-not-active').addClass('model-modules-nav-active');
}

function showNotActivateModule(el) {
    el.removeClass('model-modules-nav-active').addClass('model-modules-nav-not-active');
}


function loadUserModule(el, model, callback) {
    // console.log("loadUserModule", this);
    let modules =  $('.model-module');

    let titles = $('.model-modules-title');
    for (let index = 0; index < titles.length; index++) {
        const title = titles[index];
        if(title == el) {
            showActivateModule($(title));
        } else {
            showNotActivateModule($(title));
        }
    }
    showSelectModuleById();
    window[callback]();
    function showSelectModuleById() {
        for (let index = 0; index < modules.length; index++) {
            const module = modules[index];
            const id = module.id;
            if (id == model) {
                $(module).show();
            } else {
                $(module).hide();
            }
        }
    }
}

/*************************finetue************************************/
function loadFinetuneData() {
    // alert('加载微调数据');
    getUserDatasets();
    getModel();
}

function getUserDatasets() {
    let userId = getCookie('userId');
    let url = `/model/getDataSetInfo?userId=${userId}`;
    fetch(url)
    .then(response => response.json())
    .then(data => {
        const keys = Object.keys(data.data);
        $('#sel-datasets').empty();
        for(let i = 0; i < keys.length; i++) {
            $('#sel-datasets').append(`<label>${keys[i]}:<input type="checkbox" name="datasets" value="${keys[i]}"></label>`);
        }
    })
    .catch(error => {
        console.log('加载用户datasets 失败');
    });
}




function getModel() {
    let url = `/model/getModels`;
    fetch(url)
    .then(response => response.json())
    .then(data => {
        let supportModels = data.data;
        let models = supportModels["models"];
        $('#sel-model').empty();
        for (let index = 0; index < models.length; index++) {
            const model = models[index];
            $('#sel-model').append(`<option value="${model["name"]}">${model["name"]}</option>`);
            model_templte_map[model["name"]] = model;
            if(index == 0) {
                $('#train-form input[name="template"]').val(model_templte_map[model["name"]]["template"]);
                $('#train-form input[name="output-dir"]').val(model["name"]);
            }
        }
    })
    .catch(error => {
        console.log('加载模型列表失败');
    });
}




function uploadUserDatasets() {
    const fileInput = document.getElementById('up-datasets');
    const file = fileInput.files[0];

    if (!file) {
        alert('请先选择一个文件');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);
    let userId = getCookie('userId');
    let url = `/model/uploadDataSet?userId=${userId}`
    fetch(url, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('网络响应不是OK');
        }
        getUserDatasets();
        return response.json();
    })
    .then(data => {
        document.getElementById('up-datasets-status').innerText = `上传成功`;
    })
    .catch(error => {
        console.error('上传失败:', error);
        document.getElementById('up-datasets-status').innerText = `上传失败}`;
    });
}


function deleteUserDatasets() {
    let userId = getCookie('userId');
    let url = `/model/deleteDataset`;
    let checkedValues = $('#sel-datasets input[type="checkbox"]:checked').map(function() {
        return this.value;
    }).get();
    let params = {
        "userId" : userId,
        "datasetNames": checkedValues
    };
    fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(params)}
    )
    .then(response => response.json())
    .then(data => {
        alert('删除用户datasets 成功');
        getUserDatasets();
    })
    .catch(error => {
        alert('删除用户datasets 失败');
    });
}


function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function doTrain(el) {
    disabledTrain(el);
    // await sleep(6000);
    let userId = getCookie('userId');
    const inputValues = {};
    $('#train-form input').each(function(){
        inputValues[$(this).attr('name')] = $(this).val();
    });
    let datasets = $('#sel-datasets input[type="checkbox"]:checked').map(function() {
        return this.value;
    }).get();
    const selectValues = {};
    $('#train-form select').each(function(){
        selectValues[$(this).attr('name')] = $(this).val();
    });
    let params = {
        "userId":userId,
        "fineTuneArgs" : {
            "stage" :"sft",
            "model_name" : selectValues["sel-model"],
            "finetuning_type" : "lora",
            "template" : inputValues["template"], //"qwen",
            "lora_target":"all",
            "dataset" : datasets,
            "cutoff_len" : inputValues["cutoff-length"], //1024,
            "max_samples": inputValues["max-samples"], //1000,
            "overwrite_cache" : true,
            "preprocessing_num_workers" : 16,
            "logging_steps":10,
            "save_steps":500,
            "plot_loss":true,
            "overwrite_output_dir": true,
            "batch_size": inputValues["batch-size"], //1,
            "gradient_accumulation_steps": inputValues["gradient-accumulation"], //2,
            "learning_rate" : inputValues["learning-rate"], // 0.0001,
            "num_train_epochs" : inputValues["epochs"], //3.0,
            "lr_scheduler_type" : selectValues["LR-scheduler"] , // "cosine",
            "warmup_steps" : 1,
            "maximum_gradient_norm" : inputValues["maximum-gradient-norm"], // 1.0
            "compute_type" : selectValues["compute-type"], //"fp16",
            "val_size" : inputValues["val-size"], //0.1,
            "per_device_eval_batch_size": 1,
            "evaluation_strategy" : "steps",
            "eval_steps" : 500,
            "ds_stage": "0",
            "output_dir": inputValues["output-dir"]
        }
    }
    try {
        checkTrainParams(params);
    } catch(error) {
        alert(error);
        enableTrain(el);
        return ;
    }

    function disabledTrain(el) {
        $(el).prop('disabled', true).css("opacity", 0.5);
    }

    function enableTrain(el) {
        $(el).prop('disabled', false).css("opacity", 1);
    }

    $('#train-view-content').empty();
    try {
        const response = await fetch('model/train', {
            method: "POST",
            cache: "no-cache",
            keepalive: true,
            headers: {
                "Content-Type": "application/json",
                "Accept": "text/event-stream",
            },
            body: JSON.stringify(params),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');

        function readStream(params) {
            return reader.read().then(({ done, value }) => {
                if (done) {
                    getLastTrainLossPng(params, $('#train-view-content'));
                    console.log('Connection closed by server');
                    return;
                }
                let msg = decoder.decode(value, { stream: true });
                msg =  msg.slice(5);
                console.log('Received data:', msg);
                $('#train-view-content').append(`<p>${msg}</p>`);
                scrollToButton();
                return readStream(params);
            }).catch(error => {
                console.error('Connection error:', error);
            });
        }

        readStream(params);
    } catch (error) {
        console.error('Fetch error:', error);
    }
    enableTrain(el);
}


function getLastTrainLossPng(trainParam , el) {
    let params = {
        "userId":trainParam["userId"],
        "fineTuneArgs" : {
            "output_dir": trainParam["fineTuneArgs"]["output_dir"]
        }
    }
    fetch('model/getLastTrainLossPng', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let res = data.data;
        if(res) {
            el.append(`<image src="data:image/png;base64,${res}"></image>`);
            scrollToButton();
        } else {
            console.log('获取损失图片失败.');
        }
    })
    .catch(error => {
        console.log('获取损失图片失败');
    });
}

function isNumeric(str) {
    return !isNaN(parseFloat(str)) && isFinite(str);
}


function checkTrainParams(params) {
    let fineTuneArgs =  params["fineTuneArgs"];
    if( !(fineTuneArgs["dataset"]) || fineTuneArgs["dataset"].length == 0) {
        throw new Error(`请选择一个训练的数据集`);
    }
    if(!(fineTuneArgs["template"])) {
        throw new Error(`template 不可为空`);
    }
    if(!isNumeric(fineTuneArgs["cutoff_len"])) {
        throw new Error(`cutoff_len 不可为空或非数字`);
    }
    if(!isNumeric(fineTuneArgs["max_samples"])) {
        throw new Error(`cutoff_len 不可为空或非数字`);
    }
    if(!isNumeric(fineTuneArgs["batch_size"])) {
        throw new Error(`batch-size 不可为空或非数字`);
    }
    if(!isNumeric(fineTuneArgs["gradient_accumulation_steps"] )) {
        throw new Error(`gradient_accumulation_steps 不可为空或非数字`);
    }
    if(!isNumeric(fineTuneArgs["learning_rate"])) {
        throw new Error(`learning_rate 不可为空或非数字`);
    }
    if(!isNumeric(fineTuneArgs["num_train_epochs"])) {
        throw new Error(`num_train_epochs 不可为空或非数字`);
    }
    if(!(fineTuneArgs["lr_scheduler_type"])) {
        throw new Error(`lr_scheduler_type 不可为空或数字`);
    }
    if(!isNumeric(fineTuneArgs["maximum_gradient_norm"])) {
        throw new Error(`maximum_gradient_norm 不可为空或非数字`);
    }
    if(!(fineTuneArgs["compute_type"])) {
        throw new Error(`compute_type 不可为空或数字`);
    }
    if(!isNumeric(fineTuneArgs["val_size"])) {
        throw new Error(`val_size 不可为空或非数字`);
    }
    if(!(fineTuneArgs["output_dir"])) {
        throw new Error(`output_dir 不可为空或数字`);
    }
}


function scrollToButton() {
    var div = $('#train-view-content');
    div.scrollTop(div.prop('scrollHeight'));
}

/*************************develop************************************/
function loadDevelopData() {
    // alert('加载模型部署数据');
    getDevelop();
}


function addDevelop() {
    let userId = getCookie("userId");
    let modelPath = $('#modelPath').val();
    let adapterPath = $('#adapterPath').val();
    let params = {
        "userId" : userId,
        "modelPath":modelPath,
        "adapterPath": adapterPath
    }
    fetch('model/addDevelop', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let res = data.data;
        if(res) {
            getDevelop();
            $('#modelForm')[0].reset();
            develop.hide();
        } else {
            alert('创建启动失败');
        }
    })
    .catch(error => {
        testAddDevelop();
        alert('创建启动失败');
    });
}


function testAddDevelop() {
    let modelPath = "deeepseek";
    let text1 = "停止";
    let statusClass = "pulished";
    let id = '-1';
    let text2 = '启动';
    let newModelCard = `
            <div class="model-card">
                <div class="model-card-info">
                    <div></div>
                    <div class="model-name">模型名: ${modelPath}</div>
                </div>
                <div class="model-status-container"><div class="model-status">是否启动: ${text1}</div> <div class="status-indicator ${statusClass}"></div></div>
                <div class="model-actions">
                    <button class="copy-develop-btn" data-id="${id}">复制模型地址</button>
                    <button class="toggle-develop-btn ${statusClass}" data-id="${id}">${text2}</button>
                    <button class="delete-develop-btn" data-id="${id}" >删除</button>
                </div>
            </div>
        `;
    $('#modelList').append(newModelCard);
}


function delDevelop(el) {
    let id =  $(el).data('id');
    let params = {
        "id": id,
    }
    fetch('model/delDevelop', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let res = data.data;
        if(res) {
            $(el).closest('.model-card').remove();
        }
    })
    .catch(error => {
        alert('删除部署失败');
    });
}


const runningToggleMap = {
    "0": "启动",
    "1": "停止",
    "2": "启动"
}

const runningMap = {
    "0": "停止",
    "1": "启动",
    "2": "启动中"
}

const runningToggleImg = {
    "0": "images/stop.png",
    "1": "images/start.png",
    "2": "images/loading.png",
}

const runningStyleClass = {
    "0": "published",
    "1": "unpublished",
    "2": "publishing",
}

function loadDevelopPageEle(cardInfo) {
    let id = cardInfo["id"];
    let modelPath = cardInfo["modelPath"];
    let status = cardInfo["running"];
    let statusClass = runningStyleClass[status];
    let text1 = runningMap[status];
    let text2 = runningToggleMap[status];
    let img =  runningToggleImg[status];
    return [id, modelPath, statusClass, img, text1, text2]
}


function loadDevelopCard(cardInfo) {
    
    // let id = cardInfo["id"];
    // let modelPath = cardInfo["modelPath"];
    // let status = cardInfo["running"];
    // let statusClass = runningStyleClass[status];
    // let text1 = runningToggleMap[status];
    // let text2 = runningMap[status];
    let [id,  modelPath,  statusClass, img, text1,  text2]   = loadDevelopPageEle(cardInfo);
    let html = `
        <div class="model-card">
        <div class="model-card-info">
            <div class="model-card-icon">
            <img src="images/deepseek.png" alt="">
            </div>
            <div class="model-card-detail">
            <div class="model-name">模型名: ${modelPath}</div>
            <div><span>大语言模型</span></div>
            </div>
        </div>
        <div class="model-status-container"><div class="model-status">是否启动: ${text1}</div> <div class="status-indicator ${statusClass}"></div></div>
        <div class="model-actions">
            <button class="copy-develop-btn" data-id="${id}"><img src="images/copy.png" alt=""> 复制模型地址</button> 
            <button class="toggle-develop-btn ${statusClass}" data-id="${id}"><img src="${img}" alt=""><span>${text2}</span></button>
            <button class="delete-develop-btn" data-id="${id}" ><img src="images/delete.png" alt=""> 删除</button>
        </div>
        </div>
    `
    $('#modelList').append(html);
}


function getDevelop() {
    let userId = getCookie("userId");
    let url = `/model/getDevelop?userId=${userId}`;
    fetch(url)
    .then(response => response.json())
    .then(data => {
        let develops = data.data;
        $('#modelList').empty();
        for (let index = 0; index < develops.length; index++) {
            loadDevelopCard(develops[index]);
        //     let modal_develop = develops[index];
        //     let modelPath = modal_develop["modelPath"];
        //     let statusClass = modal_develop["running"] == 1 ? "unpublished" : "published";
        //     let port = modal_develop["port"];
        //     let id = modal_develop["id"];
        //     let text1 = modal_develop["running"] == 1? "启动": "停止"; 
        //     let text2 = modal_develop["running"] == 1? "停止": "开启"; 
        //     let newModelCard = `
        //         <div class="model-card">
        //             <div class="model-name">模型名: ${modelPath}</div>
        //             <div class="model-status-container"><div class="model-status">是否启动: ${text1}</div> <div class="status-indicator ${statusClass}"></div></div>
        //             <div class="model-actions">
        //                 <button class="copy-develop-btn" data-id="${id}">复制模型地址</button>
        //                 <button class="toggle-develop-btn ${statusClass}" data-id="${id}">${text2}</button>
        //                 <button class="delete-develop-btn" data-id="${id}" >删除</button>
        //             </div>
        //         </div>
        //     `;
        //     $('#modelList').append(newModelCard);
        }
    })
    .catch(error => {
        console.log('加载模型列表失败');
    });
}


function updateRenderDevelop(el, info) {
    let [xId,  modelPath,  statusClass,  img,  text1,  text2]   = loadDevelopPageEle(info);
    $(el).closest('.model-card').find('.model-status').text(`是否启动: ${text1}`);
    $(el).children("img")[0].src = img;
    $($(el).children("span")[0]).text(text2);
    let keys = Object.keys(runningStyleClass);
    for(let i = 0; i < keys.length; i++) {
        const key = keys[i];
        const clazz = runningStyleClass[key];
        if(clazz == statusClass) {
            $(el).addClass(clazz);
            $(el).closest('.model-card').find('.status-indicator').addClass(clazz);
        } else  {
            $(el).removeClass(clazz);
            $(el).closest('.model-card').find('.status-indicator').removeClass(clazz);
        }
    }
}

function runningDevelop(el) {
    let id =  $(el).data('id');
    let params = {
        "id":id,
    }

    let info = {
        id : "id",
        running: "2",
        modelPath: ""
    }
    updateRenderDevelop(el, info);
    fetch('model/start', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let status = data.data;
        if(status) {
            info.running = 1;
        } else {
            info.running = 0;
            alert('启动模型失败');
        }
        updateRenderDevelop(el, info);
    })
    .catch(error => {
        info.running = 0;
        updateRenderDevelop(el, info);
        alert('启动模型失败');
    });
}

function stopDevelop(el) {
    let id =  $(el).data('id');
    let userId = getCookie("userId");
    let params = {
        "id":id,
        "userId": userId
    }
    fetch('model/stop', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let status = data.data;
        if(status) {
            $(el).closest('.model-card').find('.model-status').text('是否启动: 停止');
            $(el).text('开启');
            $(el).removeClass('unpublished').addClass('published');
            $(el).closest('.model-card').find('.status-indicator').removeClass('unpublished').addClass('published');
        } else {
            alert('停止模型失败');
        }
    })
    .catch(error => {
        alert('停止模型失败');
    });
}


function copyDevelopedAddress(el) {
    // var modelId = $(el).closest('.model-card').find('.model-name').text().split(': ')[1];
    const id = $(el).data("id");
    fetch(`model/getDevelopedAddress?id=${id}`)
    .then(response => response.json())
    .then(data => {
        let address = data.data;
        if(address) {
            navigator.clipboard.writeText(address).then(function() {
                alert('模型地址已复制');
            }, function(err) {
                console.error('无法复制文本: ', err);
            });
        }
    })
    .catch(error => {
        console.log('加载模型列表失败');
    });
}

/*************************start model manager************************************/
function loadDevelopManagerData() {
    // alert('加载模型管理数据');
    renderModelType();
    getManagerModels();
}

function addManagerModel() {
    const userId =  getCookie("userId");
    const name = $('#modelName').val();
    const isOnline = $('#isOnline').is(':checked') ? 1 : 0;
    const apiKey = isOnline ? $('#apiKey').val() : '';
    const modelType = isOnline ? $('#modelType').val() : '';
    const modelEndpoint = !isOnline ? $('#modelEndpoint').val() : '';
    let params = {
        userId: userId,
        modelName: name,
        online: isOnline,
        apiKey: apiKey,
        modelType: modelType,
        endpoint: modelEndpoint,
        status: 0,
    }
    fetch('model/addManagerModel', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let res = data.data;
        if(res) {
            hideModal();
            getManagerModels();
        } else {
            alert('添加模型失败');
        }
    })
    .catch(error => {
        alert('添加模型失败');
    });
}

function delManagerModel(el) {
    const id =  $(el).data("id");
    let params = {
        "id": id
    }
    fetch('model/delManagerModel', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let res = data.data;
        if(res) {
            getManagerModels();
        }
    })
    .catch(error => {
        alert('删除模型失败');
    });
}



function updateManagerModel(id, modelData, fresh) {
    const userId =  getCookie("userId");
    const modelName = modelData["modelName"];
    const isOnline = modelData["isOnline"] != undefined ? modelData["isOnline"]: modelData["online"] ;
    const apiKey =  modelData["apiKey"];
    const modelType = modelData["modelType"];
    const modelEndpoint =  modelData["modelEndpoint"] ? modelData["modelEndpoint"] : modelData["endpoint"];
    const status =  modelData["status"];
    let params = {
        id: id,
        userId: userId,
        modelName: modelName,
        online: isOnline,
        apiKey: apiKey,
        modelType: modelType,
        endpoint: modelEndpoint,
        status: status,
    }
    fetch('model/updateManagerModel', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let res = data.data;
        if(res) {
            // if (currentModelIndex === -1) {
            //     manager_models.push({ name, isOnline, apiKey, modelType, endpoint, status: true });
            // } else {
            //     manager_models[currentModelIndex] = { name, isOnline, apiKey, modelType, endpoint, enabled: manager_models[currentModelIndex].status };
            // }
            // renderTable();
            // hideModal();
            if(fresh) {
                getManagerModels();
                hideModal();
            }
        } else {
            alert('更新模型失败');
        }
    })
    .catch(error => {
        alert('更新模型失败');
    });
}

function getManagerModels() {
    const userId =  getCookie("userId");
    let params = {
        userId: userId,
    }
    fetch('model/getManagerModels', {
        method: "POST",
        body: JSON.stringify(params),
    })
    .then(response => response.json())
    .then(data => {
        let res = data.data;
        if(res) {
            manager_models = res;
            renderTable();
        }
    })
    .catch(error => {
        console.log('获取管理模型失败');
    });
}


function renderModelType() {
    let ModelTypesEums = [
        "Baichuan",
        "Claude",
        "DeepSeek",
        "Doubao",
        "Ernie",
        "Gemini",
        "GPT",
        "GPTAzure",
        "Moonshot",
        "Qwen",
        "Sense",
        "Spark",
        "Zhipu"
    ];
    $("#modelType").empty();
    for (let i = 0; i < ModelTypesEums.length; i++) {
        const modelType = ModelTypesEums[i];
        $("#modelType").append(`<option value="${modelType}">${modelType}</option>`);
    }
    $("#modelType").val('Qwen');
}


function renderTable() {
    const tbody = $('#modelTable tbody');
    tbody.empty();
    manager_models.forEach((model, index) => {
        const row = $('<tr>');
        row.append(`<td>${model.modelName}</td>`);
        row.append(`<td>${model.online == 1 ? '是' : '否'}</td>`);
        row.append(`<td><span>${model.apiKey ? "*************************" : '-'}</span> ${model.apiKey ? `<button class="show-eyes" onclick="toggleApiKey(this, ${index})"  data-show="0" ></button>`: ''}</td>`);
        row.append(`<td>${model.modelType || '-'}</td>`);
        row.append(`<td>${model.endpoint || '-'}</td>`);
        row.append(`<td><button class="toggleBtn" data-index="${index}" data-id="${model.id}" >${model.status ? '开启' : '关闭'}</button></td>`);
        row.append(`<td>
            <button class="editBtn" data-index="${index}" data-id="${model.id}" >编辑</button>
            <button class="deleteBtn" data-index="${index}" data-id="${model.id}">删除</button>
        </td>`);
        tbody.append(row);
    });
}

function toggleApiKey(el, index) {
    let apiKeyEl = $(el).parent().find('span');
    let toggleBtn = $(el);
    let show =  toggleBtn.data("show");
    if(show == "0") {
        toggleBtn.data("show", 1);
        toggleBtn.removeClass("show-eyes").addClass("hide-eyes");
        apiKeyEl.text(manager_models[index].apiKey);
    } else {
        toggleBtn.data("show", 0);
        toggleBtn.removeClass("hide-eyes").addClass("show-eyes");
        apiKeyEl.text("*************************");
    }
}

 // 隐藏模态框
 function hideModal() {
    currentModelId = -1;
    currentModelIndex = -1;
    $('#modelModal').hide();
}