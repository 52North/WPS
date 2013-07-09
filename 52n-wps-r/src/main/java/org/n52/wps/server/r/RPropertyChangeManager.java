/**
 * ﻿Copyright (C) 2010
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

package org.n52.wps.server.r;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.WPSConfigurationDocument.WPSConfiguration;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.r.data.CustomDataTypeManager;

public class RPropertyChangeManager implements PropertyChangeListener {

    private static Logger LOGGER = LoggerFactory.getLogger(RPropertyChangeManager.class);

    private static RPropertyChangeManager instance;

    private RPropertyChangeManager() {
    }

    public static RPropertyChangeManager getInstance() {
        if (instance == null) {
            instance = new RPropertyChangeManager();
            WPSConfig.getInstance().addPropertyChangeListener(WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, instance);
        }
        return instance;
    }

    public static RPropertyChangeManager reInitialize() {
        WPSConfig.getInstance().removePropertyChangeListener(WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, instance);
        instance = new RPropertyChangeManager();
        return instance;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // String repName = LocalRAlgorithmRepository.class.getCanonicalName();
        // RepositoryManager manager = RepositoryManager.getInstance();
        // LocalRAlgorithmRepository repository = (LocalRAlgorithmRepository)
        // manager.getRepositoryForClassName(repName);

        LOGGER.info("received PropertyChangeEvent: " + evt.getPropertyName());
        updateRepositoryConfiguration();
        CustomDataTypeManager.getInstance().update();
        // deleteUnregisteredScripts();
        // TODO: How might processes be renamed?

    }

    private class PropertyComparator implements Comparator<Property> {
        @Override
        public int compare(Property o1, Property o2) {
            int com1 = o1.getName().compareToIgnoreCase(o2.getName());
            if (com1 != 0)
                return com1;
            return (o1.getStringValue().compareToIgnoreCase(o2.getStringValue()));
        }
    }

    /**
     * 
     */
    public void updateRepositoryConfiguration() {
        // Retrieve repository document and properties:
        String localRAlgorithmRepository_className = LocalRAlgorithmRepository.class.getCanonicalName();
        Repository[] repositoryDocuments = WPSConfig.getInstance().getRegisterdAlgorithmRepositories();
        Repository repositoryDocument = null;

        for (Repository doc : repositoryDocuments) {
            if (doc.getClassName().equals(localRAlgorithmRepository_className)) {
                repositoryDocument = doc;
            }
        }

        if (repositoryDocument == null) {
            LOGGER.error("Local R Algorithm Repository is not registered");
            // return;
        }

        Property[] oldPropertyArray = repositoryDocument.getPropertyArray();
        HashMap<String, Property> algorithmPropertyHash = new HashMap<String, Property>();

        boolean propertyChanged = false;
        ArrayList<Property> newPropertyList = new ArrayList<Property>();

        // test if host and port of rserve are available in config properties, if not, values from R_Config
        // will be used

        // retrieve set of string representations for all config variables:
        HashSet<String> configVariableNames = new HashSet<String>();

        for (RWPSConfigVariables var : RWPSConfigVariables.values()) {
            configVariableNames.add(var.toString().toLowerCase());
        }

        for (Property property : oldPropertyArray) {
            String pname = property.getName().toLowerCase();

            // check the name and active state
            if (pname.equalsIgnoreCase(RWPSConfigVariables.ALGORITHM.toString())) {
                LOGGER.debug("Algorithm property: " + property);

                // put id into a dictionary to check and add later:
                algorithmPropertyHash.put(property.getStringValue(), property);
            }
            else {
                LOGGER.debug("NOT-algorithm property: " + property);

                if (configVariableNames.contains(pname)) {
                    boolean success = handleConfigVariable(property);
                    if ( !success)
                        LOGGER.warn("Invalid config variable was omitted and deleted: " + property);

                    // config variable should occur only once, doubling will be omitted:
                    configVariableNames.remove(pname);
                }
                else {
                    // valid properties which are not algorithms will be just passed to the new list
                    LOGGER.debug("Unprocessed property: " + property);
                }

                newPropertyList.add(property);
            }
        }

        propertyChanged = checkMandatoryParameters(repositoryDocument,
                                                   propertyChanged,
                                                   newPropertyList,
                                                   configVariableNames);

        propertyChanged = addMissingAlgorithms(repositoryDocument,
                                               algorithmPropertyHash,
                                               propertyChanged,
                                               newPropertyList);

        // there might be registered algorithms, which don't got a script file any more,
        // those will be deleted here:
        if ( !algorithmPropertyHash.isEmpty())
            propertyChanged = true;

        propertyChanged = checkPropertyOrder(oldPropertyArray, propertyChanged);

        if (propertyChanged) {
            Property[] newPropertyArray = newPropertyList.toArray(new Property[0]);

            // sort list of properties lexicographically:

            Arrays.sort(newPropertyArray, new PropertyComparator());
            repositoryDocument.setPropertyArray(newPropertyArray);
            propertyChanged = true;

            // write new WPSConfig if property had to be changed
            WPSConfigurationDocument wpsConfigurationDocument = WPSConfigurationDocument.Factory.newInstance();
            WPSConfiguration wpsConfig = WPSConfig.getInstance().getWPSConfig();
            wpsConfigurationDocument.setWPSConfiguration(wpsConfig);

            // writes the new WPSConfig to a file
            try {
                String configurationPath = WPSConfig.getConfigPath();
                File XMLFile = new File(configurationPath);
                wpsConfigurationDocument.save(XMLFile,
                                              new org.apache.xmlbeans.XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
                WPSConfig.forceInitialization(configurationPath);
                LOGGER.info("WPS Config was changed.");
            }
            catch (IOException e) {
                LOGGER.error("Could not write configuration to file: " + e.getMessage());
            }
            catch (org.apache.xmlbeans.XmlException e) {
                LOGGER.error("Could not generate XML File from Data: " + e.getMessage());
            }
        }

    }

    private boolean checkPropertyOrder(Property[] oldPropertyArray, boolean propertyChanged) {
        // check if properties need to be re-ordered:
        if ( !propertyChanged) {
            PropertyComparator comp = new PropertyComparator();
            for (int i = 0; i < oldPropertyArray.length - 1; i++) {
                int order = comp.compare(oldPropertyArray[i], oldPropertyArray[i + 1]);
                if (order > 0) {
                    propertyChanged = true;
                    break;
                }
            }
        }
        return propertyChanged;
    }

    private boolean addMissingAlgorithms(Repository repositoryDocument,
                                         HashMap<String, Property> algorithmPropertyHash,
                                         boolean propertyChanged,
                                         ArrayList<Property> newPropertyList) {
        // check script dir for R process files
        // adjusts WPS config
        String scriptDir = R_Config.getInstance().getScriptDirFullPath();
        File algorithmDir = new File(scriptDir);
        if (algorithmDir.isDirectory()) {
            File[] scripts = algorithmDir.listFiles(new R_Config.RFileExtensionFilter());
            LOGGER.debug("Loading script files from " + algorithmDir + ": " + Arrays.toString(scripts));
            for (File scriptf : scripts) {
                String wkn = R_Config.getInstance().FileToWkn(scriptf);
                Property prop = algorithmPropertyHash.get(wkn);

                // case: property is missing in wps config
                if (prop == null) {
                    // Change Property if Algorithm is not inside process description:
                    prop = repositoryDocument.addNewProperty();
                    prop.setActive(true);
                    prop.setName(RWPSConfigVariables.ALGORITHM.toString());
                    prop.setStringValue(wkn);
                    newPropertyList.add(prop);
                    LOGGER.debug("Added new algorithm property to repo document: " + prop);

                    propertyChanged = true;
                }
                else {
                    LOGGER.debug("Algorithm property already repo document: " + prop);
                    newPropertyList.add(algorithmPropertyHash.remove(wkn));
                }

                /*
                 * if(prop.getActive() && addAlgorithm){ repository.addAlgorithm(wkn); }
                 */
            }
        }
        return propertyChanged;
    }

    private boolean checkMandatoryParameters(Repository repositoryDocument,
                                             boolean propertyChanged,
                                             ArrayList<Property> newPropertyList,
                                             HashSet<String> configVariableNames) {
        /*
         * mandatory paramters, the ones from param that have not been covered yet.
         */
        // If there was no required parameters given by WPSconfig, host and port defaults will be added
        // (RServe_User and RServe_port won't be added)
        if (configVariableNames.contains(RWPSConfigVariables.RSERVE_HOST.toString().toLowerCase())) {
            Property host = repositoryDocument.addNewProperty();
            host.setActive(true);
            host.setName(RWPSConfigVariables.RSERVE_HOST.toString());
            host.setStringValue(R_Config.getInstance().rServeHost);
            newPropertyList.add(host);
            propertyChanged = true;
        }

        if (configVariableNames.contains(RWPSConfigVariables.RSERVE_PORT.toString().toLowerCase())) {
            Property port = repositoryDocument.addNewProperty();
            port.setActive(true);
            port.setName(RWPSConfigVariables.RSERVE_PORT.toString());
            port.setStringValue(Integer.toString(R_Config.getInstance().rServePort));
            newPropertyList.add(port);
            propertyChanged = true;
        }
        return propertyChanged;
    }

    /**
     * Retrieves configuration parameter from WPS config
     * 
     * @param property
     * @return true
     */
    private boolean handleConfigVariable(Property property) {
        String pname = property.getName();
        // RWPSConfigVariables.v
        boolean success = false;
        for (RWPSConfigVariables configvariable : RWPSConfigVariables.values()) {
            if (pname.equalsIgnoreCase(configvariable.toString())) {
                R_Config.getInstance().setConfigVariable(configvariable, property.getStringValue());
                success = true;
                break;
            }
        }

        // TODO: Replace outdated variable assignments:

        if (pname.equalsIgnoreCase(RWPSConfigVariables.RSERVE_HOST.toString())) {
            R_Config.getInstance().rServeHost = property.getStringValue();
        }
        else if (pname.equalsIgnoreCase(RWPSConfigVariables.RSERVE_PORT.toString())) {
            try {
                R_Config.getInstance().rServePort = Integer.parseInt(property.getStringValue());
            }
            catch (NumberFormatException e) {
                LOGGER.error("Non numeric RServe_Port property found - it will be ignored and deleted");
                return false;
            }
        }
        else if (pname.equalsIgnoreCase(RWPSConfigVariables.RSERVE_USER.toString())) {
            R_Config.getInstance().rServeUser = property.getStringValue();
        }
        else if (pname.equalsIgnoreCase(RWPSConfigVariables.RSERVE_PASSWORD.toString())) {
            R_Config.getInstance().rServePassword = property.getStringValue();
        }
        else if (pname.equalsIgnoreCase(RWPSConfigVariables.SCRIPT_DIR.toString()) && property.getActive()) {
            R_Config.getInstance().SCRIPT_DIR = property.getStringValue();
            LOGGER.info("Using script dir " + R_Config.getInstance().SCRIPT_DIR);
        }
        else if (pname.equalsIgnoreCase(RWPSConfigVariables.RESOURCE_DIR.toString()) && property.getActive()) {
            R_Config.getInstance().resourceDirectory = property.getStringValue();
        }
        else if (pname.equalsIgnoreCase(RWPSConfigVariables.ENABLE_BATCH_START.toString()) && property.getActive()) {
            R_Config.getInstance().enableBatchStart = Boolean.parseBoolean(property.getStringValue());
            LOGGER.info("Trying batch start if R is not running: " + R_Config.getInstance().enableBatchStart);
        }// else
         // return false;

        return success;

    }

    /**
     * Deletes *.R file from repository TODO give this method a purpose
     */
    private boolean deleteScript(String processName) {
        boolean deleted = false;
        try {
            File processFile = R_Config.getInstance().wknToFile(processName);
            deleted = processFile.delete();
            if ( !deleted) {
                LOGGER.error("Process file " + processFile.getName() + " could not be deleted, "
                        + "Process just removed temporarly");
            }
            else
                LOGGER.info("Process " + processName + " and process file " + processFile.getName()
                        + " successfully deleted!");
        }
        catch (Exception e) {
            LOGGER.error("Process file refering to " + processName + "could not be deleted, this"
                    + "error was not expected:\n" + e.getLocalizedMessage());
        }
        return deleted;

    }

    public void registerScript(File destFile) {

    }

}
