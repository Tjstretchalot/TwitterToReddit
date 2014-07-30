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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.parser.ParseException;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.UserStreamAdapter;
import twitter4j.UserStreamListener;

import com.github.jreddit.utils.restclient.HttpRestClient;

public class TwitterToReddit {
	public static final Pattern URL_PATTERN = Pattern.compile("(http://(bit\\.ly|t\\.co|lnkd\\.in|tcrn\\.ch)\\S*)\\b");
	private HttpRestClient restClient;
	private com.github.jreddit.user.User redditUser;
	private UserStreamListener listener;
	private long twitterId;
	private String subreddit;
	
	public TwitterToReddit(HttpRestClient restClient, com.github.jreddit.user.User redditUser, long twId, String sub) {
		this.restClient = restClient;
		this.redditUser = redditUser;
		this.twitterId = twId;
		this.subreddit = sub;
		
		listener = new UserStreamAdapter() { 
			@Override
			public void onStatus(final Status status) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						postTweet(status);
					}
					
				}).start();
			}
			
		};
	}
	
	public void postTweet(Status status) {
		if(status.getUser().getId() != twitterId)
			return; 
		String text = status.getText();
		// Find the first hyperlink
		System.out.println(status.getUser().getName() + " - " + text);
		Matcher matcher = URL_PATTERN.matcher(text);
		if(matcher.find()) {
			String firstLink = matcher.group();
			System.out.println("  Link: " + firstLink);
			
			firstLink = LinkUnshortener.unshorten(restClient, firstLink);
			System.out.println("  Unshortened: " + firstLink);
			String realText = text.substring(0, matcher.start()).trim();
			
			try {
				redditUser.submitLink(realText, firstLink, subreddit);
			} catch (IOException | ParseException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}else {
			System.out.println("  Failed to find link");
		}
	}
	
	public void beginStreaming(TwitterStream stream) {
		stream.addListener(listener);
	}
	
	public void stopStreaming(TwitterStream stream) {
		stream.removeListener(listener);
	}
}
