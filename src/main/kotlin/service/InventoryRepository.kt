package com.august.service

import com.august.domain.model.Wine

interface InventoryRepository {
    // 재고 등록
    fun register(wine: Wine): Boolean

    // 재고 삭제
    fun delete(id: String)
    // 입고
    fun store(id: String, quantity: Int): Boolean

    // 출고
    fun retrieve(id: String, quantity: Int): Boolean
    // 전체 조회
    fun getAll(): List<Wine>
}
