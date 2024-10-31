package Main;

import DB.ConectorCafeDB;
import Tasks.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static Main.Main.*;

public class Cafe {
    public Cafe() {
        Slot splitterInput, splitterOutput, replicatorOutput1, replicatorOutput2,
                translatorOutput, conectorOutput, correlatorOutput1, correlatorOutput2,
                contextEnricherOutput, mergerOutput, agreggatorOutput;
        List<Slot> distributorOutputList, replicatorOutputList, correlatorInputList, correlatorOutputList, mergerInputList;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Scanner in = new Scanner(System.in);
            System.out.print("Introduce una secuencia de comandas del 1 al 9 separadas por ',' (ej: 4,7,2,1): ");
            String orders = in.next();

            String[] ordersList = orders.split(",");

            for (int n = 0; n < ordersList.length; n++) {

                Document documento = builder.parse(new File("Orders/order" + ordersList[n] + ".xml"));
                System.out.println("------------------------------------------------");
                System.out.println("| CARGANDO ORDER " + n + " DE LA RUTA [Orders/order" + n + ".xml] |");
                System.out.println("------------------------------------------------");
                System.out.println("XML de entrada: ");
                printXmlDocument(documento);

                splitterInput = new Slot();
                splitterInput.enqueue(documento);
                splitterOutput = new Slot();
                Splitter splitter = new Splitter(splitterInput, splitterOutput, "//drink");
                splitter.Split();

                System.out.println("Despues del splitter: ");
                printSlot(splitterOutput);

                distributorOutputList = new LinkedList<>();
                Distributor distributor = new Distributor(splitterOutput, distributorOutputList, "//type");
                List<String> types = distributor.Distribute();

                mergerInputList = new LinkedList<>();
                for (int i = 0; i < distributorOutputList.size(); i++) {
                    System.out.println("Salida" + i + " del distributor: ");
                    printSlot(distributorOutputList.get(i));

                    // Replicator
                    replicatorOutputList = new LinkedList<>();
                    replicatorOutput1 = new Slot();
                    replicatorOutput2 = new Slot();
                    replicatorOutputList.add(replicatorOutput1);
                    replicatorOutputList.add(replicatorOutput2);
                    Replicator replicator = new Replicator(distributorOutputList.get(i), replicatorOutputList);
                    replicator.Replicate();

                    System.out.println("Despues del replicator: ");
                    for (int slot = 0; slot < replicatorOutputList.size(); slot++) {
                        System.out.println("Salida " + slot + "replicator: ");
                        printSlot(replicatorOutputList.get(slot));
                    }

                    // Translator
                    translatorOutput = new Slot();
                    Translator translator = new Translator(replicatorOutputList.get(0), translatorOutput);
                    if (types.get(i).equals("hot"))
                        translator.TranslateSQL("*", "dbo.BEBIDAS_CALIENTES", "Nombre", "and stock>0", "//drink/name");
                    else
                        translator.TranslateSQL("*", "dbo.BEBIDAS_FRIAS", "Nombre", "and stock>0", "//drink/name");

                    System.out.println("Despues del translator: ");
                    printSlot(translatorOutput);

                    // Conector
                    conectorOutput = new Slot();
                    ConectorCafeDB conector = new ConectorCafeDB(translatorOutput, conectorOutput);
                    conector.Conect();

                    System.out.println("Despues del conector: ");
                    printSlot(conectorOutput);

                    // Entrada correlator
                    correlatorInputList = new LinkedList<>();
                    correlatorInputList.add(replicatorOutputList.get(1));
                    correlatorInputList.add(conectorOutput);
                    // Salida correlator
                    correlatorOutputList = new LinkedList<>();
                    correlatorOutput1 = new Slot();
                    correlatorOutput2 = new Slot();
                    correlatorOutputList.add(correlatorOutput1);
                    correlatorOutputList.add(correlatorOutput2);
                    // Correlator
                    Correlator correlator = new Correlator(correlatorInputList, correlatorOutputList, "replicator_id");
                    correlator.Correlate();

                    System.out.println("Despues del correlator: ");
                    for (int slot = 0; slot < correlatorOutputList.size(); slot++) {
                        System.out.println("Salida " + slot + "correlator: ");
                        printSlot(correlatorOutputList.get(slot));
                    }

                    // Context enricher
                    contextEnricherOutput = new Slot();
                    ContextEnricher contextEnricher = new ContextEnricher(correlatorOutputList.get(0), correlatorOutputList.get(1), contextEnricherOutput, "//drink[1]");
                    contextEnricher.Enrich();
                    mergerInputList.add(contextEnricherOutput);

                    System.out.println("Despues del context enricher: ");
                    printSlot(contextEnricherOutput);
                }

                // Merger
                mergerOutput = new Slot();
                Merger merger = new Merger(mergerInputList, mergerOutput);
                merger.Merge();

                System.out.println("Despues del merger: ");
                printSlot(mergerOutput);

                // Aggregator
                agreggatorOutput = new Slot();
                Aggregator aggregator = new Aggregator(mergerOutput, agreggatorOutput, "//drink");
                aggregator.Aggregate();

                System.out.println("Despues del aggregator: ");
                printSlot(agreggatorOutput);

                Document outputDoc = agreggatorOutput.getQueue().element();
                String pathArchivo = "Orders/Entregas/entrega"+ordersList[n]+".xml";
                xmlDocumentToFile(outputDoc, pathArchivo);
            }
            System.out.println();
            System.out.println("SE HAN GENERADO " + ordersList.length + " ARCHIVOS.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
