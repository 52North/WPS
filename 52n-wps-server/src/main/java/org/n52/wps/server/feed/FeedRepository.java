package org.n52.wps.server.feed;

import java.net.URI;
import java.util.ArrayList;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.feed.movingcode.MovingCodeObject;

public class FeedRepository {
	private static FeedRepository instance = new FeedRepository();
	private static final String FEED = "FEED";
	private static final String LOCAL_FEED_MIRROR = "LOCAL_FEED_MIRROR";
	private String localFeedPath; //"D:/development/localFeedMirror";
	private ArrayList<String> feedsURLs; //{"http://test.org/feed.zip","http://test2.org/feed.zip"};
	private AlgorithmFeed[] registeredFeeds;
	
	private FeedRepository(){
		
		feedsURLs = new ArrayList<String>();
		
		Property[] props = WPSConfig.getInstance().getWPSConfig().getServer().getPropertyArray();
		for (Property currentProp : props){
			
			if (currentProp.getActive() && currentProp.getName().equalsIgnoreCase(FEED)){
				feedsURLs.add(currentProp.getStringValue());
			}
			if (currentProp.getActive() && currentProp.getName().equalsIgnoreCase(LOCAL_FEED_MIRROR)){
				localFeedPath = currentProp.getStringValue();
			}
		}
		
		registerFeeds();
	}
	
	public static FeedRepository getInstance(){
		return instance;
	}
	
	private void registerFeeds(){
		ArrayList<AlgorithmFeed> feedList = new ArrayList<AlgorithmFeed>();
		for (String currentURL : feedsURLs){
			AlgorithmFeed feed = new AlgorithmFeed(currentURL, localFeedPath);
			feedList.add(feed);
		}
		
		registeredFeeds = feedList.toArray(new AlgorithmFeed[0]);
	}
	
	public MovingCodeObject[] getMovingCodeObjects (URI containerURN, URI[] providedComponentURN){
		ArrayList<MovingCodeObject> mocList = new ArrayList<MovingCodeObject>();
		for (AlgorithmFeed currentFeed : registeredFeeds){
			mocList.addAll(currentFeed.getMovingCodeObjects(containerURN, providedComponentURN));
		}
		
		MovingCodeObject[] mocArray = mocList.toArray(new MovingCodeObject[0]);
		mocList = null;
		
		return mocArray;
	}
}
