/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

public class LiteralDateTimeBinding extends AbstractLiteralDataBinding {
    /**
     *
     */
    private static final long serialVersionUID = -4336688658437832346L;
    private transient Date date;

    public LiteralDateTimeBinding(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public Time getTime() {
        return new Time(date.getTime());
    }

    public Timestamp getTimestamp() {
        return new Timestamp(date.getTime());
    }

    @Override
    public Date getPayload() {
        return date;
    }

    @Override
    public Class<Date> getSupportedClass() {
        return Date.class;
    }

    private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
    {
        oos.writeObject(new Long(date.getTime()).toString());
    }

    private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
    {
        date = new Date( ((Long) oos.readObject()).longValue() );
    }

}
