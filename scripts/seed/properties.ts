import { SeedContext, CreatedUsers, SeedProperty, SeedRoom, SeedRoomElement, SeedAddress } from './types';

function idFrom(db: SeedContext['db'], col: string) {
  return db.collection(col).doc().id;
}

function now() { return Date.now(); }

// Map element type to French display name
export function getElementName(type: SeedRoomElement['type']): string {
  const names: Record<SeedRoomElement['type'], string> = {
    floor: 'Sol',
    wall: 'Murs',
    ceiling: 'Plafond',
    window: 'Fenêtre',
    door: 'Porte',
    furniture: 'Mobilier',
    equipment: 'Équipements'
  };
  return names[type] || type;
}

function el(type: SeedRoomElement['type']): SeedRoomElement {
  return {
    id: Math.random().toString(36).slice(2, 9),
    type,
    elementName: getElementName(type)
  };
}

function baseRooms(): SeedRoom[] {
  return [
    { id: 'room_bed_1', name: 'Chambre', type: 'bedroom', elements: [el('floor'), el('wall'), el('ceiling'), el('window'), el('door')] },
    { id: 'room_liv_1', name: 'Salon', type: 'living_room', elements: [el('floor'), el('wall'), el('ceiling'), el('window'), el('door')] },
    { id: 'room_kit_1', name: 'Cuisine', type: 'kitchen', elements: [el('floor'), el('wall'), el('ceiling'), el('equipment')] },
    { id: 'room_bath_1', name: 'Salle de bain', type: 'bathroom', elements: [el('floor'), el('wall'), el('ceiling'), el('equipment')] }
  ];
}

function addr(street: string, city: string, postalCode: string, latitude: number, longitude: number): SeedAddress {
  return { street, city, postalCode, province: 'QC', country: 'CA', latitude, longitude };
}

type CreatedProperty = SeedProperty;

