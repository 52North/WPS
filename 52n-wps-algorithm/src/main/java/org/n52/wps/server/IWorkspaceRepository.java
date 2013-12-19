package org.n52.wps.server;

import java.io.File;

public interface IWorkspaceRepository {
	File getWorkspace();
	boolean createWorkspace();
}
