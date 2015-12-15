package org.n52.wps.mc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.n52.movingcode.runtime.GlobalRepositoryManager;
import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import org.n52.movingcode.runtime.coderepository.MovingCodeRepository;
import org.n52.movingcode.runtime.coderepository.RepositoryChangeListener;
import org.n52.movingcode.runtime.processors.ProcessorFactory;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;
import org.n52.wps.webapp.api.types.URIConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.wps.x100.ProcessDescriptionType;

/**
 * Module Configuration for {@link MCProcessRepository}.
 * 
 * @author matthias
 */
public class MCProcessRepository extends ClassKnowingModule implements IAlgorithmRepository{
	
	/**
	 * Initial configuration options.
	 * TODO: move to config file.
	 */
	static final String configFile = "mc-config.json";
	static final String REPO_FEED_REPO_PARAM = "remote_repositories";
	static final String LOCAL_ZIP_REPO_PARAM = "local_repositories";
	
	static final String LOCAL_REPO_KEY = "LOCAL_REPO";
	static final String REMOTE_REPO_KEY = "REMOTE_REPO";
	
	private volatile boolean isActive = false;
	private final List<? extends ConfigurationEntry<?>> configurationEntries = readConfig();
	
	private static Logger LOGGER = LoggerFactory.getLogger(MCProcessRepository.class);
	
	// use the GlobalRepoManager from mc-runtime for the process inventory
	private GlobalRepositoryManager rm = GlobalRepositoryManager.getInstance();

	// valid functionIDs
	// needs to be volatile since this field may be updated by #updateContent()
	private volatile Collection<String> supportedFunctionIDs = Collections.emptyList();
	
	public MCProcessRepository() {
		super();
		
		// register a change listener with the GlobalRepositoryManager
		// to listen for content updates
		rm.addRepositoryChangeListener(new RepositoryChangeListener() {

			@Override
			public void onRepositoryUpdate(MovingCodeRepository updatedRepo) {
				// trigger a content update of this repo
				updateContent();
				LOGGER.info("Moving Code repository content has changed. Content update finished.");
			}
		});
		
		// trigger remote repo init in separate thread
		Thread tLoadRemote = new LoadRepoThread(this);
		tLoadRemote.start();
	}
	
