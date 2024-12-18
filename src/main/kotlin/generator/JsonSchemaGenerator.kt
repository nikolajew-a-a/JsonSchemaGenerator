package org.example.generator

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*
import org.example.generator.annotations.AnyOf
import org.example.generator.annotations.EnumEntries

object JsonSchemaGenerator {

    private const val FIELD_TYPE = "type"
    private const val FIELD_PROPERTIES = "properties"
    private const val FIELD_ITEMS = "items"
    private const val FIELD_ENUM = "enum"
    private const val FIELD_ANY_OF = "anyOf"
    private const val FIELD_VERSION = "\$schema"
    private const val DEFAULT_VERSION = "http://json-schema.org/draft-04/schema"

    fun accept(descriptor: SerialDescriptor): JsonObject {
        return JsonObject(
            JsonObject(mapOf(FIELD_VERSION to JsonPrimitive(DEFAULT_VERSION))) +
                    acceptInternal(descriptor = descriptor, annotations = emptyList())
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun acceptInternal(
        descriptor: SerialDescriptor,
        annotations: List<Annotation>
    ): JsonObject = with(descriptor.kind) {
        when (this) {
            SerialKind.ENUM -> acceptEnum(annotations = annotations)
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

            PolymorphicKind.SEALED -> acceptSealedClass(descriptor = descriptor, annotations = annotations)
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
                FIELD_ITEMS to acceptInternal(childDescriptors.first(), emptyList())
            )
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun acceptSealedClass(
        descriptor: SerialDescriptor,
        annotations: List<Annotation>
    ): JsonObject {
        val anyOfAnnotations = annotations.filterIsInstance<AnyOf>()
        check(anyOfAnnotations.size == 1) {
            "Sealed class should be annotated by single @AnyOf annotation"
        }

        val elementDescriptors = descriptor.elementDescriptors.toList()
        check(elementDescriptors.size == 2) {
            """
            Sealed class should have 2 elementDescriptors:
            1. For property "type". This property's descriptor.kind is PrimitiveKind.STRING
            2. For subclasses. For this descriptor.kind is SerialKind.CONTEXTUAL
            """
        }
        val subclassesJsonObjects = elementDescriptors[1].elementDescriptors.map { acceptInternal(it, emptyList()) }
        return JsonObject(mapOf(FIELD_ANY_OF to JsonArray(subclassesJsonObjects)))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun SerialKind.throwNotSupported(): Nothing =
        throw IllegalArgumentException("Descriptor with type ${this::class.simpleName} not supported")

}
