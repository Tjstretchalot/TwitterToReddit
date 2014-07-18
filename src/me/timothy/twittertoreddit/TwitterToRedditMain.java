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

import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import twitter4j.FilterQuery;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import com.github.jreddit.user.User;
import com.github.jreddit.utils.restclient.HttpRestClient;

public class TwitterToRedditMain {
	public static void main(String[] args) {
		System.out.println("TwitterToReddit  Copyright (C) 2014  Timothy Moore\n" +
				"This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.\n" + 
				"This is free software, and you are welcome to redistribute it\n" +
				"under certain conditions; type `show c' for details.");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					new TwitterToRedditMain().begin();
				}catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}).start();
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				Scanner sc = new Scanner(System.in);
				while(true) {
					String ln = sc.nextLine();
					if(ln.equalsIgnoreCase("show w")) {
						System.out.println("See http://www.gnu.org/licenses/gpl-3.0.html for more information");
						System.out.println("THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW. EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM “AS IS” WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.");
					}else if(ln.equalsIgnoreCase("show c")) {
						System.out.println("See http://www.gnu.org/licenses/gpl-3.0.html for more information");
						System.out.println("You may make, run and propagate covered works that you do not convey, without conditions so long as your license otherwise remains in force. You may convey covered works to others for the sole purpose of having them make modifications exclusively for you, or provide you with facilities for running those works, provided that you comply with the terms of this License in conveying all material for which you do not control copyright. Those thus making or running the covered works for you must do so exclusively on your behalf, under your direction and control, on terms that prohibit them from making any copies of your copyrighted material outside their relationship with you.");
					}
				}
			}
		});
		th.setDaemon(true);
		th.start();
	}

	private String username;
	private String password;
	private long twitterId;
	private String subreddit;

	private void begin() throws InterruptedException {
		loadConfiguration();
		HttpRestClient rClient = new HttpRestClient();
		rClient.setUserAgent("TwitterToReddit Bot by /u/Tjstretchalot");
		
		User user = new User(rClient, username, password);
		try {
			user.connect();
		}catch(Exception e) {
			System.err.println("Failed to connect");
			return;
		}
		
		System.out.println("Succesfully Authenticated to Reddit");
		Thread.sleep(1000);
		System.out.println("..");
		Thread.sleep(1000);
		System.out.println("Authenticating to Twitter..");
		
		TwitterStream stream = new TwitterStreamFactory().getInstance();
		FilterQuery filter = new FilterQuery();
		filter.follow(new long[] { twitterId });
		
		TwitterToReddit mBot = new TwitterToReddit(rClient, user, twitterId, subreddit);
		mBot.beginStreaming(stream);

		stream.filter(filter);
		
		System.out.println("Success! The bot is now active and will post new tweets as well as print out here");
		System.out.println();
		
		
	}
	
	private void loadConfiguration() {
		File file = new File("user.ini");
		if(!file.exists()) {
			System.out.println("Please create user.ini that resembles:");
			System.out.println("username=asdf");
			System.out.println("password=ghjk");
			System.out.println("subreddit=asdf");
			System.out.println("twitterId=1234");
			System.exit(0);
		}

		Properties props = new Properties();
		try(FileReader in = new FileReader(file)) {
			props.load(in);
		}catch(IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}

		if(!(props.containsKey("username")) || !(props.containsKey("password"))
				|| !(props.containsKey("subreddit")) || !(props.containsKey("twitterId"))) {
			System.out.println("user.ini needs to have:");
			System.out.println("  username, password, subreddit, twitterId");
			System.exit(0);
		}

		username = props.getProperty("username");
		password = props.getProperty("password");
		subreddit = props.getProperty("subreddit");
		try {
			twitterId = Integer.valueOf(props.getProperty("twitterId"));
		}catch(NumberFormatException nfe) {
			System.out.println("Twitter id is invalid (" + nfe.getMessage() + ")");
			System.exit(1);
		}
	}
}
