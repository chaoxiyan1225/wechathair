(function(e){function t(t){for(var r,o,c=t[0],s=t[1],u=t[2],d=0,l=[];d<c.length;d++)o=c[d],i[o]&&l.push(i[o][0]),i[o]=0;for(r in s)Object.prototype.hasOwnProperty.call(s,r)&&(e[r]=s[r]);f&&f(t);while(l.length)l.shift()();return a.push.apply(a,u||[]),n()}function n(){for(var e,t=0;t<a.length;t++){for(var n=a[t],r=!0,o=1;o<n.length;o++){var c=n[o];0!==i[c]&&(r=!1)}r&&(a.splice(t--,1),e=s(s.s=n[0]))}return e}var r={},o={app:0},i={app:0},a=[];function c(e){return s.p+"js/"+({error:"error",home:"home"}[e]||e)+"."+{error:"08dd463f",home:"e587a0b4"}[e]+".js"}function s(t){if(r[t])return r[t].exports;var n=r[t]={i:t,l:!1,exports:{}};return e[t].call(n.exports,n,n.exports,s),n.l=!0,n.exports}s.e=function(e){var t=[],n={error:1,home:1};o[e]?t.push(o[e]):0!==o[e]&&n[e]&&t.push(o[e]=new Promise(function(t,n){for(var r="css/"+({error:"error",home:"home"}[e]||e)+"."+{error:"25842c7a",home:"5fa56f2b"}[e]+".css",i=s.p+r,a=document.getElementsByTagName("link"),c=0;c<a.length;c++){var u=a[c],d=u.getAttribute("data-href")||u.getAttribute("href");if("stylesheet"===u.rel&&(d===r||d===i))return t()}var l=document.getElementsByTagName("style");for(c=0;c<l.length;c++){u=l[c],d=u.getAttribute("data-href");if(d===r||d===i)return t()}var f=document.createElement("link");f.rel="stylesheet",f.type="text/css",f.onload=t,f.onerror=function(t){var r=t&&t.target&&t.target.src||i,a=new Error("Loading CSS chunk "+e+" failed.\n("+r+")");a.code="CSS_CHUNK_LOAD_FAILED",a.request=r,delete o[e],f.parentNode.removeChild(f),n(a)},f.href=i;var p=document.getElementsByTagName("head")[0];p.appendChild(f)}).then(function(){o[e]=0}));var r=i[e];if(0!==r)if(r)t.push(r[2]);else{var a=new Promise(function(t,n){r=i[e]=[t,n]});t.push(r[2]=a);var u,d=document.createElement("script");d.charset="utf-8",d.timeout=120,s.nc&&d.setAttribute("nonce",s.nc),d.src=c(e),u=function(t){d.onerror=d.onload=null,clearTimeout(l);var n=i[e];if(0!==n){if(n){var r=t&&("load"===t.type?"missing":t.type),o=t&&t.target&&t.target.src,a=new Error("Loading chunk "+e+" failed.\n("+r+": "+o+")");a.type=r,a.request=o,n[1](a)}i[e]=void 0}};var l=setTimeout(function(){u({type:"timeout",target:d})},12e4);d.onerror=d.onload=u,document.head.appendChild(d)}return Promise.all(t)},s.m=e,s.c=r,s.d=function(e,t,n){s.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:n})},s.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},s.t=function(e,t){if(1&t&&(e=s(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var n=Object.create(null);if(s.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var r in e)s.d(n,r,function(t){return e[t]}.bind(null,r));return n},s.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return s.d(t,"a",t),t},s.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},s.p="",s.oe=function(e){throw console.error(e),e};var u=window["webpackJsonp"]=window["webpackJsonp"]||[],d=u.push.bind(u);u.push=t,u=u.slice();for(var l=0;l<u.length;l++)t(u[l]);var f=d;a.push([0,"chunk-vendors"]),n()})({0:function(e,t,n){e.exports=n("56d7")},1:function(e,t){},"56d7":function(e,t,n){"use strict";n.r(t);n("7f7f"),n("cadf"),n("551c"),n("f751"),n("097d");var r=n("a026"),o=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{staticClass:"app",class:{p64:e.isIphone,is_X:e.isX,p0:e.isWx}},[n("transition",{attrs:{name:"router-fade",mode:"out-in"}},[n("keep-alive",[e.$route.meta.keepAlive?n("router-view"):e._e()],1)],1),n("transition",{attrs:{name:"router-fade",mode:"out-in"}},[e.$route.meta.keepAlive?e._e():n("router-view")],1)],1)},i=[],a=(n("28a5"),n("4917"),n("96cf"),n("3b8d")),c=n("cebc"),s=n("2f62"),u=n("bc3a"),d=n.n(u),l=(n("6b54"),n("ac6a"),n("b54a"),n("a481"),n("386d"),n("3b2b"),n("720d"),function(e,t){e&&("string"!==typeof t&&(t=JSON.stringify(t)),window.localStorage.setItem(e,t))}),f=function(e){if(e)return window.localStorage.getItem(e)},p=function(){var e=navigator.userAgent,t=!!e.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/),n=e.indexOf("iPhone")>-1,r=e.indexOf("iPad")>-1,o=e.indexOf("iOS")>-1,i=!!e.match(/AppleWebKit.*Mobile.*/);return 1==o||(1==t&&1==n&&1==i||1==t&&1==r&&1==i)},h=function(e){e=encodeURI(e);var t=new JSEncrypt;t.setPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDgKbCVrpn3KufsVht0hH2vW//wLh6HPw7gOH9WWDj7FE2FaWSSzWBp9kq1U1xR73YDng6/TQHLi1jQQNOXUT4irh/Z+WPEVXenmPyjuLPC1TQieTE7n0x/7+I6hPMln/wgjJNRHTU+iQN8BwrCpF2LRkqNEXaToDAV/THlnRuHbwIDAQAB");var n=t.encryptLong(e);return n},g=function(e){var t=new JSEncrypt;t.setPrivateKey("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJloEhJLOBJkwqirSAVgJCggcXEqqZXJiafo1E7Fd0+gCU3wsfUCwAYARTxu7EcEy0ZqQ+QGGQ4kjLXWwrE5dh1ha2VK0djI6JCHFes98l9deOVmLJ+7UtAy6YIE7v/aopwxGOHjaOOWgTpvD9aFrNMFk7slsdmyiobmed9/pbi9AgMBAAECgYAyZO6y1cTovfL4Z7P0GB4olGfPzj0BeMBgyWqdKd9E6ATGdtBu9WVRU1trO+Egf2GrkxMUYDBw5e7XeWJhx8eV4m0NDQ4KmgzNI7dkcBBdH0MaPUNGnp4yD0JD4n0x90xczyPEsmuoAcIqeb4eAz2LsvOjcFBYwtQFKdJ+244OAQJBANqDY6vCxzcs/GtEmDcMnf4wx6LoCCR1Td0noz1nBF9Grky2JkJx9prLRWrbxuR71pe03BoXttXAbbeYvCePAj0CQQCzuVcy+yS8gvOfVkZqyKHakHRraVw+L1KWZIA5wSwfMEOz+5Gv602dr+PSve1wX1ZwCukhIJMA+eZpIx/rPXiBAkB9f8jW9V+FiQjQcu0ZcN2tCEcggTNr/RNjWUigE3KnZejllhgTQXDpJqYP4Xox8A4nw3BGbChOS963+7jU34XhAkEAoXfi3+rDFpqQRpc8rLOUtskRz27ZcQUTLgsdKMZk3aB/MzTRrzu4f/OGu8/ngw4kmoUy7pBVgosCd3MXOkIAAQJBAIJL7fJLtxzBIuhjj+7NFUUoxFKCRjfRNsfnCktnN4QfLAJCrTPiyi3eXok/1qdyRPcHZl1qPgIhiM7zGXFxNG8=");var n=t.decryptLong2(e);return decodeURIComponent(n)};JSEncrypt.prototype.encryptLong=function(e){var t=this.key,n=(t.n.bitLength()+7>>3)-11;try{var r="",o="";if(e.length>n)return r=e.match(/.{1,117}/g),r.forEach(function(e){var n=t.encrypt(e);o+=n}),v(o);var i=t.encrypt(e),a=v(i);return a}catch(c){return console.log("加密失败"),!1}},JSEncrypt.prototype.decryptLong2=function(e){var t=this.getKey(),n=128;try{var r,o,i="",a=[],c=y(e),s=I(c),u=s.length,d=0,l=n;while(u-d>0)u-d>n?(o=s.slice(d,l),a=b(o),r=t.decrypt(a),i+=r):(o=s.slice(d,u),a=b(o),r=t.decrypt(a),i+=r),d+=n,l+=n;return i}catch(f){return console.log("解密失败"),!1}};var m="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",w="=";function v(e){var t,n,r="";for(t=0;t+3<=e.length;t+=3)n=parseInt(e.substring(t,t+3),16),r+=m.charAt(n>>6)+m.charAt(63&n);if(t+1==e.length?(n=parseInt(e.substring(t,t+1),16),r+=m.charAt(n<<2)):t+2==e.length&&(n=parseInt(e.substring(t,t+2),16),r+=m.charAt(n>>2)+m.charAt((3&n)<<4)),w)while((3&r.length)>0)r+=w;return r}function y(e){var t,n,r,o="",i=0;for(t=0;t<e.length;++t){if(e.charAt(t)==w)break;r=m.indexOf(e.charAt(t)),r<0||(0==i?(o+=P(r>>2),n=3&r,i=1):1==i?(o+=P(n<<2|r>>4),n=15&r,i=2):2==i?(o+=P(n),o+=P(r>>2),n=3&r,i=3):(o+=P(n<<2|r>>4),o+=P(15&r),i=0))}return 1==i&&(o+=P(n<<2)),o}function I(e){for(var t=[],n=0;n<e.length;n+=2)t.push(parseInt(e.substr(n,2),16));return t}function b(e){for(var t=[],n=0;n<e.length;n++)t.push((e[n]>>>4).toString(16)),t.push((15&e[n]).toString(16));return t.join("")}var A="0123456789abcdefghijklmnopqrstuvwxyz";function P(e){return A.charAt(e)}var x="https://www.lifecrystal.cn/lifecrystal-server/",C=x;var E=function(e){return d()({method:"post",url:C+"/ticket/getTicket.do",encryptParam:{domainUrl:e}})},k=function(e){return d()({method:"post",url:C+"/user/thirdPartLogin.do",encryptParam:{code:e}})},S={data:function(){return{deviceId:null,urlCode:"",isX:!1}},computed:Object(c["a"])({},Object(s["b"])(["isIphone","isWx","isImprove"])),created:function(){var e=localStorage.getItem("gameUserInfo");e&&this.$store.commit("recordUserInfo",e),p()&&this.$store.commit("isIphoneChange",!0);var t=/iphone/gi.test(window.navigator.userAgent)&&window.devicePixelRatio&&3===window.devicePixelRatio&&375===window.screen.width&&812===window.screen.height,n=/iphone/gi.test(window.navigator.userAgent)&&window.devicePixelRatio&&3===window.devicePixelRatio&&414===window.screen.width&&896===window.screen.height,r=/iphone/gi.test(window.navigator.userAgent)&&window.devicePixelRatio&&2===window.devicePixelRatio&&414===window.screen.width&&896===window.screen.height;(t||n||r)&&(this.isX=!0,this.$store.commit("isIphoneXChange",!0))},mounted:function(){this.wxConfig()},methods:{thirdPartLogin:function(){var e=Object(a["a"])(regeneratorRuntime.mark(function e(){var t,n;return regeneratorRuntime.wrap(function(e){while(1)switch(e.prev=e.next){case 0:return t=f("code"),e.next=3,k(t);case 3:n=e.sent,200==n.decodeData.code&&(l("gameUserInfo",n.decodeData.result),l("gameUserId",n.decodeData.result.userId)),l("code","");case 6:case"end":return e.stop()}},e)}));function t(){return e.apply(this,arguments)}return t}(),wxConfig:function(){var e=Object(a["a"])(regeneratorRuntime.mark(function e(){var t,n,r;return regeneratorRuntime.wrap(function(e){while(1)switch(e.prev=e.next){case 0:if(t=window.navigator.userAgent.toLowerCase(),"micromessenger"!=t.match(/MicroMessenger/i)){e.next=8;break}return this.$store.commit("isWXChange",!0),n=window.location.href.split("#")[0],e.next=6,E(n);case 6:r=e.sent,wx.config({debug:!1,appId:r.decodeData.result.appId,timestamp:r.decodeData.result.timestamp,nonceStr:r.decodeData.result.nonceStr,signature:r.decodeData.result.signature,jsApiList:["checkJsApi","onMenuShareTimeline","onMenuShareAppMessage","onMenuShareQQ","onMenuShareWeibo","onMenuShareQZone","hideMenuItems","showMenuItems","hideAllNonBaseMenuItem","showAllNonBaseMenuItem","translateVoice","startRecord","stopRecord","onVoiceRecordEnd","playVoice","onVoicePlayEnd","pauseVoice","stopVoice","uploadVoice","downloadVoice","chooseImage","previewImage","uploadImage","downloadImage","getNetworkType","openLocation","getLocation","hideOptionMenu","showOptionMenu","closeWindow","scanQRCode","chooseWXPay","openProductSpecificView","addCard","chooseCard","openCard"]});case 8:case"end":return e.stop()}},e,this)}));function t(){return e.apply(this,arguments)}return t}()}},M=S,O=(n("5c0b"),n("2877")),D=Object(O["a"])(M,o,i,!1,null,null,null),R=D.exports,j=n("8c4f");r["default"].use(j["a"]);var L=new j["a"]({routes:[{path:"/",redirect:"/publicityPage"},{path:"/publicityPage",name:"publicityPage",component:function(){return n.e("home").then(n.bind(null,"a68b"))},meta:{name:"生命晶石"}},{path:"*",component:function(){return n.e("error").then(n.bind(null,"9ce4"))}}]});r["default"].use(s["a"]);var B={userId:null,userInfo:null,isAddress:!1,addressId:"",isOrder:!1,shopID:null,latitude:null,longitude:null,address:null,isIphone:!1,relationProductIds:[],isTagsChange:null,isWx:!1,isIphoneX:!1,isImprove:!0,isPortrait:!0,screenRotate:0,defaultConfig:{successCode:"200",txIMID:"1400238383",txIMSecret:"ef95fff16b4ae386f3dbaa0e2e91cc9040cfeb4c27d82bee6774bb3c7e25f4ee",imgDomain:"http://qnlifecrystalactivity.lifecrystal.cn/"}},J=new s["a"].Store({state:B,mutations:{setData:function(e,t){e[t.key]=t.value},recordUserInfo:function(e,t){e.userInfo=t},recordUserId:function(e,t){e.userId=t},recordShopID:function(e,t){e.shopID=t},recordLocation:function(e,t){e.latitude=t.latitude,e.longitude=t.longitude,e.address=t.address},isIphoneChange:function(e,t){e.isIphone=t},isIphoneXChange:function(e,t){e.isIphoneX=t},isWXChange:function(e,t){e.isWx=t},isImproveChange:function(e,t){e.isImprove=t},relationProductIdChange:function(e,t){e.relationProductIds=t},tagsChange:function(e,t){e.isTagsChange=t},recordProxyid:function(e,t){e.proxyid=t},outLogin:function(e){e.userId=null,e.shopID=null,e.addressId=null,e.userInfo=null,e.isAddress=!1}}}),Q=n("487a"),T=n.n(Q),U=n("d6d3"),X=n.n(U),N=n("4328"),W=n.n(N);n("a034");Date.prototype.format=function(e){var t={"M+":this.getMonth()+1,"d+":this.getDate(),"h+":this.getHours(),"m+":this.getMinutes(),"s+":this.getSeconds(),"q+":Math.floor((this.getMonth()+3)/3),S:this.getMilliseconds()};for(var n in/(y+)/.test(e)&&(e=e.replace(RegExp.$1,(this.getFullYear()+"").substr(4-RegExp.$1.length))),t)new RegExp("("+n+")").test(e)&&(e=e.replace(RegExp.$1,1==RegExp.$1.length?t[n]:("00"+t[n]).substr((""+t[n]).length)));return e};n("dfa4"),n("4b8a");var q=n("76a0");n("aa35");function V(){180!==window.orientation&&0!==window.orientation||J.commit("setData",{key:"isPortrait",value:!0}),90!==window.orientation&&-90!==window.orientation||J.commit("setData",{key:"isPortrait",value:!1}),J.commit("setData",{key:"screenRotate",value:window.orientation})}r["default"].component(q["Picker"].name,q["Picker"]),r["default"].component(q["Popup"].name,q["Popup"]),n("fda2"),n("451f"),r["default"].use(X.a),r["default"].use(T.a),V(),window.onorientationchange=function(e){console.log("旋转",e),V()},L.beforeEach(function(e,t,n){e.meta.name?(console.log("to",e.meta.name),document.title=e.meta.name):document.title="",n()}),d.a.interceptors.request.use(function(e){if(e.encryptParam){e.encryptParam.os="5",e.encryptParam.versionCode="V3.0",e.encryptParam.userId||(e.encryptParam.userId=f("gameUserId")),console.log(e.encryptParam);var t=h(JSON.stringify(e.encryptParam));e.data=W.a.stringify({requestParam:t,requestOs:3})}return e},function(e){return Promise.reject(e)}),d.a.interceptors.response.use(function(e){if("200"==e.status){g(e.data);return e.decodeData=e.data,console.log("解密结果",e),e}return e},function(e){return q["Indicator"].close(),Promise.reject(e)}),r["default"].config.productionTip=!1,new r["default"]({router:L,store:J,render:function(e){return e(R)}}).$mount("#app")},"5c0b":function(e,t,n){"use strict";var r=n("5e27"),o=n.n(r);o.a},"5e27":function(e,t,n){},a034:function(e,t){(function(e,t){var n=!1,r=e.documentElement,o="orientationchange"in window?"orientationchange":"resize",i=function(){var e=r.clientWidth>750?750:r.clientWidth;(e||n)&&(r.style.fontSize=e/375*50+"px",n=!0)};e.addEventListener&&(t.addEventListener(o,i,!1),e.addEventListener("DOMContentLoaded",i,!1))})(document,window)}});