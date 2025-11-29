import {onCall, HttpsError} from "firebase-functions/v2/https";
import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import PDFDocument from "pdfkit";
import * as path from "path";

// Initialize Firebase
const db = admin.firestore();
const storage = admin.storage();

// --- TYPE DEFINITIONS ---

type InventoryData = {
  id: string;
  rentalId: string;
  type: "entry" | "exit";
  rooms: RoomData[];
  landlordSignature?: SignatureData;
  tenantSignature?: SignatureData;
  status: string;
  createdAt: number;
  completedAt?: number;
};

type RoomData = {
  roomId: string;
  roomName: string;
  elements: ElementData[];
  status: string;
  photoUrls: string[];
};

type ElementData = {
  elementId: string;
  elementName: string;
  condition: "good" | "to_check" | "damaged" | "not_applicable";
  comment: string;
  photoUrls: string[];
};

type SignatureData = {
  userId: string;
  signatureImageUrl: string;
  signedAt: number;
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

// --- DESIGN CONFIGURATION ---

// Mapped from AppColorsLight object
const COLORS = {
  primary: "#2F80ED", // AppColorsLight.primary
  primarySurface: "#EFF6FF", // AppColorsLight.primarySurface

  textMain: "#000000", // AppColorsLight.black
  textDark: "#4F4F4F", // AppColorsLight.grayDark
  textMedium: "#7D7E87", // AppColorsLight.grayMedium

  bgLight: "#F9FAFB", // AppColorsLight.graySurface
  border: "#E5E5E5", // AppColorsLight.grayBorder

  // Status Colors (Main)
  success: "#27AE60",
  error: "#EB5757",
  warning: "#F2C94C",
  info: "#2F80ED",

  // Status Backgrounds (Surface)
  successSurface: "#E6F9EF",
  errorSurface: "#FFEBEB",
  warningSurface: "#FDF6E2",
  infoSurface: "#EFF6FF",

  // Text on Status Surfaces
  onSuccessSurface: "#084C2E",
  onErrorSurface: "#6A1B1B",
  onWarningSurface: "#7A3206",
  onInfoSurface: "#0E3A73",
};

const LAYOUT = {
  margin: 40,
  colGutter: 20,
  lineHeight: 16,
  cardRadius: 6,
};

// --- HELPER FUNCTIONS ---

/**
 * Downloads an image from URL and returns it as a Buffer
 * @param {string} url - The URL of the image to download
 * @return {Promise<Buffer | null>} Image buffer or null if failed
 */
async function downloadImage(url: string): Promise<Buffer | null> {
  try {
    const response = await fetch(url);
    if (!response.ok) return null;
    const arrayBuffer = await response.arrayBuffer();
    return Buffer.from(arrayBuffer);
  } catch (error) {
    console.error(`Failed to download image from ${url}:`, error);
    return null;
  }
}

/**
 * Helper to draw a modern "Chip" style status badge
 * Uses the Surface color for background and OnSurface color for text
 * @param {object} doc - The PDF document
 * @param {string} text - The badge text to display
 * @param {string} type - Badge type (good, damaged, to_check, not_applicable)
 * @param {number} x - X position on the page
 * @param {number} y - Y position on the page
 * @return {number} The width of the drawn badge
 */
function drawStatusBadge(
  doc: InstanceType<typeof PDFDocument>,
  text: string,
  type: "good" | "damaged" | "to_check" | "not_applicable",
  x: number,
  y: number
) {
  let bgColor = COLORS.bgLight;
  let textColor = COLORS.textMedium;

  // Map condition to your color palette
  switch (type) {
  case "good":
    bgColor = COLORS.successSurface;
    textColor = COLORS.onSuccessSurface;
    break;
  case "damaged":
    bgColor = COLORS.errorSurface;
    textColor = COLORS.onErrorSurface;
    break;
  case "to_check":
    bgColor = COLORS.warningSurface;
    textColor = COLORS.onWarningSurface;
    break;
  case "not_applicable":
  default:
    bgColor = COLORS.bgLight; // graySurface
    textColor = COLORS.textDark; // grayDark
    break;
  }

  const paddingX = 10;

  // Calculate width based on text
  doc.fontSize(8).font("Montserrat-Bold");
  const textWidth = doc.widthOfString(text);
  const badgeWidth = textWidth + (paddingX * 2);
  const badgeHeight = 18;

  // Draw pill shape background (radius 9 for pill shape)
  doc.roundedRect(x, y - 2, badgeWidth, badgeHeight, 9)
    .fill(bgColor);

  // Draw text
  doc.fillColor(textColor)
    .text(text, x + paddingX, y + 3);

  return badgeWidth;
}

/**
 * Checks if a page break is needed based on required height
 * Reserves space for footer (separator + text)
 * @param {object} doc - The PDF document
 * @param {number} neededHeight - The height required for content
 * @return {boolean} True if a new page was added, false otherwise
 */
function checkPageBreak(
  doc: InstanceType<typeof PDFDocument>,
  neededHeight: number
) {
  const footerSpace = 50; // Space for footer separator + text
  const maxY = doc.page.height - LAYOUT.margin - footerSpace;
  if (doc.y + neededHeight > maxY) {
    doc.addPage();
    return true;
  }
  return false;
}

// --- MAIN PDF GENERATOR ---

/**
 * Generates the PDF document buffer
 * @param {InventoryData} inventory - The inventory data
 * @param {PropertyData} property - The property data
 * @param {UserData} landlord - The landlord data
 * @param {UserData} tenant - The tenant data
 * @return {Promise<Buffer>} The generated PDF as a buffer
 */
async function generatePdfDocument(
  inventory: InventoryData,
  property: PropertyData,
  landlord: UserData,
  tenant: UserData
): Promise<Buffer> {
  // 1. Validate signatures
  if (!inventory.landlordSignature || !inventory.tenantSignature) {
    throw new Error("Both signatures must be present");
  }

  const landlordSignature = inventory.landlordSignature;
  const tenantSignature = inventory.tenantSignature;

  // 2. Pre-download assets
  // Parallel downloads could be implemented here for speed
  const landlordSigUrl = landlordSignature.signatureImageUrl;
  const tenantSigUrl = tenantSignature.signatureImageUrl;
  const landlordSigImage = await downloadImage(landlordSigUrl);
  const tenantSigImage = await downloadImage(tenantSigUrl);

  let propertyImage: Buffer | null = null;
  if (property.photos && property.photos.length > 0) {
    propertyImage = await downloadImage(property.photos[0]);
  }

  // Download room photos
  const roomPhotos: Map<string, Buffer[]> = new Map();
  for (const room of inventory.rooms) {
    if (room.photoUrls.length > 0) {
      const photos: Buffer[] = [];
      for (const photoUrl of room.photoUrls) {
        const photoBuffer = await downloadImage(photoUrl);
        if (photoBuffer) photos.push(photoBuffer);
      }
      if (photos.length > 0) roomPhotos.set(room.roomId, photos);
    }
  }

  // Download element photos
  const elementPhotos: Map<string, Buffer[]> = new Map();
  for (const room of inventory.rooms) {
    for (const element of room.elements) {
      if (element.photoUrls.length > 0) {
        const photos: Buffer[] = [];
        for (const photoUrl of element.photoUrls) {
          const photoBuffer = await downloadImage(photoUrl);
          if (photoBuffer) photos.push(photoBuffer);
        }
        if (photos.length > 0) elementPhotos.set(element.elementId, photos);
      }
    }
  }

  // 3. Create Document
  return new Promise((resolve, reject) => {
    const doc = new PDFDocument({
      size: "A4",
      margins: {
        top: LAYOUT.margin,
        bottom: LAYOUT.margin,
        left: LAYOUT.margin,
        right: LAYOUT.margin,
      },
      bufferPages: true,
    });

    // Register Fonts
    const fontsPath = path.join(__dirname, "../fonts");
    const montRegular = "montserrat_regular.ttf";
    const montSemiBold = "montserrat_semibold.ttf";
    const montBold = "montserrat_bold.ttf";
    doc.registerFont("Montserrat", path.join(fontsPath, montRegular));
    doc.registerFont(
      "Montserrat-SemiBold",
      path.join(fontsPath, montSemiBold)
    );
    doc.registerFont("Montserrat-Bold", path.join(fontsPath, montBold));

    const chunks: Buffer[] = [];
    doc.on("data", (chunk) => chunks.push(chunk));
    doc.on("end", () => resolve(Buffer.concat(chunks)));
    doc.on("error", reject);

    // --- HEADER SECTION ---

    // Colored top strip (optional, adds brand touch)
    doc.rect(0, 0, doc.page.width, 10).fill(COLORS.primary);

    const logoPath = path.join(__dirname, "../images/logo.jpg");
    // Place logo top-left
    doc.image(logoPath, LAYOUT.margin, 40, {fit: [100, 50]});

    // Title and Date top-right
    const reportTitle = inventory.type === "entry" ?
      "ÉTAT DES LIEUX D'ENTRÉE" :
      "ÉTAT DES LIEUX DE SORTIE";
    const titleWidth = doc.page.width - LAYOUT.margin;
    doc.fillColor(COLORS.primary)
      .fontSize(18)
      .font("Montserrat-Bold")
      .text(reportTitle, 0, 45, {align: "right", width: titleWidth});

    const docDate = inventory.completedAt ?
      new Date(inventory.completedAt) :
      new Date(
        Math.max(landlordSignature.signedAt, tenantSignature.signedAt)
      );

    const dateOptions = {year: "numeric", month: "long", day: "numeric"};
    const formattedDate = docDate.toLocaleDateString(
      "fr-FR",
      dateOptions as Intl.DateTimeFormatOptions
    );
    doc.fillColor(COLORS.textMedium)
      .fontSize(10)
      .font("Montserrat-SemiBold")
      .text(formattedDate, 0, 70, {align: "right", width: titleWidth});

    doc.moveDown(4);

    // --- SUMMARY CARDS (Property & Parties) ---
    const startY = doc.y;
    const totalWidth = doc.page.width - (LAYOUT.margin * 2);
    const colWidth = (totalWidth - LAYOUT.colGutter) / 2;

    // Card 1: Property
    doc.roundedRect(
      LAYOUT.margin,
      startY,
      colWidth,
      110,
      LAYOUT.cardRadius
    ).fill(COLORS.bgLight); // Gray Surface

    // Property Icon/Title
    doc.fillColor(COLORS.primary)
      .fontSize(10)
      .font("Montserrat-Bold")
      .text("DÉTAILS DE LA PROPRIÉTÉ", LAYOUT.margin + 15, startY + 15);

    const cityProvince = `${property.address.city}, ` +
      `${property.address.province} ${property.address.postalCode}`;
    doc.fillColor(COLORS.textDark).fontSize(9).font("Montserrat")
      .text(property.name, LAYOUT.margin + 15, startY + 35)
      .text(property.address.street)
      .text(cityProvince);

    // Card 2: Parties
    const col2X = LAYOUT.margin + colWidth + LAYOUT.colGutter;
    doc.roundedRect(col2X, startY, colWidth, 110, LAYOUT.cardRadius)
      .fill(COLORS.bgLight);

    doc.fillColor(COLORS.primary)
      .fontSize(10)
      .font("Montserrat-Bold")
      .text("PARTIES IMPLIQUÉES", col2X + 15, startY + 15);

    // Landlord
    doc.fillColor(COLORS.textMedium)
      .fontSize(8)
      .font("Montserrat-Bold")
      .text("PROPRIÉTAIRE", col2X + 15, startY + 35);
    const landlordName = `${landlord.firstName} ${landlord.lastName}`;
    doc.fillColor(COLORS.textMain)
      .fontSize(9)
      .font("Montserrat")
      .text(landlordName, col2X + 15, startY + 45);

    // Tenant
    doc.fillColor(COLORS.textMedium)
      .fontSize(8)
      .font("Montserrat-Bold")
      .text("LOCATAIRE", col2X + 15, startY + 70);
    const tenantName = `${tenant.firstName} ${tenant.lastName}`;
    doc.fillColor(COLORS.textMain)
      .fontSize(9)
      .font("Montserrat")
      .text(tenantName, col2X + 15, startY + 80);

    doc.y = startY + 130; // Move cursor past cards

    // Optional: Main Property Photo Centered
    if (propertyImage) {
      const pImgHeight = 180;
      const pImgWidth = 300;
      const pImgX = (doc.page.width - pImgWidth) / 2;

      // Check space
      checkPageBreak(doc, pImgHeight + 20);

      doc.image(propertyImage, pImgX, doc.y, {fit: [pImgWidth, pImgHeight]});
      doc.y += pImgHeight + 30;
    }

    // --- ROOM DETAILS SECTION ---

    inventory.rooms.forEach((room, index) => {
      checkPageBreak(doc, 100);

      // Room Header Bar
      const headerY = doc.y;
      const headerWidth = doc.page.width - (LAYOUT.margin * 2);
      doc.roundedRect(LAYOUT.margin, headerY, headerWidth, 28, 4)
        .fill(COLORS.primary);

      const roomTitle = `${index + 1}. ${room.roomName.toUpperCase()}`;
      doc.fillColor(COLORS.primarySurface) // text on primary
        .fontSize(12)
        .font("Montserrat-Bold")
        .text(roomTitle, LAYOUT.margin + 10, headerY + 8);

      doc.moveDown(2.5);

      // Room Photos (Grid System)
      const currentRoomPhotos = roomPhotos.get(room.roomId);
      if (currentRoomPhotos && currentRoomPhotos.length > 0) {
        const imgSize = 100;
        const gap = 10;
        let currentX = LAYOUT.margin;
        let currentY = doc.y;

        currentRoomPhotos.forEach((photo) => {
          if (currentX + imgSize > doc.page.width - LAYOUT.margin) {
            currentX = LAYOUT.margin;
            currentY += imgSize + gap;
          }

          // If photo pushes to new page (reserve footer space)
          const footerSpace = 50;
          const maxPhotoY = doc.page.height - LAYOUT.margin - footerSpace;
          if (currentY + imgSize > maxPhotoY) {
            doc.addPage();
            currentY = LAYOUT.margin;
            currentX = LAYOUT.margin;
          }

          try {
            doc.image(photo, currentX, currentY, {fit: [imgSize, imgSize]});
          } catch (e) {/* ignore corrupt images */}

          currentX += imgSize + gap;
        });

        // Update Y position after photos
        doc.y = currentY + imgSize + 20;
      }

      // Room Elements List
      room.elements.forEach((element) => {
        // Check for page break
        if (checkPageBreak(doc, 50)) {
          doc.moveDown(1);
        }

        const elemY = doc.y;

        // Element Name
        doc.fillColor(COLORS.textMain)
          .fontSize(10)
          .font("Montserrat-SemiBold")
          .text(element.elementName, LAYOUT.margin, elemY);

        const conditionTextMap: Record<string, string> = {
          good: "Bon état",
          damaged: "Endommagé",
          to_check: "À vérifier",
          not_applicable: "Non applicable",
        };

        const badgeText = conditionTextMap[element.condition] ||
          element.condition;

        // Draw Badge aligned to the right side
        // Calculate exact width inside the function
        doc.font("Montserrat-Bold").fontSize(8);
        const badgeW = doc.widthOfString(badgeText) + 20;
        const badgeX = doc.page.width - LAYOUT.margin - badgeW;
        drawStatusBadge(doc, badgeText, element.condition, badgeX, elemY);

        // Comment
        if (element.comment) {
          doc.moveDown(0.4);
          doc.fillColor(COLORS.textMedium)
            .fontSize(9)
            .font("Montserrat")
            .text(
              element.comment,
              LAYOUT.margin + 10,
              doc.y,
              {oblique: true}
            );
        }

        // Element Photos
        const elPhotos = elementPhotos.get(element.elementId);
        if (elPhotos && elPhotos.length > 0) {
          doc.moveDown(0.5);
          const pSize = 60;

          // Check if we have space for photos, otherwise new page
          checkPageBreak(doc, pSize + 5);

          let pX = LAYOUT.margin + 10;
          const startY = doc.y;

          elPhotos.forEach((p) => {
            // Simple check to avoid going off page width
            if (pX + pSize < doc.page.width - LAYOUT.margin) {
              doc.image(p, pX, startY, {fit: [pSize, pSize]});
              pX += pSize + 5;
            }
          });
          doc.y = startY + pSize + 5;
        }

        // Divider Line
        doc.moveDown(0.5);
        doc.moveTo(LAYOUT.margin, doc.y)
          .lineTo(doc.page.width - LAYOUT.margin, doc.y)
          .strokeColor(COLORS.border)
          .lineWidth(0.5)
          .stroke();

        doc.moveDown(0.8);
      });

      doc.moveDown(1);
    });

    // --- SIGNATURES SECTION ---
    doc.addPage();

    doc.fillColor(COLORS.primary)
      .fontSize(16)
      .font("Montserrat-Bold")
      .text("SIGNATURES", {align: "center"});

    doc.moveDown(2);

    const sigStartY = doc.y;
    const sigBoxWidth = (doc.page.width - (LAYOUT.margin * 2) - 20) / 2;

    // Landlord Box
    doc.roundedRect(LAYOUT.margin, sigStartY, sigBoxWidth, 180, 4)
      .lineWidth(1)
      .strokeColor(COLORS.border)
      .stroke();

    doc.fillColor(COLORS.textMain)
      .fontSize(11)
      .font("Montserrat-Bold")
      .text("Propriétaire", LAYOUT.margin + 15, sigStartY + 15);

    const lName = `${landlord.firstName} ${landlord.lastName}`;
    doc.fillColor(COLORS.textDark)
      .fontSize(9)
      .font("Montserrat")
      .text(lName, LAYOUT.margin + 15, sigStartY + 30);

    const lDate = new Date(landlordSignature.signedAt);
    const lDateStr = `Signé le : ${lDate.toLocaleString("fr-FR")}`;
    doc.fillColor(COLORS.textMedium)
      .fontSize(8)
      .text(lDateStr, LAYOUT.margin + 15, sigStartY + 45);

    if (landlordSigImage) {
      doc.image(
        landlordSigImage,
        LAYOUT.margin + 20,
        sigStartY + 70,
        {fit: [sigBoxWidth - 40, 60]}
      );
    }

    // Tenant Box
    const tenantBoxX = LAYOUT.margin + sigBoxWidth + 20;
    doc.roundedRect(tenantBoxX, sigStartY, sigBoxWidth, 180, 4)
      .lineWidth(1)
      .strokeColor(COLORS.border)
      .stroke();

    doc.fillColor(COLORS.textMain)
      .fontSize(11)
      .font("Montserrat-Bold")
      .text("Locataire", tenantBoxX + 15, sigStartY + 15);

    const tName = `${tenant.firstName} ${tenant.lastName}`;
    doc.fillColor(COLORS.textDark)
      .fontSize(9)
      .font("Montserrat")
      .text(tName, tenantBoxX + 15, sigStartY + 30);

    const tDate = new Date(tenantSignature.signedAt);
    const tDateStr = `Signé le : ${tDate.toLocaleString("fr-FR")}`;
    doc.fillColor(COLORS.textMedium)
      .fontSize(8)
      .text(tDateStr, tenantBoxX + 15, sigStartY + 45);

    if (tenantSigImage) {
      doc.image(
        tenantSigImage,
        tenantBoxX + 20,
        sigStartY + 70,
        {fit: [sigBoxWidth - 40, 60]}
      );
    }

    // Legal Footer Text
    doc.moveDown(15);
    const legalText = "Ce document est généré par TrueStay " +
      "et a une valeur légale.";
    doc.fillColor(COLORS.textMedium)
      .fontSize(7)
      .font("Montserrat")
      .text(legalText, {align: "center"});

    // --- FOOTER PAGINATION ---
    const range = doc.bufferedPageRange();
    for (let i = 0; i < range.count; i++) {
      doc.switchToPage(i);
      const footerY = doc.page.height - 30;

      // Thin separator
      doc.moveTo(LAYOUT.margin, footerY - 10)
        .lineTo(doc.page.width - LAYOUT.margin, footerY - 10)
        .strokeColor(COLORS.border)
        .lineWidth(0.5)
        .stroke();

      // Logo or Text Left
      doc.fillColor(COLORS.primary)
        .fontSize(8)
        .font("Montserrat-Bold")
        .text("TrueStay", LAYOUT.margin, footerY, {lineBreak: false});

      // Page Number Right - calculate position exactly
      const pageText = `Page ${i + 1} sur ${range.count}`;
      doc.fontSize(8).font("Montserrat");
      const pageTextWidth = doc.widthOfString(pageText);
      const pageX = doc.page.width - LAYOUT.margin - pageTextWidth;
      doc.fillColor(COLORS.textMedium)
        .text(pageText, pageX, footerY, {lineBreak: false});
    }

    // Finalize
    doc.end();
  });
}

/**
 * Exported function for testing PDF generation locally
 * @param {InventoryData} inventory - Inventory data from Firestore
 * @param {PropertyData} property - Property data from Firestore
 * @param {UserData} landlord - Landlord data from Firestore
 * @param {UserData} tenant - Tenant data from Firestore
 * @return {Promise<Buffer>} PDF buffer
 */
export async function generatePdfForTest(
  inventory: InventoryData,
  property: PropertyData,
  landlord: UserData,
  tenant: UserData
): Promise<Buffer> {
  return generatePdfDocument(inventory, property, landlord, tenant);
}

/**
 * Firestore trigger that automatically generates PDF when both
 * signatures are present
 *
 * Triggers when an inventory document is updated and both landlord
 * and tenant have signed (status changes to SIGNED).
 */
export const onInventorySign = onDocumentUpdated(
  {document: "inventories/{inventoryId}", region: "us-central1"},
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async (event: any) => {
    const inventoryId = event.params.inventoryId;
    console.log(`[onInventorySign] Triggered for inventory ${inventoryId}`);

    const before = event.data?.before?.data() as
      InventoryData | undefined;
    const after = event.data?.after?.data() as
      InventoryData | undefined;

    console.log(`[onInventorySign] Before status: ${before?.status}`);
    console.log(`[onInventorySign] After status: ${after?.status}`);

    // Only proceed if both signatures are present and
    // status just changed to SIGNED
    if (!after || !before) {
      console.log("[onInventorySign] Missing before or after data, exiting");
      return;
    }

    const justSigned = after.status === "signed" &&
      before.status !== "signed";
    const bothSignaturesPresent = after.landlordSignature &&
      after.tenantSignature;

    console.log(`[onInventorySign] justSigned: ${justSigned}`);
    console.log(
      `[onInventorySign] bothSignaturesPresent: ${bothSignaturesPresent}`
    );

    if (!justSigned || !bothSignaturesPresent) {
      console.log("[onInventorySign] Conditions not met, exiting");
      return;
    }

    try {
      console.log(`Auto-generating PDF for inventory ${inventoryId}`);
      await generatePdfForInventory(inventoryId);
      console.log(
        `PDF generated successfully for inventory ${inventoryId}`
      );
    } catch (error) {
      console.error(
        `Error generating PDF for inventory ${inventoryId}:`,
        error
      );

      // Store error in inventory document for user feedback
      try {
        const errorMessage = error instanceof Error ?
          error.message :
          "Unknown error occurred during PDF generation";
        await db.collection("inventories").doc(inventoryId).update({
          pdfGenerationError: errorMessage,
        });
      } catch (updateError) {
        console.error(
          `Failed to update error status for inventory ${inventoryId}:`,
          updateError
        );
      }

      // Don't throw - we don't want to retry indefinitely
    }
  }
);

/**
 * Internal helper function to generate PDF for an inventory
 * Used by both the trigger and the callable function
 * @param {string} inventoryId - The inventory ID
 * @return {Promise<string>} URL of the generated PDF
 */
async function generatePdfForInventory(
  inventoryId: string
): Promise<string> {
  // Get inventory data
  const inventorySnap = await db.collection("inventories")
    .doc(inventoryId).get();
  if (!inventorySnap.exists) {
    throw new Error("Inventory not found");
  }

  const inventory = inventorySnap.data() as InventoryData;

  // Validate signatures
  if (!inventory.landlordSignature || !inventory.tenantSignature) {
    throw new Error(
      "Both landlord and tenant must sign before generating PDF"
    );
  }

  // Clear previous error if any
  await db.collection("inventories").doc(inventoryId).update({
    pdfGenerationError: null,
  });

  // Get related data
  const rentalSnap = await db.collection("rentals")
    .doc(inventory.rentalId).get();
  if (!rentalSnap.exists) {
    throw new Error("Rental not found");
  }
  const rental = rentalSnap.data();
  if (!rental) {
    throw new Error("Rental data is null");
  }

  const propertySnap = await db.collection("properties")
    .doc(rental.propertyId).get();
  if (!propertySnap.exists) {
    throw new Error("Property not found");
  }
  const property = propertySnap.data() as PropertyData;

  // Get landlord and tenant info
  const landlordSnap = await db.collection("users")
    .doc(rental.landlordId).get();
  const tenantSnap = await db.collection("users")
    .doc(rental.tenantId).get();
  const landlord = landlordSnap.data() as UserData;
  const tenant = tenantSnap.data() as UserData;

  // Generate PDF
  const pdfBuffer = await generatePdfDocument(
    inventory,
    property,
    landlord,
    tenant
  );

  // Upload to Storage
  const timestamp = Date.now();
  const fileName = `inventories/${inventoryId}/inventory_${timestamp}.pdf`;
  const file = storage.bucket().file(fileName);

  await file.save(pdfBuffer, {
    metadata: {
      contentType: "application/pdf",
    },
  });

  // Make file publicly accessible
  await file.makePublic();
  const pdfUrl = file.publicUrl();

  // Update inventory with PDF URL and status COMPLETED
  await db.collection("inventories").doc(inventoryId).update({
    pdfUrl: pdfUrl,
    pdfGenerationError: null,
    status: "completed",
    completedAt: Date.now(),
  });

  return pdfUrl;
}

/**
 * Callable function to generate PDF for an inventory
 *
 * This can be used to retry PDF generation if it failed, or to manually
 * trigger PDF generation
 */
export const generateInventoryPdf = onCall(
  {region: "us-central1"},
  async (request) => {
    const {inventoryId} = request.data;

    if (!inventoryId) {
      throw new HttpsError("invalid-argument", "inventoryId is required");
    }

    try {
      const pdfUrl = await generatePdfForInventory(inventoryId);
      console.log(
        `PDF generated successfully for inventory ${inventoryId}: ${pdfUrl}`
      );
      return {success: true, pdfUrl};
    } catch (error) {
      console.error(
        `Error generating PDF for inventory ${inventoryId}:`,
        error
      );

      // Store error in inventory document
      try {
        const errorMessage = error instanceof Error ?
          error.message :
          "Unknown error occurred during PDF generation";
        await db.collection("inventories").doc(inventoryId).update({
          pdfGenerationError: errorMessage,
        });
      } catch (updateError) {
        console.error(
          `Failed to update error status for inventory ${inventoryId}:`,
          updateError
        );
      }

      const errorMsg = error instanceof Error ?
        error.message :
        "Failed to generate PDF";
      throw new HttpsError("internal", errorMsg);
    }
  }
);
