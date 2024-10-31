package Tasks;

import Main.Slot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Translator {

    Slot inputSlot;
    Slot outputSlot;

    public Translator(Slot inputSlot, Slot outputSlot) {
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
    }

    public void TranslateSQL(String selection, String table, String variable, String otherConditions, String variableNode) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        while (!inputSlot.getQueue().isEmpty()) {
            Document inputDocument = inputSlot.dequeue();
            try {
                // Consulta xPath para extraer una lista de elementos encontrados.
                NodeList node = (NodeList) xPath.evaluate(variableNode, inputDocument, XPathConstants.NODESET);

                String element = node.item(0).getTextContent();

                // Builder para cargar el documento
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = dbFactory.newDocumentBuilder();

                // Creamos un documento
                Document translatedDocument = builder.newDocument();

                // Creamos el nodo sql
                Element sqlElement = translatedDocument.createElement("sql");
                sqlElement.setTextContent("select " + selection + " from " + table + " where " + variable + " = '" + element + "' " + otherConditions);

                // Le añadimos el replicator id si tiene
                NodeList replicatorIDNode = (NodeList) xPath.evaluate("//replicator_id", inputDocument, XPathConstants.NODESET);
                if (replicatorIDNode.getLength() > 0) {
                    sqlElement.setAttribute("replicator_id", replicatorIDNode.item(0).getTextContent());
                }


                // Añadimos el nodo sql
                translatedDocument.appendChild(sqlElement);

                outputSlot.enqueue(translatedDocument);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void TranslateEmailGateway(String from, String toNodeName, String subject, String subjectNodeNames[], String content, String contentNodeNames[]) {
        while (!inputSlot.getQueue().isEmpty()) {
            Document inputDocument = inputSlot.dequeue();
            try {
                // Builder para cargar el documento
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = dbFactory.newDocumentBuilder();

                // Creamos un documento
                Document translatedDocument = builder.newDocument();

                // Creamos los nodos
                Element rootElement = translatedDocument.createElement("email");

                Element fromEmailElement = translatedDocument.createElement("from");
                fromEmailElement.setTextContent(from);

                Element toEmailElement = translatedDocument.createElement("to");
                toEmailElement.setTextContent(getNodeTextContent(inputDocument, toNodeName));

                Element subjectElement = translatedDocument.createElement("subject");
                for (String subjectNodeName : subjectNodeNames) {
                    String nodeTextContent = getNodeTextContent(inputDocument, subjectNodeName);
                    subject = subject.replaceFirst("\\?", nodeTextContent);
                }
                subjectElement.setTextContent(subject);

                Element contentElement = translatedDocument.createElement("content");
                for (String contentNodeName : contentNodeNames) {
                    String nodeTextContent = getNodeTextContent(inputDocument, contentNodeName);
                    content = content.replaceFirst("\\?", nodeTextContent);
                }
                contentElement.setTextContent(content);

                // Añadimos el nodo sql
                translatedDocument.appendChild(rootElement);
                rootElement.appendChild(fromEmailElement);
                rootElement.appendChild(toEmailElement);
                rootElement.appendChild(subjectElement);
                rootElement.appendChild(contentElement);

                outputSlot.enqueue(translatedDocument);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getNodeTextContent(Document inputDocument, String nodeName) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodeList = (NodeList) xPath.evaluate("//*", inputDocument, XPathConstants.NODESET);
            boolean encontrado = false;
            int i = 0;
            String resultado = "";
            while (!encontrado) {
                String comparedNode = nodeList.item(i).getNodeName();
                if (comparedNode.equals(nodeName)) {
                    encontrado = true;
                } else i++;
            }

            return nodeList.item(i).getTextContent();

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
