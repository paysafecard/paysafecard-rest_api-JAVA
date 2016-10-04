# Paysafecard payment api JAVA class & examples

## minimal basic usage

```java
package com.paysafecard.test;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.paysafecard.PaysafeRefundController;

public class PaysafecardRefundTest {

	public static void main(String[] args) {
		PaysafeRefundController pscrefund = new PaysafeRefundController("psc_xl0EwfLX-96bEkjy-mXYD7SFviyvaqA", PaysafeRefundController.Environment.TEST);
		
		String payment_id = "pay_1000005843_testCorrID_5780325650790_EUR";
		double amount = 0.01;
		String currency = "EUR";
		String merchant_client_id = "cc03e747a6afbbcbf8be7668acfebee5";
		String client_mail = "psc.mypins+matwal_blFxgFUJfbNS@gmail.com";
		String client_ip = "127.0.0.1";
		
		String correlation_id = "";
		String submerchant_id ="";
		
		HashMap<String, Object> responseRefund = pscrefund.validateRefund(payment_id, amount, currency, merchant_client_id, client_mail, client_ip, correlation_id , submerchant_id);
		String refund_id = (String)responseRefund.get("id");
		String status = (String)responseRefund.get("status");		
		
		/*
		* Show Response
		*for(Entry<String, Object> entry : responseRefund.entrySet()) {
		*    //System.out.println(entry.getKey()+" "+entry.getValue());
		*}
		*/
		
		if(pscrefund.requestIsOk()){
			if(status.equals("VALIDATION_SUCCESSFUL")){
				HashMap<String, Object> responseRefundExecute = pscrefund.executeRefund(payment_id, refund_id, amount, currency, merchant_client_id, client_mail, client_ip, correlation_id , submerchant_id);
				status = (String)responseRefundExecute.get("status");
				if(status.equals("SUCCESS")){
					/*
					 * --------------------------------------
					 * 				Refund is successful
					 * --------------------------------------
					 */
					JOptionPane.showMessageDialog(null,
						    "Refund SUCCESS");
				}else{
					JOptionPane.showMessageDialog(null,
							(String)pscrefund.getError().get("message"),
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}else{
				JOptionPane.showMessageDialog(null,
						(String)pscrefund.getError().get("message"),
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
			}
		}else{
			JOptionPane.showMessageDialog(null,
					(String)pscrefund.getError().get("message"),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
		
		
		
	}

}
```

## examples and extended usage can be found within the script.
