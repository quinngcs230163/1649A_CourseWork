package structure;

public class LinkedQueue<T> {
    private DoublyNode<T> front;
    private DoublyNode<T> rear;
    private int size;

    public LinkedQueue() {
        front = null;
        rear = null;
        size = 0;
    }

    public void enqueue(T data) {
        DoublyNode<T> newNode = new DoublyNode<>(data);

        if (isEmpty()) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            newNode.prev = rear;
            rear = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            return null;
        }

        T data = front.data;

        if (front == rear) {
            front = rear = null;
        } else {
            front = front.next;
            front.prev = null;
        }

        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return front.data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return size;
    }

    public DoublyNode<T> getFrontNode() {
        return front;
    }

    public void display() {
        if (isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        DoublyNode<T> current = front;
        while (current != null) {
            System.out.println(current.data);
            current = current.next;
        }
    }
}