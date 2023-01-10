package com.rizzle.sdk.faas.uistylers


/**
 * Annotation for fields or properties in data classes to be used to declare
 * that it is not provided by server and is a client side property or field.
 * */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class ClientOnly
