window.onload = function () {
	getCategories();
};

function openPage(pageName, elmnt) {
    var color = "rgb(18, 45, 107)";
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablink");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].style.backgroundColor = "";
    }
    document.getElementById(pageName).style.display = "block";
    elmnt.style.backgroundColor = color;
}

document.getElementById("defaultOpen").click();

var graphNodes = [];
var graphLines = [];

function searchKG() {
    var searchStr = $("#searchStr").val();
    var category = $("#category").val();

    var paras = {
        "searchStr": searchStr,
        "category": category
    };

    var c = document.getElementById("myCanvas");
    var ctx = c.getContext("2d");
    ctx.clearRect(0, 0, c.width, c.height);
    graphNodes = [];
    graphLines = [];

    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "search/searchKG",
        data: JSON.stringify(paras),
        success: function(json) {
            var result = $.parseJSON(json);

            if (result.SearchResult.node === undefined) {
            	$('#search_info').text("无返回结果");
            	$('#search_info').show();
            	return;
            } else {
            	$('#search_info').hide();
            }
            
            var height = 200;
            var intervalY = 50;
            ctx.canvas.width = $("#search_graph").width();
            ctx.canvas.height = (height + intervalY) * result.SearchResult.node.length + 50;
            getGraphData(result.SearchResult.node, ctx.canvas.width / 2.5, height, intervalY);

            for (var i = 0; i < graphLines.length; i++) {
                var n1 = graphLines[i].start;
                var n2 = graphLines[i].end;
                drawLines(ctx, graphNodes[n1].x, graphNodes[n1].y, graphNodes[n2].x, graphNodes[n2].y);
            }

            for (var i = 0; i < graphNodes.length; i++) {
                var x = graphNodes[i].x;
                var y = graphNodes[i].y;
                var txt = graphNodes[i].name;
                drawNodeEllipse(ctx, x, y, 12, txt, '#6FB6FF', '#6FFFFF');
            }

            document.getElementById("search_graph").onmousemove = editDivMove;

        }
    });
}

function questionKG() {
    var questionStr = $("#questionStr").val();
    var category = $("#category").val();

    var paras = {
        "questionStr": questionStr,
        "category": category
    };
    
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "search/questionAnswer",
        data: JSON.stringify(paras),
        success: function(json) {
            var result = $.parseJSON(json);

            if (result.QuestionResult === undefined) {
            	$('#answer_info').text("无返回结果");
            	$('#answer_info').show();
            	return;
            } else {
            	$('#answer_info').text(result.QuestionResult);
            }
        }
    });
}

function getGraphData(nodeTrees, startX, height, intervalY) {
    var startY = 0;

    for (var i = 0; i < nodeTrees.length; i++) {
        nodeTreeXY(nodeTrees[i], i);
    }

    function nodeTreeXY(nodeTree, level) {
        var totalX = 400;
        var root = {};
        root.x = startX;
        root.y = startY + intervalY;
        root.name = nodeTree.name + "";
        root.txt = "name: " + nodeTree.name + "</br>description: " + nodeTree.description;
        startY = root.y + height;
        graphNodes.push(root);

        if (nodeTree.children.hasOwnProperty('node')) {
        	var rootIndex = graphNodes.length - 1;
        	var children = nodeTree.children.node;
        	var unitX = totalX / (children.length - 1);
            var startPoint = {
                x: startX - totalX / 2,
                y: root.y + height
            };
            
            for (var j = 0; j < children.length; j++) {
                var child = {};
                child.x = startPoint.x + (j * unitX);
                child.y = startPoint.y;
                child.name = children[j].name + "";
                child.txt = "name: " + children[j].name + "</br>description: " + children[j].description;
                graphNodes.push(child);
                var line = {};
                line.start = graphNodes.length - 1;
                line.end = rootIndex;
                graphLines.push(line);
            }
        }
    }
}

