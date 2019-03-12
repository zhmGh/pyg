app.controller('contentController',function($scope,contentService){
	
	//定义一个广告集合
	$scope.contentList=[];	
	//查询广告的列表
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId]=response;
			}
		);		
	}
	
	$scope.search=function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
	
	
})
