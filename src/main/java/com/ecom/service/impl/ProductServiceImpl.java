package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Sale configuration properties
    @Value("${sale.active:false}")
    private boolean saleActive;
    @Value("${sale.discount:0}")
    private int saleDiscount;
    @Value("${sale.start}")
    private String saleStart;
    @Value("${sale.end}")
    private String saleEnd;
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    // Check if sale is active based on date range
    public boolean isSaleActive() {
        logger.info("Checking sale status...");
        logger.info("sale.active: {}", saleActive);
        logger.info("sale.start: {}", saleStart);
        logger.info("sale.end: {}", saleEnd);

        if (!saleActive) {
            logger.info("Sale is not active because sale.active is false");
            return false;
        }

        LocalDate now = LocalDate.now();
        logger.info("Current date: {}", now);

        try {
            LocalDate start = LocalDate.parse(saleStart);
            LocalDate end = LocalDate.parse(saleEnd);
            logger.info("Parsed sale start date: {}", start);
            logger.info("Parsed sale end date: {}", end);

            boolean isActive = !now.isBefore(start) && !now.isAfter(end);
            logger.info("Sale active status: {}", isActive);
            return isActive;
        } catch (Exception e) {
            logger.error("Error parsing sale dates: {}", e.getMessage());
            return false; // Fallback to false if date parsing fails
        }
    }

    // Apply effective discount (sale or product-specific, whichever is higher)
    private void applyEffectiveDiscount(Product product) {
        int effectiveDiscount = product.getDiscount();
        if (isSaleActive()) {
            effectiveDiscount = Math.max(effectiveDiscount, saleDiscount);
        }
        double discountAmount = product.getPrice() * (effectiveDiscount / 100.0);
        product.setDiscountPrice(product.getPrice() - discountAmount);
    }

    @Override
    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        applyEffectiveDiscount(savedProduct); // Apply sale discount if active
        return productRepository.save(savedProduct); // Save again with updated discountPrice
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(this::applyEffectiveDiscount); // Apply sale discount to all
        return products;
    }

    @Override
    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> page = productRepository.findAll(pageable);
        page.forEach(this::applyEffectiveDiscount); // Apply sale discount
        return page;
    }

    @Override
    public Boolean deleteProduct(Integer id) {
        Product product = productRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(product)) {
            productRepository.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            applyEffectiveDiscount(product); // Apply sale discount
        }
        return product;
    }

    @Override
    public Product updateProduct(Product product, MultipartFile image) {
        Product dbProduct = getProductById(product.getId());

        String imageName = image.isEmpty() ? dbProduct.getImage() : image.getOriginalFilename();

        dbProduct.setTitle(product.getTitle());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setImage(imageName);
        dbProduct.setIsActive(product.getIsActive());
        dbProduct.setDiscount(product.getDiscount());

        // Apply effective discount (sale or product-specific)
        applyEffectiveDiscount(dbProduct);

        Product updateProduct = productRepository.save(dbProduct);

        if (!ObjectUtils.isEmpty(updateProduct)) {
            if (!image.isEmpty()) {
                try {
                    File saveFile = new ClassPathResource("static/img").getFile();
                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
                            + image.getOriginalFilename());
                    Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return product;
        }
        return null;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        List<Product> products = null;
        if (ObjectUtils.isEmpty(category)) {
            products = productRepository.findByIsActiveTrue();
        } else {
            products = productRepository.findByCategory(category);
        }
        if (products != null) {
            products.forEach(this::applyEffectiveDiscount); // Apply sale discount
        }
        return products;
    }

    @Override
    public List<Product> findByProductId(Integer productId) {
        return productRepository.findById(productId).map(List::of).orElse(List.of());
    }

    @Override
    public List<Product> searchProduct(String ch) {
        List<Product> products = productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
        products.forEach(this::applyEffectiveDiscount); // Apply sale discount
        return products;
    }

    @Override
    public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> page = productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
        page.forEach(this::applyEffectiveDiscount); // Apply sale discount
        return page;
    }

    @Override
    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> pageProduct = null;

        if (ObjectUtils.isEmpty(category)) {
            pageProduct = productRepository.findByIsActiveTrue(pageable);
        } else {
            pageProduct = productRepository.findByCategory(pageable, category);
        }
        pageProduct.forEach(this::applyEffectiveDiscount); // Apply sale discount
        return pageProduct;
    }

    @Override
    public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> pageProduct = productRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,
                ch, pageable);
        pageProduct.forEach(this::applyEffectiveDiscount); // Apply sale discount
        return pageProduct;
    }
}