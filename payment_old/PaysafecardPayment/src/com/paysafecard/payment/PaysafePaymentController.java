package com.paysafecard.payment;

import java.io.BufferedReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class PaysafePaymentController {
    private String psc_key = "";
    private String api_url = "";
    private HashMap<String, Object> response;
    private int response_code = 0;
    private String request = "";

    public PaysafePaymentController(String psc_key, Environment environment) {
        this.psc_key = psc_key;
        if (environment == Environment.TEST) {
            this.api_url = "https://apitest.paysafecard.com/v1/payments/";;
            //this.api_url = "http://psc.hosting-core.de/payment.php";;
        } else if (environment == Environment.PRODUCTION) {
            this.api_url = "https://api.paysafecard.com/v1/payments/";
        } else {
            System.err.println("Environment not supported");
        }
    }

    /**
     *
     * @param amount necessary | the amount to pay, i.e. "10.00"
     * @param currency necessary | currency of the payment, i.e. "EU"
     * @param customer_id necessary | your customer id (merchant client id)
     * @param customer_ip necessary | the customers IP
     * @param success_url necessary | failure url to redirect customer to on
     * success, i.e.
     * "http://www.yourdomain.com/success.php?payment_id={payment_id}"
     * @param failure_url necessary | failure url to redirect customer to on
     * failure, i.e.
     * "http://www.yourdomain.com/failure.php?payment_id={payment_id}"
     * @param notification_url necessary | notification url for payment infos ,
     * i.e. "http://www.yourdomain.com/notification.php?payment_id={payment_id}"
     * @param correlation_id default = "" | optional | parameter to reference
     * @param country_restriction default = "" | optional | retrict to specific
     * country, i.e. "DE"
     * @param kyc_restriction default = "" | optional | restrict kyc
     * @param min_age default = 0 | optional | set customers minimal age, i.e.
     * 18
     * @param shop_id default = 0 | optional | set your shop id
     * @param submerchant_id default = 0 | optional | set submerchant id for
     * reporting
     * @return 
     */
    public HashMap<String, Object> createPayment(double amount, String currency,  JsonObject customer, String success_url, String failure_url, String notification_url, String correlation_id, String country_restriction, String kyc_restriction, int min_age, int shop_id, String submerchant_id) {
    	HashMap<String, String> headers = new HashMap<String, String>();
    	HashMap<String, Object> parameters = new HashMap<String, Object>();
    
    	if(!correlation_id.equals("")){
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
    	
    	if(!country_restriction.equals("")){
    		jsonObject.addProperty("country_restriction", country_restriction);
    	}
    	
    	if(!kyc_restriction.equals("")){
    		jsonObject.addProperty("kyc_level", kyc_restriction);
    	}
    	
    	if(min_age != 0){
    		jsonObject.addProperty("min_age", min_age);
    	}
    	
    	if(!submerchant_id.equals("")){
    		jsonObject.addProperty("submerchant_id", submerchant_id);
    	}
    	
    	
    	HashMap<String, Object> response = this.doRequest("", jsonObject.toString(), Method.POST, headers);

    	return response;
    }
    
    
    public HashMap<String, Object> capturePayment(String payment){
    	HashMap<String, String> headers = new HashMap<String, String>();
    	
    	JsonObject jo = new JsonObject();
    	jo.addProperty("id", payment);
    	
    	HashMap<String, Object> response = this.doRequest(payment+"/capture", jo.toString(), Method.POST, headers);
    	return response; 
    }
    public HashMap<String, Object> retrievePayment(String payment){
    	HashMap<String, String> headers = new HashMap<String, String>();
    	HashMap<String, Object> response = this.doRequest(payment, "", Method.GET, headers);
    	
    	return response; 
    }

    /**
     * Requests
     */
    private HashMap<String, Object> doRequest(String url_param, String parameters, Method method, HashMap<String, String> headers) {

    	HashMap<String, Object> response = null;

        if (method == Method.GET) {
        	
            response = sendGet(url_param, parameters, headers); // empty path, default URL request, 
        } else if (method == Method.POST) {
            response = sendPost(url_param, parameters, headers);
        }

        return response;
    }

    private HashMap<String, Object> sendGet(String urlparam, String parameters, HashMap<String, String> headers) {
        try {

            String USER_AGENT = "Mozilla/5.0";
            URL obj = new URL(this.api_url + urlparam);
            System.setProperty("http.keepAlive", "false");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");
            con.setUseCaches(false); 

            //con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encode(this.psc_key.getBytes()).toString());
            con.setRequestProperty("Authorization", "Basic cHNjX3hsMEV3ZkxYLTk2YkVranktbVhZRDdTRnZpeXZhcUE=");
            con.setRequestProperty("Content-Type", "application/json");
            
            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept", "*/*");
            con.setRequestProperty("charset", "utf-8");
            for (String headerKey : headers.keySet()) {
                con.setRequestProperty(headerKey, headers.get(headerKey));
            }

            this.response_code = con.getResponseCode();
            BufferedReader in;
            if (this.response_code >= 400) {
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
            this.response = this.parseJson(response.toString());
        	return this.response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // HTTP POST request
    private HashMap<String, Object> sendPost(String url_param, String parameters, HashMap<String, String> headers) {

        try {
            String USER_AGENT = "Mozilla/5.0";
            URL obj = new URL(this.api_url+url_param);
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

            this.response_code = con.getResponseCode();
            BufferedReader in = null;
            if (this.response_code >= 400) {
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
            this.response = this.parseJson(response.toString());
        	return this.response;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private HashMap<String, Object> parseJson(String json){
    	HashMap<String, Object> response = new HashMap<String, Object>();
    	
    	JsonParser jsonParser = new JsonParser();
    	JsonObject jo = (JsonObject)jsonParser.parse(json);
    	
    	JsonObject obj = jo.getAsJsonObject();
    	Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
    	for (Map.Entry<String, JsonElement> entry: entries) {
    		if(entry.getValue().toString().contains("{")){
    			//System.out.println("Sub: " +entry.getValue().toString());
    			HashMap<String, Object> response2 = this.parseJson(entry.getValue().toString().replace("[", "").replace("]", ""));
    			response.put(entry.getKey().toString(), response2);
    		}else{
    			response.put(entry.getKey().toString(), entry.getValue().toString().replace("\"", ""));
    		}	
    	}
    	
		return response;
    }
    
    public HashMap<String, Object> getError(){
    	HashMap<String, Object> error = new HashMap<String, Object>();
        if (this.response_code > 200) {
        	
            switch (this.response_code) {
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
        int error_number = Integer.parseInt((String) this.response.get("number"));
        
        switch (error_number) {
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

    public boolean requestIsOk(){
    	if(this.response_code < 300){
    		return true;
    	}else{
    		return false;
    	}
    }
    public enum Method {
        GET, POST
    }

    public enum Environment {
        TEST, PRODUCTION
    }
}
