/* 
    This file is part of TwitterToReddit.

    TwitterToReddit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TwitterToReddit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TwitterToReddit.  If not, see http://www.gnu.org/licenses/
*/
package me.timothy.twittertoreddit;

import java.io.File;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.Properties;

import org.json.simple.JSONObject;

import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.methodbuilders.HttpGetMethodBuilder;

public class LinkUnshortener {
	public static String apiKey;
	
	public static String unshorten(HttpRestClient rClient, String url) {
		if(apiKey == null)
			throw new RuntimeException("apiKey not loaded");
		try {
			String encUrl = URLEncoder.encode(url, "UTF-8");
			String unshortenUrl = "http://api.unshorten.it?shortURL=" + encUrl + "&responseFormat=json&apiKey=" + apiKey;
			JSONObject jObj = ((JSONObject) rClient.get(HttpGetMethodBuilder.httpGetMethod().withUrl(unshortenUrl)).getResponseObject());
			return (String) jObj.get("fullurl");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void loadConfiguration() {
		try {
			File file = new File("unshortenit.ini");
			if(!file.exists())
				throw new RuntimeException("unshortenit.ini must exist and have apiKey");
			
			Properties props = new Properties();
			try(FileReader fr = new FileReader(file)) {
				props.load(fr);
			}
			
			if(props.isEmpty() || !props.containsKey("apiKey"))
				throw new RuntimeException("unshortenit.ini must exist and have apiKey");
			
			apiKey = props.getProperty("apiKey");
		}catch(Exception e) {
			if(e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}
}
