package happylife.model;

// Generated 2016-5-21 15:47:52 by Hibernate Tools 3.4.0.CR1

import java.util.Date;

/**
 * TbWechatUser generated by hbm2java
 */
public class TbWechatUser implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2760496399320322775L;
	private Integer userId;
	private String userNickName;
	private String userHeadPath;
	private String userTelNum;
	private String userOpenid;
	private Boolean userSex;
	private int userRewardPoint;
	private int userLevel;
	private Date userRegisterTime;
	private String extProps;

	public TbWechatUser() {
	}

	public TbWechatUser(int userRewardPoint, int userLevel,
			Date userRegisterTime) {
		this.userRewardPoint = userRewardPoint;
		this.userLevel = userLevel;
		this.userRegisterTime = userRegisterTime;
	}

	public TbWechatUser(String userNickName, String userHeadPath,
			String userTelNum, String userOpenid, Boolean userSex,
			int userRewardPoint, int userLevel, Date userRegisterTime) {
		this.userNickName = userNickName;
		this.userHeadPath = userHeadPath;
		this.userTelNum = userTelNum;
		this.userOpenid = userOpenid;
		this.userSex = userSex;
		this.userRewardPoint = userRewardPoint;
		this.userLevel = userLevel;
		this.userRegisterTime = userRegisterTime;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserNickName() {
		return this.userNickName;
	}

	public void setUserNickName(String userNickName) {
		this.userNickName = userNickName;
	}

	public String getUserHeadPath() {
		return this.userHeadPath;
	}

	public void setUserHeadPath(String userHeadPath) {
		this.userHeadPath = userHeadPath;
	}

	public String getUserTelNum() {
		return this.userTelNum;
	}

	public void setUserTelNum(String userTelNum) {
		this.userTelNum = userTelNum;
	}

	public String getUserOpenid() {
		return this.userOpenid;
	}

	public void setUserOpenid(String userOpenid) {
		this.userOpenid = userOpenid;
	}

	public Boolean getUserSex() {
		return this.userSex;
	}

	public void setUserSex(Boolean userSex) {
		this.userSex = userSex;
	}

	public int getUserRewardPoint() {
		return this.userRewardPoint;
	}

	public void setUserRewardPoint(int userRewardPoint) {
		this.userRewardPoint = userRewardPoint;
	}

	public int getUserLevel() {
		return this.userLevel;
	}

	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	public Date getUserRegisterTime() {
		return this.userRegisterTime;
	}

	public void setUserRegisterTime(Date userRegisterTime) {
		this.userRegisterTime = userRegisterTime;
	}

	public String getExtProps() {
		return extProps;
	}

	public void setExtProps(String extProps) {
		this.extProps = extProps;
	}

	@Override
	public String toString() {
		return "TbWechatUser [userId=" + userId + ", userNickName="
				+ userNickName + ", userHeadPath=" + userHeadPath
				+ ", userTelNum=" + userTelNum + ", userOpenid=" + userOpenid
				+ ", userSex=" + userSex + ", userRewardPoint="
				+ userRewardPoint + ", userLevel=" + userLevel
				+ ", userRegisterTime=" + userRegisterTime + ", extProps="
				+ extProps + "]";
	}
	
}