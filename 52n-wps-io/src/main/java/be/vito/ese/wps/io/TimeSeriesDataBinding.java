package be.vito.ese.wps.io;

import java.util.Date;
import java.util.List;

import org.n52.wps.io.data.IComplexData;

public class TimeSeriesDataBinding implements IComplexData{

	private static final long serialVersionUID = 6945469444347550464L;

	private String coverageId;
	private List<Double> averages;
	private List<Date> dates;
	
	public TimeSeriesDataBinding(String coverageId,
			List<Double> averages,
			List<Date> dates){
		
		this.coverageId = coverageId;
		this.averages = averages;
		this.dates = dates;
		
	}
	
	@Override
	public TimeSeriesDocument getPayload(){
		return new TimeSeriesDocument(coverageId,
				averages, dates);
	}

	@Override
	public Class<TimeSeriesDocument> getSupportedClass(){
		return TimeSeriesDocument.class;
	}

}