package org.n52.wps.server.feed;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RemoteRepositoryDocument;
import org.n52.wps.RemoteRepositoryDocument.RemoteRepository;
import org.n52.wps.RemoteRepositoryListDocument.RemoteRepositoryList;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.commons.WPSConfig;

public class FeedServlet extends HttpServlet {

	private static transient Logger LOGGER = LoggerFactory.getLogger(FeedServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -6316984224210904216L;

	private final String getParamName = "name";
	private final String getParamFeed = "feed";
	private final String getParamLocalFeedMirror = "localfeedmirror";
	private final String propertyFeed = "FEED";
	private final String propertyLocalFeedMirror = "LOCAL_FEED_MIRROR";
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		/*
		 * how can the new remote repository be posted?! 
		 * 1. complete xml snippet
		 * 2. what else?!
		 */

		RemoteRepository newRemoteRepo = null;
		
		OutputStream out = res.getOutputStream();
		try {
			InputStream is = req.getInputStream();
			if (req.getParameterMap().containsKey("request")) {
				is = new ByteArrayInputStream(req.getParameter("request")
						.getBytes("UTF-8"));
			}

			// WORKAROUND cut the parameter name "request" of the stream
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			StringWriter sw = new StringWriter();
			int k;
			while ((k = br.read()) != -1) {
				sw.write(k);
			}
			LOGGER.debug(sw.toString());
			String s;
			String reqContentType = req.getContentType();
			if (sw.toString().startsWith("request=")) {
				if (reqContentType.equalsIgnoreCase("text/plain")) {
					s = sw.toString().substring(8);
				} else {
					s = URLDecoder.decode(sw.toString().substring(8), "UTF-8");
				}
				LOGGER.debug(s);
			} else {
				s = sw.toString();
			}
			
			newRemoteRepo = RemoteRepositoryDocument.Factory.parse(s).getRemoteRepository();
				
			addNewRemoteRepository(newRemoteRepo);
			
			res.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error occured");
		}
		out.flush();
		out.close();

	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		RemoteRepository newRemoteRepo = null;
		
		OutputStream out = res.getOutputStream();
		try {

			Map<String, String[]> parameters = (Map<String, String[]>)req.getParameterMap();

			CaseInsensitiveMap ciMap = new CaseInsensitiveMap(parameters);
			
			if(!ciMap.keySet().contains(getParamName)){
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"parameter '" + getParamName + "' not found");				
			}else if(!ciMap.keySet().contains(getParamFeed)){
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"parameter '" + getParamFeed + "' not found");
			}else if(!ciMap.keySet().contains(getParamLocalFeedMirror)){
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"parameter '" + getParamLocalFeedMirror + "' not found");
				
			}
			
			newRemoteRepo = RemoteRepository.Factory.newInstance();
			
			newRemoteRepo.setName(((String[])ciMap.get(getParamName))[0]);
			newRemoteRepo.setActive(true);
			
			Property feedProp = newRemoteRepo.addNewProperty();
			
			feedProp.setName(propertyFeed);
			feedProp.setStringValue(((String[])ciMap.get(getParamFeed))[0]);
			feedProp.setActive(true);
			
			Property localFeedMirrorProp = newRemoteRepo.addNewProperty();
			
			localFeedMirrorProp.setName(propertyLocalFeedMirror);
			localFeedMirrorProp.setStringValue(((String[])ciMap.get(getParamLocalFeedMirror))[0]);
			localFeedMirrorProp.setActive(true);
			
			addNewRemoteRepository(newRemoteRepo);
			res.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error occured");
		}
		out.flush();
		out.close();
		
	}
	
	private void addNewRemoteRepository(RemoteRepository newRemoteRepository) throws Exception {

		String configurationPath = WPSConfig.getConfigPath();
		File XMLFile = new File(configurationPath);

		WPSConfigurationDocument wpsCon = WPSConfigurationDocument.Factory
				.parse(XMLFile);

		RemoteRepositoryList remoteRepoList = wpsCon.getWPSConfiguration()
				.getRemoteRepositoryList();

		if (remoteRepoList == null) {
			remoteRepoList = wpsCon.getWPSConfiguration()
					.addNewRemoteRepositoryList();
		}

		int newNumberOfRemoteRepos = remoteRepoList.sizeOfRemoteRepositoryArray() + 1;
		
		RemoteRepository[] remoteRepos = remoteRepoList.getRemoteRepositoryArray();
		RemoteRepository[] newRemoteRepos = new RemoteRepository[newNumberOfRemoteRepos];
		
		for (int i = 0; i < remoteRepos.length; i++) {
			newRemoteRepos[i] = remoteRepos[i];
		}
		
		newRemoteRepos[newNumberOfRemoteRepos - 1] = newRemoteRepository;
		
		remoteRepoList.setRemoteRepositoryArray(newRemoteRepos);

		wpsCon.save(XMLFile, new org.apache.xmlbeans.XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());

	}
	

}
