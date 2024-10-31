package Tasks;

import Main.Slot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.UUID;

public class Replicator { // Por comprobar

    Slot inputSlot;
    List<Slot> outputSlotList;

    public Replicator(Slot inputSlot, List<Slot> outputSlotList) {
        this.inputSlot = inputSlot;
        this.outputSlotList = outputSlotList;
    }

    public void Replicate() {

        while (!inputSlot.getQueue().isEmpty()) {
            Document inputDocument = inputSlot.dequeue();

            // Creamos un nuevo elemento replicator_id
            Element root = inputDocument.getDocumentElement();
            Element idElement = inputDocument.createElement("replicator_id");

            // Creamos un ID único para asignárselo al elemento creado que necesitaremos en el correlator
            idElement.setTextContent(UUID.randomUUID().toString());
            root.appendChild(idElement);

            for (int i = 0; i < outputSlotList.size(); i++)
                outputSlotList.get(i).enqueue(inputDocument);
        }
    }
}