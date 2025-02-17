package com.august.service

import com.august.domain.model.Wine

interface InventoryRepository {
    // 재고 등록
    fun register(wine: Wine): Boolean

    // 재고 삭제
    fun delete(id: String): Boolean

    // 입고
    fun store(id: String, quantity: Int): Boolean

    // 출고
    fun retrieve(id: String, quantity: Int): Boolean

    //검색
    fun findWineByFilter(filterType: InventoryFilterType): List<Wine>?

    // 전체 조회
    fun getAll(): List<Wine>
}

sealed class InventoryFilterType {
    data class WineryName(val name: String) : InventoryFilterType()
    data class CountryCode(val code: String) : InventoryFilterType()
    data class Vintage(val year: Int) : InventoryFilterType()
    data class Price(val min:Int, val max:Int) : InventoryFilterType()
    data class Quantity(val num: Int) : InventoryFilterType()
}