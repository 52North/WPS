package org.n52.wps.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;

public abstract class AbstractObservableAlgorithm implements IAlgorithm, ISubject{

	protected ProcessDescriptionType description;
	protected final String wkName;
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractAlgorithm.class);
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractObservableAlgorithm() {
		this.description = initializeDescription();
		this.wkName = "";
	}
	
	public AbstractObservableAlgorithm(ProcessDescriptionType description) {
		this.description = description;
		this.wkName = "";
	}
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractObservableAlgorithm(String wellKnownName) {
		this.wkName = wellKnownName; // Has to be initialized before the description. 
		this.description = initializeDescription();
	}
	
	/** 
	 * This method should be overwritten, in case you want to have a way of initializing.
	 * 
	 * In detail it looks for a xml descfile, which is located in the same directory as the implementing class and has the same
	 * name as the class, but with the extension XML.
	 * @return
	 */
	protected ProcessDescriptionType initializeDescription() {
		String className = this.getClass().getName().replace(".", "/");
		InputStream xmlDesc = this.getClass().getResourceAsStream("/" + className + ".xml");
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription does not contain correct any description");
				return null;
			}
			
			// Checking that the process name (full class name or well-known name) matches the identifier.
			if(!doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().getStringValue().equals(this.getClass().getName()) &&
					!doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().getStringValue().equals(this.getWellKnownName())) {
				doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().setStringValue(this.getClass().getName());
				LOGGER.warn("Identifier was not correct, was changed now temporary for server use to " + this.getClass().getName() + ". Please change it later in the description!");
			}
			
			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		return null;
	}
	
	public ProcessDescriptionType getDescription()  {
		return description;
	}

	public boolean processDescriptionIsValid() {
		return description.validate();
	}
	
	public String getWellKnownName() {
		return this.wkName;
	}
	
	private List observers = new ArrayList();

	private Object state = null;

	public Object getState() {
	  return state;
	}

	public void update(Object state) {
	   this.state = state;
	   notifyObservers();
	}

	 public void addObserver(IObserver o) {
	   observers.add(o);
	 }

	 public void removeObserver(IObserver o) {
	   observers.remove(o);
	 }

	 public void notifyObservers() {
	   Iterator i = observers.iterator();
	   while (i.hasNext()) {
	     IObserver o = (IObserver) i.next();
	     o.update(this);
	   }
	 }
}
