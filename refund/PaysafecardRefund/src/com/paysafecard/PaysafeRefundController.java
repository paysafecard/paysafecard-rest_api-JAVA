package com.paysafecard;

import java.io.BufferedReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class PaysafeRefundController {
    private String psc_key = "";
    private String api_url = "";
    private HashMap<String, Object> response;
    private int response_code = 0;
    private String request = "";

    public PaysafeRefundController(String psc_key, Environment environment) {
        this.psc_key = psc_key;
        if (environment == Environment.TEST) {
            this.api_url = "https://apitest.paysafecard.com/v1/payments/";
        } else if (environment == Environment.PRODUCTION) {
            this.api_url = "https://api.paysafecard.com/v1/payments/";
        } else {
            System.err.println("Environment not supported");
        }
    }

    public String getPaymentDetail(String payment){
    	return "";
    }
    
    public HashMap<String, Object> validateRefund(String payment_id, double amount, String currency, String merchantclientid, String customer_mail, String customer_ip, String correlation_id , String submerchant_id){
    	
    	HashMap<String, String> headers = new HashMap<String, String>();
    	
    	if(!correlation_id.equals("")){
    		headers.put("Correlation-ID", correlation_id);
    	}
    	
    	JsonObject customer = new JsonObject();
    	customer.addProperty("id", merchantclientid);
    	customer.addProperty("email", customer_mail);
    	customer.addProperty("first_name", "TEST");
    	customer.addProperty("last_name", "TEST");
    	customer.addProperty("ip", customer_ip);
    	
    	
    	JsonObject jsonObject = new JsonObject();
    	jsonObject.addProperty("amount", Double.toString(amount));
    	jsonObject.addProperty("currency", currency);
    	jsonObject.addProperty("type", "PAYSAFECARD");
    	jsonObject.add("customer", customer);
    	jsonObject.addProperty("capture", "false");
    	
    	if(!submerchant_id.equals("")){
    		jsonObject.addProperty("submerchant_id", submerchant_id);
    	}

    	HashMap<String, Object> response = this.doRequest(payment_id+"/refunds", jsonObject.toString(), Method.POST, headers);
    	
    	return response;
    }
    
    public HashMap<String, Object> executeRefund(String payment_id, String refund_id, double amount, String currency, String merchantclientid, String customer_mail, String customer_ip, String correlation_id , String submerchant_id){
    	
    	HashMap<String, String> headers = new HashMap<String, String>();
    	
    	if(!correlation_id.equals("")){
    		headers.put("Correlation-ID", correlation_id);
    	}
    	
    	JsonObject customer = new JsonObject();
    	customer.addProperty("id", merchantclientid);
    	customer.addProperty("email", customer_mail);
    	customer.addProperty("first_name", "TEST");
    	customer.addProperty("last_name", "TEST");
    	customer.addProperty("ip", customer_ip);
    	
    	
    	JsonObject jsonObject = new JsonObject();
    	jsonObject.addProperty("amount", Double.toString(amount));
    	jsonObject.addProperty("currency", currency);
    	jsonObject.addProperty("type", "PAYSAFECARD");
    	jsonObject.add("customer", customer);
    	jsonObject.addProperty("capture", "false");

    	if(!submerchant_id.equals("")){
    		jsonObject.addProperty("submerchant_id", submerchant_id);
    	}
    	
    	HashMap<String, Object> response = this.doRequest(payment_id+"/refunds/"+refund_id+"/capture", jsonObject.toString(), Method.POST, headers);
    	
    	return response;
    }
    
    //Make direct Refund without validation
    public HashMap<String, Object> directRefund(String payment_id, double amount, String currency, String merchantclientid, String customer_mail, String customer_ip, String correlation_id , String submerchant_id){
    	HashMap<String, String> headers = new HashMap<String, String>();
    	
    	if(!correlation_id.equals("")){
    		headers.put("Correlation-ID", correlation_id);
    	}
    	
    	JsonObject customer = new JsonObject();
    	customer.addProperty("id", merchantclientid);
    	customer.addProperty("email", customer_mail);
    	customer.addProperty("first_name", "TEST");
    	customer.addProperty("last_name", "TEST");
    	customer.addProperty("ip", customer_ip);
    	
    	
    	JsonObject jsonObject = new JsonObject();
    	jsonObject.addProperty("amount", Double.toString(amount));
    	jsonObject.addProperty("currency", currency);
    	jsonObject.addProperty("type", "PAYSAFECARD");
    	jsonObject.add("customer", customer);
    	jsonObject.addProperty("capture", "true");

    	if(!submerchant_id.equals("")){
    		jsonObject.addProperty("submerchant_id", submerchant_id);
    	}
    	
    	HashMap<String, Object> response = this.doRequest(payment_id+"/refunds", jsonObject.toString(), Method.POST, headers);
    	
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
            case 3160:
            	error.put("message", "Invalid customer details. Please forward the customer to contact our support");
                break;
            case 3162:
            	error.put("message", "E-mail address is not registered with mypaysafecard");
                break;
            case 3165:
            	error.put("message", "The amount is invalid. Maximum refund amount cannot exceed the original payment amount");
                break;
            case 3167:
            	error.put("message", "Customer limit exceeded. Please forward the customer to contact our support");
                break;
            case 3179:
            	error.put("message", "The amount is invalid. Maximum refund amount cannot exceed the original payment amount");
                break;
            case 3180:
            	error.put("message", "Original Transaction is in an invalid state");
                break;
            case 3181:
            	error.put("message", "Merchantclient-ID is not matching with original transaction");
                break;
            case 3182:
            	error.put("message", "Merchantclient-ID is a mandatory parameter");
                break;
            case 3184:
            	error.put("message", "Original payment transaction does not exist");
                break;
            case 10028:
            	error.put("message", "One or more neccessary parameters are empty");
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
