package org.n52.wps.server.algorithm.streaming;

import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.RasterPlaylistBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.datahandler.generator.RasterPlaylistGenerator;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.handler.PlaylistOutputHandler;
import org.n52.wps.server.request.ExecuteRequest;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class AbstractRasterOutputStreamingAlgorithm extends AbstractSelfDescribingAlgorithm implements Runnable {

	private AbstractSelfDescribingAlgorithm delegate;
	private org.n52.wps.server.handler.PlaylistOutputHandler playlistOutputHandler;
	private Map<String, List<IData>> inputData;
	private ExecuteRequest executeRequest;
	
	public abstract String getBaseAlgorithmName();
	public abstract List<String> getInputStreamableIdentifiers();
	public abstract String getOutputIdentifier();
	
	protected static Logger LOGGER = Logger.getLogger(AbstractRasterOutputStreamingAlgorithm.class);
	long start; 
	
	public AbstractRasterOutputStreamingAlgorithm(){
		initDelegate();
	}
	
	public void initDelegate(){
		delegate = (AbstractSelfDescribingAlgorithm) RepositoryManager.getInstance().getAlgorithm(getBaseAlgorithmName(), null);
	}
	
	public void setExecuteRequest(ExecuteRequest executeRequest){
		this.executeRequest = executeRequest;
	}
	
	public AbstractRasterOutputStreamingAlgorithm runnableAlgorithm() {
		return this;
	}
	
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		start = System.nanoTime(); 
		LOGGER.info("Run method called...");
		
		Map<String,String> format = getOutputFormat(); // Gets mimeType, schema and encoding 
		
		// Get the appropriate generator to create output chunks
		IGenerator chunkGenerator = GeneratorFactory.getInstance().getGenerator(format.get("schema"), format.get("mimeType"), format.get("encoding"), GTRasterDataBinding.class);
		
		playlistOutputHandler = new PlaylistOutputHandler(new RasterPlaylistGenerator(), chunkGenerator, format.get("mimeType"), format.get("schema"), format.get("encoding"));
		
		// Check at least data and chunks are there
		List<RenderedImage> images = new ArrayList<RenderedImage>(); 
		Map<String, IData> datasets = new HashMap<String, IData>(); 
		for (String id : getInputStreamableIdentifiers()){
			if(inputData==null || !inputData.containsKey(id)){
				throw new RuntimeException("Error while allocating input parameters");
			}		
			List<IData> dataList = inputData.get(id);
			if(dataList == null || dataList.size() != 1){
				throw new RuntimeException("Error while allocating input parameters");
			}
			datasets.put(id, dataList.get(0));
			GridCoverage2D gridCoverage = (GridCoverage2D) datasets.get(id).getPayload();
			images.add(gridCoverage.getRenderedImage());
		}		
		
		if(inputData==null || !inputData.containsKey("chunksByRow") || !inputData.containsKey("chunksByColumn")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> chunksDataListRow = inputData.get("chunksByRow");
		if(chunksDataListRow == null || chunksDataListRow.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}	
		List<IData> chunksDataListColumn = inputData.get("chunksByColumn");
		if(chunksDataListColumn == null || chunksDataListColumn.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}	
		Integer chunksByRow = ((LiteralIntBinding) chunksDataListRow.get(0)).getPayload();
		Integer chunksByColumn = ((LiteralIntBinding) chunksDataListColumn.get(0)).getPayload();
		if (chunksByRow<1 || chunksByColumn<1){
			throw new RuntimeException("The parameters chunksByRow and chunksByColumn must be greater than 0.");
		}
		for (RenderedImage image : images){
			if (chunksByRow>image.getWidth() || chunksByColumn>image.getHeight()){
				throw new RuntimeException("The parameters chunksByRow and chunksByColumn must not be greater than the raster's width and height, respectively.");		
			}
		}
		
		this.inputData = inputData;
		
		String url = null;
		url = playlistOutputHandler.createPlaylist();
		    
		// Start the new thread for processing chunks
		(new Thread(runnableAlgorithm())).start();
	
		Map<String, IData> result = new HashMap<String, IData>();
		result.put("result", new RasterPlaylistBinding(url));
				
		return result;
	}
	
	@Override
	public void run() {
		// Get the data
		List<RenderedImage> images = new ArrayList<RenderedImage>(); 
		Map<String, IData> datasets = new HashMap<String, IData>(); 
		for (String id : getInputStreamableIdentifiers()){
			List<IData> dataList = inputData.get(id);
			datasets.put(id, dataList.get(0));
		}		
		
		// Get the number of chunks by row/column from the input data
		List<IData> chunksDataListRow = inputData.get("chunksByRow");
		Integer chunksByRow = ((LiteralIntBinding) chunksDataListRow.get(0)).getPayload();
		List<IData> chunksDataListColumn = inputData.get("chunksByColumn");		
		Integer chunksByColumn = ((LiteralIntBinding) chunksDataListColumn.get(0)).getPayload();		
		
		splitInputData(datasets, chunksByRow, chunksByColumn);
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		LOGGER.info("Chunks finished: " +  elapsedTimeInSec);
	}

	
	public void splitInputData(Map<String, IData> datasets, Integer chunksByRow, Integer chunksByColumn) {
		
		// Take 1st element and extract dimensions
		GridCoverage2D gridCoverage = (GridCoverage2D) datasets.get(getInputStreamableIdentifiers().get(0)).getPayload();
		RenderedImage image = gridCoverage.getRenderedImage();
		
		// Prepare some variables to call the process
		int noOfChunk = 1;
		
		// Create a tile-based image
		int width = image.getWidth();
		int height = image.getHeight();
		int tWidth = (int) Math.floor(width / chunksByRow.doubleValue());
		int tHeight = (int) Math.floor(height / chunksByColumn.doubleValue());
		double minX, minY, w, h;
		
		Map<String, IData> chunkList = new HashMap<String, IData>();
		final CoverageProcessor processor= CoverageProcessor.getInstance();
		final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
		
		for (int col=0; col<chunksByColumn; col++){
			for (int row=0; row<chunksByRow; row++){
				minX = (double) row * tWidth;
				minY = (double) col * tHeight;
				
				if (row==chunksByRow-1){
					w = (double) width - minX;
				}
				else{
					w = tWidth - 1;
				}
				if (col==chunksByColumn-1){
					h = (double) height - minY;
				}
				else{
					h = tHeight - 1; 
				}

				// Get real world bounds for each tile
				MathTransform2D mt = gridCoverage.getGridGeometry().getGridToCRS2D(); 
				Rectangle2D tileBoundsinPixels = new Rectangle2D.Double(minX, minY, w, h);
				Rectangle2D tileBoundsinWorld = null;
				try {
					tileBoundsinWorld = CRS.transform(mt, tileBoundsinPixels, null);
				} catch (TransformException e) {
					e.printStackTrace();
				}
				Coordinate ll = new Coordinate(tileBoundsinWorld.getMinX(), tileBoundsinWorld.getMinY());
				Coordinate ur = new Coordinate(tileBoundsinWorld.getMaxX(), tileBoundsinWorld.getMaxY());
				com.vividsolutions.jts.geom.Envelope internalEnvelope = new com.vividsolutions.jts.geom.Envelope(ll,ur);
				Envelope envelope = new ReferencedEnvelope(internalEnvelope, gridCoverage.getCoordinateReferenceSystem());

				// Iterate through all images and crop them
				for (String id : getInputStreamableIdentifiers()){
					GridCoverage2D coverage = (GridCoverage2D) datasets.get(id).getPayload();
					param.parameter("Source").setValue( coverage );
					param.parameter("Envelope").setValue( envelope );
					GridCoverage2D output = (GridCoverage2D) processor.doOperation(param);
					chunkList.put(id, new GTRasterDataBinding(output));
				}				
				// Process the chunk (TODO Start a new thread here?)
				processChunkList(chunkList, noOfChunk);
				noOfChunk = noOfChunk + 1;
			}
		}
		
		playlistOutputHandler.closePlaylist();
	}

	
	/*
	 * Prepares and processes input data, and updates the playlist afterwards
	 */
	public void processChunkList(Map<String, IData> splittedData, int noOfChunk){
		// Prepare the new input data
		Map<String, List<IData>> inputDataChunk = new HashMap<String, List<IData>>();
		for (String id : getInputStreamableIdentifiers()){
			inputDataChunk.put(id, new ArrayList<IData>(Arrays.asList(splittedData.get(id))));	
		}
		for (String key : inputData.keySet()){
			if (!getInputStreamableIdentifiers().contains(key) &&
					!key.equalsIgnoreCase("chunksByRow") && !key.equalsIgnoreCase("chunksByColumn")){
				inputDataChunk.put(key, inputData.get(key));
			}
		}
		
		try{
			// Process and store the chunk
			if (!playlistOutputHandler.isClosed){ // Is it worth processing incoming chunks? 
				Map<String, IData> result = delegate.run(inputDataChunk);
				if (result.get(getOutputIdentifier()) == null){
					throw new RuntimeException("Error while allocating intermediate results");
				}
				playlistOutputHandler.appendChunk(result.get(getOutputIdentifier()), Integer.toString(noOfChunk));
			} else{
				LOGGER.warn("Output playlist already closed, skipping processing.");
			}
		} catch(RuntimeException e){
			handleException(e);
		}
	}
	
	
	private Map<String, String> getOutputFormat(){
		// Get output parameter's mimeType, encoding and schema
		// I guess this method could be offered in the ExecuteResponseBuilder 
		
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
		if (mimeType == null && schema == null && encoding == null){
			OutputDescriptionType[] outputDescs = description.getProcessOutputs().getOutputArray();
			for (OutputDescriptionType output : outputDescs){
				if (output.getIdentifier().getStringValue().equalsIgnoreCase(getOutputIdentifiers().get(0))){
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
		identifierList.add("chunksByRow");
		identifierList.add("chunksByColumn");
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
		if(id.equalsIgnoreCase("chunksByRow") || id.equalsIgnoreCase("chunksByColumn")){
			return LiteralIntBinding.class;
		}
		else {
			initDelegate();
			return delegate.getInputDataType(id);
		}
	}
	
	@Override
	public Class getOutputDataType(String id) {
		return RasterPlaylistBinding.class;
	}
	
	private synchronized void handleException(RuntimeException e){
		if (!playlistOutputHandler.isClosed){
			ExceptionReport exception = new ExceptionReport(e.getMessage(),	"NoApplicableCode", e);
			playlistOutputHandler.appendException(exception);
			playlistOutputHandler.closePlaylist();
			ExecutionContextFactory.unregisterContext();
		} else {
			LOGGER.warn("Output playlist already closed, skipping appending exception.");			
		}
	}
}
