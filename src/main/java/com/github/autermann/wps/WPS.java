package com.github.autermann.wps;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import net.opengis.ows.x11.HTTPDocument.HTTP;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.LanguagesDocument.Languages;
import net.opengis.wps.x100.WPSCapabilitiesType;

import org.apache.xmlbeans.XmlException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.n52.wps.DatahandlersDocument.Datahandlers;
import org.n52.wps.FormatDocument;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.ServerDocument;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.WPSConfigurationDocument.WPSConfiguration;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.RetrieveResultServlet;
import org.n52.wps.server.WebProcessingService;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

public class WPS {
    private static final String UPDATE_SEQUENCE = "1";
    private static final String SERVICE_VERSION = "1.0.0";
    private static final String EXECUTE = "Execute";
    private static final String DESCRIBE_PROCESS = "DescribeProcess";
    private static final String GET_CAPABILITIES = "GetCapabilities";
    private static final String SERVICE = "WPS";
    private static final String LANGUAGE = "en-US";
    private static final String EMPTY = "";
    private static final String WEB_PROCESSING_SERVICE_PATH = "/WebProcessingService";
    private static final String RETRIEVE_RESULT_SERVLET_PATH = "/RetrieveResultServlet";
    private static final String ROOT_CONTEXT = "/";
    
    private final AtomicInteger parserCount = new AtomicInteger(0);
    private final AtomicInteger generatorCount = new AtomicInteger(0);
    private final Server server;
    private final ReentrantLock lock = new ReentrantLock();
    private final WPSConfigurationDocument config;

    public WPS(String host, int port) throws XmlException, IOException,
                                             URISyntaxException {
        checkArgument(port > 0 && host != null && !host.isEmpty());
        this.config = createEmptyConfig(host, port);
        this.server = new Server(port);
        ServletContextHandler sch = new ServletContextHandler(server, ROOT_CONTEXT);
        sch.addServlet(WebProcessingService.class, WEB_PROCESSING_SERVICE_PATH);
        sch.addServlet(RetrieveResultServlet.class, RETRIEVE_RESULT_SERVLET_PATH);
    }


    private WPSConfigurationDocument createEmptyConfig(String host, int port) {
        WPSConfigurationDocument document = WPSConfigurationDocument.Factory
                .newInstance();
        WPSConfiguration wpsConfig = document.addNewWPSConfiguration();
        wpsConfig.addNewAlgorithmRepositoryList();
        wpsConfig.addNewRemoteRepositoryList();
        Datahandlers datahandlers = wpsConfig.addNewDatahandlers();
        datahandlers.addNewGeneratorList();
        datahandlers.addNewParserList();
        ServerDocument.Server serverConfig = wpsConfig.addNewServer();
        serverConfig.setHostport(String.valueOf(port));
        serverConfig.setHostname(host);
        serverConfig.setWebappPath(EMPTY);
        serverConfig.setCacheCapabilites(true);
        return document;
    }

    private CapabilitiesDocument createCapabilitiesSkeleton() {
        CapabilitiesDocument doc = CapabilitiesDocument.Factory.newInstance();
        WPSCapabilitiesType caps = doc.addNewCapabilities();
        caps.setLang(LANGUAGE);
        Languages languages = caps.addNewLanguages();
        languages.addNewDefault().setLanguage(LANGUAGE);
        languages.addNewSupported().addLanguage(LANGUAGE);
        caps.addNewService().setStringValue(SERVICE);
        caps.setVersion(SERVICE_VERSION);
        caps.setUpdateSequence(UPDATE_SEQUENCE);
        OperationsMetadata operationsMetadata = caps.addNewOperationsMetadata();
        Operation getCapabilities = operationsMetadata.addNewOperation();
        getCapabilities.setName(GET_CAPABILITIES);
        getCapabilities.addNewDCP().addNewHTTP().addNewGet().setHref(EMPTY);
        Operation describeProcess = operationsMetadata.addNewOperation();
        describeProcess.setName(DESCRIBE_PROCESS);
        describeProcess.addNewDCP().addNewHTTP().addNewGet().setHref(EMPTY);
        Operation execute = operationsMetadata.addNewOperation();
        execute.setName(EXECUTE);
        HTTP executeHttp = execute.addNewDCP().addNewHTTP();
        executeHttp.addNewGet().setHref(EMPTY);
        executeHttp.addNewPost().setHref(EMPTY);
        return doc;
    }

    private void configure() throws XmlException, IOException {
        WPSConfig.forceInitialization(getConfigDocument().newInputStream());
        CapabilitiesConfiguration.getInstance(createCapabilitiesSkeleton());
    }

    private WPSConfiguration getConfig() {
        return getConfigDocument().getWPSConfiguration();
    }

    private WPSConfigurationDocument getConfigDocument() {
        return config;
    }

    public WPS addAlgorithmRepository(
            Class<? extends IAlgorithmRepository> repoClass) {
        return addAlgorithmRepository(repoClass, null);
    }

    public WPS addAlgorithmRepository(
            Class<? extends IAlgorithmRepository> repoClass,
            Multimap<String, String> properties) {
        lock.lock();
        try {
            checkState(!isRunning());
            _addAlgorithmRepository(repoClass, properties);
            return this;
        } finally {
            lock.unlock();
        }
    }

