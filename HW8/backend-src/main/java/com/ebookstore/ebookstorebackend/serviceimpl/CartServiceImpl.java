package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.dto.CartDTO;
import com.ebookstore.ebookstorebackend.dto.mapper.CartMapper;
import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.entity.BookInfo;
import com.ebookstore.ebookstorebackend.entity.Cart;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.dao.BookDao;
import com.ebookstore.ebookstorebackend.dao.CartDao;
import com.ebookstore.ebookstorebackend.dao.UserDao;
import com.ebookstore.ebookstorebackend.repository.BookInfoRepository;
import com.ebookstore.ebookstorebackend.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class CartServiceImpl implements CartService{
    
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);    @Autowired
    private CartDao cartDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private BookDao bookDao;
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private BookInfoRepository bookInfoRepository;

    @Override
    public List<CartDTO> getCartItems(String account) {
        User user = userDao.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Cart> cartItems = cartDao.findByUser(user);
        
        // 从 MongoDB 批量获取书籍封面和描述信息
        if (!cartItems.isEmpty()) {
            List<Long> bookIds = cartItems.stream()
                    .map(cart -> cart.getBook().getId())
                    .collect(Collectors.toList());
            
            logger.info("===== 购物车：从 MongoDB 获取书籍信息 =====");
            logger.info("查询的 Book IDs: {}", bookIds);
            
            List<BookInfo> bookInfos = bookInfoRepository.findAllById(bookIds);
            logger.info("MongoDB 返回记录数: {}", bookInfos.size());
            
            Map<Long, BookInfo> infoMap = bookInfos.stream()
                    .collect(Collectors.toMap(BookInfo::getBookId, info -> info));
            
            // 填充每个购物车项中的书籍信息
            cartItems.forEach(cart -> {
                BookInfo info = infoMap.get(cart.getBook().getId());
                if (info != null) {
                    cart.getBook().setCover(info.getCover());
                    cart.getBook().setDescription(info.getDescription());
                    logger.info("填充书籍 [{}] {} 的封面信息", cart.getBook().getId(), cart.getBook().getTitle());
                } else {
                    logger.warn("书籍 [{}] 在 MongoDB 中没有找到信息", cart.getBook().getId());
                }
            });
            logger.info("===== MongoDB 查询完成 =====");
        }
        
        return cartItems.stream()
                .map(cartMapper::toDTO)
                .collect(Collectors.toList());
    }      
    
    @Override
    @Transactional
    public CartDTO addToCart(String account, Long bookId, Integer quantity) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查书籍是否存在且未删除
        Book book = bookDao.findByIdAndDeletedFalse(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在或已下架"));

        Optional<Cart> existingCart = cartDao.findByUserIdAndBookId(user.getId(), bookId);

        int newQuantity = quantity;
        if (existingCart.isPresent()) {
            newQuantity = existingCart.get().getQuantity() + quantity;
        }
        
        // 检查库存是否足够
        if (newQuantity > book.getStock()) {
            throw new RuntimeException("库存不足。当前库存: " + book.getStock() + "本，您尝试添加后的数量: " + newQuantity + "本");
        }

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            cart.setQuantity(newQuantity);
        } else {
            cart = new Cart(user, book, quantity);
        }

        cartDao.save(cart);
        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    // 保证数据库要么成功要么失败
    public CartDTO updateCartItem(String account, Long bookId, Integer quantity) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));

        Cart cart = cartDao.findByUserIdAndBookId(user.getId(), bookId).orElseThrow(() -> new RuntimeException("购物车中不存在该商品"));

        // 检查书籍是否存在且未删除
        Book book = bookDao.findByIdAndDeletedFalse(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在或已下架"));
        
        // 检查库存是否足够
        if (quantity > book.getStock()) {
            throw new RuntimeException("库存不足。当前库存: " + book.getStock() + "本，您尝试设置的数量: " + quantity + "本");
        }

        cart.setQuantity(quantity);
        cartDao.save(cart);
        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(String account, Long bookId) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));
        cartDao.deleteByUserIdAndBookId(user.getId(), bookId);
    }

    @Override
    @Transactional
    public void clearCart(String account) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));
        cartDao.deleteByUserId(user.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasDeletedBooks(String account) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Cart> cartItems = cartDao.findByUser(user);
        
        for (Cart cartItem : cartItems) {
            Book book = cartItem.getBook();
            if (book.isDeleted()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Long> getDeletedBookIds(String account) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Cart> cartItems = cartDao.findByUser(user);
        
        return cartItems.stream()
                .filter(cartItem -> cartItem.getBook().isDeleted())
                .map(cartItem -> cartItem.getBook().getId())
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void removeDeletedBooks(String account) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Cart> cartItems = cartDao.findByUser(user);
        
        List<Cart> deletedItems = cartItems.stream()
                .filter(cartItem -> cartItem.getBook().isDeleted())
                .collect(Collectors.toList());
          for (Cart deletedItem : deletedItems) {
            cartDao.deleteByUserIdAndBookId(user.getId(), deletedItem.getBook().getId());
        }
    }
      @Override
    @Transactional(readOnly = true)
    public boolean canCheckout(String account) {
        return !hasDeletedBooks(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> checkCartStock(String account) {
        User user = userDao.findByAccount(account).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Cart> cartItems = cartDao.findByUser(user);
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> insufficientStockItems = new ArrayList<>();
        boolean hasInsufficientStock = false;
        
        for (Cart cartItem : cartItems) {
            Book book = cartItem.getBook();
            // 跳过已删除的书籍，这些会在另一个检查中处理
            if (book.isDeleted()) {
                continue;
            }
            
            if (cartItem.getQuantity() > book.getStock()) {
                hasInsufficientStock = true;
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("bookId", book.getId());
                itemInfo.put("bookTitle", book.getTitle());
                itemInfo.put("requestedQuantity", cartItem.getQuantity());
                itemInfo.put("availableStock", book.getStock());
                insufficientStockItems.add(itemInfo);
            }
        }
        
        result.put("hasInsufficientStock", hasInsufficientStock);
        result.put("insufficientStockItems", insufficientStockItems);
        result.put("canCheckout", !hasInsufficientStock && !hasDeletedBooks(account));
        
        return result;
    }

}
