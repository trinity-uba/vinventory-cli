package service

import com.august.domain.model.Wine
import com.august.service.InventoryRepository

class FakeInventoryRepository : InventoryRepository {
    val wines = mutableListOf<Wine>()

    override fun register(wine: Wine): Boolean {
        if (wines.any{ it.id == wine.id}) { // 중복 방지
            return false
        }
        return wines.add(wine)
    }

    override fun delete(id: String): Boolean {
        if (findWineById(id) == null) throw WineNotFoundException("Cannot find wine ID : $id")
        return wines.removeIf { wine -> wine.id == id }
    }

    override fun store(id: String, quantity: Int): Boolean {
        val wine = findWineById(id) ?: return false
        return adjustQuantity(wine, wine.quantity + quantity)
    }

    override fun retrieve(id: String, quantity: Int): Boolean {
        val wine = findWineById(id) ?: return false
        val adjustedQuantity = wine.quantity - quantity
        if (adjustedQuantity < 0) {
            // 예외가 발생한 경우 원래 상태를 유지하는가? 출고 후 데이터가 실제로 어떻게 변화하는가?
            throw NotEnoughStockException(stockLeft = wine.quantity)
        }
        return adjustQuantity(wine, adjustedQuantity)
    }

    private fun findWineById(id: String): Wine? {
        return wines.find { it.id == id }
    }

    private fun adjustQuantity(wine: Wine, quantity: Int): Boolean {
        val index = wines.indexOfFirst { it.id == wine.id }
        wines[index] = wine.copy(quantity = quantity)
        return true
    }

    override fun getAll(): List<Wine> {
        // 단순히 리스트를 반환하는 것이지만, 리스트가 변경될 때 정확한 데이터를 리턴하는지 검증할 필요가 있다.
        // 데이터가 추가되거나 삭제된 후 getAll이 올바른 상태를 유지하는지 확인해야 함
        return wines
    }
}

class WineNotFoundException(message: String) : IllegalStateException(message)

class NotEnoughStockException(
    private val stockLeft: Int, message: String = "Not enough items. Only $stockLeft items left."
) : IllegalStateException(message)