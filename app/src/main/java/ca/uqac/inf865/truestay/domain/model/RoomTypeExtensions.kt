package ca.uqac.inf865.truestay.domain.model

import ca.uqac.inf865.truestay.R

/**
 * Extensions for RoomType
 */
fun RoomType.getLabelRes(): Int {
    return when (this) {
        RoomType.BEDROOM -> R.string.room_type_bedroom
        RoomType.LIVING_ROOM -> R.string.room_type_living_room
        RoomType.KITCHEN -> R.string.room_type_kitchen
        RoomType.BATHROOM -> R.string.room_type_bathroom
        RoomType.TOILET -> R.string.room_type_toilet
        RoomType.ENTRANCE -> R.string.room_type_entrance
        RoomType.HALLWAY -> R.string.room_type_hallway
        RoomType.DINING_ROOM -> R.string.room_type_dining_room
        RoomType.OFFICE -> R.string.room_type_office
        RoomType.LAUNDRY_ROOM -> R.string.room_type_laundry_room
        RoomType.STORAGE_ROOM -> R.string.room_type_storage_room
        RoomType.GARAGE -> R.string.room_type_garage
        RoomType.BASEMENT -> R.string.room_type_basement
        RoomType.ATTIC -> R.string.room_type_attic
        RoomType.BALCONY -> R.string.room_type_balcony
        RoomType.TERRACE -> R.string.room_type_terrace
        RoomType.GARDEN -> R.string.room_type_garden
        RoomType.VERANDA -> R.string.room_type_veranda
        RoomType.STAIRCASE -> R.string.room_type_staircase
        RoomType.OTHER -> R.string.room_type_other
    }
}

