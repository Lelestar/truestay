import { SeedContext, CreatedUsers, SeedProperty, SeedFavorite } from './types';

function idFrom(db: SeedContext['db'], col: string) { return db.collection(col).doc().id; }
function now() { return Date.now(); }

export async function seedFavorites(
  ctx: SeedContext,
  users: CreatedUsers,
  properties: SeedProperty[]
): Promise<SeedFavorite[]> {
  const out: SeedFavorite[] = [];
  const tenants = users.tenants.map(t => t.uid);

  if (tenants.length === 0 || properties.length === 0) return out;

  // Each of first 3 tenants favorites 2 different properties, if available
  const sliceTenants = tenants.slice(0, Math.min(3, tenants.length));
  for (let i = 0; i < sliceTenants.length; i++) {
    const tenantId = sliceTenants[i];
    const picks = [properties[i % properties.length], properties[(i + 2) % properties.length]];
    for (const p of picks) {
      const id = idFrom(ctx.db, 'favorites');
      const f: SeedFavorite = { id, userId: tenantId, propertyId: p.id, addedAt: now() - (i + 1) * 1000 };
      await ctx.db.collection('favorites').doc(id).set(f);
      out.push(f);
    }
  }

  console.log(`Created ${out.length} favorites.`);
  return out;
}

