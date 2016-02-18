package unidue.rc.io;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * A <code>DayOfBirthConverter</code> is able to convert a xml node with the form {@code<node
 * format="someformat">somevalue</node>}. It can be used as a {@linkplain Converter} inside simplexml.
 *
 * @author Nils Verheyen
 * @see <a href="http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php#converters">Converters in
 * simplexml</a>
 */
public class DayOfBirthConverter implements Converter<DateTime> {

    private static final String DATETIME_OUTPUT_FORMAT = "dd.MM.yyyy HH:mm:ss";

    private static final String FORMAT_ATTRIBUTE_NAME = "format";

    @Override
    public DateTime read(InputNode node) throws Exception {
        InputNode attributeNode = node.getAttribute(FORMAT_ATTRIBUTE_NAME);
        String formatValue = attributeNode != null
                ? attributeNode.getValue()
                : null;
        String value = node.getValue();

        DateTime result = null;
        if (formatValue != null && value != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(formatValue);
            result = formatter.parseDateTime(value);
        }
        return result;
    }

    /**
     * Write target {@linkplain DateTime} to target node with the format of {@linkplain
     * DayOfBirthConverter#DATETIME_OUTPUT_FORMAT} and attribute name {@linkplain
     * DayOfBirthConverter#FORMAT_ATTRIBUTE_NAME}
     *
     * @param node  output value
     * @param value input value
     * @throws IllegalArgumentException thrown if no formatter for target format could be created.
     * @throws Exception                thrown if the value could not be added to the node
     */
    @Override
    public void write(OutputNode node, DateTime value) throws Exception {
        node.setAttribute(FORMAT_ATTRIBUTE_NAME, DATETIME_OUTPUT_FORMAT);
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATETIME_OUTPUT_FORMAT);
        node.setValue(formatter.print(value));
    }
}
