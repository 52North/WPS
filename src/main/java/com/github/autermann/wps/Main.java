package com.github.autermann.wps;

import com.github.autermann.wps.commons.WPS;
import com.github.autermann.wps.matlab.MatlabAlgorithmRepository;
import com.github.autermann.wps.matlab.MatlabFileHandler;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class Main {

    public static void main(String[] args) throws Exception {
        WPS wps = new WPS("localhost", 12121);
        SetMultimap<String, String> properties = HashMultimap.create();
        properties.put(MatlabAlgorithmRepository.CONFIG_PROPERTY,
                       "/home/auti/Source/Lake-Analyzer/WPS/lakeAnalyzer.yaml");
        wps.addAlgorithmRepository(MatlabAlgorithmRepository.class, properties);
        wps.addGenerator(MatlabFileHandler.class);
        wps.addParser(MatlabFileHandler.class);
        wps.start();
    }
}
