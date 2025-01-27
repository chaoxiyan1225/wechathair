package happylife.controller;

import happylife.model.TbMch;
import happylife.model.TbMchStaff;
import happylife.model.TbTransactionRecord;
import happylife.model.TbWechatUser;
import happylife.model.servicemodel.CacheMchUser;
import happylife.model.servicemodel.CacheMchUserQueueOrder;
import happylife.model.servicemodel.CacheProduct;
import happylife.model.servicemodel.CacheRedeemCode;
import happylife.model.servicemodel.CacheTransaction;
import happylife.model.servicemodel.CacheWechatUser;
import happylife.model.servicemodel.MchServiceStatusEnum;
import happylife.model.servicemodel.ProductSearchCondition;
import happylife.model.servicemodel.TestBean;
import happylife.model.servicemodel.TransactionStatusEnum;
import happylife.service.WeChatUserService;
import happylife.service.exception.HappyLifeException;
import happylife.util.StrUtil;
import happylife.util.cache.MchStaffProductCacheManager;
import happylife.util.cache.QueueOrderCacheManager;
import happylife.util.config.IndexConfig;
import happylife.util.config.OrderTypeConfigEnum;
import happylife.util.config.PageConfigUtil;
import happylife.util.config.ProductType;
import happylife.util.config.WinterOrangeSysConf;
import happylife.util.requestandresponse.ParseRequest;
import happylife.util.requestandresponse.ResponseToClient;
import happylife.util.requestandresponse.WeChatRequestUtil;
import happylife.util.service.ProductUtil;
import happylife.util.service.WechatUserUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信用户的controller类，处理微信界面转接来的各种请求上面的用户的各类请求
 *
 * @author 闫朝喜
 */
