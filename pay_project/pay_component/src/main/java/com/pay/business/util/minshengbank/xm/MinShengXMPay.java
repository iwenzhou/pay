package com.pay.business.util.minshengbank.xm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

public class MinShengXMPay {
	private static Logger log = Logger.getLogger(MinShengXMPay.class);
	
	private static Map<String, String> resultMap = null;
	
	/**
	 * 民生厦门扫码支付
	 * @param memberCode 商户号
	 * @param orderNum 订单号
	 * @param payMoney 金额(元)
	 * @param payType 支付类型(1WX、2ALI、3QQ)
	 * @param key
	 * @return
	 */
	public static Map<String, String> xmScanPay(String memberCode, String orderNum, String payMoney, String payType, String key){
		resultMap = new HashMap<String, String>();
		Map<String, String> param = new HashMap<String, String>();
		param.put("memberCode", memberCode);
		param.put("orderNum", orderNum);
		param.put("payMoney", payMoney);
		param.put("payType", payType);
		param.put("callbackUrl", MinShengXMConfig.NOTIFY_URL);
		param.put("signStr", EpaySignUtil.sign(key, param));
		log.info("=====>民生厦门扫码支付参数：\n" + param.toString());
		try {
			String doPost = doPost(MinShengXMConfig.PAY_URL, param);
			log.info("=====>民生厦门扫码支付返回的预支付订单信息：：\n" + doPost);
			JSONObject maps = JSONObject.parseObject(doPost);
			if("0000".equals(maps.get("returnCode").toString())){
				JSONObject object = (JSONObject)maps.get("resData");
				String qrCode = object.get("qrCode").toString();
				qrCode = Base64.getFromBASE64(qrCode);
				resultMap.put("qr_code", qrCode);
				resultMap.put("code","10000");
				log.info("=====>民生厦门扫码支付调起成功:qr_code : " + qrCode);
			}else {
				resultMap.put("code","10001");
				resultMap.put("msg", maps.get("returnMsg").toString());
				log.error("=====>民生厦门扫码支付失败原因:" + maps.get("returnMsg"));
			}
		} catch (Exception e) {
			resultMap.put("code","10001");
			e.printStackTrace();
		}
		return resultMap;
	}
	
	/**
	 * 提现 
	 * @param memberCode 商户编号
	 * @param orderNum 订单号
	 * @param txnType 交易类型(1WX、2ALI、3QQ、4BAIDU)
	 * @param key
	 * @return
	 */
	public static Map<String, String> drawPay(String memberCode, String orderNum, String txnType, String key) {
		resultMap = new HashMap<String, String>();
		Map<String, String> param = new HashMap<String, String>();
		param.put("memberCode", memberCode);
		param.put("orderNum", orderNum);
		param.put("txnType", txnType);
		param.put("callbackUrl", MinShengXMConfig.DRAW_NOTIFY);
		param.put("signStr", EpaySignUtil.sign(key, param));
		try {
			String doPost = doPost(MinShengXMConfig.DRAW_URL, param);
			log.info("提现上游返回：" + doPost);
			JSONObject maps = JSONObject.parseObject(doPost);
			System.out.println(maps);
			resultMap.put("code", maps.getString("returnCode"));
			resultMap.put("msg", maps.getString("returnMsg"));
		}catch (Exception e) {
			resultMap.put("code","10001");
			resultMap.put("msg", "请求异常！");
			e.printStackTrace();
		}
		resultMap.put("param", param.toString());
		return resultMap;
	}
	
	public static void main(String[] args) {
		/*Map<String, String> param = new HashMap<String, String>();
		param.put("memberCode", "820440348160001");
		param.put("orderNum", "cv");
		param.put("payMoney", "qwe");
		param.put("payType", "asd");
		param.put("callbackUrl", "xzcx");
		param.put("signStr", "kT//2LIsqHzPANYcY7OMDbi+HRe45mTlMaLtSbd27UgRU7edy+XzGSRe4xdqSjvbQie44nm5RMkuy6xme/u4gQ==");
		String sign = EpaySignUtil.sign("MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAm7H8eHHvTgovFU1J88Aa4FfFbwRIpzHJqz15VDHD5k4RhoCZGm6/Y0LNe9nJlfMmfDMPp4TWVsb66Qq7+f7UrwIDAQABAkAYtQX7lxkCqVsPZlR1+eZJ86PBGkztO1llczvtwHf18+2v0kjdSa9i9WghGMB4IsfEcj2LznYGNl0WrVnpdfMBAiEA4yB6yE0hdwwLtPD+7OAiS8CqAAZSWnlWyFEBX//bycsCIQCvfN3SyB5/KvyIoON8+9coiUVFPacEaiFY3qwA1LmULQIhAJ5WbkJlQwczJpYlzBJmzoHw9pK91XutS4qqrkK2pAqxAiA3EGM6RHjtRju/U1yOVyeIHKqTs2i4xeR40kX+bMFecQIhAMqwCplNKDUC/B3ac5Ewg7AnWE0eYOjDfcRMeFIcv/Ja"
		, param);
		System.out.println(sign);*/
		String key = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAgM80HpaSArW5psunbOdBq8dKoHCfZ9WW7iBTWA1axN79wcIKVuP2P1UL5f0eM5mjIO8a8mgI2GKG6XBwEDHcQwIDAQABAkAKUQ4qmQKuxuFMs6kANvFyka6vdPW/ekLxZZNnTQKFG/2UDBYMDlw0GEnl2AiJCMtH8MzyIOP4YPNqqbHTet0RAiEA1vrm3oPZAFGJ4TvB3Uc+3ZP3VQdJHOBV00oPljxHxUsCIQCZYyKqR/ntRjEZS8PX3eb5NlijZFM/mTGIrIqGww4B6QIgMlle+txcVWPDbBnnxKBsC88czTBAol+0GiEtV11U3dMCIH8si7RFIvnQZasEf2b+K5/aqFkoOEGVwVUBQYWEOZvpAiATBDk9LPWydTJSZUjBF6JqMcni5XpCjAR0R4BZwNw4vA==";
		Map<String, String> xmScanPay = xmScanPay("1010001626", Long.toString(new Date().getTime()), "500", "2", key);
		System.out.println(xmScanPay.toString());
//		Map<String, String> drawPay = drawPay("1010001459", Long.toString(new Date().getTime()), "1", key);
//		System.out.println(drawPay.toString());
	}
	
	/**
	 * post传递多个参数
	 */
	private static String doPost(String url,Map<String, String> params) throws Exception{
		String result=null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		if (params != null) {
			// 设置2个post参数
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				parameters.add(new BasicNameValuePair(key, (String) params
						.get(key)));
			}
			System.out.println("list：" + parameters);
			// 构造一个form表单式的实体
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "GBK");
			// 将请求实体设置到httpPost对象中
			httpPost.setEntity(formEntity);
		}
		try {
			response = httpClient.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "GBK");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			response.close();
		}
		return result;
	}
}