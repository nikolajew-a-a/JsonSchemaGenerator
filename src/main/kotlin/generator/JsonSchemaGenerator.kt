package org.example.generator

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*

object JsonSchemaGenerator {

    private const val FIELD_TYPE = "type"
    private const val FIELD_PROPERTIES = "properties"
    private const val FIELD_ITEMS = "items"
    private const val FIELD_ENUM = "enum"

    @OptIn(ExperimentalSerializationApi::class)
    fun accept(descriptor: SerialDescriptor): JsonObject = with(descriptor.kind) {
        when (this) {
            SerialKind.ENUM -> acceptEnum(descriptor)
            SerialKind.CONTEXTUAL -> this.throwNotSupported()

            PrimitiveKind.BOOLEAN -> acceptBoolean()
            PrimitiveKind.BYTE,
            PrimitiveKind.SHORT,
            PrimitiveKind.INT,
            PrimitiveKind.LONG -> acceptInteger()

            PrimitiveKind.FLOAT,
            PrimitiveKind.DOUBLE -> acceptNumber()

            PrimitiveKind.STRING -> acceptString()
            PrimitiveKind.CHAR -> this.throwNotSupported()

            StructureKind.CLASS -> acceptClass(descriptor)
            StructureKind.LIST -> acceptList(descriptor)
            StructureKind.MAP -> this.throwNotSupported()
            StructureKind.OBJECT -> this.throwNotSupported()

            PolymorphicKind.SEALED -> acceptSealedClass()
            PolymorphicKind.OPEN -> this.throwNotSupported()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun acceptEnum(descriptor: SerialDescriptor): JsonObject {
        val allElementNames = (0 until descriptor.elementsCount).map(descriptor::getElementName)
        return JsonObject(
            mapOf(
                FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.STRING.value),
                FIELD_ENUM to JsonArray(allElementNames.map(::JsonPrimitive))
            )
        )
    }

    private fun acceptBoolean(): JsonObject {
        return JsonObject(
            mapOf(FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.BOOLEAN.value))
        )
    }

    private fun acceptInteger(): JsonObject {
        return JsonObject(
            mapOf(FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.INTEGER.value))
        )
    }

    private fun acceptNumber(): JsonObject {
        return JsonObject(
            mapOf(FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.NUMBER.value))
        )
    }

    private fun acceptString(): JsonObject {
        return JsonObject(
            mapOf(FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.STRING.value))
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun acceptClass(descriptor: SerialDescriptor): JsonObject {
        val properties: MutableMap<String, JsonObject> = mutableMapOf()

        descriptor.elementDescriptors.forEachIndexed { index, child ->
            val elementName = descriptor.getElementName(index)
            properties[elementName] = accept(child)
        }

        return JsonObject(
            mapOf(
                FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.OBJECT.value),
                FIELD_PROPERTIES to JsonObject(properties)
            )
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun acceptList(descriptor: SerialDescriptor): JsonObject {
        val childDescriptors = descriptor.elementDescriptors.toList()
        check(childDescriptors.size == 1) {
            "Array descriptor has returned inconsistent number of elements: expected 1, found ${childDescriptors.size}"
        }
        return JsonObject(
            mapOf(
                FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.ARRAY.value),
                FIELD_ITEMS to accept(childDescriptors.first())
            )
        )
    }

    private fun acceptSealedClass(): JsonObject {
        throw IllegalArgumentException("Sealed classes not supported yet")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun SerialKind.throwNotSupported(): Nothing =
        throw IllegalArgumentException("Descriptor with type ${this::class.simpleName} not supported")

}
