package core

@Target(AnnotationTarget.FUNCTION)
annotation class Get(vararg val path: String)