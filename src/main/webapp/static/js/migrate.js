var nodeMetaDiv = {};
var excelSheetInfo = {};
var nodeNameOptions = {};
var occupiedDiv = [];
var dbInfo = {};
var prevNodeMetaDiv = "";
var dataType = 'Database';
var dataMap = {};

window.onload = function () {
    findAllNodeNameByCategory("smartmp");
    getAspectNamesByCategory("smartmp");
};

function startMigrate() {
	if (dataType === 'Excel') {
		migrateData = migrateExcel();
	} else if (dataType === 'Database') {
		migrateData = migrateDB();
	} else {
		return;
	}
}

function migrateDB() {
	if (jQuery.isEmptyObject(dbInfo)) {
		alert('请先连接数据库');
		return;
	} else if ($('#tableNames').prop('selectedIndex') === 0){
		alert('请先选择数据库表');
		return;
	}  else if ($('#aspectNames').prop('selectedIndex') === 0){
		alert('请先选择切面');
		return;
	}  else if ($('#nodeNames').prop('selectedIndex') === 0){
		alert('请先选择节点表');
		return;
	} else 	if (jQuery.isEmptyObject(dataMap)) {
		alert('请先匹配迁移数据列');
		return;
	} 

	var migrateData = {};
	migrateData['dataType'] = dataType;
	migrateData['dbInfo'] = dbInfo;
	migrateData['table'] = getSelectedOption("tableNames");
	migrateData['aspect'] = getSelectedOption("aspectNames");
	migrateData['data'] = dataMap;

    $.ajax({
        url: 'migrate/dbMigrate', /* 接口域名地址 */
        type: 'post',
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(migrateData),
        success: function (res) {
            alert(res);
        }
    });
}

function migrateExcel() {
	if ($('#srcFileName').prop('value') === ''){
		alert('请先上传Excel');
		return;
	} else if ($('#sheetNames').prop('selectedIndex') === 0){
		alert('请先选择sheet');
		return;
	} else if ($('#aspectNames').prop('selectedIndex') === 0){
		alert('请先选择切面');
		return;
	}  else if ($('#nodeNames').prop('selectedIndex') === 0){
		alert('请先选择节点表');
		return;
	}  else	if (jQuery.isEmptyObject(dataMap)) {
		alert('请先匹配迁移数据列');
		return;
	}
	
	var migrateData = {};
	migrateData['dataType'] = dataType;
	migrateData['fileName'] = document.getElementById("srcFileName").getAttribute("value");
	migrateData['sheet'] = getSelectedOption("sheetNames");
	migrateData['aspect'] = getSelectedOption("aspectNames");
	migrateData['data'] = dataMap;
	
    $.ajax({
        url: 'migrate/excelMigrate', /* 接口域名地址 */
        type: 'post',
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(migrateData),
        success: function (res) {
            alert(res);
        }
    });
}

// 上传文件
function uploadMigrateSrcFile(file) {
    var formData = new FormData();
    formData.append("fileToUpload", file.files[0]);
    $.ajax({
        url: 'uploadFile/uploadMigrateSrcFile', /* 接口域名地址 */
        type: 'post',
        data: formData,
        contentType: false,
        processData: false,
        success: function (res) {
            var obj = eval('(' + res + ')');
            if (obj.result == true) {
            	var sheets = [];
            	var columns = obj.columns;
            	for (var i = 0;i < columns.length;i ++) {
            		sheets.push(columns[i].sheetName);
            		excelSheetInfo[columns[i].sheetName] = columns[i].columnNames;
            	}
            	fillSelectOptions("sheetNames", sheets, "请选择Sheet");
                $('#sheetNames').show();
                document.getElementById("srcFileName").setAttribute("value", obj.fileName);
            } else {
                alert(obj.msg);
            }
        }
    });
}


