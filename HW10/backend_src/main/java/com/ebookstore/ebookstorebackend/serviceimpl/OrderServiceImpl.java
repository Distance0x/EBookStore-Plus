package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebookstore.ebookstorebackend.dto.mapper.OrderMapper;
import com.ebookstore.ebookstorebackend.entity.*;
import com.ebookstore.ebookstorebackend.dao.*;
import com.ebookstore.ebookstorebackend.service.OrderItemService;
import com.ebookstore.ebookstorebackend.service.OrderService;
import com.ebookstore.ebookstorebackend.service.BookService;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.math.RoundingMode;

@Service
public class OrderServiceImpl implements OrderService {    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private BookDao bookDao;
    
    @Autowired
    private CartDao cartDao;
    
    @Autowired
    private OrderItemService orderItemService;
      @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private BookService bookService;    @Override
    @Transactional
    public OrderDTO createOrderFromCart(Long userId, String shippingAddress, String contactPhone) {
        // 获取用户
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取用户购物车
        List<Cart> cartItems = cartDao.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空，无法创建订单");
        }
          // 检查购物车中是否有已删除的书籍
        List<String> deletedBookTitles = new ArrayList<>();
        for (Cart cartItem : cartItems) {
            Optional<Book> bookOpt = bookDao.findById(cartItem.getBook().getId());
            if (bookOpt.isPresent() && bookOpt.get().isDeleted()) {
                deletedBookTitles.add(bookOpt.get().getTitle());
            }
        }
        
        if (!deletedBookTitles.isEmpty()) {
            throw new RuntimeException("购物车中包含已下架的图书：" + String.join("、", deletedBookTitles) + "。请先清理购物车中的已下架图书后再下单。");
        }
        
        // 创建订单
        String orderNumber = generateOrderNumber();
        Order order = new Order(user, orderNumber, "待支付", shippingAddress, contactPhone);
          // 计算总金额并检查库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            Book book = cartItem.getBook();
            
            // 检查库存是否充足
            if (book.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("图书《" + book.getTitle() + "》库存不足，当前库存：" + book.getStock() + "，需要：" + cartItem.getQuantity());
            }
            
