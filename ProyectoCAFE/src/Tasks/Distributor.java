package Tasks;

import Main.Slot;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.LinkedList;
import java.util.List;

public class Distributor {

    Slot inputSlot;
    List<Slot> outputSlotList;
    String criteria;

    public Distributor(Slot inputSlot, List<Slot> outputSlotList, String criteria) {
        this.inputSlot = inputSlot;
        this.outputSlotList = outputSlotList;
        this.criteria = criteria; // "type"
    }

    public List<String> Distribute() {
        List<String> types = new LinkedList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        while (!inputSlot.getQueue().isEmpty()) {
            Document inputDocument = inputSlot.dequeue();
            try {
                NodeList root = (NodeList) xPath.evaluate(criteria, inputDocument, XPathConstants.NODESET);
                String type = root.item(0).getTextContent();
                int typePos = types.indexOf(type);
                if (typePos == -1) {
                    Slot slot = new Slot();
                    slot.enqueue(inputDocument);
                    outputSlotList.add(slot);
                    types.add(type);
                }
                else {
                    outputSlotList.get(typePos).enqueue(inputDocument);
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
        return types;
    }
}
