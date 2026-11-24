import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order placed by a customer.
 */
public class Order {

    private int orderId;
    private int customerId;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPincode;
    private String orderDate;
    private double totalAmount;
    private String status;

    // Products/items included in this order
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(int customerId, String orderDate, double totalAmount, String status) {
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
    return customerName;
}

public void setCustomerName(String customerName) {
    this.customerName = customerName;
}

public String getCustomerPhone() {
    return customerPhone;
}

public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
}

public String getDeliveryAddress() {
    return deliveryAddress;
}

public void setDeliveryAddress(String deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
}

public String getDeliveryCity() {
    return deliveryCity;
}

public void setDeliveryCity(String deliveryCity) {
    this.deliveryCity = deliveryCity;
}

public String getDeliveryPincode() {
    return deliveryPincode;
}

public void setDeliveryPincode(String deliveryPincode) {
    this.deliveryPincode = deliveryPincode;
}

   

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ---- Order Items ----

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}