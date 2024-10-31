package Tasks;

import Main.Slot;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Filter {
    Slot inputSlot;
    Slot outputSlot;
    String xPathExpression;

    public Filter(Slot input, Slot output, String xPathExpression) {
        this.inputSlot = input;
        this.outputSlot = output;
        this.xPathExpression = xPathExpression;
    }

    public void Filt() {
        // Desencolamos el documento del slot de entrada
        while (!inputSlot.getQueue().isEmpty()) {
            Document inputDocument = inputSlot.dequeue();
            try {
                // Consulta xPath para extraer una lista de elementos encontrados.
                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList splitNodes = (NodeList) xPath.evaluate(xPathExpression, inputDocument, XPathConstants.NODESET);

                // Si hemos encontrado el elemento
                if (splitNodes.getLength() > 0) {
                    outputSlot.enqueue(inputDocument);
                }
            } catch (XPathExpressionException ex) {
                Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
