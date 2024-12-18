package org.example

import kotlinx.serialization.*
import org.example.ElementType.Companion.COLLECTION_VALUE
import org.example.ElementType.Companion.MOVIE_VALUE
import org.example.ElementType.Companion.SERIAL_VALUE
import org.example.generator.annotations.AnyOf
import org.example.generator.annotations.EnumEntries


@Serializable
data class ExampleData(
    val name: String,
    val id: Int,
    val nestedData: NestedData,
    @AnyOf
    val sealedData: SealedData,
    val someList: List<String>,
    @EnumEntries(entries = [COLLECTION_VALUE, MOVIE_VALUE])
    val type: ElementType,
)

@Serializable
data class NestedData(
    val title: String,
    val isBest: Boolean,
    @EnumEntries(entries = [COLLECTION_VALUE, MOVIE_VALUE, SERIAL_VALUE])
    val elementType: ElementType
)

@Serializable
sealed class SealedData {
    @Serializable

    data class SealedDataChild1(val id: Int) : SealedData()

    @Serializable

    data class SealedDataChild2(val id: Int) : SealedData()

}

enum class ElementType {
    COLLECTION,
    MOVIE,
    SERIAL;

    companion object {
        const val COLLECTION_VALUE = "COLLECTION"
        const val MOVIE_VALUE = "MOVIE"
        const val SERIAL_VALUE = "SERIAL"
    }

}