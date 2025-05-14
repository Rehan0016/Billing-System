import java.util.*;
public class BillingSystem {
    private static final Scanner scanner = new Scanner(System.in);

    private static final Map<Integer, Product> productMap = new HashMap<>();
    private static final TreeMap<Product, Integer> stock = new TreeMap<>(Comparator.comparing(Product::getId));
    private static final LinkedHashMap<Product, Integer> cart = new LinkedHashMap<>();
    private static final TreeSet<String> categories = new TreeSet<>();
    private static final Map<String, List<Product>> categoryMap = new HashMap<>();
    private static final Queue<String> purchaseHistory = new LinkedList<>();
    private static final Stack<String> undoStack = new Stack<>(); // Stack for undoing cart actions

    public static void main(String[] args) {
        initializeProducts();
        displayWelcomeMessage();
        showMainMenu();
    }

    private static void initializeProducts() {
        addProduct(new Product(1, "Pen", "Stationery", 10));
        addProduct(new Product(2, "Notebook", "Stationery", 40));
        addProduct(new Product(3, "Laptop", "Electronics", 50000));
        addProduct(new Product(4, "Charger", "Electronics", 800));
        addProduct(new Product(5, "USB Cable", "Electronics", 150));
    }

    private static void addProduct(Product p) {
        productMap.put(p.getId(), p);
        stock.put(p, 20); // default stock for each product
        categories.add(p.getCategory());
        categoryMap.putIfAbsent(p.getCategory(), new ArrayList<>());
        categoryMap.get(p.getCategory()).add(p);
    }

    private static void displayWelcomeMessage() {
        System.out.println("=====================================");
        System.out.println("      Welcome to Billing System      ");
        System.out.println("=====================================");
    }

    private static void showMainMenu() {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Browse by Category");
            System.out.println("2. View Cart");
            System.out.println("3. Generate Bill");
            System.out.println("4. View Stock");
            System.out.println("5. View Purchase History");
            System.out.println("6. CRUD Operations for Products");
            System.out.println("7. Undo Last Cart Action");
            System.out.println("8. Search Products");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1: browseCategories(); break;
                case 2: viewCart(); break;
                case 3: generateBill(); break;
                case 4: viewStock(); break;
                case 5: viewPurchaseHistory(); break;
                case 6: crudOperations(); break;
                case 7: undoLastCartAction(); break;
                case 8: searchProducts(); break;
                case 9:
                    isRunning = false;
                    System.out.println("Thank you! Visit again.");
                    break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // New method to handle product search
    private static void searchProducts() {
        System.out.println("\n--- Search Products ---");
        System.out.println("1. Search by Product ID");
        System.out.println("2. Search by Product Name");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();

        scanner.nextLine(); // Consume newline
        switch (choice) {
            case 1:
                System.out.print("Enter Product ID to search: ");
                int id = scanner.nextInt();
                Product p = productMap.get(id);
                if (p != null) {
                    System.out.println("Product Found: " + p + " | In Stock: " + stock.get(p));
                } else {
                    System.out.println("Product not found with ID: " + id);
                }
                break;

            case 2:
                System.out.print("Enter Product Name to search: ");
                String name = scanner.nextLine().toLowerCase();
                boolean found = false;
                for (Product product : productMap.values()) {
                    if (product.getName().toLowerCase().contains(name)) {
                        System.out.println("Found Product: " + product + " | In Stock: " + stock.get(product));
                        found = true;
                    }
                }
                if (!found) {
                    System.out.println("No products found with name containing: " + name);
                }
                break;

            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void browseCategories() {
        System.out.println("\n--- Categories ---");
        int i = 1;
        List<String> categoryList = new ArrayList<>(categories);
        for (String category : categoryList) {
            System.out.println(i++ + ". " + category);
        }

        System.out.print("Select category: ");
        int choice = scanner.nextInt();
        if (choice < 1 || choice > categoryList.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        String selectedCategory = categoryList.get(choice - 1);
        List<Product> products = categoryMap.get(selectedCategory);

        System.out.println("\n--- Products in " + selectedCategory + " ---");
        for (Product p : products) {
            System.out.println(p + " | In Stock: " + stock.get(p));
        }

        System.out.print("Enter product ID to add to cart (or 0 to go back): ");
        int id = scanner.nextInt();
        if (id == 0) return;

        Product selected = productMap.get(id);
        if (selected == null || !products.contains(selected)) {
            System.out.println("Invalid Product ID.");
            return;
        }

        System.out.print("Enter quantity: ");
        int qty = scanner.nextInt();
        int available = stock.getOrDefault(selected, 0);
        if (qty <= 0 || qty > available) {
            System.out.println("Invalid quantity. Available: " + available);
            return;
        }

        cart.put(selected, cart.getOrDefault(selected, 0) + qty);
        stock.put(selected, available - qty);
        undoStack.push("Added " + selected.getName() + " x" + qty);  // Pushing action to undo stack
        System.out.println("Added to cart.");
    }

    private static void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        System.out.println("\n--- Your Cart ---");
        double total = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double price = p.getPrice() * qty;
            System.out.println(p + " | Qty: " + qty + " | Total: ₹" + price);
            total += price;
        }
        System.out.println("Total Amount: ₹" + total);
    }

    private static void generateBill() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        System.out.println("\n=== Final Bill ===");
        double total = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double amount = p.getPrice() * qty;
            total += amount;
            System.out.println(p + " x" + qty + " = ₹" + amount);
            purchaseHistory.add(p.getName() + " x" + qty + " = ₹" + amount);
        }

        System.out.println("Total: ₹" + total);
        System.out.println("Thank you for your purchase!");
        cart.clear();
    }

