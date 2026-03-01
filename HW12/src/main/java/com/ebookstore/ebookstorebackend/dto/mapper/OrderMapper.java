package com.ebookstore.ebookstorebackend.dto.mapper;

import com.ebookstore.ebookstorebackend.entity.Order;
import com.ebookstore.ebookstorebackend.entity.OrderItem;
import com.ebookstore.ebookstorebackend.dto.OrderDTO;
import com.ebookstore.ebookstorebackend.dto.OrderItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;

    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
          OrderDTO orderDTO = new OrderDTO(
            order.getId(),
            order.getOrderNumber(),
            order.getCreateTime(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getShippingAddress(),
            order.getContactPhone(),
            order.getPaymentTime()
        );
        
        // Set nested objects using setters
        if (order.getUser() != null) {
            orderDTO.setUser(userMapper.toDTO(order.getUser()));
        }
        
        if (order.getOrderItems() != null) {
            List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItemMapper::toDTO)
                .collect(Collectors.toList());
            orderDTO.setOrderItems(orderItemDTOs);
        }
        
        return orderDTO;
    } 
    
    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }
        
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setOrderNumber(orderDTO.getOrderNumber());
        order.setCreateTime(orderDTO.getCreateTime());
        order.setTotalAmount(orderDTO.getTotal());
        order.setStatus(orderDTO.getStatus());
        order.setShippingAddress(orderDTO.getAddress());
        order.setContactPhone(orderDTO.getPhone());
        order.setPaymentTime(orderDTO.getPaymentTime());
        
        if (orderDTO.getUser() != null) {
            order.setUser(userMapper.toEntity(orderDTO.getUser()));
        }
        
        if (orderDTO.getOrderItems() != null) {
            List<OrderItem> orderItems = orderDTO.getOrderItems().stream()
                .map(orderItemMapper::toEntity)
                .collect(Collectors.toList());
            order.setOrderItems(orderItems);
        }
        
        return order;
    }
}