package core

@Target(AnnotationTarget.FUNCTION)
annotation class Delete(vararg val path: String)