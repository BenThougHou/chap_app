package com.yiliao.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

public class RSACoderUtil {

	public static final String CHARSET = "UTF-8";
	public static final String RSA_ALGORITHM = "RSA";

	public static Map<String, String> createKeys(int keySize) {
		// 为RSA算法创建一个KeyPairGenerator对象
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("No such algorithm-->[" + RSA_ALGORITHM + "]");
		}

		// 初始化KeyPairGenerator对象,密钥长度
		kpg.initialize(keySize);
		// 生成密匙对
		KeyPair keyPair = kpg.generateKeyPair();
		// 得到公钥
		Key publicKey = keyPair.getPublic();
		String publicKeyStr =Base64.getEncoder().encodeToString(publicKey.getEncoded());
//		String publicKeyStr = Base64.encodeBase64URLSafeString(publicKey.getEncoded());
//		String publicKeyStr = new String(publicKey.getEncoded());
		// 得到私钥
		Key privateKey = keyPair.getPrivate();
//		String privateKeyStr = Base64.encodeBase64URLSafeString(privateKey.getEncoded());
		String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		
		Map<String, String> keyPairMap = new HashMap<String, String>();
		keyPairMap.put("publicKey", publicKeyStr);
		keyPairMap.put("privateKey", privateKeyStr);

		return keyPairMap;
	}

	/**
	 * 得到公钥
	 * 
	 * @param publicKey 密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 通过X509编码的Key指令获得公钥对象
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
//		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
		RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
		return key;
	}

	/**
	 * 得到私钥
	 * 
	 * @param privateKey 密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static RSAPrivateKey getPrivateKey(String privateKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 通过PKCS#8编码的Key指令获得私钥对象
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
		RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
		return key;
	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 */
	public static String publicEncrypt(String data, RSAPublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET),
					publicKey.getModulus().bitLength()));
		} catch (Exception e) {
			throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
		}
	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 */

	public static JSONObject privateDecrypt(HttpServletRequest req) {
		try {

			String cipher_param = "";
			
			BufferedReader br = req.getReader();
			String str = "";
			while ((str = br.readLine()) != null) {
				cipher_param += str;
			}
//			String[] str1 = cipher_param.split("=");
			
			cipher_param = cipher_param.substring(6);
			
			cipher_param =URLDecoder.decode(cipher_param,"UTF-8") ;

			if (StringUtils.isBlank(cipher_param)) {
				return null;
			}
			
			RSAPrivateKey privateKey = RSACoderUtil.getPrivateKey(SystemConfig.getValue("encrypt_private"));

			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			return JSONObject.fromObject(new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE,
					Base64.getDecoder().decode(cipher_param.replace("\r\n", "")), privateKey.getModulus().bitLength()), CHARSET));
		} catch (Exception e) {
			throw new RuntimeException("解密数据遇到异常", e);
		}
	}
	/**
	 * 私钥解密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 */

	public static JSONObject privateDecrypt1(String  param) {
		try {
			param =URLDecoder.decode(param,"UTF-8") ;

			if (StringUtils.isBlank(param)) {
				return null;
			}
			
			RSAPrivateKey privateKey = RSACoderUtil.getPrivateKey(SystemConfig.getValue("encrypt_private"));

			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			return JSONObject.fromObject(new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE,
					Base64.getDecoder().decode(param.replace("\r\n", "")), privateKey.getModulus().bitLength()), CHARSET));
		} catch (Exception e) {
			throw new RuntimeException("解密数据遇到异常", e);
		}
	}

	/**
	 * 私钥加密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 */

	public static String privateEncrypt(String data, RSAPrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			return java.util.Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET),
					privateKey.getModulus().bitLength()));
		} catch (Exception e) {
			throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
		}
	}

	/**
	 * 公钥解密
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 */
	public static String publicDecrypt(String data, RSAPublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(data),
					publicKey.getModulus().bitLength()), CHARSET);
		} catch (Exception e) {
			throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
		}
	}

	public static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
		int maxBlock = 0;
		if (opmode == Cipher.DECRYPT_MODE) {
			maxBlock = keySize / 8;
		} else {
			maxBlock = keySize / 8 - 11;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] buff;
		int i = 0;
		try {
			while (datas.length > offSet) {
				if (datas.length - offSet > maxBlock) {
					buff = cipher.doFinal(datas, offSet, maxBlock);
				} else {
					buff = cipher.doFinal(datas, offSet, datas.length - offSet);
				}
				out.write(buff, 0, buff.length);
				i++;
				offSet = i * maxBlock;
			}
		} catch (Exception e) {
			throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
		}
		byte[] resultDatas = out.toByteArray();
		IOUtils.closeQuietly(out);
		return resultDatas;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Map<String, String> keyMap = RSACoderUtil.createKeys(1024);
