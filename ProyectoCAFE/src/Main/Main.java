package Main;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
         System.out.print("Introduce cafe o uni: ");
         String dsl = in.nextLine();
         if (dsl.equalsIgnoreCase("cafe")) new Cafe();
         else if (dsl.equalsIgnoreCase("uni")) new Uni();
         else System.out.println("Fallo al cargar DSL. Finalizando programa...");
    }

    // Método para darle formato al XML al mostrarlo en la consola
    public static void printXmlDocument(Document document) {
        removeEmptyNodes(document);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            // initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();
            System.out.println(xmlString);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    // Método para mostrar todos los XML que haya en un Slot
    public static void printSlot(Slot slot) {
        for (int i = 0; i < slot.getQueue().size(); i++) {
            Document doc = slot.dequeue();
            printXmlDocument(doc);
            slot.enqueue(doc);
        }
    }

    // Método para guardar XML en un archivo
    public static void xmlDocumentToFile(Document document, String pathArchivo) {
        try {
            removeEmptyNodes(document);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(pathArchivo);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            System.out.println("[" + pathArchivo + " GENERADO CON EXITO]\n");
        } catch (IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private static void removeEmptyNodes(Document document) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();

            XPathExpression xpathExp;
            xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
            NodeList emptyTextNodes;
            emptyTextNodes = (NodeList) xpathExp.evaluate(document, XPathConstants.NODESET);

            // Remove each empty text node from document.
            for (int i = 0; i < emptyTextNodes.getLength(); i++) {
                Node emptyTextNode = emptyTextNodes.item(i);
                emptyTextNode.getParentNode().removeChild(emptyTextNode);
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        // XPath to find empty text nodes.

    }
}
