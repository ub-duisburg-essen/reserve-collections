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
