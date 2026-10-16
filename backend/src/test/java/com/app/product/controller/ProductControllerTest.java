package com.app.product.controller;

import com.app.common.dto.ApiResponse;
import com.app.common.dto.PageResponse;
import com.app.product.dto.ProductRequest;
import com.app.product.dto.ProductResponse;
import com.app.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private ProductService productService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productService))
                .build();
    }

    private ProductResponse sampleProduct() {
        return ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .description("Business laptop")
                .price(new BigDecimal("50000.00"))
                .stock(10)
                .category("Electronics")
                .imageUrl("https://example.com/laptop.png")
                .build();
    }

    private PageResponse<ProductResponse> samplePage() {
        return PageResponse.<ProductResponse>builder()
                .content(List.of(sampleProduct()))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
    }

    private ProductRequest sampleRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Business laptop");
        request.setPrice(new BigDecimal("50000.00"));
        request.setStock(10);
        request.setCategory("Electronics");
        request.setImageUrl("https://example.com/laptop.png");
        return request;
    }

    @Test
    void getAllProducts_ReturnsOk() throws Exception {
        when(productService.getAllProducts(0, 10, "createdAt"))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Laptop"));

        verify(productService).getAllProducts(0, 10, "createdAt");
    }

    @Test
    void search_ReturnsMatchingProducts() throws Exception {
        when(productService.searchProducts("Laptop", 0, 10))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Laptop"));

        verify(productService).searchProducts("Laptop", 0, 10);
    }

    @Test
    void getByCategory_ReturnsProducts() throws Exception {
        when(productService.getByCategory("Electronics", 0, 10))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].category")
                        .value("Electronics"));

        verify(productService).getByCategory("Electronics", 0, 10);
    }

    @Test
    void getById_ReturnsProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProduct());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Laptop"));

        verify(productService).getProductById(1L);
    }

    @Test
    void create_ReturnsCreated() throws Exception {
        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(sampleProduct());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Laptop"));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void update_ReturnsOk() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenReturn(sampleProduct());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void delete_ReturnsOk() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService).deleteProduct(1L);
    }
}