// 根据category获取节点名称
function findAllNodeNameByCategory(category) {
    $.ajax({
        url: 'model/findAllNodeNameByCategory?category=' + category, /* 接口域名地址 */
        type: 'get',
        success: function (res) {
            var obj = eval('(' + res + ')');
            if (obj.result == true) {
                var data = obj.data;
                fillSelectOptions("nodeNames", data, "请选择");
            } else {
                alert(obj.msg);
            }
        }
    });
}

function fillSelectOptions(eleID, data, defaultContent) {
    document.getElementById(eleID).innerHTML = "";
    var html = "<option selected='selected'>" + defaultContent + "</option>";
    for (var i = 0; i < data.length; i++) {
        html += "<option>" + data[i] + "</option>";
        nodeNameOptions[data[i]] = i;
    }
    document.getElementById(eleID).innerHTML = html;
}

function getAspectNamesByCategory(category) {
    if (category == undefined) {
        category = "";
    }
    $.ajax({
        type: "GET",
        url: "model/getAspectNames",
        data: {'category': category},
        success: function (result) {
            var data = $.parseJSON(result);
            if (data.length > 0) {
                fillSelectOptions("aspectNames", data, "请选择");
            } else {
                alert(result);
            }
        }
    });
}

// 根据nodename获取表中的字段
function findAllMetaByNodeName() {
    var obj = document.getElementById("nodeNames");
    var index = obj.selectedIndex;
    var nodeName = obj.options[index].text;
    if (nodeMetaDiv[nodeName] != undefined) {
        loadRight(nodeName);
    } else {
        $.ajax({
            url: 'model/findAllMetaByNodeName?meta=' + nodeName, /* 接口域名地址 */
            type: 'get',
            success: function (res) {
                var obj = eval('(' + res + ')');
                if (obj.result == true) {
                    nodeMetaDiv[nodeName] = nodeDataToHtml(obj.data, nodeName);
                    loadRight(nodeName);
                } else {
                    alert(obj.msg);
                }
            }
        });
    }
}

function getTableMeta() {
    var obj = document.getElementById("tableNames");
    var index = obj.selectedIndex;
    var tableName = obj.options[index].text;
    var requestData = {};
    for (var key in dbInfo) {
    	requestData[key]  = dbInfo[key];
    }
    requestData['tableName'] = tableName;
    
    $.ajax({
    	type: "POST",
        url: "database/getTableMeta",
        data: requestData,
        async: true,
        dataType: "text",
        success: function (result) {
        	var data = $.parseJSON(result);
        	loadLeft(data.columnNames);
        	occupiedDiv = [];
            var obj = document.getElementById("nodeNames");
            var index = obj.selectedIndex;
            var nodeName = obj.options[index].text;
            prevNodeMetaDiv = '';
            loadRight(nodeName);
        }
    });
}


function changeDataType() {
	if (dataType === 'Excel') {
		$('#excel-upload').hide();
	} else if (dataType === 'Database') {
		$('#db-info').hide();
	}
    var obj = document.getElementById("data_type");
    var index = obj.selectedIndex;
    dataType = obj.options[index].text;
	
	if (dataType === 'Excel') {
		$('#excel-upload').show();
	} else if (dataType === 'Database') {
		$('#db-url').text('');
		$('#db-url').hide();
		$('#tableNames').hide();
		$('#db-info').show();
	}
	
	document.getElementById("right").innerHTML = '';
	document.getElementById("left").innerHTML = '';
	prevNodeMetaDiv = '';
	occupiedDiv = [];
	dataMap = {};
	
	$('#aspectNames option').eq(0).prop('selected', true);
	$('#sheetNames option').eq(0).prop('selected', true);
	$('#tableNames option').eq(0).prop('selected', true);
	$('#nodeNames option').eq(0).prop('selected', true);
}

function loadSheetColumnNames() {
    var obj = document.getElementById("sheetNames");
    var index = obj.selectedIndex;
    var sheetName = obj.options[index].text;
    loadLeft(excelSheetInfo[sheetName]);
}

