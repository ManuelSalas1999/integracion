package Tasks;

import Main.Slot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.LinkedList;
import java.util.Queue;

public class Aggregator {

    Slot inputSlot;
    Slot outputSlot;
    String xPathExpression;

    public Aggregator(Slot input, Slot output, String xPathExpression) {
        this.inputSlot = input;
        this.outputSlot = output;
        this.xPathExpression = xPathExpression;
    }

    public void Aggregate() {
        try {
            Document inputDocument = null;
            XPath xPath = XPathFactory.newInstance().newXPath();
            Queue<NodeList> splitNodesQueue = new LinkedList<>();
            while (!inputSlot.getQueue().isEmpty()) {
                inputDocument = inputSlot.dequeue();
                splitNodesQueue.add((NodeList) xPath.evaluate(xPathExpression, inputDocument, XPathConstants.NODESET));
            }
            NodeList root = (NodeList) xPath.evaluate("/*", inputDocument, XPathConstants.NODESET);

            // Builder para cargar el documento
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();

            // Creamos un documento
            Document aggregatedDocument = builder.newDocument();

            // Añadimos el nodo raíz
            Element rootElement = aggregatedDocument.createElement(root.item(0).getNodeName());
            aggregatedDocument.appendChild(rootElement);

            // Creamos una NodeList con los hijos de la raíz
            NodeList rootChild = root.item(0).getChildNodes();

            // Para cada hijo de la raíz comprobamos si es el padre de los elementos que queremos juntar
            int i = 0;
            while (i < rootChild.getLength() && !splitNodesQueue.isEmpty()) {
                // Si no lo es lo añadimos para conservar el resto de la estructura
                if (!rootChild.item(i).getNodeName().equals(splitNodesQueue.element().item(0).getParentNode().getNodeName())) {
                    Node node = aggregatedDocument.importNode(rootChild.item(i), true);
                    rootElement.appendChild(node);
                }
                // Si lo es añadimos el elemento y debajo el resto de elementos de la cola de NodeList
                else {
                    Element parentElement = aggregatedDocument.createElement(rootChild.item(i).getNodeName());
                    rootElement.appendChild(parentElement);
                    while (!splitNodesQueue.isEmpty()) {
                        Node node = aggregatedDocument.importNode(splitNodesQueue.poll().item(0), true);
                        parentElement.appendChild(node);
                    }
                }
                i++;
            }

            // Encolamos el documento resultante en el slot de salida
            outputSlot.enqueue(aggregatedDocument);
        } catch (XPathExpressionException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}