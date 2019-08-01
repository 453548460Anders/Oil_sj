// Dom7
var $$ = Dom7;

$$.url = 'http://erp.hubangsy.com/plug/js/appApi.ashx?';
window.url = 'http:erp.hubangsy.com/plug/js/appApi.ashx?';
window.imageUrl = 'http://lh.ch000.com';

Framework7.request.setup({
	headers: {
		'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
	}
})

// Framework7 App main instance
var app = new Framework7({
	root: '#app', // App root element
	id: 'io.dcloud.H58015AA2', // App bundle ID
	name: '东北中油司机版', // App name
	theme: 'auto', // Automatic theme detection
	// App root data
	data: function() {
		return {};
	},
	// App root methods
	methods: {},
	// App routes
	routes: routes,
	swipeout: {
		noFollow: true,
		removeElements: false
	},
	pushState: true
});

// Init/Create views
var homeView = app.views.create('#view-hall', {
	url: '/',
});

var orderView = app.views.create('#view-order', {
	url: '/order/',
});

var serviceView = app.views.create('#view-service', {
	url: '/service/',
});

var meView = app.views.create('#view-me', {
	url: '/me/',
});

var loginSuccess = function(data) {
	app.preloader.hide();
	var result = JSON.parse(data);
	console.error('登录成功', result);
	if(result.errcode === '00000') {
		var telphone = localStorage.getItem('username') || $$('#login-input')[0].value.trim();
		var password = localStorage.getItem('password') || $$('#login-input-password')[0].value.trim();
		var gmobile = localStorage.getItem('gmobile') || $$('#login-input-gmoblie')[0].value.trim();

		localStorage.setItem('username', telphone);
		localStorage.setItem('password', password);
		localStorage.setItem('gmobile', gmobile);

		window.userid = result.info.id;
		window.name = result.info.name;
		window.telphone = result.info.mobile;
		window.plate = result.info.plate;
		window.weight = result.info.weight;
		window.sscd = result.info.sscd;
		window.guahao = result.info.guahao;
		window.idcard = result.info.idcard;
		setTimeout(function() {
			$$(document).trigger('hallReady');
			$$(document).trigger('meReady');
			$$(document).trigger('orderReady');
			$$(document).trigger('serviceReady');
			$$('#service-user-name')[0] ? $$('#service-user-name')[0].innerHTML = window.name : '';
			$$('#service-user-telphone')[0] ? $$('#service-user-telphone')[0].innerHTML = window.telphone : '';
			$$('#me-chepaihao-id')[0] ? $$('#me-chepaihao-id')[0].innerHTML = window.plate : '';
			$$('#me-guahao-id')[0] ? $$('#me-guahao-id')[0].innerHTML = window.guahao : '';
			$$('#me-name-id')[0] ? $$('#me-name-id')[0].innerHTML = window.name : '';
			$$('#me-idcard-id')[0] ? $$('#me-idcard-id')[0].innerHTML = window.idcard : '';
			$$('#me-telphone-id')[0] ? $$('#me-telphone-id')[0].innerHTML = window.telphone : '';
			$$('#me-weight-id')[0] ? $$('#me-weight-id')[0].innerHTML = window.weight : '';
			$$('#me-chedui-id')[0] ? $$('#me-chedui-id')[0].innerHTML = window.sscd : '';
		}, 500);
		app.loginScreen.close('#my-login-screen');
	} else {
		app.loginScreen.open('#my-login-screen');
		var toastTop = app.toast.create({
			text: result.result,
			position: 'center',
			closeTimeout: 2000,
		});
		toastTop.open();
	}
}

var loginFailed = function() {
	console.error('登录失败');
	app.preloader.hide();
	this.$router.navigate('/', {
		refreshAll: true
	});
	app.loginScreen.open('#my-login-screen');
}

// Login Screen 
var loginFn = function() {
	console.error('自动登录');
	var username = localStorage.getItem('username');
	var password = localStorage.getItem('password');
		var gmobile = localStorage.getItem('gmobile');

	if(!username || !password || !gmobile) {
		app.loginScreen.open('#my-login-screen');
		return;
	}
	app.preloader.show();
	app.request.post(window.url + 'type=driverdl', {
		mobile: username,
		pass: password,
		gmobile: gmobile,
		token: "//todo",
	}, loginSuccess, loginFailed);
}
// 自动登录
loginFn();
var me = this;
$$('.login-btn').on('click', function() {
	if(me.$$('.toolbar') && me.$$('.toolbar').length > 0) {
		me.$$('.toolbar').removeClass('hidden');
	}
	var telphone = $$('#login-input')[0].value.trim();
	var password = $$('#login-input-password')[0].value.trim();
	var gmobile = $$('#login-input-gmoblie')[0].value.trim();
	if(!telphone) {
		var toastTop = app.toast.create({
			text: '请输入车牌号!',
			position: 'center',
			closeTimeout: 2000,
		});
		toastTop.open();
		return;
	}
	if(!gmobile) {
    		var toastTop = app.toast.create({
    			text: '请输入手机号',
    			position: 'center',
    			closeTimeout: 2000,
    		});
    		toastTop.open();
    		return;
        }
	if(!password) {
		var toastTop = app.toast.create({
			text: '请输入密码!',
			position: 'center',
			closeTimeout: 2000,
		});
		toastTop.open();
		return;
	}
	console.error('手动登录');
	app.request.post(window.url + 'type=driverdl', {
		mobile: telphone,
		pass: password,
		gmobile: gmobile,
		token: "//todo",
	}, loginSuccess, loginFailed);
});

// 监听注册按钮
$$('#gotoRegister').on('click', function() {
	app.loginScreen.close('#my-login-screen');
	app.router.navigate('/login_register_1/', {
		animate: false,
		ignoreCache: true,
	});
});

// 忘记密码
$$('#forgotPassword').on('click', function() {
	app.loginScreen.close('#my-login-screen');
	app.router.navigate('/forgot-password/', {
		animate: false,
		ignoreCache: true,
	});
});

if(typeof Object.assign != 'function') {
	Object.assign = function(target) {
		'use strict';
		if(target == null) {
			throw new TypeError('Cannot convert undefined or null to object');
		}
		target = Object(target);
		for(var index = 1; index < arguments.length; index++) {
			var source = arguments[index];
			if(source != null) {
				for(var key in source) {
					if(Object.prototype.hasOwnProperty.call(source, key)) {
						target[key] = source[key];
					}
				}
			}
		}
		return target;
	};
}

app.$('#tab-view-order').on('click', function() {
	$$(document).trigger('orderOpened');
});

app.$('#tab-view-hall').on('click', function() {
	$$(document).trigger('hallReady');
});

var first = null;
mui.back = function() {
	//首次按键，提示‘再按一次退出应用’
	if(!first) {
		first = new Date().getTime();
		mui.toast('再按一次退出应用');
		setTimeout(function() {
			first = null;
		}, 1000);
	} else {
		if(new Date().getTime() - first < 1000) {
			plus.runtime.quit();
		}
	}
};