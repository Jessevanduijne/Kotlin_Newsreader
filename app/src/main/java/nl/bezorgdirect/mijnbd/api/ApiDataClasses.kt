package nl.bezorgdirect.mijnbd.api


data class Feed (val Id: Int?, val Name: String?)

data class Location (val Id: String?, val Latitude: Float?, val Longitude: Float?, val Address: String?, val PostalCode: String?,
                     val Place: String?, val IsWareHouse: Boolean?)

data class User (val id: String?, val emailAddress: String?, val token: String?, val phoneNumber: String?,
                          val home: Location?, val dateOfBirth: String?, val range: Int?, val vehicle: Int?, val vehicleDisplayName: String?, val fare: Float?, val totalEarnings: Float?)

data class Availability (val Id: String?, val DelivererId: String?, val Date:  String?, val StartTime: String?, val EndTime: String?)

data class BDNotification (val Id: String?, val DelivererId: String?, val DeliveryId: String?,val CreatedAt: String?, val AcceptedAt: String?,
                           val RefusedAt: String?, val ExpiredAt: String?, val Status: Int?, val StatusDisplayName: String?)


data class Delivery (val Id: String?, val DelivererId: String?, val CustomerPhoneNumber: String?, val DueDate: String?, val Vehicle: Int?, val StartedAtId: String?,
                     val WarehouseDistaceInKilometers: Float?, val WarehouseETA: String?, val WarehousePickUpAt:  String?, val CustomerDistanceInKilometers: Float?,
                     val CustomerETA: String?, val CustomerId: String?, val CurrentId: String?, val DeliveredAt: String?, val Price: Float?, val tip: Float?,
                     val PaymentMethod: Int?, val PaymenMethodDisplayName: String?, val Status: Int?, val StatusDisplayName: String?, val Warehouse: Location,
                     val Customer: Location, val Current: Location)
