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

package org.n52.wps.install.wizard.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DirectoryCopier {

	  public static void copyDirectory(File srcPath, File dstPath)
	                               throws IOException{
	  
	  if (srcPath.isDirectory()){

	      if (!dstPath.exists()){

	        dstPath.mkdir();
	 
	     }

	 
	     String files[] = srcPath.list();
	  
	    for(int i = 0; i < files.length; i++){
	        copyDirectory(new File(srcPath, files[i]), 
	                     new File(dstPath, files[i]));
	 
	      }

	    }
	 
	   else{
	 
	      if(!srcPath.exists()){

	        throw new RuntimeException("Directory does not exists");
	 
	      

	      }else
	 
	      {
	 
	       InputStream in = new FileInputStream(srcPath);
	       OutputStream out = new FileOutputStream(dstPath); 
	                     // Transfer bytes from in to out
	            byte[] buf = new byte[10024];
	 
	              int len;
	 
	           while ((len = in.read(buf)) > 0) {
	 
	          out.write(buf, 0, len);

	        }
	 
	       in.close();
	 
	           out.close();

	      }
	 
	   }
	   

	 
	 
	}
}
