﻿<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
	<!-- sidebar -->
	<div id="sidebar-nav">
		<ul id="dashboard-menu">
			<li class="${indexSwitch}">
				<a href="./toindex"> <i class="icon-home"></i> <span>首页</span></a>
			</li >
			<li class="${reportSwitch}">
			   <a href="./totransactionlist"> 
			     <i class="icon-signal"></i><span>订单管理</span>
			   </a>
			</li>
			<li class="${userSwitch}">
			    <a href="./tostafflist"> 
			    <i class="icon-star-half-full"></i><span>员工管理</span>
			    </a>
		    </li>
		    
		    <li class="${wechatuserSwitch}">
			   <a href="./towechatusers"> <i class="icon-picture"></i>
					<span>微信用户</span>
			   </a>
			</li>
			<li class="${productSwitch}">
			    <a class="dropdown-toggle" href="#"> <i
					class="icon-edit"></i> <span>商品</span> <i class="icon-chevron-down"></i>
			    </a>
				<ul class="${productSwitch} submenu">
					<li><a class="${productSwitch_all}" href="./toproductlist">全部商品</a></li>
					<li><a class="${productSwitch_hot}" href="form-wizard.html">热门商品</a></li>
					<li><a class="${productSwitch_add}" href="./toaddproduct">添加商品</a></li>
				</ul>
		     </li>
			<li class="${infoSwitch}">
			    <a href="./tomchuserinfo"> <i class="icon-cog"></i> <span>个人信息</span>	</a>
			</li>
			<li class="${suggestSwitch}">
			    <a href="./tosuggest"> <i class="icon-calendar-empty"></i>
					<span>查看意见</span>
		    	</a>
		    </li>
		</ul>
	</div>
	
	<!-- end sidebar -->