package org.kerw1n.javautil.format;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Object工具类
 * <p>
 * 基于jackson {@link ObjectMapper}实现Object转换.
 * 数组更多操作可直接调用{@link Arrays}提供的API.
 *
 * @author : kerw1n
 * @see ObjectMapper
 **/
public class ObjectUtil {

    private ObjectUtil() {
    }

    /**
     * 判断对象是否为空
     *
     * @param o
     * @return
     */
    public static boolean isNull(Object o) {
        return null == o;
    }

    /**
     * 判断对象是否不为空
     *
     * @param o
     * @return
     */
    public static boolean isNotNull(Object o) {
        return !isNull(o);
    }

    /**
     * 判断两个对象是否相同
     *
     * @param o1
     * @param o2
     * @return
     */
    public static boolean equals(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * 判断数组是否为空
     *
     * @param arr Object类型
     * @return
     */
    public static boolean isEmpty(Object[] arr) {
        return null == arr || arr.length == 0;
    }

    /**
     * 判断数组是否为空
     *
     * @param arr Object类型
     * @return
     */
    public static boolean isNotEmpty(Object[] arr) {
        return !isEmpty(arr);
    }

    /**
     * 判断数组是否包含指定元素
     *
     * @param arr   请确保参数均不为空,否则抛出{@code java.lang.NullPointerException}异常
     * @param value
     * @return
     */
    public static boolean contains(Object[] arr, Object value) {
        Objects.requireNonNull(arr);
        return Arrays.asList(arr).contains(value);
    }

    private static class ObjectMapperInner {
        private static ObjectMapper mapper = new ObjectMapper();

        static {
            // 日期格式
            mapper.setDateFormat(SimpleDateFormatFactory.getInstance());
            // 配置当json中存在的字段而实体中不存在时不报错
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return ObjectMapperInner.mapper;
    }

    /**
     * Object转Map
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> objectToMap(Object obj) throws IOException {
        return jsonToMap(getMapper().writeValueAsString(Objects.requireNonNull(obj)));
    }

    /**
     * Json转Map
     *
     * @param json
     * @return
     * @throws IOException
     */
    public static Map<String, Object> jsonToMap(String json) throws IOException {
        return getMapper().readValue(json, Map.class);
    }

    /**
     * Map转Object
     *
     * @param map
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T mapToObject(Map map, Class<?> clazz) throws IOException {
        return jsonToObject(getMapper().writeValueAsString(map), clazz);
    }

    /**
     * Json转Object
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T jsonToObject(String json, Class<?> clazz) throws IOException {
        return (T) getMapper().readValue(json, clazz);
    }

    /**
     * Object转Json
     *
     * @param obj
     * @return
     * @throws IOException
     */
    public static String objectToJson(Object obj) throws IOException {
        return getMapper().writeValueAsString(Objects.requireNonNull(obj));
    }

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    public static byte[] serialize(Object obj) throws IOException {
        byte[] bytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            bytes = baos.toByteArray();
        }
        return bytes;
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public static Object unSerialize(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            obj = ois.readObject();
        }
        return obj;
    }
}
