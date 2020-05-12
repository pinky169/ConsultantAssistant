package pl.mymonat.models

import java.io.Serializable

data class Customer(var customerID: String = "", var name: String = "", var surname: String = "", var email: String = "", var userLevel: String = ""): Serializable {

    // Used when calling updateChildren
    // which requires Map<String, Any> argument
    // Updates only mentioned fields
    fun toMap(): Map<String, Any> {
        val result: HashMap<String, Any> = HashMap()
        result["name"] = name
        result["surname"] = surname
        result["email"] = email
        result["userLevel"] = userLevel
        return result
    }
}