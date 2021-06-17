package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface AdTableService {

	MessageUtil getAdTable(int type, int page, int size,int userId);

}
