var initialIds = [];
var isClicked = false;

function clearSelectionTable() {
	var selectionTable = $("#selectionTable");
	selectionTable.empty();
}

function clearStructureTable() {
	var structureTable = $("#structureTable");
	structureTable.empty();
}

function toggleAdvancedSection() {
	$("#advancedSection").toggle();
	if (!isClicked) {
		getStructureTrees();
		isClicked = true;
	}
}

function advancedOption() {
	  var checkBox = document.getElementById("struct_confirm_checkbox");
	  if (checkBox.checked == true){
		  $('.singleTree').show();
		  $('.singleLabel').show();
	  } else {
		  $('.singleTree').hide();
		  $('.singleLabel').hide();
	  }
}

function addSelection(targetTable, selectorClass, src, possibilities) {
	var selectionTable = $("#" + targetTable);
	var label, newRow;
	if (possibilities.length === 1) {
		label =  $("<label class='keyword singleLabel'></label>");
		newRow = $("<tr class='singleTree " + src + "'></tr>");
	} else {
		label =  $("<label class='keyword'></label>");
		newRow = $("<tr class='" + src + "'></tr>");
	}
	var srcCell = $("<tr></tr>");
	var cellToAdd = $("<td></td>");
	label.text(src);
	srcCell.append(label);
	newRow.append(srcCell);
	
	var newForm = $("<form id='"+ src + "_form'></form>");
	addNewForm(newForm, src, selectorClass, possibilities);
	
	cellToAdd.append(srcCell);
	cellToAdd.append(newForm);
	newRow.append(cellToAdd);
	selectionTable.append(newRow);
}

function addNewForm(newForm, src, selectorClass, possibilities) {
	var moreResultRadio = $("<input id='" + src + "_more_radio' onclick='getMoreProbableTrees(\"" + src + "\")'></input>");
	var moreResultLabel = $("<label id='" + src + "_more_label' class='moreLabel'><canvas id='" + src + "_no_more'></canvas></label>");
	var moreResultDiv = $("<div class='treeDiv'></div>");
	moreResultRadio.prop("type", "radio");
	moreResultDiv.append(moreResultRadio);
	moreResultDiv.append(moreResultLabel);
	newForm.append(moreResultDiv);
	
	var loadedTrees = treeLevel[selection].hasLoaded;
	
	possibilities.forEach(function(elt, i) {
		if (loadedTrees.has(elt)) {
			return true;
		}
		var radioCell = $("<input></input>");
		var radioLabel = $("<label></label>");
		var div = $("<div class='treeDiv'></div>");
		radioCell.prop("type", "radio");
		radioCell.prop("value", elt);
		radioCell.prop("name", "elt");
		radioCell.prop("class", selectorClass);
		radioLabel.prop("for", elt);
		radioLabel.append('<div style="height: 250px;"><canvas id="'+ selection + i + '"></canvas></div');
		
		if (possibilities.length === 1 || elt.includes('<default>true</default>')) {
			radioCell.prop("checked", "checked");
		}
		
		div.append(radioCell);
		div.append(radioLabel);
		newForm.append(div);
	});
}

var treeLevel = {};

function getMoreProbableTrees(keyword) {
	var count = 0;
	callService();
	
	function callService() {
		var preSize = treeLevel[keyword].hasLoaded.size;
		treeLevel[keyword].level = treeLevel[keyword].level + 1;
		$.ajax({
		    url: 'model/getProbablyTreeByWord',
		    type: 'post',
		    data: {"keyword":JSON.stringify([keyword]), "level":treeLevel[keyword].level},
	        success: function (res) {
	        	$("#" + keyword + "_form").empty();
	        	for(selection in res) {
	        		addNewForm($("#" + keyword + "_form"), keyword, "radioSelector", res[selection]);
	        		drawProbableTrees(res[selection], selection);
	        	}
	        	var currSize = treeLevel[keyword].hasLoaded.size;
	        	
	        	if (currSize - preSize === 0) {
	        		if (count === 2) {
	        			$.ajax({
	        			    url: 'model/getProbablyTreeByWord',
	        			    type: 'post',
	        			    data: {"keyword":JSON.stringify([keyword]), "level":treeLevel[keyword].level},
	        		        success: function (res) {
	        		        	treeLevel[keyword].hasLoaded = new Set();
	        		        	$("#" + keyword + "_form").empty();
	        		        	for(selection in res) {
	        		        		addNewForm($("#" + keyword + "_form"), keyword, "radioSelector", res[selection]);
	        		        		drawProbableTrees(res[selection], selection);
	        		        	}
	    	        			$("#" + keyword + "_more_radio").prop("disabled", true);
	    	        			$("#" + keyword + "_more_text").hide();
	    	        			$("#" + keyword + "_no_more").show();
	    	        			drawNoMoreNode(keyword + "_no_more", "没有更多");
	        		        }
	        			});
	        			return;
	        		}
	        		callService();
	        		currSize = treeLevel[keyword].hasLoaded.size;
	        		count ++;
	        	}
	        }
	    });
	}
}


