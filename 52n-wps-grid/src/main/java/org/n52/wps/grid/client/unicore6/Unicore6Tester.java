package org.n52.wps.grid.client.unicore6;

import java.util.List;

import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.TargetSystemFactory;
import de.fzj.unicore.uas.client.RegistryClient;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.security.DSigOutHandler;
import de.fzj.unicore.uas.security.IUASSecurityProperties;
import de.fzj.unicore.uas.security.TDOutHandler;
import de.fzj.unicore.uas.security.UASSecurityProperties;

public class Unicore6Tester {

	public static void main(String[] args) throws Exception {

		System.setProperty("javax.net.debug","ssl,handshake"); 
		
		String pRegistry = "https://localhost:8080/REGISTRY/services/Registry?res=default_registry";
		
		String pKeystore = "/home/bastian/.gpe4unicore/keystore.jks";
		String pPassword = "unicore";
		String pType = "jks";
		String pAlias = "bb";
		
		// CREATE SECURITY PROPERTIES
		UASSecurityProperties pSecurityProperties = new UASSecurityProperties();

		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_KEYSTORE, pKeystore);
		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_KEYPASS, pPassword);
		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_KEYTYPE, pType);
		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_KEYALIAS, pAlias);
		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_TRUSTSTORE, pKeystore);
		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_TRUSTPASS, pPassword);
		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_TRUSTTYPE, pType);

		pSecurityProperties
				.setProperty(IUASSecurityProperties.WSRF_SSL, "true");
		pSecurityProperties.setProperty(
				IUASSecurityProperties.WSRF_SSL_CLIENTAUTH, "true");

		String outHandlers = DSigOutHandler.class.getName() + " "
				+ TDOutHandler.class.getName();
		pSecurityProperties.setProperty(
				IUASSecurityProperties.UAS_OUTHANDLER_NAME, outHandlers);

		pSecurityProperties.setSignMessage(true);
		pSecurityProperties.setAddTrustDelegation(true);

		// CONNECT TO REGISTRY
		EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
		registryEpr.addNewAddress().setStringValue(pRegistry);
		RegistryClient registry = new RegistryClient(registryEpr.getAddress().getStringValue(), registryEpr, pSecurityProperties);
		
		// GET TARGET SYSTEM FACTORY
		List<EndpointReferenceType> tsfList = registry.listServices(TargetSystemFactory.TSF_PORT);
		for (EndpointReferenceType tsfEpr : tsfList)
		{
			System.out.println("Found Target System Factory (TSF) at '" + tsfEpr.getAddress().getStringValue() + "'.");
		}
		
	}
	
}
