import * as admin from 'firebase-admin';
import { seedUsers } from './seed/users';
import { seedProperties } from './seed/properties';
import { seedRentals } from './seed/rentals';
import { seedFavorites } from './seed/favorites';
import { seedReviews } from './seed/reviews';
import { SeedContext } from './seed/types';

// Import Firebase credentials
const serviceAccount = require('../serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'truestay-8inf865'
});

async function main() {
  console.log('⚠️  WARNING: You are about to seed PRODUCTION Firebase!');
  console.log('This will create real data in your Firebase project.');

  // Wait for 5 seconds to allow user to cancel if needed
  await new Promise(resolve => setTimeout(resolve, 5000));

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

  console.log('✅ Production seed complete');
}

main()
  .then(() => process.exit(0))
  .catch((e) => {
    console.error('Seed failed', e);
    process.exit(1);
  });
