import { SeedContext, CreatedUsers, SeedProperty, SeedRental, SeedInventory } from './types';

function idFrom(db: SeedContext['db'], col: string) { return db.collection(col).doc().id; }
function now() { return Date.now(); }

export async function seedRentals(
  ctx: SeedContext,
  users: CreatedUsers,
  properties: SeedProperty[]
): Promise<SeedRental[]> {
  const tenants = users.tenants.map(t => t.uid);
  const landlords = users.landlords.map(l => l.uid);
  const rentals: SeedRental[] = [];

  if (properties.length < 3 || tenants.length < 2 || landlords.length < 1) {
    console.warn('Not enough data to create varied rentals. Skipping.');
    return rentals;
  }

  const [p1, p2, p3] = properties;
  const [t1, t2, t3] = tenants;
  const l1 = landlords[0];
  const l2 = landlords[1] ?? landlords[0];

  const createdAt = now();

  // Active rental on p1
  {
    const id = idFrom(ctx.db, 'rentals');
    const start = createdAt - 60 * 24 * 60 * 60 * 1000; // ~2 months ago
    const end = createdAt + 10 * 30 * 24 * 60 * 60 * 1000; // ~10 months ahead
    const r: SeedRental = {
      id, propertyId: p1.id, tenantId: t1, landlordId: p1.landlordId,
      startDate: start, endDate: end, status: 'active', entryInventoryId: null, exitInventoryId: null, reviewId: null,
      createdAt, acceptedAt: createdAt - 59 * 24 * 60 * 60 * 1000
    };
    await ctx.db.collection('rentals').doc(id).set(r);
    rentals.push(r);
    // Mark property unavailable
    await ctx.db.collection('properties').doc(p1.id).set({ isAvailable: false, updatedAt: now() }, { merge: true });

    // Create entry inventory (draft)
    const invId = idFrom(ctx.db, 'inventories');
    const inventory: SeedInventory = {
      id: invId,
      rentalId: id,
      type: 'entry',
      rooms: (p1.rooms || []).slice(0, 2).map((room) => ({
        roomId: room.id,
        roomName: room.name,
        elements: (room.elements || []).slice(0, 2).map((e) => ({ elementId: e.id, condition: 'to_check', comment: '', photoUrls: [] })),
        status: 'todo'
      })),
      landlordSignature: null,
      tenantSignature: null,
      status: 'draft',
      pdfUrl: null,
      createdAt
    };
    await ctx.db.collection('inventories').doc(invId).set(inventory);
    await ctx.db.collection('rentals').doc(id).set({ entryInventoryId: invId }, { merge: true });
  }

  // Pending rental on p2
  {
    const id = idFrom(ctx.db, 'rentals');
    const start = createdAt + 15 * 24 * 60 * 60 * 1000; // in 2 weeks
    const end = start + 12 * 30 * 24 * 60 * 60 * 1000; // 12 months
    const r: SeedRental = {
      id, propertyId: p2.id, tenantId: t2, landlordId: p2.landlordId,
      startDate: start, endDate: end, status: 'pending', entryInventoryId: null, exitInventoryId: null, reviewId: null,
      createdAt
    };
    await ctx.db.collection('rentals').doc(id).set(r);
    rentals.push(r);
  }

  // Ended rental on p3
  {
    const id = idFrom(ctx.db, 'rentals');
    const start = createdAt - 18 * 30 * 24 * 60 * 60 * 1000; // 18 months ago
    const end = createdAt - 6 * 30 * 24 * 60 * 60 * 1000; // ended 6 months ago
    const r: SeedRental = {
      id, propertyId: p3.id, tenantId: t3 ?? tenants[0], landlordId: p3.landlordId || l2,
      startDate: start, endDate: end, status: 'ended', entryInventoryId: null, exitInventoryId: null, reviewId: null,
      createdAt, acceptedAt: start + 2 * 24 * 60 * 60 * 1000
    };
    await ctx.db.collection('rentals').doc(id).set(r);
    rentals.push(r);

    // Completed inventories (entry & exit)
    const entryId = idFrom(ctx.db, 'inventories');
    const exitId = idFrom(ctx.db, 'inventories');
    const entry: SeedInventory = {
      id: entryId,
      rentalId: id,
      type: 'entry',
      rooms: (p3.rooms || []).slice(0, 2).map((room) => ({
        roomId: room.id, roomName: room.name,
        elements: (room.elements || []).slice(0, 2).map((e) => ({ elementId: e.id, condition: 'good', comment: '', photoUrls: [] })),
        status: 'completed'
      })),
      landlordSignature: { userId: r.landlordId, signatureImageUrl: '', signedAt: start },
      tenantSignature: { userId: r.tenantId, signatureImageUrl: '', signedAt: start },
      status: 'completed', pdfUrl: null, createdAt: start, completedAt: start
    };
    const exit: SeedInventory = {
      id: exitId,
      rentalId: id,
      type: 'exit',
      rooms: (p3.rooms || []).slice(0, 2).map((room) => ({
        roomId: room.id, roomName: room.name,
        elements: (room.elements || []).slice(0, 2).map((e) => ({ elementId: e.id, condition: 'good', comment: '', photoUrls: [] })),
        status: 'completed'
      })),
      landlordSignature: { userId: r.landlordId, signatureImageUrl: '', signedAt: end },
      tenantSignature: { userId: r.tenantId, signatureImageUrl: '', signedAt: end },
      status: 'completed', pdfUrl: null, createdAt: end - 2 * 24 * 60 * 60 * 1000, completedAt: end
    };
    await ctx.db.collection('inventories').doc(entryId).set(entry);
    await ctx.db.collection('inventories').doc(exitId).set(exit);
    await ctx.db.collection('rentals').doc(id).set({ entryInventoryId: entryId, exitInventoryId: exitId }, { merge: true });
  }

  console.log(`Created ${rentals.length} rentals (with inventories).`);
  return rentals;
}

