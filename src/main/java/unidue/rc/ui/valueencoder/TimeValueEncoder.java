/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.ui.valueencoder;


import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeValueEncoder implements ValueEncoder<Calendar>, ValueEncoderFactory<Calendar> {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL);

    private static final Logger LOG = LoggerFactory.getLogger(TimeValueEncoder.class);

    @Override
    public ValueEncoder<Calendar> create(Class<Calendar> type) {
        return this;
    }

    @Override
    public String toClient(Calendar value) {
        return DATE_FORMAT.format(value.getTime());
    }

    @Override
    public Calendar toValue(String clientValue) {
        Calendar result = new GregorianCalendar();
        try {
            result.setTime(DATE_FORMAT.parse(clientValue));
        } catch (ParseException e) {
            LOG.error("could not parse " + clientValue, e);
        }
        return result;
    }

}
