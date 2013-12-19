package org.n52.wps.ags.workspace.test;

import org.junit.Test;
import org.n52.wps.ags.workspace.AGSPropertiesWrapper;

public class PropertiesReflectionTest {

	@Test
	public void testPropertiesReflectionInstantiation() {
		AGSPropertiesWrapper wrapper = AGSPropertiesWrapper.getInstance();
		wrapper.getDomain();
		wrapper.getIP();
		wrapper.getPass();
		wrapper.getUser();
		wrapper.getWorkspaceBase();
	}
	
}
