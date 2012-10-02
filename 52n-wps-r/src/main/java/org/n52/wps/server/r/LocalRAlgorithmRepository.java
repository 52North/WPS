/***************************************************************
This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

Copyright (C) 2009 by con terra GmbH

Authors: 
	Bastian Schäffer, University of Muenster



Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
48155 Muenster, Germany, 52n@conterra.de

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program (see gnu-gpl v2.txt); if not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA or visit the web page of the Free
Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.r;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * A static repository to retrieve the available algorithms.
 * 
 * @author Matthias Hinz
 * 
 */
public class LocalRAlgorithmRepository implements ITransactionalAlgorithmRepository {

    private static Logger LOGGER = Logger.getLogger(LocalRAlgorithmRepository.class);

    // registered Processes
    private Map<String, String> algorithmMap;

    public LocalRAlgorithmRepository() {
        LOGGER.info("Instantiating new LocalRAlgorithmRepository");

        // check if the repository is active:
        String className = this.getClass().getCanonicalName();
        if ( !WPSConfig.getInstance().isRepositoryActive(className)) {
            LOGGER.debug("Local R Algorithm Repository is inactive.");
            return;
        }

        LOGGER.info("Initializing Local R Algorithm Repository");
        algorithmMap = new HashMap<String, String>();

        // add algorithms from config file to repository
        Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
        for (Property property : propertyArray) {
            if (property.getName().equalsIgnoreCase(RWPSConfigVariables.ALGORITHM.toString()) && property.getActive()) {
                addAlgorithm(property.getStringValue());
            }
        }

        RPropertyChangeManager changeManager = RPropertyChangeManager.getInstance();

        // Try to build up a connection to Rserve
        // If it is refused, a new instance of Rserve will be opened
        LOGGER.debug("[Rserve] Trying to connect to Rserve.");
        R_Config rConfig = R_Config.getInstance();
        try {
            RConnection testcon = rConfig.openRConnection();
            LOGGER.info("[Rserve] Connection to Rserve could be opened.");
            testcon.close();
        }
        catch (RserveException e) {
            // try to start Rserve via batchfile if enabled
            LOGGER.debug("[Rserve] Could not open connection to Rserve - trying to start Rserve with batch file.");
            rConfig.startRserve();

            try {
                RConnection testcon = rConfig.openRConnection();
                LOGGER.info("[Rserve] Connection to Rserve could be opened after batch file start.");
                // changeManager.addUnregisteredScripts() -- TODO maybe save the value of testcon.getVersion
                // or something similar to a property so that the connection to RServe can be check in admin
                // console.

                testcon.close();
            }
            catch (RserveException e2) {
                LOGGER.error("[Rserve] Could not start Rserve.");
            }
        }

        // unregistered scripts from repository folder will be added as Algorithm to WPSconfig
        changeManager.addUnregisteredScripts();
    }

    public boolean addAlgorithms(String[] algorithms) {
        for (String algorithmClassName : algorithms) {
            addAlgorithm(algorithmClassName);
        }
        LOGGER.info("Algorithms registered!");
        return true;

    }

    public IAlgorithm getAlgorithm(String className, ExecuteRequest request) {
        try {
            return loadAlgorithm(algorithmMap.get(className));
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public Collection<IAlgorithm> getAlgorithms() {
        Collection<IAlgorithm> resultList = new ArrayList<IAlgorithm>();
        try {
            for (String algorithmClasses : algorithmMap.values()) {
                resultList.add(loadAlgorithm(algorithmMap.get(algorithmClasses)));
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return resultList;
    }

    public Collection<String> getAlgorithmNames() {
        return new ArrayList<String>(algorithmMap.keySet());
    }

    public boolean containsAlgorithm(String className) {
        return algorithmMap.containsKey(className);
    }

    private IAlgorithm loadAlgorithm(String wellKnownName) throws Exception {
        IAlgorithm algorithm = (IAlgorithm) new GenericRProcess(wellKnownName);
        if ( !algorithm.processDescriptionIsValid()) {
            LOGGER.warn("Algorithm description is not valid: " + wellKnownName);
            throw new Exception("Could not load algorithm " + wellKnownName + ". ProcessDescription Not Valid.");
        }
        return algorithm;
    }

    public boolean addAlgorithm(Object processID) {
        if ( ! (processID instanceof String)) {
            return false;
        }
        String algorithmClassName = (String) processID;

        algorithmMap.put(algorithmClassName, algorithmClassName);
        LOGGER.info("Algorithm class registered: " + algorithmClassName);

        return true;
    }

    /**
     * Removes algorithm from AlgorithmMap
     */
    public boolean removeAlgorithm(Object processID) {
        if ( ! (processID instanceof String)) {
            return false;
        }
        String processName = (String) processID;
        if (algorithmMap.containsKey(processName)) {
            algorithmMap.remove(processName);
        }

        return true;
    }

    @Override
    public ProcessDescriptionType getProcessDescription(String processID) {
        return new GenericRProcess(processID).getDescription();
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

}
