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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContextEnricher {
    Slot inputSlot;
    Slot contextSlot;
    Slot outputSlot;
    String xPathExpression;

    public ContextEnricher(Slot input, Slot context, Slot output, String xPathExpression) {
        this.inputSlot = input;
        this.contextSlot = context;
        this.outputSlot = output;
        this.xPathExpression = xPathExpression;
    }

    public void Enrich() {
        while (!inputSlot.getQueue().isEmpty() && !contextSlot.getQueue().isEmpty()) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            // Desencolamos el documento del slot de entrada
            Document inputDocument = inputSlot.dequeue();
            Document contextDocument = contextSlot.dequeue();
            try {
                // Consulta xPath para extraer una lista de elementos encontrados.
                NodeList inputNode = (NodeList) xPath.evaluate(xPathExpression, inputDocument, XPathConstants.NODESET);
                NodeList contextNode = (NodeList) xPath.evaluate("/*", contextDocument, XPathConstants.NODESET);

                NodeList inputNodeChilds = inputNode.item(0).getChildNodes();
                NodeList contextNodeChilds = contextNode.item(0).getChildNodes();

                // Builder para cargar el documento
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = dbFactory.newDocumentBuilder();

                for (int contextNodePos = 0; contextNodePos < contextNodeChilds.getLength(); contextNodePos++) {
                    Node node = contextNodeChilds.item(contextNodePos);
                    boolean found = false;
                    int inputNodePos = 0;
                    while (inputNodePos < inputNodeChilds.getLength() && !found) {
                        if (node.getNodeName().equals(inputNodeChilds.item(inputNodePos).getNodeName()))
                            found = true;
                        else
                            inputNodePos++;
                    }

                    if (!found) {
                        Element newElement = inputDocument.createElement(node.getNodeName());
                        newElement.setTextContent(node.getTextContent());
                        inputNode.item(0).appendChild(newElement);
                    }
                }

                outputSlot.enqueue(inputDocument);

            } catch (XPathExpressionException ex) {
                Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
    }
}