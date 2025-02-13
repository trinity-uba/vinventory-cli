package service

import com.august.domain.model.Wine
import com.august.service.InventoryRepository

class FakeInventoryRepository : InventoryRepository {
    val wines = mutableListOf<Wine>()

    override fun register(wine: Wine): Boolean {
        return wines.add(wine)
    }

    override fun delete(id: String) {
        wines.removeIf { wine -> wine.id == id }
    }

    override fun store(id: String, quantity: Int): Boolean {
        val wine = wines.find { it.id == id }
        if (wine == null) {
            return false
        } else {
            val num = wine.quantity + quantity
            val index = wines.indexOfFirst { it.id == id }
            wines.add(index, wine.copy(quantity = num))
            return true
        }
    }

    override fun retrieve(id: String, quantity: Int): Boolean {
        val wine = wines.find { it.id == id }
        if (wine == null) {
            return false
        } else {
            val num = wine.quantity - quantity
            val index = wines.indexOfFirst { it.id == id }
            wines.add(index, wine.copy(quantity = num))
            return true
        }
    }

    override fun getAll(): List<Wine> {
        return wines
    }
}