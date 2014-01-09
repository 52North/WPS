/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.algorithm.descriptor;

import com.google.common.base.Preconditions;

/**
 *
 * @author tkunicki
 */
public abstract class BoundDescriptor<T extends Class<?>> extends Descriptor {

    private final T binding;

	BoundDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
		this.binding = builder.binding;
    }

    public T getBinding() {
        return binding;
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<?>> extends Descriptor.Builder<B> {

        private final T binding;

        protected Builder(String identifier, T binding) {
            super(identifier);
            Preconditions.checkArgument(binding != null, "binding may not be null");
            this.binding = binding;
        }
    }
}