function drawNoMoreNode(canvasID, text) {
	var root = {'x':150, 'y':20, 'text': text};
	var c = document.getElementById(canvasID);
	var ctx = c.getContext("2d");
    ctx.canvas.width = 300;
    ctx.canvas.height = 250;
    drawNoMoreText(ctx, root.x, root.y, 12, root.text, '#6FB6FF', '#6FB6FF');
}

function drawNoMoreText(ctx, x, y, r, text, fillColor, strokeColor, lineDash) {
    if (lineDash) {
        ctx.setLineDash(lineDash);
    } else {
        ctx.setLineDash([]);
    }
    ctx.lineWidth = 1;
    var w = 71;
    if (text != '更多...>>') {
    	w = text.length * 20 - 3 * (text.length - 1);
    }
    var h = 20;
    drawNodeRect(ctx, x, y, w, h, fillColor, true);
    drawNodeRect(ctx, x, y, w, h, strokeColor, false);
    ctx.setLineDash([]);
    txtCircle(ctx, x, y, text, "#FFFF33");
}

function getProbableTrees(selections) {
	initialIds = selections;
	$.ajax({
        url: 'model/getProbableTrees',
        type: 'post',
        data: JSON.stringify(selections),
        success: function (res) {
        	clearSelectionTable();
            for(selection in res) {
            	treeLevel[selection] = {};
            	treeLevel[selection].level = 0;
            	treeLevel[selection].hasLoaded = new Set();
            	addSelection("selectionTable", "radioSelector", selection, res[selection]);
            	drawNoMoreNode(selection + "_no_more", "更多...>>");
            	drawProbableTrees(res[selection], selection);
            }
            disableAdvancedOption();
        }
    });
}

function disableAdvancedOption() {
	var keywordCount = $('#selectionTable .keyword').length;
	var singleTreeCount = $('#selectionTable .singleLabel').length;
	if (keywordCount == singleTreeCount) {
		var checkBox = document.getElementById("struct_confirm_checkbox");
		$('.singleTree').show();
		$('.singleLabel').show();
		checkBox.disabled= true;
	}
}

function drawProbableTrees(possibilities, selection) {
	var loadedTrees = treeLevel[selection].hasLoaded;
	possibilities.forEach(function(elt, i) {
		if (loadedTrees.has(elt)) {
			return true;
		}
		loadedTrees.add(elt);
		var tree = parseXml(elt);
    	drawProbableTree(tree, selection + i);
	});
}

function drawProbableTree(tree, canvasID) {
	var root = {'x':150, 'y':20, 'text': tree.node.value.text};
	var nodes = [];
	nodes.push(root);
	var lines = [];
	calcXY(tree.node, 0, 300, 0, 50);
	
	var c = document.getElementById(canvasID);
	var ctx = c.getContext("2d");
    ctx.canvas.width = 300;
    ctx.canvas.height = 250;
	
	for (var i = 0;i < nodes.length;i ++) {
		if (nodes[i].y > ctx.canvas.height) {
			ctx.canvas.height = nodes[i].y + 20;
		}
		if (nodes[i].x > ctx.canvas.width) {
			ctx.canvas.width = nodes[i].x + 20;
		}
	}
    
	for (var i = 0;i < lines.length;i ++) {
		drawLines(ctx, nodes[lines[i].parent].x,  nodes[lines[i].parent].y,  nodes[lines[i].child].x, nodes[lines[i].child].y);
	}
    
	for (var i = 0;i < nodes.length;i ++) {
		drawNode(ctx, nodes[i].x, nodes[i].y, 12, nodes[i].text, '#6FB6FF', '#6FFFFF');
	}
	
	function calcXY(treeRoot, startX, endX, rootID, offsetY) {
		var children = treeRoot.children.node;
		
		if (typeof children === "undefined") {
			return;
		}
		
		var root = nodes[rootID];
		var xLen = endX - startX;
		var unit = xLen / treeRoot.value.text.length;
		var range = [];
		var nodeIndex = [];
		for (var i = 0;i < children.length;i ++) {
			var x1 = (i === 0? startX : range[i - 1].x2);
			var x2 = x1 + unit * children[i].value.text.length;
			range.push({'x1':x1, 'x2':x2});
			nodes.push({'x':(x2 + x1)/2, 'y':root.y + offsetY, 'text':children[i].value.text});
			var index = nodes.length - 1;
			nodeIndex.push(index);
			lines.push({'parent':rootID, 'child':index});
		}
		
		for (var i = 0;i < children.length;i ++) {
			calcXY(children[i], range[i].x1, range[i].x2, nodeIndex[i], offsetY);
		}
	}
}