    private Repository _addAlgorithmRepository(
            Class<? extends IAlgorithmRepository> repoClass,
            Multimap<String, String> properties) {
        Repository repo = getRepository(repoClass).or(getConfig()
                .getAlgorithmRepositoryList().addNewRepository());
        repo.setActive(true);
        repo.setClassName(repoClass.getName());
        repo.setName(repoClass.getName());
        if (properties != null) {
            for (String property : properties.keys()) {
                for (String value : properties.get(property)) {
                    Property p = repo.addNewProperty();
                    p.setActive(true);
                    p.setName(property);
                    p.setStringValue(value);
                }
            }
        }
        return repo;
    }

    private Optional<Repository> getRepository(
            Class<? extends IAlgorithmRepository> c) {
        for (Repository r : getConfig().getAlgorithmRepositoryList()
                .getRepositoryArray()) {
            if (r.getClassName().equals(c.getName())) {
                return Optional.of(r);
            }
        }
        return Optional.absent();
    }

    public WPS addAlgorithm(Class<? extends IAlgorithm> algoClass) {
        lock.lock();
        try {
            checkState(!isRunning());
            Repository repo
                    = _addAlgorithmRepository(LocalAlgorithmRepository.class, null);
            Property p = repo.addNewProperty();
            p.setName("Algorithm");
            p.setActive(true);
            p.setStringValue(algoClass.getName());
            return this;
        } finally {
            lock.unlock();
        }
    }

    public WPS addParser(Class<? extends IParser> clazz) {
        return addParser(clazz, null, null);

    }

    public WPS addParser(Class<? extends IParser> clazz,
                         Iterable<Format> formats) {
        return addParser(clazz, formats, null);

    }

    public WPS addParser(Class<? extends IParser> clazz,
                         Iterable<Format> formats,
                         Multimap<String, String> properties) {
        lock.lock();
        try {
            checkState(!isRunning());
            Parser parser = getConfig().getDatahandlers()
                    .getParserList().addNewParser();
            parser.setActive(true);
            parser.setClassName(clazz.getName());
            parser.setName("parser" + parserCount.getAndIncrement());
            if (formats != null) {
                for (Format f : formats) {
                    FormatDocument.Format format = parser.addNewFormat();
                    format.setEncoding(f.getEncoding());
                    format.setMimetype(f.getMimeType());
                    format.setSchema(f.getSchema());
                }
            }
            if (properties != null) {
                for (String property : properties.keys()) {
                    for (String value : properties.get(property)) {
                        Property p = parser.addNewProperty();
                        p.setActive(true);
                        p.setName(property);
                        p.setStringValue(value);
                    }
                }
            }
            return this;
        } finally {
            lock.unlock();
        }
    }

    public WPS addGenerator(Class<? extends IGenerator> clazz) {
        return addGenerator(clazz, null, null);
    }

    public WPS addGenerator(Class<? extends IGenerator> clazz,
                            Iterable<Format> formats) {
        return addGenerator(clazz, formats, null);
    }

    public WPS addGenerator(Class<? extends IGenerator> clazz,
                            Iterable<Format> formats,
                            Multimap<String, String> properties) {
        lock.lock();
        try {
            checkState(!isRunning());
            Generator generator = getConfig().getDatahandlers()
                    .getGeneratorList().addNewGenerator();
            generator.setActive(true);
            generator.setClassName(clazz.getName());
            generator.setName("generator" + generatorCount.getAndIncrement());
            if (formats != null) {
                for (Format f : formats) {
                    FormatDocument.Format format = generator.addNewFormat();
                    format.setEncoding(f.getEncoding());
                    format.setMimetype(f.getMimeType());
                    format.setSchema(f.getSchema());
                }
            }
            if (properties != null) {
                for (String property : properties.keys()) {
                    for (String value : properties.get(property)) {
                        Property p = generator.addNewProperty();
                        p.setActive(true);
                        p.setName(property);
                        p.setStringValue(value);
                    }
                }
            }
            return this;
        } finally {
            lock.unlock();
        }
    }

    private Server getServer() {
        return server.getServer();
    }

    public void start() throws Exception {
        lock.lock();
        try {
            checkState(!isRunning());
            configure();
            getServer().start();
        } finally {
            lock.unlock();
        }

    }

    public void stop() throws Exception {
        lock.lock();
        try {
            checkState(isRunning());
            getServer().stop();
        } finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        lock.lock();
        try {
            return getServer().isRunning();
        } finally {
            lock.unlock();
        }
    }

    public boolean isStarted() {
        lock.lock();
        try {
            return getServer().isStarted();
        } finally {
            lock.unlock();
        }
    }

    public boolean isStarting() {
        lock.lock();
        try {
            return getServer().isStarting();
        } finally {
            lock.unlock();
        }
    }

    public boolean isStopping() {
        lock.lock();
        try {
            return getServer().isStopping();
        } finally {
            lock.unlock();
        }
    }

    public boolean isStopped() {
        lock.lock();
        try {
            return getServer().isStopped();
        } finally {
            lock.unlock();
        }
    }

    public boolean isFailed() {
        lock.lock();
        try {
            return server.isFailed();
        } finally {
            lock.unlock();
        }
    }

    public static class Format {
        private final String mimeType;
        private final String encoding;
        private final String schema;

        public Format(String mimeType, String schema, String encoding) {
            this.mimeType = mimeType;
            this.encoding = encoding;
            this.schema = schema;
        }

        public Format(String mimeType, String schema) {
            this(mimeType, schema, null);
        }

        public Format(String mimeType) {
            this(mimeType, null, null);
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getEncoding() {
            return encoding;
        }

        public String getSchema() {
            return schema;
        }
    }
}
