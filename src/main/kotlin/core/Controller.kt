package core

@Target(AnnotationTarget.CLASS)
annotation class Controller(vararg val path: String)