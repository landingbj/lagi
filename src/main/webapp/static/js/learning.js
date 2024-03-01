var nodeMetaDiv = {};
var excelSheetInfo = {};
var nodeNameOptions = {};
var occupiedDiv = [];
var dbInfo = {};
var prevNodeMetaDiv = "";
var dataType = 'Database';
var dataMap = {};
var currentCategory = {};
var currentIds = {};

//////////////////////////////////////////////////////////////////////
//																	//
//					Candidate Selection Form Logic					//
//																	//
//////////////////////////////////////////////////////////////////////


function setCurrentIds(ids) {
	currentIds = ids;
}

function clearOptions() {
	$("#nodesSection").empty();
	$("#hybridsSection").empty();
	$("#edgesSection").empty();
}

function getCategories() {
	$.ajax({
        url: 'model/getCategories',
        type: 'get',
        contentType: "application/json;charset=utf-8",
        success: function (res) {
        	var defaultOption = $("<option></option>")
        	defaultOption.val("-1");
        	defaultOption.text("Select Category");
        	$("#categorySelect").append(defaultOption);
            res.forEach(function(elt, i) {
            	var newOptionSelect = $("<option></option>");
            	newOptionSelect.val(elt.name);
            	newOptionSelect.text(elt.name);
            	$("#categorySelect").append(newOptionSelect.clone());
            	$("#category").append(newOptionSelect.clone());
            });
        }
    });
}

function advancedOption() {
	$('#categorySelect').val('-1');
	clearOptions();
}

function refreshOptions() {
	clearOptions();
	var cat = $("#categorySelect").val();
	if(cat == "-1") {
		return;
	}
	var nodeTableFinished = findAllNodeNameByCategory(cat);
    nodeTableFinished.then(function(nodeTables) {
    	var aspectTableFinished = getAspectNamesByCategory(cat);
    	var nodeTable2pass = nodeTables;
    	aspectTableFinished.then(function(aspectTables) {
    		getCandidates(nodeTable2pass, aspectTables);
    	});
    });
}

function getCandidates(nodeTable, aspectTable) {
	 $.ajax({
	        url: 'model/getCandidates',
	        type: 'post',
	        contentType: "application/json;charset=utf-8",
	        data: JSON.stringify(currentIds),
	        success: function (res) {
	            for(var key in res.node) {
	            	var value = res.node[key];
	            	var elt = new Object();
	            	elt.uid = key;
	            	elt.desc = value;
	            	var nodeRow = generateSelectionRow(elt, "nodeRow", nodeTable);
	            	$("#nodesSection").append(nodeRow);
	            }
	            
	            for(var key in res.hybrid) {
	            	var value = res.hybrid[key];
	            	var elt = new Object();
	            	elt.uid = key;
	            	elt.desc = value;
	            	var hybridRow = generateSelectionRow(elt, "hybridRow", nodeTable);
	            	$("#hybridsSection").append(hybridRow);
	            }
	            
	            for(var key in res.edge) {
	            	var value = res.edge[key];
	            	var elt = new Object();
	            	elt.uid = key;
	            	elt.desc = value;
	            	var hybridRow = generateSelectionRow(elt, "edgesSection", aspectTable);
	            	$("#edgesSection").append(hybridRow);
	            }
	        }
	    });
}

function generateSelectionRow(entry, inputClass, options) {
	var newRow = $("<div></div>");
	newRow.prop("class", "entry_row");
	
	var newCol = $("<div></div>");
	newCol.prop("class", "entry");
	
	var label = $("<label></label>");
	label.text(entry.desc);
	
	var selectionForm = $("<select></select>");
	selectionForm.prop("class", inputClass);
	selectionForm.prop("uid", entry.uid);
	
	options.forEach(function(elt, i) {
		var optionTag = $("<option></option>");
		optionTag.val(elt);
		optionTag.text(elt);
		selectionForm.append(optionTag);
	});
	
	var otherOption = $("<option selected></option>")
	otherOption.val("--other--");
	otherOption.text("--Other--");
	selectionForm.append(otherOption);
	
	newRow.append(newCol);
	newCol.append(label);
	newCol.append(selectionForm);
	
	return newRow;
}

