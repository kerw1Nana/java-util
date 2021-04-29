package org.kerw1n.javautil.format;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 反射工具类
 *
 * @author Guan Yonchao
 */
public class ReflectionUtil {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * 校验对象是否有属性为空
     *
     * @param obj
     * @return 是否包含为空属性
     */
    public static boolean hasNullFiled(Object obj) {
        return hasNullFiled(obj, EMPTY_STRING_ARRAY);
    }

    /**
     * 校验对象是否有属性为空
     *
     * @param obj
     * @return 是否包含为空属性
     */
    public static boolean hasNullFiled(Object obj, String... excludeFiled) {
        Objects.requireNonNull(obj);
        int excludeLen = excludeFiled != null ? excludeFiled.length : 0;

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            try {
                for (Field f : fields) {
                    f.setAccessible(true);
                    if (excludeLen != 0 && ObjectUtil.contains(excludeFiled, f.getName())) {
                        continue;
                    }
                    if (f.get(obj) == null || "".equals(f.get(obj))) {
                        return true;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取对象中所有属性
     *
     * @param clazz
     * @return
     */
    public static Field[] getClassFields(Class<?> clazz) {
        if (Object.class.getName().equals(clazz.getName())) {
            return null;
        }
        return clazz.getDeclaredFields();
    }

    /**
     * 获取对象中指定的属性
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field getClassField(Class<?> clazz, String fieldName) {
        Field[] fields = getClassFields(clazz);
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            return getClassField(superClass, fieldName);
        }
        return null;
    }
}
