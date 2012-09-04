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
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.VectorPlaylistBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.generator.VectorPlaylistGenerator;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.handler.PlaylistInputHandler;
import org.n52.wps.server.handler.PlaylistOutputHandler;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.request.ExecuteRequest;

/**
 * Class to define full streaming for vectors.
 * The response is sent right away, input data come in a playlist that is 
 *  constantly read in another thread, where the data are processed as well. 
 *  Intermediate results' URLs are appended to an output playlist
 * 
 * @author German Carrillo
 *
 */
public abstract class AbstractVectorFullStreamingAlgorithm extends AbstractSelfDescribingAlgorithm implements Runnable, IObserver {

	private AbstractSelfDescribingAlgorithm delegate;
	private PlaylistOutputHandler playlistOutputHandler;
	private PlaylistInputHandler playlistInputHandler;
	private Map<String, List<IData>> inputData; 
	private ExecuteRequest executeRequest;
	private int noOfChunk = 0; // Current chunk's ID
	private int loadedChunks = 0; // No. of chunks to be processed  
	private int deliveredChunks = 0; // No. of chunks already processed and appended 
	
	public abstract String getBaseAlgorithmName();
	public abstract String getInputStreamableIdentifier();
	public abstract String getOutputIdentifier();
	public abstract int getTimeSlot(); // How often should the playlist be read
	
	protected static Logger LOGGER = Logger.getLogger(AbstractVectorFullStreamingAlgorithm.class);
	
	public AbstractVectorFullStreamingAlgorithm() {
		initDelegate();
	}
	
	/**
	 *	Gets the base algorithm. It is not placed in the constructor because 
	 *   it can be called by other methods before.  
	 */
	private void initDelegate() {
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
	
		/* Check parameters */
		List<IData> playlistDataList = inputData.get(getInputStreamableIdentifier());
		if (playlistDataList == null || playlistDataList.size() != 1) {
			throw new RuntimeException("Error while allocating input parameters");
		}	
		
		Integer maxTimeIdle;
		List<IData> maxTimeDataList = inputData.get("MaxTimeIdle");
		if (maxTimeDataList == null || maxTimeDataList.size() != 1) {
			throw new RuntimeException("Error while allocating input parameters");
		} 
		maxTimeIdle = ((LiteralIntBinding) maxTimeDataList.get(0)).getPayload();
		if (maxTimeIdle < getTimeSlot() || maxTimeIdle > Integer.MAX_VALUE) {
			throw new RuntimeException(
				"The parameter maxTimeIdle must be greater than or equal to " 
				+ getTimeSlot() + " and less than " + Integer.MAX_VALUE 
				+ " miliseconds.");
		}		
		
		this.inputData = inputData;
		
		/* Get input's mimeType, schema and encoding, 
		 * associated parser and create handler */
		Map<String,String> inputFormat = getInputFormat();
		Class algorithmInput = RepositoryManager.getInstance()
			.getInputDataTypeForAlgorithm(getBaseAlgorithmName(), 
					getInputStreamableIdentifier());
		
		IParser chunkParser = ParserFactory.getInstance()
			.getParser(inputFormat.get("schema"), inputFormat.get("mimeType"), 
					inputFormat.get("encoding"), algorithmInput);
		
		playlistInputHandler = new PlaylistInputHandler(chunkParser, 
				inputFormat.get("mimeType"), inputFormat.get("schema"), 
				inputFormat.get("encoding"));
		playlistInputHandler.addObserver(this);
		
		/* Get output's mimeType, schema and encoding, 
		 * associated generator and create handler */
		Map<String,String> outputFormat = getOutputFormat(); // Get output's mimeType, schema and encoding 
		IGenerator chunkGenerator = GeneratorFactory.getInstance()
			.getGenerator(outputFormat.get("schema"), 
					outputFormat.get("mimeType"), outputFormat.get("encoding"),
					GTVectorDataBinding.class);
		
		playlistOutputHandler = new PlaylistOutputHandler(new VectorPlaylistGenerator(), 
				chunkGenerator, outputFormat.get("mimeType"), 
				outputFormat.get("schema"), outputFormat.get("encoding"));
		
		String url = null;
		url = playlistOutputHandler.createPlaylist();
		    
		/* Start the new thread for reading and processing chunks */
		(new Thread(this)).start();
	
		Map<String, IData> result = new HashMap<String, IData>();
		result.put(getOutputIdentifiers().get(0), new VectorPlaylistBinding(url));
		return result;
	}
	
	/**
	 * New thread's run method
	 */
	@Override
	public void run() {
		// Get the playlistURL from the input data
		List<IData> playlistDataList = inputData.get(getInputStreamableIdentifier());			
		String playlistURL = ((LiteralStringBinding) playlistDataList.get(0)).getPayload(); 
		
		// Get the maxTimeIdle from the input data
		Integer maxTimeIdle;
		List<IData> maxTimeDataList = inputData.get("MaxTimeIdle");
		maxTimeIdle = ((LiteralIntBinding) maxTimeDataList.get(0)).getPayload();
		
		playlistInputHandler.start(playlistURL, getTimeSlot(), maxTimeIdle);
		LOGGER.info("Reading input playlist...");
	}
	
