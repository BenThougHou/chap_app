package com.yiliao.util;

public class KeyWordUtil {

	/**
	 *  过滤字符串  出现关键字的地方已*号代替
	 * @param keyWords
	 * @param str
	 * @return
	 */
	public static String  filterKeyWord(String[] keyWords,String str) {

		for(String s : keyWords) {
			if(str.contains(s)){
				char[] str_char = s.toCharArray();
				for(char c : str_char) {
					str = str.replaceAll(String.valueOf(c), "*");
				}
			}
		}
		return str;
	}
	 
}
