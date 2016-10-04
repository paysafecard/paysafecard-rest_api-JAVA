# Paysafecard payment api JAVA class & examples

## minimal basic usage

```java
package com.paysafecard.payment.test;

import java.awt.Desktop;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.google.gson.JsonObject;
import com.paysafecard.payment.PaysafePaymentController;

public class PaysafecardPaymentTest {

	public static void main(String[] args) {
		PaysafePaymentController pscpayment = new PaysafePaymentController("psc_xl0EwfLX-96bEkjy-mXYD7SFviyvaqA", PaysafePaymentController.Environment.TEST);
		
		// Prepare Customer data
		JsonObject customer = new JsonObject();
		customer.addProperty("id", "asdfhgsdjfgsjdfghj");
		customer.addProperty("ip", "84.200.56.234");
		
		String currency = "EUR";
		double amount = 2000.01;
		
		//Redirect URLs
		String success_url = "http://success.url/{payment_id}";
		String failture_url = "http://failture.url/{payment_id}";
		String notify_url = "http://notify.url/{payment_id}";
		
		String correlation_id = "";
		String country_restriction = "";
		String kyc_restriction = "";
		int min_age = 0;
		int shop_id = 1;
		String submerchant_id = "0";
		
		
		HashMap<String, Object> response =  pscpayment.createPayment(amount, currency, customer,success_url, failture_url, notify_url, correlation_id, country_restriction, kyc_restriction, min_age, shop_id, submerchant_id);
		
		//check if Request was OK
		if(pscpayment.requestIsOk()){
			HashMap<String, Object> redirect = (HashMap<String, Object>) response.get("redirect");
			String auth = (String) redirect.get("auth_url");
			String id = (String) response.get("id");
			
			// Open the Auth URL in the Browser
			try {
			    Desktop.getDesktop().browse(new URL(auth).toURI());
			} catch (Exception e) {}
			
			// Asking the Client if the Payment is finished in the Browser.
			JOptionPane.showMessageDialog(null,
				    "Payment is finished?",
				    "Payment",
				    JOptionPane.PLAIN_MESSAGE);
			
			/*
			* Show Response
			*for(Entry<String, Object> entry : response.entrySet()) {
			*    //System.out.println(entry.getKey()+" "+entry.getValue());
			*}
			*/
			
			HashMap<String, Object> responsePayment =  pscpayment.retrievePayment(id);
			String status = (String) responsePayment.get("status");
			
			
			// Check status. 
			if(status.equals("AUTHORIZED")){
				HashMap<String, Object> responseCapture =  pscpayment.capturePayment(id);
				 status = (String) responseCapture.get("status");
				// If capturing is OK. Show success message.
				if(status.equals("SUCCESS")){
					JOptionPane.showMessageDialog(null,
						    "Payment SUCCESS");
				}
			}else{
				// If capturing is not OK. Show error message.
				if(status.equals("CANCELED_CUSTOMER")){

				}else{
					JOptionPane.showMessageDialog(null,
						    "Error Payment",
						    (String)pscpayment.getError().get("message"),
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		}else{
			JOptionPane.showMessageDialog(null,
					(String)pscpayment.getError().get("message"),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
		
	}

}
```

## examples and extended usage can be found within the script.
