$( document ).ready(function() {
	$("#data_type1").prop("selectedIndex", -1);
	$("#data_type2").prop("selectedIndex", -1);
	$("#data_type3").prop("selectedIndex", -1);
});

var nodeMetaDiv = {};
var excelSheetInfo = {};
var nodeNameOptions = {};
var occupiedDiv = [];
var prevNodeMetaDiv = "";
var dataType = 'file';
var dataMap = {};
var candidates = [];

function toLearningPage(isFinished) {
	if (dataType === 'db') {
		dbBatchLearn();
	} else if(dataType === 'excel'){
		excelBatchLearn();
	} else {
		if (getSelectedKeywords("keyword").length === 0) {
			alert('请确认相关专用词汇');
		} else {
			addUserDictWords(getSelectedKeywords("candidate"), isFinished);
		}
	}
}

function excelBatchLearn() {
	var migrateData = {};
	migrateData['fileName'] = document.getElementById("srcFileName").getAttribute("value");
	migrateData['sheet'] = getSelectedOption("sheetNames");
	migrateData['columnName'] = getSelectedOption("columnNames");
	
    $.ajax({
        url: 'migrate/excelLearn', /* 接口域名地址 */
        type: 'post',
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(migrateData),
        success: function (res) {
            alert(res);
        }
    });
}

function dbBatchLearn() {
	var migrateData = {};
	migrateData['dbInfo'] = dbInfo;
	migrateData['table'] = getSelectedOption("tableNames");
	migrateData['fieldName'] = getSelectedOption("fieldNames");
	
    $.ajax({
        type: "POST",
        url: "database/dbBatchLearn",
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(migrateData),
        success: function (obj) {
        	alert(obj);
        }
    });
}

function addUserDictWords(selectedWords, isFinished) {
    $.ajax({
        type: "POST",
        url: "model/AddUserDict",
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(selectedWords),
        success: function (obj) {
        	getWordsInNode(getSelectedKeywords("keyword"), isFinished);
        }
    });
}

function getWordsInNode(keywords, isFinished) {
    $.ajax({
        type: "POST",
        url: "model/addPhraseToPool",
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(keywords),
        success: function (obj) {
        	var postObj = new Object();
        	obj = $.parseJSON(obj);
        	postObj["selected"] = obj;
        	if (isFinished) {
        		$.ajax({
        	        url: 'migrate/qaLearn',
        	        type: 'post',
        	        success: function (res) {
        	        }
        	    });
        		returnCandidate();
        	} else {
			    $.ajax({
			        type: "POST",
			        url: "model/translatePooledPhrases",
			        contentType: "application/json;charset=utf-8",
			        data: JSON.stringify(postObj["selected"]),
			        success: function (res) {
			        	if (res.length > 0) {
			        		post("structure.jsp", postObj);
			        	} else {
			        		disableButton('.learn_buttons .btnAdvanced');
			        	}
			        }
			    });
        	}
        }
    });
}


function disableButton(selector) {
	$(selector).addClass('disabledButton');
	$(selector).prop('disabled', true);	
}

