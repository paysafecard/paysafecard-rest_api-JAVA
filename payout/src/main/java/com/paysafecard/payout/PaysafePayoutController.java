package com.paysafecard.payout;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.paysafecard.paysafebase.PaySafeBase;
import com.paysafecard.paysafebase.enums.Environment;
import com.paysafecard.paysafebase.enums.PaysafeMethod;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by Pascal on 27.09.2017.
 */
public class PaysafePayoutController extends PaySafeBase {
	
	/**
	 * Just tell which environment you are using!
	 * @param environment means the api you wanna use.
	 */
	public PaysafePayoutController(Environment environment) {
		switch(environment) {
			case TEST:
				apiUrl = "https://apitest.paysafecard.com/v1/payouts/";
				break;
			case PRODUCTION:
				apiUrl = "https://api.paysafecard.com/v1/payouts/";
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
	 * outprint the logical error.
	 * @return error hashmap with string and object.
	 */
	public HashMap<String, Object> getError() {
		HashMap<String, Object> error;
		
		if(!(error = super.getError()).isEmpty()) {
			return error;
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
}