// 加载左侧数据
function loadLeft(leftData) {
    document.getElementById("left").innerHTML = "";
    if (leftData == undefined) {
        return;
    }
    var leftHTML = "";
    for (var i = 0; i < leftData.length; i++) {
        leftHTML += "<div><span class='label' id='" + leftData[i]
            + "' draggable='true'  ondragstart='drag(event)'>"
            + leftData[i] + "</span></div>";
    }
    document.getElementById("left").innerHTML = leftHTML;
}


// 加载右侧数据
function loadRight(nodeName) {
    var div = "";
    if (occupiedDiv.includes(nodeName)) {
        div = prevNodeMetaDiv;
    } else {
        var rightHTML = nodeMetaDiv[nodeName];
        div = prevNodeMetaDiv + rightHTML;
    }
    document.getElementById("right").innerHTML = div;
}

function nodeDataToHtml(data, nodeName) {
    var html = "";
    for (var i = 0; i < data.length; i++) {
        html += "<div><span class='label label-inverse' ondrop='drop(event)' ondragover='allowDrop(event)'><div>"
            + data[i] + "</div><div class='" + nodeName + " " +  data[i] + "'></div></span></div>";
    }
    return html;
}

function allowDrop(ev) {
    ev.preventDefault();
}

function drag(ev) {
    ev.dataTransfer.setData("Text", ev.target.id);
}


function drop(ev) {
    ev.preventDefault();
    var data = ev.dataTransfer.getData("Text");
    var dest = ev.target.nextElementSibling;
    var src = document.getElementById(data);
    var srcClone = src.cloneNode(true);
    srcClone.removeAttribute("id");
    srcClone.setAttribute("class", "label-important");
    clearText(dest);
    dest.appendChild(srcClone);
    var nodeAttr = dest.getAttribute("class").split(" ");
    var nodeName = nodeAttr[0];
    var columnName = nodeAttr[1];
    
    if (!occupiedDiv.includes(nodeName)) {
        occupiedDiv.push(nodeName);
    }
    prevNodeMetaDiv = document.getElementById("right").innerHTML;
    if (nodeName in dataMap) {
    	dataMap[nodeName].push({"src" : data, "dest" : columnName});
    } else {
    	dataMap[nodeName] = [];
    	dataMap[nodeName].push({"src" : data, "dest" : columnName});
    }
}

function divDrop(ev) {
    ev.preventDefault();
}

function clearText(document) {
    for (var i = document.childNodes.length - 1; i >= 0; i--) {
        var childNode = document.childNodes[i];
        document.removeChild(childNode);
    }
    document.innerHTML = "";
}

function getRelation() {//获取切面关系
    $.ajax({
        type: "POST",
        url: "model/GetRelation",
        data: {'t': new Date().toString(), 'r': Math.random()},
        async: true,
        dataType: "text",
        success: function (obj) {
            var result = $.parseJSON(obj);
            var len = result.length;
            var relation = $("#asp_relation");
            relation.empty();
            var option = $("<option>").text("--请选择切面关系--").val("-1");
            relation.append(option);
            for (var i = 0; i < len; i++) {
                var rel_id = result[i].rel_id;
                var relation_chinese = result[i].relation_chinese;
                option = $("<option>").text(relation_chinese).val(rel_id);
                relation.append(option);
            }
        }
    });
}

function addAspect() {
    var name = $("#asp_table_name").val();
    if (name == "") {
        alert("请输入切面名");
        $("#asp_table_name").focus();
        return false;
    } else if ($("#asp_relation").val() == "-1") {
        $("#asp_relation").focus();
        return false;
    } else {
        $.ajax({
            type: "POST",
            url: "model/AddAspect",
            async: true,
            data: $("#asp_form").serialize(),
            success: function (result) {
                alert(result);
                if (result === '切面添加成功!') {
                    $('#aspectNames').append('<option>' + name + '</option>');
                }
                hideAspectDiv();
            }
        });
    }
}

