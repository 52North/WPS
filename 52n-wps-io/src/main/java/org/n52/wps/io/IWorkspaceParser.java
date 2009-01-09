package org.n52.wps.io;

import java.io.File;
import java.io.InputStream;

import org.n52.wps.io.data.IData;

public interface IWorkspaceParser extends IParser {
	IData parse(InputStream stream, File directory);
}