            // 计算总金额
            BigDecimal itemTotal = book.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
              // 创建订单项 - 简化版本，只存储必要信息
            OrderItem orderItem = new OrderItem(
                order,
                book.getId(),
                book.getPrice(),
                cartItem.getQuantity()
            );
            order.addOrderItem(orderItem);
        }
        
        // 扣减库存（在创建订单时就扣减）
        for (Cart cartItem : cartItems) {
            Book book = cartItem.getBook();
            book.setStock(book.getStock() - cartItem.getQuantity());
            bookDao.save(book);
        }
          // 更新订单总金额
        order.setTotalAmount(totalAmount);
        orderDao.save(order);
        
        // 清空购物车 - 使用仓库方法
        cartDao.deleteByUserId(userId);
        
        return orderMapper.toDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        return orderMapper.toDTO(order);
    }

    @Override
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderDao.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        return orderMapper.toDTO(order);
    }    @Override
    public List<OrderDTO> getOrdersByUser(User user) {
        List<Order> orders = orderDao.findByUserOrderByCreateTimeDesc(user);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        order.setStatus(status);
        
        // 如果状态为已支付，设置支付时间
        if ("已完成".equals(status) || "COMPLETED".equals(status)) {
            order.setPaymentTime(LocalDateTime.now());
        }
        
        Order savedOrder = orderDao.save(order);
        return orderMapper.toDTO(savedOrder);
    }    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 只能删除待处理或已取消的订单
        if (!Arrays.asList("待支付", "PENDING", "CANCELLED").contains(order.getStatus())) {
            throw new RuntimeException("只能删除待处理或已取消的订单");
        }
          // 如果订单状态是待支付，需要恢复库存
        if ("待支付".equals(order.getStatus()) || "PENDING".equals(order.getStatus())) {
            for (OrderItem orderItem : order.getOrderItems()) {
                Book book = bookDao.findById(orderItem.getBookId())
                        .orElseThrow(() -> new RuntimeException("图书不存在：ID=" + orderItem.getBookId()));
                
                // 恢复库存
                book.setStock(book.getStock() + orderItem.getQuantity());
                bookDao.save(book);
            }
        }
        
        // 删除相关的订单项
        orderItemService.deleteOrderItemsByOrderId(orderId);
        
        // 删除订单
        orderDao.delete(order);
    }@Override
    public List<OrderDTO> getOrderDetails(Long orderId) {
        // 获取订单
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 返回单个订单的DTO列表（为了保持接口一致性）
        return Arrays.asList(orderMapper.toDTO(order));
    }
      // 删除用户所有订单 - 使用仓库的自定义方法
    @Override
    @Transactional
    public void deleteAllOrdersByUser(Long userId) {
        // 验证用户是否存在
        if (!userDao.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        
        // 获取用户所有订单
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Order> userOrders = orderDao.findByUser(user);
          // 对于待支付的订单，需要恢复库存
        for (Order order : userOrders) {
            if ("待支付".equals(order.getStatus()) || "PENDING".equals(order.getStatus())) {
                for (OrderItem orderItem : order.getOrderItems()) {
                    Book book = bookDao.findById(orderItem.getBookId())
                            .orElseThrow(() -> new RuntimeException("图书不存在：ID=" + orderItem.getBookId()));
                    
                    // 恢复库存
                    book.setStock(book.getStock() + orderItem.getQuantity());
                    bookDao.save(book);
                }
            }
        }
        
        // 先删除所有相关的订单项
        orderItemService.deleteOrderItemsByUserId(userId);
        
        // 再删除所有订单
        orderDao.deleteAllByUserId(userId);
    }
     // 生成订单编号：ORD + 年月日 + 4位随机数
   private String generateOrderNumber() {
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
       String datePart = LocalDateTime.now().format(formatter);
       String randomPart = String.format("%04d", new Random().nextInt(10000));
       return "ORD" + datePart + randomPart;
   }
     @Override
   @Transactional
   public OrderDTO payOrder(Long orderId) {
       // 获取订单
       Order order = orderDao.findById(orderId)
               .orElseThrow(() -> new RuntimeException("订单不存在"));
       
       // 检查订单状态
       if (!"待支付".equals(order.getStatus())) {
           throw new RuntimeException("订单状态不是待支付，无法进行支付");
       }
       
       // 获取用户
       User user = order.getUser();
       
       // 检查用户余额是否充足
       if (user.getBalance().compareTo(order.getTotalAmount()) < 0) {
           throw new RuntimeException("账户余额不足，当前余额：" + user.getBalance() + "，订单金额：" + order.getTotalAmount());
       }
       
       // 扣减用户余额（库存已在创建订单时扣减，这里只扣减余额）
       user.setBalance(user.getBalance().subtract(order.getTotalAmount()));
       userDao.save(user);
       
       // 更新订单状态为已支付
       order.setStatus("已完成");
       order.setPaymentTime(LocalDateTime.now());
       Order savedOrder = orderDao.save(order);
         return orderMapper.toDTO(savedOrder);
   }
   
   @Override
   @Transactional(readOnly = true)
   public List<OrderDTO> getAllOrders() {
       List<Order> orders = orderDao.findAll();
       return orders.stream()
               .map(orderMapper::toDTO)
               .collect(Collectors.toList());
   }
   
   @Override
   @Transactional(readOnly = true)
   public List<OrderDTO> searchUserOrders(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime) {
       List<Order> orders;
       
       // 根据搜索条件选择相应的查询方法
       if (bookTitle != null && !bookTitle.trim().isEmpty() && startTime != null && endTime != null) {
           // 同时按书籍标题和时间范围搜索
           orders = orderDao.findUserOrdersByBookTitleAndTimeRange(userId, bookTitle.trim(), startTime, endTime);
       } else if (bookTitle != null && !bookTitle.trim().isEmpty()) {
           // 仅按书籍标题搜索
           orders = orderDao.findUserOrdersByBookTitle(userId, bookTitle.trim());
       } else if (startTime != null && endTime != null) {
           // 仅按时间范围搜索
           orders = orderDao.findUserOrdersByTimeRange(userId, startTime, endTime);
       } else {
           // 无搜索条件，返回用户所有订单
           User user = userDao.findById(userId)
                   .orElseThrow(() -> new RuntimeException("用户不存在"));
           orders = orderDao.findByUserOrderByCreateTimeDesc(user);
       }
       
       return orders.stream()
               .map(orderMapper::toDTO)
               .collect(Collectors.toList());
   }
   
   @Override
   @Transactional(readOnly = true)
   public List<OrderDTO> searchAllOrders(String bookTitle, LocalDateTime startTime, LocalDateTime endTime) {
       List<Order> orders;
       
       // 根据搜索条件选择相应的查询方法
       if (bookTitle != null && !bookTitle.trim().isEmpty() && startTime != null && endTime != null) {
           // 同时按书籍标题和时间范围搜索
           orders = orderDao.findOrdersByBookTitleAndTimeRange(bookTitle.trim(), startTime, endTime);
       } else if (bookTitle != null && !bookTitle.trim().isEmpty()) {
           // 仅按书籍标题搜索
           orders = orderDao.findOrdersByBookTitle(bookTitle.trim());
       } else if (startTime != null && endTime != null) {
           // 仅按时间范围搜索
           orders = orderDao.findOrdersByTimeRange(startTime, endTime);
       } else {
           // 无搜索条件，返回所有订单
           orders = orderDao.findAll();
       }
       
       return orders.stream()
               .map(orderMapper::toDTO)
               .collect(Collectors.toList());
   }
   
   @Override
   @Transactional
   public OrderDTO cancelOrder(Long orderId) {
       // 获取订单
       Order order = orderDao.findById(orderId)
               .orElseThrow(() -> new RuntimeException("订单不存在"));
       
       // 检查订单状态，只允许取消未支付的订单
       if (!"待支付".equals(order.getStatus()) && !"PENDING".equals(order.getStatus())) {
           throw new RuntimeException("只能取消待支付状态的订单");
       }
         // 恢复库存
       for (OrderItem orderItem : order.getOrderItems()) {
           Book book = bookDao.findById(orderItem.getBookId())
                   .orElseThrow(() -> new RuntimeException("图书不存在：ID=" + orderItem.getBookId()));
           
           // 恢复库存
           book.setStock(book.getStock() + orderItem.getQuantity());
           bookDao.save(book);
       }
       
       // 更新订单状态为已取消
       order.setStatus("已取消");
       Order savedOrder = orderDao.save(order);
       
       return orderMapper.toDTO(savedOrder);
   }
   
   @Override
   @Transactional(readOnly = true)
   public PurchaseStatisticsDTO getUserPurchaseStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
       // 获取用户在指定时间范围内的已完成订单
       List<Order> completedOrders = orderDao.findCompletedOrdersByUserAndTimeRange(userId, startTime, endTime);
       
       if (completedOrders.isEmpty()) {
           return new PurchaseStatisticsDTO(0, BigDecimal.ZERO, new ArrayList<>());
       }
       
       // 用于存储每本书的统计信息
       Map<String, PurchaseStatisticsDTO.BookStatisticsDTO> bookStatsMap = new HashMap<>();
       BigDecimal totalAmount = BigDecimal.ZERO;
       Integer totalBooks = 0;
       
       // 遍历所有已完成的订单
       for (Order order : completedOrders) {
           totalAmount = totalAmount.add(order.getTotalAmount());
             // 遍历订单中的每个商品
           for (OrderItem item : order.getOrderItems()) {
               // 使用 bookId 作为 key，更简单可靠
               String bookKey = "book_" + item.getBookId();
               
               totalBooks += item.getQuantity();
               
               if (bookStatsMap.containsKey(bookKey)) {
                   // 如果已存在，累加数量和金额
                   PurchaseStatisticsDTO.BookStatisticsDTO existingStats = bookStatsMap.get(bookKey);
                   existingStats.setQuantity(existingStats.getQuantity() + item.getQuantity());
                   existingStats.setTotalPrice(existingStats.getTotalPrice().add(item.getPrice().multiply(new BigDecimal(item.getQuantity()))));
               } else {
                   // 如果不存在，创建新的统计项
                   BigDecimal itemTotalPrice = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                   
                   // 通过 BookService 获取书籍信息
                   String bookTitle = "未知书籍";
                   String bookAuthor = "未知作者";
                   String bookCover = "";
                     try {
                       Optional<BookDTO> bookOpt = bookService.getBookByIdIncludingDeleted(item.getBookId());
                       if (bookOpt.isPresent()) {
                           BookDTO book = bookOpt.get();
                           bookTitle = book.getTitle();
                           bookAuthor = book.getAuthor();
                           bookCover = book.getCover();
                       }
                   } catch (Exception e) {
                       // 异常处理，使用默认值
                   }
                   
                   PurchaseStatisticsDTO.BookStatisticsDTO bookStats = new PurchaseStatisticsDTO.BookStatisticsDTO(
                       bookTitle,
                       bookAuthor,
                       bookCover,
                       item.getQuantity(),
                       itemTotalPrice
                   );
                   bookStatsMap.put(bookKey, bookStats);
               }
           }
       }
       
       // 将Map转换为List，并按购买数量降序排序
       List<PurchaseStatisticsDTO.BookStatisticsDTO> bookStatistics = new ArrayList<>(bookStatsMap.values());
       bookStatistics.sort((a, b) -> b.getQuantity().compareTo(a.getQuantity()));
       
       return new PurchaseStatisticsDTO(totalBooks, totalAmount, bookStatistics);
   }
   
   @Override
   @Transactional
   public OrderDTO updateOrderContactInfo(Long orderId, Long userId, String address, String phone) {
       // 获取订单
       Order order = orderDao.findById(orderId)
               .orElseThrow(() -> new RuntimeException("订单不存在"));
       
       // 验证订单是否属于当前用户
       if (!order.getUser().getId().equals(userId)) {
           throw new RuntimeException("无权限修改此订单");
       }
       
       // 只允许修改未支付订单的联系信息
       if (!"待支付".equals(order.getStatus()) && !"PENDING".equals(order.getStatus())) {
           throw new RuntimeException("只能修改待支付订单的联系信息");
       }
       
       // 验证输入参数
       if (address == null || address.trim().isEmpty()) {
           throw new RuntimeException("收货地址不能为空");
       }
       
       if (phone == null || phone.trim().isEmpty()) {
           throw new RuntimeException("联系电话不能为空");
       }
       
       // 更新联系信息
       order.setShippingAddress(address.trim());
       order.setContactPhone(phone.trim());
       
       // 保存更新
       Order savedOrder = orderDao.save(order);
       
       return orderMapper.toDTO(savedOrder);
   }

    // 在OrderServiceImpl中添加
    @Override
    @Transactional(readOnly = true)
    public BookSalesStatisticsDTO getBookSalesStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 获取指定时间范围内的已完成订单
            List<Order> completedOrders = orderDao.findCompletedOrdersByTimeRange(startTime, endTime);

            if (completedOrders.isEmpty()) {
                return new BookSalesStatisticsDTO(new ArrayList<>(), 0, 0, BigDecimal.ZERO);
            }

            // 用于存储每本书的销量统计
            Map<Long, BookSalesStatisticsDTO.BookSalesDTO> bookSalesMap = new HashMap<>();
            BigDecimal totalRevenue = BigDecimal.ZERO;
            Integer totalBooksSold = 0;

            // 遍历所有已完成的订单
            for (Order order : completedOrders) {
                totalRevenue = totalRevenue.add(order.getTotalAmount());

                // 遍历订单中的每个商品
                for (OrderItem item : order.getOrderItems()) {
                    Long bookId = item.getBookId();
                    totalBooksSold += item.getQuantity();

                    if (bookSalesMap.containsKey(bookId)) {
                        // 如果已存在，累加数量和收入
                        BookSalesStatisticsDTO.BookSalesDTO existingStats = bookSalesMap.get(bookId);
                        existingStats.setQuantitySold(existingStats.getQuantitySold() + item.getQuantity());
                        existingStats.setRevenue(existingStats.getRevenue().add(
                                item.getPrice().multiply(new BigDecimal(item.getQuantity()))));
                        existingStats.setOrderCount(existingStats.getOrderCount() + 1);
                    } else {
                        // 如果不存在，创建新的统计项
                        BigDecimal itemRevenue = item.getPrice().multiply(new BigDecimal(item.getQuantity()));

                        // 通过 BookService 获取书籍详细信息
                        String bookTitle = "未知书籍";
                        String bookAuthor = "未知作者";
                        String bookCover = "";
                        String publisher = "";
                        BigDecimal currentPrice = item.getPrice();                        try {
                            Optional<BookDTO> bookOpt = bookService.getBookByIdIncludingDeleted(bookId);
                            if (bookOpt.isPresent()) {
                                BookDTO book = bookOpt.get();
                                bookTitle = book.getTitle();
                                bookAuthor = book.getAuthor();
                                bookCover = book.getCover();
                                publisher = book.getPublisher();
                                currentPrice = book.getPrice();
                            }
                        } catch (Exception e) {
                            System.err.println("获取书籍信息失败: " + e.getMessage());
                        }

                        BookSalesStatisticsDTO.BookSalesDTO bookSales = new BookSalesStatisticsDTO.BookSalesDTO(
                                bookId,
                                bookTitle,
                                bookAuthor,
                                bookCover,
                                publisher,
                                currentPrice,
                                item.getQuantity(),
                                itemRevenue,
                                1
                        );
                        bookSalesMap.put(bookId, bookSales);
                    }
                }
            }

            // 将Map转换为List，并按销量降序排序
            List<BookSalesStatisticsDTO.BookSalesDTO> bookSales = new ArrayList<>(bookSalesMap.values());
            bookSales.sort((a, b) -> b.getQuantitySold().compareTo(a.getQuantitySold()));

            return new BookSalesStatisticsDTO(bookSales, completedOrders.size(), totalBooksSold, totalRevenue);
        } catch (Exception e) {
            throw new RuntimeException("获取销量统计失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserConsumptionStatisticsDTO getUserConsumptionStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 获取指定时间范围内的已完成订单
            List<Order> completedOrders = orderDao.findCompletedOrdersByTimeRange(startTime, endTime);

            if (completedOrders.isEmpty()) {
                return new UserConsumptionStatisticsDTO(new ArrayList<>(), 0, 0, BigDecimal.ZERO);
            }

            // 用于存储每个用户的消费统计
            Map<Long, UserConsumptionStatisticsDTO.UserConsumptionDTO> userConsumptionMap = new HashMap<>();
            BigDecimal totalRevenue = BigDecimal.ZERO;

            // 遍历所有已完成的订单
            for (Order order : completedOrders) {
                totalRevenue = totalRevenue.add(order.getTotalAmount());
                Long userId = order.getUser().getId();

                if (userConsumptionMap.containsKey(userId)) {
                    // 如果已存在，累加消费金额和订单数
                    UserConsumptionStatisticsDTO.UserConsumptionDTO existingStats = userConsumptionMap.get(userId);
                    existingStats.setTotalSpent(existingStats.getTotalSpent().add(order.getTotalAmount()));
                    existingStats.setOrderCount(existingStats.getOrderCount() + 1);

                    // 累加书籍数量
                    int booksInOrder = order.getOrderItems().stream()
                            .mapToInt(OrderItem::getQuantity)
                            .sum();
                    existingStats.setTotalBooksCount(existingStats.getTotalBooksCount() + booksInOrder);

                    // 重新计算平均订单金额
                    BigDecimal avgOrderValue = existingStats.getTotalSpent()
                            .divide(new BigDecimal(existingStats.getOrderCount()), 2, RoundingMode.HALF_UP);
                    existingStats.setAverageOrderValue(avgOrderValue);
                } else {
                    // 如果不存在，创建新的统计项
                    User user = order.getUser();

                    // 计算该订单的书籍总数
                    int booksInOrder = order.getOrderItems().stream()
                            .mapToInt(OrderItem::getQuantity)
                            .sum();

                    UserConsumptionStatisticsDTO.UserConsumptionDTO userConsumption =
                            new UserConsumptionStatisticsDTO.UserConsumptionDTO(
                                    userId,
                                    user.getAccount(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    order.getTotalAmount(),
                                    1,
                                    booksInOrder,
                                    order.getTotalAmount()  // 第一个订单的平均值就是订单金额
                            );
                    userConsumptionMap.put(userId, userConsumption);
                }
            }

            // 将Map转换为List，并按消费金额降序排序
            List<UserConsumptionStatisticsDTO.UserConsumptionDTO> userConsumptions =
                    new ArrayList<>(userConsumptionMap.values());
            userConsumptions.sort((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()));            return new UserConsumptionStatisticsDTO(
                    userConsumptions,
                    userConsumptions.size(),
                    completedOrders.size(),
                    totalRevenue
            );
        } catch (Exception e) {
            throw new RuntimeException("获取用户消费统计失败: " + e.getMessage(), e);
        }
    }
    
    // 分页方法实现
    @Override
    public Page<OrderDTO> getUserOrdersByPage(Long userId, Pageable pageable) {
        try {
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Page<Order> orderPage = orderDao.findByUserOrderByCreateTimeDesc(user, pageable);
            return orderPage.map(orderMapper::toDTO);
        } catch (Exception e) {
            throw new RuntimeException("获取用户订单分页失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<OrderDTO> searchUserOrdersByPage(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            Page<Order> orderPage;
            
            if (bookTitle != null && !bookTitle.trim().isEmpty() && startTime != null && endTime != null) {
                orderPage = orderDao.findUserOrdersByBookTitleAndTimeRange(userId, bookTitle, startTime, endTime, pageable);
            } else if (bookTitle != null && !bookTitle.trim().isEmpty()) {
                orderPage = orderDao.findUserOrdersByBookTitle(userId, bookTitle, pageable);
            } else if (startTime != null && endTime != null) {
                orderPage = orderDao.findUserOrdersByTimeRange(userId, startTime, endTime, pageable);
            } else {
                // 如果没有搜索条件，直接返回用户的所有订单
                User user = userDao.findById(userId)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                orderPage = orderDao.findByUserOrderByCreateTimeDesc(user, pageable);
            }
            
            return orderPage.map(orderMapper::toDTO);
        } catch (Exception e) {
            throw new RuntimeException("搜索用户订单分页失败: " + e.getMessage(), e);
        }
    }    @Override
    public Page<PurchaseStatisticsDTO> getUserPurchaseStatisticsByPage(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            // 获取用户已完成订单
            List<Order> allOrders = orderDao.findCompletedOrdersByUserAndTimeRange(userId, startTime, endTime);
            
            // 按书籍聚合统计
            Map<Long, PurchaseStatisticsDTO.BookStatisticsDTO> bookStatsMap = new HashMap<>();
            
            for (Order order : allOrders) {
                for (OrderItem item : order.getOrderItems()) {
                    Long bookId = item.getBookId();
                    PurchaseStatisticsDTO.BookStatisticsDTO existingStats = bookStatsMap.get(bookId);
                    
                    if (existingStats != null) {
                        // 更新现有统计
                        existingStats.setQuantity(existingStats.getQuantity() + item.getQuantity());
                        BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                        existingStats.setTotalPrice(existingStats.getTotalPrice().add(itemTotal));
                    } else {
                        // 创建新的书籍统计
                        Book book = bookDao.findById(bookId).orElse(null);
                        if (book != null) {
                            BigDecimal totalPrice = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                            PurchaseStatisticsDTO.BookStatisticsDTO bookStats = 
                                new PurchaseStatisticsDTO.BookStatisticsDTO(
                                    book.getTitle(),
                                    book.getAuthor(),
                                    book.getCover(),
                                    item.getQuantity(),
                                    totalPrice
                                );
                            bookStatsMap.put(bookId, bookStats);
                        }
                    }
                }
            }
            
            // 将Map转换为List并排序
            List<PurchaseStatisticsDTO.BookStatisticsDTO> allBookStats = new ArrayList<>(bookStatsMap.values());
            allBookStats.sort((a, b) -> b.getTotalPrice().compareTo(a.getTotalPrice())); // 按总价降序排序
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allBookStats.size());
            List<PurchaseStatisticsDTO.BookStatisticsDTO> pageContent = start >= allBookStats.size() ? 
                    new ArrayList<>() : allBookStats.subList(start, end);
            
            // 为分页的每本书创建一个PurchaseStatisticsDTO
            List<PurchaseStatisticsDTO> content = pageContent.stream()
                    .map(bookStats -> new PurchaseStatisticsDTO(
                            bookStats.getQuantity(),
                            bookStats.getTotalPrice(),
                            List.of(bookStats)
                    ))
                    .collect(Collectors.toList());
            
            return new org.springframework.data.domain.PageImpl<>(content, pageable, allBookStats.size());
        } catch (Exception e) {
            throw new RuntimeException("获取用户购买统计分页失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<OrderDTO> getAllOrdersByPage(Pageable pageable) {
        try {
            Page<Order> orderPage = orderDao.findAllByOrderByCreateTimeDesc(pageable);
            return orderPage.map(orderMapper::toDTO);
        } catch (Exception e) {
            throw new RuntimeException("获取所有订单分页失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<OrderDTO> searchAllOrdersByPage(String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            Page<Order> orderPage;
            
            if (bookTitle != null && !bookTitle.trim().isEmpty() && startTime != null && endTime != null) {
                orderPage = orderDao.findOrdersByBookTitleAndTimeRange(bookTitle, startTime, endTime, pageable);
            } else if (bookTitle != null && !bookTitle.trim().isEmpty()) {
                orderPage = orderDao.findOrdersByBookTitle(bookTitle, pageable);
            } else if (startTime != null && endTime != null) {
                orderPage = orderDao.findOrdersByTimeRange(startTime, endTime, pageable);
            } else {
                orderPage = orderDao.findAllByOrderByCreateTimeDesc(pageable);
            }
            
            return orderPage.map(orderMapper::toDTO);
        } catch (Exception e) {
            throw new RuntimeException("搜索所有订单分页失败: " + e.getMessage(), e);
        }
    }    @Override
    public Page<BookSalesStatisticsDTO> getBookSalesStatisticsByPage(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            // 获取已完成订单的分页（按书籍分组前需要先获取所有相关订单）
            List<Order> allOrders = orderDao.findCompletedOrdersByTimeRange(startTime, endTime);
            
            // 按书籍分组统计销量
            Map<Long, BookSalesStatisticsDTO.BookSalesDTO> bookSalesMap = new HashMap<>();
            
            for (Order order : allOrders) {
                for (OrderItem item : order.getOrderItems()) {
                    Long bookId = item.getBookId();
                    BookSalesStatisticsDTO.BookSalesDTO existingSales = bookSalesMap.get(bookId);
                    
                    if (existingSales != null) {
                        // 更新现有统计
                        existingSales.setQuantitySold(existingSales.getQuantitySold() + item.getQuantity());
                        existingSales.setRevenue(existingSales.getRevenue().add(
                                item.getPrice().multiply(new BigDecimal(item.getQuantity()))));
                        existingSales.setOrderCount(existingSales.getOrderCount() + 1);
                    } else {
                        // 创建新的销量统计
                        Book book = bookDao.findById(bookId).orElse(null);
                        if (book != null) {
                            BigDecimal revenue = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                            BookSalesStatisticsDTO.BookSalesDTO bookSales = new BookSalesStatisticsDTO.BookSalesDTO(
                                    book.getId(),
                                    book.getTitle(),
                                    book.getAuthor(),
                                    book.getCover(),
                                    book.getPublisher(),
                                    book.getPrice(),
                                    item.getQuantity(),
                                    revenue,
                                    1
                            );
                            bookSalesMap.put(bookId, bookSales);
                        }
                    }
                }
            }
            
            // 将Map转换为List并排序
            List<BookSalesStatisticsDTO.BookSalesDTO> allBookSales = new ArrayList<>(bookSalesMap.values());
            allBookSales.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue())); // 按收入降序排序
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allBookSales.size());
            List<BookSalesStatisticsDTO.BookSalesDTO> pageContent = start >= allBookSales.size() ? 
                    new ArrayList<>() : allBookSales.subList(start, end);
            
            // 为每个书籍销量创建一个BookSalesStatisticsDTO
            List<BookSalesStatisticsDTO> content = pageContent.stream()
                    .map(bookSales -> new BookSalesStatisticsDTO(
                            List.of(bookSales),
                            1, // 这是单本书的统计，订单数就是该书的订单数
                            bookSales.getQuantitySold(),
                            bookSales.getRevenue()
                    ))
                    .collect(Collectors.toList());
            
            return new org.springframework.data.domain.PageImpl<>(content, pageable, allBookSales.size());
        } catch (Exception e) {
            throw new RuntimeException("获取书籍销量统计分页失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<UserConsumptionStatisticsDTO> getUserConsumptionStatisticsByPage(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            // 获取已完成订单
            List<Order> allOrders = orderDao.findCompletedOrdersByTimeRange(startTime, endTime);
            
            // 按用户分组统计消费
            Map<Long, UserConsumptionStatisticsDTO.UserConsumptionDTO> userConsumptionMap = new HashMap<>();
            
            for (Order order : allOrders) {
                Long userId = order.getUser().getId();
                UserConsumptionStatisticsDTO.UserConsumptionDTO existingConsumption = userConsumptionMap.get(userId);
                
                if (existingConsumption != null) {
                    // 更新现有统计
                    existingConsumption.setTotalSpent(existingConsumption.getTotalSpent().add(order.getTotalAmount()));
                    existingConsumption.setOrderCount(existingConsumption.getOrderCount() + 1);
                    
                    // 累加书籍数量
                    int booksInOrder = order.getOrderItems().stream()
                            .mapToInt(OrderItem::getQuantity)
                            .sum();
                    existingConsumption.setTotalBooksCount(existingConsumption.getTotalBooksCount() + booksInOrder);
                    
                    // 重新计算平均订单金额
                    BigDecimal avgOrderValue = existingConsumption.getTotalSpent()
                            .divide(new BigDecimal(existingConsumption.getOrderCount()), 2, RoundingMode.HALF_UP);
                    existingConsumption.setAverageOrderValue(avgOrderValue);
                } else {
                    // 创建新的消费统计
                    User user = order.getUser();
                    int booksInOrder = order.getOrderItems().stream()
                            .mapToInt(OrderItem::getQuantity)
                            .sum();
                    
                    UserConsumptionStatisticsDTO.UserConsumptionDTO userConsumption =
                            new UserConsumptionStatisticsDTO.UserConsumptionDTO(
                                    userId,
                                    user.getAccount(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    order.getTotalAmount(),
                                    1,
                                    booksInOrder,
                                    order.getTotalAmount()
                            );
                    userConsumptionMap.put(userId, userConsumption);
                }
            }
            
            // 将Map转换为List并排序
            List<UserConsumptionStatisticsDTO.UserConsumptionDTO> allUserConsumptions = new ArrayList<>(userConsumptionMap.values());
            allUserConsumptions.sort((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent())); // 按消费金额降序排序
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allUserConsumptions.size());
            List<UserConsumptionStatisticsDTO.UserConsumptionDTO> pageContent = start >= allUserConsumptions.size() ? 
                    new ArrayList<>() : allUserConsumptions.subList(start, end);
            
            // 为每个用户消费创建一个UserConsumptionStatisticsDTO
            List<UserConsumptionStatisticsDTO> content = pageContent.stream()
                    .map(userConsumption -> new UserConsumptionStatisticsDTO(
                            List.of(userConsumption),
                            1, // 这是单个用户的统计
                            userConsumption.getOrderCount(),
                            userConsumption.getTotalSpent()
                    ))
                    .collect(Collectors.toList());
              return new org.springframework.data.domain.PageImpl<>(content, pageable, allUserConsumptions.size());
        } catch (Exception e) {
            throw new RuntimeException("获取用户消费统计分页失败: " + e.getMessage(), e);
        }
    }    @Override
    public StatisticsPageResponseDTO<PurchaseStatisticsDTO> getUserPurchaseStatisticsWithGlobal(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            // 获取分页数据
            Page<PurchaseStatisticsDTO> page = getUserPurchaseStatisticsByPage(userId, startTime, endTime, pageable);
            
            // 计算全局统计信息
            List<Order> allOrders = orderDao.findCompletedOrdersByUserAndTimeRange(userId, startTime, endTime);
            
            int totalOrderCount = 0;
            int totalBooksSold = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            Set<Long> uniqueBookIds = new HashSet<>();
            
            for (Order order : allOrders) {
                totalOrderCount++;
                for (OrderItem item : order.getOrderItems()) {
                    totalBooksSold += item.getQuantity();
                    totalRevenue = totalRevenue.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    uniqueBookIds.add(item.getBookId());
                }
            }
              StatisticsPageResponseDTO.GlobalStatistics globalStats = new StatisticsPageResponseDTO.GlobalStatistics(
                    totalOrderCount,
                    totalBooksSold,
                    totalRevenue,
                    uniqueBookIds.size()
            );
              return new StatisticsPageResponseDTO<PurchaseStatisticsDTO>(page, globalStats);
        } catch (Exception e) {
            throw new RuntimeException("获取用户购买统计（含全局统计）失败: " + e.getMessage(), e);
        }
    }

    @Override
    public StatisticsPageResponseDTO<BookSalesStatisticsDTO> getBookSalesStatisticsWithGlobal(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            // 获取分页数据
            Page<BookSalesStatisticsDTO> page = getBookSalesStatisticsByPage(startTime, endTime, pageable);
            
            // 计算全局统计信息
            List<Order> allOrders = orderDao.findCompletedOrdersByTimeRange(startTime, endTime);
            
            int totalOrderCount = 0;
            int totalBooksSold = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            Set<Long> uniqueBookIds = new HashSet<>();
            
            for (Order order : allOrders) {
                totalOrderCount++;
                for (OrderItem item : order.getOrderItems()) {
                    totalBooksSold += item.getQuantity();
                    totalRevenue = totalRevenue.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    uniqueBookIds.add(item.getBookId());
                }
            }
              StatisticsPageResponseDTO.GlobalStatistics globalStats = new StatisticsPageResponseDTO.GlobalStatistics(
                    totalOrderCount,
                    totalBooksSold,
                    totalRevenue,
                    uniqueBookIds.size()
            );
              return new StatisticsPageResponseDTO<BookSalesStatisticsDTO>(page, globalStats);
        } catch (Exception e) {
            throw new RuntimeException("获取书籍销量统计（含全局统计）失败: " + e.getMessage(), e);
        }
    }

    @Override
    public StatisticsPageResponseDTO<UserConsumptionStatisticsDTO> getUserConsumptionStatisticsWithGlobal(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            // 获取分页数据
            Page<UserConsumptionStatisticsDTO> page = getUserConsumptionStatisticsByPage(startTime, endTime, pageable);
            
            // 计算全局统计信息
            List<Order> allOrders = orderDao.findCompletedOrdersByTimeRange(startTime, endTime);
            
            int totalOrderCount = 0;
            int totalBooksSold = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            Set<Long> uniqueUserIds = new HashSet<>();
            
            for (Order order : allOrders) {
                totalOrderCount++;
                uniqueUserIds.add(order.getUser().getId());
                for (OrderItem item : order.getOrderItems()) {
                    totalBooksSold += item.getQuantity();
                    totalRevenue = totalRevenue.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                }
            }
              StatisticsPageResponseDTO.GlobalStatistics globalStats = new StatisticsPageResponseDTO.GlobalStatistics(
                    totalOrderCount,
                    totalBooksSold,
                    totalRevenue,
                    uniqueUserIds.size() // 用户消费统计中，这个字段表示消费用户总数
            );
            
            return new StatisticsPageResponseDTO<UserConsumptionStatisticsDTO>(page, globalStats);
        } catch (Exception e) {
            throw new RuntimeException("获取用户消费统计（含全局统计）失败: " + e.getMessage(), e);
        }
    }
}