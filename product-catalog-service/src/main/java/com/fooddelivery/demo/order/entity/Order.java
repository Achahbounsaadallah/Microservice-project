package com.fooddelivery.demo.order.entity;

import com.fooddelivery.demo.cart.entity.Cart;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(
                        name = "idx_order_customer",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_order_status",
                        columnList = "status"
                )
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "order_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String orderNumber;

    // One order comes from one cart
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_cart_id",
            nullable = false,
            unique = true
    )
    private Cart sourceCart;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    public Order(
            Cart sourceCart,
            UUID customerId,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal totalAmount,
            String deliveryAddress,
            String customerPhone
    ) {
        this.sourceCart = sourceCart;
        this.customerId = customerId;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.customerPhone = customerPhone;
        this.status = OrderStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        this.orderNumber = "ORD-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Cart getSourceCart() {
        return sourceCart;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
