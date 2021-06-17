package com.yiliao.util;

import java.util.List;

/**
 * 分页信息
 * 
 * @author ZhouShuhua
 * @datetime 2013.5.26
 * @param <T>
 */
public class PageInfo<T> {

	private int pagenum; // 当前页数
	private long rowsCount; // 总的条数
	private int pagesize; // 显示条数
	private List<T> list; // 返回的结果集
	private String operateCaption; // 对应前台的按钮名称
	private String operateName; // 对应前台的按钮事件名称
	private String operatePara; // 对应前台按钮事件的参数值
	private String showFun;//对应前台按钮名称
	private String ico;
	private String linkName;
	private String linkPara;
	private String functionTerm;

	public PageInfo() {
	};

	public PageInfo(int pagesize, int pagenum) {
		this.pagenum = pagenum;
		this.pagesize = pagesize;
	}

	// setter/getter
	public int getPagenum() {
		return pagenum;
	}

	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}

	public long getRowsCount() {
		return rowsCount;
	}

	public void setRowsCount(long rowsCount) {
		this.rowsCount = rowsCount;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public String getOperateCaption() {
		return operateCaption;
	}

	public void setOperateCaption(String operateCaption) {
		this.operateCaption = operateCaption;
	}

	public String getOperateName() {
		return operateName;
	}

	public void setOperateName(String operateName) {
		this.operateName = operateName;
	}

	public String getOperatePara() {
		return operatePara;
	}

	public void setOperatePara(String operatePara) {
		this.operatePara = operatePara;
	}

	public String getIco() {
		return ico;
	}

	public void setIco(String ico) {
		this.ico = ico;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getLinkPara() {
		return linkPara;
	}

	public void setLinkPara(String linkPara) {
		this.linkPara = linkPara;
	}

	public String getFunctionTerm() {
		return functionTerm;
	}

	public void setFunctionTerm(String functionTerm) {
		this.functionTerm = functionTerm;
	}

	public String getShowFun() {
		return showFun;
	}

	public void setShowFun(String showFun) {
		this.showFun = showFun;
	}
	

	// public String getOperateCaption() {
	// return operateCaption;
	// }
	//
	// public void setOperateCaption(String operateCaption) {
	// this.operateCaption = operateCaption;
	// }
	//
	// public String getOperateName() {
	// return operateName;
	// }
	//
	// public void setOperateName(String operateName) {
	// this.operateName = operateName;
	// }
	//
	// public String getOperatePara() {
	// return operatePara;
	// }
	//
	// public void setOperatePara(String operatePara) {
	// this.operatePara = operatePara;
	// }
	//
	// public String getIco() {
	// return ico;
	// }
	//
	// public void setIco(String ico) {
	// this.ico = ico;
	// }
	//
	// public String getLinkName() {
	// return linkName;
	// }
	//
	// public void setLinkName(String linkName) {
	// this.linkName = linkName;
	// }
	//
	// public String getLinkPara() {
	// return linkPara;
	// }
	//
	// public void setLinkPara(String linkPara) {
	// this.linkPara = linkPara;
	// }
	//
	// public String getFunctionTerm() {
	// return functionTerm;
	// }
	//
	// public void setFunctionTerm(String functionTerm) {
	// this.functionTerm = functionTerm;
	// }

	// private String pagenum; //当前页面值
	// private String rowsCount; //传给前台的总条数值
	//	
	// public String getOperateCaption() {
	// return operateCaption;
	// }
	// public void setOperateCaption(String operateCaption) {
	// this.operateCaption = operateCaption;
	// }
	// public String getOperateName() {
	// return operateName;
	// }
	// public void setOperateName(String operateName) {
	// this.operateName = operateName;
	// }
	// public String getOperatePara() {
	// return operatePara;
	// }
	// public void setOperatePara(String operatePara) {
	// this.operatePara = operatePara;
	// }
	// public String getIco() {
	// return ico;
	// }
	// public void setIco(String ico) {
	// this.ico = ico;
	// }
	// public String getLinkName() {
	// return linkName;
	// }
	// public void setLinkName(String linkName) {
	// this.linkName = linkName;
	// }
	// public String getLinkPara() {
	// return linkPara;
	// }
	// public void setLinkPara(String linkPara) {
	// this.linkPara = linkPara;
	// }
	// public String getFunctionTerm() {
	// return functionTerm;
	// }
	// public void setFunctionTerm(String functionTerm) {
	// this.functionTerm = functionTerm;
	// }
	// public String getPagenum() {
	// return pagenum;
	// }
	// public void setPagenum(String pagenum) {
	// this.pagenum = pagenum;
	// }
	// public String getRowsCount() {
	// return rowsCount;
	// }
	// public void setRowsCount(String rowsCount) {
	// this.rowsCount = rowsCount;
	// }
}
