package org.n52.wps.algorithm.descriptor;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public abstract class InputDescriptor<T extends Class<? extends IData>> extends BoundDescriptor<T> {

	private final BigInteger minOccurs;
	private final BigInteger maxOccurs;

	protected InputDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
		this.minOccurs = builder.minOccurs;
		this.maxOccurs = builder.maxOccurs;
        Preconditions.checkState(maxOccurs.longValue() >= minOccurs.longValue(), "maxOccurs must be >= minOccurs");
    }

    public BigInteger getMinOccurs() {
        return minOccurs;
    }

    public BigInteger getMaxOccurs() {
        return maxOccurs;
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IData>> extends BoundDescriptor.Builder<B,T>{

        private BigInteger minOccurs = BigInteger.ONE;
        private BigInteger maxOccurs = BigInteger.ONE;

        protected Builder(String identifier, T binding) {
            super(identifier, binding);
        }

        public B minOccurs(int minOccurs) {
            return minOccurs(BigInteger.valueOf(minOccurs));
        }

        public B minOccurs(BigInteger minOccurs) {
            Preconditions.checkArgument(minOccurs.longValue() >= 0, "minOccurs must be >= 0");
            this.minOccurs = minOccurs;
            return self();
        }

        public B maxOccurs(int maxOccurs) {
            return maxOccurs(BigInteger.valueOf(maxOccurs));
        }

        public B maxOccurs(BigInteger maxOccurs) {
            Preconditions.checkArgument(maxOccurs.longValue() > 0, "maxOccurs must be > 0");
            this.maxOccurs = maxOccurs;
            return self();
        }

        public <E extends Enum<E>> B maxOccurs(Class<E> enumType) {
            return maxOccurs(enumType.getEnumConstants().length);
        }

        public abstract InputDescriptor<T> build();
    }
}
