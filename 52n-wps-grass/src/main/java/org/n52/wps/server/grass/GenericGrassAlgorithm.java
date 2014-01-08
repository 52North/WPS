/**
 * ﻿Copyright (C) 2007
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

package org.n52.wps.server.grass;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.server.AbstractAlgorithm;

/**
 * @author Benjamin Pross (bpross-52n)
 *
 */
public abstract class GenericGrassAlgorithm extends AbstractAlgorithm{

	private ProcessDescriptionType description;
	private final String wkName;
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public GenericGrassAlgorithm() {
		this.description = initializeDescription();
		this.wkName = "";
	}
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public GenericGrassAlgorithm(String wellKnownName) {
		this.wkName = wellKnownName; // Has to be initialized before the description. 
		this.description = initializeDescription();
	}
	
	/** 
	 * This method should be overwritten, in case you want to have a way of initializing.
	 * 
	 * In detail it looks for a xml descfile, which is located in the same directory as the implementing class and has the same
	 * name as the class, but with the extension XML.
	 * @return
	 */
	protected ProcessDescriptionType initializeDescription() {
		return null;
	}
	
	public ProcessDescriptionType getDescription()  {
		return description;
	}

	public boolean processDescriptionIsValid() {
		return description.validate();
	}
	
	public String getWellKnownName() {
		return this.wkName;
	}


	
	
	
	
}
