package pl.consultantassistant.data.models

data class Partner(var name: String = "",
              var surname: String = "",
              var email: String = "") {

    // Used when calling updateChildren
    // which requires Map<String, Any> argument
    // Updates only mentioned fields
    fun toMap(): Map<String, Any> {
        val result: HashMap<String, Any> = HashMap()
        result["name"] = name
        result["surname"] = surname
        result["email"] = email
        return result
    }
}