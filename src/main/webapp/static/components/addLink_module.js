define(function (require, exports){
	function addLink($, showBtn, maskBox, hasData){
		if(!maskBox){
			maskBox = '.mask';
		}
		//获取当前用户id
		var userId=$(".fr .firstI .id").val();
   		if(!userId && hasData.userId){
   			userId = hasData.userId;
   		}
		 //获取连接用途列表
	    $.post("/query/cmdb/linkUse",function(data){
	    	var li="";
	    	$.each(data,function(i,e){
				li+="<li value='"+e.id+"'>"+e.name+"</li>";
			});
			$(".lk_use").html(li);
	    });
		//获取用户id对应的用户组
		var showGroupData=null;
		$.post("/queryGroupById",{"userId":userId},function(data){
			showGroupData=data;
		});
		var userName = $('.topT .firstI .who').text();
		//用户组选择
		showUserPower('.newLinkUserGroup', '.newLink .userGrouppBox', '.newLink .selectGroup');
		// IP地址验证
		jQuery.validator.addMethod("ip", function(value, element) { 
			var ip = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/; 
			return this.optional(element) || (ip.test(value) && (RegExp.$1 < 256 && RegExp.$2 < 256 && RegExp.$3 < 256 && RegExp.$4 < 256)); 
			}, "非法的ip地址");
		
	    
		
		// 端口验证
	    jQuery.validator.addMethod("port", function(value, element) {    
	        return this.optional(element) || /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-5]{2}[0-3][0-5])$/.test(value);   
	      }, "非法的端口号");
	    
	    // 字符验证，只能包含中文、英文、数字、下划线、小数点等字符。    
	    jQuery.validator.addMethod("stringCheck", function(value, element) {       
	         return this.optional(element) || /^[a-zA-Z0-9\.\s\u4e00-\u9fa5-_]+$/.test(value);       
	    }, "含有非法字符");
	  //========================顶行   新增连接===================
	    $(showBtn).click(function () {
	        $(maskBox).show();
	        $(".newLink").show();
	        $(".newLink .isJumpBox").show();
	        $(".newLink .save").removeAttr("disabled");
	        //默认单选框选中
	        $(".addLink_form input[name='is_board']").eq(1).prop("checked",true);
	        //$(".addLink_form input[name='enabled']").eq(1).prop("checked",true);
	        $("#isBoard_div").show();
			$("#isBoard_div").next().show();
	        
	        //清空内容
	        add_validate.resetForm();
//	        $(".addLink_form input[name='port']").val("22");
	        $(".addLink_form .selText").text("");
	        $("#hidden_link_use").removeAttr("oid");
	        $(".newLink .board_input").prop("checked",false);
	        $(".newLink .board_input").parent().removeClass("checkboxBefore");
	        $(".newLink .addLinkTest_result").html("");
	        $(".addLink_form .pass").attr("type", "password").val("");
	        $(".addLink_form .arrowSelf .disNone").val("");
	        $(".newLink .new_userName").val("");
			$(".newLink .jumpList").hide();
			$(".newLink .link_ins").removeAttr("value");
			$(".newLink .re_te").removeAttr("value");
			$(".addLink_form .sure_noticeObject").empty();
			$('#resource_id').val('');
	    	var userStr = "<div class='grou userSelect userSelfSelect'><input type='hidden' value='"+userId+"'/><em>"+userName+"</em></div>";
	    	$.each(showGroupData, function(i, e){
		    	userStr += '<div class="grou groupSelect"><input type="hidden" value="'+ e.id +'"><em>'+ e.group_name +'</em><b class="remo"></b></div>'
		    });
	    	$(".newLink .sure_noticeObject").append(userStr);
	    	
	    });
	    //连接地址和连接用途的下拉选中赋值事件
	    $('.add_lU .head .selText').click(function(){
	    	$('.add_lU .head .arrowDD').click();
	    });
	    $(document).on('click', '.newLink .arrowSelf .linkUSE ul>li', function(){
	    	//去掉选择跳板选中状态
	    	$(".board_input").prop("checked",false).parent().removeClass("checkboxBefore");
	    	$(".edit_baordLink").prop("checked",false).parent().removeClass("checkboxBefore");
	    	$(".jumpList").hide();
	    	var value = $(this).attr('value');
	    	var html = $(this).html();
	    	var int = $(this).parent().parent().parent().parent().siblings().children('input');
	    	int.val(html).attr('oid', value);
	    });
	    
	    //点击连接用途--新增 清空相关内容
	    $(".addLink_form .lk_use").on('click','li',function(){
	    	//获取连接用途--新增的显示值
	    	var textAdd=$("#link_use").prev('.selText').text().trim();
	    	var trim=$(this).text().trim();
	    	//var value=$(this).parent().parent().parent().parent().prev().children(".selText").text().trim();
	    	//判断连接用途是否与之前一致
	    	if(trim!=textAdd){
	    		clearLinkUse();
	    	}
	    });
	    function clearLinkUse(){
	    	//清空连接类型
	    	$(".lk_te").empty();
	    	$(".add_linkType_div").text("");
	    	$(".edit_linkType_div").text("");
	    	
	    	//清除资源类型
	    	$("#link_re_ty").text("");
	    	$(".edit_model_div").text("");
	    	$(".re_te").empty();
	    	$(".re_te").removeAttr("value");
	    	
	    	//清除资源对象
	    	$(".resDiv p").text("");
	    	$(".edit_instance_div").text("");
	    	$(".link_ins").html("");
	    	
	    	//清空连接地址
	    	$("input[name='address']").val("");
	    	$(".lk_addr").html("");
	    	$(".lk_addr").empty();
	    }
	  //获取连接类型--新增连接
	    $(".addLink_form").on('click','.linkType_add .head',function(){
	    	var li="";
	    	$(".lk_te").empty();
	    	//获取模型id
	    	var modelId=$("#link_use").attr("oid");
	        //根据模型id查询连接类型
	    	$.post("/cmdb/query/linkType",{"modelId":modelId},function(data){
	    		//console.log(data);
	    		$.each(data,function(key,value){
	    			li+="<li value='"+key+"'>"+value+"</li>";
	    		});
	    		$(".lk_te").html(li);
	    	});
	    });
	  //点击连接类型--编辑    清空资源类型,连接用途,资源对象,连接地址	
    	$(".newLink .lk_te").on('click','li',function(){
    		//获取连接类型的显示值
    		var value=$(".addLink_form input[name=linkTypeID]").val();
    		var select=$(this).attr('value');
    		if(select == 1){
    			$('.newLink input[name=address]').rules('add', {
    				ip: true
    			});
    		}else{
    			$('.newLink input[name=address]').rules('remove', 'ip');
    		}
    		if(select == 3){
    			$('.add_resourceType').prev('span').text('适配器');
    			$('.newLink .isJumpBox').hide();
    			$('.newLink .http-type').show();
    			$('.newLink .user-i').hide();
    			$('.newLink .new_userName').rules('remove', 'required');
    			$('.newLink .pass').rules('remove', 'required');
    			$('.newLink .isJumpBox').find('input').prop('checked', false);
    			$('.newLink .isJumpBox').find('.checkboxBefore').removeClass('checkboxBefore');
    			$('.newLink .http-type').find('input[value=http]').prop('checked', true);
    			$(".addLink_form input[name='port']").val("");
    			var add_oid = $('#link_use').attr('oid');
    			$('#link_resource').attr('name', 'adapterId');
    			$('#link_source_id').attr('name', 'modelID').val(add_oid);
    		}else{
    			$('.add_resourceType').prev('span').text('资源类型');
    			$('.newLink .user-i').show();
    			$('.newLink .isJumpBox').show();
    			$('.newLink .http-type').hide();
    			$('.newLink .new_userName').rules('add', {
    				required: true
    			});
    			$('.newLink .pass').rules('add', {
    				required: true
    			});
    			$('.newLink .http-type').find('input').prop('checked', false);
    			$('.newLink .isJumpBox').find('input[value=n]').prop('checked', true);
    			$(".addLink_form input[name='port']").val("22");
    			$('#link_resource').attr('name', 'modelID');
    			$('#link_source_id').attr('name', 'adapterId').val('');
    		}
    		if(select != value){
    			clearLinkType();
    		}
    	});
    	
    	 //jmx类型去掉跳板选择	
    	$(".lk_te").on('click','li',function(){
    		if($(this).attr('value')==2){
    			$(".isJumpBox").hide();
    			$(".lists").empty();
    			$("input[name='is_board'][value='n']").prop("checked",true);
    			$(".editBoardLink_div").show();
       		    $(".editBoardLink_div").next().show();
       		    $(".board_input").prop("checked",false).parent().removeClass("checkboxBefore");
       		    $(".jumpList").hide();
       		    $("#hidden_link_use").removeAttr("oid");
    		}
//    		else{
//    			$(".isJumpBox").show();
//    		}
    	});
    	function clearLinkType(){
    		//清空资源类型
    		$("#link_re_ty").text("");
    		$(".re_te").html("");
    		$(".re_te").removeAttr("value");
        	$(".edit_model_div").text("");
        	
        	//清空资源对象
        	$(".resDiv p").text("");
        	$(".edit_instance_div").text("");
        	$(".link_ins").html("");
        	$(".link_ins").empty();
        	//清空连接地址
        	$("input[name='address']").val("");
        	$(".lk_addr").html("");
        	$(".lk_addr").empty();
    	}
    	
    	//==================新增连接--资源类型===================================   
    	$(".addLink_form").on('click','.add_resourceType .head',function(){
    		//获取连接类型
    		var text=$(".addLink_form input[name=linkTypeID]").val();
    		if(text !=null&&text !=""){
    			if(text.trim()==1){
    				$.post("/cmdb/linkResourceType",function(data){
    			    	var li="";
    			    	$.each(data,function(i,e){
    			    		li+="<li value='"+e.id+"'>"+e.name+"</li>";
    			    	});
    			    	$(".addLink_form .re_te").html(li);
    			    });
    			}
    			else if(text.trim()==2){
    				$.post("/cmdb/query/middleware",function(data){
    			    	var li="";
    			    	$.each(data,function(i,e){
    			    		li+="<li value='"+e.id+"'>"+e.name+"</li>";
    			    	});
    			    	$(".addLink_form .re_te").html(li);
    			    });
    			}else if(text.trim() == 3){
    				//adapter 适配器
    				var id = $('#link_use').attr('oid');
    				$.get("/cmdb/findAllAdapterInfoByMid", {mid: id}, function(data){
    			    	var li="";
    			    	$.each(data,function(i,e){
    			    		li+="<li value='"+i+"'>"+e+"</li>";
    			    	});
    			    	$(".addLink_form .re_te").html(li);
    			    });
    			}
    			
    		}
    	});
    	//==================点击资源类型_新增--清空对象和地址======================   
        $(".newLink .re_te").on("click","li", function(){
        	//获取当前资源类型的显示值
        	var value=$("#link_re_ty").text().trim();
        	var select=$(this).text().trim();
        	if(select !=value){
        		clearSourceType();
        	}
        });
        function clearSourceType(){
        	//清空资源对象
        	$(".link_ins").html("");
        	$(".resDiv p").text("");
        	$(".edit_instance_div").text("");
        	//清空ip地址
        	$("input[name='address']").val("");
        	$(".lk_addr").html("");
        	$(".lk_addr").empty();
        }
        //获取所属资源对应的实例--新增
        $(".addLink_form").on('click','.resDiv .head',function(){
        	var id=$('#link_resource').val();
        	var text=$(".addLink_form input[name=linkTypeID]").val();
        	if(text == 3){
        		id = $('#link_use').attr('oid');
        	}
        	var li="";
        	if(id !=null&&id !=""){
        		$.post("/cmdb/linkIns",{"id":id},function(data){
    				$.each(data,function(i,e){
    		    	    li+="<li value='"+e["_id"]["$oid"]+"'><p>"+e.name+"</p></li>";
    		    });
    			$(".addLink_form .link_ins").html(li);
    	    });
        	}
        });
      //=================点击资源对象_新增--清空地址======================================    
        $(".newLink .link_ins").on("click","li", function(){
        	//获取资源对象的显示值
        	var value=$(".newLink input[name='instanceID']").prev('.selText').text().trim();
        	var select=$(this).text().trim();
        	if(select !=value){
            	var ins_id=$(this).attr("value");
            	var resource_id=$(".newLink input[name=modelID]").val();
            	if(ins_id !=null&&ins_id !=""&&resource_id !=null&&resource_id !=""){
            		$("input[name='address']").val('');
        			$("#new_address").text('');
        			$('.newLink input[name=port]').val('');
            	$.post("/cmdb/address",{"resourceId":resource_id,"insId":ins_id},function(data){
            		if(data !=null&&data !=""){
            			if(data.ip){
            				$("input[name='address']").val(data.ip);
            				$("#new_address").text(data.ip);
            				if(data.port != undefined){
            					$('.newLink input[name=port]').val(data.port);
            				}else{
            					$('.newLink input[name=port]').val(22);
            				}
            			}
            		}
            	});
            	}
            	//清空连接地址
            	$(".lk_addr").html("");
            	$(".lk_addr").empty();
        	}
        });
        //获取连接地址--新增
        $(".link_addr_click").click(function(){
        	addLinkAddr();
        });
        function addLinkAddr(){
        	var li="";
        	$(".lk_addr").empty();
        	var ins_id=$(".resDiv input[name=instanceID]").val();
        	var resource_id=$("#link_resource").val();
        	if(ins_id !=null&&ins_id !=""&&resource_id !=null&&resource_id !=""){
        	$.post("/cmdb/address",{"resourceId":resource_id,"insId":ins_id},function(data){
        		if(data !=null&&data !=""){
        			if(data.ip){
        				li="<li value='"+data.ip+"'>"+data.ip+"</li>";
        			}
            		$(".lk_addr").html(li);
        		}
        	});
        	}
        }
        //点击事件移除错误信息(下拉框)
    	$(".link-model-form .form .cont").click(function(){
        	var ele= $(this).parents(".taskSystem").next().next();
        	if(ele.hasClass("error")){
        		ele.remove();
        	}
        });
    	//点击事件移除错误信息(可输可选框)
    	$(".link-model-form .form .arrowSelf .cont").click(function(){
        	var ele= $(this).parents(".taskSystem").next().next();
        	if(ele.hasClass("error")){
        		ele.remove();
        	}
        });
    	
//    	// IP地址验证
//    	jQuery.validator.addMethod("ip", function(value, element) { 
//    		var ip = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/; 
//    		return this.optional(element) || (ip.test(value) && (RegExp.$1 < 256 && RegExp.$2 < 256 && RegExp.$3 < 256 && RegExp.$4 < 256)); 
//    		}, "非法的ip地址");
//    	
//        
//    	
//    	// 端口验证
//        jQuery.validator.addMethod("port", function(value, element) {    
//            return this.optional(element) || /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-5]{2}[0-3][0-5])$/.test(value);   
//          }, "非法的端口号");
//        
//        // 字符验证，只能包含中文、英文、数字、下划线、小数点等字符。    
//        jQuery.validator.addMethod("stringCheck", function(value, element) {       
//             return this.optional(element) || /^[a-zA-Z0-9\.\s\u4e00-\u9fa5-_]+$/.test(value);       
//        }, "含有非法字符");
        
        //========================新增连接--校验======================================================
    
         var add_validate=$(".addLink_form").validate({
        	onkeyup: false,
        	onsubmit: false,
    		onfocusout: function(element) { $(element).valid(); },
    		errorElement:"b",
    		errorPlacement:function(error,element) {  
    			if(element.attr("name")=="linkUse_add"||element.attr("name")=="address"){
    				element.parent().parent().next().after(error);
    			}
    			else{
    				element.next().after(error);
    			}
    		},
    		 rules:{
    			 userName:{
                     maxlength :30,
                     stringCheck:true,
                     required:true
                 },
                 port:{
                	 port:true,
                	 required:true
                 },
                 address:{
                     ip:true,
                     required:true
                 },
                password:{
                	 required:true
                 },
                 linkUse_add:{
                	 required:true
                 }
             },
             messages:{    
            	 userName:{    
     	        	maxlength:"最多输入30个字符",
     	        	required:"请输入用户名"
     	       },
     	       port:{
     	    	  digits:"请输入合法的数字",
     	    	 required:"请输入端口"
     	       },
     	      password:{
     	    	 required:"请输入密码"
     	       },
     	      address:{
     	    	 required: function(){
     	    		 var error =  "请输入ip";
     	    		 if($('.addLink_form input[name=linkTypeID]').val() != 1){
     	    			 error = '请输入ip/域名/主机名';
     	    		 }
     	    		 return error;
     	    	 }
     	      },
     	      linkUse_add:{
     	    	 required:"请选择连接用途"
     	      }
     	   }
        });
        
      //新建连接_显示密码
        $(".addLink_form ").on("click",".eyes",(function(){
        	if($(this).hasClass("blind")) {
        		$(this).removeClass("blind").addClass("lighting")
        		$(".addLink_form .pass").attr("type", "text");
        	} else {
        		$(this).removeClass("lighting").addClass("blind");
        		$(".addLink_form .pass").attr("type", "password");
        	}
        })
        );
        
        //新建连接的确定
        $(".newLink .save").click(function () {
        	//是否为简易模型的保存
        	var is_simple_model = !(!$.isEmptyObject(hasData) && !$.isEmptyObject(hasData.simple_model));
        	//页面校验
        	//判断连接类型是否为空
        	var add_linkType=$(".addLink_form input[name=linkTypeID]").val().trim();
        	//判断资源类型是否为空
        	var resourceType=$(".addLink_form #link_resource").val().trim();
        	//判断资源对象
        	var add_instance=$(".resDiv input[name=instanceID]").val().trim();
        	var valid=$(".addLink_form").valid();
        	if(!valid||add_linkType==null||add_linkType==""||resourceType==null||resourceType==""||add_instance==null||add_instance==""){
        		if(add_linkType==null||add_linkType==""){
            		$(".addLink_form .add_linkType_div").parent().parent().next().after('<b class=error >请选择连接类型</b>');
            	}
        		 if(resourceType==null||resourceType==""){
        			 var l_t = $('.addLink_form input[name=linkTypeID]').val();
        			 var e_b = '请选择资源类型';
        			 if(l_t == 3){
        				 e_b = '请选择适配器';
        			 }
            		$(".addLink_form #link_re_ty").parent().parent().next().after('<b class=error >'+ e_b +'</b>');
            	}
        		if(add_instance==null||add_instance==""){
        			$(".resDiv").next().after('<b class=error >请选择资源对象</b>');
        		}
        		return false;
        	}
        	
        	//有效性校验(资源对象+ 连接类型 +连接地址+连接用途+连接用户  不能相同)
        	//获取相关数据
        	var instanceID=$(".addLink_form input[name='instanceID']").val();
        	var linkTypeID=$(".addLink_form input[name='linkTypeID']").val();
        	var address=$(".addLink_form input[name='address']").val();
        	var userName=$(".addLink_form input[name='userName']").val();
        	$("#hidden_link_use").val($("#link_use").attr('oid'));
        	var linkUseID=$(".addLink_form input[name='linkUseID']").val();
        	$.post("/cmdb/link/validate",{"linkTypeID":linkTypeID,"instanceID":instanceID,"linkUseID":linkUseID,"address":address,"userName":userName},function(data){
        		$("#hidden_link_use").val($("#link_use").attr('oid'));
        	    	var board_linkId=[];
        	    	//添加选中的跳板连接
        	    	$(".addLink_form .lists li div input").each(function(i,e){
        	    		if($(this).prop("checked")){
        	    			if($(".addLink_form .board_input").prop("checked")&&$(".addLink_form input[name='is_board'][value='n']").prop("checked")){
        	    				board_linkId.push($(this).val());
        	    			}
        	    		}
        	    	});
        	    	$("#boardLinkId_ADD").val(board_linkId);
        	    	//获取选中的用户组
        	    	var groupIds=[];
        	    	var userIds=[];
        	    	var group=$(".addLink_form .sure_noticeObject").find('.groupSelect').children("input");
        	    	$.each(group,function(){
        	    		groupIds.push($(this).val());
        	    	});
        	    	if(!is_simple_model){
        	    		groupIds = showGroupData;
        	    	}
        	    	$(".addLink_form input[name='groupId']").val(groupIds.toString());
        	    	//获取选中的用户
        	    	var user=$(".addLink_form .sure_noticeObject").find('.userSelect').children("input");
        	    	$.each(user,function(i,e){
        	    		/*if(!$(this).parent().hasClass('userSelfSelect')){
        	    			userIds.push($(e).val());
        	    		}*/
        	    		userIds.push($(e).val());
        	    	});
        	    	if(!is_simple_model){
        	    		userIds.push(userId);
        	    	}
        	    	$(".addLink_form input[name='userId']").val(userIds.toString());
        	    	//添加更新人
        	    	$(".addLink_form input[name='updateId']").val(userId);
        	    	//console.log($(".addLink_form").serialize());
        	    	//修改保存按钮的状态
        	    	if(data!=0){
        	    		if(!is_simple_model){
        	    			confirmMask('此连接已存在,是否使用该连接？', 'red', function(){
        	    				hasData.simple_model.save_fn(getData);
        	    			});
            	    	}else{
            	    		alertMask2("此连接已存在");
            	    	}
            			
            			return false;
            		}else{
            			if(!is_simple_model){
            	    		hasData.simple_model.save_fn(getData);
            	    	}else{
            	    		$(".newLink .save").prop("disabled",true);
            	    		$.post("/addLink",serialize(".addLink_form"),function(data){
                	    		$(".newLink .save").prop("disabled",false);
                	    		if("校验通过"==data["sucMes"]){
            	    				if(data["success"]){
            	    					alertMask("新增连接成功", 'green', function(){
            	    						if(!$.isEmptyObject(hasData) && hasData.noReload){
            	    							hasData.noReload_fn(getData('.addLink_form'), data);
            	    							$(maskBox).hide();
            	    				            $(".newLink").hide();
            	    						}else{
            	    							window.location.reload();
            	    						}
            	    						
            	    					});
            	    				}
            	    				else{
            	    					alertMask("新增连接失败", 'red', function(){
        	    							if(!$.isEmptyObject(hasData) && hasData.noReload){
        	    								hasData.noReload_fn(getData('.addLink_form'), data);
        	    								$(maskBox).hide();
        	    					            $(".newLink").hide();
            	    						}else{
            	    							window.location.reload();
            	    						}
            	    					});
            	    				}
            	    			}
                	    		else if("校验失败"==data["sucMes"]){
                	    			alertMask(data["errMes"]);
                	    			$(".newLink .save").removeAttr("disabled");
                	    		}
                	    		/*if(data){
                	    			alertMask("新增连接成功", 'green', function(){
                						window.location.reload();
                					});
                	    		}
                	    		else{
                	    			alertMask("新增连接失败", 'red', function(){
                						window.location.reload();
                					});
                	    		}*/
                	        });
            	    	}
            		}
        	    	
        	});
        	$(".addLink_form .eyes").removeClass("lighting").addClass("blind");
    		$(".addLink_form .eyes").attr("type", "text");
        });
        //新建连接的取消
        $(".newLink .cancel").click(function () {
            $(maskBox).hide();
            $(".newLink").hide();
            $(".addLink_form .eyes").removeClass("lighting").addClass("blind");
    		$(".addLink_form .eyes").attr("type", "text");
        });
//        $(".close").click(function () {
//        	backMon();
//        	 $(".mask").hide();
//             $(this).parent().hide();
//        });
        //选中是否跳板单选框--新增连接
        $(".addLink_form input[name='is_board']").click(function(){
       	 if($("input[name='is_board']").eq(0).prop("checked")){
       		 $("#isBoard_div").removeClass("checkboxBefore").children().prop("checked",false);
       		 $("#isBoard_div").hide();
       		 $("#isBoard_div").next().hide();
       		 $(".addLink_form .jumpList").hide();
       		 $(".addLink_form .jumpList .lists").empty();
       	 }
       	 else{
       		 $("#isBoard_div").show();
       		 $("#isBoard_div").next().show();
       	 }
        });
        
       //点击是否跳板,显示跳板选择
        $(".addLink_form").on('click','.board_input',function(data){
       	 if($(".board_input").prop("checked")){
       		//获取连接用途的id
           	 var modelId=$("#hidden_link_use").attr("oid");
       		 var str="";
       		 $(".jumpList .lists").empty();
           		 $.post("/cmdb/queryBarodList",{"modelId":modelId},function(data){
           			 if(data !=null&&data.length >0){
           				 $(".jumpList").show();
           				 $(".jumpList .lists").empty();
           				 $.each(data,function(i,e){
           					 str+="<li><div class='runSelect checkboxDiv'><input type='checkbox' value='"+e.id+"' class='checkboxBtn'></div><em>"+e.address+"-"+e.userName+"</em><b></b></li>";
           				 });
           				 $(".jumpList .lists").append(str);
           			 }
           			 else{
           				 alertMask2("没有可选的跳板连接,请重新配置...");
           			 }
               	 });
       	 }
       	 else{
       		 $(".jumpList").hide();
       	 }
        });
        //连接测试-新增
        $('.newLink .linkTest').click(function(){
        	add_linkTest();
        });
      //====================  新增-连接测试  ==============================
        function add_linkTest(){
        	$(".addLinkTest_result").empty();
        	$(".addLinkTest_result").css("color","#c7ca1b").html("连接测试中...");
        	$(".addLink_form .lists li b").each(function(i,e){
        		$(this).html("");
        	});
        	//添加选中的跳板连接
        	var board_linkId=[];
        	if($(".board_input").prop("checked")){
        		$(".addLink_form .lists li div input").each(function(i,e){
        			if($(this).prop("checked")){
        				board_linkId.push($(this).val());
        			}
        		});
        		$("#boardLinkId_ADD").val(board_linkId);
        	}
        	
        	if(board_linkId.length==0){
        		$.post("/testLink",serialize(".addLink_form"),function(data){
//        			if(data){
//        				$(".addLinkTest_result").css("color","#3DA745").html("密码连接成功");
//        			}
//        			else{
//        				$(".addLinkTest_result").css("color","#F00").html("密码连接失败");
//        			}
        			if(data.SUCCESS){
        				$(".addLinkTest_result").css("color","#3DA745").html('密码连接成功');
        			}else{
        				$(".addLinkTest_result").css("color","#F00").html(data.msg);
        			}
        		});
        	}
        	else{
        		$.post("/testBoardLink",serialize(".addLink_form"),function(data){
        			//console.log("测试返回的数据:"+data);
        			if(data==null){
        				alertMask2("未知错误...");
        			}
        			else if($.trim(data["test"])=="测试连接成功"){
        				$(".addLinkTest_result").css("color","#3DA745").html("测试连接成功");
        			}
        			else if($.trim(data["test"])=="测试连接失败"){
        				$(".addLinkTest_result").css("color","#F00").html("测试连接失败");
        			}
        			for ( var key in data) {
        				if($.trim(data[key])=="测试成功"){
        					$(".addLink_form .lists li div").find('input[value='+key+']').parent().next().next().text(data[key]).addClass("succ");
        				}
        				else if($.trim(data[key])=="测试失败"){
        					$(".addLink_form .lists li div").find('input[value='+key+']').parent().next().next().text(data[key]).addClass("fail");
        				}
        			}
        		});
        	}
        }
        if(!$.isEmptyObject(hasData)){
        	addLink_two(hasData);
        }
        function serialize(obj){
        	$(obj).find('.disable-select input').prop('disabled', false);
        	var str = $(obj).serialize();
        	$(obj).find('.disable-select input').prop('disabled', true);
        	return str;
        }
   	 function getData(form) {
	      var o = {};
	      $(form).find('.disable-select input').prop('disabled', false);
      	  var a = $(form).serializeArray();
      	  $(form).find('.disable-select input').prop('disabled', true);
	      $.each(a, function() {
	          if (o[this.name] !== undefined) {
	              if (!o[this.name].push) {
	                  o[this.name] = [o[this.name]];
	              }
	              o[this.name].push(this.value || '');
	          } else {
	              o[this.name] = this.value || '';
	          }
	      });
	      return o;
   	 }
        function addLink_two(hasData){
        	var modelName = hasData.modelName,
		   		mid = hasData.mid,
		   		rid = hasData.rid,
		   		toolId = hasData.toolId,
		   		resName = hasData.resName
        	//========================顶行   新增连接===================
    	    $(showBtn).click(function () {
    	    	var is_simple_model = $.isEmptyObject(hasData.simple_model);
    	    	if(!is_simple_model){
    	    		var new_hasData = hasData.simple_model.data_fn();
    	    		
    	    		modelName = new_hasData.modelName;
    		   		mid = new_hasData.mid;
    		   		rid = new_hasData.rid;
    		   		toolId = new_hasData.toolId;
    		   		resName = new_hasData.resName;
    	    	}
    	    	live_select('.disable-select');
    	    	$(".newLink #link_use").val(modelName);
    			$(".newLink #link_use").prev('.selText').text(modelName);
    			$(".newLink #link_use").attr('oid',mid);
    			$(".newLink input[name='linkUseID']").val(mid);
    			disable_select('.add_lU');
    			$.getJSON('/cmdb/query/'+ mid +'/'+ rid +'/linkType/' + toolId, function(data){
    				console.log(data);
    				var link_type_name = data.linkTypeName,
    					link_type_id = data.linkTypeId;
    				
//    				for(var key in data.linkType){
//    					link_type_id = key;
//    					link_type_name = data.linkType[key];
//    				}
//    				link_type_name = 'Adapter'
//    				var j = '{"SUCCESS":true,"adapterInfo":{"desc":"","fileName":"ext_tomcat.jar","id":2,"modelId":"5acc2850e037c537ace531a0","name":"tomcat","version":""},"linkType":{"3":"Adapter"}}';
//    				data = JSON.parse(j);
    				//连接类型填充
    				disable_select('.linkType_add');
    				$('.add_linkType_div').text(link_type_name)
    				$('.newLink input[name=linkTypeID]').val(link_type_id);
    				if(link_type_id == 1){
    	    			$('.newLink input[name=address]').rules('add', {
    	    				ip: true
    	    			});
    	    		}else{
    	    			$('.newLink input[name=address]').rules('remove', 'ip');
    	    		}
    	    		if(link_type_id == 3){
    	    			$('.add_resourceType').prev('span').text('适配器');
    	    			$('.newLink .isJumpBox').hide();
    	    			$('.newLink .http-type').show();
    	    			$('.newLink .user-i').hide();
    	    			$('.newLink .new_userName').rules('remove', 'required');
    	    			$('.newLink .pass').rules('remove', 'required');
    	    			$('.newLink .isJumpBox').find('input').prop('checked', false);
    	    			$('.newLink .isJumpBox').find('.checkboxBefore').removeClass('checkboxBefore');
    	    			$('.newLink .http-type').find('input[value=http]').prop('checked', true);
    	    			$(".addLink_form input[name='port']").val("");
    	    			var add_oid = $('#link_use').attr('oid');
    	    			$('#link_resource').attr('name', 'adapterId');
    	    			$('#link_source_id').attr('name', 'modelID').val(add_oid);
    	    		}else{
    	    			$('.add_resourceType').prev('span').text('资源类型');
    	    			$('.newLink .user-i').show();
    	    			$('.newLink .isJumpBox').show();
    	    			$('.newLink .http-type').hide();
    	    			$('.newLink .new_userName').rules('add', {
    	    				required: true
    	    			});
    	    			$('.newLink .pass').rules('add', {
    	    				required: true
    	    			});
    	    			$('.newLink .http-type').find('input').prop('checked', false);
    	    			$('.newLink .isJumpBox').find('input[value=n]').prop('checked', true);
    	    			$(".addLink_form input[name='port']").val("22");
    	    			$('#link_resource').attr('name', 'modelID');
    	    			$('#link_source_id').attr('name', 'adapterId').val('');
    	    		}
    	    		if(link_type_id==2){
    	    			$(".isJumpBox").hide();
    	    			$(".lists").empty();
    	    			$("input[name='is_board'][value='n']").prop("checked",true);
    	    			$(".editBoardLink_div").show();
    	       		    $(".editBoardLink_div").next().show();
    	       		    $(".board_input").prop("checked",false).parent().removeClass("checkboxBefore");
    	       		    $(".jumpList").hide();
    	       		    $("#hidden_link_use").removeAttr("oid");
    	    		}
    	    		
    	    		if(link_type_id == 3){
    	    			$('#link_resource').val(data.adapterInfo.objectId);
    	    			$('#link_re_ty').text(data.adapterInfo.name);
    	    			disable_select('.add_resourceType');
    	    			
    	    			$('.resDiv input[name=instanceID]').val(rid);
    	    			$('.resDiv .selText').text(resName);
    	    		}else if(link_type_id == 2){
    	    			$('#link_resource').val(mid);
    	    			$('#link_re_ty').text(modelName);
    	    			disable_select('.add_resourceType');
    	    			
    	    			$('.resDiv input[name=instanceID]').val(rid);
    	    			$('.resDiv .selText').text(resName);
    	    		}else if(link_type_id == 1 && data.ins != undefined){
    	    			$('#link_resource').val(data.model.id);
    	    			$('#link_re_ty').text(data.model.name);
    	    			disable_select('.add_resourceType');
    	    			
    	    			$('.resDiv input[name=instanceID]').val(data.ins._id.$oid);
    	    			$('.resDiv .selText').text(data.ins.name);
    	    			disable_select('.resDiv');
    	    			if(data.ins.ip){
            				$("input[name='address']").val(data.ins.ip);
            				$("#new_address").text(data.ins.ip);
            				if(data.ins.port != undefined && data.ins.port != ''){
            					$('.newLink input[name=port]').val(data.ins.port);
            				}else{
            					$('.newLink input[name=port]').val('22');
            				}
            			}
    	    		}
    	    		if(link_type_id == 1){
    	    			$('#resource_id').val(rid);
    	    		}
    	    		if(link_type_id != 1){
    	    			$.post("/cmdb/address",{"resourceId":mid,"insId":rid},function(data){
    	            		if(data !=null&&data !=""){
    	            			if(data.ip){
    	            				$("input[name='address']").val(data.ip);
    	            				$("#new_address").text(data.ip);
    	            				if(data.port != undefined){
    	            					$('.newLink input[name=port]').val(data.port);
    	            				}else{
    	            					$('.newLink input[name=port]').val(22);
    	            				}
    	            			}
    	            		}
    	            	});
    	    		}
    	    		
    			});
    	    });
    	  //==================新增连接--资源类型====================== 
    	    $(".addLink_form").off('click','.add_resourceType .head');
    		$(".addLink_form").on('click','.add_resourceType .head',function(e){
    			//获取连接类型
//    			var text=$(".add_linkType_div").text();
    			var lt_id = $('.addLink_form input[name=linkTypeID]').val();
    			if(lt_id !=null&&lt_id !=""){
    				if(lt_id.trim()==1){
    					$.post("/cmdb/ins/"+mid+"/linkResourceType",function(data){
    				    	var li="";
    				    	$.each(data,function(i,e){
    				    		li+="<li value='"+e.id+"'>"+e.name+"</li>";
    				    	});
    				    	$(".re_te").html(li);
    				    	/*if(!li){
    				    		$(".addLink_form input[name='modelID']").parent().parent().next().after("<b class='error'>没有找到运行于的模型</b>");
    				    	}*/
    				    });
    				}
    				else if(lt_id.trim()==2){
        				$.post("/cmdb/query/middleware",function(data){
        			    	var li="";
        			    	$.each(data,function(i,e){
        			    		li+="<li value='"+e.id+"'>"+e.name+"</li>";
        			    	});
        			    	$(".addLink_form .re_te").html(li);
        			    });
        			}else if(lt_id.trim() == 3){
        				//adapter 适配器
        				var id = $('#link_use').attr('oid');
        				$.get("/cmdb/findAllAdapterInfoByMid", {mid: id}, function(data){
        			    	var li="";
        			    	$.each(data,function(i,e){
        			    		li+="<li value='"+i+"'>"+e+"</li>";
        			    	});
        			    	$(".addLink_form .re_te").html(li);
        			    });
        			}
    			}
    		});
        }
        return add_validate;
	}
	exports.addLink = addLink;
	
});