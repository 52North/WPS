/***************************************************************
Copyright (C) 2012
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

package org.n52.wps.server.algorithm.streaming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.VectorPlaylistBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.datahandler.generator.VectorPlaylistGenerator;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.handler.PlaylistOutputHandler;
import org.n52.wps.server.request.ExecuteRequest;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Class to define output streaming for vectors.
 * The response is sent right away, the data are split and processed in 
 *  another thread. Intermediate results' URLs are appended to a playlist
 * 
 * @author German Carrillo
 *
 */
public abstract class AbstractVectorOutputStreamingAlgorithm extends AbstractSelfDescribingAlgorithm implements Runnable {

	private AbstractSelfDescribingAlgorithm delegate;
	private PlaylistOutputHandler playlistOutputHandler;
	private Map<String, List<IData>> inputData;
	private ExecuteRequest executeRequest;
	
	public abstract String getBaseAlgorithmName();
	public abstract String getInputStreamableIdentifier();
	public abstract String getOutputIdentifier();
	
	protected static Logger LOGGER = Logger.getLogger(AbstractVectorOutputStreamingAlgorithm.class);
	//long start; 
	
	public AbstractVectorOutputStreamingAlgorithm() {
		initDelegate();
	}
	
	/**
	 *	Gets the base algorithm. It is not placed in the constructor because 
	 *   it can be called by other methods before.  
	 */
	public void initDelegate() {
		delegate = (AbstractSelfDescribingAlgorithm) RepositoryManager
			.getInstance().getAlgorithm(getBaseAlgorithmName(), null);
	}
	
	/**
	 * Makes the executeRequest available in the algorithm's class
	 * 
	 * @param executeRequest
	 */
	public void setExecuteRequest(ExecuteRequest executeRequest) {
		this.executeRequest = executeRequest;
	}
	
	/**
	 * Business logic
	 */
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		//start = System.nanoTime(); 
		//LOGGER.info("Run method called...");
		
		Map<String,String> format = getOutputFormat(); // Gets mimeType, schema and encoding 
		
		/* Get the appropriate generator to create output chunks */
		IGenerator chunkGenerator = GeneratorFactory.getInstance()
			.getGenerator(format.get("schema"), format.get("mimeType"), 
					format.get("encoding"), GTVectorDataBinding.class);
		
		playlistOutputHandler = new PlaylistOutputHandler(new VectorPlaylistGenerator(), 
				chunkGenerator, format.get("mimeType"), format.get("schema"), 
				format.get("encoding"));
		
