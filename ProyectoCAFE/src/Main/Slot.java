package Main;

import java.util.LinkedList;
import java.util.Queue;
import org.w3c.dom.Document;

public class Slot {

    private final Queue<Document> queue;

    public Slot() {
        queue = new LinkedList<>();
    }

    public Queue<Document> getQueue() {
        return this.queue;
    }

    public void enqueue(Document doc) {
        queue.add(doc);
    }

    public Document dequeue() {
        return queue.poll();
    }
}