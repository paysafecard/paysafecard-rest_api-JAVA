package com.paysafecard.paysafebase;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paysafecard.paysafebase.enums.Environment;
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
 * Created by Pascal on 02.10.2017.
 */
public abstract class PaySafeBase {
	
	public String apiUrl;
	
	public HashMap<String, Object> response;
	public int responseCode = 0;
	
	/**
	 * send a get asynchronously.
	 *
	 * @param consumer whatever you which to do after - can be null!
	 * @param urlParam this params will be added to your link as a get-call.
	 * @param headers other header settings will get added to header.
	 */
	public void sendAsyncGet(Consumer<HashMap<String, Object>> consumer, String urlParam, HashMap<String, String> headers) {
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
			
			headers.keySet().forEach(headerKey -> {
				if(request.containsHeader(headerKey)) {
					request.removeHeaders(headerKey);
				}
				
				request.addHeader(headerKey, headers.get(headerKey));
			});
			
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
	 *
	 * @param consumer whatever you which to do after - can be null!
	 * @param urlParam this params will get posted
	 * @param headers other header settings will get added to header.
	 */
	public void sendAsyncPost(Consumer<HashMap<String, Object>> consumer, String urlParam, HashMap<String, String> headers) {
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
			
			headers.keySet().forEach(headerKey -> {
				if(request.containsHeader(headerKey)) {
					request.removeHeaders(headerKey);
				}
				
				request.addHeader(headerKey, headers.get(headerKey));
			});
			
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
	 *
	 * @param json means a string which should get formatted.
	 * @return a json hashmap with keys and objects.
	 */
	public HashMap<String, Object> parseJson(String json) {
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
	 *
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