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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.github.jreddit.utils.restclient.HttpRestClient;

public class LinkUnshortener {
	/**
	 * Only works for 301 redirects, which t.co always is
	 * @param rClient
	 * @param url
	 * @return
	 */
	public static String unshorten(HttpRestClient rClient, String url) {
		try {
			HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
			con.setInstanceFollowRedirects(false);
			con.connect();
			
			int responseCode = con.getResponseCode();
			if(responseCode != 301) {
				throw new IllegalArgumentException(url + " does not return 301 as expected");
			}
			
			String destinationUrl = con.getHeaderField("location");
			con.disconnect();
			return destinationUrl;
		}catch(IOException ex) {
			ex.printStackTrace();
			
			return null;
		}
	}
}
