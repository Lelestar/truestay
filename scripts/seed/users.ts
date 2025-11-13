import { SeedContext, SeedUser, CreatedUsers, SeededUser } from './types';

function now() {
  return Date.now();
}

const TENANTS: SeedUser[] = [
  { uid: 'tenant_tania', email: 'tania.tremblay@example.com', password: 'Truestay123', firstName: 'Tania', lastName: 'Tremblay', role: 'tenant', phoneNumber: '+14185550101' },
  { uid: 'tenant_marc', email: 'marc.gagnon@example.com', password: 'Truestay123', firstName: 'Marc', lastName: 'Gagnon', role: 'tenant', phoneNumber: '+15145550102' },
  { uid: 'tenant_sophie', email: 'sophie.lavoie@example.com', password: 'Truestay123', firstName: 'Sophie', lastName: 'Lavoie', role: 'tenant', phoneNumber: '+15815550103' },
  { uid: 'tenant_julien', email: 'julien.ouellet@example.com', password: 'Truestay123', firstName: 'Julien', lastName: 'Ouellet', role: 'tenant', phoneNumber: '+18195550104' },
  { uid: 'tenant_aisha', email: 'aisha.benali@example.com', password: 'Truestay123', firstName: 'Aïsha', lastName: 'Benali', role: 'tenant', phoneNumber: '+14505550105' }
];

const LANDLORDS: SeedUser[] = [
  { uid: 'landlord_luc', email: 'luc.dubois@owners.ca', password: 'Truestay123', firstName: 'Luc', lastName: 'Dubois', role: 'landlord', phoneNumber: '+15145550201' },
  { uid: 'landlord_catherine', email: 'catherine.moreau@owners.ca', password: 'Truestay123', firstName: 'Catherine', lastName: 'Moreau', role: 'landlord', phoneNumber: '+14185550202' },
  { uid: 'landlord_etienne', email: 'etienne.lefebvre@owners.ca', password: 'Truestay123', firstName: 'Étienne', lastName: 'Lefebvre', role: 'landlord', phoneNumber: '+14505550203' }
];

async function upsertAuthUser(ctx: SeedContext, u: SeedUser): Promise<SeededUser> {
  try {
    await ctx.auth.createUser({ uid: u.uid, email: u.email, password: u.password });
  } catch (e: any) {
    // if already exists, ignore
    const code = e?.errorInfo?.code || e?.code || '';
    if (!String(code).includes('already-exists')) {
      // Try to fallback: ensure user exists (get or rethrow)
      try {
        await ctx.auth.getUser(u.uid);
      } catch {
        throw e;
      }
    }
  }
  // Ensure email is correct (update may no-op in emulator)
  try {
    await ctx.auth.updateUser(u.uid, { email: u.email });
  } catch {}
  return { ...u, uid: u.uid };
}

async function upsertUserDoc(ctx: SeedContext, u: SeededUser) {
  const createdAt = now();
  await ctx.db.collection('users').doc(u.uid).set({
    id: u.uid,
    email: u.email,
    firstName: u.firstName,
    lastName: u.lastName,
    role: u.role,
    phoneNumber: u.phoneNumber ?? '',
    profilePictureUrl: u.profilePictureUrl ?? '',
    createdAt,
    twoFactorEnabled: false,
    pushNotificationsEnabled: true,
    emailNotificationsEnabled: true
  }, { merge: true });
}

export async function seedUsers(ctx: SeedContext): Promise<CreatedUsers> {
  const tenants: SeededUser[] = [];
  const landlords: SeededUser[] = [];

  for (const t of TENANTS) {
    const su = await upsertAuthUser(ctx, t);
    await upsertUserDoc(ctx, su);
    tenants.push(su);
  }

  for (const l of LANDLORDS) {
    const su = await upsertAuthUser(ctx, l);
    await upsertUserDoc(ctx, su);
    landlords.push(su);
  }

  console.log(`Created/ensured ${tenants.length} tenants and ${landlords.length} landlords.`);
  return { tenants, landlords };
}
