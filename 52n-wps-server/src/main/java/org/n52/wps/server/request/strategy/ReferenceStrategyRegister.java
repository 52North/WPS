package org.n52.wps.server.request.strategy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.n52.wps.server.ExceptionReport;

import net.opengis.wps.x100.InputType;

public class ReferenceStrategyRegister {

	protected List<IReferenceStrategy> registeredStrategies;
	private static ReferenceStrategyRegister instance;
	
	
	public synchronized static ReferenceStrategyRegister getInstance(){
		if(instance==null){
			instance = new ReferenceStrategyRegister();
		}
		return instance;
	}
	
	private ReferenceStrategyRegister(){
		registeredStrategies = new ArrayList<IReferenceStrategy>();
		registeredStrategies.add(new WCS111XMLEmbeddedBase64OutputReferenceStrategy());
	}
	
	protected void registerStrategy(IReferenceStrategy strategy){
		registeredStrategies.add(strategy);
	}
	
	public ReferenceInputStream resolveReference(InputType input) throws ExceptionReport{
		IReferenceStrategy foundStrategy = new DefaultReferenceStrategy();
		for(IReferenceStrategy strategy : registeredStrategies){
			if(strategy.isApplicable(input)){
				foundStrategy = strategy;
				break;
			}
		}
		return foundStrategy.fetchData(input);
	}
}
