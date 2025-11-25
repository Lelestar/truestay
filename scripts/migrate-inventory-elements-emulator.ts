import * as admin from 'firebase-admin';
import { getElementName } from './seed/properties';

// Set environment variables for emulator
process.env.FIRESTORE_EMULATOR_HOST = 'localhost:8080';

// Initialize Firebase Admin for emulator
admin.initializeApp({
  projectId: 'truestay-8inf865'
});

const db = admin.firestore();

type ElementType = 'floor' | 'wall' | 'ceiling' | 'window' | 'door' | 'furniture' | 'equipment';

interface PropertyRoom {
  id: string;
  name: string;
  type: string;
  elements: Array<{
    id: string;
    type: ElementType;
    elementName?: string;
  }>;
}

interface Property {
  id: string;
  rooms: PropertyRoom[];
}

interface InventoryElement {
  elementId: string;
  elementName?: string;
  condition: string;
  comment: string;
  photoUrls: string[];
}

interface InventoryRoom {
  roomId: string;
  roomName: string;
  elements: InventoryElement[];
  status: string;
  photoUrls?: string[];
}

interface Inventory {
  id: string;
  rentalId: string;
  type: string;
  rooms: InventoryRoom[];
}

interface Rental {
  id: string;
  propertyId: string;
  entryInventoryId?: string | null;
  exitInventoryId?: string | null;
}

async function migrateInventoryElements() {
  console.log('🚀 Starting migration of inventory elements (EMULATOR)...\n');

  try {
    // Get all rentals
    const rentalsSnapshot = await db.collection('rentals').get();
    console.log(`📋 Found ${rentalsSnapshot.size} rentals\n`);

    let updatedInventories = 0;
    let updatedRoomsCount = 0;
    let addedElements = 0;
    let addedElementNames = 0;

    for (const rentalDoc of rentalsSnapshot.docs) {
      const rental = rentalDoc.data() as Rental;

      // Get the property to know what elements should exist
      const propertyDoc = await db.collection('properties').doc(rental.propertyId).get();
      if (!propertyDoc.exists) {
        console.log(`⚠️  Property ${rental.propertyId} not found for rental ${rental.id}`);
        continue;
      }

      const property = propertyDoc.data() as Property;
      const inventoryIds = [rental.entryInventoryId, rental.exitInventoryId].filter(Boolean) as string[];

      // Process each inventory (entry and exit)
      for (const inventoryId of inventoryIds) {
        const inventoryDoc = await db.collection('inventories').doc(inventoryId).get();
        if (!inventoryDoc.exists) {
          console.log(`⚠️  Inventory ${inventoryId} not found`);
          continue;
        }

        const inventory = inventoryDoc.data() as Inventory;
        let inventoryModified = false;

        // Process each room in the inventory
        const updatedRoomsArray = inventory.rooms.map((inventoryRoom) => {
          // Find corresponding room in property
          const propertyRoom = property.rooms?.find(r => r.id === inventoryRoom.roomId);
          if (!propertyRoom) {
            console.log(`⚠️  Room ${inventoryRoom.roomId} not found in property`);
            return inventoryRoom;
          }

          let roomModified = false;
          const existingElementIds = new Set(inventoryRoom.elements.map(e => e.elementId));

          // Add missing elements from property
          const missingElements = (propertyRoom.elements || []).filter(
            propElement => !existingElementIds.has(propElement.id)
          );

          if (missingElements.length > 0) {
            console.log(`  ➕ Adding ${missingElements.length} missing elements to room "${inventoryRoom.roomName}" in inventory ${inventoryId}`);
            addedElements += missingElements.length;
            roomModified = true;
            inventoryModified = true;
          }

          // Update existing elements with elementName if missing
          const updatedElements = inventoryRoom.elements.map(element => {
            if (!element.elementName) {
              // Find the element in property to get its type
              const propElement = propertyRoom.elements?.find(e => e.id === element.elementId);
              if (propElement) {
                const elementName = propElement.elementName || getElementName(propElement.type);
                console.log(`  ✏️  Adding elementName "${elementName}" to element ${element.elementId}`);
                addedElementNames++;
                roomModified = true;
                inventoryModified = true;
                return { ...element, elementName };
              }
            }
            return element;
          });

          // Add missing elements with default values
          const newElements = missingElements.map(propElement => ({
            elementId: propElement.id,
            elementName: propElement.elementName || getElementName(propElement.type),
            condition: 'to_check' as const,
            comment: '',
            photoUrls: []
          }));

          if (roomModified) {
            updatedRoomsCount++;
          }

          return {
            ...inventoryRoom,
            elements: [...updatedElements, ...newElements],
            photoUrls: inventoryRoom.photoUrls || []
          };
        });

        // Update inventory if modified
        if (inventoryModified) {
          await db.collection('inventories').doc(inventoryId).update({
            rooms: updatedRoomsArray
          });
          updatedInventories++;
          console.log(`✅ Updated inventory ${inventoryId} (${inventory.type})\n`);
        }
      }
    }

    console.log('\n🎉 Migration completed!');
    console.log(`📊 Summary:`);
    console.log(`   - Updated inventories: ${updatedInventories}`);
    console.log(`   - Updated rooms: ${updatedRoomsCount}`);
    console.log(`   - Added elements: ${addedElements}`);
    console.log(`   - Added elementNames: ${addedElementNames}`);

  } catch (error) {
    console.error('❌ Error during migration:', error);
    throw error;
  }
}

// Run migration
migrateInventoryElements()
  .then(() => {
    console.log('\n✨ Migration script finished successfully');
    process.exit(0);
  })
  .catch((error) => {
    console.error('\n💥 Migration script failed:', error);
    process.exit(1);
  });
