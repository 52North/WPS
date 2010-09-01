/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.n52.wps.transactional.deploy.bpel.apache;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.ode.utils.Namespaces;

/**
 *
 * @author hoffmannn
 * this class is based on: org.apache.ode.axis2.service.ServiceClientUtil
 * Except that the send method is patched so that the serviceClient gets closed properly.
 */
public class ODEServiceClient{

        public OMElement send(OMElement msg, String url) throws AxisFault {
            return send(msg, url, 180000);
    }

    public OMElement send(OMElement msg, String url, long timeout) throws AxisFault {
        
        Options options = new Options();
        EndpointReference target = new EndpointReference(url);
        options.setTo(target);
        options.setTimeOutInMilliSeconds(timeout);
        OMElement result = null;
        ServiceClient serviceClient = null;

        try {
            serviceClient = new ServiceClient();
            serviceClient.setOptions(options);

            result = serviceClient.sendReceive(msg);
        }catch(AxisFault af){
            af.printStackTrace();
        }finally{
            if (serviceClient != null){
                try{
                    serviceClient.cleanupTransport();
                    
                }catch(Exception e){
                    e.printStackTrace();
                }
                try{
                    serviceClient.cleanup();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public OMElement buildMessage(String operation, String[] params, Object[] values) {
        OMFactory _factory = OMAbstractFactory.getOMFactory();
        OMNamespace pmns = _factory.createOMNamespace(Namespaces.ODE_PMAPI_NS, "pmapi");
        OMElement root = _factory.createOMElement(operation, pmns);
        for (int m = 0; m < params.length; m++) {
            OMElement omelmt = _factory.createOMElement(params[m], null);
            if (values[m] == null)
                omelmt.setText("");
            else if (values[m] instanceof String)
                omelmt.setText((String) values[m]);
            else if (values[m] instanceof QName)
                omelmt.setText((QName) values[m]);
            else if (values[m] instanceof OMElement)
                omelmt.addChild((OMElement) values[m]);
            else if (values[m] instanceof Object[]) {
                Object[] subarr = (Object[]) values[m];
                String elmtName = (String) subarr[0];
                for (int p = 1; p < subarr.length; p++) {
                    OMElement omarrelmt = _factory.createOMElement(elmtName, null);
                    omarrelmt.setText(subarr[p].toString());
                    omelmt.addChild(omarrelmt);
                }
            } else throw new UnsupportedOperationException("Type " + values[m].getClass() + "isn't supported as " +
                    "a parameter type (only String and QName are).");
            root.addChild(omelmt);
        }
        return root;
    }
}
