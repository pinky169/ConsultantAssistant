package pl.consultantassistant.data.models

data class Product(var customerID: String = "", var productID: String = "", var productName: String = "") {

    // Used when calling updateChildren
    // which requires Map<String, Any> argument
    // Updates only mentioned fields
    fun toMap(): Map<String, Any> {
        val result: HashMap<String, Any> = HashMap()
        result["productName"] = productName
        return result
    }
}