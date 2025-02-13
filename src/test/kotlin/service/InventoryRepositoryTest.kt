package service

import com.august.domain.model.Wine
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InventoryRepositoryTest {
    private val wine = Wine(
        id = "1",
        wineryName = "Chateau Margaux",
        countryCode = "FR",
        vintage = 2015,
        price = 500,
        quantity = 10
    )

    @Test
    fun `와인을 등록하면 재고 목록에 와인이 추가됨`() {
        val repository = FakeInventoryRepository()
        assertTrue { repository.register(wine) }
    }

    @Test
    fun `와인을 삭제하면 재고 목록에서 와인이 삭제됨`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        repository.delete("1")
        assertTrue(repository.wines.isEmpty())
    }

    @Test
    fun `와인 재고를 추가하면 추가한 수량만큼 재고가 증가함`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        val originalQuantity = wine.quantity
        repository.store(wine.id, quantity = 4)
        assertEquals(repository.wines.find { it.id == wine.id }?.quantity, originalQuantity + 4)
    }

    @Test
    fun `와인 재고를 출고하면 출고한 수량만큼 재고가 감소함`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        val originalQuantity = wine.quantity
        repository.retrieve("1", quantity = 4)
        assertEquals(repository.wines.find { it.id == wine.id }?.quantity, originalQuantity - 4)
    }
}