	@Override
	public String getModuleName() {
		return "Moving Code Configuration Module";
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void setActive(boolean active) {
		isActive = active;
	}

	@Override
	public ConfigurationCategory getCategory() {
		return ConfigurationCategory.REPOSITORY;
	}

	@Override
	public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
		return configurationEntries;
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries() {
		List<AlgorithmEntry> algos = new ArrayList<>();
		for (String fid : supportedFunctionIDs){
			algos.add(new AlgorithmEntry(fid, true));
		}
		return algos;
	}

	@Override
	public List<FormatEntry> getFormatEntries() {
		return null;
	}

	@Override
	public String getClassName() {
		return MCProcessRepository.class.getName();
	}
	
	
	/**
	 * Properties reader
	 * 
	 * @return
	 */
	private static final List<? extends ConfigurationEntry<?>> readConfig(){
		List<ConfigurationEntry<?>> configList = new ArrayList<>();
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream(configFile);
		
		int lineCnt = 0;
		try(BufferedReader br = new BufferedReader(new InputStreamReader(is))){
			{
				String line;
		        while((line = br.readLine()) != null) {
		        	switch (getType(line)) {
					case EMPTY:
						break;
					case COMMENT:
						break;
					case LOCAL:
						configList.add(new StringConfigurationEntry(LOCAL_REPO_KEY, "Local Repository Folder", "Absolute path to a local repository folder", false, line.trim()));
						break;
					case REMOTE:
						URI uri = toURI(line.trim());
						if (uri != null) {
							configList.add(new URIConfigurationEntry(REMOTE_REPO_KEY, "Remote Repository Feed", "URL of a remote feed repository", false, uri));
						} else {
							LOGGER.error("Invalid repository URL in line {}.", lineCnt);
						}
						break;
					default:
						break;
					}
		        	lineCnt++;
		        }
			}
		} catch (IOException e) {
			LOGGER.error("Could not parse repository configuration. There was an error in line {}", lineCnt);
		}
		
		return configList;
	}
	
	private static URI toURI(String s){
		try {
			return new URI(s);
		} catch (URISyntaxException e) {
			LOGGER.error("{} is not a valid URL", s);
			return null;
		}
	}
	
	private static LineType getType(String line){
		String s = line.trim();
		if (s.length() == 0){
			return LineType.EMPTY;
		}else if (s.startsWith("#")){
			return LineType.COMMENT;
		} else if (s.startsWith("http://")){
			return LineType.REMOTE;
		} else if (s.startsWith("https://")){
			return LineType.REMOTE;
		} else {
			return LineType.LOCAL;
		}
	}
	
	
	private enum LineType {
		COMMENT, REMOTE, LOCAL, EMPTY
	}
	
	private synchronized void updateContent(){

		// run this block if validFunctionIDs are not yet available
		// checks which available functions can be executed with current configuration

		// 1. get all available functionIDs
		String[] fids = rm.getFunctionIDs();
		ArrayList<String> exFIDs = new ArrayList<String>();

		// 2. for each function ID
		for (String currentFID : fids) {
			// 2.a retrieve implementing packages
			MovingCodePackage[] mcps = rm.getPackageByFunction(currentFID);
			// 2.b check whether each one of them can be executed
			//     by the current processor configuration
			for (MovingCodePackage currentMCP : mcps) {
				boolean supported = ProcessorFactory.getInstance().supportsPackage(currentMCP);
				if (supported) {
					exFIDs.add(currentFID);
					break;
				}
			}
		}

		supportedFunctionIDs = exFIDs;
	}
	
	/**
	 * 
	 * @author Matthias Mueller
	 *
	 */
	private final class LoadRepoThread extends Thread {
		
		private final MCProcessRepository master;
		
		private LoadRepoThread(MCProcessRepository master){
			this.master = master;
		}
		
		@Override
		public void run() {
			
			// for each remote repository: add to RepoManager
			for (ConfigurationEntry<?> cEntry : master.configurationEntries) {
				if (cEntry.getKey().equalsIgnoreCase(REMOTE_REPO_KEY)
						&& cEntry.getValue() instanceof URI){
					// convert value to URL, check and register
					try {
						URL repoURL = ((URI) cEntry.getValue()).toURL();
						rm.addRepository(repoURL);
						LOGGER.info("Added MovingCode Repository: " + REMOTE_REPO_KEY + " - "
								+ repoURL.toString());
					}
					catch (MalformedURLException e) {
						LOGGER.warn("MovingCode Repository is not a valid URL: " + REMOTE_REPO_KEY + " - "
								+ cEntry.getValue().toString());
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						LOGGER.error("Error loading repository: " + REMOTE_REPO_KEY + " - "
								+ cEntry.getValue().toString());
					}
				}
			}

			// for each local repository: add to RepoManager
			for (ConfigurationEntry<?> cEntry : master.configurationEntries) {
				if (cEntry.getKey().equalsIgnoreCase(LOCAL_REPO_KEY)
						&& cEntry.getKey() instanceof String){
					// identify Folder, check and register
					try {
						String repoFolder = (String) cEntry.getValue();
						rm.addLocalZipPackageRepository(repoFolder);
						LOGGER.info("Added MovingCode Repository: " + LOCAL_REPO_KEY + " - "
								+ repoFolder);
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						LOGGER.error("Error loading repository: " + LOCAL_REPO_KEY + " - "
								+ cEntry.getValue().toString());
						e.printStackTrace();
					}
				}
			}


			LOGGER.info("The following repositories have been loaded:\n{}", Arrays.toString(rm.getRegisteredRepositories()));
		}
	}

	@Override
	public Collection<String> getAlgorithmNames() {
		return supportedFunctionIDs;
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		return new MCProcessDelegator(processID);
	}

	@Override
	public ProcessDescription getProcessDescription(String processID) {
		ProcessDescriptionType pdt =  filterProcessDescription(rm.getProcessDescription(processID));
		ProcessDescription pd = new ProcessDescription();
		pd.addProcessDescriptionForVersion(pdt, WPSConfig.VERSION_100);
		pd.addProcessDescriptionForVersion(ProcessDescription.createProcessDescriptionV200fromV100(pdt), WPSConfig.VERSION_200);
		return pd;
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		return rm.providesFunction(processID);
	}

	@Override
	public void shutdown() {
		// we probably do not need any logic here
	}
	
	static ProcessDescriptionType filterProcessDescription(ProcessDescriptionType description){
		description.setStatusSupported(true);
		description.setStoreSupported(true);
		return description;
	}
}
