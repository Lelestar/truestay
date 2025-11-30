import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

const db = admin.firestore();

/**
 * Type definitions matching the Kotlin models
 */
interface Room {
  id: string;
  name: string;
  type: string;
  elements: RoomElement[];
}

interface RoomElement {
  id: string;
  type: string;
}

interface InventoryRoom {
  roomId: string;
  roomName: string;
  elements: InventoryElement[];
  status: string; // TODO
  photoUrls: string[];
}

interface InventoryElement {
  elementId: string;
  elementName: string;
  condition: string; // TO_CHECK
  comment: string;
  photoUrls: string[];
}

interface Inventory {
  id: string;
  rentalId: string;
  type: "entry" | "exit";
  rooms: InventoryRoom[];
  landlordSignature: null;
  tenantSignature: null;
  status: string; // DRAFT
  pdfUrl: null;
  pdfGenerationError: null;
  createdAt: number;
  completedAt: null;
}

/**
 * Maps element type to French name
 * @param {string} elementType The element type enum value
 * @return {string} The French name for the element type
 */
function getElementNameInFrench(elementType: string): string {
  const mapping: {[key: string]: string} = {
    FLOOR: "Sol",
    WALL: "Mur",
    CEILING: "Plafond",
    WINDOW: "Fenêtre",
    DOOR: "Porte",
    FURNITURE: "Meuble",
    EQUIPMENT: "Équipement",
  };
  return mapping[elementType] || elementType;
}

/**
 * Creates two empty inventories (entry and exit) when a rental is accepted.
 * Triggered when a rental status changes from PENDING to ACTIVE.
 */
export const onRentalAccepted = onDocumentUpdated(
  {document: "rentals/{rentalId}", region: "us-central1"},
  async (event) => {
    const before = event.data?.before?.data();
    const after = event.data?.after?.data();

    if (!before || !after) return;

    // Only trigger when status changes from PENDING to ACTIVE
    const statusChanged =
      before.status === "pending" && after.status === "active";
    if (!statusChanged) return;

    const rentalId = event.params.rentalId;
    const propertyId = after.propertyId as string;

    console.log(
      `Creating inventories for rental ${rentalId}, ` +
      `property ${propertyId}`
    );

    try {
      // Fetch the property to get room structure
      const propertySnap = await db
        .collection("properties")
        .doc(propertyId)
        .get();

      if (!propertySnap.exists) {
        console.error(`Property ${propertyId} not found`);
        return;
      }

      const propertyData = propertySnap.data();
      const propertyRooms = (propertyData?.rooms || []) as Room[];

      // Transform property rooms into inventory rooms
      const inventoryRooms: InventoryRoom[] = propertyRooms.map((room) => ({
        roomId: room.id,
        roomName: room.name,
        elements: room.elements.map((element) => ({
          elementId: element.id,
          elementName: getElementNameInFrench(element.type),
          condition: "TO_CHECK",
          comment: "",
          photoUrls: [],
        })),
        status: "TODO",
        photoUrls: [],
      }));

      const now = Date.now();

      // Create entry inventory
      const entryInventory: Inventory = {
        id: "", // Will be set after creation
        rentalId: rentalId,
        type: "entry",
        rooms: inventoryRooms,
        landlordSignature: null,
        tenantSignature: null,
        status: "DRAFT",
        pdfUrl: null,
        pdfGenerationError: null,
        createdAt: now,
        completedAt: null,
      };

      // Create exit inventory
      const exitInventory: Inventory = {
        id: "", // Will be set after creation
        rentalId: rentalId,
        type: "exit",
        rooms: inventoryRooms,
        landlordSignature: null,
        tenantSignature: null,
        status: "DRAFT",
        pdfUrl: null,
        pdfGenerationError: null,
        createdAt: now,
        completedAt: null,
      };

      // Save both inventories to Firestore
      const inventoriesCollection = db.collection("inventories");

      // Create entry inventory
      const entryRef = await inventoriesCollection.add(entryInventory);
      await entryRef.update({id: entryRef.id});
      console.log(`Created entry inventory: ${entryRef.id}`);

      // Create exit inventory
      const exitRef = await inventoriesCollection.add(exitInventory);
      await exitRef.update({id: exitRef.id});
      console.log(`Created exit inventory: ${exitRef.id}`);

      // Update the rental with inventory IDs
      const rentalRef = db.collection("rentals").doc(rentalId);
      await rentalRef.update({
        entryInventoryId: entryRef.id,
        exitInventoryId: exitRef.id,
      });
      console.log(`Updated rental ${rentalId} with inventory IDs`);

      console.log(
        `Successfully created inventories for rental ${rentalId}`
      );
    } catch (error) {
      console.error(
        `Error creating inventories for rental ${rentalId}:`,
        error
      );
      throw error;
    }
  }
);
