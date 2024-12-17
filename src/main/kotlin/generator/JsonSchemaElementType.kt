package org.example.generator

enum class JsonSchemaElementType(val value: String) {
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    OBJECT("object"),
    ARRAY("array"),
    BOOLEAN("boolean");
}