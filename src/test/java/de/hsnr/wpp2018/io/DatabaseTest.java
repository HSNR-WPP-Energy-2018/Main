package de.hsnr.wpp2018.io;

import de.hsnr.wpp2018.algorithms.AlgorithmTest;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.database.*;
import de.hsnr.wpp2018.io.database.Parser;
import de.hsnr.wpp2018.io.database.Writer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Simple database test
 */
public class DatabaseTest {

    /**
     * Parser test
     *
     * @throws ParserException on parse error
     */
    @Test
    public void parserString() throws ParserException {
        String data = "900\n" +
                "string|test\n" +
                "MONTH:7:THURSDAY-0:30:0=0.09975;17:15:0=0.095;2:30:0=0.06115625;19:15:0=0.06175;4:30:0=0.059375000000000004;21:15:0=0.144875;6:30:0=0.0866875;23:15:0=0.078375;8:30:0=0.0665;10:30:0=0.09975;12:30:0=0.06590625;14:30:0=0.0665;16:30:0=0.09737499999999999;1:45:0=0.07184375;18:30:0=0.0676875;3:45:0=0.07065625;20:30:0=0.08015625;5:45:0=0.0534375;22:30:0=0.097375;7:45:0=0.06471874999999999;9:45:0=0.1033125;11:45:0=0.07065625;13:45:0=0.06828125;1:0:0=0.0653125;15:45:0=0.1318125;3:0:0=0.09796875;17:45:0=0.064125;5:0:0=0.09440625;19:45:0=0.1270625;7:0:0=0.130625;21:45:0=0.14487499999999998;9:0:0=0.07659374999999999;23:45:0=0.06709375000000001;11:0:0=0.0724375;13:0:0=0.076;15:0:0=0.07896875;0:15:0=0.14546875000000004;17:0:0=0.1531875;2:15:0=0.059375;19:0:0=0.083125;4:15:0=0.0558125;21:0:0=0.11875;6:15:0=0.0914375;23:0:0=0.09796875000000001;8:15:0=0.06887499999999999;10:15:0=0.13775;12:15:0=0.06887499999999999;14:15:0=0.064125;16:15:0=0.09440625;1:30:0=0.197125;18:15:0=0.0914375;3:30:0=0.07421875;20:15:0=0.06353125;5:30:0=0.0558125;22:15:0=0.1460625;7:30:0=0.055218750000000004;9:30:0=0.12409374999999999;11:30:0=0.09084375;13:30:0=0.06828125;15:30:0=0.15378125;0:45:0=0.0890625;17:30:0=0.06471874999999999;2:45:0=0.0665;19:30:0=0.13715625;4:45:0=0.1080625;21:30:0=0.20009375000000001;6:45:0=0.07956250000000001;23:30:0=0.0914375;8:45:0=0.07303125;10:45:0=0.08490625;12:45:0=0.06353125;0:0:0=0.0605625;14:45:0=0.07659375;2:0:0=0.05878125;16:45:0=0.13953125;4:0:0=0.0558125;18:45:0=0.06353125000000001;6:0:0=0.054625;20:45:0=0.26303125;8:0:0=0.08075;22:45:0=0.08609375;10:0:0=0.08134375;12:0:0=0.064125;14:0:0=0.06887499999999999;16:0:0=0.1318125;1:15:0=0.0748125;18:0:0=0.097375;3:15:0=0.08075;20:0:0=0.0665;5:15:0=0.05284375;22:0:0=0.16565625;7:15:0=0.10450000000000001;9:15:0=0.10212500000000001;11:15:0=0.07718749999999999;13:15:0=0.06887499999999999;15:15:0=0.14190625\n";
        Element element = Parser.parse(data);
        System.out.println("Parsed element: " + element);
        Assert.assertEquals(element.getDescriptors().size(), 1);
        Assert.assertEquals(element.getValues().size(), 1);
    }

    /**
     * Writer tester
     *
     * @throws IOException on IO error
     * @throws ParserException on parse error
     */
    @Test
    public void writer() throws IOException, ParserException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        TreeMap<LocalDateTime, Consumption> data = importer.getData();

        Database database = new Database();
        ArrayList<Descriptor> descriptors = new ArrayList<>();
        descriptors.add(new StringDescriptor("test"));
        database.addElement(new Element(AlgorithmTest.INTERVAL, descriptors, ElementKey.Type.MONTH, data));

        Writer.write(database, "out");
    }
}