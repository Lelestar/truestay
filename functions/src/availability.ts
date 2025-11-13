import {onDocumentWritten} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

type Json = Record<string, unknown>;

const db = admin.firestore();

/**
 * Computes availability for a property based on active rentals.
 * Rule: available iff no rental with status == "active" exists
 * for the property.
 * @param {string} propertyId Property identifier
 * @return {Promise<boolean>} True when no active rental exists
 */
async function computeIsAvailable(propertyId: string): Promise<boolean> {
  const activeSnap = await db
    .collection("rentals")
    .where("propertyId", "==", propertyId)
    .where("status", "==", "active")
    .limit(1)
    .get();

  return activeSnap.empty;
}

/**
 * Updates the property document's isAvailable flag if it differs
 * from the computed value. No-op if property doc does not exist.
 * @param {string} propertyId Property identifier
 * @return {Promise<void>} Resolves when sync completes
 */
async function syncPropertyAvailability(propertyId: string): Promise<void> {
  if (!propertyId) return;

  const propRef = db.collection("properties").doc(propertyId);
  const propSnap = await propRef.get();
  if (!propSnap.exists) return;

  const current = propSnap.get("isAvailable");
  const desired = await computeIsAvailable(propertyId);

  if (current !== desired) {
    await propRef.update({isAvailable: desired, updatedAt: Date.now()});
  }
}

/**
 * Trigger on any write to rentals collection to keep
 * properties/{id}.isAvailable in sync.
 */
export const onRentalWrite = onDocumentWritten(
  {document: "rentals/{rentalId}", region: "us-central1"},
  async (event) => {
    const before =
      (event.data?.before?.data() as Json | undefined);
    const after =
      (event.data?.after?.data() as Json | undefined);

    // Determine affected property IDs (handles propertyId changes and deletes)
    const affected = new Set<string>();
    const beforePid = (before?.propertyId as string) || "";
    const afterPid = (after?.propertyId as string) || "";
    if (beforePid) affected.add(beforePid);
    if (afterPid) affected.add(afterPid);

    await Promise.all(
      [...affected].map((pid) => syncPropertyAvailability(pid))
    );
  }
);
