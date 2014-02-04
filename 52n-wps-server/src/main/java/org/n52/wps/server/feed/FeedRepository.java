/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
