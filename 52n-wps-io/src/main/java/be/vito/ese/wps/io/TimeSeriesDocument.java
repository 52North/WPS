package be.vito.ese.wps.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TimeSeriesDocument{

	private String coverageId;
	private List<Double> averages;
	private List<Date> dates;
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	public TimeSeriesDocument(String coverageId,
			List<Double> averages,
			List<Date> dates){
		
		this.coverageId = coverageId;
		this.averages = averages;
		this.dates = dates;
		
		if(averages.size() != dates.size()){
			throw new IllegalArgumentException("Date and average sizes must be equal");
		}
		
	}

	public Node getDomNode(){
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();		
		factory.setNamespaceAware(true);
		
		DocumentBuilder documentBuilder = null;
		Document document = null;
		Element rootElement = null;

		try{
		
			documentBuilder = factory.newDocumentBuilder();			
			document = documentBuilder.newDocument();

			rootElement = document.createElementNS("http://vito.be/ese/timeseries", "ts:timeseries");

			rootElement.setAttribute("coverageId", coverageId);
			document.appendChild(rootElement);
			
			int i = 0;
			for(Double average : averages){
				
				Element entryElement = document.createElementNS("http://vito.be/ese/timeseries", "ts:entry");
				rootElement.appendChild(entryElement);

				Element averageElement = document.createElementNS("http://vito.be/ese/timeseries", "ts:average");
				averageElement.appendChild(document.createTextNode(average.toString()));
				entryElement.appendChild(averageElement);
				
				Element dateElement = document.createElementNS("http://vito.be/ese/timeseries", "ts:date");
				dateElement.appendChild(document.createTextNode(DATE_FORMAT.format(dates.get(i))));
				entryElement.appendChild(dateElement);
				
				i++;
				
			}
		
		}
		catch(ParserConfigurationException ex){
			ex.printStackTrace();
		}
		
		return rootElement;
	}
	
}
