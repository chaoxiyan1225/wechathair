package happylife.controller;

import happylife.model.TbActivity;
import happylife.model.TbMch;
import happylife.model.TbMchStaff;
import happylife.model.TbMchWechatRelation;
import happylife.model.TbProduct;
import happylife.model.TbSuggestion;
import happylife.model.TbTransactionRecord;
import happylife.model.TbWechatUser;
import happylife.model.servicemodel.CacheMchUser;
import happylife.model.servicemodel.CacheProduct;
import happylife.model.servicemodel.CacheRedeemCode;
import happylife.model.servicemodel.CacheTransaction;
import happylife.model.servicemodel.CacheWechatUser;
import happylife.model.servicemodel.MchServiceStatusEnum;
import happylife.model.servicemodel.TransactionReportBean;
import happylife.model.servicemodel.TransactionStatusEnum;
import happylife.service.MchUserService;
import happylife.service.exception.HappyLifeException;
import happylife.service.exception.RequestInvalidException;
import happylife.service.exception.SessionInvalidException;
import happylife.util.DesUtil;
import happylife.util.ExcelUtil;
import happylife.util.StrUtil;
import happylife.util.cache.MchStaffProductCacheManager;
import happylife.util.config.HttpRequestHeaderKey;
import happylife.util.config.IndexConfig;
import happylife.util.config.PageConfigUtil;
import happylife.util.config.WeChatConfig;
import happylife.util.config.WinterOrangeSysConf;
import happylife.util.requestandresponse.MenuSwitchShowManager;
import happylife.util.requestandresponse.ParseRequest;
import happylife.util.requestandresponse.ResponseToClient;
import happylife.util.requestandresponse.WeChatRequestUtil;
import happylife.util.requestandresponse.WechatMessageUtil;
import happylife.util.requestandresponse.messagebean.ResultMsgBean;
import happylife.util.service.ProductUtil;
import happylife.util.service.WechatUserUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 商家用户的controler类，商家用户查询自己的流水以及编辑自己的产品
 * 
 * @author 闫朝喜
 * 
 */
@SuppressWarnings("rawtypes")
@Controller
@RequestMapping(PageConfigUtil.MCH_PREFIX)
public class MchUserController {
	private static final Log logger = LogFactory
			.getLog(MchUserController.class);
	@Autowired
	private MchUserService mchUserService;

