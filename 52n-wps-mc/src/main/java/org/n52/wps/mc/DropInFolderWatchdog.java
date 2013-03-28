/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.mc;

import java.io.File;

import org.n52.movingcode.runtime.coderepository.IMovingCodeRepository;
import org.n52.movingcode.runtime.coderepository.LocalDropInFolderRepository;
import org.n52.movingcode.runtime.coderepository.RepositoryChangeListener;
import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropInFolderWatchdog {

	private static final Logger logger = LoggerFactory.getLogger(DropInFolderWatchdog.class);
	private IMovingCodeRepository repository;

	public DropInFolderWatchdog(String wpsBaseDir, String dropInFolderDir,
			int checkIntervalSeconds) {
		File folder = new File(wpsBaseDir, dropInFolderDir);
		
		this.repository = new LocalDropInFolderRepository(folder, checkIntervalSeconds);
		this.repository.addRepositoryChangeListener(new RepositoryChangeListener() {
			@Override
			public void onRepositoryUpdate(IMovingCodeRepository updatedRepo) {
				logger.info("Invoking Capabilities updated...");
				WPSConfig.getInstance().firePropertyChange(
						WPSConfig.WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME);				
			}
		});
	}


}
