package com.app.product.service;

import com.app.product.dto.ProductRequest;
import com.app.product.dto.ProductResponse;
import com.app.product.entity.Product;
import com.app.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Business laptop")
                .price(new BigDecimal("50000.00"))
                .stock(10)
                .category("Electronics")
                .imageUrl("https://example.com/laptop.png")
                .build();

        request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Business laptop");
        request.setPrice(new BigDecimal("50000.00"));
        request.setStock(10);
        request.setCategory("Electronics");
        request.setImageUrl("https://example.com/laptop.png");
    }

    @Test
    void getAllProducts_ReturnsProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
        when(productRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        var result = productService.getAllProducts(0, 10, "name");

        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void searchProducts_ReturnsMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.searchByKeyword("Laptop", pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        var result = productService.searchProducts("Laptop", 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository).searchByKeyword("Laptop", pageable);
    }

    @Test
    void getByCategory_ReturnsProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByCategory("Electronics", pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        var result = productService.getByCategory("Electronics", 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("Electronics", result.getContent().get(0).getCategory());
        verify(productRepository).findByCategory("Electronics", pageable);
    }

    @Test
    void getProductById_WhenFound_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("50000.00"), result.getPrice());
    }

    @Test
    void getProductById_WhenMissing_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.getProductById(99L));

        assertEquals("Product not found: 99", exception.getMessage());
    }

    @Test
    void createProduct_SavesAndReturnsProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse result = productService.createProduct(request);

        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("50000.00"), result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenFound_UpdatesAndReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        request.setName("Updated Laptop");
        request.setStock(20);

        ProductResponse result = productService.updateProduct(1L, request);

        assertEquals("Updated Laptop", result.getName());
        assertEquals(20, result.getStock());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_WhenMissing_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.updateProduct(99L, request));

        assertEquals("Product not found: 99", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenFound_DeletesProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenMissing_ThrowsException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(99L));

        assertEquals("Product not found: 99", exception.getMessage());
        verify(productRepository, never()).deleteById(99L);
    }
}
