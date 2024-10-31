package Main;

import DB.ConectorUniDB;
import Tasks.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static Main.Main.*;

public class Uni {
    public Uni() {
        Slot splitterInput, splitterOutput, filterOutput, replicatorOutput1,
                replicatorOutput2, translatorSQLOutput, conectorOutput, correlatorOutput1,
                correlatorOutput2, contextEnricherOutput, translatorEmailGatewayOutput;
        List<Slot> replicatorOutputList, correlatorInputList, correlatorOutputList;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Scanner in = new Scanner(System.in);
            System.out.print("Introduce una secuencia de actas del 1 al 4 separadas por ',' (ej: 4,2,3,1): ");
            String actas = in.next();

            String[] actasList = actas.split(",");

            for (int n = 0; n < actasList.length; n++) {

                Document documento = builder.parse(new File("Actas/acta" + actasList[n] + ".xml"));

                System.out.println("------------------------------------------------");
                System.out.println("| CARGANDO ACTA " + n + " DE LA RUTA [Actas/acta" + n + ".xml] |");
                System.out.println("------------------------------------------------");
                System.out.println("XML de entrada: ");
                printXmlDocument(documento);

                splitterInput = new Slot();
                splitterInput.enqueue(documento);

                splitterOutput = new Slot();
                Splitter splitter = new Splitter(splitterInput, splitterOutput, "//alumno");
                splitter.Split();

                System.out.println("Despues del splitter: ");
                printSlot(splitterOutput);

                filterOutput = new Slot();
                Filter filter = new Filter(splitterOutput, filterOutput, "//alumnos/alumno[calificacion!=\"No presentado\"]");
                filter.Filt();

                System.out.println("Despues del filter: ");
                printSlot(filterOutput);

                replicatorOutputList = new LinkedList<>();
                replicatorOutput1 = new Slot();
                replicatorOutput2 = new Slot();
                replicatorOutputList.add(replicatorOutput1);
                replicatorOutputList.add(replicatorOutput2);

                Replicator replicator = new Replicator(filterOutput, replicatorOutputList);
                replicator.Replicate();

                System.out.println("Despues del replicator: ");
                for (int slot = 0; slot < replicatorOutputList.size(); slot++) {
                    System.out.println("Salida " + slot + " replicator: ");
                    printSlot(replicatorOutputList.get(slot));
                }

                translatorSQLOutput = new Slot();
                Translator translatorSQL = new Translator(replicatorOutputList.get(0), translatorSQLOutput);
                translatorSQL.TranslateSQL("email", "dbo.ALUMNOS", "dni", "", "//alumno/dni");

                System.out.println("Despues del translator: ");
                printSlot(translatorSQLOutput);

                conectorOutput = new Slot();
                ConectorUniDB conector = new ConectorUniDB(translatorSQLOutput, conectorOutput);
                conector.Conect();

                System.out.println("Despues del conector: ");
                printSlot(conectorOutput);

                correlatorInputList = new LinkedList<>();
                correlatorInputList.add(replicatorOutputList.get(1));
                correlatorInputList.add(conectorOutput);

                correlatorOutputList = new LinkedList<>();
                correlatorOutput1 = new Slot();
                correlatorOutput2 = new Slot();
                correlatorOutputList.add(correlatorOutput1);
                correlatorOutputList.add(correlatorOutput2);

                Correlator correlator = new Correlator(correlatorInputList, correlatorOutputList, "replicator_id");
                correlator.Correlate();

                System.out.println("Despues del correlator: ");
                for (int slot = 0; slot < correlatorOutputList.size(); slot++) {
                    System.out.println("Salida " + slot + " correlator: ");
                    printSlot(correlatorOutputList.get(slot));
                }

                contextEnricherOutput = new Slot();
                ContextEnricher contextEnricher = new ContextEnricher(correlatorOutputList.get(0), correlatorOutputList.get(1), contextEnricherOutput, "//alumno[1]");
                contextEnricher.Enrich();

                System.out.println("Despues del context enricher: ");
                printSlot(contextEnricherOutput);

                translatorEmailGatewayOutput = new Slot();
                Translator translatorEmailGateway = new Translator(contextEnricherOutput, translatorEmailGatewayOutput);
                String subject = "Calificaciones ? convocatoria ?.";
                String[] subjectVariables = {"id_asignatura", "convocatoria"};
                String content = "El alumno ? ha obtenido una calificación de ? en la convocatoria ? de ?.";
                String[] contentVariables = {"nombreCompleto", "calificacion", "convocatoria", "id_asignatura"};
                translatorEmailGateway.TranslateEmailGateway("no-reply@uhu.es", "email", subject, subjectVariables, content, contentVariables);

                System.out.println("Despues del translator: ");
                printSlot(translatorEmailGatewayOutput);

                Document outputDoc = translatorEmailGatewayOutput.getQueue().element();
                String pathArchivo = "Actas/Resultados/resultado" + actasList[n] + ".xml";
                xmlDocumentToFile(outputDoc, pathArchivo);
            }
            System.out.println();
            System.out.println("SE HAN GENERADO " + actasList.length + " ARCHIVOS.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}