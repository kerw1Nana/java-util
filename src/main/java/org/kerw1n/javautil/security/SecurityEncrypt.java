package org.kerw1n.javautil.security;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.kerw1n.javautil.constant.BaseConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

/**
 * 安全加密工具类
 *
 * @author Guan Yonchao
 */
public class SecurityEncrypt {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityEncrypt.class);

    /**
     * 算法名称/加密模式/数据填充方式
     */
    private static final String ALGORITHM_PKCS5 = "AES/ECB/PKCS5Padding";

    private static final String ALGORITHM_PKCS7 = "AES/ECB/PKCS7Padding";

    private static final String ALGORITHM_AES = "AES";

    private static final String ALGORITHM_MD5 = "MD5";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SecurityEncrypt() {
    }

    /**
     * AES 加密
     *
     * @param content 内容
     * @param key     密钥
     * @return 成功返回加密串，失败返回待加密串
     */
    public static String aesEncrypt(String content, String key) {
        try {
            KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM_AES);
            gen.init(128);
            Cipher cipher = Cipher.getInstance(ALGORITHM_PKCS5);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] b = cipher.doFinal(content.getBytes(BaseConst.CHARSET_UTF8));
            // base64转码,避免中文乱码
            return Base64.encodeBase64String(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * AES 解密
     *
     * @param encrypt 加密串
     * @param key     密钥
     * @return 成功返回明文串，失败返回加密串
     */
    public static String aesDecrypt(String encrypt, String key) {
        try {
            KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM_AES);
            gen.init(128);
            Cipher cipher = Cipher.getInstance(ALGORITHM_PKCS5);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), ALGORITHM_AES));
            byte[] encryptBytes = Base64.decodeBase64(encrypt);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            return new String(decryptBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypt;
    }

    /**
     * AES/ECB/PKCS7Padding 解码
     *
     * @param key
     * @return
     */
    public static String decryptWithPKCS7(String data, String key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_PKCS7, "BC");
            // 生成加密解密需要的Key
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM_AES);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = cipher.doFinal(new String(java.util.Base64.getDecoder().decode(data), StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.ISO_8859_1));
            return new String(decoded, BaseConst.CHARSET_UTF8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * MD5 加密
     *
     * @param data 内容
     * @return 32位小写加密串
     * @throws Exception
     */
    public static String md5(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM_MD5);
        byte[] array = md.digest(data.getBytes(BaseConst.CHARSET_UTF8));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    /**
     * MD5 加密
     *
     * @param data
     * @return 32位大写加密串
     * @throws Exception
     */
    public static String MD5(String data) throws Exception {
        return md5(data).toUpperCase();
    }
    
}
