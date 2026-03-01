package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.BookDao;
import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.entity.BookInfo;
import com.ebookstore.ebookstorebackend.repository.BookInfoRepository;
import com.ebookstore.ebookstorebackend.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BookDaoImpl implements BookDao {
    
    private static final Logger logger = LoggerFactory.getLogger(BookDaoImpl.class);
    
    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    
    @Autowired
    public BookDaoImpl(BookRepository bookRepository, BookInfoRepository bookInfoRepository) {
        this.bookRepository = bookRepository;
        this.bookInfoRepository = bookInfoRepository;
    }
    
    /**
     * 从 MongoDB 获取 cover 和 description，填充到单个 Book 对象
     */
    private void fillBookInfo(Book book) {
        if (book == null) return;
        
        logger.info("===== 从 MongoDB 获取单本书籍信息 =====");
        logger.info("查询 Book ID: {}", book.getId());
        
        bookInfoRepository.findById(book.getId()).ifPresent(info -> {
            book.setCover(info.getCover());
            book.setDescription(info.getDescription());
            logger.info("成功填充书籍 [{}] {} 的封面和描述", book.getId(), book.getTitle());
        });
        
        logger.info("===== MongoDB 查询完成 =====");
    }
    /**
     * 批量从 MongoDB 获取信息并填充到 Book 列表
     */
    private void fillBookInfoList(List<Book> books) {
        if (books == null || books.isEmpty()) return;
        
        // 1. 收集所有 bookId
        List<Long> bookIds = books.stream()
                .map(Book::getId)
                .collect(Collectors.toList());
        
        logger.info("===== 从 MongoDB 批量获取书籍信息 =====");
        logger.info("查询的 Book IDs: {}", bookIds);
        
        // 2. 批量查询 MongoDB
        List<BookInfo> bookInfos = bookInfoRepository.findAllById(bookIds);
        logger.info("MongoDB 返回记录数: {}", bookInfos.size());
        
        // 3. 转为 Map 方便查找
        Map<Long, BookInfo> infoMap = bookInfos.stream()
                .collect(Collectors.toMap(BookInfo::getBookId, info -> info));
        
        // 4. 填充数据
        books.forEach(book -> {
            BookInfo info = infoMap.get(book.getId());
            if (info != null) {
                book.setCover(info.getCover());
                book.setDescription(info.getDescription());
                logger.info("填充书籍 [{}] {}", book.getId(), book.getTitle());
            } else {
                logger.warn("书籍 [{}] 在 MongoDB 中没有找到信息", book.getId());
            }
        });
        
        logger.info("===== MongoDB 查询完成 =====");
    }
    
    @Override
    public List<Book> findAll() {
        List<Book> books = bookRepository.findAll();
        fillBookInfoList(books);
        return books;
    }
    
    @Override
    public Page<Book> findAll(Pageable pageable) {
        Page<Book> page = bookRepository.findAll(pageable);
        fillBookInfoList(page.getContent());
        return page;
    }
    
    @Override
    public Optional<Book> findById(Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        bookOpt.ifPresent(this::fillBookInfo);
        return bookOpt;
    }
    
    @Override
    public List<Book> findByTitleContainingIgnoreCase(String title) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
        fillBookInfoList(books);
        return books;
    }
    
    @Override
    public List<Book> findByAuthorContainingIgnoreCase(String author) {
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        fillBookInfoList(books);
        return books;
    }
    
    @Override
    public Book save(Book book) {
        // 1. 先保存到 MySQL（获取生成的 ID）
        Book savedBook = bookRepository.save(book);
        
        // 2. 保存到 MongoDB
        BookInfo bookInfo = new BookInfo(
                savedBook.getId(),
                book.getCover(),
                book.getDescription()
        );
        bookInfoRepository.save(bookInfo);
        
        // 3. 确保返回对象包含 cover 和 description
        savedBook.setCover(book.getCover());
        savedBook.setDescription(book.getDescription());
        
        return savedBook;
    }
    
    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
        bookInfoRepository.deleteById(id);  // 同步删除 MongoDB 数据
    }
    
    // 软删除相关方法实现
    @Override
    public List<Book> findByDeletedFalseOrderByIdAsc() {
        List<Book> books = bookRepository.findByDeletedFalseOrderByIdAsc();
        fillBookInfoList(books);
        return books;
    }
    
    @Override
    public Page<Book> findByDeletedFalse(Pageable pageable) {
        Page<Book> page = bookRepository.findByDeletedFalse(pageable);
        fillBookInfoList(page.getContent());
        return page;
    }
    
    @Override
    public List<Book> findByTitleContainingAndDeletedFalseOrderByIdAsc(String title) {
        List<Book> books = bookRepository.findByTitleContainingAndDeletedFalseOrderByIdAsc(title);
        fillBookInfoList(books);
        return books;
    }
    
    @Override
    public List<Book> findByAuthorContainingAndDeletedFalseOrderByIdAsc(String author) {
        List<Book> books = bookRepository.findByAuthorContainingAndDeletedFalseOrderByIdAsc(author);
        fillBookInfoList(books);
        return books;
    }
    
    @Override
    public Optional<Book> findByIdAndDeletedFalse(Long id) {
        Optional<Book> bookOpt = bookRepository.findByIdAndDeletedFalse(id);
        bookOpt.ifPresent(this::fillBookInfo);
        return bookOpt;
    }
    
    @Override
    public List<Book> findByIdIn(List<Long> ids) {
        List<Book> books = bookRepository.findByIdIn(ids);
        fillBookInfoList(books);
        return books;
    }

    @Override
    public List<Book> findByTagsIn(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有未删除的书籍，筛选出包含指定标签的
        List<Book> allBooks = bookRepository.findByDeletedFalseOrderByIdAsc();
        
        List<Book> matchedBooks = allBooks.stream()
                .filter(book -> {
                    String bookTags = book.getTags();
                    if (bookTags == null || bookTags.isEmpty()) {
                        return false;
                    }
                    // 检查书籍是否包含任意一个搜索标签
                    for (String tag : tags) {
                        if (bookTags.contains(tag)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // 填充 MongoDB 数据（cover 和 description）
        fillBookInfoList(matchedBooks);
        
        return matchedBooks;
    }
}
