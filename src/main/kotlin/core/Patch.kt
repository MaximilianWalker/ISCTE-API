package core

@Target(AnnotationTarget.FUNCTION)
annotation class Patch(vararg val path: String)