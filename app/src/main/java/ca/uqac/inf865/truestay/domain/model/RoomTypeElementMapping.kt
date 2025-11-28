package ca.uqac.inf865.truestay.domain.model

/**
 * Mapping entre les types de pièces et les éléments qu'elles contiennent par défaut
 */
object RoomTypeElementMapping {

    /**
     * Retourne la liste des types d'éléments par défaut pour un type de pièce donné
     */
    fun getDefaultElementsForRoomType(roomType: RoomType): List<ElementType> {
        return when (roomType) {
            RoomType.BEDROOM -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR
            )
            RoomType.LIVING_ROOM -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR
            )
            RoomType.KITCHEN -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR,
                ElementType.EQUIPMENT
            )
            RoomType.BATHROOM -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR,
                ElementType.EQUIPMENT
            )
            RoomType.TOILET -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR,
                ElementType.EQUIPMENT
            )
            RoomType.ENTRANCE -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.DOOR
            )
            RoomType.HALLWAY -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.DOOR
            )
            RoomType.DINING_ROOM -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR
            )
            RoomType.OFFICE -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR
            )
            RoomType.LAUNDRY_ROOM -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR,
                ElementType.EQUIPMENT
            )
            RoomType.STORAGE_ROOM -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.DOOR
            )
            RoomType.GARAGE -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.DOOR
            )
            RoomType.BASEMENT -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR
            )
            RoomType.ATTIC -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR
            )
            RoomType.BALCONY -> listOf(
                ElementType.FLOOR,
                ElementType.DOOR
            )
            RoomType.TERRACE -> listOf(
                ElementType.FLOOR
            )
            RoomType.GARDEN -> listOf(
                ElementType.FLOOR
            )
            RoomType.VERANDA -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.WINDOW,
                ElementType.DOOR
            )
            RoomType.STAIRCASE -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING
            )
            RoomType.OTHER -> listOf(
                ElementType.FLOOR,
                ElementType.WALL,
                ElementType.CEILING,
                ElementType.DOOR
            )
        }
    }
}

