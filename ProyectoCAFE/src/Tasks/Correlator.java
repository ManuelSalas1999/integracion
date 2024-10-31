package Tasks;

import Main.Slot;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.List;

public class Correlator {

    List<Slot> inputSlotList;
    List<Slot> outputSlotList;
    String correlationNode;

    public Correlator(List<Slot> inputSlotList, List<Slot> outputSlotList, String correlationNode) {
        this.inputSlotList = inputSlotList;
        this.outputSlotList = outputSlotList;
        this.correlationNode = correlationNode;
    }

    public void Correlate() {
        // Mínimo dos entradas
        try {
            boolean found, foundAllSlots = true;
            int i = 0;
            XPath xPath = XPathFactory.newInstance().newXPath();
            while (i < inputSlotList.get(0).getQueue().size()) {
                Document inputDocument = inputSlotList.get(0).dequeue();
                NodeList replicatorIDNode = (NodeList) xPath.evaluate("//"+correlationNode, inputDocument, XPathConstants.NODESET);
                String replicatorID = replicatorIDNode.item(0).getTextContent();
                int j = 1;
                // Comprobamos si todos los slot de entrada tienen un documento con el replicator id
                while (j < inputSlotList.size()) {
                    int documentPos = 0;
                    found = false;
                    while (documentPos < inputSlotList.get(j).getQueue().size() && !found) {
                        Document comparedDocument = inputSlotList.get(j).dequeue();
                        NodeList comparedIDNode = (NodeList) xPath.evaluate("//"+correlationNode, comparedDocument, XPathConstants.NODESET);
                        String comparedID = comparedIDNode.item(0).getTextContent();
                        if (replicatorID.equals(comparedID))
                            found = true;
                        documentPos++;
                        inputSlotList.get(j).enqueue(comparedDocument);
                    }
                    if (!found) foundAllSlots = false;
                    j++;
                }

                // En el caso de que todos los slot tienen un documento con el replicator_id deseado, los sacamos al mismo tiempo por la salida
                if (foundAllSlots) {
                    // Eliminamos el replicator_id del documento del primer slot
                    NodeList nodeList = (NodeList) xPath.evaluate("/*", inputDocument, XPathConstants.NODESET);
                    NodeList nodeListChild = nodeList.item(0).getChildNodes();
                    for (int nodePos = nodeListChild.getLength()-1; nodePos>=0 ; nodePos--) {
                        Node node = nodeListChild.item(nodePos);
                        if (node.getNodeName().equals("replicator_id")) {
                            node.getParentNode().removeChild(node);
                        }
                    }
                    outputSlotList.get(0).enqueue(inputDocument);
                    // Para el resto de slots de entrada (quitando el primero)
                    for (int k = 1; k < inputSlotList.size(); k++) {
                        found = false;
                        int l = 0;
                        // Buscamos el documento del slot que queremos llevar a la salida (por replicator_id)
                        while (!found && l < inputSlotList.get(k).getQueue().size() ) {
                            Document aux = inputSlotList.get(k).dequeue();
                            NodeList auxIDNode = (NodeList) xPath.evaluate("//" + correlationNode, aux, XPathConstants.NODESET);
                            String auxID = auxIDNode.item(0).getTextContent();

                            // Si hemos encontrado el documento
                            if (replicatorID.equals(auxID)) {
                                // Eliminamos el replicator_id
                                NodeList nodeList2 = (NodeList) xPath.evaluate("/*", aux, XPathConstants.NODESET);
                                NodeList nodeListChild2 = nodeList2.item(0).getChildNodes();
                                for (int nodePos = nodeListChild2.getLength()-1; nodePos>=0 ; nodePos--) {
                                    Node node = nodeListChild2.item(nodePos);
                                    if (node.getNodeName().equals("replicator_id"))
                                        node.getParentNode().removeChild(node);
                                }

                                // Encolamos el documento en la salida
                                outputSlotList.get(k).enqueue(aux);
                                found = true;
                            }
                            // Si no hemos encontrado el elemento
                            else {
                                // Encolamos el documento en la entrada de nuevo
                                inputSlotList.get(k).enqueue(aux);
                                l++;
                            }
                        }
                    }
                }
                else {
                    inputSlotList.get(0).enqueue(inputDocument);
                    i++;
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

    }
}