function resetButton(selector) {
	$(selector).removeClass('disabledButton');
	$(selector).prop('disabled', false);	
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

// 上传文件
function uploadMigrateSrcFile() {
	var files = document.getElementById('uploadTextFile').files;
	var formData = new FormData();
	
	for (var i = 0;i < files.length;i ++) {
		formData.append("fileToUpload", files[i]);
	}
	
    $.ajax({
        url: 'uploadFile/uploadLearningFile', /* 接口域名地址 */
        enctype: 'multipart/form-data',
        type: 'post',
        data: formData,
        contentType: false,
        processData: false,
        success: function (res) {
            var obj = eval('(' + res + ')');
        	$("#uploadFileSpan").text(files[0].name);
        	$("#uploadFileSpan").attr("title", files[0].name);
            if (obj.result == true) {
                document.getElementById("srcFileName").setAttribute("value", files.length);
                getHighFreqKeywords(obj.file);
            } else {
                alert(obj.msg);
            }
        },
        error: function(data){
//        	$("#progressStatus").hide();
        }
    });

//    updateProgress();
}

function getHighFreqKeywords(fileName) {
    $.ajax({
        url: 'model/getHighFreqKeywords',
        type: 'post',
        data: {'fileName' : fileName},
        success: function (res) {
        	var keywords = $.parseJSON(res);
        	$("#progressStatus").hide();
        	loadLeft(keywords);
        	$('.learn_buttons').show();
        	resetButton('.learn_buttons .btnAdvanced');
        },
        error: function(data){
        	$("#progressStatus").hide();
        	location.href='candidate.jsp';
        }
    });
}

function updateProgress() {
	$("#progressStatus").show();
	var element = document.getElementById("progressBar"); 
	var identity = setInterval(scene, 10000); 
	function scene() {
    	if(!$("#progressStatus").is(":visible")) {
    		clearInterval(identity);
    		element.style.width = '0%'; 
    		element.innerHTML = '0%';
    	}
    	
	    $.ajax({
	        url: 'uploadFile/getUploadProgress', /* 接口域名地址 */
	        type: 'post',
	        success: function (res) {
	        	var width = parseInt(res);
	        	if (width != -1) {
	    			element.style.width = width + '%'; 
	    			element.innerHTML = width + '%'; 
		    		if (width === 100) { 
		    			clearInterval(identity); 
		    		}
	        	}
	        }
	    });
	} 
}

function pairExcelLearn() {
	var migrateData = {};
	migrateData['fileName'] = document.getElementById("pairExcelSrcFile").getAttribute("value");
	
	if (!$('#pairColumnDiv').is(':visible')) {
		alert('请选择excel的sheet！');
		return;
	}
	
	migrateData['sheet'] = getSelectedOption("pairSheets");
	
    var obj = document.getElementById("qColumn");
    var qIndex = obj.selectedIndex;
    var qOption = obj.options[qIndex].text;
	
	migrateData['qColumn'] = qOption;
	
    obj = document.getElementById("aColumn");
    var aIndex = obj.selectedIndex;
    var aOption = obj.options[aIndex].text;
	migrateData['aColumn'] = aOption;
	
	if (qIndex === 0 || aIndex === 0) {
		alert('请选择问题和答案！');
		return;
	}
	
    $.ajax({
        url: 'migrate/pairDataLearn', /* 接口域名地址 */
        type: 'post',
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(migrateData),
        success: function (res) {
            var obj = eval('(' + res + ')');
            if (obj.result == true) {
            	getPairKeywords(obj.file);
            } else {
                alert(obj.msg);
            }
            
        }
    });
}

function getPairKeywords(fileName) {
    $.ajax({
        url: 'migrate/getPairKeywords',
        type: 'post',
        data: {'fileName' : fileName},
        success: function (res) {
        	var obj = eval('(' + res + ')');
            if (obj.result == true) {
            	var keywords = obj.keywords;
                loadLeft(keywords);
                $('.learn_buttons').show();
                resetButton('.learn_buttons .btnAdvanced');
            } else {
                alert(obj.msg);
            }
        },
        error: function(data){
        	location.href='candidate.jsp';
        }
    });
}

function crawlHtml() {
	var url = $("#url").val();
    $.ajax({
        type: "GET",
        url: "crawl/crawlUrl",
        data: {'url': url},
        success: function (result) {
            var obj = eval('(' + result + ')');
            if (obj.result == true) {
            	var keywords = obj.keywords;
                loadLeft(keywords);
                $('.learn_buttons').show();
                resetButton('.learn_buttons .btnAdvanced');
            } else {
                alert(obj.msg);
            }
        }
    });
}

var excelSheetInfo = {};

function uploadExcelFile(file) {
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
            	$("#uploadExcelSpan").text(file.files[0].name);
            	$("#uploadExcelSpan").attr("title", file.files[0].name);
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

function loadSheetColumnNames() {
    var obj = document.getElementById("sheetNames");
    var index = obj.selectedIndex;
    var sheetName = obj.options[index].text;
    $('#columnNames').show();
    fillSelectOptions("columnNames", excelSheetInfo[sheetName], "请选择数据列");
}

var pairExcelSheet = {};

function uploadPairExcelFile(file) {
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
            	$("#uploadPairSpan").text(file.files[0].name);
            	$("#uploadPairSpan").attr("title", file.files[0].name);
            	var sheets = [];
            	var columns = obj.columns;
            	for (var i = 0;i < columns.length;i ++) {
            		sheets.push(columns[i].sheetName);
            		pairExcelSheet[columns[i].sheetName] = columns[i].columnNames;
            	}
            	fillSelectOptions("pairSheets", sheets, "请选择Sheet");
            	$("#pairSheetDiv").show();
            	$('.pairExcelReadButton').css('margin-top', '62px');
                document.getElementById("pairExcelSrcFile").setAttribute("value", obj.fileName);
            } else {
                alert(obj.msg);
            }
        }
    });
}