	/**
	 * 登陆
	 * 
	 * @param request
	 * @param map
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.LOGIN_PAGE)
	public void login(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		TbMch mchUserInfo = ParseRequest.generateMchUserLoginInfo(request);
		if (null == mchUserInfo) {
			// ResponseToUser.writeJsonMsg(response, new
			// ResultMsgBean(false,"登录信息错误"));
			logger.error("the mchuser info not right,redirect to login page.");

			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(false,
					"登录信息有误"));
			return;
		}

		// 确认登录信息成功 跳转到 兑换码界面
		if (mchUserService.checkLogin(mchUserInfo)) {
			logger.info("put mchuser to session.mchuserName="
					+ mchUserInfo.getMchName());
			request.getSession().setAttribute(IndexConfig.MCHUSER_SESSION_KEY,
					DesUtil.encrypt(String.valueOf(mchUserInfo.getMchId())));

			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(true,
					"登录成功"));
			return;
		}

		// 登录失败 重新登录
		logger.warn("login failed ,now redirect to login page");
		ResponseToClient.writeJsonMsg(response, new ResultMsgBean(false,
				"请重新登陆"));
	}

	/**
	 * 跳转登录界面
	 * 
	 * @return
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_LOGIN_URI)
	public ModelAndView toLogin(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		logger.info("to login page.");
		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.LOGIN_PAGE);
	}

	/**
	 * 注销
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_LOGOUT_URI)
	public ModelAndView logOut(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().removeAttribute(IndexConfig.MCHUSER_SESSION_KEY);
		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.LOGIN_PAGE);
	}

	/**
	 * 商家账户修改密码 : 通过 ajax 方式异步通信
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.CHANGE_PASSWD_URI)
	public void changePwd(HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("to change passwd .");
		// ajax实现读取原密码与输入相比较
		mchUserService.changeMchUserPasswd(request, response);
	}

	/**
	 * 商家确认 员工完成订单: 修改订单状态，并且发送订单完成消息给用户
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.CONFIRM_FINISH_TRANSACTION)
	public void confirmStaffFinishTransaction(HttpServletRequest request,
			HttpServletResponse response) {
		int newStatus = TransactionStatusEnum.FINISHED.getStatusInt();
		logger.info("to change  transaction status to " + newStatus);
		// ajax实现读取原密码与输入相比较
		mchUserService.changeTransactionStatus(request, response, newStatus);
		logger.info(" change  transaction  finished,status to " + newStatus);

	}

	/**
	 * 查看商家自己的微信用户 ： 商家 则只能查看自己的微信用户
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.TO_WECHAT_LIST)
	public ModelAndView toWechatUserList(HttpServletRequest request,
			ModelMap map, HttpServletResponse response) {
		List<TbWechatUser> wechatUsers;
		try {

			wechatUsers = mchUserService.getMchWechatUsers(request, response,
					map);

			String nickName = request.getParameter("nickName");
			if(StringUtils.isNotBlank(nickName)){
				map.put("nickName", nickName);
			}

			// 根据关注表转化为微信用户表
			changeNickName(wechatUsers, nickName);

			map.put("cacheWechat", wechatUsers);

			int allCount = wechatUsers == null ? 0 : wechatUsers.size();

			logger.info("find data and to wechatUsers page ,count=" + allCount);

			MenuSwitchShowManager.updatePageMenuSwitch(map,
					MenuSwitchShowManager.WECHATUSER_SWITCH, "active");
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.WECHAT_LIST_PAGE);

		} catch (SessionInvalidException e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.TO_LOGIN_URI);
		} catch (HappyLifeException e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}

	}

	/**
	 * 商家去确认订单
	 */
	@RequestMapping("/toconfirmtransaction")
	public ModelAndView toConfirmTranaction(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		logger.info("mchuser query  transactions...");

		String recordId = ParseRequest.parseRequestByType("recordId", false,
				request);
		logger.info("mchuser query  transactions ,recordId=" + recordId);
		if (StringUtils.isBlank(recordId)) {
			logger.error("must query one transaction info ,request has no recordId");
			return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
		}

		@SuppressWarnings("unchecked")
		TbTransactionRecord record;
		try {
			record = (TbTransactionRecord) mchUserService.getById(
					TbTransactionRecord.class, Integer.valueOf(recordId));
		} catch (NumberFormatException e) {
			logger.error("format  error ,e" + e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		} catch (HappyLifeException e) {
			logger.error("get record error ,id=" + recordId + ","
					+ e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}

		Integer fkMchUserId = ParseRequest.getMchUserIdFromSession(request);

		if (!WinterOrangeSysConf.IN_TEST_MODEL
				&& (null == fkMchUserId || record.getFkMchId() != fkMchUserId)) {
			logger.error("the mchUser ,can not query transaction id="
					+ recordId);
			return new ModelAndView(PageConfigUtil.ERROR_403_PAGE);
		}

		StringBuilder tmpProductName = new StringBuilder();

		CacheTransaction tmpCacheTransaction = null;
		MchStaffProductCacheManager manager = MchStaffProductCacheManager
				.getInstance();

		tmpCacheTransaction = new CacheTransaction();
		tmpCacheTransaction.setTransaction(record);

		TransactionStatusEnum[] statusEnums = TransactionStatusEnum.values();
		for (TransactionStatusEnum tran : statusEnums) {
			if (tran.getStatusInt() == record.getRecordStatus()) {
				tmpCacheTransaction.setStatusMsg(tran.getStatusMsg());
				break;
			}
		}

		String tmpMchShopName = manager.getMchUserById(record.getFkMchId())
				.getShopName();

		tmpCacheTransaction.setMchShopName(tmpMchShopName);

		String extPropsStr = record.getExtProps();
		Map<String, Object> extPropsMap = null;
		if (StringUtils.isNotBlank(extPropsStr)) {
			extPropsMap = JSON.parseObject(extPropsStr, Map.class);
		}
		extPropsMap = (extPropsMap == null) ? new HashMap() : extPropsMap;
		tmpCacheTransaction.setProcessMsg((String) extPropsMap.get("detail"));
		JSONObject jsonmuweihao = (JSONObject) extPropsMap.get("muweihaos");
		jsonmuweihao = jsonmuweihao == null ? new JSONObject() : jsonmuweihao;
		tmpCacheTransaction.setMuweihao(jsonmuweihao);

		String contentPictures = (String) extPropsMap.get("contentPicture");
		String[] pictures = null;
		if (StringUtils.isNotBlank(contentPictures)) {
			pictures = contentPictures.replace("\\", "/").split(
					StrUtil.SPLIT_STR);
		}

		JSONArray gouwucheIds = (JSONArray) extPropsMap.get("gouwucheIds");
		if (null == gouwucheIds || gouwucheIds.size() == 0) {
			logger.warn("the  record have no product, record id="
					+ record.getRecordId());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}

		TbMchStaff staff = manager.getStaff(record.getFkStaffId(), null);
		if (null != staff) {
			tmpCacheTransaction.setMchStaff(staff);
		}

		// 生成购物车的商品列表，按单个商品以及对应的数量去组织数据
		ArrayList<CacheProduct> cacheProducts = WechatUserUtil
				.genCacheProductsForGouwuche(gouwucheIds);
		for (CacheProduct p : cacheProducts) {
			tmpProductName.append(p.getProduct().getProductName());
			tmpProductName.append("  ");
		}

		tmpCacheTransaction.setProductName(tmpProductName.toString());
		tmpCacheTransaction.setContentPictures(pictures);
		model.put("cacheTransaction", tmpCacheTransaction);
		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ "/transaction/single");
	}

	/**
	 * 判断商家原密码是否输入正确 : 通过 ajax 方式异步通信
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.VALIDATE_PASSWD_URI)
	public void validatePwd(HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("to validate passwd .");
		// ajax实现读取原密码与输入相比较
		mchUserService.validateMchUserPasswd(request, response);
	}

	/**
	 * 商家编辑自己个人信息界面
	 * 
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.UPDATE_USERINFO_URI)
	public ModelAndView updateMchUserInfo(ModelMap map,
			HttpServletRequest request, TbMch mch, HttpServletResponse response) {
		logger.info("to change mchuser info.");

		try {
			mchUserService.updateMchUserInfo(request, mch, response);
			Integer fkMchUserId = ParseRequest.getMchUserIdFromSession(request);

			TbMch mchUserInfo = MchStaffProductCacheManager.getInstance()
					.getMchUserById(fkMchUserId);

			TbMch cloneMchUser = mchUserInfo.clone();

			String extProps = cloneMchUser.getExtProps();
			Map<String, String> propsMap = JSON
					.parseObject(extProps, Map.class);
			if (propsMap == null) {
				extProps = "请设置服务时间例如：09:00-21:00";
			} else {
				String start = propsMap.get("start");
				String end = propsMap.get("end");

				if (StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
					extProps = "请设置服务时间例如：09:00-21:00";
				} else {
					extProps = start + "-" + end;
				}
			}

			cloneMchUser.setExtProps(extProps);

			map.put("mchUser", cloneMchUser);
			map.put("result", "更新成功");
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.INFO_PAGE);
		} catch (RequestInvalidException e) {
			logger.error("the request does not contain new mchUserinfo .");
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);

		}

	}

	/**
	 * 商家账户查看自己的信息
	 * 
	 * @param
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_INFO_URI)
	public ModelAndView toMchUserInfo(ModelMap map, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String fkMchId = (String) request.getSession().getAttribute(
				IndexConfig.MCHUSER_SESSION_KEY);
		String fkMchIdStr = DesUtil.decrypt(fkMchId);
		logger.info("begin to mchuserInfo fkMchIdStr:" + fkMchIdStr);
		/* 这样的方式默认按照 以 商家为维度 查询商品的方式 */
		if (StringUtils.isBlank(fkMchId) || !StringUtils.isNumeric(fkMchIdStr)) {
			logger.error("fkMchId can not be null,or not numirreturn");
			return null;
		}
		TbMch mchUser = MchStaffProductCacheManager.getInstance()
				.getMchUserById(Integer.valueOf(fkMchIdStr));

		TbMch cloneMchUser = mchUser.clone();

		String extProps = cloneMchUser.getExtProps();
		Map<String, String> propsMap = JSON.parseObject(extProps, Map.class);

		String start = propsMap.get("start");
		String end = propsMap.get("end");

		if (StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
			extProps = "请设置服务时间例如：09:00-21:00";
		} else {
			extProps = start + "-" + end;
		}

		cloneMchUser.setExtProps(extProps);

		// 放入缓存
		map.put("mchUser", cloneMchUser);

		map.put("mchStatuss", MchServiceStatusEnum.values());

		MenuSwitchShowManager.updatePageMenuSwitch(map,
				MenuSwitchShowManager.INFO_SWITCH, "active");

		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.INFO_PAGE);
	}

	/**
	 * 查看 兑换码界面 ： 商家 则只能查看 自己的兑换码
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.TO_REDEEMCODE_LIST_URI)
	public ModelAndView toRedeemCodeList(HttpServletRequest request,
			ModelMap map, HttpServletResponse response) {
		List<CacheRedeemCode> cacheRedeemCodes = mchUserService
				.generateRedeemCodeList(request, map);
		map.put("cacheRedeemCodes", cacheRedeemCodes);
		logger.info("find data and to redeemcode list");
		// 判断该请求是否来自主页
		if (request.getParameter("index") != null
				&& request.getParameter("index").equals("index")) {
			// 流水
			List<TransactionReportBean> reports = mchUserService
					.generateOneWeekTransactionRecords(request, map);
			map.put("reports", reports);

			MenuSwitchShowManager.updatePageMenuSwitch(map,
					MenuSwitchShowManager.INDEX_SWITCH, "active");

			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.INDEX_PAGE);
		}
		logger.info("i am herer");

		MenuSwitchShowManager.updatePageMenuSwitch(map,
				MenuSwitchShowManager.REDEEM_SWITCH, "active");

		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.REDEEMCODE_LIST_PAGE);
	}

	/**
	 * 商家置换 微信账户的 兑换码，用于实际 门店内的消费
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.CHANGE_REDEEMCODE_URI)
	public void changeRedeemCodeStatus(HttpServletRequest request,
			ModelMap map, HttpServletResponse response) {

		// 当前选中的标签页是 兑换码界面，更新显示开关
		logger.info("begin to change redeemCode status");
		mchUserService.changeRedeemCodeStatus(request, response, false);
		logger.info("finish to change redeemCode status");
	}

	/**
	 * 商家给微信用户重新发送兑换码
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.RESEND_REDEEMCODE_URI)
	public void resendRedeemCode(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {

		// 当前选中的标签页是 兑换码界面，更新显示开关
		logger.info("begin to resend redeemCode ");
		mchUserService.changeRedeemCodeStatus(request, response, true);
		logger.info("finish to resend redeemCode");
	}

	// /**
	// * 查看商家自己的微信用户 ： 商家 则只能查看自己的微信用户
	// *
	// * @author snnile2012
	// * @param request
	// * @param map
	// * @param response
	// * @return
	// */
	// @SuppressWarnings("unchecked")
	// @RequestMapping(PageConfigUtil.MchUserPage.TO_WECHAT_LIST)
	// public ModelAndView toWechatUserList(HttpServletRequest request,
	// ModelMap map, HttpServletResponse response) {
	// List<TbMchWechatRelation> relations;
	// try {
	//
	// relations = mchUserService
	// .getMchWechatUsers(request, response, map);
	//
	// // 根据关注表转化为微信用户表
	// List<CacheWechatUser> wechatUsers = getWechatUsersByRelation(relations);
	//
	// map.put("cacheWechat", wechatUsers);
	//
	// int allPages = wechatUsers == null ? 0 : wechatUsers.size();
	//
	// logger.info("find data and to wechatUsers page ,count=" + allPages);
	// map.put("allPages", allPages);
	//
	// MenuSwitchShowManager.updatePageMenuSwitch(map,
	// MenuSwitchShowManager.WECHATUSER_SWITCH, "active");
	// return new ModelAndView(PageConfigUtil.MCH_PREFIX
	// + PageConfigUtil.MchUserPage.WECHAT_LIST_PAGE);
	//
	// } catch (SessionInvalidException e) {
	// logger.error(e.getMessage());
	// return new ModelAndView(PageConfigUtil.MCH_PREFIX
	// + PageConfigUtil.MchUserPage.TO_LOGIN_URI);
	// } catch (HappyLifeException e) {
	// logger.error(e.getMessage());
	// return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
	// }
	//
	// }

	/**
	 * 查看 兑换码界面 ： 商家 则只能查看 自己的兑换码
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.TO_STAFF_LIST)
	public ModelAndView toStaffList(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		List<TbMchStaff> staffs;
		try {

			staffs = mchUserService.getStaffs(request, response, map);

			logger.info("find data and to stafflist page");

			map.put("staffs", staffs);

			int allPages = staffs == null ? 0 : staffs.size();
			map.put("allPages", allPages);

			MenuSwitchShowManager.updatePageMenuSwitch(map,
					MenuSwitchShowManager.USER_SWITCH, "active");
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.STAFF_LIST_PAGE);

		} catch (SessionInvalidException e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.TO_LOGIN_URI);
		} catch (HappyLifeException e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}

	}

	/**
	 * 查看某个员工信息页面
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.TO_SINGLESTAFF_URI)
	public ModelAndView toSingleStaff(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		try {

			String staffId = (String) request
					.getParameter(HttpRequestHeaderKey.MCH_STAFF_ID);

			MenuSwitchShowManager.updatePageMenuSwitch(map,
					MenuSwitchShowManager.USER_SWITCH, "active");
			// 说明是新增，调到新页面
			if (StringUtils.isBlank(staffId)) {

				logger.warn("to new single staff page");
				return new ModelAndView(PageConfigUtil.MCH_PREFIX
						+ PageConfigUtil.MchUserPage.SINGLESTAFF_PAGE);
			}

			// 说明是编辑已有的员工，先判断这个员工的fkMchId 是否是当前商家的必须有匹配关系
			logger.info(" to single staff  page");
			TbMchStaff staff = (TbMchStaff) mchUserService.getById(
					TbMchStaff.class, Integer.valueOf(staffId));

			if (null == staff
					|| (int) ParseRequest.getMchUserIdFromSession(request) != staff
							.getFkMchId()) {
				logger.warn("the staff does not exit,id=" + staffId);
				return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
			}

			map.put("staff", staff);
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.SINGLESTAFF_PAGE);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}

	}

	/**
	 * 注销某个员工
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.DELETE_STAFF_URI)
	public void deleteStaff(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		try {

			String staffId = (String) request
					.getParameter(HttpRequestHeaderKey.MCH_STAFF_ID);
			if (StringUtils.isBlank(staffId)) {
				logger.warn("no need delete staff");
				ResponseToClient.writeJsonMsg(response, new ResultMsgBean(true,
						"注销成功"));
				return;
			}

			// 说明是编辑已有的员工，先判断这个员工的fkMchId 是否是当前商家的必须有匹配关系
			@SuppressWarnings("unchecked")
			TbMchStaff staff = (TbMchStaff) mchUserService.getById(
					TbMchStaff.class, Integer.valueOf(staffId));
			if (null == staff
					|| (int) ParseRequest.getMchUserIdFromSession(request) != staff
							.getFkMchId()) {
				logger.warn("the staff does not exit,id=" + staffId);
				ResponseToClient.writeJsonMsg(response, new ResultMsgBean(true,
						"注销成功"));
				return;
			}

			staff.setIsDelete(1);
			staff.setModifyTime(new Date());

			mchUserService.saveOrUpdateObject(staff);

			// 从商户缓存中删除该员工

			CacheMchUser mchUser = MchStaffProductCacheManager.getInstance()
					.getCacheMchUserById(staff.getFkMchId());

			List<TbMchStaff> staffs = mchUser.getStaffs();
			TbMchStaff temStaff = null;

			for (TbMchStaff s : staffs) {
				if (s.getId().intValue() == staff.getId().intValue()) {
					temStaff = s;
				}
			}

			staffs.remove(temStaff);
			mchUser.setStaffs(staffs);
			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(true,
					"注销成功"));

		} catch (Exception e) {
			logger.error(e.getMessage());
			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(false,
					"注销失败"));
		}

	}

	/**
	 * 新增或者更新一个用户
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@RequestMapping(value = PageConfigUtil.MchUserPage.ADD_OR_UPDATE_STAFF_URI, method = RequestMethod.POST)
	public ModelAndView addOrUpdateStaff(TbMchStaff staff,
			HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		try {
			MenuSwitchShowManager.updatePageMenuSwitch(map,
					MenuSwitchShowManager.USER_SWITCH, "active");

			@SuppressWarnings("unchecked")
			boolean isSuccess = mchUserService.saveOrUpdateStaff(staff,
					request, response, map);
			if (isSuccess) {
				List<TbMchStaff> staffs = mchUserService.getStaffs(request,
						response, map);
				logger.info("find data and to stafflist page");
				map.put("staffs", staffs);
				int allPages = staffs == null ? 0 : staffs.size();
				map.put("allPages", allPages);
				return new ModelAndView(PageConfigUtil.MCH_PREFIX
						+ PageConfigUtil.MchUserPage.STAFF_LIST_PAGE);
			}

			logger.warn("update staff error");
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);

		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}
	}

	/**
	 * 查看 兑换码界面 ： 商家 则只能查看 自己的兑换码
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_PRODUCT_LIST_URI)
	public ModelAndView toProductList(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		List<TbProduct> products;
		try {
			products = mchUserService.generateProductsList(request, map);
			map.put("products", products);
			logger.info("find data and to mch user products page");

			MenuSwitchShowManager.updatePageMenuSwitch(map,
					MenuSwitchShowManager.PRODUCT_SWITCH_ALL, "active");
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.PRODUCT_LIST_PAGE);

		} catch (SessionInvalidException e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.LOGIN_PAGE);
		}

	}

	/**
	 * 商家查询单个 商品
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_SINGLEPRODUCT_URI)
	public ModelAndView toSingleProductPage(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		logger.info("reward to single product page");

		CacheProduct cacheProduct = ParseRequest.findCacheProduct(request);
		if (null == cacheProduct) {
			logger.error("the product not found,redirect to 404 ");
			// 如果没有找到产品 直接跳转到 404 界面
			return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
		}

		// 放入缓存
		model.put("cacheProduct", cacheProduct);
		model.put("commonPicturePath", IndexConfig.RELATIVE_PATH_PREFIX);
		model.put("contentPicts", ProductUtil
				.translateContentPathToArr(cacheProduct.getProduct()
						.getProductContentPicture()));

		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.SINGLE_PRODUCT_PAGE + "_"
				+ cacheProduct.getProduct().getProductType());

	}

	/**
	 * 商家 跳转到新增商品界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_ADD_PRODUCT_URI)
	public ModelAndView toAddProductPage(ModelMap map,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		logger.info("reward to add product page");

		// 把商家信息读取出来
		String fkMchId = (String) request.getSession().getAttribute(
				IndexConfig.MCHUSER_SESSION_KEY);

		String fkMchIdStr = DesUtil.decrypt(fkMchId);

		logger.info("begin to fkMchIdStr:" + fkMchIdStr);
		/* 这样的方式默认按照 以 商家为维度 查询商品的方式 */
		if (StringUtils.isBlank(fkMchId) || !StringUtils.isNumeric(fkMchIdStr)) {
			logger.error("fkMchId can not be null,or not numirreturn");
			return null;
		}

		MenuSwitchShowManager.updatePageMenuSwitch(map,
				MenuSwitchShowManager.PRODUCT_SWITCH_ADD, "active");

		TbMch mchUser = MchStaffProductCacheManager.getInstance()
				.getMchUserById(Integer.valueOf(fkMchIdStr));
		// 放入缓存
		map.put("mchUser", mchUser);

		// 需要把商家信息读取出来
		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.ADD_PRODUCT_PAGE);
	}

	/**
	 * 商家 跳转到新增商品界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_SUGGEST)
	public ModelAndView toSuggestionPage(ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// 商家去针对自己的 微信用户的意见 页面
		logger.info("reward to suggest  page");
		List<TbSuggestion> suggests = null;
		try {
			suggests = mchUserService.getSuggestions(request, response, model);
		} catch (RequestInvalidException e) {
			logger.error("request is invalid");
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.LOGIN_PAGE);
		} catch (SessionInvalidException e) {
			logger.error("session invalid ,to relogin");
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.LOGIN_PAGE);
		} catch (HappyLifeException e) {
			logger.error("internal error!");
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}
		// 放入缓存
		model.put("suggests", suggests);

		MenuSwitchShowManager.updatePageMenuSwitch(model,
				MenuSwitchShowManager.SUGGEST_SWITCH, "active");

		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.SUGGEST_PAGE);
	}

	/**
	 * 商家预添加一个 商品， 里面有图片信息
	 * 
	 * @param map
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	// TODO：补充一个字段商品类型便于检索
	@RequestMapping(PageConfigUtil.MchUserPage.PREADD_PRODUCT_URI)
	public ModelAndView preAddProduct(ModelMap map, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// 需要查询 所有的 美食类商家以及每个商家先的 美食种类
		logger.info("pre add a product");
		CacheProduct cacheProduct = mchUserService.addNewSingleProduct(request);

		if (null == cacheProduct) {
			logger.error("internal error!");
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}

		// 放入缓存
		map.put("cacheProduct", cacheProduct);
		map.put("commonPicturePath", IndexConfig.RELATIVE_PATH_PREFIX);
		map.put("contentPicts", ProductUtil
				.translateContentPathToArr(cacheProduct.getProduct()
						.getProductContentPicture()));

		MenuSwitchShowManager.updatePageMenuSwitch(map,
				MenuSwitchShowManager.PRODUCT_SWITCH, "active");

		// 需要把商家信息读取出来
		return new ModelAndView(PageConfigUtil.WECHAT_PREFIX
				+ PageConfigUtil.WechatUserPage.PRODUCT_COMMON_PAGE
				+ cacheProduct.getProduct().getProductType() + "/single");

	}

	/**
	 * 商家编辑 商品的状态： 如从 下架更新为上架等
	 * 
	 * @param map
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.SET_PRODUCT_STATUS_URI)
	public void editProductStatus(ModelMap map, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		logger.info("begin to change product status");
		mchUserService.changeProductStatus(request, response);
		logger.info("finish to change product status");

	}

	/**
	 * 商家查看自己的订单
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.TO_TRANSACTIONS_URI)
	public ModelAndView toTransactionRecords(ModelMap map,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		try {
			logger.info("begin to find transactions");
			List<CacheTransaction> cacheTransactions = mchUserService
					.generateCacheTransactionList(request, map, false);
			// 放入缓存
			map.put("cacheTransactions", cacheTransactions);

			// 把商家信息读取出来
			Integer fkMchId = ParseRequest.getMchUserIdFromSession(request);
			if (null == fkMchId) {
				ResponseToClient.writeJsonMsg(response, new ResultMsgBean(
						false, "用户信息不对，请重新登录"));
				return null;
			}

			List<TbMchStaff> staffs = MchStaffProductCacheManager.getInstance()
					.getCacheMchUserById(fkMchId).getStaffs();

			if (null == staffs) {
				ResponseToClient.writeJsonMsg(response, new ResultMsgBean(
						false, "查询不到员工数据"));
				return null;
			}

			// 需要把 注销状态的员工去掉
			List<TbMchStaff> activeStaffs = new ArrayList();
			for (TbMchStaff s : staffs) {
				if (s.getIsDelete() == 0) {
					activeStaffs.add(s);
				}

			}
			map.put("staffs", activeStaffs);

			MenuSwitchShowManager.updatePageMenuSwitch(map,
					MenuSwitchShowManager.REPORT_SWITCH, "active");

			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.TRANSACTIONS_LIST_PAGE);

		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}
	}

	/**
	 * 修改订单信息，会触发给员工发送微信消息
	 * 
	 * @param tbMchStaff
	 * @param map
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TRANSACTION_MANAGER_URI)
	public void updateTransaction(
			@ModelAttribute TbTransactionRecord transaction, ModelMap map,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		if (null == transaction || transaction.getRecordId() == null
				|| transaction.getFkStaffId() == 0) {
			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(false,
					"参数错误，请重试"));
			return;
		}

		// 把商家信息读取出来
		Integer fkMchId = ParseRequest.getMchUserIdFromSession(request);
		if (null == fkMchId) {
			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(false,
					"用户信息不对，请重新登录"));
			return;
		}

		try {
			TbTransactionRecord oldRecord = (TbTransactionRecord) mchUserService
					.getById(TbTransactionRecord.class,
							transaction.getRecordId());

			if (null == oldRecord) {
				logger.error("the record not exit ,id="
						+ transaction.getRecordId());
				ResponseToClient.writeJsonMsg(response, new ResultMsgBean(
						false, "订单不存在了，请联系管理员"));
				return;
			}

			oldRecord.setFkStaffId(transaction.getFkStaffId());

			// 这里触发下给该员工发消息
			TbMchStaff staff = (TbMchStaff) mchUserService.getById(
					TbMchStaff.class, transaction.getFkStaffId());

			if (null == staff) {
				ResponseToClient.writeJsonMsg(response, new ResultMsgBean(
						false, "该员工不存在，请重新选择"));
				return;
			}

			String fkStaffOpenId = staff.getFkOpenId();
			if (StringUtils.isBlank(fkStaffOpenId)) {
				logger.error("the staff have no weixin fkOpenId id="
						+ staff.getId());
			} else {
				// 微信通知下

				String url = WeChatConfig.WEBSITE_WECHAT_PREFIX
						+ "/tofinishjisidingdan?recordId="
						+ transaction.getRecordId();
				JSONObject respJson = WechatMessageUtil.sendTransactionToStaff(
						oldRecord, fkStaffOpenId, url);
				logger.warn(respJson);
			}

			oldRecord.setRecordStatus(2); // 后面统一定义
			mchUserService.saveOrUpdateObject(oldRecord);

			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(true,
					"指派成功"));
			return;

		} catch (HappyLifeException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());

			ResponseToClient.writeJsonMsg(response, new ResultMsgBean(false,
					"系统异常，请重试"));
			return;
		}

	}

	/**
	 * 商家账户导出自己的流水
	 * 
	 * @param
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.EXPORT_DATA)
	public void exportData(ModelMap map, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("begin to export data");
		List<CacheTransaction> cacheTransactions = mchUserService
				.generateCacheTransactionList(request, map, true);
		if (null == cacheTransactions) {
			return;
		}

		ExcelUtil.exportTsrExcel(cacheTransactions, response);
	}

	/**
	 * 跳转到主页
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@RequestMapping(PageConfigUtil.MchUserPage.TO_INDEX_URI)
	public ModelAndView toIndex(HttpServletRequest request, ModelMap map,
			HttpServletResponse response) {
		// 兑换码
		List<CacheRedeemCode> cacheRedeemCodes = mchUserService
				.generateRedeemCodeList(request, map);
		map.put("cacheRedeemCodes", cacheRedeemCodes);
		// 流水
		List<TransactionReportBean> reports = mchUserService
				.generateOneWeekTransactionRecords(request, map);
		map.put("reports", reports);

		MenuSwitchShowManager.updatePageMenuSwitch(map,
				MenuSwitchShowManager.INDEX_SWITCH, "active");

		logger.info("now to the Index");
		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.INDEX_PAGE);
	}

	/**
	 * 跳转到活动界面
	 * 
	 * @author snnile2012
	 * @param request
	 * @param map
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(PageConfigUtil.MchUserPage.TO_ACTIVITY_LIST)
	public ModelAndView toActivityList(HttpServletRequest request,
			ModelMap map, HttpServletResponse response) {

		logger.info("to activities page");
		// 查看活动
		List<TbActivity> activities = null;
		map.put("activities", activities);

		// 封装自己的商品，方便加入到活动
		List<TbProduct> products = null;
		try {
			products = mchUserService.generateProductsList(request, map);
			map.put("products", products);
			logger.info("find data and to mch user products page");

		} catch (SessionInvalidException e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.MCH_PREFIX
					+ PageConfigUtil.MchUserPage.LOGIN_PAGE);
		}

		map.put("products", products);

		MenuSwitchShowManager.updatePageMenuSwitch(map,
				MenuSwitchShowManager.ACTIVITY_SWITCH, "active");

		logger.info("now to activity page");
		return new ModelAndView(PageConfigUtil.MCH_PREFIX
				+ PageConfigUtil.MchUserPage.ACTIVITY_PAGE);
	}

	private void changeNickName(List<TbWechatUser> wechatUsers, String nickName) {
		if (null == wechatUsers || wechatUsers.size() == 0) {
			return;
		}

		try {
			Iterator<TbWechatUser> it = wechatUsers.iterator();
			while (it.hasNext()) {
				TbWechatUser user = it.next();
				if (StringUtils.isNotBlank(nickName)) {
					String nickNameEncode = URLEncoder
							.encode(nickName, "utf-8");
					if (!user.getUserNickName().contains(nickNameEncode)) {
						it.remove();
					}
				}

			}

			for (TbWechatUser user : wechatUsers) {
				String userNickName = user.getUserNickName();
				user.setUserNickName(URLDecoder.decode(userNickName, "utf-8"));
			}

		} catch (UnsupportedEncodingException e) {
			logger.warn("the encode error,e"+e.getMessage());
		}
	}

}
