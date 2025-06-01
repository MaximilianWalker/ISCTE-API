package core

@Target(AnnotationTarget.FUNCTION)
annotation class Put(vararg val path: String)