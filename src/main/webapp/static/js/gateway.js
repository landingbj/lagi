var root = new TreeNode("config");
var currentConfigFile;
var isMonitor = false;

$(document).ready(function() {
	$.ajax({
		url : '../gateway/getConfigFile',
		type : 'get',
		success : function(res) {
			if (res.status == 'success') {
				showFileTree(res);
			}
		}
	});
});

$(".monitorBtn").click(function() {
	$(".configContent").val("");
	showApiGatewayStatus();
	$(".configContent").prop('readonly', true);
	$(".editBtn").show();
	$(".saveBtn").hide();
	isMonitor = true;
});

$(".editBtn").click(function() {
	if ($(".configContent").val() == '' || isMonitor) {
		return;
	}
	$(".configContent").prop('readonly', false);
	$(".saveBtn").show();
	$(".editBtn").hide();
});

$(".saveBtn").click(function() {
	$.ajax({
		url : "saveConfigFile",
		type : "post",
		data : JSON.stringify({
			"filePath" : currentConfigFile.getFilePath(),
			"fileContent" : $(".configContent").val()
		}),
		contentType : "application/json; charset=utf-8",
		dataType : "json",
		success : function(res) {
			if (res.status === 'success') {
				currentConfigFile.setContent($(".configContent").val());
			}
		},
		error : function(res) {
			alert("保存失败");
		},
	});

	$(".configContent").prop('readonly', true);
	$(".editBtn").show();
	$(".saveBtn").hide();
});

$(".startBtn").click(function() {
	if ($(".saveBtn").is(":visible") && !($(".configContent").val() == currentConfigFile.getContent())) {
		alert("请先保存！");
	}
	$.ajax({
		url : "gatewayOperate",
		type : "post",
		data : JSON.stringify({
			"command" : "start"
		}),
		contentType : "application/json; charset=utf-8",
		dataType : "json",
		success : function(res) {
			if (res.status === 'success') {
			} else {
				alert("启动失败");
			}
		},
		error : function(res) {
			alert("启动失败");
		},
	});
});

$(".stopBtn").click(function() {
	if ($(".saveBtn").is(":visible") && !($(".configContent").val() == currentConfigFile.getContent())) {
		alert("请先保存！");
	}
	$.ajax({
		url : "gatewayOperate",
		type : "post",
		data : JSON.stringify({
			"command" : "stop"
		}),
		contentType : "application/json; charset=utf-8",
		dataType : "json",
		success : function(res) {
			if (res.status === 'success') {
			} else {
				alert("停止失败");
			}
		},
		error : function(res) {
			alert("停止失败");
		},
	});
});

$(".restartBtn").click(function() {
	if ($(".saveBtn").is(":visible") && !($(".configContent").val() == currentConfigFile.getContent())) {
		alert("请先保存！");
	}
	$.ajax({
		url : "gatewayOperate",
		type : "post",
		data : JSON.stringify({
			"command" : "restart"
		}),
		contentType : "application/json; charset=utf-8",
		dataType : "json",
		success : function(res) {
			if (res.status === 'success') {
			} else {
				alert("重启失败");
			}
		},
		error : function(res) {
			alert("重启失败");
		},
	});
});


function showFileTree(res) {
	var data = res.data;
	var dataMap = {};
	var treeNodeMap = {};
	for (var i = 0; i < data.length; i++) {
		dataMap[data[i].filePath] = data[i];
		treeNodeMap[data[i].filePath] = new TreeNode(data[i].fileName);
	}

	var emptyTreeNodes = [];

	for ( var key in treeNodeMap) {
		var treeNode = treeNodeMap[key];
		var parentDir = dataMap[key].parentDir;
		if (dataMap[key].fileType == "dir") {
			treeNode.setOptions({
				"forceParent" : true,
				"expanded" : true
			});
			if (dataMap[key].isEmpty) {
				emptyTreeNodes.push(treeNode);
			}
			if (parentDir == '.') {
				root.addChild(treeNode);
			} else {
				var parent = treeNodeMap[parentDir];
				parent.addChild(treeNode);
			}

		}
	}

	for ( var key in treeNodeMap) {
		var treeNode = treeNodeMap[key];
		var parentDir = dataMap[key].parentDir;
		if (dataMap[key].fileType == "file") {
			if (parentDir == '.') {
				root.addChild(treeNode);
			} else {
				var parent = treeNodeMap[parentDir];
				parent.addChild(treeNode);
			}
			treeNode.setContent(dataMap[key].fileContent);
			treeNode.setFilePath(dataMap[key].filePath);
			treeNode.on("click", function(e, node) {
				$(".configContent").prop('readonly', true);
				$(".configContent").val(node.getContent());
				currentConfigFile = node;
				isMonitor = false;
			});
		}
	}

	options = {
		"show_root" : false
	};
	TreeConfig.leaf_icon = "<span><img src='../static/images/file.png' alt='file.png'></span>";
	TreeConfig.parent_icon = "<span><img src='../static/images/folder.png' alt='folder.png'></span>";
	TreeConfig.open_icon = "<span><img src='../static/images/open.png' alt='open.png'></span>";
	TreeConfig.close_icon = "<span><img src='../static/images/close.png' alt='close.png'></span>";
	var view = new TreeView(root, ".fileTree", options);

	for (var i = 0; i < emptyTreeNodes.length; i++) {
		emptyTreeNodes[i].setExpanded(false);
	}
	view.reload();
}

function getUrlHost() {
	var host = location.host.split(':', 1);
	return host;
}

function showApiGatewayStatus() {
	if (getUrlHost() == "kgraph.landingbj.com") {
		return;
	}
	$.ajax({
		url : '../gateway/getKrakendStatus',
		method : "get",
		success : function(res) {
			console.log(res);
			$(".configContent").prop('readonly', true);
			$(".configContent").val(res);
		},
		error : function(res) {
			$(".configContent").prop('readonly', true);
			$(".configContent").val('Krakend未活动');
		},
	});
}