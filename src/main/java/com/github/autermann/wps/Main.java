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
package com.github.autermann.wps;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.autermann.wps.commons.WPS;
import com.github.autermann.wps.matlab.MatlabAlgorithmRepository;
import com.github.autermann.wps.matlab.MatlabFileHandler;


public class Main {

    public static void main(String[] args)
            throws Exception {
        Map<String, List<String>> properties = new HashMap<>();
        List<String> configs = properties.computeIfAbsent(MatlabAlgorithmRepository.CONFIG_PROPERTY, k -> new LinkedList<>());

        configs.add("classpath:add.yml");
        configs.add("classpath:org/asdf/asdf.yml");

        new WPS("localhost", 12121)
                .addAlgorithmRepository(MatlabAlgorithmRepository.class, properties)
                .addGenerator(MatlabFileHandler.class)
                .addParser(MatlabFileHandler.class)
                .start();


    }
}