function editDivMove(ev) {
    ev.preventDefault();
    var OffsetLeft = $('#search_graph').offset().left;
    var OffsetTop = $('#search_graph').offset().top;
    var clickX = ev.pageX - OffsetLeft;
    var clickY = ev.pageY - OffsetTop;
    var hoverNodeID = -1;

    if (isNodeSelected(clickX, clickY)) { // 鼠标撞到节点，高亮该节点，然后显示该节点的content
        prehoverNodeID = hoverNodeID;
        var node = graphNodes[hoverNodeID];
        $('#showTxt').css('top', ev.pageY - 35);
        $('#showTxt').css('left', ev.pageX - 15);
        $('#showTxt').show();
        $('#showTxt').html(node.txt);
        return true;
    } else {
        $('#showTxt').hide();
    }

    function isNodeSelected(x, y) {
        for (var i = 0; i < graphNodes.length; i++) {
            if (isInCircles(x, y, graphNodes[i].x, graphNodes[i].y, 6)) {
                hoverNodeID = i;
                return true;
            }
        }
        return false;
    }

    function isInCircles(x, y, c_x, c_y, c_r) { // 判断鼠标是否在椭圆内部,鼠标坐标和节点坐标及半径
        var F = Math.pow(x - c_x, 2) + Math.pow(y - c_y, 2);
        var R = Math.pow(2 * c_r, 2);
        if (F < R) {
            return true;
        } else {
            return false;
        }

    }

    function fillEllipse(ctx, x, y, r) {
        var a = 4 * r;
        var b = 2 * r;
        drawEllipseByCenter(ctx, x, y, a, b, '#6FB6FF', true);
    }

    function drawEllipseByCenter(ctx, cx, cy, w, h, color, isFull) {
        drawEllipse(ctx, cx - w / 2.0, cy - h / 2.0, w, h, color, isFull);
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

function drawNodeEllipse(ctx, x, y, r, text, fillColor, strokeColor, lineDash) {
    if (lineDash) {
        ctx.setLineDash(lineDash);
    } else {
        ctx.setLineDash([]);
    }
    var a = 4 * r;
    var b = 2 * r;
    drawEllipseByCenter(ctx, x, y, a, b, fillColor, true);
    drawEllipseByCenter(ctx, x, y, a * 1.25, b * 1.25, strokeColor, false);
    ctx.setLineDash([]);
    txtCircle(ctx, x, y, r, text);
}

function drawEllipseByCenter(ctx, cx, cy, w, h, color, isFull) {
    drawEllipse(ctx, cx - w / 2.0, cy - h / 2.0, w, h, color, isFull);
}

function drawEllipse(ctx, x, y, w, h, color, isFull) {
    var kappa = .5522848,
        ox = (w / 2) * kappa,
        oy = (h / 2) * kappa,
        xe = x +
        w,
        ye = y + h,
        xm = x + w / 2,
        ym = y + h / 2;

    ctx.beginPath();

    ctx.moveTo(x, ym);
    ctx.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y);
    ctx.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym);
    ctx.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye);
    ctx.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym);
    if (isFull) {
        ctx.fillStyle = color;
        ctx.fill();
    } else {
        ctx.strokeStyle = color;
        ctx.stroke();
    }
}

function txtCircle(Ctx, x, y, r, txt) {
    if (r > 5) {
        if (r < 15) {
            Ctx.font = "normal 11px Arial";
        } else if (r > 15 && r < 31) {
            Ctx.font = "normal 13px Arial";
        } else if (r > 31) {
            Ctx.font = "bold 15px Arial";
        }
        Ctx.fillStyle = "#fff";
        Ctx.textAlign = "center";
        Ctx.textBaseline = "middle";
        txt = txt.substr(0, 5);
        Ctx.fillText(txt, x, y);
    }
}

function getCategories() {
	$.ajax({
        url: 'model/getCategories',
        type: 'get',
        contentType: "application/json;charset=utf-8",
        success: function (res) {
            res.forEach(function(elt, i) {
            	var newOptionSelect = $("<option></option>");
            	if (elt.name === 'smartqa') {
            		newOptionSelect = $("<option selected = 'selected'></option>");
            	}
            	newOptionSelect.val(elt.name);
            	newOptionSelect.text(elt.name);
            	$("#category").append(newOptionSelect.clone());
            });
        }
    });
}