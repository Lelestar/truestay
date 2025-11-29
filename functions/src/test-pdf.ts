import * as admin from "firebase-admin";
import * as fs from "fs";
import * as path from "path";

// Import types from inventory module
type InventoryData = {
  id: string;
  rentalId: string;
  type: "entry" | "exit";
  rooms: Array<{
    roomId: string;
    roomName: string;
    elements: Array<{
      elementId: string;
      elementName: string;
      condition: "good" | "to_check" | "damaged" | "not_applicable";
      comment: string;
      photoUrls: string[];
    }>;
    status: string;
    photoUrls: string[];
  }>;
  landlordSignature?: {
    userId: string;
    signatureImageUrl: string;
    signedAt: number;
  };
  tenantSignature?: {
    userId: string;
    signatureImageUrl: string;
    signedAt: number;
  };
  status: string;
  createdAt: number;
  completedAt?: number;
};

type PropertyData = {
  name: string;
  address: {
    street: string;
    city: string;
    province: string;
    postalCode: string;
    country: string;
  };
  photos?: string[];
};

type UserData = {
  firstName: string;
  lastName: string;
};

// Read Firebase project ID from .firebaserc
const firebaseRcPath = path.join(__dirname, "../../.firebaserc");
let projectId = "truestay-8inf865"; // Default project ID

try {
  const firebaseRc = JSON.parse(fs.readFileSync(firebaseRcPath, "utf8"));
  projectId = firebaseRc.projects.default;
  console.log(`Using Firebase project: ${projectId}`);
} catch (error) {
  console.log(`Using default project: ${projectId}`);
}

// Initialize Firebase Admin (only if not already initialized)
if (!admin.apps.length) {
  admin.initializeApp({
    projectId: projectId,
  });
}

const db = admin.firestore();

/**
 * Test script to generate PDF locally
 *
 * Usage:
 * 1. Build: npm run build
 * 2. Run: node lib/test-pdf.js <inventoryId>
 *
 * Example: node lib/test-pdf.js abc123
 */

/**
 * Test PDF generation for a given inventory ID
 * @param {string} inventoryId - The inventory ID to generate PDF for
 * @return {Promise<void>} Promise that resolves when PDF is generated
 */
async function testPdfGeneration(inventoryId: string): Promise<void> {
  console.log(`Fetching inventory ${inventoryId}...`);

  try {
    // Get inventory
    const inventorySnap = await db.collection("inventories")
      .doc(inventoryId)
      .get();
    if (!inventorySnap.exists) {
      throw new Error("Inventory not found");
    }

    const inventoryData = inventorySnap.data();
    if (!inventoryData) {
      throw new Error("Inventory data is null");
    }
    const inventory = inventoryData as InventoryData;
    console.log("Inventory found:", inventory.type);

    // Get rental
    const rentalSnap = await db.collection("rentals")
      .doc(inventory.rentalId)
      .get();
    if (!rentalSnap.exists) {
      throw new Error("Rental not found");
    }
    const rentalData = rentalSnap.data();
    if (!rentalData) {
      throw new Error("Rental data is null");
    }
    const rental = rentalData as {
      propertyId: string;
      landlordId: string;
      tenantId: string;
    };

    // Get property
    const propertySnap = await db.collection("properties")
      .doc(rental.propertyId)
      .get();
    if (!propertySnap.exists) {
      throw new Error("Property not found");
    }
    const propertyData = propertySnap.data();
    if (!propertyData) {
      throw new Error("Property data is null");
    }
    const property = propertyData as PropertyData;

    // Get users
    const landlordSnap = await db.collection("users")
      .doc(rental.landlordId)
      .get();
    const tenantSnap = await db.collection("users")
      .doc(rental.tenantId)
      .get();
    const landlordData = landlordSnap.data();
    const tenantData = tenantSnap.data();
    if (!landlordData) {
      throw new Error("Landlord data is null");
    }
    if (!tenantData) {
      throw new Error("Tenant data is null");
    }
    const landlord = landlordData as UserData;
    const tenant = tenantData as UserData;

    console.log("All data fetched successfully");
    console.log("Property:", property.name);
    console.log("Landlord:", landlord.firstName, landlord.lastName);
    console.log("Tenant:", tenant.firstName, tenant.lastName);

    // Import the PDF generation function
    const {generatePdfForTest} = await import("./inventory.js");

    console.log("Generating PDF...");
    const pdfBuffer = await generatePdfForTest(
      inventory,
      property,
      landlord,
      tenant
    );

    // Save PDF to local file
    const fileName = `../test-output-${inventoryId}.pdf`;
    const outputPath = path.join(__dirname, fileName);
    fs.writeFileSync(outputPath, pdfBuffer);

    console.log("✅ PDF generated successfully!");
    console.log(`📄 File saved to: ${outputPath}`);
    console.log("\nOpen the PDF to preview the result.");

    process.exit(0);
  } catch (error) {
    console.error("❌ Error:", error);
    process.exit(1);
  }
}

// Get inventory ID from command line argument
const inventoryId = process.argv[2];

if (!inventoryId) {
  console.error("Usage: node lib/test-pdf.js <inventoryId>");
  process.exit(1);
}

testPdfGeneration(inventoryId);
