package org.n52.wps.server;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.n52.wps.io.data.binding.complex.DataListDataBinding;
import org.n52.wps.io.data.binding.complex.EODataCacheDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.URLListDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

public abstract class AbstractCancellableAlgorithm  extends AbstractAlgorithm implements ICancelAlgorithm{


	private String instanceId;

	public String getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/** In the case of the AbstractCancellableAlgorith, it was chosen to 
	 * give a default getInput/Output Data Type implementation in order
	 * to facilitate later implementation .
	 * Off course, this should rather be defined in a specialization class */
	
	public Class getInputDataType(String id) {
		InputDescriptionType[] inputs = this.getDescription().getDataInputs()
				.getInputArray();
		for (InputDescriptionType input : inputs) {
			if (input.getIdentifier().getStringValue().equals(id)) {
				if (input.isSetLiteralData()) {
					String datatype = input.getLiteralData().getDataType()
							.getStringValue();
					if (datatype.contains("tring")) {
						return LiteralStringBinding.class;
					}
					if (datatype.contains("ong")) {
						return LiteralLongBinding.class;
					}
					if (datatype.contains("ollean")) {
						return LiteralBooleanBinding.class;
					}
					if (datatype.contains("loat") || datatype.contains("ouble")) {
						return LiteralDoubleBinding.class;
					}
					if (datatype.contains("nt")) {
						return LiteralIntBinding.class;
					}
				}
				if (input.isSetComplexData()) {
					String mimeType = input.getComplexData().getDefault()
							.getFormat().getMimeType();
					if (mimeType.contains("xml") || (mimeType.contains("XML"))) {
						return URLListDataBinding.class;
					} else {
						return URLListDataBinding.class;
					}
				}
			}
		}
		throw new RuntimeException("Could not determie internal inputDataType");
	}

	public Class getOutputDataType(String id) {
		OutputDescriptionType[] outputs = this.getDescription()
				.getProcessOutputs().getOutputArray();

		for (OutputDescriptionType output : outputs) {
			if (output.getIdentifier().getStringValue().equals(id)) {
				
				if (output.isSetLiteralOutput()) {
					// Missing case when dataType is not present
					if (output.getLiteralOutput().getDataType() == null) {
						return LiteralStringBinding.class;
					}
					String datatype = output.getLiteralOutput().getDataType()
							.getStringValue();
					if (datatype.contains("tring")) {
						return LiteralStringBinding.class;
					}
					if (datatype.contains("ollean")) {
						return LiteralBooleanBinding.class;
					}
					if (datatype.contains("ong")) {
						return LiteralLongBinding.class;
					}
					if (datatype.contains("loat") || datatype.contains("ouble")) {
						return LiteralDoubleBinding.class;
					}
					if (datatype.contains("nt")) {
						return LiteralIntBinding.class;
					}
				}
				if (output.isSetComplexOutput()) {
					String mimeType = output.getComplexOutput().getDefault()
							.getFormat().getMimeType();
					if (mimeType.contains("xml") || (mimeType.contains("XML"))) {
						if (output.getComplexOutput().getDefault().getFormat()
								.getSchema().contains("URLList")) {
							return URLListDataBinding.class;

						} 
						else if (output.getComplexOutput().getDefault().getFormat()
								.getSchema().contains("DataList")) {
							return DataListDataBinding.class;

						} 
						else if (output.getComplexOutput().getDefault().getFormat()
								.getSchema().contains("EODataCache")) {
							return EODataCacheDataBinding.class;

						} 
						else {
							return GenericFileDataBinding.class;
						}

					} else {
						return GenericFileDataBinding.class;
					}
				}
			}
		}
		throw new RuntimeException("Could not determie internal inputDataType");
	}

	
}
