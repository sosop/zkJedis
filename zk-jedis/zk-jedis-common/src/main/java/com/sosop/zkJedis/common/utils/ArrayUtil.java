package com.sosop.zkJedis.common.utils;

/**
 * 
 * 创建人: sosop
 * 
 * 创建时间：Jan 30, 2015 11:20:20 AM
 * 
 * @ClassName: ArrayUtil
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
public class ArrayUtil {
    public static boolean isMatch(String[] arr, String v) {
        for (String s : arr) {
            if (v.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNull(Object[] objs) {
        if (objs == null || objs.length == 0) {
            return true;
        }
        return false;
    }

    public static boolean notNull(Object[] objs) {
        return !isNull(objs);
    }
}
