import { SeedContext, CreatedUsers, SeedProperty, SeedRental, SeedReview } from './types';

function idFrom(db: SeedContext['db'], col: string) { return db.collection(col).doc().id; }
function now() { return Date.now(); }

export async function seedReviews(
  ctx: SeedContext,
  _users: CreatedUsers,
  _properties: SeedProperty[],
  rentals: SeedRental[]
): Promise<SeedReview[]> {
  const ended = rentals.filter(r => r.status === 'ended');
  const out: SeedReview[] = [];
  for (const r of ended) {
    const id = idFrom(ctx.db, 'reviews');
    const propertyReview = {
      generalCondition: 4,
      comfort: 5,
      compliance: 4,
      valueForMoney: 4,
      overallRating: (4 + 5 + 4 + 4) / 4,
      comment: 'Bel appartement, conforme à la description. Quelques bruits le soir.',
      photos: []
    };
    const buildingReview = {
      maintenance: 4,
      neighborhood: 4,
      security: 4,
      services: 4,
      overallRating: (4 + 4 + 4 + 4) / 4,
      comment: 'Immeuble bien entretenu, voisins respectueux.',
      photos: []
    };
    const neighborhoodReview = {
      transport: 5,
      amenities: 4,
      calm: 3,
      safety: 4,
      atmosphere: 4,
      overallRating: (5 + 4 + 3 + 4 + 4) / 5,
      comment: 'Quartier vivant, très bien desservi en transports.',
      photos: []
    };
    const doc: SeedReview = {
      id,
      rentalId: r.id,
      propertyId: r.propertyId,
      tenantId: r.tenantId,
      propertyReview,
      buildingReview,
      neighborhoodReview,
      createdAt: now()
    };
    await ctx.db.collection('reviews').doc(id).set(doc);
    await ctx.db.collection('rentals').doc(r.id).set({ reviewId: id }, { merge: true });
    out.push(doc);
  }

  console.log(`Created ${out.length} reviews for ended rentals.`);
  return out;
}

