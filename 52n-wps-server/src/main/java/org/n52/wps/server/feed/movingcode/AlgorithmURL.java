package org.n52.wps.server.feed.movingcode;

import java.net.URI;
import java.util.StringTokenizer;

public class AlgorithmURL {
	
	private final URI uri;
	private final String publicPath;
	private final String privatePath;
	private static final String SCHEME = "algorithm://";
	private static final String TOKEN = "?";
	
	public AlgorithmURL (String str){
		uri = URI.create(str);
		if (isValid()){
			StringTokenizer st = new StringTokenizer(uri.getSchemeSpecificPart(), TOKEN);
			if (st.hasMoreTokens()){
				publicPath = st.nextToken();
			} else {
				publicPath = null;
			}
			if (st.hasMoreTokens()){
				privatePath = st.nextToken();
			} else {
				privatePath = null;
			}
		} else {
			publicPath = null;
			privatePath = null;
		}
	}
	
	public boolean isValid(){
		if (uri.getScheme().equalsIgnoreCase(SCHEME)){
			return true;
		}
		else return false;
	}
	
	public String getPublicPath(){
		return publicPath;
	}
	
	public String getPrivatePath(){
		return privatePath;
	}
	
}
