package org.n52.wps.server.repository;

import java.io.File;

public interface IWorkspaceRepository {
	File getWorkspace();
	boolean createWorkspace();
}