function getNodeAttrGroup() {
    $.ajax({
        type: "POST",
        url: "model/GetNodeAttrGroup",
        data: {'t': new Date().toString(), 'r': Math.random()},
        async: true,
        dataType: "text",
        success: function (obj) {
            var result = $.parseJSON(obj);
            var len = result.length;
            var attrGroup = $("#attr_group");
            attrGroup.empty();
            var option = $("<option>").text("--请选择属性组合--").val("-1");
            attrGroup.append(option);
            for (var i = 0; i < len; i++) {
                var gid = result[i].gid;
                var attrNames = result[i].attrNames;
                option = $("<option>").text(attrNames).val(gid);
                attrGroup.append(option);
            }
        }
    });
}

function addNodeTable() {
	var name = $("#node_table_alias").val();
    if (name == "") {
        $("#node_table_alias").focus();
        return false;
    } else if ($("#node_table_dim").val() == "") {
        $("#node_table_dim").focus();
        return false;
    } else if ($("#attr_group").val() == "-1") {
        $("#attr_group").focus();
        return false;
    } else {
        $.ajax({
            type: "POST",
            url: "model/AddNodeTable",
            async: true,
            data: $("#node_form").serialize(),
            success: function (result) {
            	if (result === '1') {
            		alert("添加成功！");
            		$('#nodeNames').append('<option>' + name + '</option>');
            		hideNodeDiv();
            	} else {
            		alert("添加失败！");
            	}
            }
        });
    }
}


function getDatabaseMeta() {
	if ($("#host").val() == "") {
        $("#host").focus();
        return false;
    } else if ($("#port").val() == "") {
        $("#port").focus();
        return false;
    }else if ($("#dbName").val() == "") {
        $("#dbName").focus();
        return false;
    } else if ($("#userName").val() == "") {
        $("#userName").focus();
        return false;
    } else if ($("#password").val() == "") {
        $("#password").focus();
        return false;
    } else {
        $.ajax({
            type: "POST",
            url: "database/getTableNames",
            async: true,
            data: $("#db_form").serialize(),
            success: function (obj) {
            	var result = $.parseJSON(obj);
            	if (result.flag === '1') {
            		$("#db_form").serializeArray().map(function(x){dbInfo[x.name] = x.value;}); 
            		$('#db-url').text(result.jdbc_url);
            		$('#db-url').show();
            		fillSelectOptions('tableNames', result.tableNames, "请选择数据表");
            		$('#tableNames').show();
            		hideDBDiv();
            	} else {
            		alert("连接数据库失败！");
            	}
            }
        });
    }
}

function validateNum(evt) {
    var theEvent = evt || window.event;

    var key = '';
    if (theEvent.type === 'paste') {
        key = event.clipboardData.getData('text/plain');
    } else {
        key = theEvent.keyCode || theEvent.which;
        key = String.fromCharCode(key);
    }
    var regex = /[0-9]|\./;
    if (!regex.test(key)) {
        theEvent.returnValue = false;
        if (theEvent.preventDefault) theEvent.preventDefault();
    }
}

function getSelectedOption(elemID) {
    var obj = document.getElementById(elemID);
    var index = obj.selectedIndex;
    var option = obj.options[index].text;
    return option;
}

function showAspectDiv() {//显示添加切面弹出层
    $('#aspDiv').show();
    getRelation();
}

function hideAspectDiv() {//隐藏添加切面弹出层
    $('#aspDiv').fadeOut(100);
}

function showNodeDiv() {//显示添加切面弹出层
    $('#nodeDiv').show();
    getNodeAttrGroup();
}

function hideNodeDiv() {//显示添加切面弹出层
    $('#nodeDiv').fadeOut(100);
}

function showDBDiv() {
	$('#dbDiv').show();
}

function hideDBDiv() {
	$('#dbDiv').fadeOut(100);
}