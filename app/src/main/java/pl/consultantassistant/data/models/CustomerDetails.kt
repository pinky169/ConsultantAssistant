package pl.mymonat.models

data class CustomerDetails(
    var customerID: String = "",
    var hairColor: String = "",
    var dyedHair: String = "",
    var typeOfHair: String = "",
    var washingFrequency: String = "",
    var hairCondition: String = "",
    var routineActivities: String = "",
    var termoProtection: String = "",
    var purposeOfTreatment: String = "",
    var moreDetails: String = ""
) {

    // Used when calling updateChildren
    // which requires Map<String, Any> argument
    // Updates only mentioned fields
    fun toMap(): Map<String, Any> {
        val result: HashMap<String, Any> = HashMap()
        result["hairColor"] = hairColor
        result["dyedHair"] = dyedHair
        result["typeOfHair"] = typeOfHair
        result["washingFrequency"] = washingFrequency
        result["hairCondition"] = hairCondition
        result["routineActivities"] = routineActivities
        result["termoProtection"] = termoProtection
        result["purposeOfTreatment"] = purposeOfTreatment
        result["moreDetails"] = moreDetails
        return result
    }
}