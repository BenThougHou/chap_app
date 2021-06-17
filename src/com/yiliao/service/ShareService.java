package com.yiliao.service;

import java.util.Map;

import com.yiliao.util.MessageUtil;

public interface ShareService {


   /**
    * 保存设备信息
    * @param userId
    * @param equipment
    * @param system_moble
    * @param ipAddress
    */
   public void	addShareInfo(int userId,String equipment,String system_moble,String ipAddress);
   
   /**
    * 添加分享次数
    * @param userId
    * @return
    */
   public MessageUtil addShareCount(int userId);
   
   /**
    * 获取下载地址
    */
   Map<String, Object> getDownLoadUrl();
   /**
    * 获取分享链接
    * @return
    */
   MessageUtil  getSpreadUrl(int userId);
   
   public void getIMInfo(Map<String, Object> params);

public Map<String, Object> getPacDownLoadUrl(String parameter);

public Map<String, Object> getDownLoadUrlList(int t_effect_type);
}
