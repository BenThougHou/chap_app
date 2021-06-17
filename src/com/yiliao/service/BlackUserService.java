package com.yiliao.service;

import com.yiliao.util.MessageUtil;

import net.sf.json.JSONObject;

public interface BlackUserService {

	MessageUtil addBlackUser(String t_user_id,String t_cover_user_id);

	MessageUtil delBlackUser(String userId,String t_id);

	MessageUtil getBlackUserList(String userId,int page,int size);

}
