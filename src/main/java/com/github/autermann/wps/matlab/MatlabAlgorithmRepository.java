package com.github.autermann.wps.matlab;

import com.github.autermann.wps.matlab.util.FileExtensionPredicate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.matlab.description.MatlabProcessDescription;
import com.github.autermann.yaml.Yaml;
import com.github.autermann.yaml.YamlNode;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabAlgorithmRepository implements IAlgorithmRepository {
    private static final Logger LOG = LoggerFactory
            .getLogger(MatlabAlgorithmRepository.class);

    public static final String CONFIG_PROPERTY = "config";

    private final Map<String, MatlabProcessDescription> descriptions = Maps.newHashMap();

    public MatlabAlgorithmRepository() {
        this(getProperties());
    }

    public MatlabAlgorithmRepository(ListMultimap<String, String> properties) {
        for (YamlNode config : getConfigPaths(properties.get(CONFIG_PROPERTY))) {
            MatlabProcessDescription description
                    = MatlabProcessDescription.load(config);
            descriptions.put(description.getId(), description);
        }
    }

    private FluentIterable<YamlNode> getConfigPaths(List<String> properties) {
        Predicate<File> p = FileExtensionPredicate.of("yaml", "yml");
        List<File> paths = Lists.newLinkedList();
        for (String configPath : properties) {
            File file = new File(configPath);
            if (!file.exists()) {
                LOG.warn("Config path {} does not exist!", file
                        .getAbsolutePath());
            } else if (file.isDirectory()) {
                for (File f : Files.fileTreeTraverser()
                        .breadthFirstTraversal(file)) {
                    if (f.isFile() && p.apply(f)) {
                        paths.add(f);
                    } else if (!f.isDirectory()) {
                        LOG.info("Ignoring {}.", f);
                    }
                }
            } else if (file.isFile() && p.apply(file)) {
                paths.add(file);
            } else {
                LOG.info("Ignoring {}.", file);
            }
        }
        Iterable<YamlNode> nodes = Collections.<YamlNode>emptyList();
        Yaml yaml = new Yaml();
        for (File path : paths) {
            try {
                nodes = Iterables.concat(nodes, yaml
                        .loadAll(new FileInputStream(path)));
            } catch (FileNotFoundException ex) {
                LOG.warn("Could not load config at " + path, ex);
            }
        }
        return FluentIterable.from(nodes);
    }

    @Override
    public Collection<String> getAlgorithmNames() {
        return descriptions.keySet();
    }

    @Override
    public IAlgorithm getAlgorithm(String name) {
        if (!containsAlgorithm(name)) {
            return null;
        }
        return new MatlabAlgorithm(descriptions.get(name));
    }

    @Override
    public ProcessDescriptionType getProcessDescription(String name) {
        final IAlgorithm a = getAlgorithm(name);
        return a == null ? null : a.getDescription();
    }

    @Override
    public boolean containsAlgorithm(String name) {
        return descriptions.containsKey(name);
    }

    @Override
    public void shutdown() {
        /* noop */
    }

    private static ListMultimap<String, String> getProperties() {
        String name = MatlabAlgorithmRepository.class.getCanonicalName();
        return toMultimap(WPSConfig.getInstance()
                .getPropertiesForRepositoryClass(name));
    }

    private static ListMultimap<String, String> toMultimap(Property[] properties) {
        ListMultimap<String, String> p = ArrayListMultimap.create();
        if (properties != null) {
            for (Property property : properties) {
                if (property.getActive()) {
                    p.put(property.getName(), property.getStringValue());
                }
            }
        }
        return p;
    }

}
