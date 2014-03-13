package com.github.autermann.wps;

import com.github.autermann.wps.commons.WPS;
import com.github.autermann.wps.matlab.MatlabAlgorithmRepository;
import com.github.autermann.wps.matlab.MatlabFileHandler;
import com.google.common.collect.ImmutableMultimap;

public class Main {

    public static void main(String[] args)
            throws Exception {
        new WPS("localhost", 12121)
                .addAlgorithmRepository(MatlabAlgorithmRepository.class, ImmutableMultimap
                        .of(MatlabAlgorithmRepository.CONFIG_PROPERTY,
                            "/home/auti/Source/Lake-Analyzer/WPS/lakeAnalyzer.yaml"))
                .addGenerator(MatlabFileHandler.class)
                .addParser(MatlabFileHandler.class)
                .start();
    }
}
