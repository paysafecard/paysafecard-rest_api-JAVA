package com.paysafecard.payout;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paysafecard.payout.enums.Environment;
import com.paysafecard.payout.enums.PaysafeMethod;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by Pascal on 27.09.2017.
 */
public class PaysafePayoutController {
	
	private String apiUrl = "";
	
	private HashMap<String, Object> response;
	private int responseCode = 0;
	
	/**
	 * Just tell which environment you are using!
	 * @param environment means the api you wanna use.
	 */
	public PaysafePayoutController(Environment environment) {
		switch(environment) {
			case TEST:
				this.apiUrl = "https://apitest.paysafecard.com/v1/payouts/";
				break;
			case PRODUCTION:
				this.apiUrl = "https://api.paysafecard.com/v1/payouts/";
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
	
	/**
	 * get the payout details of a payment id
	 * @param consumer whatever you which to do after - can be null!
	 * @param payment the payment id
	 */
	public void getPayoutDetails(Consumer<HashMap<String, Object>> consumer, String payment) {
		this.doRequest(consumer::accept,payment, "", PaysafeMethod.GET, Maps.newHashMap());
	}
	
	/**
	 * get the limit details of a currency.
	 * @param consumer whatever you which to do after - can be null!
	 * @param currency the currency id of your payment.
	 */
	public void getLimits(Consumer<HashMap<String, Object>> consumer, String currency) {
		this.doRequest(consumer::accept,"limits/" + currency, "", PaysafeMethod.GET, Maps.newHashMap());
	}
	
	/**
	 * Validate a Payout asynchronously.
	 * @param consumer whatever you which to do after - can be null!
	 * @param amount
	 * @param currency
	 * @param merchantClientId
	 * @param customerMail
	 * @param customerIp
	 * @param firstName
	 * @param lastName
	 * @param birthDate
	 * @param correlationId
	 * @param subMerchantId
	 */
	public void validatePayout(Consumer<HashMap<String, Object>> consumer, double amount, String currency,
	                           String merchantClientId, String customerMail, String customerIp, String firstName, String lastName,
	                           String birthDate, String correlationId, String subMerchantId) {
		HashMap<String, String> headers = Maps.newHashMap();
	 
		if(!correlationId.equals("")) {
			headers.put("Correlation-ID", correlationId);
		}
		
		JsonObject customer = new JsonObject();
		customer.addProperty("id", merchantClientId);
		customer.addProperty("email", customerMail);
		customer.addProperty("first_name", firstName);
		customer.addProperty("last_name", lastName);
		customer.addProperty("date_of_birth", birthDate);
		customer.addProperty("ip", customerIp);
		
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("amount", Double.toString(amount));
		jsonObject.addProperty("currency", currency);
		jsonObject.addProperty("type", "PAYSAFECARD");
		jsonObject.add("customer", customer);
		jsonObject.addProperty("capture", "false");
		
		if(!subMerchantId.equals("")) {
			jsonObject.addProperty("submerchant_id", subMerchantId);
		}
		
		this.doRequest(consumer::accept, "", jsonObject.toString(), PaysafeMethod.POST, headers);
	}
	
	/**
	 * Execute the Payout asynchronously.
	 * @param consumer whatever you which to do after - can be null!
	 * @param payoutId
	 * @param amount
	 * @param currency
	 * @param merchantClientId
	 * @param customerMail
	 * @param customerIp
	 * @param firstName
	 * @param lastName
	 * @param birthDate
	 * @param correlationId
	 * @param subMerchantId
	 */
	public void executePayout(Consumer<HashMap<String, Object>> consumer, String payoutId, double amount, String currency,
	                          String merchantClientId, String customerMail, String customerIp, String firstName, String lastName,
	                          String birthDate, String correlationId, String subMerchantId) {
		HashMap<String, String> headers = Maps.newHashMap();
		
		if(!correlationId.equals("")) {
			headers.put("Correlation-ID", correlationId);
		}
		
		JsonObject customer = new JsonObject();
		customer.addProperty("id", merchantClientId);
		customer.addProperty("email", customerMail);
		customer.addProperty("first_name", firstName);
		customer.addProperty("last_name", lastName);
		customer.addProperty("date_of_birth", birthDate);
		customer.addProperty("ip", customerIp);
		
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("amount", Double.toString(amount));
		jsonObject.addProperty("currency", currency);
		jsonObject.addProperty("type", "PAYSAFECARD");
		jsonObject.add("customer", customer);
		jsonObject.addProperty("capture", "true");
		
		if(!subMerchantId.equals("")) {
			jsonObject.addProperty("submerchant_id", subMerchantId);
		}
		
		this.doRequest(consumer::accept, payoutId + "/capture", jsonObject.toString(), PaysafeMethod.POST, headers);
	}
	
	/**
	 * Do a post or call request to the environment api
	 * @param consumer whatever you which to do after - can be null!
	 * @param urlParam this params will be added to your link if it's a get-call.
	 * @param parameters this params will get posted if it's a post-call
	 * @param paysafeMethod post or get?
	 * @param headers other header settings will get added to post or get header.
	 */
	private void doRequest(Consumer<HashMap<String, Object>> consumer, String urlParam, String parameters,
	                       PaysafeMethod paysafeMethod, HashMap<String, String> headers) {
		switch(paysafeMethod) {
			case GET:
				sendAsyncGet(consumer::accept, urlParam, headers);
				break;
			case POST:
				sendAsyncPost(consumer::accept, parameters, headers);
				break;
		}
	}
	
	/**
	 * send a get asynchronously.
	 * @param consumer whatever you which to do after - can be null!
	 * @param urlParam this params will be added to your link as a get-call.
	 * @param headers other header settings will get added to header.
	 */
	private void sendAsyncGet(Consumer<HashMap<String, Object>> consumer, String urlParam, HashMap<String, String> headers) {
		System.out.println(urlParam);
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		HttpResponse httpResponse = null;
		try {
			httpclient.start();
			HttpGet request = new HttpGet(this.apiUrl + urlParam);
			
			request.addHeader("Authorization", "Basic cHNjX3hsMEV3ZkxYLTk2YkVranktbVhZRDdTRnZpeXZhcUE=");
			request.addHeader("Content-Type", "application/json");
			
			request.addHeader("User-Agent", "Mozilla/5.0");
			request.addHeader("Accept", "*/*");
			request.addHeader("charset", "utf-8");
			
			headers.keySet().forEach(headerKey -> request.addHeader(headerKey, headers.get(headerKey)));
			
			Future<HttpResponse> future = httpclient.execute(request, null);
			httpResponse = future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			consumer.accept((this.response
					= this.parseJson(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * send a post asynchronously.
	 * @param consumer whatever you which to do after - can be null!
	 * @param urlParam this params will get posted
	 * @param headers other header settings will get added to header.
	 */
	private void sendAsyncPost(Consumer<HashMap<String, Object>> consumer, String urlParam, HashMap<String, String> headers) {
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		HttpResponse httpResponse = null;
		try {
			httpclient.start();
			HttpPost request = new HttpPost(this.apiUrl);
			request.setEntity(new StringEntity(urlParam));
			
			request.addHeader("Authorization", "Basic cHNjX3hsMEV3ZkxYLTk2YkVranktbVhZRDdTRnZpeXZhcUE=");
			request.addHeader("Content-Type", "application/json");
			
			request.addHeader("User-Agent", "Mozilla/5.0");
			request.addHeader("Accept", "*/*");
			request.addHeader("charset", "utf-8");
			
			headers.keySet().forEach(headerKey -> request.addHeader(headerKey, headers.get(headerKey)));
			
			Future<HttpResponse> future = httpclient.execute(request, null);
			httpResponse = future.get();
		} catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			consumer.accept((this.response
					= this.parseJson(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * format a json string to a hashmap with a string (key) and object (value).
	 * @param json means a string which should get formatted.
	 * @return a json hashmap with keys and objects.
	 */
	private HashMap<String, Object> parseJson(String json) {
		HashMap<String, Object> response = Maps.newHashMap();
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject) jsonParser.parse(json);
		
		for(Map.Entry<String, JsonElement> entry : jsonObject.getAsJsonObject().entrySet()) {
			if(entry.getValue().toString().contains("{")) {
				HashMap<String, Object> response2 = this.parseJson(entry.getValue().toString()
						.replace("[", "").replace("]", ""));
				response.put(entry.getKey(), response2);
			} else {
				response.put(entry.getKey(), entry.getValue().toString().replace("\"", ""));
			}
		}
		
		return response;
	}
	
	/**
	 * outprint the logical error.
	 * @return error hashmap with string and object.
	 */
	public HashMap<String, Object> getError() {
		HashMap<String, Object> error = Maps.newHashMap();
		
		if(this.responseCode > 200) {
			switch(this.responseCode) {
				case 400:
					error.put("number", "HTTP:400");
					error.put("message", "Logical error. Please check logs.");
					break;
				case 403:
					error.put("number", "HTTP:403");
					error.put("message", "IP not whitelisted!");
					break;
				case 500:
					error.put("number", "HTTP:500");
					error.put("message", "Server error. Please check logs.");
					break;
			}
		}
		
		int errorNumber = Integer.parseInt((String) this.response.get("number"));
		
		switch(errorNumber) {
			case 3162:
				error.put("message", "Unfortunately, no my paysafecard account exists under the e-mail address you have entered. Please check the address for a typing error. If you do not have a my paysafecard account, you can register for one online now for free.");
				break;
			case 3195:
				error.put("message", "The personal details associated with your my paysafecard account do not match the details of this account. Please check the first names, surnames and dates of birth entered in both accounts and request the payout again.");
				break;
			case 3167: case 3170: case 3194: case 3168: case 3230: case 3231: case 3232: case 3233: case 3234:
				error.put("message", "Unfortunately, the payout could not be completed due to a problem which has arisen with your my paysafecard account. paysafecard has already sent you an e-mail with further information on this. Please follow the instructions found in this e-mail before requesting the payout again.");
				break;
			case 3197: case 3198:
				error.put("message", "Unfortunately, the payout could not be completed due to a problem which has arisen with your my paysafecard account. Please contact the paysafecard support team. info@paysafecard.com");
				break;
			case 10008:
				error.put("message", "Invalid API Key");
				break;
			default:
				error.put("message", "Unfortunately there has been a technical problem and your payout request could not be executed. If the problem persists, please contact our customer support: support@company.com");
				break;
		}
		
		return error;
	}
	
	/**
	 * Check if request is successful...
	 *
	 * @return if responseCode is okay
	 */
	public boolean requestIsOk() {
		return this.responseCode < 300;
	}
}