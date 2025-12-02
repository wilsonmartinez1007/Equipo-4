package com.univalle.inventory.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class InventoryViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var inventoryViewModel: InventoryViewModel

    @Mock
    lateinit var inventoryRepository: InventoryRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        inventoryViewModel = InventoryViewModel(inventoryRepository)
    }

    @Test
    fun `test para getListInventory`() = runBlocking {
        val mockList = listOf(
            Inventory(id = 1, name = "Item 1", price = 100, quantity = 2),
            Inventory(id = 2, name = "Item 2", price = 200, quantity = 3)
        )
        `when`(inventoryRepository.getListInventory()).thenReturn(mockList)
        inventoryViewModel.getListInventory()
        assertEquals(mockList, inventoryViewModel.listInventory.value)
        assertEquals(false, inventoryViewModel.progressState.value)
    }

    @Test
    fun `test para saveInventory`() = runBlocking {
        val inventory = Inventory(
            id = 1,
            name = "Item 1",
            price = 10,
            quantity = 5
        )
        var messageReceived: String? = null
        val messageLambda: (String) -> Unit = { msg -> messageReceived = msg }
        inventoryViewModel.saveInventory(inventory, messageLambda)
        verify(inventoryRepository).saveInventory(inventory, messageLambda)
        assertEquals(false, inventoryViewModel.progressState.value)
    }


}
