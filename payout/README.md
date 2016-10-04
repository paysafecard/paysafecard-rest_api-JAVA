# Paysafecard payment api JAVA class & examples

## minimal basic usage

```java
package com.paysafecard.test;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.paysafecard.PaysafePayoutController;
public class PaysafecardPayoutTest {

	public static void main(String[] args) {
		PaysafePayoutController pscpayout = new PaysafePayoutController("psc_xl0EwfLX-96bEkjy-mXYD7SFviyvaqA", PaysafePayoutController.Environment.TEST);
		
		HashMap<String, Object> responsePayment =  pscpayout.getLimits("EUR");
		
		/*
		 * Show Payment Limit result
		 *for(Entry<String, Object> entry : responsePayment.entrySet()) {
		 *    System.out.println(entry.getKey()+" "+entry.getValue());
		 *}
		 */
		double amount = 0.10;
		String currency = "EUR";
		String client_id = "434186408713";
		String customer_mail = "VrAtTRLRyS@avUVWdRVeH.NYE";
		String customer_firstname ="Test";
		String customer_lastname ="BubxNFGHwdGCElzbmjxsycWdYX";
		String customer_birthday ="1986-06-16";
		String customer_ip = "84.59.200.59";
		
		String correlation_id = "";
		String submerchant_id = "";
		
		
		
		HashMap<String, Object> responseValidate =  pscpayout.validatePayout(amount, currency, client_id, customer_mail, customer_ip, customer_firstname, customer_lastname, customer_birthday, correlation_id, submerchant_id );
		String payout_id = (String) responseValidate.get("id");
		String status = (String) responseValidate.get("status");
		
		if(pscpayout.requestIsOk()){
			if(status.equals("VALIDATION_SUCCESSFUL")){
				HashMap<String, Object> responseExecute =  pscpayout.executePayout(payout_id, amount, currency, client_id, customer_mail, customer_ip, customer_firstname, customer_lastname, customer_birthday, correlation_id, submerchant_id);
				status = (String) responseExecute.get("status");
				if(status.equals("SUCCESS")){
					/*
					 * --------------------------------------
					 * 				Payout is successful
					 * --------------------------------------
					 */
					
					JOptionPane.showMessageDialog(null,
						    "Payout SUCCESS");
				}else{
					JOptionPane.showMessageDialog(null,
							(String)pscpayout.getError().get("message"),
						    "Error Payment",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		}else{
			JOptionPane.showMessageDialog(null,
					(String)pscpayout.getError().get("message"),
				    "Error Payment",
				    JOptionPane.ERROR_MESSAGE);
		}
		
	}

}

```

## examples and extended usage can be found within the script.
