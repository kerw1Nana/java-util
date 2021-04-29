package org.kerw1n.javautil.security;

import org.apache.commons.codec.binary.Base64;
import org.kerw1n.javautil.constant.BaseConst;
import org.kerw1n.javautil.file.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA 加密工具类
 * <p>
 * 该算法于1977年由美国麻省理工学院MIT(Massachusetts Institute of Technology)的Ronal Rivest，Adi Shamir和Len
 * Adleman三位年轻教授提出，并以三人的姓氏Rivest，Shamir和Adlernan命名为RSA算法，是一个支持变长密钥的公共密钥算法，需要加密的文件快的长度也是可变的!
 * <p>
 * 所谓RSA加密算法，是世界上第一个非对称加密算法，也是数论的第一个实际应用。它的算法如下：
 * <p>
 * 1.找两个非常大的质数p和q（通常p和q都有155十进制位或都有512十进制位）并计算n=pq，k=(p-1)(q-1)。
 * <p>
 * 2.将明文编码成整数M，保证M不小于0但是小于n。
 * <p>
 * 3.任取一个整数e，保证e和k互质，而且e不小于0但是小于k。加密钥匙（称作公钥）是(e, n)。
 * <p>
 * 4.找到一个整数d，使得ed除以k的余数是1（只要e和n满足上面条件，d肯定存在）。解密钥匙（称作密钥）是(d, n)。
 * <p>
 * 加密过程： 加密后的编码C等于M的e次方除以n所得的余数。
 * <p>
 * 解密过程： 解密后的编码N等于C的d次方除以n所得的余数。
 * <p>
 * 只要e、d和n满足上面给定的条件。M等于N。
 * <p>
 * --------------------------------------------**********--------------------------------------------
 *
 * @author Guan Yonchao
 */
public class RSAEncrypt {

    private static final Logger LOG = LoggerFactory.getLogger(RSAEncrypt.class);

    private static final String ALGORITHM_RSA = "RSA";

    private static final String SIGNATURE_ALGORITHM_RSA = "SHA256WithRSA";

    private static final int KEY_SIZE = 2048;

    private static Base64 base64Line64 = new Base64(64);

    private static Base64 base64 = new Base64();

    private RSAEncrypt() {
    }

    /**
     * 生成密钥对
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static Map<String, String> generateKeyPair() throws NoSuchAlgorithmException {
        // 随机数源
        SecureRandom sr = new SecureRandom();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        // 随机数据源初始化 KeyPairGenerator对象
        kpg.initialize(KEY_SIZE, sr);
        // 生成密钥对
        KeyPair kp = kpg.generateKeyPair();

        Key publicKey = kp.getPublic();
        byte[] publicKeyBytes = publicKey.getEncoded();
        String pubKey = base64Line64.encodeToString(publicKeyBytes);

        Key privateKey = kp.getPrivate();
        byte[] privateKeyBytes = privateKey.getEncoded();
        String priKey = base64Line64.encodeToString(privateKeyBytes);

        Map<String, String> map = new HashMap<>(3);
        map.put("publicKey", pubKey);
        map.put("privateKey", priKey);
        RSAPublicKey rsp = (RSAPublicKey) kp.getPublic();
        byte[] b = rsp.getModulus().toByteArray();
        map.put("modulus", new String(base64Line64.encode(b)));
        return map;
    }

    /**
     * 生成密钥对到指定路径
     *
     * @param path
     * @return
     */
    public static boolean generateKeyPair(String path) {
        FileWriter writer = null;
        try {
            Map<String, String> keyMap = generateKeyPair();
            String pubKey = keyMap.get("publicKey");
            String priKey = keyMap.get("privateKey");

            StringBuilder pubKeyBuilder = new StringBuilder();
            pubKeyBuilder.append("-----BEGIN PUBLIC KEY-----")
                    .append("\r\n").append(pubKey)
                    .append("-----END PUBLIC KEY-----");
            // 生成公钥证书文件
            writer = new FileWriter(new File(path));
            writer.write("");
            writePath(writer, pubKeyBuilder.toString());

            StringBuilder priKeyBuilder = new StringBuilder();
            priKeyBuilder.append("-----BEGIN PRIVATE KEY-----")
                    .append("\r\n").append(priKey)
                    .append("-----END PRIVATE KEY-----");
            writer = new FileWriter(new File(path), true);
            writePath(writer, priKeyBuilder.toString());

            if (LOG.isDebugEnabled()) {
                LOG.debug(pubKeyBuilder.toString());
                LOG.debug(priKeyBuilder.toString());
            }
            return true;
        } catch (Exception e) {
            LOG.error("Generate key pair error.{}", e);
            return false;
        } finally {
            IoUtil.close(writer);
        }
    }

