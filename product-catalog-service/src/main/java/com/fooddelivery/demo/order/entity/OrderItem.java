package com.fooddelivery.demo.order.entity;



import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // IDs from Product/Catalog Service
    @Column(name = "meal_id", nullable = false)
    private UUID mealId;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    // Product snapshot at checkout
    @Column(name = "meal_name", nullable = false)
    private String mealName;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected OrderItem() {
    }

    public OrderItem(
            UUID mealId,
            UUID variantId,
            String mealName,
            String variantName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {
        this.mealId = mealId;
        this.variantId = variantId;
        this.mealName = mealName;
        this.variantName = variantName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMealId() {
        return mealId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public String getMealName() {
        return mealName;
    }

    public String getVariantName() {
        return variantName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
