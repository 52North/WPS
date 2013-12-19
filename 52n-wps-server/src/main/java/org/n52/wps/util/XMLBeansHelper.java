/*****************************************************************
Copyright (C) 2007-2013
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License version 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.util;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;

/*
 * 
 * Some conveniant methods, to access some XMLBean objects.
 * @author foerster
 *
 */
public class XMLBeansHelper {
	public static OutputDescriptionType findOutputByID(String outputID, OutputDescriptionType[] outputDescs) {
		for(OutputDescriptionType desc : outputDescs) {
			if(desc.getIdentifier().getStringValue().equals(outputID)) {
				return desc;
			}
		}
		return null;
	}
	
	public static InputDescriptionType findInputByID(String outputID, DataInputs inputs) {
		for(InputDescriptionType desc : inputs.getInputArray()) {
			if(desc.getIdentifier().getStringValue().equals(outputID)) {
				return desc;
			}
		}
		return null;
	}
}
