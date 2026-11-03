package com.fooddelivery.demo.order.service;

import com.fooddelivery.demo.cart.entity.*;
import com.fooddelivery.demo.cart.repository.CartRepository;

import com.fooddelivery.demo.client.catalog.*;
import com.fooddelivery.demo.exception.*;
import com.fooddelivery.demo.order.dto.*;
import com.fooddelivery.demo.order.entity.*;
import com.fooddelivery.demo.order.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CatalogClient catalogClient;
    private final BigDecimal deliveryFee;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CatalogClient catalogClient,
            @Value("${app.order.delivery-fee:10.00}")
            BigDecimal deliveryFee
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.catalogClient = catalogClient;
        this.deliveryFee = deliveryFee;
    }

    // ==================================
    // CHECKOUT
    // ==================================

    @Override
    public OrderResponse checkout(
            UUID customerId,
            CheckoutRequest request
    ) {

        // 1. Find active cart
        Cart cart = cartRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        CartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Active cart not found"
                        )
                );

        // 2. Verify cart is not empty
        if (cart.getItems().isEmpty()) {
            throw new CartEmptyException(
                    "Cannot checkout an empty cart"
            );
        }

        // 3. Prepare order items
        BigDecimal subtotal = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {

            // Retrieve current variant information from Catalog
            CatalogVariantResponse variant =
                    catalogClient.getVariant(
                            cartItem.getVariantId()
                    );

            // 4. Verify availability
            if (!variant.available()) {
                throw new MealUnavailableException(
                        variant.mealName() + " is unavailable"
                );
            }

            // Verify price
            if (variant.price() == null ||
                    variant.price().signum() < 0) {

                throw new CatalogUnavailableException(
                        "Invalid price received from Catalog"
                );
            }

            // 5. Calculate line total
            BigDecimal unitPrice = variant.price();

            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            subtotal = subtotal.add(lineTotal);

            // 6. Create order item snapshot
            OrderItem orderItem = new OrderItem(
                    variant.mealId(),
                    variant.id(),
                    variant.mealName(),
                    variant.size(),
                    unitPrice,
                    cartItem.getQuantity(),
                    lineTotal
            );

            orderItems.add(orderItem);
        }

        // 7. Calculate total
        BigDecimal totalAmount = subtotal.add(deliveryFee);

        // 8. Verify the restaurant still exists and is open
        CatalogRestaurantResponse restaurant =
                catalogClient.getRestaurant(cart.getRestaurantId());

        if (!restaurant.active()) {
            throw new MealUnavailableException(
                    "This restaurant is not accepting orders at the moment"
            );
        }

        // 9. Create Order
        Order order = new Order(
                cart,
                customerId,
                subtotal,
                deliveryFee,
                totalAmount,
                request.deliveryAddress(),
                request.customerPhone(),
                restaurant.id(),
                restaurant.name()
        );

        // 9. Attach items to order
        for (OrderItem item : orderItems) {
            order.addItem(item);
        }

        // 10. Persist order and its items
        Order savedOrder = orderRepository.saveAndFlush(order);

        // 11. Convert cart
        cart.markAsConverted();
        cartRepository.save(cart);

        // 12. Return response
        return mapToResponse(savedOrder);
    }

    // ==================================
    // GET ORDER
    // ==================================

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(
            UUID customerId,
            UUID orderId
    ) {

        Order order = orderRepository
                .findWithItemsByIdAndCustomerId(
                        orderId,
                        customerId
                )
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );

        return mapToResponse(order);
    }

    // ==================================
    // CUSTOMER ORDER HISTORY
    // ==================================

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(
            UUID customerId
    ) {

        return orderRepository
                .findAllByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==================================
    // CANCEL ORDER
    // ==================================

    @Override
    public OrderResponse cancelOrder(
            UUID customerId,
            UUID orderId
    ) {

        Order order = findOrderForCustomer(customerId, orderId);

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.CONFIRMED) {

            throw new InvalidOrderStatusException(
                    "This order cannot be cancelled"
            );
        }

        order.changeStatus(OrderStatus.CANCELLED);

        return mapToResponse(order);
    }

    // ==================================
    // CHANGE ORDER STATUS
    // ==================================

    @Override
    public OrderResponse changeStatus(
            UUID orderId,
            OrderStatus newStatus
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );

        if (!isValidTransition(
                order.getStatus(),
                newStatus
        )) {
            throw new InvalidOrderStatusException(
                    "Invalid transition from " +
                            order.getStatus() + " to " + newStatus
            );
        }

        order.changeStatus(newStatus);

        return mapToResponse(order);
    }

    // ==================================
    // HELPER METHODS
    // ==================================

    private Order findOrderForCustomer(
            UUID customerId,
            UUID orderId
    ) {

        return orderRepository
                .findWithItemsByIdAndCustomerId(
                        orderId,
                        customerId
                )
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );
    }

    private boolean isValidTransition(
            OrderStatus current,
            OrderStatus next
    ) {

        return switch (current) {

            case PENDING ->
                    next == OrderStatus.CONFIRMED ||
                            next == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    next == OrderStatus.PREPARING ||
                            next == OrderStatus.CANCELLED;

            case PREPARING ->
                    next == OrderStatus.READY_FOR_PICKUP;

            case READY_FOR_PICKUP ->
                    next == OrderStatus.COMPLETED;

            case COMPLETED, CANCELLED -> false;
        };
    }

    // ==================================
    // ENTITY TO DTO
    // ==================================

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getMealId(),
                                item.getVariantId(),
                                item.getMealName(),
                                item.getVariantName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getLineTotal()
                        ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                items,
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getCustomerPhone(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}