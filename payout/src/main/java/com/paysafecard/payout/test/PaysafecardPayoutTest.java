package com.paysafecard.payout.test;

import com.paysafecard.payout.PaysafePayoutController;
import com.paysafecard.paysafebase.enums.Environment;

import java.util.Map.Entry;

public class PaysafecardPayoutTest {
	
	public static void main(String[] args) {
		PaysafePayoutController payoutController = new PaysafePayoutController(Environment.TEST);
		
		payoutController.getLimits(responsePayment -> {
			for(Entry<String, Object> entry : responsePayment.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
		}, "EUR");
		
		double amount = 0.10;
		String currency = "EUR";
		
		String client_id = "434186408713", customer_mail = "VrAtTRLRyS@avUVWdRVeH.NYE";
		String customer_firstname = "Test", customer_lastname = "BubxNFGHwdGCElzbmjxsycWdYX";
		String customer_birthday = "1986-06-16";
		String customer_ip = "84.59.200.59";
		
		String correlation_id = "", submerchant_id = "";
		
		payoutController.validatePayout(responseValidate -> {
			String payout_id = (String) responseValidate.get("id");
			
			if(payoutController.requestIsOk()) {
				if(responseValidate.get("status").equals("VALIDATION_SUCCESSFUL")) {
					payoutController.executePayout(responseExecute -> {
						if(responseExecute.get("status").equals("SUCCESS")) {
							System.out.println("Payout successful!");
						} else {
							throw new IllegalStateException("Error with Payment-State! " + payoutController.getError().get("message"));
						}
					}, payout_id, amount, currency, client_id,
							customer_mail, customer_ip, customer_firstname, customer_lastname, customer_birthday,
							correlation_id, submerchant_id);
				}
			} else {
				throw new IllegalStateException("Error with Payment-Validation! " + payoutController.getError().get("message"));
			}
		}, amount, currency, client_id, customer_mail, customer_ip,
			customer_firstname, customer_lastname, customer_birthday, correlation_id, submerchant_id);
	}
}