function loadPairColumns() {
    var obj = document.getElementById("pairSheets");
    var index = obj.selectedIndex;
    var sheetName = obj.options[index].text;
    $('#pairColumnDiv').css('display', 'inline-block');
    $('.pairExcelReadButton').css('margin-top', '6px');
    fillSelectOptions("qColumn", pairExcelSheet[sheetName], "请选择数据列");
    fillSelectOptions("aColumn", pairExcelSheet[sheetName], "请选择数据列");
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

var indexDivMarginMap = {'.formatted_Excel_data_div':'440px', '.accurate_data_div':'880px', 
		'.text_data_div':'0px',	'.URL_data_div':'220px', '.DB_data_div':'660px'};
var animationTime = 300;
var lastIndexType = ''; 

function indexButtonMove(selector) {
	for (var s in indexDivMarginMap){
		if (s != selector) {
			$(s).hide();
		}
	} 
	
	$(selector).css('margin-left', indexDivMarginMap[selector]);
	indexTitleMove(selector);
	$(selector).animate({'margin-left': '220px'}, animationTime);
}

function indexTitleMove(selector) {
	if (selector === '.text_data_div' || selector === '.URL_data_div' ) {
		$('.formatedTitle').hide();
		$('.pairedDataTitle').hide();
		$('.unformatedTitle').animate({'margin-left': '220px'}, animationTime);
	} else if(selector === '.DB_data_div' || selector === '.formatted_Excel_data_div' ) {
		$('.unformatedTitle').hide();
		$('.pairedDataTitle').hide();
		$('.formatedTitle').css('margin-left', '440px');
		$('.formatedTitle').animate({'margin-left': '220px'}, animationTime);
	} else if (selector === '.accurate_data_div') {
		$('.formatedTitle').hide();
		$('.unformatedTitle').hide();
		$('.pairedDataTitle').css('margin-left', '660px');
		$('.pairedDataTitle').animate({'margin-left': '220px'}, animationTime);
	}
}

function cancel() {
	$('#file-upload').hide();
	$('#url-div').hide();
	$('#db-info').hide();
	$('#excel-upload').hide();
	$('#pair_excel_upload').hide();
	$('.content').hide();
	$('.keyword_area').hide();
	$('.learn_buttons').hide();
	$('.currentDataSource').hide();
	$('.unformatedTitle').css('margin-left', '');
	$('.formatedTitle').css('margin-left', '');
	$('.pairedDataTitle').css('margin-left', '');
	$('.upload_div').css('margin-left', '');
	$('.pairExcelReadButton').css('margin-top', '');
	$('.unformatedTitle').show();
	$('.formatedTitle').show();
	$('.pairedDataTitle').show();
	for (var s in indexDivMarginMap){
		$(s).css('margin-left', '');
		$(s).show();
	}
	lastIndexType = '';
}

function changeDataType(dataType) {
	if (lastIndexType === dataType) {
		return;
	}
	$('#file-upload').hide();
	$('#url-div').hide();
	$('#db-info').hide();
	$('#excel-upload').hide();
	$('#pair_excel_upload').hide();
	$('.content').hide();
	
	if (dataType === 'file') {
		$("#uploadTextFile").val('');
		$("#uploadFileSpan").text('未选择文件');
    	$("#uploadFileSpan").attr("title", '');
    	indexButtonMove('.text_data_div');
    	setTimeout(function(){
    		$('.currentDataSource').text('文件选择');
    		$('.currentDataSource').show();
    		$('#file-upload').show();
    	}, animationTime);
	} else if (dataType === 'url') {
		indexButtonMove('.URL_data_div');
		setTimeout(function(){
    		$('.currentDataSource').text('URL链接地址');
    		$('.currentDataSource').show();
			$('#url-div').show();
		}, animationTime);
	} else if (dataType === 'db') {
		$('#db-url').hide();
		$('#tableNames').hide();
		$('#fieldNames').hide();
		resetButton('.learn_buttons .btnAdvanced');
		indexButtonMove('.DB_data_div');
		setTimeout(function(){
    		$('.currentDataSource').text('数据库映射');
    		$('.currentDataSource').show();
			$('#db-info').show();
		}, animationTime);
	} else if (dataType === 'excel') {
		$('#sheetNames').hide();
		$('#columnNames').hide();
		$("#uploadExcel").val('');
		$("#uploadExcelSpan").text('未选择文件');
    	$("#uploadExcelSpan").attr("title", '');
    	resetButton('.learn_buttons .btnAdvanced');
    	indexButtonMove('.formatted_Excel_data_div');
    	setTimeout(function(){
    		$('.currentDataSource').text('文件选择');
    		$('.currentDataSource').show();
    		$('#excel-upload').show();
    	}, animationTime);
	} else if (dataType === 'pair_excel') {
		$("#pairSheetDiv").hide();
		$('#pairColumnDiv').hide();
		$('.btn_read').hide();
		$("#uploadPairExcel").val('');
		$("#uploadPairSpan").text('未选择文件');
    	$("#uploadPairSpan").attr("title", '');
    	indexButtonMove('.accurate_data_div');
    	setTimeout(function(){
    		$('.currentDataSource').text('文件选择');
    		$('.upload_div').css('margin-left', '200px');
    		$('.currentDataSource').show();
    		$('#pair_excel_upload').show();
    	}, animationTime);
	}
	
	document.getElementById("keywords-checkbox").innerHTML = '';
	prevNodeMetaDiv = '';
	occupiedDiv = [];
	dataMap = {};
	
	$('#aspectNames option').eq(0).prop('selected', true);
	$('#nodeNames option').eq(0).prop('selected', true);
	
	$('.keyword_area').hide();
	$('.learn_buttons').hide();
	
	lastIndexType = dataType;
}

var keywordData = [];
var hiddenDiv = [];

//加载左侧数据
function loadLeft(leftData) {
	keywordData = leftData;
	document.getElementById("keyword_confirm_checkbox").checked = false;
	$('.keyword_area').show();
	candidates = [];
    document.getElementById("keywords-checkbox").innerHTML = "";
    if (leftData == undefined) {
        return;
    }
    var leftHTML = "";
    var wordSet = new Set();
    hiddenDiv = [];
    for (var i = 0; i < leftData.length; i++) {
    	var keywords = [];
    	for (var j = 0;j < leftData[i].length;j ++) {
    		if(!wordSet.has(leftData[i][j].word)) {
    			keywords.push(leftData[i][j]);
    		}
    	}
    	if (keywords.length === 0) {
    		continue;
    	}
    	var count = 1;
    	leftHTML += "<div class='keywordGroupDiv' id='div" + i + "'>";
    	var group = $('<div class=\'keyword_div\'></div>');
    	var words = $('<div class=\'keyword_group\'></div>');
    	for (var j = 0;j < keywords.length;j ++) {
    		if (keywords[j].segment === '0') {
    			if (count === 1) {
    				group.append("<div class='keyword_tag required'><label>(必选)</label></div>");
    				count ++;
    			}
    			words.append("<div class='keyword_option required'><input class='keyword' type='checkbox' name='-1' value='" + keywords[j].word + "' checked/><label class='option_label' for='-1'>" + keywords[j].word + "</label></div>");   
    			wordSet.add(keywords[j].word);
    		}
    	}
    	group.append(words);
    	leftHTML += group[0].outerHTML;

    	if (count > 1) {
    		leftHTML += "</br>";
    	}
    	count = 1;
    	
    	group = $('<div class=\'keyword_div\'></div>');
    	words = $('<div class=\'keyword_group\'></div>');
    	
    	for (var j = 0;j < keywords.length;j ++) {
    		if (keywords[j].segment === '2') {
    			if (count === 1) {
    				group.append("<div class='keyword_tag required'><label>(已选)</label></div>");
    				count ++;
    			}
    			words.append("<div class='keyword_option required'><input class='keyword' type='checkbox' disabled='disabled' name='-1' value='" + keywords[j].word + "' checked/><label class='option_label' for='-1'>" + keywords[j].word + "</label></div>");   
    			wordSet.add(keywords[j].word);
    		}
    	}
    	group.append(words);
    	leftHTML += group[0].outerHTML;
    	
    	if (count > 1) {
    		leftHTML += "</br>";
    	}
    	count = 1;
    	
    	group = $('<div class=\'keyword_div selected_keyword_div\'></div>');
    	words = $('<div class=\'keyword_group\'></div>');
    	
    	for (var j = 0;j < keywords.length;j ++) {
    		if (keywords[j].segment === '1') {
    			if (count === 1) {
    				group.append("<div class='keyword_tag'><label>(可选)</label></div>");
    				words.append("<div class='selected_option'><input class='keyword candidate' type='radio'  name='" + i
        			+ "' value='_NO_CHOICE_'/><label class='no_choice_label' for='" + i + "'>不选择</label></div>"); 
    				count ++;
    			}
    			words.append("<div class='selected_option'><input class='keyword candidate' type='radio' onclick='resetButton(\".learn_buttons .btnAdvanced\");' name='" + i
    			+ "' value='" + keywords[j].word + "'/><label class='keyword_option' for='" + i + "'>" + keywords[j].word + "</label></div>"); 
    			wordSet.add(keywords[j].word);
    		}
    	}
    	
    	if (count === 1) {
    		hiddenDiv.push(i);
    	}
    	
    	group.append(words);
    	leftHTML += group[0].outerHTML;
    	leftHTML += "</br></div>";
    }
        
    document.getElementById("keywords-checkbox").innerHTML = leftHTML;
    hideRequiredWords();
}

function advancedOption() {
	  var checkBox = document.getElementById("keyword_confirm_checkbox");
	  if (checkBox.checked == true){
		  $('.keyword_option').css('display', 'inline-block');
		  $('.required').show();
		  showRequiredWords();
	  } else {
		  $('.keyword_option').css('display', '');
		  $('.required').hide();
		  hideRequiredWords();
	  }
}

function hideRequiredWords() {
	for (var i = 0;i < hiddenDiv.length;i ++) {
		$('#div'+ hiddenDiv[i]).hide();
    }
}

function showRequiredWords() {
	for (var i = 0;i < hiddenDiv.length;i ++) {
		$('#div'+ hiddenDiv[i]).show();
    }
}

function getSelectedKeywords(wordClass) {
	var checkedValues = []; 
	var inputElements = document.getElementsByClassName(wordClass);
	
	for(var i=0; inputElements[i]; ++i){
		var checkbox = inputElements[i];
	      if(checkbox.checked){
	    	  if (checkbox.value === '_NO_CHOICE_') {
	    		  continue;
	    	  }
	    	  checkedValues.push(checkbox.value);
	      }
	}
	return checkedValues;
}

function getSelectedOption(elemID) {
    var obj = document.getElementById(elemID);
    var index = obj.selectedIndex;
    var option = obj.options[index].text;
    return option;
}

function showDBDiv() {
	$('#dbDiv').show();
}

function hideDBDiv() {
	$('#dbDiv').fadeOut(100);
}

var dbInfo = {};

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
        	fillSelectOptions('fieldNames', data.columnNames, "请选择字段");
        	$('#fieldNames').show();
        }
    });
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
            		$('#db-url').text('当前数据库连接：' + result.jdbc_url);
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

function selectOnChange() {
    var obj = document.getElementById("qColumn");
    var qIndex = obj.selectedIndex;
	
    obj = document.getElementById("aColumn");
    var aIndex = obj.selectedIndex;
	
	if (qIndex != 0 && aIndex != 0) {
		$('.btn_read').css('display', 'block');
	}
}

function showLearnButtonDiv() {
	$('.learn_buttons').show();
}