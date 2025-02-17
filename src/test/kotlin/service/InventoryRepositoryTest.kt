package service

import com.august.domain.model.Wine
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun `이미 재고 목록에 있는 와인을 추가할 경우 추가 실패`() {
        val repository = FakeInventoryRepository()
        assertTrue { repository.register(wine) }
        assertFalse { repository.register(wine) } // 2번째 등록 시 실패
    }

    @Test
    fun `와인을 삭제하면 재고 목록에서 와인이 삭제됨`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        repository.delete("1")
        assertTrue(repository.getAll().isEmpty())
    }

    @Test
    fun `존재하지 않는 재고를 삭제할 경우 삭제 실패, WineNotFoundException 발생`() {
        val repository = FakeInventoryRepository()
        assertThrows<WineNotFoundException> {
            repository.delete("1") // 아무것도 없는채로 삭제
        }
    }

    @Test
    fun `와인 재고를 추가하면 추가한 수량만큼 재고가 증가함`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        val originalQuantity = wine.quantity
        repository.store(wine.id, quantity = 4)
        assertEquals(repository.getAll().find { it.id == wine.id }?.quantity, originalQuantity + 4)
    }

    @Test
    fun `존재하지 않는 와인에 대해서 와인 재고를 추가할 경우, WineNotFoundException 발생`() {
        val repository = FakeInventoryRepository()
        assertThrows<WineNotFoundException> { repository.store("1", 4) }
    }

    @Test
    fun `와인 재고를 출고하면 출고한 수량만큼 재고가 감소함`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        val originalQuantity = wine.quantity
        repository.retrieve("1", quantity = 4)
        assertEquals(repository.getAll().find { it.id == wine.id }?.quantity, originalQuantity - 4)
    }

    @Test
    fun `와인 재고를 출고할때 존재하는 수량보다 많이 출고를 시도할 경우 NotEnoughStockException`(){
        val repository = FakeInventoryRepository()
        repository.register(wine)
        val originalQuantity = wine.quantity
        assertThrows<NotEnoughStockException> {
            repository.retrieve("1", quantity = originalQuantity + 1)
        }
    }

    @Test
    fun `존재하지 않는 와인에 대해서 와인 재고를 출고할 경우, WineNotFoundException 발생`() {
        val repository = FakeInventoryRepository()
        assertThrows<WineNotFoundException> { repository.retrieve("1", quantity = 4) }
    }

    @Test
    fun `와인 재고를 추가한 후 getAll 호출시 추가된 수량이 반영됨`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        assertTrue { repository.getAll().size == 1 }
    }

    @Test
    fun `와인 재고를 삭제한 후 getAll 호출시 삭제된 수량이 반영됨`() {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        repository.delete(wine.id)
        assertTrue { repository.getAll().isEmpty() }
    }

    @Test
    fun `멀티스레드 환경에서도 재고가 일관성을 유지해야 한다`() = runTest {
        val repository = FakeInventoryRepository()
        repository.register(wine)
        val jobs = List(100) {
            launch {
                repository.store("1", 1)
                repository.retrieve("1", 1)
            }
        }
        jobs.forEach { it.join() }
        assertEquals(10, repository.getAll().find { it.id == "1" }?.quantity)
    }
}
