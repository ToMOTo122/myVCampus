package com.vcampus.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用工具类
 */
public class CommonUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 将Object转换为指定类型的List
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> convertToGenericList(Object obj, Class<T> clazz) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            List<T> result = new ArrayList<>();
            for (Object item : list) {
                if (clazz.isInstance(item)) {
                    result.add(clazz.cast(item));
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}