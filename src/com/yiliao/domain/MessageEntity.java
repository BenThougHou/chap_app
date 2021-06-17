package com.yiliao.domain;

import java.io.Serializable;
import java.util.Date;

public class MessageEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** 主键 */
	private int t_id;
	/** 用户 */
	private int t_user_id;
	/** 通知内容 */
	private String t_message_content;
	/** 是否查看  0.未查看 1.已查看*/
	private int t_is_see;
	/** 时间 */
	private Date t_create_time;
	/**推送类型 -1 无 */
	private int pushType;
	/**房间号 */
	private int roomId;
	/**链接人编号*/
	private int connectUserId;
	/** 是否可以进行聊天*/
	private int satisfy;
	
	public MessageEntity() {
		// TODO Auto-generated constructor stub
	}

	public MessageEntity(int t_user_id, String t_message_content, int t_is_see, Date t_create_time, int pushType) {
		super();
		this.t_user_id = t_user_id;
		this.t_message_content = t_message_content;
		this.t_is_see = t_is_see;
		this.t_create_time = t_create_time;
		this.pushType = pushType;
	}


	public MessageEntity(int t_id, int t_user_id, String t_message_content, int t_is_see, Date t_create_time) {
		super();
		this.t_id = t_id;
		this.t_user_id = t_user_id;
		this.t_message_content = t_message_content;
		this.t_is_see = t_is_see;
		this.t_create_time = t_create_time;
	}

	public MessageEntity(int t_user_id, String t_message_content, int t_is_see, Date t_create_time) {
		super();
		this.t_user_id = t_user_id;
		this.t_message_content = t_message_content;
		this.t_is_see = t_is_see;
		this.t_create_time = t_create_time;
	}
	
	public MessageEntity(int t_user_id, String t_message_content, int t_is_see, Date t_create_time, int pushType,
			int roomId, int connectUserId, int satisfy) {
		super();
		this.t_user_id = t_user_id;
		this.t_message_content = t_message_content;
		this.t_is_see = t_is_see;
		this.t_create_time = t_create_time;
		this.pushType = pushType;
		this.roomId = roomId;
		this.connectUserId = connectUserId;
		this.satisfy = satisfy;
	}

	public int getT_id() {
		return t_id;
	}

	public void setT_id(int t_id) {
		this.t_id = t_id;
	}

	public int getT_user_id() {
		return t_user_id;
	}

	public void setT_user_id(int t_user_id) {
		this.t_user_id = t_user_id;
	}

	public String getT_message_content() {
		return t_message_content;
	}

	public void setT_message_content(String t_message_content) {
		this.t_message_content = t_message_content;
	}

	public int getT_is_see() {
		return t_is_see;
	}

	public void setT_is_see(int t_is_see) {
		this.t_is_see = t_is_see;
	}

	public Date getT_create_time() {
		return t_create_time;
	}

	public void setT_create_time(Date t_create_time) {
		this.t_create_time = t_create_time;
	}

	public int getPushType() {
		return pushType;
	}

	public void setPushType(int pushType) {
		this.pushType = pushType;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getConnectUserId() {
		return connectUserId;
	}

	public void setConnectUserId(int connectUserId) {
		this.connectUserId = connectUserId;
	}

	public int getSatisfy() {
		return satisfy;
	}

	public void setSatisfy(int satisfy) {
		this.satisfy = satisfy;
	}
	
	
	
}
