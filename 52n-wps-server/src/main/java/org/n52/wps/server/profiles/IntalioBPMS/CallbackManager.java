package org.n52.wps.server.profiles.IntalioBPMS;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;

import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.axis2.util.XMLUtils;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;

public class CallbackManager implements AxisCallback {

	private String id = "0";
	public boolean completed = false;
	private ApacheOdeProcessManager pm;

	public CallbackManager(ApacheOdeProcessManager pm) {
		this.pm = pm;
	}

	@Override
	public void onComplete() {
		this.pm.notifyRequestManager();
	}

	@Override
	public void onError(Exception arg0) {
		// TODO
		System.out.println("Unhandled error");
	}

	@Override
	public void onFault(MessageContext arg0) {
		// TODO Auto-generated method stub
		System.out.println("Thread #" + this.id + " fault");
	}

	@Override
	public void onMessage(MessageContext message) {
		Document envelope = SAAJUtil.getDocumentFromSOAPEnvelope(message
				.getEnvelope());
		ExecuteResponseDocument result = null;
		try {
			result = ExecuteResponseDocument.Factory.parse(envelope
					.getFirstChild().getChildNodes().item(1).getFirstChild());
		} catch (XmlException e) {
			e.printStackTrace();
		}
		if (result instanceof ExecuteResponseDocument) {
			this.pm.setExecuteResponse((ExecuteResponseDocument) result);
		}
	}

}
