import * as fs from 'node:fs';
import * as path from 'node:path';
import * as admin from 'firebase-admin';

// Emulators
process.env.FIRESTORE_EMULATOR_HOST = 'localhost:8080';
process.env.FIREBASE_AUTH_EMULATOR_HOST = 'localhost:9099';
process.env.FIREBASE_STORAGE_EMULATOR_HOST = 'localhost:9199';

admin.initializeApp({ projectId: 'truestay-8inf865' });

type PhotoMapItem = {
  propertyId?: string;
  name?: string;
  photos: string[];
};

async function findPropertyIdByName(db: FirebaseFirestore.Firestore, name: string): Promise<string | null> {
  const snap = await db.collection('properties').where('name', '==', name).limit(1).get();
  if (snap.empty) return null;
  return snap.docs[0].id;
}

async function main() {
  const db = admin.firestore();
  const mapPath = path.resolve(__dirname, 'photo-map.json');
  if (!fs.existsSync(mapPath)) {
    console.error('Missing scripts/seed/photo-map.json');
    process.exit(1);
  }
  const content = fs.readFileSync(mapPath, 'utf-8');
  const items: PhotoMapItem[] = JSON.parse(content);

  for (const item of items) {
    let propId = item.propertyId ?? null;
    if (!propId && item.name) {
      propId = await findPropertyIdByName(db, item.name);
    }
    if (!propId) {
      console.warn('Skipping mapping entry: cannot resolve property id for', item);
      continue;
    }
    await db.collection('properties').doc(propId).set({ photos: item.photos, updatedAt: Date.now() }, { merge: true });
    console.log(`Updated photos for property ${propId}${item.name ? ` (${item.name})` : ''}`);
  }
}

main().then(() => process.exit(0)).catch((e) => { console.error(e); process.exit(1); });

