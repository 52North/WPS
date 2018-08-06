/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.NotImplementedException;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Bastian Schaeffer, University of Muenster
 *
 */
public class RepositoryManager implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryManager.class);

    private ApplicationContext applicationContext;

    protected Map<String, IAlgorithmRepository> repositories = new HashMap<>();

    private final ProcessIDRegistry globalProcessIDs = ProcessIDRegistry.getInstance();

    private UpdateThread updateThread;

    public void init() {
        RepositoryManagerSingletonWrapper.init(this);

        globalProcessIDs.clearRegistry();
        loadAllRepositories();

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        WPSConfig.getInstance().addPropertyChangeListener(WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME,
                (final PropertyChangeEvent propertyChangeEvent) -> {
                    LOGGER.info("Received Property Change Event: {}",
                            propertyChangeEvent.getPropertyName());
                    loadAllRepositories();
                });

        Double updateHours = WPSConfig.getInstance().getWPSConfig().getServerConfigurationModule().getRepoReloadInterval();
        if (updateHours != 0) {
            LOGGER.info("Setting repository update period to {} hours.", updateHours);
            updateHours = updateHours * 3600 * 1000; // make milliseconds
            long updateInterval = updateHours.longValue();
            this.updateThread = new UpdateThread(this, updateInterval);
            updateThread.start();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private List<String> getRepositoryNames() {

        List<String> repositoryNames = new ArrayList<>();

        Map<String, ConfigurationModule> repositoryMap = WPSConfig.getInstance()
                .getRegisteredAlgorithmRepositoryConfigModules();

        for (ConfigurationModule configModule : repositoryMap.values()) {
            if (configModule.isActive() && configModule instanceof ClassKnowingModule) {
                String repositoryClassName = ((ClassKnowingModule) configModule).getClassName();
                repositoryNames.add(repositoryClassName);
                if (!repositories.containsKey(repositoryClassName)) {
                    loadRepository(configModule.getClass().getCanonicalName(), (ClassKnowingModule) configModule);
                }
            }

        }

        return repositoryNames;
    }

    protected void loadAllRepositories() {
        LOGGER.debug("Loading all repositories: {} (doing a gc beforehand...)", repositories);//FIXME not sure log statement makes a lot of sense

        System.gc();

        Map<String, ConfigurationModule> repositoryConfigModules = WPSConfig.getInstance()
                .getRegisteredAlgorithmRepositoryConfigModules();

        for (String configModuleName : repositoryConfigModules.keySet()) {

            ConfigurationModule configModule = repositoryConfigModules.get(configModuleName);

            if (configModule instanceof ClassKnowingModule) {
                loadRepository(configModuleName, (ClassKnowingModule) configModule);
            } else {
                LOGGER.warn("ConfigModule {} not instanceof ClassKnowingModule. Will not load it.", configModuleName);
            }
        }
    }

    private void loadRepository(String configModuleName, ClassKnowingModule configModule) {
        if (configModule.isActive()) {
            LOGGER.debug("Loading module '{}'", configModuleName);
            registerRepository(configModuleName, configModule);
        } else {
            LOGGER.warn("Won't load inactive module '{}'", configModuleName);
        }
    }

    private void registerRepository(String configModuleName, ClassKnowingModule configModule) {
        String repositoryClassName = configModule.getClassName();
        try {
            // XXX configModuleName != COMPONENT_NAME of LocalRAlgorithmRepository
            addRepository(repositoryClassName, applicationContext.getBean(configModuleName, IAlgorithmRepository.class));
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("Hard wiring '{}' for module '{}'.", repositoryClassName, configModuleName);
            registerNewRepository(repositoryClassName);
        } catch (BeansException e) {
            LOGGER.warn("Could not create '{}' for module '{}'", repositoryClassName, configModuleName, e);
        }
    }

    private void registerNewRepository(String repositoryClassName) {
        try {
            Class<?> repositoryClass = RepositoryManager.class
                    .getClassLoader()
                    .loadClass(repositoryClassName);
            IAlgorithmRepository algorithmRepository = (IAlgorithmRepository) repositoryClass
                    .newInstance();

            addRepository(repositoryClassName, algorithmRepository);
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.warn(
                    "An error occured while registering AlgorithmRepository: {}",
                    repositoryClassName, e);
        } catch (ClassNotFoundException | IllegalArgumentException | SecurityException e) {
            LOGGER.warn(
                    "An error occured while registering AlgorithmRepository: {}",
                    repositoryClassName, e);
        }
    }

    protected void addRepository(String name, IAlgorithmRepository repository) {
        repositories.put(name, repository);
        LOGGER.info("Algorithm Repository {} registered", name);
    }

    /**
     * Allows to reInitialize the Repositories
     *
     */
    protected void reloadRepositories() {
        loadAllRepositories();
    }

    /**
     * Methods looks for Algorithm in all Repositories.
     * The first match is returned.
     * If no match could be found, null is returned
     *
     * @param className the class name of the algorithm
     * @return IAlgorithm or null
     */
    public IAlgorithm getAlgorithm(String className) {

        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            if (repository.containsAlgorithm(className)) {
                return repository.getAlgorithm(className);
            }
        }
        return null;
    }

    /**
     *
     * @return all algorithms
     */
    public List<String> getAlgorithms() {
        List<String> allAlgorithmNamesCollection = new ArrayList<>();
        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            allAlgorithmNamesCollection.addAll(repository.getAlgorithmNames());
        }
        return allAlgorithmNamesCollection;

    }

    public boolean containsAlgorithm(String algorithmName) {
        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            if (repository.containsAlgorithm(algorithmName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds given algorithm item to the first (transactional) repository which
     * {@link ITransactionalAlgorithmRepository#addAlgorithm(java.lang.Object)}
     * returns <code>true</code>, whereas the implementation has to take care if
     * it is capabable to handle giving item correctly.
     *
     * // XXX repo-implementations expect mostly a simple string which is certainly
     *    not enough info to determine if repo is capable for the given item
     * // TODO item should contain more infos for repositories to decide
     *
     * @param item the algorithm item to add.
     * @return <code>true</code> if item could be added, <code>false</code> otherwise.
     */
    public boolean addAlgorithm(Object item) {
        LOGGER.debug("Adding algorithm based on {} ...", item);
        for (IAlgorithmRepository repository : repositories.values()) {
            if (ITransactionalAlgorithmRepository.class.isAssignableFrom(repository.getClass())) {
                ITransactionalAlgorithmRepository transactionalRepository = ITransactionalAlgorithmRepository.class.cast(repository);
                LOGGER.trace("Found compatible algorithms repository {} for {}", transactionalRepository, item);
                if (transactionalRepository.addAlgorithm(item)) {
                    LOGGER.debug("Added algorithm {} to {}", item, transactionalRepository);
                    return true;
                }
            }
        }

        LOGGER.debug("Could NOT add algorithm based on {}", item);
        return false;
    }

    /**
     * Removes given algorithm item from the first (transactional) repository which
     * {@link ITransactionalAlgorithmRepository#addAlgorithm(java.lang.Object)}
     * returns <code>true</code>, whereas the implementation has to take care if
     * it is capabable to handle giving item correctly.
     *
     * // XXX repo-implementations expect mostly a simple string which is certainly
     *    not enough info to determine if repo is capable for the given item
     * // TODO item should contain more infos for repositories to decide
     *
     * @param item the algorithm item to remove.
     * @return <code>true</code> if item could be removed, <code>false</code> otherwise.
     */
    public boolean removeAlgorithm(Object item) {
        LOGGER.debug("Removing algorithm based on {} ...", item);
        for (IAlgorithmRepository repository : repositories.values()) {
            if (ITransactionalAlgorithmRepository.class.isAssignableFrom(repository.getClass())) {
                ITransactionalAlgorithmRepository transactionalRepository = ITransactionalAlgorithmRepository.class.cast(repository);
                try {
                    if (transactionalRepository.removeAlgorithm(item)) {
                        LOGGER.debug("Removed algorithm {} from {}", item, transactionalRepository);
                        return true;
                    }
                } catch (NotImplementedException e) {
                    LOGGER.warn("Problem removing algorihtm {} from {}", item, transactionalRepository, e);
                }
            }
        }

        LOGGER.debug("Could not remove algorithm based {}", item);
        return false;
    }

    public IAlgorithmRepository getRepositoryForAlgorithm(String algorithmName) {
        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            if (repository.containsAlgorithm(algorithmName)) {
                return repository;
            }
        }
        return null;
    }

    public Class<?> getInputDataTypeForAlgorithm(String algorithmIdentifier, String inputIdentifier) {
        IAlgorithm algorithm = getAlgorithm(algorithmIdentifier);
        return algorithm.getInputDataType(inputIdentifier);
    }

    public Class<?> getOutputDataTypeForAlgorithm(String algorithmIdentifier, String inputIdentifier) {
        IAlgorithm algorithm = getAlgorithm(algorithmIdentifier);
        return algorithm.getOutputDataType(inputIdentifier);
    }

    public boolean registerAlgorithm(String id, IAlgorithmRepository repository) {
        if (globalProcessIDs.addID(id)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean unregisterAlgorithm(String id) {
        if (globalProcessIDs.removeID(id)) {
            return true;
        } else {
            return false;
        }
    }

    public IAlgorithmRepository getAlgorithmRepository(String name) {
        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            if (repository.getClass().getName().equals(name)) {
                return repository;
            }
        }
        return null;
    }

    public IAlgorithmRepository getRepositoryForClassName(
            String className) {
        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            if (repository.getClass().getName().equals(className)) {
                return repository;
            }
        }
        return null;
    }

    public ProcessDescription getProcessDescription(String processClassName) {
        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            if (repository.containsAlgorithm(processClassName)) {
                return repository.getProcessDescription(processClassName);
            }
        }
        return new ProcessDescription();
    }

    static class UpdateThread extends Thread {

        private final long interval;

        private boolean firstrun = true;

        private final RepositoryManager repositoryManager;

        public UpdateThread(RepositoryManager repositoryManager, long interval) {
            this.repositoryManager = repositoryManager;
            this.interval = interval;
        }

        @Override
        public void run() {
            LOGGER.debug("UpdateThread started");

            try {
                // never terminate the run method
                while (true) {
                    // do not update on first run!
                    if (!firstrun) {
                        LOGGER.info("Reloading repositories - this might take a while ...");
                        long timestamp = System.currentTimeMillis();
                        repositoryManager.reloadRepositories();
                        LOGGER.info("Repositories reloaded - going to sleep. Took {} seconds.",
                                (System.currentTimeMillis() - timestamp) / 1000);
                    } else {
                        firstrun = false;
                    }

                    // sleep for a given INTERVAL
                    Thread.sleep(interval);
                }
            } catch (InterruptedException e) {
                LOGGER.debug("Interrupt received - Terminating the UpdateThread.");
            }
        }

    }

    // shut down the update thread
    public void finalize() throws Throwable {
        super.finalize();
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    public void shutdown() {
        LOGGER.debug("Shutting down all repositories..");
        for (String repositoryClassName : getRepositoryNames()) {
            IAlgorithmRepository repository = repositories.get(repositoryClassName);
            repository.shutdown();
        }
    }

}
