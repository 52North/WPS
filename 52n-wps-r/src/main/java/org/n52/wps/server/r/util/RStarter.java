/**
 * Copyright (C) 2010-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.r.util;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starting RServe via command line on different OS.
 * 
 * For documentation see http://www.rforge.net/Rserve/doc.html
 * 
 * For information about RServe on Windows see http://rforge.net/Rserve/rserve-win.html
 * 
 * @author Daniel
 * 
 */
public class RStarter {

    private static Logger log = LoggerFactory.getLogger(RStarter.class);

    private static boolean classicStartCommand = false;

    // TODO: make starter non-static and variables configurable
    public enum OutputLevel {
        quiet, slave, verbose;

        public String getCommand() {
            switch (this) {
            case quiet:
                return "--quiet";
            case slave:
                return "--slave";
            case verbose:
                return "--verbose";
            default:
                return slave.getCommand();
            }
        }
    }

    private static void startRServeOnLinux() throws InterruptedException, IOException {
        String rserveStartCMD = "R CMD Rserve --vanilla --slave"; // TODO make configurable
        Runtime.getRuntime().exec(rserveStartCMD).waitFor();
    }

    /**
     * command for local testing: cmd /c start R --vanilla --slave -e library(Rserve);Rserve()
     * 
     * @throws IOException
     */
    private static void startRServeOnWindows() throws IOException {
        // TODO save the process id so that it can be destroyed on shutdown.

        if (classicStartCommand) {
            String rserveStartCMD = "cmd /c start R -e library(Rserve);Rserve() --vanilla --slave";
            Runtime.getRuntime().exec(rserveStartCMD);
        }
        else {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "R",
            // log.isDebugEnabled() ? OutputLevel.verbose.getCommand()
            // : OutputLevel.slave.getCommand(),
                                                   "-e",
                                                   "library(Rserve);Rserve()",
                                                   "--vanilla",
                                                   OutputLevel.slave.getCommand());
            pb.inheritIO(); // nothing here since the command starts a new shell

            log.info("ProcessBuilder: {} | command: {} | directory: {} | environment: {}",
                     pb,
                     pb.command(),
                     pb.directory(),
                     Arrays.toString(pb.environment().entrySet().toArray()));
            Process process = pb.start();

            log.info("Process: {}, alive: {}", process.toString(), process.isAlive());
            // process should have already exited at this point
            // TODO see if access to the started shell is possible, maybe this helps:
            // http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
            if ( !process.isAlive())
                log.debug("Process exit status: {}", process.exitValue());
        }
    }

    public synchronized void startR() throws InterruptedException, IOException {
        log.debug("Starting R locally...");

        if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) {
            startRServeOnLinux();
        }
        else if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
            startRServeOnWindows();
        }

        log.info("Started R.");
    }

    /**
     * TODO test this method
     */
    public void killRserveOnWindows() {
        try {
            if (Runtime.getRuntime().exec("taskkill /IM RServe.exe /T /F").waitFor() == 0)
                return;
        }
        catch (InterruptedException | IOException e) {
            log.warn("Error trying to stop Rserve on windows.", e);
        }
    }

}
