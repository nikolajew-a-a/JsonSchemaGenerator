package org.example.generator

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*
import org.example.generator.annotations.EnumEntries

object JsonSchemaGenerator {

    private const val FIELD_TYPE = "type"
    private const val FIELD_PROPERTIES = "properties"
    private const val FIELD_ITEMS = "items"
    private const val FIELD_ENUM = "enum"

    fun accept(descriptor: SerialDescriptor): JsonObject = acceptInternal(
        descriptor = descriptor,
        annotations = emptyList()
    )

    @OptIn(ExperimentalSerializationApi::class)
    private fun acceptInternal(
        descriptor: SerialDescriptor,
        annotations: List<Annotation>
    ): JsonObject = with(descriptor.kind) {
        when (this) {
            SerialKind.ENUM -> acceptEnum(annotations =  annotations)
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

            StructureKind.CLASS -> acceptClass(descriptor = descriptor)
            StructureKind.LIST -> acceptList(descriptor = descriptor)
            StructureKind.MAP -> this.throwNotSupported()
            StructureKind.OBJECT -> this.throwNotSupported()

            PolymorphicKind.SEALED -> acceptSealedClass()
            PolymorphicKind.OPEN -> this.throwNotSupported()
        }
    }

    private fun acceptEnum(annotations: List<Annotation>): JsonObject {
        val enumEntriesAnnotation = annotations.filterIsInstance<EnumEntries>()
        check(enumEntriesAnnotation.size == 1) {
            "Enum class should be annotated by single @EnumEntries annotation"
        }
        val enumEntriesNames = enumEntriesAnnotation.first().entries

        return JsonObject(
            mapOf(
                FIELD_TYPE to JsonPrimitive(JsonSchemaElementType.STRING.value),
                FIELD_ENUM to JsonArray(enumEntriesNames.map(::JsonPrimitive))
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
            val elementAnnotations = descriptor.getElementAnnotations(index)
            properties[elementName] = acceptInternal(descriptor = child, annotations = elementAnnotations)
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
