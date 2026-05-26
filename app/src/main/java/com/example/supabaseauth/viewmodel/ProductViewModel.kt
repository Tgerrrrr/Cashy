package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository =
        ProductRepository()

    /* ================= PRODUCT LIST ================= */

    private val _productList =
        MutableStateFlow<List<Product>>(
            emptyList()
        )

    val productList:
            StateFlow<List<Product>> =
        _productList

    /* ================= SELECTED PRODUCT ================= */

    private val _selectedProduct =
        MutableStateFlow<Product?>(null)

    val selectedProduct:
            StateFlow<Product?> =
        _selectedProduct

    /* ================= LOADING ================= */

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading:
            StateFlow<Boolean> =
        _isLoading

    /* ================= MESSAGE ================= */

    private val _message =
        MutableStateFlow<String?>(null)

    val message:
            StateFlow<String?> =
        _message

    /* ================= INIT ================= */

    init {

        loadProducts()
    }

    /* ================= LOAD PRODUCTS ================= */

    fun loadProducts() {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                _productList.value =
                    repository.getProducts()

            } catch (e: Exception) {

                _message.value =
                    e.message ?: "Failed to load products"

                e.printStackTrace()

            } finally {

                _isLoading.value = false
            }
        }
    }

    /* ================= LOAD PRODUCT BY ID ================= */

    fun loadProductById(
        id: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                _selectedProduct.value =
                    repository.getProductById(id)

            } catch (e: Exception) {

                _message.value =
                    e.message ?: "Failed to load product"

                e.printStackTrace()

            } finally {

                _isLoading.value = false
            }
        }
    }

    /* ================= INSERT PRODUCT ================= */

    fun insertProduct(
        product: Product
    ) {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                repository.addProduct(product)

                _message.value =
                    "Product added successfully"

                loadProducts()

            } catch (e: Exception) {

                _message.value =
                    e.message ?: "Failed to add product"

                e.printStackTrace()

            } finally {

                _isLoading.value = false
            }
        }
    }

    /* ================= UPDATE PRODUCT ================= */

    fun updateProduct(
        product: Product
    ) {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                repository.updateProduct(product)

                _message.value =
                    "Product updated successfully"

                loadProducts()

            } catch (e: Exception) {

                _message.value =
                    e.message ?: "Failed to update product"

                e.printStackTrace()

            } finally {

                _isLoading.value = false
            }
        }
    }

    /* ================= DELETE PRODUCT ================= */

    fun deleteProduct(
        id: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                repository.deleteProduct(id)

                _message.value =
                    "Product deleted successfully"

                loadProducts()

            } catch (e: Exception) {

                _message.value =
                    e.message ?: "Failed to delete product"

                e.printStackTrace()

            } finally {

                _isLoading.value = false
            }
        }
    }

    /* ================= CLEAR MESSAGE ================= */

    fun clearMessage() {

        _message.value = null
    }

    /* ================= CLEAR SELECTED PRODUCT ================= */

    fun clearSelectedProduct() {

        _selectedProduct.value = null
    }
}