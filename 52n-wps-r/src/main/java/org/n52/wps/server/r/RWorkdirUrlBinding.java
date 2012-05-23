
package org.n52.wps.server.r;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

public class RWorkdirUrlBinding extends LiteralStringBinding {

    private static Logger LOGGER = Logger.getLogger(RWorkdirUrlBinding.class);

    /**
     * 
     */
    private static final long serialVersionUID = 6106100279013286421L;
    private URL url;

    public RWorkdirUrlBinding(URL url) {
        super(url.toString());
        this.url = url;
    }

    public RWorkdirUrlBinding(String currentWorkdir, String filename) {
        super(currentWorkdir + "/" + filename);
        try {
            this.url = R_Config.getOutputFileURL(currentWorkdir, filename);
        }
        catch (IOException e) {
            LOGGER.error(e);
        }

        LOGGER.info("NEW " + this);
    }

    public Class< ? > getSupportedClass() {
        return URL.class;
    }

    public String getPayload() {
        return this.url.toString();
    }

    @Override
    public String toString() {
        return "RWorkdirUrlBinding [" + this.url.toString() + "]";
    }

}
