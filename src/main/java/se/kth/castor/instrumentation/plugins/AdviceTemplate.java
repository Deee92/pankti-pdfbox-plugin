package se.kth.castor.instrumentation.plugins;

import com.thoughtworks.xstream.XStream;
import se.kth.castor.instrumentation.converters.CleanerImplConverter;
import se.kth.castor.instrumentation.converters.FileCleanableConverter;
import se.kth.castor.instrumentation.converters.InflaterConverter;

public interface AdviceTemplate {
    XStream xStream = new XStream();

    static void setUpXStream() {
        xStream.registerConverter(new FileCleanableConverter());
        xStream.registerConverter(new InflaterConverter());
        xStream.registerConverter(new CleanerImplConverter());
    }

    static String[] setUpFiles(int count) {
        String receivingObjectFilePath = "/home/user/pdfbox-object-data/" + count + "-receiving.xml";
        String paramObjectsFilePath = "/home/user/pdfbox-object-data/" + count + "-params.xml";
        String returnedObjectFilePath = "/home/user/pdfbox-object-data/" + count + "-returned.xml";
        return new String[]{receivingObjectFilePath, paramObjectsFilePath, returnedObjectFilePath};
    }

    static String[] setUpFiles(String path) {
        String receivingObjectFilePath = "/home/user/pdfbox-object-data/" + path + "-receiving.xml";
        String paramObjectsFilePath = "/home/user/pdfbox-object-data/" + path + "-params.xml";
        String returnedObjectFilePath = "/home/user/pdfbox-object-data/" + path + "-returned.xml";
        return new String[]{receivingObjectFilePath, paramObjectsFilePath, returnedObjectFilePath};
    }
}
