/***************************************************************
Copyright ? 2007 52?North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

 Contact: Andreas Wytzisk, 
 52?North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation?s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axiom.om.util.CopyUtils;
import org.apache.commons.io.IOUtils;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.database.FlatFileDatabase;
import org.n52.wps.server.database.FolderDatabase;
import org.n52.wps.server.database.IDatabase;
import org.n52.wps.server.request.ExecuteRequest;

/**
 * This is a simple servlet, which serves processed results. It serves
 * as a frontend to the database.
 * @author foerster
 */
public class RetrieveResultServlet extends HttpServlet {
	// Universal version identifier for a Serializable class.
	// Should be used here, because HttpServlet implements the java.io.Serializable
	private static final long serialVersionUID = -268198171054599696L;
	
	/** The only accepted parameter is id. Otherwise it will return a ExceptionReportDocument. */
	public static String SERVLET_PATH = "RetrieveResultServlet";
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (SERVLET_PATH == null) {
			SERVLET_PATH = req.getContextPath();
		}
		String id = req.getParameter("id");
		
		/* Streaming based WPS */
		Boolean bFolder = id.toLowerCase().startsWith("dir");
		String folder=null;
		IDatabase db = null;
		if (bFolder) {
			id = id.substring(3);
			folder = id.split("_")[0];
			db = FolderDatabase.getInstance(); 
		}
		else{
			db = FlatFileDatabase.getInstance();
		}
		
		OutputStream os = res.getOutputStream();
		if(id == null || id.equals("")) {
			
			res.setContentType("text/html");
			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			PrintWriter pw = new PrintWriter(os);
			pw.write("<html><title>52n WPS - id not found</title><body><H1>ID not found: " + id + "<H1></body></html>");
		}

		//set appropriate mimetype for result
		String mimeType = db.getMimeTypeForStoreResponse(id);
		res.setContentType(mimeType);
		
		String suffix = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
		if(suffix==null){
			suffix = "dat";
		}
		// Not Supported workaround -> removed.
		File file = db.lookupResponseAsFile(id+"result."+suffix);
		String fileName = URLEncoder.encode(file.getName());
		
		String redirect;
		if (bFolder){
			redirect = "Databases/Folder/"+folder+"/"+fileName;
		}
		else{
			redirect = "Databases/FlatFile/"+fileName;
		}
		res.sendRedirect(redirect);
		res.flushBuffer();
//		
//		
//		db.deleteStoredResponse(id);
		res.flushBuffer();
	}	
}