app.controller('brandController', function($scope,brandService,$controller,brandService) {

	$controller('baseController',{$scope:$scope});
	
	//查询所有
	$scope.findAll = function() {
		brandService.findAll().success(function(brandList) {
			$scope.list = brandList;
		});
	}

	//重新加载列表 数据
	/* $scope.reloadList = function() {
		//切换页码 
		$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}  */

	//分页
	$scope.findPage = function(page, rows) {
		brandService.findPage(page,rows).success(
			function(response) {
				$scope.list = response.rows;
				//更新总记录数
				$scope.paginationConf.totalItems = response.total;
			});
	} 

	//增加品牌保存 或 修改品牌保存
	$scope.save = function() {
		var object = null;
		if ($scope.entity.id != null) {
			object = brandService.update($scope.entity);
		} else {
			object = brandService.add($scope.entity);
		}
		object.success(
			function(response) {
				if (response.success) {
					$scope.reloadList();
				} else {
					alert(response.message);
				}
			});
	}

	//查找当前修改品牌ID
	$scope.findOne = function(id) {
		brandService.findOne(id).success(
			function(response) {
				$scope.entity = response;
			})
	}

	
	
	//删除选中
	$scope.dele = function(){
		brandService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					//更新列表
					$scope.reloadList();
				}
			}
		)
	}
	
	//改造查询,加入实体类参数(可有可无),完成普通查询与条件查询的结合
	//初始化搜索对象
	$scope.searchEntity={};
	//条件查询
	$scope.search=function(page,rows){
		brandService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.paginationConf.totalItems=response.total;//总记录数
				$scope.list=response.rows;//给列表变量赋值
			}
		);
	}

});