//        String  publicKey = keyMap.get("publicKey");
//        String  privateKey = keyMap.get("privateKey");
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCEX8uKgV3qbs+JjOGldH+OXA/7S7KtgWu2DHydcrUS+pwDtAw5cA8gRSHlVk8vVxSn89gy3Nhz0EA2qfxPB1hciWTy4lziKPEsCJcGq1ri/bADHj2CXiYLOkM6RxWhgFDiT+Afcu4XnYRCL5gFGaEvyVk3UV369qjG7y3ijhUHGwIDAQAB";
		String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIRfy4qBXepuz4mM4aV0f45cD/tLsq2Ba7YMfJ1ytRL6nAO0DDlwDyBFIeVWTy9XFKfz2DLc2HPQQDap/E8HWFyJZPLiXOIo8SwIlwarWuL9sAMePYJeJgs6QzpHFaGAUOJP4B9y7hedhEIvmAUZoS/JWTdRXfr2qMbvLeKOFQcbAgMBAAECgYAmDsycI5l2vykX6NMvGJFbwogtvZO/YogvLOPe+I2qyTZ1shwZn2gZ5QZbwza865JavzaXWPs6QNNVZAXQStJSTjqGLHiOK9kQWflF7XtZtBoAVAKP9lLveyBXtTrII/3LSzIvMECdT5YYYLzU91vpolg9+0WpzEHtRdtbDIyLEQJBAMm5ofn6kpQ88IW7ypMcZ5RFy46XPxeaZhmAQwSONYzGVArpjHfe3wKC5QxqfGThHT2YCPSFukhvsLN6etF537cCQQCn/W2ruSXrLhMy8dgkEwQL+EX9cd97qDvpd1u5u1TQ+kZo3cJ5SEONucM3YN5Tnu9FfG2aBEk9wZywl4pNcgu9AkEAvRVphJ2hA55TfadW7Kn1VQBPG2sd0owBI+ob54s9GqVUZZ4g8sNPmlUDSa4Ox6hS9kcqLbmjsP4wTSuDMBLi5QJARqbceCufWgD/zB5QxCNHQW2Cqiw5TL2drPwpFL1H17ha4oHZ575PwwyMoP7ZopumkDfbtLn2v8VtDAw/+4BqlQJAKlzcv+cjy+aA41gX68QeDr3sBx6LxtmJMhblwNGRsU1Y+AX0dAMFNLxss+sYQC9MiPW/LDkmyHPrAtAD9F/ASw==";
		System.out.println("公钥: \n\r" + publicKey);
		System.out.println("私钥： \n\r" + privateKey);
//

//
		JSONObject json = new JSONObject();
		json.put("tokenId", "335");
		json.put("t_token", "fc0086cdcc0f442ba9da3df5a0a82848");
		json.put("userId", "335");
//		json.put("page", "1");
//		json.put("queryType", "1");

		System.out.println("\r明文：\r\n" + json.toString());
		System.out.println("\r明文大小：\r\n" + json.toString().getBytes().length);
		String encodedData = RSACoderUtil.publicEncrypt(json.toString(), RSACoderUtil.getPublicKey(publicKey));
		System.out.println("密文：\r\n" + encodedData);
		
		
		RSAPrivateKey privateKey1 = RSACoderUtil.getPrivateKey(privateKey);
		Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, privateKey1);
//		
		String string = new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE,Base64.getDecoder().decode(encodedData), privateKey1.getModulus().bitLength()), CHARSET);
//		
        System.out.println("解密后文字: \r\n" + string);
		
		String res = HttpUtil.httpClentString("http://127.0.0.1/app/getVIPUserInfo.html", "param="+URLEncoder.encode(encodedData, "UTF-8"));
		

		System.err.println(res);

 

	}

}
