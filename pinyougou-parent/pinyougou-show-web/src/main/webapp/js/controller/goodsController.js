 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//增加商品
	$scope.add=function(){
		$scope.entity.goodsDesc.introduction=editor.html();
		
		goodsService.add($scope.entity).success(
			function(response){
				if(response.success){
					//户用提示成功添加
					alert("新增成功")
					//添加成功后把所有填写的内容(除了富文本编辑器)清空
					$scope.entity={};
					//清空富文本编辑器内容
					editor.html('');//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	
	//上传图片
	$scope.uploadFile=function(){ 
		uploadService.uploadFile().success(function(response) { 
			if(response.success){//如果上传成功，取出 url
				$scope.image_entity.url=response.message;//设置文件地址
			}else{
				alert(response.message);
			}
	 	}).error(function() {
		 	alert("上传发生错误");
	 	}); 
	 }; 
	 
	 //为了清空三级删除的遗留问题
	 $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	 //页面内容初始化
	 //$scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};
	 
	 //添加图片列表
	 $scope.add_image_entity=function(){ 
		 $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	 }
	 
	//列表中移除图片
	 $scope.remove_image_entity=function(index){
		 $scope.entity.goodsDesc.itemImages.splice(index,1);
	 }
	 
	 //初始化一级联动
	 $scope.selectItemCat1List=function(){
		 itemCatService.findByParentId(0).success(
			 function(response){
				 $scope.itemCat1List = response;
			 }
		 )
	 }
	 
	 //一二级联动
	 $scope.$watch('entity.goods.category1Id',function(newValue,oleValue){
		 itemCatService.findByParentId(newValue).success(
			 function(response){
				 $scope.itemCat2List = response;
			 }
		 )
	 })
	 
	 //二三级联动
	 $scope.$watch('entity.goods.category2Id',function(newValue,oleValue){
		 itemCatService.findByParentId(newValue).success(
			 function(response){
				 $scope.itemCat3List = response;
			 }
		 )
	 })
	 
	 //三级与 模板ID 显现联动
	 $scope.$watch('entity.goods.category3Id',function(newValue,oleValue){
		 itemCatService.findOne(newValue).success(
			 function(response){
				 $scope.entity.goods.typeTemplateId = response.typeId;
			 }
		 )
	 })
	 
	 //监控模板ID,实现品牌列表,实现扩展列表,实现显示列表
	 $scope.$watch('entity.goods.typeTemplateId',function(newValue,oleValue){
		 //监听并得到模板ID
		 typeTemplateService.findOne(newValue).success(
			 function(response){
				 $scope.typeTemplate = response;
				 //获得品牌列表
				 $scope.typeTemplate.brandIds= JSON.parse($scope.typeTemplate.brandIds);
				 //获取扩展属性custom_attribute_items,放入goodsDesc表中 
				 $scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.typeTemplate.customAttributeItems);
			 }
		 )
		 //传入模板的ID,去查询得到规格列表,返回到页面进行规格展示
		 typeTemplateService.findSpecList(newValue).success(
			 function(response){
				 //注意,这里得到是map的集合 [{"id":27,"text":"网络",options:[{},{}]},{"id":32,"text":"机身内存"},options:[{},{}]]
				 $scope.specList = response;
			 }
		 )
	 })
	 
	 //勾选规格,将规格进行组合更新展示列表,前台传入参数规格(网络+内存)
	 $scope.updateSpecAttribute=function($event,name,value){
		 //将参数传入方法得到该添加的规格
		 //object指的就是[{“attributeName”:”网络”,”attributeValue”:[“移动”,“联通”]}]  name指的就是网络
		 var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name)
		 
		 //将obect是查询到的集合
		 if(object != null){
			 //[{“attributeName”:”网络”,”attributeValue”:[“移动”]}]--->[{“attributeName”:”网络”,”attributeValue”:[“移动”,“联通”]}]
			 
			 if($event.target.checked){
				 //被选中状态
				 object.attributeValue.push(value);
			 }else{
				 //未选中状态,则删除一个attributeValue内容
				 object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				 
				 if(object.attributeValue.length==0){
					//当全部取消后, [{“attributeName”:”网络”,”attributeValue”:[]}],这样没意义,所以直接删除全部
					 $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				 }
			 }
		 }else{
			 //[]--->[{“attributeName”:”网络”,”attributeValue”:[“移动”]}]
			 $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		 }
	 }
	 
	//创建 SKU 列表
	 $scope.createItemList=function(){
		 debugger;
		//初始化展示的规格模板
		 $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];
		 //得到规格选项的集合
		 var items= $scope.entity.goodsDesc.specificationItems;
		 //分析情况一,集合长度是1:[{“attributeName”:”网络”,”attributeValue”:[“移动”,“联通”]}]
		 //分析情况二,集合长度>1:[{“attributeName”:”网络”,”attributeValue”:[“移动”]},{“attributeName”:”内存”,”attributeValue”:[“8G”]}]
		 //分析情况三,集合长度>1:[{“attributeName”:”网络”,”attributeValue”:[“移动”,“联通”]},{“attributeName”:”内存”,”attributeValue”:[“8G”,“16G”]}]
		 for(var i=0;i< items.length;i++){
			 $scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue ); 
		 }
	 }
	 
	 //添加列值
	 addColumn=function(list,columnName,conlumnValues){
		 debugger;
		 //新的集合,该集合最后就是展示规格模板列表的集合
		 var newList=[];
		 for(var i=0;i<list.length;i++){
			 //此循环是对规格个数进行循环
			 var oldRow= list[i];
			 for(var j=0;j<conlumnValues.length;j++){
				 //此循环是对规格内容进行循环,如网络(3G,4G,5G)
				 	var newRow= JSON.parse( JSON.stringify( oldRow ) );//深克隆
				 	newRow.spec[columnName]=conlumnValues[j];
				 	newList.push(newRow);
			 } 
		 } 
		 return newList;
	 }
	 
	 
	 
});	
