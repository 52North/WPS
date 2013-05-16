package org.n52.wps.io.data.binding.complex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.n52.wps.io.data.IComplexData;

/**
     *
     * @author Dimitar Misev
     */
    public class ArrayDataBinding implements IComplexData {

        /**
	 * 
	 */
		private List<byte[]> payload;
        
       
        
        public ArrayDataBinding(List<byte[]> payload) {
            this.payload = payload;
        }

        public List<byte[]> getPayload() {
            return payload;
        }

        public Class getSupportedClass() {
            return payload.getClass();
        }

        private synchronized void writeObject(ObjectOutputStream os) throws IOException {
            for (byte[] bs : payload) {
                os.write(bs);
            }
        }

        private synchronized void readObject(ObjectInputStream os) throws IOException, ClassNotFoundException {
            throw new UnsupportedOperationException("Deserialization of 'ArrayDataBinding' data type not implemented yet.");
        }
        
        @Override
        public void dispose() {
        
        }
    }