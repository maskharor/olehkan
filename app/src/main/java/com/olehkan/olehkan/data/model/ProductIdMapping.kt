package com.olehkan.olehkan.data.model

import com.olehkan.olehkan.data.repository.DummyProductRepository

object ProductIdMapping {
    val idToUuid: Map<Int, String> = mapOf(
        1 to "929ef347-698d-4ca4-8156-c1513564f68b",
        2 to "997c3c54-221c-4964-9f43-bee00db272d0",
        3 to "6bbf590d-bc05-4f5b-82c8-50b562a318e1",
        4 to "03961344-3f9f-4634-bdce-11ce48772bac",
        5 to "45fc6b8a-31a9-4df5-89bf-9caa4aedc91a",
    )

    val uuidToId: Map<String, Int> = idToUuid.entries.associate { (k, v) -> v to k }

    fun toUuid(id: Int): String {
        return idToUuid[id]
            ?: DummyProductRepository.dummyProducts.find { it.id == id }?.uuid?.takeIf { it.isNotBlank() }
            ?: "929ef347-698d-4ca4-8156-c1513564f68b"
    }

    fun toId(uuid: String): Int {
        return uuidToId[uuid]
            ?: DummyProductRepository.dummyProducts.find { it.uuid.equals(uuid, ignoreCase = true) }?.id
            ?: 1
    }
}
