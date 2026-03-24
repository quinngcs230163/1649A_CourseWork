package model;

import structure.SinglyNode;

public class Order {
    private String orderId;
    private String customerName;
    private String shippingAddress;
    private SinglyNode<OrderItem> itemsHead;
    private String status;

    public Order(String orderId, String customerName, String shippingAddress) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.itemsHead = null;
        this.status = "Pending";
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getStatus() {
        return status;
    }

    public SinglyNode<OrderItem> getItemsHead() {
        return itemsHead;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public boolean hasItems() {
        return itemsHead != null;
    }

    public boolean containsBookId(String bookId) {
        SinglyNode<OrderItem> current = itemsHead;
        while (current != null) {
            if (current.data.getBook().getBookId().equalsIgnoreCase(bookId)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public OrderItem findItemByBookId(String bookId) {
        SinglyNode<OrderItem> current = itemsHead;
        while (current != null) {
            if (current.data.getBook().getBookId().equalsIgnoreCase(bookId)) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    public void addOrUpdateItem(OrderItem item) {
        if (item == null || item.getBook() == null || item.getQuantity() <= 0) {
            return;
        }

        if (itemsHead == null) {
            itemsHead = new SinglyNode<>(item);
            return;
        }

        SinglyNode<OrderItem> current = itemsHead;
        while (current != null) {
            if (current.data.getBook().getBookId().equalsIgnoreCase(item.getBook().getBookId())) {
                current.data.setQuantity(current.data.getQuantity() + item.getQuantity());
                return;
            }

            if (current.next == null) {
                break;
            }
            current = current.next;
        }

        current.next = new SinglyNode<>(item);
    }

    public double calculateTotal() {
        double total = 0;
        SinglyNode<OrderItem> current = itemsHead;
        while (current != null) {
            total += current.data.getSubtotal();
            current = current.next;
        }
        return total;
    }

    public void displayItems() {
        if (itemsHead == null) {
            System.out.println("No items in this order.");
            return;
        }

        SinglyNode<OrderItem> current = itemsHead;
        while (current != null) {
            System.out.println(current.data);
            current = current.next;
        }
    }

    public void sortItemsByTitle() {
        if (itemsHead == null || itemsHead.next == null) {
            return;
        }

        SinglyNode<OrderItem> sorted = null;
        SinglyNode<OrderItem> current = itemsHead;

        while (current != null) {
            SinglyNode<OrderItem> next = current.next;
            current.next = null;
            sorted = insertSortedByTitle(sorted, current);
            current = next;
        }

        itemsHead = sorted;
    }

    private SinglyNode<OrderItem> insertSortedByTitle(SinglyNode<OrderItem> sortedHead, SinglyNode<OrderItem> newNode) {
        String newTitle = newNode.data.getBook().getTitle().toLowerCase();

        if (sortedHead == null || sortedHead.data.getBook().getTitle().toLowerCase().compareTo(newTitle) > 0) {
            newNode.next = sortedHead;
            return newNode;
        }

        SinglyNode<OrderItem> current = sortedHead;
        while (current.next != null && current.next.data.getBook().getTitle().toLowerCase().compareTo(newTitle) <= 0) {
            current = current.next;
        }

        newNode.next = current.next;
        current.next = newNode;
        return sortedHead;
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId +
                ", Customer: " + customerName +
                ", Shipping Address: " + shippingAddress +
                ", Status: " + status +
                ", Total: $" + String.format("%.2f", calculateTotal());
    }
}