/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.github.autermann.wps.matlab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.matlab.description.MatlabProcessDescription;
import com.github.autermann.wps.matlab.util.FileExtensionPredicate;
import com.github.autermann.yaml.Yaml;
import com.github.autermann.yaml.YamlNode;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

    private final Map<String, MatlabAlgorithm> descriptions = Maps.newHashMap();

    public MatlabAlgorithmRepository() {
        this(getProperties());
    }

    public MatlabAlgorithmRepository(ListMultimap<String, String> properties) {
        for (YamlNode config : getConfigPaths(properties.get(CONFIG_PROPERTY))) {
            MatlabProcessDescription description
                    = MatlabProcessDescription.load(config);
            LOG.info("Loaded Matlab process:{}", description.getId());
            descriptions.put(description.getId(), new MatlabAlgorithm(description));
        }
    }

    private Iterable<YamlNode> getConfigPaths(List<String> properties) {
        Predicate<File> p = FileExtensionPredicate.of("yaml", "yml");
        Set<File> paths = Sets.newHashSet();
        for (String configPath : properties) {
            File file = new File(configPath);
            if (!file.exists()) {
                LOG.warn("Config path {} does not exist!", file
                        .getAbsolutePath());
            } else if (file.isDirectory()) {
                for (File f : Files.fileTreeTraverser()
                        .breadthFirstTraversal(file)) {
                    if (f.isFile() && p.apply(f)) {
                        LOG.info("Loading configuration from {}", f);
                        paths.add(f);
                    } else if (!f.isDirectory()) {
                        LOG.info("Ignoring {}.", f);
                    }
                }
            } else if (file.isFile() && p.apply(file)) {
                LOG.info("Loading configuration from {}", file);
                paths.add(file);
            } else {
                LOG.info("Ignoring {}.", file);
            }
        }
        List<YamlNode> nodes = Lists.newLinkedList();
        Yaml yaml = new Yaml();
        for (File path : paths) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(path);
                for (YamlNode node : yaml.loadAll(in)) {
                    nodes.add(node);
                }
            } catch (FileNotFoundException ex) {
                LOG.warn("Could not load config at " + path, ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {}
                }
            }
        }
        return nodes;
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
        return descriptions.get(name);
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
