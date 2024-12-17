package org.example

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import org.example.generator.JsonSchemaGenerator

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = "   "
        classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
    }
    val jsonSchema = prettyJson.encodeToString(
        JsonSchemaGenerator.accept(ExampleData.serializer().descriptor)
    )
    println(jsonSchema)
}