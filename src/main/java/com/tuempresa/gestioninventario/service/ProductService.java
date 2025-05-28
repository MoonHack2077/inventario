package com.tuempresa.gestioninventario.service;

import com.tuempresa.gestioninventario.entity.Product;
import com.tuempresa.gestioninventario.entity.Warehouse;
import com.tuempresa.gestioninventario.repository.ProductRepository;
import com.tuempresa.gestioninventario.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
// Ya no necesitamos LocalDateTime para entry/exit dates en los métodos de stock.

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, WarehouseRepository warehouseRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    // --- Métodos de Validación Privados ---
    private void validateProductData(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("El objeto producto no puede ser nulo.");
        }
        if (!StringUtils.hasText(product.getName())) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (product.getQuantity() == null || product.getQuantity() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser nula o negativa.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser nulo o negativo.");
        }
        // No hay SKU, Barcode, lowStockThreshold para validar aquí.
    }

    // Ya no necesitamos validateUniqueSkuAndBarcode

    // --- Métodos de Servicio ---
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser un número positivo.");
        }
        return productRepository.findById(id);
    }

    @Transactional
    public Product createProduct(Product product, Long warehouseId) {
        validateProductData(product); // Validaciones generales del producto

        if (warehouseId == null || warehouseId <= 0) {
            throw new IllegalArgumentException("El ID del almacén es obligatorio y debe ser positivo.");
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado con ID: " + warehouseId));

        product.setWarehouse(warehouse);
        // Ya no se setea entryDate aquí

        return productRepository.save(product);
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, Product productDetails, Long warehouseId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del producto a actualizar debe ser un número positivo.");
        }
        validateProductData(productDetails); // Validaciones generales del producto

        return productRepository.findById(id)
            .map(existingProduct -> {
                if (warehouseId == null || warehouseId <= 0) {
                    throw new IllegalArgumentException("El ID del almacén es obligatorio y debe ser positivo para la actualización.");
                }
                Warehouse warehouse = warehouseRepository.findById(warehouseId)
                        .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado con ID: " + warehouseId));

                existingProduct.setName(productDetails.getName());
                existingProduct.setDescription(productDetails.getDescription());
                existingProduct.setPrice(productDetails.getPrice());
                existingProduct.setQuantity(productDetails.getQuantity());
                existingProduct.setCategory(productDetails.getCategory());
                existingProduct.setWarehouse(warehouse);

                return productRepository.save(existingProduct);
            });
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del producto a eliminar debe ser un número positivo.");
        }
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Product> recordEntry(Long productId, int quantityToAdd) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("El ID del producto para registrar entrada debe ser un número positivo.");
        }
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("La cantidad para registrar entrada debe ser positiva.");
        }
        return productRepository.findById(productId)
            .map(product -> {
                product.setQuantity(product.getQuantity() + quantityToAdd);
                // Ya no se actualiza entryDate
                return productRepository.save(product);
            });
    }

    @Transactional
    public Optional<Product> recordExit(Long productId, int quantityToSubtract) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("El ID del producto para registrar salida debe ser un número positivo.");
        }
        if (quantityToSubtract <= 0) {
            throw new IllegalArgumentException("La cantidad para registrar salida debe ser positiva.");
        }
        return productRepository.findById(productId)
            .map(product -> {
                if (product.getQuantity() < quantityToSubtract) {
                    throw new IllegalArgumentException("Stock insuficiente ("+ product.getQuantity() +") para el producto: " + product.getName() + " al intentar sacar " + quantityToSubtract);
                }
                product.setQuantity(product.getQuantity() - quantityToSubtract);
                // Ya no se actualiza exitDate
                return productRepository.save(product);
            });
    }
}