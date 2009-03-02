/***************************************************************
 This implementation provides a framework to publish processes to the
 web through the  OGC Web Processing Service interface. The framework 
 is extensible in terms of processes and data handlers. It is compliant 
 to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2007 by con terra GmbH

 Authors:
 	Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany
	

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/
 
package org.n52.wps.test.checknodetree;


import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * @version 1.0 01/11/99
 */
public class CheckNode extends DefaultMutableTreeNode {

  public final static int SINGLE_SELECTION = 0;
  public final static int DIG_IN_SELECTION = 4;
  protected int selectionMode;
  protected boolean isSelected;
  private Object userObject;

  public CheckNode() {
    this(null);
  }

  public CheckNode(Object userObject) {
    this(userObject, true, false);
  }

  public CheckNode(Object userObject, boolean allowsChildren
                                    , boolean isSelected) {
    super(userObject, allowsChildren);
    this.isSelected = isSelected;
    setSelectionMode(DIG_IN_SELECTION);
  }


  public void setSelectionMode(int mode) {
    selectionMode = mode;
  }

  public int getSelectionMode() {
    return selectionMode;
  }

  public void setSelected(boolean isSelected) {
    this.isSelected = isSelected;
    
    if ((selectionMode == DIG_IN_SELECTION)
        && (children != null)) {
      Enumeration enum1 = children.elements();      
      while (enum1.hasMoreElements()) {
        CheckNode node = (CheckNode)enum1.nextElement();
        node.setSelected(isSelected);
      }
    }
  }
  
  public boolean isSelected() {
    return isSelected;
  }

  public Object getUserObject(){
	  return userObject;
  }

  // If you want to change "isSelected" by CellEditor,
  /*
  public void setUserObject(Object obj) {
    if (obj instanceof Boolean) {
      setSelected(((Boolean)obj).booleanValue());
    } else {
      super.setUserObject(obj);
    }
  }
  */

}