	/**
	 * Get input parameter's mimeType, encoding, and schema. I guess this 
	 *  method could be offered in the ExecuteResponseBuilder class
	 * 
	 * @return HashMap with mimeType, encoding, and schema
	 */
	private Map<String, String> getInputFormat() {
		String mimeType = null;
		String schema = null;
		String encoding = null;
		
		// Try first from the request itself 
		InputType[] inputs = executeRequest.getExecute().getDataInputs().getInputArray();
		for (InputType input:inputs) {
			if (input.getIdentifier().getStringValue().equalsIgnoreCase(getInputStreamableIdentifier())) {
				if (input.getData() != null) {
					mimeType = input.getData().getComplexData().getMimeType();
					schema = input.getData().getComplexData().getSchema();
					encoding = input.getData().getComplexData().getEncoding();
				} else {
					mimeType = input.getReference().getMimeType();
					schema = input.getReference().getSchema();
					encoding = input.getReference().getEncoding();
				}
				break;
			}
		}
		
		// If not, get mimeType/encoding/schema from the process description
		if (mimeType == null && schema == null && encoding == null) { 
			InputDescriptionType[] inputDescs = description.getDataInputs().getInputArray();
			for (InputDescriptionType input : inputDescs) {
				if (input.getIdentifier().getStringValue().equalsIgnoreCase(getInputStreamableIdentifier())) {
					ComplexDataDescriptionType descType = input.getComplexData().getDefault().getFormat();
					if (mimeType == null) { 
						mimeType = descType.getMimeType();
					}
					if (schema == null) { 
						schema = descType.getSchema();
					}
					if (encoding == null) {
						encoding = descType.getEncoding();	
					}		
					break;
				}
			}	
		}		
		
		Map<String,String> format = new HashMap<String, String>();
		format.put("mimeType", mimeType.split("\\+")[1]); //We want the chunk's mimeType 
		format.put("schema", schema);
		format.put("encoding", encoding);		
		return format;
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
		format.put("mimeType", mimeType.split("\\+")[1]); //We want the chunk's mimeType 
		format.put("schema", schema);
		format.put("encoding", encoding);		
		return format;
	}
	
	@Override
	public List<String> getInputIdentifiers() {
		initDelegate();
		List<String> identifierList = delegate.getInputIdentifiers();
		identifierList.add("MaxTimeIdle");
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
		if (id.equalsIgnoreCase("MaxTimeIdle")) {
			return LiteralIntBinding.class;
		} else if (id.equalsIgnoreCase(getInputStreamableIdentifier())) {
			return VectorPlaylistBinding.class;
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
	 * Observer pattern's update method
	 */
	@Override
	public void update(Object state) {
		if (!playlistOutputHandler.isClosed) {
			
			/* State may hold 3 different objects: Spatial data chunk,
			 * 	playlist finished notification, and exception */
			if (state instanceof String) {
				String stringState = (String) state;
				if (stringState.contains("PLAYLIST_FINISHED")) {
					/* Get the number of chunks in the input playlist */
					loadedChunks = Integer.parseInt(stringState.split(":")[1]);
				} 				
			} else if (state instanceof IData) {
				noOfChunk += 1; 
				processChunk((IData) state, noOfChunk);
				deliveredChunks += 1;
			} else if (state instanceof RuntimeException) {
				handleException((RuntimeException) state);
				return;
			}
			
			if (deliveredChunks == loadedChunks) {
				playlistOutputHandler.closePlaylist();
				playlistInputHandler.stop();
			}				
		} else {
			LOGGER.warn("Output playlist already closed, skipping updating.");
		}
	}

	private void processChunk(IData chunk, int noOfChunk) {
		
		/* Create input data as expected by the base algorithm */ 
		Map<String, List<IData>> inputDataChunk = new HashMap<String, List<IData>>();
		inputDataChunk.put(getInputStreamableIdentifier(), new ArrayList<IData>(Arrays.asList(chunk)));			
		for (String key : inputData.keySet()) {
			if (!key.equalsIgnoreCase(getInputStreamableIdentifier()) && !key.equalsIgnoreCase("MaxTimeIdle")) {
				inputDataChunk.put(key, inputData.get(key));
			}
		}

		// Process and store the chunk
		try{
			if (!playlistOutputHandler.isClosed) { // Is it worth processing incoming chunks? 
				Map<String, IData> result = delegate.run(inputDataChunk);
				
				if (result.get(getOutputIdentifier()) == null) {
					throw new RuntimeException("Error while allocating intermediate results");
				}
				playlistOutputHandler.appendChunk(result.get(getOutputIdentifier()), Integer.toString(noOfChunk));
			} else {
				LOGGER.warn("Output playlist already closed, skipping processing.");
			}		
		} catch(RuntimeException e) {
			handleException(e);
		}
	}
	
	/**
	 * Takes care of exceptions while reading or processing chunks. These 
	 * 	exceptions are notified via the playlist, rather than the regular 
	 * 	response document. This method is executed only by one thread at once, 
	 * 	other threads are blocked for preventing multiple notifications
	 * 
	 * @param e
	 * 			The exception whose URL will be appended to the playlist
	 */
	private synchronized void handleException(RuntimeException e) {
		if (!playlistOutputHandler.isClosed) {
			ExceptionReport exception = new ExceptionReport(e.getMessage(),	"NoApplicableCode", e);
			playlistInputHandler.stop();
			playlistOutputHandler.appendException(exception);
			playlistOutputHandler.closePlaylist();
			ExecutionContextFactory.unregisterContext();
		} else {
			LOGGER.warn("Output playlist already closed, skipping appending exception.");			
		}
	}
}
