package service;

import model.Book;
import model.Order;
import model.OrderItem;
import structure.DoublyNode;
import structure.LinkedQueue;
import structure.SinglyNode;

import java.io.*;

public class OrderFileManager {
    private static final String PENDING_FILE = "pending_orders.csv";
    private static final String PROCESSED_FILE = "processed_orders.csv";
    private static final String HEADER = "orderId,customerName,shippingAddress,status,bookId,title,author,price,quantity,total";

    public OrderFileManager() {
        initializeFile(PENDING_FILE);
        initializeFile(PROCESSED_FILE);
    }

    private void initializeFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(HEADER);
                writer.newLine();
            } catch (IOException e) {
                System.out.println("Error creating file: " + fileName);
            }
        }
    }

    public void saveAllPendingOrders(LinkedQueue<Order> queue) {
        rewriteOrderFile(queue, PENDING_FILE);
    }

    public void saveProcessedOrder(Order order) {
        appendOrderToFile(order, PROCESSED_FILE);
    }

    public LinkedQueue<Order> loadPendingOrders() {
        return loadOrdersFromFile(PENDING_FILE);
    }

    public LinkedQueue<Order> loadProcessedOrders() {
        return loadOrdersFromFile(PROCESSED_FILE);
    }

    private void rewriteOrderFile(LinkedQueue<Order> queue, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(HEADER);
            writer.newLine();

            if (queue == null || queue.isEmpty()) {
                return;
            }

            DoublyNode<Order> current = queue.getFrontNode();
            while (current != null) {
                writeOrder(writer, current.data);
                current = current.next;
            }
        } catch (IOException e) {
            System.out.println("Error rewriting file: " + fileName);
        }
    }

    private void appendOrderToFile(Order order, String fileName) {
        if (order == null || !order.hasItems()) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writeOrder(writer, order);
        } catch (IOException e) {
            System.out.println("Error writing order to file: " + fileName);
        }
    }

    private void writeOrder(BufferedWriter writer, Order order) throws IOException {
        SinglyNode<OrderItem> current = order.getItemsHead();
        while (current != null) {
            writer.write(buildCsvLine(order, current.data));
            writer.newLine();
            current = current.next;
        }
    }

    private String buildCsvLine(Order order, OrderItem item) {
        return escape(order.getOrderId()) + "," +
                escape(order.getCustomerName()) + "," +
                escape(order.getShippingAddress()) + "," +
                escape(order.getStatus()) + "," +
                escape(item.getBook().getBookId()) + "," +
                escape(item.getBook().getTitle()) + "," +
                escape(item.getBook().getAuthor()) + "," +
                item.getBook().getPrice() + "," +
                item.getQuantity() + "," +
                order.calculateTotal();
    }

    private String escape(String value) {
        if (value == null) {
            return "\"\"";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private LinkedQueue<Order> loadOrdersFromFile(String fileName) {
        LinkedQueue<Order> queue = new LinkedQueue<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseCsvLine(line);
                OrderCsvRecord record = convertToRecord(parts);
                if (record == null) {
                    continue;
                }

                Order order = findOrderInQueue(queue, record.orderId);

                if (order == null) {
                    order = new Order(record.orderId, record.customerName, record.shippingAddress);
                    order.setStatus(record.status);
                    queue.enqueue(order);
                }

                Book book = new Book(record.bookId, record.title, record.author, record.price);
                order.addOrUpdateItem(new OrderItem(book, record.quantity));
            }
        } catch (IOException e) {
            System.out.println("Error loading orders from file: " + fileName);
        }

        return queue;
    }

    private Order findOrderInQueue(LinkedQueue<Order> queue, String orderId) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }

        DoublyNode<Order> current = queue.getFrontNode();
        while (current != null) {
            if (current.data != null && current.data.getOrderId().equalsIgnoreCase(orderId)) {
                return current.data;
            }
            current = current.next;
        }

        return null;
    }

    private OrderCsvRecord convertToRecord(String[] parts) {
        if (parts == null) {
            return null;
        }

        try {
            if (parts.length >= 10) {
                String orderId = removeQuotes(parts[0]);
                String customerName = removeQuotes(parts[1]);
                String shippingAddress = removeQuotes(parts[2]);
                String status = defaultStatus(removeQuotes(parts[3]));
                String bookId = removeQuotes(parts[4]);
                String title = removeQuotes(parts[5]);
                String author = removeQuotes(parts[6]);
                double price = Double.parseDouble(parts[7]);
                int quantity = Integer.parseInt(parts[8]);

                if (!isRecordValid(orderId, customerName, shippingAddress, bookId, price, quantity)) {
                    return null;
                }

                return new OrderCsvRecord(orderId, customerName, shippingAddress, status, bookId, title, author, price, quantity);
            }

            if (parts.length == 9) {
                String orderId = removeQuotes(parts[0]);
                String customerName = removeQuotes(parts[1]);
                String shippingAddress = "N/A";
                String status = defaultStatus(removeQuotes(parts[2]));
                String bookId = removeQuotes(parts[3]);
                String title = removeQuotes(parts[4]);
                String author = removeQuotes(parts[5]);
                double price = Double.parseDouble(parts[6]);
                int quantity = Integer.parseInt(parts[7]);

                if (!isRecordValid(orderId, customerName, shippingAddress, bookId, price, quantity)) {
                    return null;
                }

                return new OrderCsvRecord(orderId, customerName, shippingAddress, status, bookId, title, author, price, quantity);
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }

    private boolean isRecordValid(String orderId, String customerName, String shippingAddress,
                                  String bookId, double price, int quantity) {
        return !orderId.isEmpty() && !customerName.isEmpty() && !shippingAddress.isEmpty()
                && !bookId.isEmpty() && price > 0 && quantity > 0;
    }

    private String defaultStatus(String status) {
        return status == null || status.trim().isEmpty() ? "Pending" : status;
    }

    private String[] parseCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String removeQuotes(String value) {
        return value.replace("\"\"", "\"").replace("\"", "").trim();
    }

    private static class OrderCsvRecord {
        private String orderId;
        private String customerName;
        private String shippingAddress;
        private String status;
        private String bookId;
        private String title;
        private String author;
        private double price;
        private int quantity;

        private OrderCsvRecord(String orderId, String customerName, String shippingAddress, String status,
                               String bookId, String title, String author, double price, int quantity) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.shippingAddress = shippingAddress;
            this.status = status;
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.price = price;
            this.quantity = quantity;
        }
    }
}