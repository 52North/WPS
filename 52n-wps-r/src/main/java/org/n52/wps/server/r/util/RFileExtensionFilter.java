package org.n52.wps.server.r.util;

import java.io.File;
import java.io.FileFilter;

import org.n52.wps.server.r.R_Config;

public class RFileExtensionFilter implements FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isFile() && f.canRead()) {
            String name = f.getName();
            if (name.endsWith(R_Config.SCRIPT_FILE_SUFFIX))
                return true;
        }
        return false;
    }
}