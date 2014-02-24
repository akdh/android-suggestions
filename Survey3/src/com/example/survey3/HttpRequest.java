package com.example.survey3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpRequest {
	
	static InputStream is = null;
	static String data = "";
	
	public static String makeHttpRequest (String url, String method, List<NameValuePair> params) {
		
		if(method == "GET") {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String paramString = URLEncodedUtils.format(params, "utf-8");
			if(!url.endsWith("?"))
		        url += "?";
			url += paramString;
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = null;
			try {
				httpResponse = httpClient.execute(httpGet);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return "ConnectionError";
			} catch (IOException e) {
				e.printStackTrace();
				return "ConnectionError";
			}
			HttpEntity httpEntity = httpResponse.getEntity();
			try {
				is = httpEntity.getContent();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return "ConnectionError";
			} catch (IOException e) {
				e.printStackTrace();
				return "ConnectionError";
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			try {
				while((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
				return "ConnectionError";
			}
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "ConnectionError";
			}
			data = sb.toString();
			return data;
			
		} else if(method == "POST"){
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(params));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				httpClient.execute(httpPost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return "ConnectionError";
			} catch (IOException e) {
				e.printStackTrace();
				return "ConnectionError";
			}
			data = null;
		}
		return data;
	}
}
