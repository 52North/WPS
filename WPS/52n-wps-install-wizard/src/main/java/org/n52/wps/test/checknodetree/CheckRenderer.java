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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.plaf.ColorUIResource;


/**
 * @version 1.0 01/11/99
 */
public class CheckRenderer extends JPanel implements TreeCellRenderer {
  protected JCheckBox check;
  protected TreeLabel label;
  
  public CheckRenderer() {
    setLayout(null);
    add(check = new JCheckBox());
    add(label = new TreeLabel());
    check.setBackground(UIManager.getColor("Tree.textBackground"));
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value,
               boolean isSelected, boolean expanded,
               boolean leaf, int row, boolean hasFocus) {
    String  stringValue = tree.convertValueToText(value, isSelected,
			expanded, leaf, row, hasFocus);
    setEnabled(tree.isEnabled());
    check.setSelected(((CheckNode)value).isSelected());
    label.setFont(tree.getFont());
    label.setText(stringValue);
    label.setSelected(isSelected);
    label.setFocus(hasFocus);
    if (leaf) {
      label.setIcon(UIManager.getIcon("Tree.leafIcon"));
    } else if (expanded) {
      label.setIcon(UIManager.getIcon("Tree.openIcon"));
    } else {
      label.setIcon(UIManager.getIcon("Tree.closedIcon"));
    }	    
    return this;
  }
  
  public Dimension getPreferredSize() {
    Dimension d_check = check.getPreferredSize();
    Dimension d_label = label.getPreferredSize();
    return new Dimension(d_check.width  + d_label.width,
      (d_check.height < d_label.height ?
       d_label.height : d_check.height));
  }
  
  public void doLayout() {
    Dimension d_check = check.getPreferredSize();
    Dimension d_label = label.getPreferredSize();
    int y_check = 0;
    int y_label = 0;
    if (d_check.height < d_label.height) {
      y_check = (d_label.height - d_check.height)/2;
    } else {
      y_label = (d_check.height - d_label.height)/2;
    }
    check.setLocation(0,y_check);
    check.setBounds(0,y_check,d_check.width,d_check.height);
    label.setLocation(d_check.width,y_label);    
    label.setBounds(d_check.width,y_label,d_label.width,d_label.height);    
  }
   
  
  public void setBackground(Color color) {
    if (color instanceof ColorUIResource)
      color = null;
    super.setBackground(color);
  }
  
    
  class TreeLabel extends JLabel {
    boolean isSelected;
    boolean hasFocus;
    
    TreeLabel() {
    }
        
    public void setBackground(Color color) {
	if(color instanceof ColorUIResource)
	    color = null;
	super.setBackground(color);
    } 
         
    public void paint(Graphics g) {
      String str;
      if ((str = getText()) != null) {
        if (0 < str.length()) {
          if (isSelected) {
            g.setColor(UIManager.getColor("Tree.selectionBackground"));
          } else {
            g.setColor(UIManager.getColor("Tree.textBackground"));
          }
          Dimension d = getPreferredSize();
          int imageOffset = 0;
          Icon currentI = getIcon();
          if (currentI != null) {
            imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
          }
          g.fillRect(imageOffset, 0, d.width -1 - imageOffset, d.height);
          if (hasFocus) {
            g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
            g.drawRect(imageOffset, 0, d.width -1 - imageOffset, d.height -1);     
         }
        }
      }
      super.paint(g);
    }
  
    public Dimension getPreferredSize() {
      Dimension retDimension = super.getPreferredSize();
      if (retDimension != null) {
        retDimension = new Dimension(retDimension.width + 3,
				 retDimension.height);
      }
      return retDimension;
    }
    
    void setSelected(boolean isSelected) {
      this.isSelected = isSelected;
    }
    
    void setFocus(boolean hasFocus) {
      this.hasFocus = hasFocus;
    }
  }
}    