function drawStructTree(tree, canvasID) {
	var root = {'x':150, 'y':20, 'text': tree.struct.value.text};
	var nodes = [];
	nodes.push(root);
	var lines = [];
	calcXY(tree.struct, 0, 300, 0, 50);
	
	var c = document.getElementById(canvasID);
	var ctx = c.getContext("2d");
    ctx.canvas.width = 300;
    ctx.canvas.height = 250;
	
	for (var i = 0;i < nodes.length;i ++) {
		if (nodes[i].y > ctx.canvas.height) {
			ctx.canvas.height = nodes[i].y + 20;
		}
		if (nodes[i].x > ctx.canvas.width) {
			ctx.canvas.width = nodes[i].x + 20;
		}
	}
    
	for (var i = 0;i < lines.length;i ++) {
		drawLines(ctx, nodes[lines[i].parent].x,  nodes[lines[i].parent].y,  nodes[lines[i].child].x, nodes[lines[i].child].y);
	}
    
	for (var i = 0;i < nodes.length;i ++) {
		drawNode(ctx, nodes[i].x, nodes[i].y, 12, nodes[i].text, '#6FB6FF', '#6FFFFF');
	}
	
	function calcXY(treeRoot, startX, endX, rootID, offsetY) {
		var children = treeRoot.children.struct;
		
		if (typeof children === "undefined") {
			return;
		}
		
		if (!Array.isArray(children)) {
			children = [children];
		} 
		
		var root = nodes[rootID];
		var xLen = endX - startX;
		var unit = xLen / children.length;
		var range = [];
		var nodeIndex = [];
		for (var i = 0;i < children.length;i ++) {
			var x1 = (i === 0? startX : range[i - 1].x2);
			var x2 = x1 + unit;
			range.push({'x1':x1, 'x2':x2});
			nodes.push({'x':(x2 + x1)/2, 'y':root.y + offsetY, 'text':children[i].value.text});
			var index = nodes.length - 1;
			nodeIndex.push(index);
			lines.push({'parent':rootID, 'child':index});
		}
		
		for (var i = 0;i < children.length;i ++) {
			calcXY(children[i], range[i].x1, range[i].x2, nodeIndex[i], offsetY);
		}
	}
}

function drawLines(Ctx, x1, y1, x2, y2) { // 画关系线
    Ctx.strokeStyle = '#6FB6FF';
    Ctx.lineWidth = 2;
    Ctx.beginPath();
    Ctx.moveTo(x1, y1);
    Ctx.lineTo(x2, y2);
    Ctx.closePath();
    Ctx.stroke();
}

function drawNode(ctx, x, y, r, text, fillColor, strokeColor, lineDash) {
    if (lineDash) {
        ctx.setLineDash(lineDash);
    } else {
        ctx.setLineDash([]);
    }
    ctx.lineWidth = 1;
    var w = text.length * 20 - 3 * (text.length - 1);
    var h = 20;
    drawNodeRect(ctx, x, y, w, h, fillColor, true);
    drawNodeRect(ctx, x, y, w, h, strokeColor, false);
    ctx.setLineDash([]);
    txtCircle(ctx, x, y, text, "#fff");
}

function drawNodeRect(ctx, cx, cy, w, h, color, isFull) {
    var ox = w / 2.0;
    var oy = h / 2.0;
    
	ctx.beginPath();
	ctx.roundRect(cx - ox, cy - oy, w, h, 5);
    if (isFull) {
        ctx.fillStyle = color;
        ctx.fill();
    } else {
        ctx.strokeStyle = color;
        ctx.stroke();
    }
}