		/* Check parameters */
		if (inputData==null || !inputData.containsKey(getInputStreamableIdentifier())) {
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> dataList = inputData.get(getInputStreamableIdentifier());
		if (dataList == null || dataList.size() != 1) {
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData data = dataList.get(0);
		FeatureCollection<?, ?> featureCollection = ((GTVectorDataBinding) data).getPayload();
		
		if (inputData==null || !inputData.containsKey("NumberOfChunks")) {
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> chunksDataList = inputData.get("NumberOfChunks");
		if (chunksDataList == null || chunksDataList.size() != 1) {
			throw new RuntimeException("Error while allocating input parameters");
		}	
		Integer numberOfChunks = ((LiteralIntBinding) chunksDataList.get(0)).getPayload();
		if (numberOfChunks<1 || numberOfChunks>featureCollection.size()) {
			throw new RuntimeException("The parameter NumberOfChunks must be greater than 1 but not greater than the number of features");
		}
		
		this.inputData = inputData;
		
		String url = null;
		url = playlistOutputHandler.createPlaylist();
		    
		/* Start the new thread for processing chunks */
		(new Thread(this)).start();
	
		Map<String, IData> result = new HashMap<String, IData>();
		result.put("result", new VectorPlaylistBinding(url));
		return result;
	}
		
	/**
	 * New thread's run method
	 */
	@Override
	public void run() {
		// Get the data 
		List<IData> dataList = inputData.get(getInputStreamableIdentifier());
		IData data = dataList.get(0);
		
		// Get the number of chunks from the input data
		List<IData> chunksDataList = inputData.get("NumberOfChunks");
		Integer numberOfChunks = ((LiteralIntBinding) chunksDataList.get(0)).getPayload();
		
		splitInputData(data, numberOfChunks);
		
		//double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		//LOGGER.info("Chunks finished: " +  elapsedTimeInSec);
	}	

	public void splitInputData(IData data, Integer numberOfChunks) {
		
		int noOfChunk = 1;
		
		FeatureCollection<?, ?> featColl = ((GTVectorDataBinding) data).getPayload();

		double i = 0;
		int totalNumberOfFeatures = featColl.size();
		Integer chunkSize = (int) Math.floor(totalNumberOfFeatures / numberOfChunks.doubleValue()); 
		List<SimpleFeature> featList = new ArrayList<SimpleFeature>(); 
		
		FeatureIterator<?> featIterator = featColl.features();	
		try{			
			while ( featIterator.hasNext() ) {
				i= i+1;
				featList.add((SimpleFeature) featIterator.next());
				
				// Create the chunk now?  
				if ((i % chunkSize == 0 && noOfChunk < numberOfChunks) || i == totalNumberOfFeatures) {
					FeatureCollection<?, SimpleFeature> featureCollection = DefaultFeatureCollections.newCollection();
					featureCollection.addAll(featList);
					featList.clear();
					IData chunk = new GTVectorDataBinding(featureCollection);
					
					// Start a new thread here?
					processChunk(chunk, noOfChunk);
					noOfChunk=noOfChunk+1;					
				}
			}
		} finally {
			featIterator.close();
			playlistOutputHandler.closePlaylist();
		}
	}

	public void processChunk(IData splitData, int noOfChunk) {

		/* Create input data as expected by the base algorithm */ 
		Map<String, List<IData>> inputDataChunk = new HashMap<String, List<IData>>();
		inputDataChunk.put(getInputStreamableIdentifier(), new ArrayList<IData>(Arrays.asList(splitData)));			
		for (String key : inputData.keySet()) {
			if (!key.equalsIgnoreCase(getInputStreamableIdentifier()) && !key.equalsIgnoreCase("NumberOfChunks")) {
				inputDataChunk.put(key, inputData.get(key));
			}
		}

		try{
			// Process and store the chunk
			if (!playlistOutputHandler.isClosed) { // Is it worth processing incoming chunks? 
				Map<String, IData> result = delegate.run(inputDataChunk);
				
				if (result.get(getOutputIdentifier()) == null) {
					throw new RuntimeException("Error while allocating intermediate results");
				}
				playlistOutputHandler.appendChunk(result.get(getOutputIdentifier()), Integer.toString(noOfChunk));
			} else{
				LOGGER.warn("Output playlist already closed, skipping processing.");
			}
		} catch(RuntimeException e) {
			handleException(e);
		}
	}
	
	/**
	 * Get output parameter's mimeType, encoding, and schema. I guess this 
	 *  method could be offered in the ExecuteResponseBuilder class
	 * 
	 * @return HashMap with mimeType, encoding, and schema
	 */
	private Map<String, String> getOutputFormat() {
		String mimeType = null;
		String schema = null;
		String encoding = null;
		
		// Try first from the request itself 
		for(int i = 0; i<executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray().length; i++) {
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray(i);
			String outputIdentifierString = definition.getIdentifier().getStringValue();
			
			if (outputIdentifierString.equalsIgnoreCase(getOutputIdentifiers().get(0))) {
				mimeType = definition.getMimeType(); 
				schema = definition.getSchema(); 
				encoding = definition.getEncoding();
				break;
			}
		}	
	 
		// If not, get mimeType/encoding/schema from the process description		
		if (mimeType == null && schema == null && encoding == null) {
			OutputDescriptionType[] outputDescs = description.getProcessOutputs().getOutputArray();
			for (OutputDescriptionType output : outputDescs) {
				if (output.getIdentifier().getStringValue().equalsIgnoreCase(getOutputIdentifiers().get(0))) {
					ComplexDataDescriptionType descType = output.getComplexOutput().getDefault().getFormat();
					mimeType = descType.getMimeType();
					schema = descType.getSchema();
					encoding = descType.getEncoding();	
					break;
				}
			}	
		}		
		
		Map<String,String> format = new HashMap<String, String>();
		format.put("mimeType", mimeType.split("\\+")[1]); // We want the chunk's mimeType 
		format.put("schema", schema);
		format.put("encoding", encoding);		
		return format;
	}
	
	@Override
	public List<String> getInputIdentifiers() {
		initDelegate();
		List<String> identifierList = delegate.getInputIdentifiers();
		identifierList.add("NumberOfChunks");
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("result");
		return identifierList;
	}

	@Override
	public Class getInputDataType(String id) {
		if (id.equalsIgnoreCase("NumberOfChunks")) {
			return LiteralIntBinding.class;
		} else {
			initDelegate();
			return delegate.getInputDataType(id);
		}
	}
	
	@Override
	public Class getOutputDataType(String id) {
		return VectorPlaylistBinding.class;
	}		
	
	/**
	 * Takes care of exceptions while processing chunks. These exceptions are
	 *  notified via the playlist, rather than the regular response document.
	 *  This method is executed only by one thread at once, other threads are
	 *  blocked for preventing multiple notifications
	 * 
	 * @param e
	 * 			The exception whose URL will be appended to the playlist
	 */
	private synchronized void handleException(RuntimeException e) {
		if (!playlistOutputHandler.isClosed) {
			ExceptionReport exception = new ExceptionReport(e.getMessage(),	"NoApplicableCode", e);
			playlistOutputHandler.appendException(exception);
			playlistOutputHandler.closePlaylist();
			ExecutionContextFactory.unregisterContext();
		} else {
			LOGGER.warn("Output playlist already closed, skipping appending exception.");			
		}
	}
}
