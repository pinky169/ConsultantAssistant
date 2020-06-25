package pl.consultantassistant.data.models

data class Photo(var customerID: String = "", var photoID: String = "", var photoURL: String = "", var photoExtension: String = "", var photoLastModifiedDate: String = "") {

    // Used when calling updateChildren
    // which requires Map<String, Any> argument
    // Updates only mentioned fields
    fun toMap(): Map<String, Any> {
        val result: HashMap<String, Any> = HashMap()
        result["photoURL"] = photoURL
        return result
    }
}