package org.kerw1n.javautil.format;

import org.slf4j.helpers.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * 字符串格式化替换.
 *
 * @author kerw1n
 * @see {@link org.slf4j.helpers.MessageFormatter}
 */
public class MessageFormatter {
    static final char DELIM_START = '{';
    static final char DELIM_STOP = '}';
    static final String DELIM_STR = "{}";
    private static final char ESCAPE_CHAR = '\\';

    /**
     * {}快速替换.
     *
     * @param messagePattern 原字符串
     * @param argArray       参数
     * @return 替换后的字符串
     */
    public static String format(final String messagePattern, final Object... argArray) {

        int i = 0;
        int j;
        StringBuilder sb = new StringBuilder(messagePattern.length() + 50);

        int l;
        for (l = 0; l < argArray.length; l++) {

            j = messagePattern.indexOf(DELIM_STR, i);

            // no more variables
            if (j == -1) {
                if (i == 0) {
                    return messagePattern;
                } else {  // add the remaining string
                    sb.append(messagePattern, i, messagePattern.length());
                    return sb.toString();
                }
            } else {
                if (isEscapedDelimeter(messagePattern, j)) {
                    if (!isDoubleEscaped(messagePattern, j)) {
                        l--;
                        sb.append(messagePattern, i, j - 1);
                        sb.append(DELIM_START);
                        i = j + 1;
                    } else {
                        sb.append(messagePattern, i, j - 1);
                        deeplyAppendParameter(sb, argArray[l], new HashMap<>());
                        i = j + 2;
                    }
                } else {
                    // normal case
                    sb.append(messagePattern, i, j);
                    deeplyAppendParameter(sb, argArray[l], new HashMap<>());
                    i = j + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        sb.append(messagePattern, i, messagePattern.length());
        return sb.toString();
    }

    private static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
        return delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR;
    }

    private static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {
        if (delimeterStartIndex == 0) {
            return false;
        }
        char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
        return potentialEscape == ESCAPE_CHAR;
    }

    private static void deeplyAppendParameter(StringBuilder sb, Object o, Map<Object[], Object> seenMap) {
        if (o == null) {
            sb.append("null");
            return;
        }
        if (!o.getClass().isArray()) {
            safeObjectAppend(sb, o);
        } else {
            // check for basic array types,because they cannot be cast to Object[]
            if (o instanceof boolean[]) {
                booleanArrayAppend(sb, (boolean[]) o);
            } else if (o instanceof byte[]) {
                byteArrayAppend(sb, (byte[]) o);
            } else if (o instanceof char[]) {
                charArrayAppend(sb, (char[]) o);
            } else if (o instanceof short[]) {
                shortArrayAppend(sb, (short[]) o);
            } else if (o instanceof int[]) {
                intArrayAppend(sb, (int[]) o);
            } else if (o instanceof long[]) {
                longArrayAppend(sb, (long[]) o);
            } else if (o instanceof float[]) {
                floatArrayAppend(sb, (float[]) o);
            } else if (o instanceof double[]) {
                doubleArrayAppend(sb, (double[]) o);
            } else {
                objectArrayAppend(sb, (Object[]) o, seenMap);
            }
        }
    }


    private static void safeObjectAppend(StringBuilder sb, Object o) {
        try {
            String oAsString = o.toString();
            sb.append(oAsString);
        } catch (Throwable t) {
            Util.report("Failed toString() invocation on an object of type [" + o.getClass().getName() + "]", t);
            sb.append("[FAILED toString()]");
        }

    }

    private static void objectArrayAppend(StringBuilder sb, Object[] a, Map<Object[], Object> seenMap) {
        sb.append('[');
        if (!seenMap.containsKey(a)) {
            seenMap.put(a, null);
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                deeplyAppendParameter(sb, a[i], seenMap);
                if (i != len - 1) {
                    sb.append(", ");
                }
            }
            // allow key repeats
            seenMap.remove(a);
        } else {
            sb.append("...");
        }
        sb.append(']');
    }


    private static void booleanArrayAppend(StringBuilder sb, boolean[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

    private static void byteArrayAppend(StringBuilder sb, byte[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

    private static void charArrayAppend(StringBuilder sb, char[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

    private static void shortArrayAppend(StringBuilder sb, short[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

    private static void intArrayAppend(StringBuilder sb, int[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

    private static void longArrayAppend(StringBuilder sb, long[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

    private static void floatArrayAppend(StringBuilder sb, float[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

    private static void doubleArrayAppend(StringBuilder sb, double[] a) {
        sb.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sb.append(a[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
    }

}
