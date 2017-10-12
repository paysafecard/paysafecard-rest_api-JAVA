package com.paysafecard.payment.test;

import java.awt.Desktop;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JOptionPane;

import com.google.gson.JsonObject;
import com.paysafecard.payment.PaysafePaymentController;
import com.paysafecard.paysafebase.enums.Environment;

public class PaysafecardPaymentTest {
	
	public static void main(String[] args) {
		PaysafePaymentController paymentController = new PaysafePaymentController("psc_xl0EwfLX-96bEkjy-mXYD7SFviyvaqA", Environment.TEST);
		
		JsonObject customer = new JsonObject();
		customer.addProperty("id", "asdfhgsdjfgsjdfghj");
		customer.addProperty("ip", "84.200.56.234");
		
		String currency = "EUR";
		double amount = 2.01;
		
		String success_url = "http://success.url/{payment_id}",
				failure_url = "http://failture.url/{payment_id}",
				notify_url = "http://notify.url/{payment_id}";
		
		String correlation_id = "", country_restriction = "", kyc_restriction = "";
		int min_age = 0, shop_id = 1;
		
		String submerchant_id = "";
		
		paymentController.createPayment(responseMap -> {
			responseMap.forEach((key, value) -> System.out.println("|| " + key + " -> " + value));
			
			if(paymentController.requestIsOk()) {
				String auth = (String) ((HashMap<String, Object>) responseMap.get("redirect")).get("auth_url"),
						id = (String) responseMap.get("id");
				
				// Open the Auth URL in the Browser
				try {
					Desktop.getDesktop().browse(new URL(auth).toURI());
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				
				// Asking the Client if the Payment is finished in the Browser.
				JOptionPane.showMessageDialog(null, "Payment is finished?",
						"Payment", JOptionPane.PLAIN_MESSAGE);
				
				responseMap.forEach((entry, value) -> System.out.println(entry + " - " + value));
				
				paymentController.retrievePayment(responsePaymentMap -> {
					System.out.println("State: " + responsePaymentMap.get("status"));
					
					if(responsePaymentMap.get("status").equals("AUTHORIZED")) {
						paymentController.capturePayment(responeCaptureMap -> {
							System.out.println("State: " + responsePaymentMap.get("status"));
							if(responsePaymentMap.get("status").equals("SUCCESS")) {
								System.out.println("Payment successful!");
							}
						}, id);
					} else {
						// If capturing is not OK. Show error message.
						if(responsePaymentMap.get("status").equals("CANCELED_CUSTOMER")) {
							System.out.println("Cancelled Customer");
						} else {
							throw new IllegalStateException("Error with Payment: " + paymentController.getError().get("message"));
						}
					}
				});
			} else {
				throw new IllegalStateException("Error with Payment: " + paymentController.getError().get("message"));
			}
		}, amount, currency, customer, success_url, failure_url, notify_url, correlation_id, country_restriction,
				kyc_restriction, min_age, shop_id, submerchant_id);
	}
}