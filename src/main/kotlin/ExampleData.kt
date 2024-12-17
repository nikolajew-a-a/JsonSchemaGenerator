package org.example

import kotlinx.serialization.Serializable


@Serializable
data class ExampleData(
    val name: String,
    val id: Int,
    val nestedData: NestedData,
    val someList: List<String>,
    val type: ElementType,
)

@Serializable
data class NestedData(
    val title: String,
    val isBest: Boolean,
    val elementType: ElementType
)

enum class ElementType {
    COLLECTION, MOVIE, SERIAL;
}