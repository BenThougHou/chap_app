package com.yiliao.dao.util;

/**
 * 检索命令后面的条件参数
 * @author ZhouShuhua
 */
public class SelectParam {
	
	private String key;
	private String value;
	private Operator operator;
	
	/**
	 * @author ZhouShuhua
	 */
	public enum Operator {
		EQ, 	// 等于
		NEQ, 	// 不等于
		LT, 	// 小于
		GT, 	// 大于
		LIKE, 	// 模糊
		FLIKE, 	// 前方匹配
		RLIKE  	// 后方匹配
	}
	
	// 构造函数
	public SelectParam(String key, Operator operator, String value) {
		this.key = key;
		this.operator = operator;
		this.value = value;
	}
	
	public SelectParam() {};
	
	// setter / getter
	public String getKey() {
		return key;
	}

	public SelectParam setKey(String key) {
		this.key = key;
		return this;
	}

	public String getValue() {
		return value;
	}

	public SelectParam setValue(String value) {
		this.value = value;
		return this;
	}

	public Operator getOperator() {
		return operator;
	}

	public SelectParam setOperator(Operator operator) {
		this.operator = operator;
		return this;
	}

	public String toString() {
		
		StringBuilder str = new StringBuilder(this.getKey());
		// 判断匹配方式
		switch(this.operator) {
			case EQ : str.append("=?"); break;
			case NEQ : str.append("!=?"); break;
			case LT : str.append("<?"); break;
			case GT : str.append(">?"); break;
			case LIKE : 
			case FLIKE : 
			case RLIKE : 
				str.append(" like ?");
				break; 
			default:str.append("=?");break;
		}
		str.append(this.getValue());
		return str.toString();
	}
}