    /**
     * 加密
     *
     * @param source        源数据
     * @param publicKeyPath
     * @return
     */
    public static String encrypt(String source, String publicKeyPath) {
        try {
            Key key = getPublicKey(new FileInputStream(publicKeyPath));
            // 得到 Cipher 对象来实现对源数据的 RSA 加密
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] b = source.getBytes(BaseConst.CHARSET_UTF8);
            byte[] b1 = cipher.doFinal(b);
            return new String(base64.encode(b1));
        } catch (Exception e) {
            throw new SecurityException("加密异常", e);
        }
    }

    /**
     * 解密
     *
     * @param cryptoGraph    解密算法
     * @param privateKeyPath
     * @return
     */
    public static String decrypt(String cryptoGraph, String privateKeyPath) {
        try {
            Key key = getPrivateKey(new FileInputStream(privateKeyPath));
            // 得到 Cipher 对象对已用公钥加密的数据进行 RSA 解密
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] b1 = base64.decode(cryptoGraph);
            byte[] b = cipher.doFinal(b1);
            return new String(b);
        } catch (Exception e) {
            throw new SecurityException("Decrypt error.", e);
        }
    }

    /**
     * 签名
     *
     * @param content
     * @param privateKeyPath
     * @return
     */
    public static String sign(String content, String privateKeyPath) {
        try {
            PrivateKey priKey = getPrivateKey(new FileInputStream(privateKeyPath));
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_RSA);
            signature.initSign(priKey);
            signature.update(content.getBytes(BaseConst.CHARSET_UTF8));

            byte[] signed = signature.sign();
            return base64.encodeToString(signed);
        } catch (Exception e) {
            throw new SecurityException("Signature error.", e);
        }
    }

    /**
     * 验签
     *
     * @param content
     * @param sign
     * @param publicKeyPath
     * @return
     */
    public static boolean checkSign(String content, String sign, String publicKeyPath) {
        try {
            PublicKey pubKey = getPublicKey(new FileInputStream(publicKeyPath));
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_RSA);
            signature.initVerify(pubKey);
            signature.update(content.getBytes(BaseConst.CHARSET_UTF8));
            boolean verify = signature.verify(base64.decode(sign));
            return verify;
        } catch (InvalidKeyException e) {
            LOG.error("公钥证书有误");
        } catch (SignatureException e) {
            LOG.error("签名有误");
        } catch (Exception e) {
            LOG.error("其他错误");
        }
        return false;
    }

    /**
     * 从文件中加载私钥
     *
     * @param in 私钥文件流
     * @return
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static RSAPrivateKey getPrivateKey(InputStream in)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            return getPrivateKey(readLine(in));
        } finally {
            IoUtil.close(in);
        }
    }

    /**
     * 从文件中输入流中加载公钥
     *
     * @param in 公钥输入流
     * @return
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static RSAPublicKey getPublicKey(InputStream in)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            return getPublicKey(readLine(in));
        } finally {
            IoUtil.close(in);
        }
    }

    /**
     * 获取私钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static RSAPrivateKey getPrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(base64.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * 获取公钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static RSAPublicKey getPublicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(base64.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private static String readLine(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String readLine = null;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null) {
            if (readLine.charAt(0) == '-') {
                continue;
            } else {
                sb.append(readLine);
                sb.append('\r');
            }
        }
        br.close();
        return sb.toString();
    }

    private static void writePath(OutputStreamWriter writer, String content) throws IOException {
        writer.write(content);
        writer.flush();
        writer.close();
    }
}
