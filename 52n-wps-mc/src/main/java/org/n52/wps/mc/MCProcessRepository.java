/**
 * ﻿Copyright (C) 2012
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

package org.n52.wps.mc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.movingcode.runtime.MovingCodeRepositoryManager;
import org.n52.movingcode.runtime.ProcessorConfig;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.WebProcessingService;

/**
 * 
 * @author Matthias Mueller
 * 
 *         TODO: lazy initialization
 * 
 */
public class MCProcessRepository implements IAlgorithmRepository {

    private static final String CONFIG_FILE_NAME = "processors.xml";

    private static final String REPO_FEED_URL_PARAM = "REPOSITORY_FEED_URL";

	private static final String DROP_IN_FOLDER_KEY = "DropInFolder";

	private static final String DROP_IN_CHECK_INTERVAL_SECS_KEY = "DropInCheckIntervalSecs";

    private MovingCodeRepositoryManager rm = MovingCodeRepositoryManager.getInstance();

	private DropInFolderWatchdog dropInWatchdog;



    private static Logger logger = Logger.getLogger(MCProcessRepository.class);

    public MCProcessRepository() {
        super();
        configureMCRuntime();

        // check if the repository is active
        if (WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())) {

            // get properties to find out which remote repositories we shall invoke
            Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());

            // for each remote repository: add to RepoManager
            for (Property property : propertyArray) {
                if (property.getName().equalsIgnoreCase(REPO_FEED_URL_PARAM) && property.getActive()) {
                    // convert to URL, check and register
                    try {
                        URL repoURL = new URL(property.getStringValue());
                        rm.addRepository(repoURL);
                        logger.info("Added MovingCode Repository: " + property.getName() + " - "
                                + property.getStringValue());
                    }
                    catch (MalformedURLException e) {
                        logger.warn("MovingCode Repository is not a valid URL: " + property.getName() + " - "
                                + property.getStringValue());
                    }
                    catch (Exception e) {
                        // catch any unexpected error; if we get here this is probably an indication for a
                        // bug/flaw in mc-runtime ...
                        logger.error("Error invoking MovingCode Runtime for feed URL : " + property.getName() + " - "
                                + property.getStringValue());
                    }

                }
            }

            // start dropin watchdog
            initDropInFolder();

        }
        else {
            logger.debug("MCProcessRepository does not contain any processes.");
        }
    }

	private void initDropInFolder() {
		String folderDir = null;
		int checkInterval = 0;
		for (Property property : WPSConfig.getInstance().getPropertiesForRepositoryClass(getClass().getName())) {
			if (property.getName().equals(DROP_IN_FOLDER_KEY) && property.getActive()) {
				folderDir = property.getStringValue();
			}
			else if (property.getName().equals(DROP_IN_CHECK_INTERVAL_SECS_KEY) && property.getActive()) {
				checkInterval = Integer.parseInt(property.getStringValue());
			}
		}
		
		if (folderDir != null) {
			this.dropInWatchdog = new DropInFolderWatchdog(WebProcessingService.BASE_DIR,
					folderDir, checkInterval);
		}
	}

    @Override
    public Collection<String> getAlgorithmNames() {
        return Arrays.asList(rm.getProcessIDs());
    }

    @Override
    public IAlgorithm getAlgorithm(String processID) {
        return new MCProcessDelegator(processID);
    }

    @Override
    public ProcessDescriptionType getProcessDescription(String processID) {
        return rm.getFunction(processID).getDescription().getPackageDescription().getContractedFunctionality().getWpsProcessDescription();
    }

    @Override
    public boolean containsAlgorithm(String processID) {
        return rm.providesFunction(processID);
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        // we probably do not need any logic here
    }

    // ----------------------------------------------------------------
    // methods and logic for processor configuration
    private static void configureMCRuntime() {
        String configFilePath = WPSConfig.getConfigDir() + CONFIG_FILE_NAME;
        File configFile = new File(configFilePath);
        boolean loaded = ProcessorConfig.getInstance().setConfig(configFile);
        if ( !loaded) {
            logger.error("Could not load processor configuration from " + configFilePath);
        }
    }

}