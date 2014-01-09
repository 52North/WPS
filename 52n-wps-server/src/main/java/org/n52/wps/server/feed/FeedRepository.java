/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.server.feed;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RemoteRepositoryDocument.RemoteRepository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.feed.movingcode.MovingCodeObject;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class FeedRepository {
	private static FeedRepository instance = new FeedRepository();
	private static final String FEED = "FEED";
	private static final String LOCAL_FEED_MIRROR = "LOCAL_FEED_MIRROR";
	private AlgorithmFeed[] registeredFeeds;
	
	private FeedRepository(){
		
		HashMap<String,String> feeds = new HashMap<String,String>();
		RemoteRepository[] remoteRepositories = WPSConfig.getInstance().getWPSConfig().getRemoteRepositoryList().getRemoteRepositoryArray();
		
		for (int i = 0; i < remoteRepositories.length; i++){
			Property[] props = remoteRepositories[i].getPropertyArray();
			String url = null;
			String localPath = null;
			for (Property currentProp : props){
				if (currentProp.getActive() && currentProp.getName().equalsIgnoreCase(FEED)){
					url = currentProp.getStringValue();
				}
				if (currentProp.getActive() && currentProp.getName().equalsIgnoreCase(LOCAL_FEED_MIRROR)){
					localPath = currentProp.getStringValue();
				}
			}
			
			// eventually occurring duplicate feeds are silently eliminated by the HashMap (unique URL!)
			if (url != null && localPath != null){
				feeds.put(url, localPath);
			}
		}
		
		registerFeeds(feeds);
	}
	
	public static FeedRepository getInstance(){
		return instance;
	}
	
	private void registerFeeds(HashMap<String,String> feedsMap){
		ArrayList<AlgorithmFeed> feedList = new ArrayList<AlgorithmFeed>();
		
		Set<String> allUrls = feedsMap.keySet();
		
		for (String url : allUrls){
			AlgorithmFeed feed = null;
			
			// try to instantiate an AlgorithmFeed
			try {
				feed = new AlgorithmFeed(url, feedsMap.get(url));
			} catch (Exception e) {
				// do nothing
			}
			
			// be careful not to add NULL feeds
			if (feed != null){
				feedList.add(feed);
			}
		}
		
		registeredFeeds = feedList.toArray(new AlgorithmFeed[feedList.size()]);
	}
	
	public MovingCodeObject[] getMovingCodeObjects (URI[] supportedContainerURNs, URI[] providedComponentURN){
		ArrayList<MovingCodeObject> mocList = new ArrayList<MovingCodeObject>();
		for (AlgorithmFeed currentFeed : registeredFeeds){
			mocList.addAll(currentFeed.getMovingCodeObjects(supportedContainerURNs, providedComponentURN));
		}
		
		MovingCodeObject[] mocArray = mocList.toArray(new MovingCodeObject[mocList.size()]);
		
		return mocArray;
	}
}
