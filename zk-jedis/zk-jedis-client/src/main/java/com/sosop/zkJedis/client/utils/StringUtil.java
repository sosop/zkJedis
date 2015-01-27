package com.sosop.zkJedis.client.utils;
/**
 * 
 * @author xiaolong.hou
 * 便于字符串拼接
 */
public class StringUtil {
	
	public static String append(Object...args) {
		StringBuffer buf = new StringBuffer();
		if(args != null && args.length > 0) {
			for (Object obj : args) {
				buf.append(obj);
			}
		}
		return buf.toString();
	}
	
	public static boolean isNull(String ...str) {
		boolean nil = true;
		if(null != str && str.length > 0) {
			for (String s : str) {
				// 只要有一个元素不为空，那么数组就不为空
				if(null != s && !"".equals(s.trim())) {
					nil = false; 
					break;
				}
			}
		}
		return nil;
	}
	
	public static boolean notNull(String ...str) {
		return !isNull(str);
	}
}