CanvasRenderingContext2D.prototype.roundRect = function (x, y, w, h, r) {
	  if (w < 2 * r) r = w / 2;
	  if (h < 2 * r) r = h / 2;
	  this.beginPath();
	  this.moveTo(x+r, y);
	  this.arcTo(x+w, y,   x+w, y+h, r);
	  this.arcTo(x+w, y+h, x,   y+h, r);
	  this.arcTo(x,   y+h, x,   y,   r);
	  this.arcTo(x,   y,   x+w, y,   r);
	  this.closePath();
	  return this;
};

function txtCircle(Ctx, x, y, txt, fillStyle) {
    Ctx.font = "normal 14px Arial";
    Ctx.fillStyle = fillStyle;
    Ctx.textAlign = "center";
    Ctx.textBaseline = "middle";
    Ctx.fillText(txt, x, y);
}

function parseXml(xml, arrayTags) {
    var dom = null;
    if (window.DOMParser) {
        dom = (new DOMParser()).parseFromString(xml, "text/xml");
    } else if (window.ActiveXObject) {
        dom = new ActiveXObject('Microsoft.XMLDOM');
        dom.async = false;
        if (!dom.loadXML(xml)) {
            throw dom.parseError.reason + " " + dom.parseError.srcText;
        }
    } else {
        throw "cannot parse xml string!";
    }

    function isArray(o) {
        return Object.prototype.toString.apply(o) === '[object Array]';
    }

    function parseNode(xmlNode, result) {
        if (xmlNode.nodeName == "#text") {
            var v = xmlNode.nodeValue;
            if (v.trim()) {
                result['text'] = v;
            }
            return;
        }

        var jsonNode = {};
        var existing = result[xmlNode.nodeName];
        if (existing) {
            if (!isArray(existing)) {
                result[xmlNode.nodeName] = [existing, jsonNode];
            } else {
                result[xmlNode.nodeName].push(jsonNode);
            }
        } else {
            if (arrayTags && arrayTags.indexOf(xmlNode.nodeName) != -1) {
                result[xmlNode.nodeName] = [jsonNode];
            } else {
                result[xmlNode.nodeName] = jsonNode;
            }
        }

        if (xmlNode.attributes) {
            var length = xmlNode.attributes.length;
            for (var i = 0; i < length; i++) {
                var attribute = xmlNode.attributes[i];
                jsonNode[attribute.nodeName] = attribute.nodeValue;
            }
        }

        for (var j = 0; j < xmlNode.childNodes.length; j++) {
            parseNode(xmlNode.childNodes[j], jsonNode);
        }
    }

    var result = {};
    for (var i = 0; i < dom.childNodes.length; i++) {
        parseNode(dom.childNodes[i], result);
    }

    return result;
}

function getStructureTrees() {
	$.ajax({
        url: 'model/getStructureTrees',
        type: 'get',
        success: function (res) {
        	clearStructureTable();
            for(selection in res) {
            	addStructureSelection("structureTable", "structureSelector", selection, res[selection]);
            	drawStructTrees(res[selection], selection);
            }
        }
    });
}

function drawStructTrees(possibilities, selection) {
	possibilities.forEach(function(elt, i) {
		var tree = parseXml(elt.plainTextEncoding);
    	drawStructTree(tree, "struct_" + selection + i);
	});
}

function addStructureSelection(targetTable, selectorClass, src, possibilities) {
	var selectionTable = $("#" + targetTable);
	var newForm = $("<form></form>");
	var label, newRow;
	if (possibilities.length <= 1) {
		label =  $("<label class='keyword singleLabel'></label>");
		newRow = $("<tr class='singleTree " + src + "'></tr>");
	} else {
		label =  $("<label class='keyword'></label>");
		newRow = $("<tr class='" + src + "'></tr>");
	}
	var srcCell = $("<tr></tr>");
	var cellToAdd = $("<td></td>");
	label.text(src);
	srcCell.append(label);
	newRow.append(srcCell);
	
	possibilities.forEach(function(elt, i) {
		var radioCell = $("<input></input>");
		var radioLabel = $("<label></label>");
		var div = $("<div class='treeDiv'></div>");
		radioCell.prop("type", "radio");
		radioCell.prop("value", elt.fullUidEncoding);
		radioCell.prop("name", "elt");
		radioCell.prop("class", selectorClass);
		radioLabel.prop("for", elt.fullUidEncoding);
		radioLabel.append('<div><canvas id="struct_'+ selection + i + '"></canvas></div');
				
		if (possibilities.length === 1 || elt.plainTextEncoding.includes('<default>true</default>')) {
			radioCell.prop("checked", "checked");
		}
		
		div.append(radioCell);
		div.append(radioLabel);
		newForm.append(div);
	});
	
	cellToAdd.append(srcCell);
	cellToAdd.append(newForm);
	newRow.append(cellToAdd);
	selectionTable.append(newRow);
}

