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



import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

    /**
     * @version 1.1 01/15/99
     */
    public class CheckNodeTree extends JPanel {

    	private CheckNode[] nodes;
    	
    	public CheckNodeTree(CheckNode[] nodeList){
    		this.nodes = nodeList;
    		JTree tree = new JTree( nodes[0] );
    		
    	    tree.setCellRenderer(new CheckRenderer());
    	    tree.getSelectionModel().setSelectionMode(
    	    TreeSelectionModel.SINGLE_TREE_SELECTION
    	    );
    	    tree.putClientProperty("JTree.lineStyle", "Angled");
    	    tree.addMouseListener(new NodeSelectionListener(tree));
    	    JScrollPane sp = new JScrollPane(tree);
    	    sp.setPreferredSize(new Dimension(400,250));
    	    this.setPreferredSize(new Dimension(400,250));
    	    this.add(sp);
    	}
      
      public CheckNodeTree(String[] parsers, String toplevelName) {
        nodes = new CheckNode[parsers.length+1];
        CheckNode root = new CheckNode(toplevelName);
        nodes[0] = root;
        for (int i=1;i<parsers.length+1;i++) {
        	nodes[i] = new CheckNode(parsers[i-1]);
        	root.add(nodes[i]);	
            nodes[i].setSelected(true);
        }
        
        JTree tree = new JTree( nodes[0] );
        tree.setCellRenderer(new CheckRenderer());
        tree.getSelectionModel().setSelectionMode(
          TreeSelectionModel.SINGLE_TREE_SELECTION
        );
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.addMouseListener(new NodeSelectionListener(tree));
        JScrollPane sp = new JScrollPane(tree);
        sp.setPreferredSize(new Dimension(400,250));
        this.setPreferredSize(new Dimension(400,250));
        this.add(sp);
        
      }

      class NodeSelectionListener extends MouseAdapter {
        JTree tree;
        
        NodeSelectionListener(JTree tree) {
          this.tree = tree;
        }
        
        public void mouseClicked(MouseEvent e) {
          int x = e.getX();
          int y = e.getY();
          int row = tree.getRowForLocation(x, y);
          TreePath  path = tree.getPathForRow(row);
          //TreePath  path = tree.getSelectionPath();
          if (path != null) {
            CheckNode node = (CheckNode)path.getLastPathComponent();
            boolean isSelected = ! (node.isSelected());
            node.setSelected(isSelected);
            if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION) {
              if ( isSelected ) {
                tree.expandPath(path);
              } else {
                tree.collapsePath(path);
              }
            }
            ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
            // I need revalidate if node is root.  but why?
            if (row == 0) {
              tree.revalidate();
              tree.repaint();
            }
          }
        }
      }
      
      public CheckNode[] getNodes(){
    	  return nodes;
      }
      
      
      public CheckNode[] getCheckedNodes(){
    	  List<CheckNode> list = new ArrayList<CheckNode>();
    	  for(CheckNode node : nodes){
    		  if(node.isSelected){
    			  list.add(node);
    		  }
    	  }
    	  
    	  CheckNode[] resultList = new CheckNode[list.size()];
    	  for(int i = 0; i< list.size(); i++){
    		  resultList[i] = list.get(i);
    	  }
    	  
    	  return resultList;
      }
    

    }


