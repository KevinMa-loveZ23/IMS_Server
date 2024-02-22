package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import kotlin.reflect.full.createInstance

/**
 * Class that implements this interface
 * SHOULD implement static function void(): T
 * in which T is a void instantiated object of itself.
 * (using Kotlin means class should have a companion object
 * with the function void(): T in it)
 *
 * function isVoid(): Boolean is in fact unnecessary,
 * just reminding the static function void(): T
 * */
@Serializable
sealed interface ResponseDataBody<T> {
    fun isVoid(): Boolean
//    fun void(): T
//    companion object {
////        fun void(): <T: ResponseDataBody>
//        inline fun <reified T : ResponseDataBody<T>> create(): T{
//            return T::class.createInstance()
//        }
//        fun <T : ResponseDataBody<T>> void(clazz: Class<T>): T {
//            return create(clazz.kotlin)
//        }
//    }
}