package core

@Target(AnnotationTarget.FUNCTION)
annotation class Post(vararg val path: String)