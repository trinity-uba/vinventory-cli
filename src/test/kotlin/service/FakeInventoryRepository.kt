package service

import com.august.domain.model.Wine
import com.august.service.InventoryFilterType
import com.august.service.InventoryFilterType.*
import com.august.service.InventoryRepository
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class FakeInventoryRepository : InventoryRepository {
    private val wines = mutableListOf<Wine>()
    private val lock = ReentrantLock() // 동시성 제어를 위한 락 추가

    override fun register(wine: Wine): Boolean {
        if (wines.any { it.id == wine.id }) { // 중복 방지
            return false
        }
        return wines.add(wine)
    }

    override fun delete(id: String): Boolean {
        if (findWineById(id) == null) throw WineNotFoundException("Cannot find wine ID : $id")
        return wines.removeIf { wine -> wine.id == id }
    }

    override fun store(id: String, quantity: Int): Boolean {
        if (lock.tryLock(1, TimeUnit.SECONDS).not()) return false // Lock 울 획득하지 못하면 실패 처리
        try {
            val (wine, index) = findWineAndIndexById(id)
            return adjustQuantity(index, wine, wine.quantity + quantity)
        } finally {
            lock.unlock()
        }
    }

    override fun retrieve(id: String, quantity: Int): Boolean {
        if (lock.tryLock(1, TimeUnit.SECONDS).not()) return false
        try {
            val (wine, index) = findWineAndIndexById(id)
            val adjustedQuantity = wine.quantity - quantity
            if (adjustedQuantity < 0) {
                // 예외가 발생한 경우 원래 상태를 유지하는가? 출고 후 데이터가 실제로 어떻게 변화하는가?
                throw NotEnoughStockException(stockLeft = wine.quantity)
            }
            return adjustQuantity(index, wine, adjustedQuantity)
        } finally {
            lock.unlock()
        }
    }

    override fun findWineByFilter(filterType: InventoryFilterType): List<Wine> {
        return when (filterType) {
            is WineryName -> {
                wines.filter { it.wineryName == filterType.name }
            }

            is CountryCode -> {
                wines.filter { it.countryCode == filterType.code }
            }

            is Vintage -> {
                wines.filter { it.vintage == filterType.year }
            }

            is Price -> {
                wines.filter { it.price < filterType.max && it.price > filterType.min }
            }

            is Quantity -> {
                wines.filter { it.quantity == filterType.num }
            }
        }
    }


    override fun getAll(): List<Wine> {
        // 단순히 리스트를 반환하는 것이지만, 리스트가 변경될 때 정확한 데이터를 리턴하는지 검증할 필요가 있다.
        // 데이터가 추가되거나 삭제된 후 getAll이 올바른 상태를 유지하는지 확인해야 함
        return wines
    }

    private fun findWineById(id: String): Wine? {
        return wines.find { it.id == id } // 순회 1
    }

    private fun findWineAndIndexById(id: String): Pair<Wine, Int> {
        val result = wines.withIndex().find {
            it.value.id == id
        }
        if (result == null) throw WineNotFoundException("Cannot find wine with ID : $id")
        return result.value to result.index
    }

    private fun adjustQuantity(index: Int, wine: Wine, quantity: Int): Boolean {
        wines[index] = wine.copy(quantity = quantity)
        return true
    }
}

class WineNotFoundException(message: String) : IllegalStateException(message)

class NotEnoughStockException(
    private val stockLeft: Int, message: String = "Not enough items. Only $stockLeft items left.",
) : IllegalStateException(message)