//购物车控制层 
app.controller('cartController',function($scope,cartService){
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				$scope.totalValue=cartService.sum($scope.cartList);//求合计数
			}
		);		
	}
	
	//添加商品到购物车
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){
					$scope.findCartList();//刷新列表
				}else{
					alert(response.message);//弹出错误提示
				}				
			}
		);
	}
	
	//获取地址列表
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
			function(response){
				$scope.addressList=response;
				//设置第一次地址选中为红色
				for(var i=0;$scope.addressList.length;i++){
					if($scope.addressList[i].isDefault=='1'){
						$scope.address=$scope.addressList[i];
						break;
					}
				}
			}
		)
	}
	
	//需求:选择地址时,加上红色选中标记
	//标记当前选中地址
	$scope.selectAddress=function(address){
		$scope.address=address;
	}
	$scope.isSeletedAddress=function(address){
		//比较的是深克隆的地址值
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	//默认支付方式是微信支付
	$scope.order={paymentType:'1'};
	//选择支付方式
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	
	//保存订单
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		cartService.submitOrder( $scope.order ).success(
			function(response){
				if(response.success){
					//页面跳转
					if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
						location.href="pay.html";
					}else{//如果货到付款，跳转到提示页面
						location.href="paysuccess.html";
					}					
				}else{
					alert(response.message);	//也可以跳转到提示页面				
				}				
			}				
		);		
	}
	
});