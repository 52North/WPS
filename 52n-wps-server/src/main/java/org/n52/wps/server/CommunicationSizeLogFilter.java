/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

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

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.server;

// import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.LargeBufferStream;


class ResponseSizeInfoStream extends ServletOutputStream {
	private OutputStream intStream;
	private LargeBufferStream baStream;
	private boolean closed = false;
	private long streamSize = 0;
	
	public ResponseSizeInfoStream(OutputStream outStream) {
		this.intStream = outStream;
		// baStream = new ByteArrayOutputStream();
	}
     
    public void write(int i) throws java.io.IOException {
    	this.streamSize++;
    	this.intStream.write(i);
    }
     
    public void close() throws java.io.IOException {
    	if (!this.closed) {
    		this.intStream.close();
    		this.closed = true;
    	}
    }
    
/*    public void flush() throws java.io.IOException {
        if (baStream.size() != 0) {
             if (! closed) {
//              processStream();              // need to synchronize the flush!
//              baStream = new ByteArrayOutputStream();
              }
           }
        }
*/
    public void processStream() throws java.io.IOException {
    	baStream.close();
    	baStream.writeTo(intStream);
    	this.intStream.flush();
    }
     
    public byte []  countBytes(byte [] inBytes) {
    	//streamSize = streamSize + inBytes.length;
    	return inBytes;
    }
    
    public long getSize() {
    	if(this.closed) {
    		return streamSize;
    	} else 
    		return -1;
    }

}

class ResponseSizeInfoWrapper extends HttpServletResponseWrapper {
    private PrintWriter tpWriter; 
    private ResponseSizeInfoStream tpStream;

    public ResponseSizeInfoWrapper(ServletResponse inResp) throws java.io.IOException { 
            super((HttpServletResponse) inResp);
            tpStream = new ResponseSizeInfoStream(inResp.getOutputStream());
            tpWriter = new PrintWriter(tpStream);
    }

    public ServletOutputStream getOutputStream() throws java.io.IOException {

            return tpStream;
     }
    public PrintWriter getWriter() throws java.io.IOException {

            return tpWriter;
     }
}

class RequestSizeInfoStream extends ServletInputStream {
    // private BufferedInputStream buStream;
    private boolean closed = false;
    private long streamSize = 0;
    private InputStream inputStream;
    
	public RequestSizeInfoStream(InputStream inStream) {
		this.inputStream = inStream;
		// buStream = new BufferedInputStream(inStream);
	}

    @Override
	public int read() throws IOException {
    	this.streamSize++;
		return this.inputStream.read();
	}
    
    public void close() throws java.io.IOException {
    	if (!this.closed) {
    		// processStream();
        	this.inputStream.close();
        	this.closed = true;
        }
    }

 /*  public void processStream() throws IOException {
	   byte[] bytes = new byte[8096];
	   int length = buStream.read(bytes, 0 , 8096);
	   while(length != -1) {
		   length = buStream.read(bytes, 0 , 8096);
		   streamSize = streamSize + length;
	   }
       
   }*/
   
   public long getSize() {
	   if(this.closed) {
		   return this.streamSize;
	   }
	   else
		   return -1;
   }
}
class RequestSizeInfoWrapper extends HttpServletRequestWrapper {
    private BufferedReader tpReader; 
    private RequestSizeInfoStream tpStream;

    public RequestSizeInfoWrapper(ServletRequest req) throws java.io.IOException {
    	super((HttpServletRequest) req);
    	this.tpStream = new RequestSizeInfoStream(req.getInputStream());
    	this.tpReader = new BufferedReader(new InputStreamReader(this.tpStream));
    }

    public ServletInputStream getInputStream() throws java.io.IOException {
    	return this.tpStream;
    }

	public BufferedReader getReader() throws IOException {
		return this.tpReader;
	}   
}

/** This class measures the payload of the post data
 * 
 * @author foerster
 *
 */
public final class CommunicationSizeLogFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(CommunicationSizeLogFilter.class);
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
	throws IOException, ServletException {
    	RequestSizeInfoWrapper myWrappedReq = new RequestSizeInfoWrapper(request);
    	ResponseSizeInfoWrapper myWrappedResp = new ResponseSizeInfoWrapper(response);
    	chain.doFilter(myWrappedReq,  myWrappedResp);
    	myWrappedReq.getInputStream().close();
    	myWrappedResp.getOutputStream().close();
    	long requestSize = ((RequestSizeInfoStream)myWrappedReq.getInputStream()).getSize();
    	long responseSize = ((ResponseSizeInfoStream)myWrappedResp.getOutputStream()).getSize();
    	if(requestSize == 0) {
    		return;
    	}
    	BigDecimal result = new BigDecimal((double)responseSize/(double)requestSize).setScale(4, BigDecimal.ROUND_HALF_UP);
    	result = result.movePointRight(2);
    	LOGGER.info("Simplification ratio " + result);
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) {
    }
}

