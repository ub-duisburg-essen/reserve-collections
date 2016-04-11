package unidue.rc.dao;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.OpacFacadeBook;
import unidue.rc.model.OpacFacadeFind;
import unidue.rc.model.OpacFacadeLibraryData;
import unidue.rc.system.OpacFacadeService;

import java.io.IOException;

/**
 * Created by marcus.koesters on 11.11.15.
 */
public class TestOpacFacadeImpl implements OpacFacadeService {
    private static final Logger LOG = LoggerFactory.getLogger(TestOpacFacadeImpl.class);



    @Override
    public OpacFacadeFind search(String searchString) {
        OpacFacadeFind result = get();
        return result;
    }

    @Override
    public OpacFacadeBook getDetails(String docNumber) {
        String json = "{\"description\":\"50 effektvolle Schauversuche\",\"lang\":\"ger\",\"publisher\":\"Aulis-Verl. Deubner\",\"title\":\"Chemisches Feuerwerk\",\"edition\":\"2. verb. Aufl.\",\"author\":\"Nick, Sabine\",\"year\":\"2005\",\"isbn\":\"3-7614-2374-8\",\"thumbnailURL\":\"http://books.google.de/books/content?id\u003d9r45PQAACAAJ\u0026printsec\u003dfrontcover\u0026img\u003d1\u0026zoom\u003d1\u0026source\u003dgbs_api\",\"publishingLocation\":\"Köln\"}";
                OpacFacadeBook book = parse(OpacFacadeBook.class, json);
        return book;
    }

    @Override
    public OpacFacadeLibraryData getLibraryData(String docNumber) {
       String json =  "{\"totalItemCount\":1,\"borrowableCount\":1,\"heldItems\":0,\"requested\":0,\"expected\":0,\"items\":[{\"signature\":\"UNZ1322(2)\",\"barcode\":\"E00611221\",\"itemStatus\":\"Ausleihbestand\",\"location\":\"Campus Essen\",\"collection\":\"E31\",\"isLoaned\":false,\"isProvided\":false,\"isExpected\":false,\"isRequested\":false,\"isHoldable\":true}]}";
        OpacFacadeLibraryData result = parse(OpacFacadeLibraryData.class, json);
        return result;
    }

    private OpacFacadeFind get() {
        String json = "{\"sessionId\":\"CD7668MCHF5SJCXQ8LJSM2EYY7YXEDR547TKXUJJGBBTJDGYTN\",\"count\":1,\"setNumber\":12043,\"records\":[{\"title\":\"TESTBOOK\"," +
                "\"edition\":\"2. verb. Aufl.\",\"author\":\"Test, Horst\",\"docNumber\":\"000944400\",\"year\":\"2005\",\"isbn\":\"3-3333-3333-3\",\"publishingLocation\":\"Köln\"}]}";
        OpacFacadeFind  result = parse(OpacFacadeFind.class, json);

        return result;
    }

    private <T> T parse(Class<T> clazz, String json) {

        T result = null;
        try {
            // use jackson to parse input
            ObjectMapper mapper = new ObjectMapper();

            result = mapper.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("could not parse input " + e.getMessage());
        }
        return result;
    }
}