function getSelectedPairs(classname) {
	var forms = $("." + classname);
	var migrationMap = new Object();
	
	forms.each(function(i) {
		if($(this).val() == "--other--") {
			return;
		}
		migrationMap[$(this).prop("uid")] = $(this).val();
	});
	
	return migrationMap;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
//																									//
//								Legacy Code from Initial learning.js                            	//
//																									//
//////////////////////////////////////////////////////////////////////////////////////////////////////

var isLearning = false;

function startMigrate() {
	if (!isLearning) {
		$('#saveButton').prop('disabled', true);
	}
	var nodeMigration = getSelectedPairs("nodeRow");
	var hybridMigration = getSelectedPairs("hybridRow");
	var edgeMigration = getSelectedPairs("edgesSection");
	
	var data = new Object();
	data["node"] = nodeMigration;
	data["hybrid"] = hybridMigration;
	data["edge"] = edgeMigration;
		
	$.ajax({
        url: 'migrate/keywordMigrate',
        type: 'post',
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(data),
        success: function (res) {
        	res = $.parseJSON(res);
            if(res.status === "success") {
            	alert("Finished migration");
            } else {
            	alert("Failed:" + res.msg);
            }
        	$('#saveButton').prop('disabled', false);
        	isLearning = false;
    		$.ajax({
		        url: 'migrate/qaLearn',
		        type: 'post',
		        success: function (res) {
		        }
		    });
    		returnCandidate();
        },
        error: function(res) {
        	$('#saveButton').prop('disabled', false);
        	isLearning = false;
        }
    });
	
	isLearning = true;
}

function returnCandidate() {
    $.ajax({
        url: 'uploadFile/getNextUploadFile',
        type: 'post',
        success: function (res) {
        	var obj = $.parseJSON(res);
        	if (obj.result === true) {
        		location.href='candidate.jsp?fileName=' + obj.fileName;
        	} else {
        		location.href='candidate.jsp';
        	}
        }
    });
}

// 根据category获取节点名称
function findAllNodeNameByCategory(category) {
	return new Promise(function(resolve, reject) {
		$.ajax({
	        url: 'model/findAllNodeNameByCategory?category=' + category, /* 接口域名地址 */
	        type: 'get',
	        success: function (res) {
	            var obj = $.parseJSON(res);
	            if (obj.result == true) {
	                var data = obj.data;
	                resolve(data);
	            } else {
	                alert(obj.msg);
	                reject(0);
	            }
	        }
	    });
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

function fillAspectOptions(eleID, data, defaultContent) {
    document.getElementById(eleID).innerHTML = "";
    var html = "";
    for (var i = 0; i < data.length; i++) {
    	if (data[i] == defaultContent) {
    		html += "<option selected='selected'>" + data[i] + "</option>";
    	} else {
    		html += "<option>" + data[i] + "</option>";
    	}
    }
    document.getElementById(eleID).innerHTML = html;
}

function getAspectNamesByCategory(category) {
    if (category == undefined) {
        category = "";
    }
    var checkBox = document.getElementById("learn_confirm_checkbox");
    var isFull = false;
    if (checkBox.checked == true){
    	isFull = true;
    }
    return new Promise(function(resolve, reject) {
    	$.ajax({
            type: "GET",
            url: "model/getAspectNames",
            data: {'category': category, 'full' : isFull},
            success: function (result) {
                var data = $.parseJSON(result);
                if (data.length > 0) {
                	resolve(data);
                } else {
                    alert(result);
                    reject(0);
                }
            }
        });
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
                var obj = $.parseJSON(res);
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
                	refreshOptions();
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
    } else if($("#category").val() == "-1") {
    	$("#category").focus();
    	return false;
    } else {
    	var formData = $("#node_form").serialize();
        $.ajax({
            type: "POST",
            url: "model/AddNodeTable",
            async: true,
            data: formData,
            success: function (result) {
            	if (result === '1') {
            		alert("添加成功！");
            		refreshOptions();
            		hideNodeDiv();
            	} else {
            		alert("添加失败！");
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