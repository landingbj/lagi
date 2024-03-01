/**
 * b.page.js
 * ===========================================================
 * author : Terry
 * created : 2015.08.18
 * 
 * version : 1.1
 * 
 * 基于bootstrap风格，可静态页面跳转也可异步页面处理的独立分页插件
 * 
 * 更新记录：
 * 2015.09.09 - 解决pageSize=undefined的问题
 * 2015.10.08 - 解决每页记录数下拉选中的值的问题；
 *            - 增加部分初始参数从 data- 属性中获取；
 * 2016.04.28 - 增加异步分页后的回调方法，并在分页处理完成后执行该回调方法
 * 2017.07.10 - 重构代码
 *              解决异步模式下首次不加载的问题
 *              解决异步模式下（服务端页面模式）参数提取的问题，开发者在服务端返回的页面中必须按要求设置分页信息内容
 *              增加异步模式下自定义ajax返回后的处理
 * 2017.07.17 - 增加分页信息统一刷新内部方法
 *              去除totalPage参数的显式设置，修改为程序根据totalRow和pageSize进行计算，后续程序不再需要初始化该字段
 */
!function ($) {
	"use strict";
	/**
	 * 默认参数
	 */
	var defaults = {
		//每页显示记录数
        "pageSize"       : 10,
        //当前页号
        "pageNumber"     : 1,
        //总记录个数
        "totalRow"       : 0,
        //显示页码个数，建议使用奇数
        "pageBarSize"    : 5,
        //每页显示记录数设置
        "pageSizeMenu"   : [10,20,50,100],
        //业务参数集，参数为function，function的返回值必须为Object格式：{a:1,b:2,……}
        "params"         : undefined,
        //自定义样式
        "className"      : undefined,
        //异步处理分页
        "asyncLoad"      : false,
        //异步处理对象容器，支持使用jquery表达式
        //服务端返回页面模式使用
        "asyncTarget"    : 'body',
        //异步提交方式，默认POST
        "asyncType"      : 'POST',
        //是否使用服务端返回页面的形式
        //该参数仅在异步处理模式下有效（asyncLoad = true）
        "serverSidePage" : false,
        //异步数据模式自定义处理回调，ajax请求服务端并返回json数据后，可使用数据进行自定义页面渲染
        "render"         : undefined,
        //分页跳转URL
        "url"            : '',
        //异步方式分页后，并在返回的页面内容嵌入到指定位置后，执行该回调，跳转方式不执行该回调
        //参数param，插件的参数集
        "callback"       : $.noop
	};
	/**
	 * 模板
	 */
	var template = '<div class="pagination bPage bPageRight">' + 
		 '<ul>' + 
		 '<li id="bPageList" class="disabled bPageList"><a>每页 <select id="bPageDropList"></select> 条</a></li>' + 
		 '<li id="bPageInfo" class="disabled bPageInfo"><a>&nbsp;</a></li>' + 
		 '<li id="bPageFirstPage" class="bPageControlButton"><a href="javascript:void(0);">首页</a></li>' + 
		 '<li id="bPagePreviousPage" class="bPageControlButton"><a href="javascript:void(0);">«</a></li>' + 
		 '<li id="bPageNextPage" class="bPageControlButton"><a href="javascript:void(0);">»</a></li>' + 
		 '<li id="bPageLastPage" class="bPageControlButton"><a href="javascript:void(0);">尾页</a></li>' + 
		 '</ul>' + 
		 '</div>';
	/**
	 * 构造方法
	 */
	var bPage = function(element,p){
		this.$container = element;
		this.p = p;
		this.pageNumber = Number(p.pageNumber);
		this.pageSize = Number(p.pageSize);
		this.totalRow = Number(p.totalRow);
	};
	/**
	 * 插件常量
	 */
	bPage.version = '1.1';
	/**
	 * 绑定事件的名称，使用了bPage的命名空间
	 */
	bPage.eventName = 'click.bPage';
	/**
	 * 服务端返回页面模式中，需要在返回的页面中隐藏一个分页信息区域
	 * 该区域需要固定设置样式名称，并在内部增加具体分页信息
	 */
	bPage.pageInfoBoxClassName = 'paginationInfoBox';
	/**
	 * 隐藏域中设置的当前页数据的ID
	 */
	bPage.hiddenPageNumberId = 'bPagePageNumber';
	/**
	 * 隐藏域中设置的每页记录数的ID
	 */
	bPage.hiddenPageSizeId = 'bPagePageSize';
	/**
	 * 隐藏域中设置的总行数的ID
	 */
	bPage.hiddenTotalRowId = 'bPageTotalRow';
	/**
	 * 初始化分页
	 */
	bPage.prototype.init = function(){
		var self = this,p = this.p,elem = this.$container;
		var htmlstr = template;
		$(elem).append(htmlstr);
		if(p.className) $('div.bPage',elem).addClass(p.className);
		
		//设置分页设置每页显示条数的下拉列表
		if(p.pageSizeMenu && $.isArray(p.pageSizeMenu) && p.pageSizeMenu.length > 0){
			$.each(p.pageSizeMenu,function(i,row){
				if(row == this.pageSize){
					$('#bPageDropList',$(elem)).append('<option selected="selected">'+row+'</option>');
				} else {
					$('#bPageDropList',$(elem)).append('<option>'+row+'</option>');
				}
			});
			$('#bPageDropList',$(elem)).val(self.pageSize);
		}
		if(!p.asyncLoad) this.populate();
		this.eventBind();
		
		if(p.asyncLoad) this.pageSwitch(p.pageNumber);
	};
	/**
	 * 更新分页信息
	 */
	bPage.prototype.updatePageInfo = function(){
		if($.type(this.pageNumber) == 'undefined' || $.type(this.pageNumber) != 'number' || this.pageNumber <= 0)
			this.pageNumber = defaults.pageNumber;
		if($.type(this.pageSize) == 'undefined' || $.type(this.pageSize) != 'number' || this.pageSize <= 0)
			this.pageSize = defaults.pageSize;
		if($.type(this.totalRow) != 'undefined' && $.type(this.totalRow) == 'number')
			this.totalPage = Math.ceil(this.totalRow / this.pageSize);
	};
	/**
	 * 数据填充
	 */
	bPage.prototype.populate = function(){
		this.updatePageInfo();
		var self = this, elem = this.$container, p = this.p, pNum = this.pageNumber;
		var _class, _start, _end, _half=Math.floor(p.pageBarSize/2);
		
		var pageInfo = '当前显示第' + this.pageNumber + '/' + this.totalPage + ' 页' + '（共'+this.totalRow+'条记录）';
		$('#bPageInfo a',$(elem)).html(pageInfo);

		//总页数小于显示页码个数
		if(this.totalPage < p.pageBarSize) {
			_start = 1;
			_end = this.totalPage;
		//当前页码小于显示页码个数的一半
		} else if ( pNum <= _half ) {
			_start = 1;
			_end = p.pageBarSize;
		//当前页码大于等于总页数减去显示页码个数一半的值
		} else if ( pNum >= (this.totalPage - _half) ) {
			_start = this.totalPage - p.pageBarSize + 1;
			_end = this.totalPage;
		//常规情况
		} else {
			_start = pNum - _half;
			_end = _start + p.pageBarSize - 1;
		}
		
		//移除分页控制按钮除外的所有页码
		$('li:not(.bPageList,.bPageInfo,.bPageControlButton)',$(elem)).remove();
		//设置页码及事件
		for ( var i=_start ; i<=_end ; i++ ) {
			_class = (i==pNum) ? 'class="active"' : '';
			//_url = this.setUrl(p,j);
			var curPage = $('<li '+_class+'><a href="javascript:void(0);">'+i+'</a></li>').insertBefore($('#bPageNextPage', $(elem)));
			if(i!=pNum) this.setFunction($(curPage), i);
		}
		
		// 处理静态控制按钮样式及链接
		//var _fUrl,_pUrl,_nUrl,_lUrl;
		var _fNum,_pNum,_nNum,_lNum;
		if ( pNum === 1 ) {
			$('#bPageFirstPage,#bPagePreviousPage', $(elem)).addClass('disabled');
			_fNum = -1;
			_pNum = -1;
		} else {
			$('#bPageFirstPage,#bPagePreviousPage', $(elem)).removeClass('disabled');
			_fNum = 1;
			_pNum = pNum>1?pNum-1:1;
		}
		this.setFunction($('#bPageFirstPage a',$(elem)), _fNum);
		this.setFunction($('#bPagePreviousPage a',$(elem)), _pNum);

		if ( pNum === this.totalPage || this.totalPage === 0 ) {
			$('#bPageNextPage,#bPageLastPage', $(elem)).addClass('disabled');
			_nNum = -1;
			_lNum = -1;
		} else {
			$('#bPageNextPage,#bPageLastPage', $(elem)).removeClass('disabled');
			_nNum = pNum<this.totalPage?pNum+1:this.totalPage;
			_lNum = this.totalPage;
		}
		this.setFunction($('#bPageNextPage a',$(elem)), _nNum);
		this.setFunction($('#bPageLastPage a',$(elem)), _lNum);
	};
	/**
	 * 插件事件绑定
	 */
	bPage.prototype.eventBind = function(){
		var self = this;
		$('#bPageDropList',$(this.$container)).on('change',function(event){
			var pSize = $(this).val();
			if(pSize) self.pageSize = Number(pSize);
			self.pageSwitch(1);
		});
	};
	/**
	 * 获得业务参数的URL字符串（URL跳转方式使用）
	 */
	bPage.prototype.buildParamsStr = function(){
		var str='',p = this.p;
		if(p.params && $.isFunction(p.params)){
			var pa = p.params(),attr;
			if($.isPlainObject(pa)){
				for(attr in pa){
					str += '&' + attr + '=' + pa[attr];
				}
			}
		}
		return str;
	};
	/**
	 * 设置服务端请求参数对象（异步使用ajax请求时执行）
	 */
	bPage.prototype.getParams = function(pageNumber){
		var param = {}, p = this.p;
		param.pageNumber = pageNumber;
		param.pageSize = this.pageSize;
		if(p.params && $.isFunction(p.params)){
			var pa = p.params();
			if($.isPlainObject(pa) && !$.isEmptyObject(pa)) param = $.extend({},param ,pa);
		}
		return param;
	};
	/**
	 * 设置事件
	 */
	bPage.prototype.setFunction = function(obj,pageNumber){
		var self = this;
		$(obj).off(bPage.eventName).on(bPage.eventName,function(event){
			self.pageSwitch(pageNumber);
		});
	};
	/**
	 * 从服务端返回的HTML内容中提取分页信息
	 * 
	 * @param data {string} ajax请求后返回的页面内容
	 */
	bPage.prototype.extractPageInfo = function(data){
		if(!data) return;
		var done = true;
		var errorMsg = '提取分页信息失败，请检查分页信息栏是否设置或指定的样式/ID设置有误';
		var box = undefined;
		$(data).each(function(i,row){
			if($(this).hasClass(bPage.pageInfoBoxClassName)){
				box = this;
				return false;
			}
		});
		var pNum = $('#' + bPage.hiddenPageNumberId,$(box)).val();
		var pSize = $('#' + bPage.hiddenPageSizeId,box).val();
		var tRow = $('#' + bPage.hiddenTotalRowId,box).val();
		if(!pNum || !pSize || !tRow){
			done = false;
			console.error(errorMsg);
		}else{
			try {
				if(pNum) this.pageNumber = Number(pNum);
				if(pSize) this.pageSize = Number(pSize);
				if(tRow) this.totalRow = Number(tRow);			
			} catch (e) {
				done = false;
				console.error(errorMsg);
			}
		}
		return done;
	};
	
	/**
	 * 设置页面点击事件处理
	 * event：事件对象
	 * 若pageNumber参数为-1，而设置当前页不处理操作
	 */
	bPage.prototype.pageSwitch = function(pageNumber){
		var self = this, p = this.p;
		if(typeof(pageNumer)==undefined && typeof(pageNumber)!='number') pageNumber = self.pageNumber;
		if(pageNumber == -1) return;
		if(pageNumber > self.totalPage) pageNumber = self.totalPage;
		//异步刷新页面模式
		if(p.asyncLoad){
			self.pageNumber = pageNumber;
			var param = self.getParams(pageNumber);
			var async = true;
			if(p.async !== undefined && !p.async) async= p.async;
			if(p.serverSidePage){
				$.ajax({
					url : p.url,
					data : param,
					async : async,
					type : p.asyncType,
					dataType : 'text',//使用了html会导致数据读取后，不执行success内部的问题
					success : function(returnData){
						if(self.extractPageInfo(returnData)){
							if($(p.asyncTarget).size()>0) $(p.asyncTarget).empty().html(returnData);
							if(p.callback && $.isFunction(p.callback)) p.callback(param);
							self.populate();
						}
					}
				});
			}else{
				$.ajax({
					url : p.url,
					data : param,
					async : async,
					type : p.asyncType,
					dataType : 'json',
					success : function(returnData){
						self.pageNumber = returnData.pageNumber;
						self.pageSize = returnData.pageSize;
						self.totalRow = returnData.totalRow;
						if(p.render && $.isFunction(p.render)) p.render(returnData);
						if(p.callback && $.isFunction(p.callback)) p.callback(param);
						self.populate();
					}
				});
			}
			//请求数据时刷新页面前重新定位表头头部
			$(".tableBox .thead").css({
                "position":"absolute",
                "top":0,
                "z-index":2,
            });
		}else{
			//直接跳转模式
			window.location.href = self.setUrl(pageNumber);
		}
	};
	/**
	 * 设置具体页码跳转的URL
	 */
	bPage.prototype.setUrl = function(pNum){
		var p = this.p, str ='';
		if(p.url){
			var str = p.url + '?1=1';
			str += '&pageNumber=' + pNum;
			str += '&pageSize=' + this.pageSize;
			str += this.buildParamsStr(pNum);
		}else{
			str = 'javascript:void(0);';
		}

		return str;
	};
	/**
	 * 设置配置项
	 */
	bPage.prototype.setOption = function(params){
		
	}
	/**
	 * 插件初始化入口
	 */
	function Plugin(p){
		return this.each(function(){
			//参数合并时允许读取在html元素上定义的'data-'系列的参数
			var $this = $(this),
				data = $this.data('bPage'),
				params = $.extend({}, defaults, $this.data(), typeof p == 'object' && p);
			
			if(!data){
				$this.data('bPage', (data = new bPage(this,params)));
			}else{
				data.p = params;
			}
			if($.isPlainObject(params)) data.init();
		});
	}
	
	/**
	 * 切换当前页
	 * 
	 * @param pNum {number} 目标分页
	 */
	function bPageSwitch(pNum){
		return this.each(function(){
			if(!pNum || $.type(pNum) != 'number') return;
			var $this = $(this),data = $this.data('bPage');
			if(data) data.pageSwitch(pNum);
		});
	}
	
	/**
	 * 刷新新页栏
	 */
	function bPageRefresh(p){
		return this.each(function(){
			var $this = $(this),
				data = $this.data('bPage'),
				params = $.extend({}, defaults, $this.data(), data && data.p ,typeof p == 'object' && p);
			if($.isPlainObject(params)) data.p = params;
			data.pageNumber = params.pageNumber;
			data.pageSize = params.pageSize;
			data.totalRow = params.totalRow;
			if(data) data.pageSwitch();
		});
	}
	
	var old = $.fn.bPage;

	$.fn.bPage             = Plugin;
	$.fn.bPage.Constructor = bPage;
	$.fn.bPageSwitch       = bPageSwitch;
	$.fn.bPageRefresh      = bPageRefresh;
	
	// 处理新旧版本冲突
	// =================
	$.fn.bPage.noConflict = function () {
		$.fn.bPage = old;
		return this;
	};
}(window.jQuery);