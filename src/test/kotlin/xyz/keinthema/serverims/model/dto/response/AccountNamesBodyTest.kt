package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import xyz.keinthema.serverims.model.dto.request.RequestAccountNames

class AccountNamesBodyTest{
    @Test
    fun testMap() {
        val t = AccountNamesBody(mapOf(Pair(1L, "a")))
        println(Json.encodeToString(t))
    }
//    @Test
//    fun testSet() {
//        val t = Json.decodeFromString<RequestAccountNames>("{\"userIdSet\":[\"1\",\"2\",\"2\"]}")
//        println(t)
//    }
}