@Controller
@RequestMapping(PageConfigUtil.WECHAT_PREFIX)
public class WeChatUserController {
    private static final Log logger = LogFactory.getLog(WeChatUserController.class);
    @SuppressWarnings("rawtypes")
	@Autowired
    private WeChatUserService wechatUserService;
    
  
    /**
     * 与用户的关注以及取消关注，点击事件等的互动的controller层处理
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.INTERACTIVE_URI)
    public void sortInteractiveRequst(HttpServletRequest request,
                                      HttpServletResponse response){
        String method = request.getMethod();
        
		try {
			// 如果是微信校验signature的请求
			if (method.equalsIgnoreCase("GET")) {
				wechatUserService.checkWeChatAccessSignature(request, response);
			} else {
				// 处理关注、取消关注、点击事件等请求
				wechatUserService.processWechatInteractive(request, response);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
        

    }
   
    /**
     * 微信账户跳转到所有商家界面
     */
    // TODO: 做成通用接口 根据type 跳转到不同界面； 参考 forwardSingelProductPageByType
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_MCHLIST_URI)
    public ModelAndView toMchsListPage(Map<String, Object> model,
                                          HttpServletRequest request, HttpServletResponse response){
		try {
			CacheWechatUser cacheChatUser = null ;
			// 不是测试模式
			if (!WinterOrangeSysConf.IN_TEST_MODEL) {
				// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
			    cacheChatUser = ParseRequest
						.getWechatUserFromSession(request);
				if (null == cacheChatUser
						|| StringUtils.isBlank(cacheChatUser.getWechatUser()
								.getUserOpenid())) {
					logger.error("wechat user session timeout,re auth");
					WeChatRequestUtil.redirectToAuthorize(response);
					return null;
				}
			} else {
				logger.debug("current in test model");
				
				TbWechatUser chatUser = new TbWechatUser();
				
				//chatUser.setUserOpenid("666666");//员工
				chatUser.setUserOpenid("0628st");//员工
				cacheChatUser = new CacheWechatUser();
				cacheChatUser.setWechatUser(chatUser);
				
			}
			
			
	    	//判断该用户什么类型？ staff  mch 或者admin?  后续扩展
	    	TbMchStaff staff = MchStaffProductCacheManager.getInstance().getStaff(null, cacheChatUser.getWechatUser().getUserOpenid());
	    	if(null != staff){
	    		logger.warn("the wechat user is staff ,to  staff transaciton page");
	    		//该用户同时也是staff 跳转到员工的staff页面
	    		return toStaffTransactionList(model,request,response,staff);
	    	}
	    	
			
			//选择是那种类型的：排队的 或者是商品的
			String type = ParseRequest.parseRequestByType("type", true, request);
			if(StringUtils.isBlank(type)){
				type = "product";
			}
			
			
			model.put("type", type);

			logger.debug("wechat use to mch list page");
			wechatUserService.generateMchListByPage(request, model, response,
					false);
			return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+PageConfigUtil.WechatUserPage.MCH_LIST_PAGE);
		} catch (Exception e) {
			logger.error("toMchsListPage error happends,"+e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}
    }
    
    
    //ajax方式按搜索框去检索商家全部信息列表，仅支持商家名或者地址模糊匹配
    public void searchMchsByNameOrAddress(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response){
		try {
			// 不是测试模式
			if (!WinterOrangeSysConf.IN_TEST_MODEL) {
				// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
				CacheWechatUser cacheChatUser = ParseRequest
						.getWechatUserFromSession(request);
				if (null == cacheChatUser
						|| StringUtils.isBlank(cacheChatUser.getWechatUser()
								.getUserOpenid())) {
					logger.error("wechat user session timeout,re auth");
					WeChatRequestUtil.redirectToAuthorize(response);
					return ;
				}
			} else {
				logger.debug("current in test model");
			}

			logger.debug("wechat use to mch list page");
			wechatUserService.generateMchListByPage(request, model, response,
					false);
			
		} catch (Exception e) {
			logger.error("toMchsListPage error happends,"+e.getMessage());
			return ;
		}
    	
    }
    
    //ajax方式返回满足的商家名或者地址的商家列表
    public void queryMchNameListByNameOrAddress(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response){
		try {
			// 不是测试模式
			if (!WinterOrangeSysConf.IN_TEST_MODEL) {
				// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
				CacheWechatUser cacheChatUser = ParseRequest
						.getWechatUserFromSession(request);
				if (null == cacheChatUser
						|| StringUtils.isBlank(cacheChatUser.getWechatUser()
								.getUserOpenid())) {
					logger.error("wechat user session timeout,re auth");
					WeChatRequestUtil.redirectToAuthorize(response);
					return ;
				}
			} else {
				logger.debug("current in test model");
			}

			logger.debug("wechat use to mch list page");
			wechatUserService.generateMchListByPage(request, model, response,
					false);
			
		} catch (Exception e) {
			logger.error("toMchsListPage error happends,"+e.getMessage());
			return ;
		}
    }
    
    @RequestMapping("/toindex")
    public void toIndex(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) {
    	
    	CacheMchUser cacheMchUser = new CacheMchUser();
    	
    	String[] pictures = {"aaaa","bbbb","cccc"};
    	cacheMchUser.setContentPictures(pictures);
    	
    	
    	
    	TestBean test = new TestBean();
    	test.setAddress("四川省成都市高新区");
    	test.setDate("2020-04-02 12:30:56");
    	test.setContent("大家都来吧");
    	test.setPath("https://gss0.bdstatic.com/7Ls0a8Sm1A5BphGlnYG/sys/portrait/item/00000000.jpg");
    	test.setHref("https://blog.csdn.net/daren0017/article/details/85128601");
    	
    	
    	List<TestBean> beans = new ArrayList();
    	beans.add(test);
    	Map<String,Object> data = new HashMap();
    	data.put("data", beans);
    	ResponseToClient.writeJsonMsg(response,data);
    	
    	//model.put("data", JSONObject.parse(content));
    	
    	 response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,HEAD,PUT,PATCH");
         response.setHeader("Access-Control-Max-Age", "36000");
    	
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	logger.warn("the toindex value="+JSONObject.toJSONString(cacheMchUser));
    }
    public static void main(String[] args) {
		
    	
    	TestBean test = new TestBean();
    	test.setAddress("四川省成都市高新区");
    	test.setDate("2020-04-02 12:30:56");
    	test.setContent("大家都来吧");
    	test.setPath("https://gss0.bdstatic.com/7Ls0a8Sm1A5BphGlnYG/sys/portrait/item/00000000.jpg");
    	test.setHref("https://blog.csdn.net/daren0017/article/details/85128601");
    	
    	
    	List<TestBean> beans = new ArrayList();
    	beans.add(test);
    	
    	Map<String,Object> data = new HashMap();
    	data.put("data", beans);
    
    	
    	String json = JSON.toJSONString(data);
    	
    	JSONObject ob = JSON.parseObject(json);
    	
    	JSONArray arr = (JSONArray)ob.get("data");
    	
    	System.out.print(arr);
    	
	}
    
    //跳转到完成订单界面，给staff类型的员工使用
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_FINISH_TRANSACTION_URI)
	public ModelAndView staffToFinishTransaction(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
			// 不是测试模式
			if (!WinterOrangeSysConf.IN_TEST_MODEL) {
				// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
				CacheWechatUser cacheChatUser = ParseRequest
						.getWechatUserFromSession(request);
				if (null == cacheChatUser
						|| StringUtils.isBlank(cacheChatUser.getWechatUser()
								.getUserOpenid())) {
					logger.error("wechat user session timeout,re auth");
					WeChatRequestUtil.redirectToAuthorize(response);
					return null;
				}
			} else {
				logger.debug("current in test model");
			}

			String recordId = ParseRequest.parseRequestByType("recordId",
					false, request);

			if (StringUtils.isBlank(recordId)) {
				logger.error("must query one transaction info ,request has no recordId");
				return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
			}

			@SuppressWarnings("unchecked")
			TbTransactionRecord record = (TbTransactionRecord) wechatUserService
					.getById(TbTransactionRecord.class,
							Integer.valueOf(recordId));

			CacheTransaction tmpCacheTransaction = null;

			MchStaffProductCacheManager manager = MchStaffProductCacheManager
					.getInstance();
			tmpCacheTransaction = new CacheTransaction();
			tmpCacheTransaction.setTransaction(record);

			TransactionStatusEnum[] statusEnums = TransactionStatusEnum
					.values();
			for (TransactionStatusEnum tran : statusEnums) {
				if (tran.getStatusInt() == record.getRecordStatus()) {
					tmpCacheTransaction.setStatusMsg(tran.getStatusMsg());
					break;
				}
			}

			String tmpProductName = manager
					.getProductById(record.getFkProductId()).getProduct()
					.getProductName();
			tmpCacheTransaction.setProductName(tmpProductName);

			String tmpMchShopName = manager
					.getProductById(record.getFkProductId()).getMchUser()
					.getShopName();

			tmpCacheTransaction.setMchShopName(tmpMchShopName);

			TbMchStaff staff = manager.getStaff(record.getFkStaffId(), null);

			if (null != staff) {
				tmpCacheTransaction.setMchStaff(staff);
			}
			
			model.put("cacheTransaction", tmpCacheTransaction);

			return new ModelAndView(PageConfigUtil.WECHAT_PREFIX
					+ PageConfigUtil.WechatUserPage.TRANSACTION_STAFF_ONE_PAGE);

		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}

	}  
    
    
    //跳转到完成订单界面，给staff类型的员工使用
    @RequestMapping(PageConfigUtil.WechatUserPage.FINISH_TRANSACTION_URI)
    public ModelAndView staffFinishTransaction(Map<String, Object> model,
    		HttpServletRequest request, HttpServletResponse response) {
    	try {
    		// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    		// 不是测试模式
    		CacheWechatUser cacheChatUser = null;
    		if (!WinterOrangeSysConf.IN_TEST_MODEL) {
    			// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    		    cacheChatUser = ParseRequest
    					.getWechatUserFromSession(request);
    			if (null == cacheChatUser
    					|| StringUtils.isBlank(cacheChatUser.getWechatUser()
    							.getUserOpenid())) {
    				logger.error("wechat user session timeout,re auth");
    				WeChatRequestUtil.redirectToAuthorize(response);
    				return null;
    			}
    		} else {
    			logger.debug("current in test model");
    		}
    		
    		String recordId = ParseRequest.parseRequestByType("recordId",
    				false, request);
    		if (StringUtils.isBlank(recordId)) {
    			logger.error("must query one transaction info ,request has no recordId");
    			return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
    		}
    		
    		@SuppressWarnings("unchecked")
    		TbTransactionRecord record = (TbTransactionRecord) wechatUserService
    		.getById(TbTransactionRecord.class,
    				Integer.valueOf(recordId));

    		TbMchStaff staff = MchStaffProductCacheManager.getInstance().getStaff(record.getFkStaffId(), null);
    		if(!WinterOrangeSysConf.IN_TEST_MODEL && 
    				(null == staff || !staff.getFkOpenId().equals(cacheChatUser.getWechatUser().getUserOpenid()))){
    			logger.error("the current staff not invalid ,can not update transaction id="+recordId);
    			return new ModelAndView(PageConfigUtil.ERROR_403_PAGE);
    		}
    		
    		
    		boolean isFinish = wechatUserService.staffFinishTransaction(model,record, staff, request, response);
    		if(!isFinish){
        		logger.error("update faild ,try later ");
        		return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
    			
    		}
    	
    		//跳转到继续处理订单页面
    		List<CacheTransaction> staffTransaction = wechatUserService.staffGetTransaction(model,staff , request, response);
        	
        	model.put("staff", staff);
        	model.put("cacheTransactions", staffTransaction);
        	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
                    PageConfigUtil.WechatUserPage.TRANSACTION_LIST_STAFF_PAGE);
    		
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    		return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
    	}
    	
    }   
    
    /**
     * 微信账户跳转单一商家界面 要查看排队信息这些
     */
    // TODO: 做成通用接口 根据type 跳转到不同界面； 参考 forwardSingelProductPageByType
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_PRODUCTS_OF_ONE_MCH_URI)
    public ModelAndView toProductsOfOneMchPage(Map<String, Object> model,
    		HttpServletRequest request, HttpServletResponse response) {
		try {
			// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
			// 不是测试模式
			if (!WinterOrangeSysConf.IN_TEST_MODEL) {
				// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
				CacheWechatUser cacheChatUser = ParseRequest
						.getWechatUserFromSession(request);
				if (null == cacheChatUser
						|| StringUtils.isBlank(cacheChatUser.getWechatUser()
								.getUserOpenid())) {
					logger.error("wechat user session timeout,re auth");
					WeChatRequestUtil.redirectToAuthorize(response);
					return null;
				}
			} else {
				logger.debug("current in test model");
			}

			logger.debug("wechat use to mch single page");
			String mchIdStr = ParseRequest.parseRequestByType("mchId", false,
					request);

			if (StringUtils.isBlank(mchIdStr)) {
				logger.error("must query one mchId info ,request has no mchId");
				return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
			}

			CacheMchUser cacheUser = MchStaffProductCacheManager.getInstance()
					.getCacheMchUserById(Integer.valueOf(mchIdStr));

			if (null == cacheUser) {
				logger.error("cache has no user ,mchId=" + mchIdStr);
				return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
			}
			
			model.put("cacheUser", cacheUser);

			return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
					PageConfigUtil.WechatUserPage.PRODUCTS_ONE_MCH_PAGE);

		} catch (Exception e) {
            logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}
    	
    }
    
    /**
     * 微信账户跳转单一商家界面 要查看排队信息这些
     */
    // TODO: 做成通用接口 根据type 跳转到不同界面； 参考 forwardSingelProductPageByType
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_SINGLE_MCH_URI)
    public ModelAndView toSingleMchPage(Map<String, Object> model,
    		HttpServletRequest request, HttpServletResponse response) {
    	
		try {
			// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
			// 不是测试模式
			if (!WinterOrangeSysConf.IN_TEST_MODEL) {
				// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
				CacheWechatUser cacheChatUser = ParseRequest
						.getWechatUserFromSession(request);
				if (null == cacheChatUser
						|| StringUtils.isBlank(cacheChatUser.getWechatUser()
								.getUserOpenid())) {
					logger.error("wechat user session timeout,re auth");
					WeChatRequestUtil.redirectToAuthorize(response);
					return null;
				}
			} else {
				logger.debug("current in test model");
			}

			logger.debug("wechat use to mch single page");
			String mchIdStr = ParseRequest.parseRequestByType("mchId", false,
					request);

			if (StringUtils.isBlank(mchIdStr)) {
				logger.error("must query one mchId info ,request has no mchId");
				return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
			}

			CacheMchUser cacheUser = MchStaffProductCacheManager.getInstance()
					.getCacheMchUserById(Integer.valueOf(mchIdStr));

			if (null == cacheUser) {
				logger.error("cache has no user ,mchId=" + mchIdStr);
				return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
			}

			String startTime = "10:00";
			String endTime = "21:00";
			@SuppressWarnings("unchecked")
			Map<String, Object> extProps = JSON.parseObject(cacheUser
					.getMchUser().getExtProps(), Map.class);
			if (null != extProps) {
				startTime = (String) extProps.get("start");
				startTime = StringUtils.isBlank(startTime) ? "10:00"
						: startTime;

				endTime = (String) extProps.get("end");
				endTime = StringUtils.isBlank(endTime) ? "21:00" : endTime;

			}

			model.put("start", startTime);
			model.put("end", endTime);

			// 计算下当前的服务状态
			caculateMchInservice(cacheUser, startTime, endTime);

			model.put("isFollow", "false");

			List<String> mchIds = (ArrayList) extProps.get("favoriteMchIds");
			if (null != mchIds && mchIds.size() > 0) {
				for (String s : mchIds) {
					if (s.equalsIgnoreCase(mchIdStr)) {
						model.put("isFollow", "true");
						break;
					}
				}
			}

			CacheMchUserQueueOrder mchOrder = QueueOrderCacheManager
					.getInstance().getCacheMchUserQueue(
							Integer.valueOf(mchIdStr));
			int waitingNum = (mchOrder == null) ? 0 : mchOrder
					.getWaitingQueue().size();

			model.put("cacheUser", cacheUser);
			model.put("waitingNum", waitingNum);

			return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
					PageConfigUtil.WechatUserPage.MCH_SINGLE_PAGE);

		} catch (Exception e) {
            logger.error(e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}
    }
    
    
    /**
     * 微信账户跳转到商品列表界面
     */
    @SuppressWarnings("unchecked")
	// TODO: 做成通用接口 根据type 跳转到不同界面； 参考 forwardSingelProductPageByType
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_PRODUCT_LIST_URI)
    public ModelAndView toProductListPage(Map<String, Object> model,
    		HttpServletRequest request, HttpServletResponse response){
    	
    	// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    	TbWechatUser chatUser = null ;
    	if(!WinterOrangeSysConf.IN_TEST_MODEL){
            // TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    		CacheWechatUser cacheChatUser = ParseRequest.getWechatUserFromSession(request);
    		if (null == cacheChatUser
    				|| StringUtils.isBlank(cacheChatUser.getWechatUser().getUserOpenid())) {
    			logger.error("wechat user session timeout,re auth");
    			WeChatRequestUtil.redirectToAuthorize(response);
    			return null;
    		}
    	}else{
    		logger.debug("current in test model,construct wechat user info ");
        	chatUser = new TbWechatUser();
        	chatUser.setUserOpenid("ycx06st");
    	}
    	
    	//判断该用户什么类型？ staff  mch 或者admin?  后续扩展
    	TbMchStaff staff = MchStaffProductCacheManager.getInstance().getStaff(null, chatUser.getUserOpenid());
    	if(null != staff){
    		logger.warn("the wechat user is staff ,to  staff transaciton page");
    		//该用户同时也是staff 跳转到员工的staff页面
    		return toStaffTransactionList(model,request,response,staff);
    	}
    	
    	//获取下微信用户是否是 mchUser
    	logger.info("reward to food list page");
    	wechatUserService.generateProductListByPage(request, model, response,
    			false);
    	String type = ParseRequest.parseRequestByType(ProductSearchCondition.ConditionName.TYPE, true, request);
    	if (null == type) {
    		type = ProductType.FOOD;
    	}
    	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
    			PageConfigUtil.WechatUserPage.PRODUCT_COMMON_PAGE + type + "/list");
    }
    
    
    //如果微信用户同时是员工，则需要跳转到员工的订单处理页面
    @SuppressWarnings("unchecked")
	private ModelAndView toStaffTransactionList(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response,TbMchStaff staff){
    	
    	//查询staff自己的订单，从数据库查
    	List<CacheTransaction> staffTransaction = wechatUserService.staffGetTransaction(model,staff , request, response);
    	
    	model.put("staff", staff);
    	model.put("cacheTransactions", staffTransaction);
    	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
                PageConfigUtil.WechatUserPage.TRANSACTION_LIST_STAFF_PAGE);
    }
    
    /**
     *  微信用户查看排队列表页
     * @param model
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_QUEUE_URI)
    public ModelAndView  toQueueOrderPage(ModelMap model,
            HttpServletRequest request, HttpServletResponse response){
        // TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    	//不是测试模式
    	CacheWechatUser cacheChatUser = null ;
    	if(!WinterOrangeSysConf.IN_TEST_MODEL){
            // TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    		cacheChatUser = ParseRequest.getWechatUserFromSession(request);
    		if (null == cacheChatUser
    				|| StringUtils.isBlank(cacheChatUser.getWechatUser().getUserOpenid())) {
    			logger.error("wechat user session timeout,re auth");
    			WeChatRequestUtil.redirectToAuthorize(response);
    			return null;
    		}
    	}else{
    		logger.debug("current in test model");
    		cacheChatUser = new CacheWechatUser();
        	TbWechatUser wechatUser = new TbWechatUser();
        	wechatUser.setUserOpenid("st0628");
        	cacheChatUser.setWechatUser(wechatUser);
    	}
    	
    	//判断该用户什么类型？ staff  mch 或者admin? 如果是mch 则跳转到整个商店的排队页面
    	TbMch mchUser = MchStaffProductCacheManager.getInstance().getMchUserByFkOpenId(cacheChatUser.getWechatUser().getUserOpenid());
    	if(null != mchUser){
    		logger.warn("the wechat user is mchUser ,to  mchUser queue page");
    		//该用户同时也是staff 跳转到员工的staff页面
    		return toMchQueueOrderList(model,request,response,mchUser);
    	}
    
    	//普通微信用户 则只查看自己的取号的排队情况
    	logger.info("reward to wechat user  self order page");
        return wechatUserService.querySelfQueueOrder(model, request, cacheChatUser);
    	
    }
    
    /**
     *  微信用户修改排队信息
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.ORDER_CHANGE_URI)
    public void  queueStatusChange(ModelMap model,
    		HttpServletRequest request, HttpServletResponse response){
    	// TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    	CacheWechatUser cacheChatUser = null ;
    	if(!WinterOrangeSysConf.IN_TEST_MODEL){
            // TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
    		cacheChatUser = ParseRequest.getWechatUserFromSession(request);
    		if (null == cacheChatUser
    				|| StringUtils.isBlank(cacheChatUser.getWechatUser().getUserOpenid())) {
    			logger.error("wechat user session timeout,re auth");
    			WeChatRequestUtil.redirectToAuthorize(response);
    			return ;
    		}
    	}else{
    		logger.debug("current in test model");
    		cacheChatUser = new CacheWechatUser();
        	TbWechatUser wechatUser = new TbWechatUser();
        	wechatUser.setUserOpenid("st0628");
        	cacheChatUser.setWechatUser(wechatUser);
    	}
    
    	wechatUserService.changeQueueOrder(model, request,response, cacheChatUser);
    }
    
    //微信登陆用户如果是商家，则跳转到商家的整个排队界面
	@SuppressWarnings("unused")
	private ModelAndView toMchQueueOrderList(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response, TbMch mch) {

		CacheMchUserQueueOrder cacheMchOrder = QueueOrderCacheManager.getInstance()
				.getCacheMchUserQueue(mch.getMchId());

		model.put("runningQueues", null);
		model.put("waitingQueues", null);
		model.put("outingQueues", null);
		
		if (null != cacheMchOrder) {
			model.put("runningQueues", cacheMchOrder.getRunningQueue());
			model.put("waitingQueues", cacheMchOrder.getWaitingQueue());
			model.put("outingQueues", cacheMchOrder.getOutingQueue());
		}

        return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
                PageConfigUtil.WechatUserPage.MCH_QUEUE_PAGE);
	}
	    
    /**
     * ajax 方式获取更多商品
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_MORE_PRODUCT_URI)
    public void getMoreProductList(Map<String, Object> model,
                                   HttpServletRequest request, HttpServletResponse response) {
        logger.info("get more food list page");

        wechatUserService
                .generateProductListByPage(request, model, response, true);
    }

    /**
     * 微信用户查询单个商品的接口
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_SINGLE_PRODUCT_URI)
    public ModelAndView toSingleProduct(Map<String, Object> model,
                                        HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    
    	CacheWechatUser cacheChatUser = null ;
    	if(!WinterOrangeSysConf.IN_TEST_MODEL){
    		cacheChatUser = ParseRequest.getWechatUserFromSession(request);
    		if (null == cacheChatUser
    				|| StringUtils.isBlank(cacheChatUser.getWechatUser().getUserOpenid())) {
    			logger.error("wechat user session timeout,re auth");
    			WeChatRequestUtil.redirectToAuthorize(response);
    			return null;
    		}
    	}else{
    		logger.debug("current in test model");
    		cacheChatUser = new CacheWechatUser();
        	TbWechatUser wechatUser = new TbWechatUser();
        	wechatUser.setUserOpenid("ycx0628st");
        	cacheChatUser.setWechatUser(wechatUser);
    	}
    	

        // TODO : 需要查询 所有的 美食类商家以及每个商家先的 美食种类
        logger.info("reward to single product page");
        CacheProduct cacheProduct = ParseRequest.findCacheProduct(request);
        if (cacheProduct == null) {
            logger.error("the product not found,redirect to 404 ");
            // 如果没有找到产品 直接跳转到 404 界面
            return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
        }

        // 放入缓存
        model.put("cacheProduct", cacheProduct);
        model.put("headPicture", cacheProduct.getProduct().getProductHeadPicture().replace("\\", "/"));
        model.put("commonPicturePath", IndexConfig.RELATIVE_PATH_PREFIX);
        model.put("contentPicts", ProductUtil
                .translateContentPathToArr(cacheProduct.getProduct()
                        .getProductContentPicture()));

        // 把访问次数加1
        synchronized (cacheProduct) {
            cacheProduct.getProduct().setLikeCounts(
                    cacheProduct.getProduct().getLikeCounts() + 1);
        }

        return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
                PageConfigUtil.WechatUserPage.PRODUCT_COMMON_PAGE
                        + cacheProduct.getProduct().getProductType() + "/single");

    }
    
    /**
     * 微信用户跳转至购买商品页面
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_BUYPRODUCT_URI)
    public ModelAndView toBuyProductPage(Map<String, Object> model,
                                         HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String productId = request.getParameter("productId");
        logger.debug("the productId=" + productId);
        model.put("productId", productId);
        return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
                PageConfigUtil.WechatUserPage.BUYPRODUCT_PAGE);
    }
    
    /**
     * 微信用户跳到付费取号界面
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_BUYQUEUEORDER_URI)
    public ModelAndView toBuyQueueOrderPage(Map<String, Object> model,
    		HttpServletRequest request, HttpServletResponse response)
    				throws IOException {
    	String mchId = request.getParameter("mchid");
    	logger.debug("the mchId=" + mchId);
    	model.put("mchid", mchId);
    	model.put("types", OrderTypeConfigEnum.values());
    	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
    			PageConfigUtil.WechatUserPage.BUY_QUEUE_ORDER_PAGE);
    }

    /**
     * 微信用户预支付
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.BUYPRODUCT_URI)
    public ModelAndView buyProduct(ModelMap model, HttpServletRequest request,
                                   HttpServletResponse response) {
        logger.info("now wechat user buy the product ....");
        return wechatUserService.buyProduct(model, request, response);
    }
    
    
    /**
     * 微信用户预支付取号
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.PAYFOR_QUEUEORDER_URI)
    public ModelAndView buyQueueOrder(ModelMap model, HttpServletRequest request,
    		HttpServletResponse response) {
    	logger.info("now wechat user payfor order ....");
    	return wechatUserService.payforQueueOrder(model, request, response);
    }

    /**
     * 用于处理微信支付回调的 入口
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.WECHAT_PAY_CALL_BACK)
    public void wechatPayCallBack(ModelMap model, HttpServletRequest request,
                                  HttpServletResponse response) {

        logger.info("now process wechat callback  ....");
        wechatUserService.processWechatPayBack(model, request, response);
        logger.info("finish process wechat call back ....");
    }

    /**
     * 微信用户反馈商家意见
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_CONTACTUS_URI)
    public ModelAndView toContactusPage(ModelMap map,
                                        Map<String, Object> model, HttpServletRequest request,
                                        HttpServletResponse response) {
        logger.info("to contact us");
        List<CacheMchUser> mchUsers = MchStaffProductCacheManager.getInstance()
                .getAllMchUsers();
        model.put("mchUsers", mchUsers);

        return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
                PageConfigUtil.WechatUserPage.CONTACTUS_PAGE);
    }
    
    /**
     * 微信用户跳转到自己关注的商家界面
     */
    @SuppressWarnings("rawtypes")
	@RequestMapping(PageConfigUtil.WechatUserPage.TO_FAVORITE_MCHS_URI)
    public ModelAndView toWechatFavoriteMchs(ModelMap map,
    		Map<String, Object> model, HttpServletRequest request,
    		HttpServletResponse response) {
    	logger.info("to wechat favorite mchs");
    	
    	CacheWechatUser cacheChatUser = null ;
    	if(!WinterOrangeSysConf.IN_TEST_MODEL){
    		cacheChatUser = ParseRequest.getWechatUserFromSession(request);
    		if (null == cacheChatUser
    				|| StringUtils.isBlank(cacheChatUser.getWechatUser().getUserOpenid())) {
    			logger.error("wechat user session timeout,re auth");
    			WeChatRequestUtil.redirectToAuthorize(response);
    			return null;
    		}
    	}else{
    		logger.debug("current in test model");
    		cacheChatUser = new CacheWechatUser();
        	TbWechatUser wechatUser = new TbWechatUser();
        	wechatUser.setUserOpenid("st0628");
        	cacheChatUser.setWechatUser(wechatUser);
    	}
    	
    	//从ext_props里面解析 字段
    	String extPropsStr = cacheChatUser.getWechatUser().getExtProps();
    	model.put("mchUsers", null);
    	if(StringUtils.isBlank(extPropsStr)){
    		return new ModelAndView();
    	}
    	
    	Map<String,Object> extPropsMap = JSON.parseObject(extPropsStr, Map.class);
    	
    	List<String> mchIds = (ArrayList)extPropsMap.get("favoriteMchIds");
    	if(null != mchIds && mchIds.size() >0){
    		List<TbMch> mchUsers = new ArrayList<TbMch>();
    		for(String s:mchIds){
    			TbMch temMch = MchStaffProductCacheManager.getInstance().getMchUserById(Integer.valueOf(s));
    			mchUsers.add(temMch);
    		}
    		
        	model.put("mchUsers", mchUsers);
    		
    	}
    
    	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
    			PageConfigUtil.WechatUserPage.FAVORITE_MCHS_PAGE);
    }
    
    
    /**
     * ajax 方式微信用户关注一个商家
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.FOLLOW_MCH_URI)
    public void updateMchIdForWechat(ModelMap model,
                                   HttpServletRequest request, HttpServletResponse response) {
        logger.info("wechat  follow  mch ");
        wechatUserService.wechatFollowOneMch(request, model, response);
    }
   
    /**
     * 微信用户查看自己兑换码
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_REDEMMCODE_URI)
    public ModelAndView toRedeemCodePage(ModelMap map,
    		Map<String, Object> model, HttpServletRequest request,
    		HttpServletResponse response) throws IOException {
    	logger.info("to redeemcode page ");
    	
    	TbWechatUser user = new TbWechatUser();
    	user.setUserOpenid("ycx0628st");
    	
    	request.getSession().setAttribute(IndexConfig.SESSION_WECHATUSER_KEY, user);
    	
    	List<CacheRedeemCode> redeemCodes = wechatUserService.generateRedeemCodeList(request, model);
    	
    	map.put("cacheRedeemCodes", redeemCodes);
    	
    	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
    			PageConfigUtil.WechatUserPage.REDEEMCODE_PAGE);
    }

    /**
     * 微信账户提交 意见
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.ADD_SUGGEST_URI)
    public void submitSuggestion(ModelMap map, Map<String, Object> model,
                                 HttpServletRequest request, HttpServletResponse response) {
        logger.info("submit suggestion");
        wechatUserService.addNewSuggestion(request, response);
    }
    
    
    /**
     * 微信用户跳转到订单页面
     */
    @RequestMapping(PageConfigUtil.WechatUserPage.TO_WECHT_TRANSACTIONS_URI)
    public ModelAndView toWechatTransactions(ModelMap map, Map<String, Object> model,
    		HttpServletRequest request, HttpServletResponse response) {
    	
    	logger.info("start query  wechat user transactions...");
    	
    	CacheWechatUser cacheChatUser = null ;
    	if(!WinterOrangeSysConf.IN_TEST_MODEL){
    		cacheChatUser = ParseRequest.getWechatUserFromSession(request);
    		if (null == cacheChatUser
    				|| StringUtils.isBlank(cacheChatUser.getWechatUser().getUserOpenid())) {
    			logger.error("wechat user session timeout,re auth");
    			WeChatRequestUtil.redirectToAuthorize(response);
    			return null;
    		}
    	}else{
    		logger.debug("current in test model");
    		cacheChatUser = new CacheWechatUser();
        	TbWechatUser wechatUser = new TbWechatUser();
        	wechatUser.setUserOpenid("st0628");
        	cacheChatUser.setWechatUser(wechatUser);
    	}
    	
    	
    	//判断该用户什么类型？ staff  mch 或者admin?  后续扩展
    	TbMchStaff staff = MchStaffProductCacheManager.getInstance().getStaff(null, cacheChatUser.getWechatUser().getUserOpenid());
    	if(null != staff){
    		logger.warn("the wechat user is staff ,to  staff transaciton page");
    		//该用户同时也是staff 跳转到员工的staff页面
    		return toStaffTransactionList(model,request,response,staff);
    	}
    	
    	List<CacheTransaction> cacheTransactions = wechatUserService.wechatGetTransactions(model, cacheChatUser.getWechatUser(), request, response);
    	model.put("cacheTransactions", cacheTransactions);
    	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
                PageConfigUtil.WechatUserPage.TRANSACTION_LIST_WECHAT_PAGE);
    	
    }
    
    /**
     * wechat用户查看单一订单信息
     */
    @SuppressWarnings("unused")
	@RequestMapping(PageConfigUtil.WechatUserPage.TO_WECHT_SINGLE_TRANS_URI)
    public ModelAndView toWechatsingleTrans(ModelMap map, Map<String, Object> model,
    		HttpServletRequest request, HttpServletResponse response) {
    	logger.info("start query  wechat user transactions...");
    	
    	CacheWechatUser cacheChatUser = null ;
    	if(!WinterOrangeSysConf.IN_TEST_MODEL){
    		cacheChatUser = ParseRequest.getWechatUserFromSession(request);
    		if (null == cacheChatUser
    				|| StringUtils.isBlank(cacheChatUser.getWechatUser().getUserOpenid())) {
    			logger.error("wechat user session timeout,re auth");
    			WeChatRequestUtil.redirectToAuthorize(response);
    			return null;
    		}
    	}else{
    		logger.debug("current in test model");
    		cacheChatUser = new CacheWechatUser();
    		TbWechatUser wechatUser = new TbWechatUser();
    		wechatUser.setUserOpenid("st0628");
    		cacheChatUser.setWechatUser(wechatUser);
    	}
    	
 		
		String recordId = ParseRequest.parseRequestByType("recordId",
				false, request);
		if (StringUtils.isBlank(recordId)) {
			logger.error("must query one transaction info ,request has no recordId");
			return new ModelAndView(PageConfigUtil.ERROR_404_PAGE);
		}
		
		@SuppressWarnings("unchecked")
		TbTransactionRecord record;
		try {
			record = (TbTransactionRecord) wechatUserService
			.getById(TbTransactionRecord.class,Integer.valueOf(recordId));
		} catch (NumberFormatException e) {
			logger.error("format  error ,e"+e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		} catch (HappyLifeException e) {
			logger.error("get record error ,id="+recordId+","+e.getMessage());
			return new ModelAndView(PageConfigUtil.ERROR_500_PAGE);
		}
		
		if(!WinterOrangeSysConf.IN_TEST_MODEL && 
				(null == cacheChatUser || !record.getFkOpenId().equals(cacheChatUser.getWechatUser().getUserOpenid()))){
			logger.error("the current wechat use have no pernision ,can not query transaction id="+recordId);
			return new ModelAndView(PageConfigUtil.ERROR_403_PAGE);
		}

		String tmpProductName = null;
		String tmpMchShopName = null;
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

		tmpProductName = manager.getProductById(record.getFkProductId())
				.getProduct().getProductName();
		tmpCacheTransaction.setProductName(tmpProductName);

		tmpMchShopName = manager.getProductById(record.getFkProductId())
				.getMchUser().getShopName();

		tmpCacheTransaction.setMchShopName(tmpMchShopName);
	
		String extPropsStr = record.getExtProps();
		Map<String, Object> extPropsMap = null ;
		if(StringUtils.isNotBlank(extPropsStr)){
			extPropsMap = JSON.parseObject(extPropsStr, Map.class);
		}
		extPropsMap = (extPropsMap == null)? new HashMap():extPropsMap;
		tmpCacheTransaction.setProcessMsg((String)extPropsMap.get("detail"));
		String contentPictures = (String)extPropsMap.get("contentPicture");
		String[] pictures = null ;
		if(StringUtils.isNotBlank(contentPictures)){
		   pictures = contentPictures.replace("\\", "/").split(StrUtil.SPLIT_STR);
		}
		
		tmpCacheTransaction.setContentPictures(pictures);
		model.put("cacheTransaction", tmpCacheTransaction);
    	return new ModelAndView(PageConfigUtil.WECHAT_PREFIX+
    			PageConfigUtil.WechatUserPage.TRANSACTION_WECHAT_ONE_PAGE);
    	
    }
    
    //判断当前用户是否在服务时间段内
    private void caculateMchInservice(CacheMchUser cacheUser,String startTime,String endTime){
    	
    	//需要确认商家是否当前支持取号，如果serviceStatus不是正在营业中，或者 当前取号时间 已经过了 商家设置的服务时间，则 不支持取号
    	if(!cacheUser.getMchUser().getServiceStatus().equalsIgnoreCase(MchServiceStatusEnum.IN_SERVICE.getStatusMsg())){
    		cacheUser.setInService(false);
    		return ;
    	}
    	
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");  
		String currentTime = formatter.format(new Date()); 
		
		if(currentTime.compareTo(startTime)>=0 && currentTime.compareTo(endTime)<=0)
		{
			cacheUser.setInService(true);
		}else{
			cacheUser.setInService(false);
		}
    }
    

}