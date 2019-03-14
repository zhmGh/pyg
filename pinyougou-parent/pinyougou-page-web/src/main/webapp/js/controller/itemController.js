//商品详细页（控制层）
app.controller('itemController',function($scope){
	//数量操作
	$scope.addNum=function(x){
		<!-- 不能转换 -->
		y=parseInt(x);
		$scope.num=$scope.num+y;
		if($scope.num<1){
			$scope.num=1;
		}
	}	

	$scope.specificationItems={};//记录用户选择的规格
	//用户选择规格
	$scope.selectSpecification=function(name,value){	
		$scope.specificationItems[name]=value;
		searchSku();//读取sku
	}	
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}		
	}
	
	//加载默认SKU,为了满足页面第一次打开时,加载商品信息
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		//采用深克隆技术,避免商品信息不能使用
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	
	//匹配两个对象
	matchObject=function(map1,map2){		
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}			
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}			
		}
		return true;		
	}
	
	//查询SKU
	searchSku=function(){
		//当前各种组合进行匹配
		for(var i=0;i<skuList.length;i++ ){
			if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
				$scope.sku=skuList[i];
				return ;
			}			
		}
		//当所有组合都没有时,如果没有匹配的	
		$scope.sku={id:0,title:'--------',price:0};
	}
	
	//加入购物车
	$scope.addToCart=function(){
		alert('SKUID:'+$scope.sku.id);
	}
	
	
});