package org.example.generator

import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class EnumEntries(val entries: Array<String>)