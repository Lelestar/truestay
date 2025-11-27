import {onDocumentWritten} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

const db = admin.firestore();

type ReviewData = {
  propertyId?: string;
  propertyReview?: { overallRating?: number };
  buildingReview?: { overallRating?: number };
  neighborhoodReview?: { overallRating?: number };
};

/**
 * Recalculates and updates property ratings based on all reviews
 * for a given property.
 *
 * @param {string} propertyId The ID of the property to update
 * @return {Promise<void>} Resolves when update is complete
 */
async function syncPropertyRatings(propertyId: string): Promise<void> {
  if (!propertyId) return;

  const propRef = db.collection("properties").doc(propertyId);
  const propSnap = await propRef.get();

  if (!propSnap.exists) return;

  // Get all reviews for this property
  const reviewsSnap = await db
    .collection("reviews")
    .where("propertyId", "==", propertyId)
    .get();

  let propertySum = 0;
  let propertyCount = 0;

  let buildingSum = 0;
  let buildingCount = 0;

  let neighborhoodSum = 0;
  let neighborhoodCount = 0;

  reviewsSnap.forEach((doc) => {
    const review = doc.data() as ReviewData;

    if (review.propertyReview?.overallRating !== undefined) {
      propertySum += review.propertyReview.overallRating;
      propertyCount++;
    }

    if (review.buildingReview?.overallRating !== undefined) {
      buildingSum += review.buildingReview.overallRating;
      buildingCount++;
    }

    if (review.neighborhoodReview?.overallRating !== undefined) {
      neighborhoodSum += review.neighborhoodReview.overallRating;
      neighborhoodCount++;
    }
  });

  const newRatings = {
    propertyAverageRating: propertyCount > 0 ? propertySum / propertyCount : 0,
    propertyReviewCount: propertyCount,
    buildingAverageRating: buildingCount > 0 ? buildingSum / buildingCount : 0,
    buildingReviewCount: buildingCount,
    neighborhoodAverageRating:
      neighborhoodCount > 0 ?
        neighborhoodSum / neighborhoodCount :
        0,
    neighborhoodReviewCount: neighborhoodCount,
  };

  await propRef.update({
    ratings: newRatings,
    updatedAt: Date.now(),
  });
}

/**
 * Trigger on any write to reviews collection to keep
 * property ratings in sync.
 */
export const onReviewWrite = onDocumentWritten(
  {document: "reviews/{reviewId}", region: "us-central1"},
  async (event) => {
    const before = event.data?.before?.data() as ReviewData | undefined;
    const after = event.data?.after?.data() as ReviewData | undefined;

    const affected = new Set<string>();
    if (before?.propertyId) affected.add(before.propertyId);
    if (after?.propertyId) affected.add(after.propertyId);

    await Promise.all(
      [...affected].map((pid) => syncPropertyRatings(pid))
    );
  }
);

