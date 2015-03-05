/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.wps.server.r.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.FilteredRConnection;
import org.n52.wps.server.r.RWPSConfigVariables;
import org.n52.wps.server.r.util.RLogger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RWorkspace {

    public enum CreationStrategy {
        DEFAULT, MANUAL, MANUALBASEDIR, PRESET, TEMPORARY;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private static final CreationStrategy DEFAULT_STRATEGY = CreationStrategy.DEFAULT;

    private static Logger log = LoggerFactory.getLogger(RWorkspace.class);

    private static final int TEMPDIR_NAME_LENGTH = 8;

    private static final String WORKSPACE_PREFIX = "wps4r-workspace-";

    public static final String ROOT = ".";

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMd-HHmmss");

    private static String createNewWorkspaceDirectoryName() {
        StringBuilder wd = new StringBuilder();
        wd.append(WORKSPACE_PREFIX);
        wd.append(dateFormat.format(new Date()));
        wd.append("_");
        wd.append(UUID.randomUUID().toString().substring(0, TEMPDIR_NAME_LENGTH));
        return wd.toString();
    }

    /**
     * Indicates if the R working directory should be deleted after process execution If wpsWorkDirIsRWorkDir
     * is set true, deletion of the directory shall be determined by deleteWPSWorDirectory
     */
    private boolean deleteRWorkDirectory = true;

    private String path = null;

    /**
     * In case of errors, this variable may be changed to true during runtime to prevent the system from
     * deleting the wrong files
     */
    private boolean temporarilyPreventingRWorkingDirectoryFromDelete = false;

    private boolean wpsWorkDirIsRWorkDir = true;

    private Path basedir;

    public RWorkspace(Path basedir) {
        this.basedir = basedir;
    }

    private REXP createAndSetNewWorkspaceDirectory(File directory, RConnection connection) throws RserveException {
        boolean b = directory.mkdir();
        if (b) {
            log.debug("Created new workdir: {}", directory);
            String wd = directory.getAbsolutePath();
            return setwd(connection, wd);
        }
        log.error("Could not create new temp workspace directory at {}", directory);
        return null;
    }

    private REXP createAndSetNewWorkspaceDirectoryInRTempdir(RConnection connection) throws RserveException {
        REXP oldWorkdir = connection.eval("setwd(\"tempdir()\")");
        return oldWorkdir;
    }

    private REXP createAndSetNewWorkspaceDirectoryInSystemTemp(RConnection connection) throws RserveException {
        File tempdir = new File(System.getProperty("java.io.tmpdir"), createNewWorkspaceDirectoryName());
        return createAndSetNewWorkspaceDirectory(tempdir, connection);
    }

    private REXP createAndSetNewWorkspaceDirectoryWithinCurrentRWD(RConnection connection) throws RserveException {
        String randomFolderName = createNewWorkspaceDirectoryName();
        connection.eval("dir.create(\"" + randomFolderName + "\")");
        REXP oldWorkdir = setwd(connection, randomFolderName);
        return oldWorkdir;
    }

    private REXP createAndSetNewWorkspaceDirectoyInBasePath(File f, RConnection connection) throws RserveException {
        String randomFolderName = createNewWorkspaceDirectoryName();
        File newDir = new File(f, randomFolderName);
        return createAndSetNewWorkspaceDirectory(newDir, connection);
    }

    /**
     * @return true if the unlink call was made successfully
     */
    public boolean deleteCurrentAndSetWorkdir(RConnection connection, String originalWorkDir) {
        if (this.wpsWorkDirIsRWorkDir && !connection.isConnected()) {
            // R won't delete the folder if it is the same as the wps work directory
            log.warn("Cannot delete directory, connection is not connected or workdir is WPS workdir ({}).",
                     this.wpsWorkDirIsRWorkDir);
            return false;
        }

        if ( !this.deleteRWorkDirectory) {
            log.warn("Deleting of the directory is disabled.");
            return false;
        }

        // delete R work directory
        if (this.path != null) {
            try {
                RLogger.log(connection, "Deleting workspace.");

                String wdToDelete = connection.eval("getwd()").asString();
                REXP oldwd = setwd(connection, originalWorkDir);
                log.debug("Set wd to {} (was: {})", oldwd.toDebugString(), wdToDelete);

                // should be true usually, if not, workdirectory has been changed unexpectedly (probably
                // inside script)
                if (wdToDelete != this.path) {
                    if ( !this.temporarilyPreventingRWorkingDirectoryFromDelete) {
                        log.debug("Unlinking (recursive delete) the directory {}", wdToDelete);
                        REXP eval = connection.eval("(unlink(\"" + wdToDelete + "\", recursive=TRUE))");

                        int result = eval.asInteger();
                        if (result == 0)
                            return true;
                        return false;
                    }

                    this.temporarilyPreventingRWorkingDirectoryFromDelete = false;
                }
                else
                    log.warn("Unexpected R workdirectory at end of R session, check the R sript for unwanted workdirectory changes.");
            }
            catch (RserveException e) {
                log.error("Could not reset the work directory.", e);
            }
            catch (REXPMismatchException e) {
                log.error("Could not reset the work directory.", e);
            }
        }

        return false;
    }

    public String getPath() {
        return path;
    }

    public boolean isWpsWorkDirIsRWorkDir() {
        return wpsWorkDirIsRWorkDir;
    }

    private REXP setwd(RConnection connection, String wd) throws RserveException {
        String wdString = wd.replace("\\", "/");
        REXP oldWorkdir = connection.eval("setwd(\"" + wdString + "\")");
        return oldWorkdir;
    }

    /**
     * Sets the R working directory according to the "R_Work_Dir" configuration parameter. 4 cases are
     * supported: 'default', 'preset', 'temporary' and 'custom'.
     * 
     * Do not confuse the R working directory with the temporary WPS working directory (this.currentworkdir)!
     * R and WPS use the same directory under default configuration, with Rserve on localhost, but running R
     * on a remote machine requires separate working directories for WPS and R.
     * 
     * @param connection
     * @param workDirName
     * 
     * @return the new working directory, which is already set.
     * @throws org.rosuda.REngine.REXPMismatchException
     */
    public String setWorkingDirectory(RConnection connection,
                                      String currentWorkDir,
                                      String strategyName,
                                      boolean isRserveOnLocalhost,
                                      String workDirName) throws REXPMismatchException,
            RserveException,
            ExceptionReport {
        log.debug("Setting the R working directory... current work directory: {}", currentWorkDir);

        log.debug("Try to set R work directory according to {} = {}",
                  RWPSConfigVariables.R_WORK_DIR_STRATEGY,
                  strategyName);
        REXP oldWorkdir = null;
        log.debug("Working on localhost: {}", isRserveOnLocalhost);

        CreationStrategy strategy = null;
        if (strategyName == null || strategyName.equals("")) {
            log.error("Strategy is not defined: {}. Falling back to default: {}", DEFAULT_STRATEGY);
            strategy = DEFAULT_STRATEGY;
        }
        else
            strategy = CreationStrategy.valueOf(strategyName.trim().toUpperCase());


        // if one of these strategies is used, then Java must be able to write in the folder and we need the
        // full path
        String workDirNameFullPath = null;
        if (strategy.equals(CreationStrategy.MANUALBASEDIR) || strategy.equals(CreationStrategy.MANUAL)) {
            try {
                if (workDirName == null)
                    throw new ExceptionReport("Error setting working directory with strategy '" + strategy +  "': workDirName is 'null'!", "Inconsistent property");

                File testFile = new File(workDirName);
                if ( !testFile.isAbsolute()) {
                    testFile = basedir.resolve(path).toFile();
                }
                if ( !testFile.exists())
                    throw new ExceptionReport("Invalid work dir name \"" + workDirName + "\" and full path \""
                            + testFile.getPath() + "\". It denotes a non-existent path.", "Inconsistent property");

                workDirNameFullPath = testFile.getAbsolutePath();
                log.debug("Manually set work dir name resolved to the full path '{}'", workDirNameFullPath);
            }
            catch (ExceptionReport e) {
                log.error("The config variable {} references a non-existing directory. This will be an issue if the variable is used. The current strategy is '{}'.",
                          RWPSConfigVariables.R_WORK_DIR_NAME,
                          strategy,
                          e);
            }
        }

        if (strategy.equals(DEFAULT_STRATEGY)) {
            // Default behaviour: R work directory is the same as temporary WPS work directory if R runs
            // locally otherwise, for remote connections, it is dependent on the configuration of R and Rserve
            if (isRserveOnLocalhost) {
                this.wpsWorkDirIsRWorkDir = true;
                oldWorkdir = createAndSetNewWorkspaceDirectoryInSystemTemp(connection);
            }
            else {
                // setting the R working directory relative to default R directory R starts from a work
                // directory dependent on the behaviour and configuration of the R/Rserve installation
                this.wpsWorkDirIsRWorkDir = false;
                oldWorkdir = createAndSetNewWorkspaceDirectoryWithinCurrentRWD(connection);
            }
        }
        else if (strategy.equals(CreationStrategy.PRESET)) {
            // setting the R working directory relative to default R directory using R
            wpsWorkDirIsRWorkDir = false;
            oldWorkdir = createAndSetNewWorkspaceDirectoryWithinCurrentRWD(connection);
        }
        // setting the R working directory in a temporal folder
        else if (strategy.equals(CreationStrategy.TEMPORARY)) {
            if (isRserveOnLocalhost) {
                wpsWorkDirIsRWorkDir = true;
                oldWorkdir = createAndSetNewWorkspaceDirectoryInSystemTemp(connection);
            }
            else {
                wpsWorkDirIsRWorkDir = false;
                try {
                    oldWorkdir = createAndSetNewWorkspaceDirectoryInRTempdir(connection);
                }
                catch (RserveException e) {
                    temporarilyPreventingRWorkingDirectoryFromDelete = true;
                    throw new ExceptionReport("Invalid configuration of WPS4R, failed to create temporal working directory",
                                              ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                              e);
                }

            }
        }
        else if (strategy.equals(CreationStrategy.MANUAL)) {
            // in the manual strategy, the path is simply used
            if (workDirName != null && !workDirName.isEmpty())
                oldWorkdir = setwd(connection, workDirName);
            else {
                log.error("Work directory name is not provided, falling back to default strategy.");
                return setWorkingDirectory(connection,
                                           currentWorkDir,
                                           DEFAULT_STRATEGY.toString(),
                                           isRserveOnLocalhost,
                                           workDirName);
            }
        }
        else if (strategy.equals(CreationStrategy.MANUALBASEDIR)) {
            // in the manualBaseDir strategy, the defined path is used as the base directory for random
            // workspace names
            File f = new File(workDirName);

            boolean isInvalidPath = false;
            if (isRserveOnLocalhost) {
                if (f.isDirectory()) {
                    oldWorkdir = createAndSetNewWorkspaceDirectoyInBasePath(f, connection);
                }
                else
                    isInvalidPath = true;

            }
            else {
                this.wpsWorkDirIsRWorkDir = false;
                boolean isExistingDir = connection.eval("isTRUE(file.info(\"" + strategy + "\")$isdir)").asInteger() == 1;
                if (isExistingDir) {
                    oldWorkdir = createAndSetNewWorkspaceDirectoyInBasePath(f, connection);
                }
                else
                    isInvalidPath = true;
            }

            if (isInvalidPath) {
                log.error("Invalid configurarion for work directory. | {}={} | {}={} | Falling back to '{}'.",
                          RWPSConfigVariables.R_WORK_DIR_STRATEGY,
                          strategy,
                          RWPSConfigVariables.R_WORK_DIR_NAME,
                          workDirName,
                          DEFAULT_STRATEGY);
                return setWorkingDirectory(connection,
                                           currentWorkDir,
                                           DEFAULT_STRATEGY.toString(),
                                           isRserveOnLocalhost,
                                           workDirName);
            }
        }

        REXP newWorkdir = connection.eval("getwd()");
        log.debug("Set workdir to {}, was {}", newWorkdir.toDebugString(), oldWorkdir.toDebugString());

        if (connection.eval("length(dir()) == 0").asInteger() != 1) {
            this.temporarilyPreventingRWorkingDirectoryFromDelete = true;
            throw new ExceptionReport("Non-empty R working directory on process startup: " + oldWorkdir.toDebugString()
                    + "\nThe process will not be executed to prevent the system from damage."
                    + " Please check the configuration of WPS4R.", ExceptionReport.REMOTE_COMPUTATION_ERROR);
        }

        RLogger.logGenericRProcess(connection, "working directory: " + connection.eval("getwd()").asString());

        String workDirPath = newWorkdir.asString();
        this.path = workDirPath;
        return getPath();
    }

    public Collection<File> listFiles() {
        ArrayList<File> files = new ArrayList<File>();
        if (this.path != null) {
            File f = new File(this.path);
            files.addAll(Arrays.asList(f.listFiles()));
        }
        else
            log.error("The path to the workspace is null, cannot create file list");
        return files;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RWorkspace [deleteRWorkDirectory=").append(deleteRWorkDirectory).append(", ");
        if (path != null)
            builder.append("path=").append(path).append(", ");
        builder.append("temporarilyPreventingRWorkingDirectoryFromDelete=").append(temporarilyPreventingRWorkingDirectoryFromDelete).append(", wpsWorkDirIsRWorkDir=").append(wpsWorkDirIsRWorkDir).append(", ");
        if (basedir != null)
            builder.append("basedir=").append(basedir);
        builder.append("]");
        return builder.toString();
    }

    public synchronized void createDirectory(String name, RConnection connection) throws RserveException {
        if (name.equals(ROOT))
            return;

        log.debug("Creating directory {} in workspace {}", name, this);

        StringBuilder sb = new StringBuilder();
        sb.append("dir.create(\"");
        sb.append(name);
        sb.append("\")");
        REXP result = connection.eval(sb.toString());
        log.debug("Output of creating directory in workdir: {}", result.toDebugString());
    }

    public REXP copyFile(String dirName, String file, RConnection connection) throws RserveException {
        log.debug("Copying file {} to dir '{}' in workspace {}", file, dirName, this);
        StringBuilder sb = new StringBuilder();
        sb.append("file.copy(from = \"");
        sb.append(file);
        sb.append("\", to = \"");
        sb.append(dirName);
        sb.append("/");
        sb.append(file);
        sb.append("\")");
        REXP result = connection.eval(sb.toString());
        return result;
    }

    public synchronized void copyFilesToDirectoryInWorkDir(String dirName, String[] files, RConnection connection) throws RserveException {
        for (String file : files) {
            REXP result = copyFile(dirName, file, connection);
            log.debug("Output of copying file to dir: {}", result.toDebugString());
        }
    }

    public void deleteFile(String file, RConnection connection) throws RserveException {
        log.debug("Removing file from workdir: {}", file);
        connection.removeFile(file);
    }

    public synchronized void deleteFilesInWorkDir(String[] files, RConnection connection) throws RserveException {
        for (String file : files) {
            deleteFile(file, connection);
        }
    }

    public void moveFile(String dirName, String file, RConnection connection) throws RserveException {
        log.debug("Moving file '{}' to '{}'", file, dirName);
        copyFile(dirName, file, connection);
        deleteFile(file, connection);
    }

    public synchronized void moveFilesToDirectoryInWorkDir(String dirName, String[] files, RConnection connection) throws RserveException {
        copyFilesToDirectoryInWorkDir(dirName, files, connection);
        deleteFilesInWorkDir(files, connection);
    }

    /**
     * 
     * @param source
     * @param name
     *        can be a path (starting with '.'), but all intermediate folders must exist
     * @param connection
     * @throws IOException
     */
    public void copyFile(File source, String name, FilteredRConnection connection) throws IOException {
        // use RFileOutputStream so that remote RServe is supported
        RFileOutputStream rfos = connection.createFile(name);

        byte[] buffer = new byte[2048];
        FileInputStream is = new FileInputStream(source);
        int stop = is.read(buffer);

        while (stop != -1) {
            rfos.write(buffer, 0, stop);
            stop = is.read(buffer);
        }

        rfos.flush();
        rfos.close();
        is.close();

        log.debug("File copied with R from {} as '{}' to {}", source, name, rfos);
    }

}
