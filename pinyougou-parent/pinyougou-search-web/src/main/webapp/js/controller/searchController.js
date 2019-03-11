app.controller('searchController',function($scope,searchService){	
	
	//为了满足搜索条件构建,故创建一个集合专门存放搜索条件
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}}
	
	//搜索
	$scope.search=function(){
		searchService.search( $scope.searchMap ).success(
			function(response){	
				//alert(response);
				$scope.resultMap=response;//搜索返回的结果
			}
		);	
	}
	
	//在页面添加搜索选项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key == 'brand'){
			//给选项添加分类或者是品牌
			$scope.searchMap[key]=value;
		}else{
			//规格或是其他的属性
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索 
		
	}
	
	//移除复合搜索条件
	$scope.removeSearchItem=function(key){
		if(key=="category" ||  key=="brand"){//如果是分类或品牌
			$scope.searchMap[key]="";		
		}else{//否则是规格
			delete $scope.searchMap.spec[key];//移除此属性
		}
		$scope.search();//执行搜索 
	}
	
});