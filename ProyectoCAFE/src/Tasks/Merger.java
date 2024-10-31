package Tasks;

import Main.Slot;

import java.util.List;

public class Merger {

    List<Slot> inputSlotList;
    Slot outputSlot;

    public Merger(List<Slot> inputSlotQueue, Slot outputSlot) {
        this.inputSlotList = inputSlotQueue;
        this.outputSlot = outputSlot;
    }

    public void Merge() {
        for (int i = 0; i<inputSlotList.size(); i++) {
            while (!inputSlotList.get(i).getQueue().isEmpty()) {
                outputSlot.enqueue(inputSlotList.get(i).dequeue());
            }
        }
    }
}