export async function seedProperties(ctx: SeedContext, users: CreatedUsers): Promise<CreatedProperty[]> {
  const landlords = users.landlords.map(l => l.uid);
  const created: CreatedProperty[] = [];
  const createdAt = now();

  // Credible Quebec cities and varied listings
  const seeds: Omit<SeedProperty, 'id' | 'createdAt' | 'updatedAt'>[] = [
    {
      name: 'Condo lumineux au Plateau',
      description: '2 chambres, près du Parc La Fontaine, idéal pour jeunes pros.',
      address: addr('4350 Av. Papineau', 'Montréal', 'H2H 1T8', 45.5376, -73.5660),
      monthlyRent: 1950,
      surface: 78,
      rooms: baseRooms(),
      photos: [
        'https://picsum.photos/seed/plateau-condo-1/1280/960',
        'https://picsum.photos/seed/plateau-condo-2/1280/960'
      ],
      landlordId: landlords[0],
      isInBuilding: true,
      isAvailable: true,
      status: 'published',
      ratings: { propertyAverageRating: 4.5, propertyReviewCount: 8, buildingAverageRating: 4.2, buildingReviewCount: 5, neighborhoodAverageRating: 4.8, neighborhoodReviewCount: 12 }
    },
    {
      name: 'Appartement rénové Saint-Roch',
      description: '1 chambre, cuisine moderne, proche des restos et bus.',
      address: addr('220 Rue Saint-Joseph E', 'Québec', 'G1K 3A9', 46.8143, -71.2147),
      monthlyRent: 1200,
      surface: 50,
      rooms: baseRooms().slice(0, 3),
      photos: [
        'https://picsum.photos/seed/saint-roch-app-1/1280/960'
      ],
      landlordId: landlords[1],
      isInBuilding: true,
      isAvailable: true,
      status: 'published',
      ratings: { propertyAverageRating: 4.1, propertyReviewCount: 3, buildingAverageRating: 3.9, buildingReviewCount: 2, neighborhoodAverageRating: 4.6, neighborhoodReviewCount: 6 }
    },
    {
      name: 'Maison familiale à Laval',
      description: '3 chambres, cour arrière, quartier tranquille.',
      address: addr('1450 Rue des Érables', 'Laval', 'H7M 2R8', 45.5855, -73.7127),
      monthlyRent: 2350,
      surface: 120,
      rooms: [...baseRooms(), { id: 'room_bed_2', name: 'Chambre 2', type: 'bedroom', elements: [el('floor'), el('wall'), el('ceiling')] }],
      photos: [
        'https://picsum.photos/seed/laval-house-1/1280/960',
        'https://picsum.photos/seed/laval-house-2/1280/960'
      ],
      landlordId: landlords[2],
      isInBuilding: false,
      isAvailable: true,
      status: 'published',
      ratings: { propertyAverageRating: 4.7, propertyReviewCount: 5, buildingAverageRating: 0, buildingReviewCount: 0, neighborhoodAverageRating: 4.3, neighborhoodReviewCount: 4 }
    },
    {
      name: 'Studio Lévis',
      description: 'Petit studio économique, parfait étudiant.',
      address: addr('75 Rue Wolfe', 'Lévis', 'G6V 3X5', 46.8065, -71.1758),
      monthlyRent: 720,
      surface: 28,
      rooms: [{ id: 'room_main', name: 'Pièce principale', type: 'other', elements: [el('floor'), el('wall'), el('ceiling')] }],
      photos: [
        'https://picsum.photos/seed/levis-studio-1/1280/960'
      ],
      landlordId: landlords[1],
      isInBuilding: true,
      isAvailable: true,
      status: 'published',
      ratings: { propertyAverageRating: 3.8, propertyReviewCount: 2, buildingAverageRating: 3.5, buildingReviewCount: 1, neighborhoodAverageRating: 4.0, neighborhoodReviewCount: 2 }
    },
    {
      name: '4 1/2 Sherbrooke Est',
      description: 'Spacieux 4 1/2, balcon avant, près du Cégep.',
      address: addr('980 Rue King E', 'Sherbrooke', 'J1G 1E4', 45.4049, -71.8735),
      monthlyRent: 1050,
      surface: 65,
      rooms: baseRooms(),
      photos: [
        'https://picsum.photos/seed/sherbrooke-apt-1/1280/960'
      ],
      landlordId: landlords[0],
      isInBuilding: true,
      isAvailable: true,
      status: 'paused',
      ratings: { propertyAverageRating: 4.0, propertyReviewCount: 1, buildingAverageRating: 3.6, buildingReviewCount: 1, neighborhoodAverageRating: 4.2, neighborhoodReviewCount: 2 }
    },
    {
      name: 'Condo Vieux-Longueuil',
      description: '3 1/2 moderne, stationnement, climatisation.',
      address: addr('310 Rue Saint-Charles O', 'Longueuil', 'J4H 1E4', 45.5318, -73.5126),
      monthlyRent: 1380,
      surface: 56,
      rooms: baseRooms().slice(0, 3),
      photos: [
        'https://picsum.photos/seed/longueuil-condo-1/1280/960',
        'https://picsum.photos/seed/longueuil-condo-2/1280/960'
      ],
      landlordId: landlords[2],
      isInBuilding: true,
      isAvailable: true,
      status: 'published',
      ratings: { propertyAverageRating: 4.3, propertyReviewCount: 2, buildingAverageRating: 4.1, buildingReviewCount: 1, neighborhoodAverageRating: 4.4, neighborhoodReviewCount: 3 }
    },
    {
      name: 'Triplex Gatineau - RDC',
      description: 'Logement RDC rénové, près des parcs.',
      address: addr('25 Rue Saint-René O', 'Gatineau', 'J8X 2V8', 45.4298, -75.7172),
      monthlyRent: 1290,
      surface: 60,
      rooms: baseRooms().slice(0, 3),
      photos: [
        'https://picsum.photos/seed/gatineau-triplex-1/1280/960'
      ],
      landlordId: landlords[0],
      isInBuilding: true,
      isAvailable: true,
      status: 'published',
      ratings: { propertyAverageRating: 4.2, propertyReviewCount: 2, buildingAverageRating: 3.9, buildingReviewCount: 1, neighborhoodAverageRating: 4.1, neighborhoodReviewCount: 2 }
    },
    {
      name: 'Maison jumelée Trois-Rivières',
      description: 'Quartier Cap-de-la-Madeleine, 2 étages, sous-sol fini.',
      address: addr('640 Rue des Forges', 'Trois-Rivières', 'G9A 2H3', 46.3457, -72.5413),
      monthlyRent: 1650,
      surface: 110,
      rooms: [...baseRooms(), { id: 'room_bed_3', name: 'Chambre 3', type: 'bedroom', elements: [el('floor'), el('wall'), el('ceiling')] }],
      photos: [
        'https://picsum.photos/seed/trois-rivieres-house-1/1280/960'
      ],
      landlordId: landlords[1],
      isInBuilding: false,
      isAvailable: true,
      status: 'draft',
      ratings: { propertyAverageRating: 0, propertyReviewCount: 0, buildingAverageRating: 0, buildingReviewCount: 0, neighborhoodAverageRating: 0, neighborhoodReviewCount: 0 }
    }
  ];

  for (const s of seeds) {
    const id = idFrom(ctx.db, 'properties');
    const doc: SeedProperty = { id, createdAt, updatedAt: createdAt, ...s };
    await ctx.db.collection('properties').doc(id).set(doc);
    created.push(doc);
  }

  console.log(`Created ${created.length} properties.`);
  return created;
}
