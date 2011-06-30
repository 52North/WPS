package org.n52.wps.server.repository;


import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.profiles.IProcessManager;
import org.w3c.dom.Node;

/**
 * The repository manager is an utility class use to facilitate the
 * load / search of a repository or load of a process manager class.
 * TODO this should be merged with the RepositoryManager (which is only focused
 * on (non transactional) java algorithm (local repository, uploaded repository)
 *
 */
public class TransactionalRepositoryManager {

	public static Repository getMatchingTransactionalRepositoryClassName(String schema){
		WPSConfig config = WPSConfig.getInstance();
		Repository[] repositories = config.getRegisterdAlgorithmRepositories();
		
		for(Repository repository : repositories){
			Property[] properties = repository.getPropertyArray();
			for(Property property : properties){
				if(property.getName().equals("SupportedFormat")){
					if(property.getStringValue().equals(schema)){
						return repository;
					}
					
				}
				
			}
		}
		return null;
	}
	

	public static ITransactionalAlgorithmRepository getMatchingTransactionalRepository(String schema){
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		String className = repository.getClassName();
		IAlgorithmRepository algorithmRepository = RepositoryManager.getInstance().getRepositoryForClassName(className);
		if(algorithmRepository!=null){
			if(algorithmRepository instanceof ITransactionalAlgorithmRepository){
				return (ITransactionalAlgorithmRepository) algorithmRepository;
			}
		}
		return null;
	}
	
	public static String getDeploymentProfileForSchema(String schema){
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		Property[] properties = repository.getPropertyArray();
		for(Property property : properties){
			if(property.getName().equals("DeploymentProfile")){ 
				return property.getStringValue();
			}
		}
		return null;
	}
	
	public static IProcessManager getProcessManagerForSchema(String schema) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		Property[] properties = repository.getPropertyArray();
		for(Property property : properties){
			System.out.println("Property : "+property.getName()+"-"+property.getStringValue());
			if(property.getName().equals("ProcessManager")){
				//return (IDeployManager) Class.forName(property.getStringValue()).newInstance();
                            try{
                                Class depManager = Class.forName(property.getStringValue());
                                Constructor con = depManager.getConstructor(ITransactionalAlgorithmRepository.class);
                                Object o = con.newInstance(new Object[]{getMatchingTransactionalRepository(schema)});
                                return (IProcessManager)o;
                            }catch(ClassNotFoundException e){
                                e.printStackTrace();
                            }catch(NoSuchMethodException e){
                                e.printStackTrace();
                            }catch(SecurityException e){
                                e.printStackTrace();
                            }catch(IllegalAccessException e){
                                e.printStackTrace();
                            }catch(InvocationTargetException e){
                                e.printStackTrace();
                            }catch(Exception e){
                                e.printStackTrace();
                            }

			}
		}
		return null;
	}
	
	public static void writeXmlFile(Node node, String filename) {
	    try {
	        // Prepare the DOM document for writing
	        Source source = new DOMSource(node);

	        // Prepare the output file
	        File file = new File(filename);
	        Result result = new StreamResult(file);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.transform(source, result);
	    } catch (TransformerConfigurationException e) {
	    } catch (TransformerException e) {
	    }
	}

}
