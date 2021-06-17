package com.yiliao.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.github.wxpay.sdk.WXPayConfig;

public class WXPayConfigImpl implements WXPayConfig {

	private String appId;
	private String mchId;//
	private String key;// 密钥key

	private byte[] certData;
	private static WXPayConfigImpl INSTANCE;

	public static WXPayConfigImpl getInstance(String appId, String mchId, String key) throws Exception {
		INSTANCE = new WXPayConfigImpl(appId, mchId, key);
		return INSTANCE;
	}

	public String getAppID() {
		return appId;
	}

	public String getMchID() {
		return mchId;
	}

	public String getKey() {
		return key;
	}

	public InputStream getCertStream() {
		ByteArrayInputStream certBis;
		certBis = new ByteArrayInputStream(this.certData);
		return certBis;
	}

	public int getHttpConnectTimeoutMs() {
		return 2000;
	}

	public int getHttpReadTimeoutMs() {
		return 10000;
	}

	public WXPayConfigImpl(String appId, String mchId, String key) {
		this.appId = appId;
		this.mchId = mchId;
		this.key = key;
	}

}
