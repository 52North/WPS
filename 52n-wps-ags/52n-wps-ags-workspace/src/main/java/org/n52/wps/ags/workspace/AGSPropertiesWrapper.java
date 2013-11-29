package org.n52.wps.ags.workspace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AGSPropertiesWrapper {

	private static final String AGS_PROPERTIES_CLASSNAME = "org.n52.wps.ags.AGSProperties";
	private static AGSPropertiesWrapper _instance;
	private Object properties;
	
	public static synchronized AGSPropertiesWrapper getInstance() {
		if (_instance == null) {
			_instance = new AGSPropertiesWrapper();
		}
		
		return _instance;
	}

	
	private AGSPropertiesWrapper() {
		this.properties = reflectAGSProperties();
	}

	private Object reflectAGSProperties() {
		try {
			Class<?> clazz = Class.forName(AGS_PROPERTIES_CLASSNAME);
			Method method = clazz.getMethod("getInstance", new Class<?>[] {});
			return method.invoke(null, new Object[] {});
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return null;
	}


	public String getDomain() {
		return (String) invokeMethodWithoutParameters("getDomain");
	}


	private Object invokeMethodWithoutParameters(String string) {
		if (this.properties == null) return null;
		
		try {
			Method method = this.properties.getClass().getMethod(string, new Class<?>[] {});
			return method.invoke(this.properties, new Object[] {});
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}


	public String getUser() {
		return (String) invokeMethodWithoutParameters("getUser");
	}


	public String getPass() {
		return (String) invokeMethodWithoutParameters("getPass");
	}


	public String getIP() {
		return (String) invokeMethodWithoutParameters("getIP");
	}


	public String getWorkspaceBase() {
		return (String) invokeMethodWithoutParameters("getWorkspaceBase");
	}

}
