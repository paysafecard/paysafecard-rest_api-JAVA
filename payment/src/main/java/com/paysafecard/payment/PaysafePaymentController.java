package com.paysafecard.payment;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.paysafecard.paysafebase.PaySafeBase;
import com.paysafecard.paysafebase.enums.Environment;
import com.paysafecard.paysafebase.enums.PaysafeMethod;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.function.Consumer;


public class PaysafePaymentController extends PaySafeBase {
	
	private String paysafeKey;
	
	/**
	 * Just tell which environment you are using!
	 *
	 * @param paysafeKey  your paysafe business key.
	 * @param environment means the api you wanna use.
	 */
	public PaysafePaymentController(String paysafeKey, Environment environment) {
		this.paysafeKey = paysafeKey;
		switch(environment) {
			case TEST:
				apiUrl = "https://apitest.paysafecard.com/v1/payments/";
				break;
			case PRODUCTION:
				apiUrl = "https://api.paysafecard.com/v1/payments/";
				break;
			default:
				try {
					throw new IllegalAccessException("Environment not supported");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				break;
		}
	}
	
	public void createPayment(Consumer<HashMap<String, Object>> consumer, double amount, String currency, JsonObject customer, String success_url, String failure_url, String notification_url, String correlation_id, String country_restriction, String kyc_restriction, int min_age, int shop_id, String submerchant_id) {
		HashMap<String, String> headers = Maps.newHashMap();
		
		if(!correlation_id.equals("")) {
			headers.put("Correlation-ID", correlation_id);
		}
		
		JsonObject redirect = new JsonObject();
		redirect.addProperty("success_url", success_url);
		redirect.addProperty("failure_url", failure_url);
		
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty("currency", currency);
		jsonObject.addProperty("amount", Double.toString(amount));
		jsonObject.add("customer", customer);
		jsonObject.addProperty("type", "PAYSAFECARD");
		jsonObject.add("redirect", redirect);
		jsonObject.addProperty("notification_url", notification_url);
		jsonObject.addProperty("shop_id", shop_id);
		
		if(!country_restriction.equals("")) {
			jsonObject.addProperty("country_restriction", country_restriction);
		}
		
		if(!kyc_restriction.equals("")) {
			jsonObject.addProperty("kyc_level", kyc_restriction);
		}
		
		if(min_age != 0) {
			jsonObject.addProperty("min_age", min_age);
		}
		
		if(!submerchant_id.equals("")) {
			jsonObject.addProperty("submerchant_id", submerchant_id);
		}
		
		this.doRequest(consumer::accept, "", jsonObject.toString(), PaysafeMethod.POST, headers);
	}
	
	public void capturePayment(Consumer<HashMap<String, Object>> consumer, String payment) {
		HashMap<String, String> headers = Maps.newHashMap();
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", payment);
		
		System.out.println("Payment-ID: ");
		System.out.println(payment);
		this.doRequest(consumer::accept, payment + "/capture", jsonObject.toString(), PaysafeMethod.POST, headers);
	}
	
	public void retrievePayment(Consumer<HashMap<String, Object>> consumer) {
		this.doRequest(consumer::accept, "", "", PaysafeMethod.GET, Maps.newHashMap());
	}
	
	private void doRequest(Consumer<HashMap<String, Object>> consumer, String urlParam, String parameters, PaysafeMethod paysafeMethod, HashMap<String, String> headers) {
		switch(paysafeMethod) {
			case GET:
				sendAsyncGet(consumer::accept, parameters, headers);
				break;
			case POST:
				sendAsyncPost(consumer::accept, urlParam, parameters, headers);
				break;
		}
	}
	
	@Override
	public void sendAsyncGet(Consumer<HashMap<String, Object>> consumer, String urlParam, HashMap<String, String> headers) {
		System.out.println(urlParam);
		
		headers.put("Authorization", "Basic " + Base64.getEncoder().encode(this.paysafeKey.getBytes()).toString());
		
		super.sendAsyncGet(consumer, urlParam, headers);
	}
	
	public void sendAsyncPost(Consumer<HashMap<String, Object>> consumer, String urlParam, String parameters, HashMap<String, String> headers) {
		try {
			String USER_AGENT = "Mozilla/5.0";
			URL obj = new URL(this.apiUrl + urlParam);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			//add reuqest header
			con.setRequestMethod("POST");
			System.setProperty("http.keepAlive", "false");
			//con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encode(this.psc_key.getBytes()).toString());
			con.setRequestProperty("Authorization", "Basic cHNjX3hsMEV3ZkxYLTk2YkVranktbVhZRDdTRnZpeXZhcUE=");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("charset", "utf-8");
			
			for (String headerKey : headers.keySet()) {
				con.setRequestProperty(headerKey, headers.get(headerKey));
			}
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			
			wr.writeBytes(parameters);
			
			wr.flush();
			wr.close();
			
			this.responseCode = con.getResponseCode();
			BufferedReader in = null;
			if (this.responseCode >= 400) {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			} else {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			//print result
			//System.err.println(response.toString());
			this.response = parseJson(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		consumer.accept(this.response);
	}
	
	public HashMap<String, Object> getError() {
		HashMap<String, Object> error;
		
		if(!(error = super.getError()).isEmpty()) {
			return error;
		}
		
		int error_number = Integer.parseInt((String) this.response.get("number"));
		
		switch(error_number) {
			case 4003:
				error.put("message", "The amount for this transaction exceeds the maximum amount. The maximum amount is 1000 EURO (equivalent in other currencies)");
				break;
			case 3001:
				error.put("message", "Transaction could not be initiated because the account is inactive.");
				break;
			case 2002:
				error.put("message", "payment id is unknown.");
				break;
			case 2010:
				error.put("message", "Currency is not supported.");
				break;
			case 2029:
				error.put("message", "Amount is not valid. Valid amount has to be above 0.");
				break;
			case 10028:
				error.put("message", "Argument missing.");
				break;
			default:
				error.put("message", "Transaction could not be initiated due to connection problems. If the problem persists, please contact our support. ");
				break;
		}
		
		return error;
	}
}
