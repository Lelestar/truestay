import * as admin from 'firebase-admin';

// Point towards the local emulators
process.env.FIRESTORE_EMULATOR_HOST = 'localhost:8080';
process.env.FIREBASE_AUTH_EMULATOR_HOST = 'localhost:9099';

admin.initializeApp({ projectId: 'truestay-8inf865' });

const db = admin.firestore();
const auth = admin.auth();

async function createTestUser() {
  try {
    // Create in Auth
    const userRecord = await auth.createUser({
      uid: 'HN2TP1rYO1CqOSQuiPYP52mHct4r',
      email: 'test@example.com',
      password: '12345678'
    });

    console.log('Auth user created:', userRecord.uid);

    // Create in Firestore
    await db.collection('users').doc(userRecord.uid).set({
      id: userRecord.uid,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      phoneNumber: '',
      role: 'tenant',
      createdAt: 1730489282000,
      profilePictureUrl: '',
      twoFactorEnabled: false,
      pushNotificationsEnabled: true,
      emailNotificationsEnabled: true
    });

    console.log('Firestore document created');
    console.log('Test user ready! Email: test@example.com, Password: 12345678');
  } catch (error) {
    console.error('Error:', error);
  }
}

createTestUser().then(() => process.exit(0));