function addCommonStructures() {
	var radioButtons = $(".structureSelector:checked");
	var encodingStrings = new Array();
	radioButtons.each(function(i) {
		encodingStrings.push($(this).val());
	});
	return new Promise(function(resolve, reject) {
		$.ajax({
	        url: 'model/addCommonStructures',
	        type: 'post',
	        data: JSON.stringify(encodingStrings),
	        success: function (res) {
	        	var success = true;
	        	res.forEach(function(elt, i) {
	        		if(elt === -1) {
	        			success = false;
	        		}
	        	});
	        	if(success) {
	        		resolve(0);
	        	} else {
	        		alert("failed to persist common structures");
	        		reject(1);
	        	}
	        }
	    });
	});
}

function addHybridNodes() {
	var radioButtons = $(".radioSelector:checked");
	var encodingStrings = new Array();
	radioButtons.each(function(i) {
		encodingStrings.push($(this).val());
	});
	
	var result = new Promise(function(resolve, reject) {
		$.ajax({
	        url: 'model/persistTree',
	        type: 'post',
	        data: JSON.stringify(encodingStrings),
	        success: function (res) {
	        	resolve(res);
	        }
	    });
	});
	
	$.ajax({
        url: 'model/transitionPhrasesToSelected',
        type: 'post',
        data: JSON.stringify(initialIds),
        success: function (res) {
        	console.log("phrases marked");
        }
    });
	
	return result;
}

function post(path, parameters) {
    var form = $('<form></form>');

    form.attr("method", "post");
    form.attr("action", path);

    $.each(parameters, function(key, value) {
        var field = $('<input></input>');

        field.attr("type", "hidden");
        field.attr("name", key);
        field.attr("value", value);

        form.append(field);
    });
    $(document.body).append(form);
    form.submit();
}

function toLearningPage() {
	if (isClicked) {
		addHybridNodes().then(function(resolution) {
			addCommonStructures().then(function(res2) {
				post("learning.jsp", resolution);
			});
		});
	} else {
		addHybridNodes().then(function(resolution) {
			post("learning.jsp", resolution);
		});
	}

}

function toAdminLearningPage() {
	var checkedRadioButtons = $(".radioSelector:checked");
	var keywords = $(".keyword");
	
	if (keywords.length != checkedRadioButtons.length) {
		alert('还有词汇结构未作出选择！');
		return;
	}
	
	if (isClicked) {
		addHybridNodes().then(function(resolution) {
			addCommonStructures().then(function(res2) {
				post("admin/learning.jsp", resolution);
			});
		});
	} else {
		addHybridNodes().then(function(resolution) {
			post("admin/learning.jsp", resolution);
		});
	}
}

function toFinish() {
	if (isClicked) {
		addHybridNodes().then(function(resolution) {
			addCommonStructures().then(function(res2) {
				$.ajax({
			        url: 'migrate/qaLearn',
			        type: 'post',
			        success: function (res) {
			        }
			    });
				returnCandidate();
			});
		});
	} else {
		addHybridNodes().then(function(resolution) {
			$.ajax({
		        url: 'migrate/qaLearn',
		        type: 'post',
		        success: function (res) {
		        }
		    });
			returnCandidate();
		});
	}
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

function toAdminFinish() {
	var checkedRadioButtons = $(".radioSelector:checked");
	var keywords = $(".keyword");
	
	if (keywords.length != checkedRadioButtons.length) {
		alert('还有词汇结构未作出选择！');
		return;
	}
	
	if (isClicked) {
		addHybridNodes().then(function(resolution) {
			addCommonStructures().then(function(res2) {
				location.href='admin/keyword.jsp';
			});
		});
	} else {
		addHybridNodes().then(function(resolution) {
			location.href='admin/keyword.jsp';
		});
	}
}

function deleteKeyword(e) {
	var word = [];
	word.push(e.getAttribute('data-keyword'));
	
	$.ajax({
        url: 'model/deletePhrases',
        type: 'post',
        data: JSON.stringify(word),
        success: function (res) {
        	var result = parseInt(res);
        	if (result === 1) {
            	$('.' + word).remove();
            	$('#list_' + word).remove();
        	} else {
        		alert("删除失败");
        	}
        }
    });
}