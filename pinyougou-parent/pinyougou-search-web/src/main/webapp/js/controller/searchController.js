app.controller('searchController',function($scope,$location,searchService){	
	
	//为了满足搜索条件构建,故创建一个集合专门存放搜索条件
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':''}
	
	//搜索
	$scope.search=function(){
		//将前台手动数字页码查询得到的是String,要转换为数字到后台去
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search( $scope.searchMap ).success(
			function(response){	
				//alert(response);
				$scope.resultMap=response;//搜索返回的结果
				//调用分页的方法
				buildPageLabel();
			}
		);	
	}
	
	//在页面添加搜索选项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key == 'brand' || key == 'price'){
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
		if(key=="category" ||  key=="brand" || key == 'price'){//如果是分类或品牌
			$scope.searchMap[key]="";		
		}else{//否则是规格
			delete $scope.searchMap.spec[key];//移除此属性
		}
		$scope.search();//执行搜索 
	}
	
	//对页面上分页显示进行改造
	buildPageLabel=function(){
		//定义一个产生页码的数组,
		$scope.pageLabel=[];
		
		//当前页码,我们默认为1
		var firstPage = 1;
		//获取页码数,定义为最后一页(从后台当中获取)
		var lastPage = $scope.resultMap.totalPages;
		
		$scope.firstDot=false;//前面无点
		$scope.lastDot=false;//后边无点	
		
		//要实现的需求为每页可以点击5页,当前页码大于5页,则显示5页可选
		
		//总页码数大于7时,对当前页进行判断
		if($scope.resultMap.totalPages>5){
			if($scope.searchMap.pageNo<=3){
				//如果当前页小于等于3  1 2 3 4 5 
				lastPage = 5;
				$scope.lastDot=true;//后边有点	
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){
				//如果当前页大于等于最大页码-2   94 95 96 97 98 99 100
				firstPage = $scope.resultMap.totalPages-4;//最大页码数是返回
				$scope.firstDot=true;//前面有点
			}else{
				
				firstPage = $scope.searchMap.pageNo-2;
				lastPage = $scope.searchMap.pageNo+2;
				$scope.firstDot=true;//前面有点
				$scope.lastDot=true;//后边有点
			}
		}
		
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		
		//排除非法的页码查询,小于第1页大于最大页码数
		if(pageNo<1 || pageNo >$scope.resultMap.totalPages){
			return;
		}
		//查询的页码的当前页,用于去后台查询
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	//判断当前页为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否未最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	
	//设置排序规则
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;	
		$scope.searchMap.sort=sort;	
		$scope.search();
	}
	
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			//循环品牌列表里的所有品牌与查询的keywords进行比较
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果包含
				return true;
			}			
		}		
		return false;
	}
	
	$scope.loadkeywords=function(){
		//接受从其他页面携带的数据
		$scope.searchMap.keywords=$location.search()['keywords'];
		//再次调用方法
		$scope.search();
	}
	
	
	
});