package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import kotlin.reflect.full.createInstance

@Serializable
sealed interface ResponseDataBody<T> {
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