    private static void viewStock() {
        System.out.println("\n--- Available Stock ---");
        for (Map.Entry<Product, Integer> entry : stock.entrySet()) {
            System.out.println(entry.getKey() + " | Qty: " + entry.getValue());
        }
    }

    private static void viewPurchaseHistory() {
        if (purchaseHistory.isEmpty()) {
            System.out.println("No purchase history.");
            return;
        }

        System.out.println("\n--- Purchase History ---");
        for (String record : purchaseHistory) {
            System.out.println(record);
        }
    }

    private static void crudOperations() {
        System.out.println("\n--- CRUD Operations ---");
        System.out.println("1. Add Product");
        System.out.println("2. Update Product");
        System.out.println("3. Delete Product");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1: addNewProduct(); break;
            case 2: updateProduct(); break;
            case 3: deleteProduct(); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void addNewProduct() {
        System.out.print("Enter product ID: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter product name: ");
        String name = scanner.nextLine();

        System.out.print("Enter product category: ");
        String category = scanner.nextLine();

        System.out.print("Enter product price: ₹");
        double price = scanner.nextDouble();

        addProduct(new Product(id, name, category, price));
        System.out.println("Product added successfully!");
    }

    private static void updateProduct() {
        System.out.print("Enter product ID to update: ");
        int id = scanner.nextInt();

        Product p = productMap.get(id);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }

        scanner.nextLine(); // Consume newline
        System.out.print("Enter new name (or press Enter to keep current): ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) p.setName(name);

        System.out.print("Enter new category (or press Enter to keep current): ");
        String category = scanner.nextLine();
        if (!category.isEmpty()) p.setCategory(category);

        System.out.print("Enter new price (or press Enter to keep current): ₹");
        double price = scanner.nextDouble();
        if (price > 0) p.setPrice(price);

        System.out.println("Product updated successfully!");
    }

    private static void deleteProduct() {
        System.out.print("Enter product ID to delete: ");
        int id = scanner.nextInt();

        Product p = productMap.remove(id);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }

        stock.remove(p);
        categoryMap.get(p.getCategory()).remove(p);
        categories.remove(p.getCategory());

        System.out.println("Product deleted successfully!");
    }

    private static void undoLastCartAction() {
        if (undoStack.isEmpty()) {
            System.out.println("No actions to undo.");
            return;
        }

        String lastAction = undoStack.pop();
        System.out.println("Undoing last action: " + lastAction);
    }
}
