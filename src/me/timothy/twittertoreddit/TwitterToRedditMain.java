package me.timothy.twittertoreddit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import com.github.jreddit.user.User;
import com.github.jreddit.utils.restclient.HttpRestClient;

public class TwitterToRedditMain {
	public static void main(String[] args) {
		try {
			new TwitterToRedditMain().begin();
		}catch(InterruptedException ie) {
			ie.printStackTrace();
		}
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
		LinkUnshortener.loadConfiguration();
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
