package DB;

import Main.Slot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class ConectorUniDB {
    Slot inputSlot;
    Slot outputSlot;
    IIADB con;

    public ConectorUniDB(Slot inputSlot, Slot outputSlot) throws Exception {
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        con=new IIADB();
    }

    public void Conect() {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            while (!inputSlot.getQueue().isEmpty()) {
                Document inputDocument = inputSlot.dequeue();

                // Consulta xPath para extraer una lista de elementos encontrados.
                NodeList node = (NodeList) xPath.evaluate("/sql", inputDocument, XPathConstants.NODESET);
                String sqlQuery = node.item(0).getTextContent();
                String id_replicator = node.item(0).getAttributes().item(0).getTextContent();

                String[] parts = sqlQuery.split("'");

                // Builder para cargar el documento
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = dbFactory.newDocumentBuilder();

                // Creamos un documento
                Document reponseDocument = builder.newDocument();

                // Creamos el nodo sql
                Element resultSetElement = reponseDocument.createElement("resultSet");
                Element dniElement = reponseDocument.createElement("dni");
                Element replicatorIDElement = reponseDocument.createElement("replicator_id");
                Element emailElement = reponseDocument.createElement("email");

                dniElement.setTextContent(parts[1]);
                replicatorIDElement.setTextContent(id_replicator);

                String email = con.realizarConsultaAlumno(sqlQuery);

                emailElement.setTextContent(email);

                reponseDocument.appendChild(resultSetElement);
                resultSetElement.appendChild(dniElement);
                resultSetElement.appendChild(replicatorIDElement);
                resultSetElement.appendChild(emailElement);

                outputSlot.enqueue(reponseDocument);

            }
            con.desconexion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}