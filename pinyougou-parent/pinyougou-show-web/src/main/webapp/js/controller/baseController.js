app.controller('baseController',function($scope){
	
	//分页控件配置
	$scope.paginationConf = {
		currentPage : 1,//当前页码
		totalItems : 10,//总记录数
		itemsPerPage : 10,//分页数
		perPageOptions : [ 10, 20, 30, 40, 50 ],//每页记录数
		onChange : function() {
			$scope.reloadList();
		}
	};
	
	//改造加载列表,去调用search方法,将 普通的页面展示 和 条件查询 结合一起使用
	$scope.reloadList=function(){
		$scope.search( $scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	//定义选中的 ID 集合
	$scope.selectIds = [];
	$scope.updateSelection = function($event,id) {
		if($event.target.checked){//如果是被选中,则增加到数组
			$scope.selectIds.push( id);
		}else{
			var idx = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(idx, 1);//删除
		}
	}
	
	//提取 json 字符串数据中某个属性，返回拼接字符串 逗号分隔
	$scope.jsonToString=function(jsonString,key){
		var json=JSON.parse(jsonString);//将 json 字符串转换为 json 对象
		var value="";
		for(var i=0;i<json.length;i++){
			if(i>0){
				value+=","
			}
			value+=json[i][key];
		}
		return value;
	}
	
	//在list集合中根据key查询对象
	$scope.searchObjectByKey=function(list,key,keyValue){
		//循环集合
		for(var i=0;i<list.length;i++){
			//如果某个key所对应的keyValue相等,则返回这个key-keyValue
			if(list[i][key]==keyValue){
				return list[i];
			}
		}
		//循环结束后,说明不存在,直接返回null即可
		return null;
	}
	
	
})