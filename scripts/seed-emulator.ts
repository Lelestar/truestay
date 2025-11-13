import * as admin from 'firebase-admin';
import { seedUsers } from './seed/users';
import { seedProperties } from './seed/properties';
import { seedRentals } from './seed/rentals';
import { seedFavorites } from './seed/favorites';
import { seedReviews } from './seed/reviews';
import { SeedContext } from './seed/types';

// Point towards the local emulators
process.env.FIRESTORE_EMULATOR_HOST = 'localhost:8080';
process.env.FIREBASE_AUTH_EMULATOR_HOST = 'localhost:9099';

admin.initializeApp({ projectId: 'truestay-8inf865' });

async function main() {
  const db = admin.firestore();
  const auth = admin.auth();

  const ctx: SeedContext = { db, auth };

  console.log('--- Seeding users (tenants & landlords)');
  const users = await seedUsers(ctx);

  console.log('--- Seeding properties across Quebec');
  const properties = await seedProperties(ctx, users);

  console.log('--- Seeding rentals (active, pending, ended)');
  const rentals = await seedRentals(ctx, users, properties);

  console.log('--- Seeding favorites for tenants');
  await seedFavorites(ctx, users, properties);

  console.log('--- Seeding reviews for ended rentals');
  await seedReviews(ctx, users, properties, rentals);

  console.log('✅ Seed complete');
}

main()
  .then(() => process.exit(0))
  .catch((e) => {
    console.error('Seed failed', e);
    process.exit(1);
  });
