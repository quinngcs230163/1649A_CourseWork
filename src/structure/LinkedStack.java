package structure;

public class LinkedStack<T> {
    private SinglyNode<T> top;
    private int size;

    public LinkedStack() {
        top = null;
        size = 0;
    }

    public void push(T data) {
        SinglyNode<T> newNode = new SinglyNode<>(data);
        newNode.next = top;
        top = newNode;
        size++;
    }

    public T pop() {
        if (isEmpty()) {
            return null;
        }

        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }

    public void display() {
        if (isEmpty()) {
            System.out.println("Action history is empty.");
            return;
        }

        SinglyNode<T> current = top;
        System.out.println("=== Action History (Latest First) ===");
        while (current != null) {
            System.out.println(current.data);
            current = current.next;
        }
    }
}