import type * as admin from 'firebase-admin';

export type SeedContext = {
  db: admin.firestore.Firestore;
  auth: admin.auth.Auth;
};

export type SeedUser = {
  uid: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: 'tenant' | 'landlord';
  phoneNumber?: string;
  profilePictureUrl?: string;
};

export type SeededUser = SeedUser & {
  uid: string;
};

export type SeedAddress = {
  street: string;
  city: string;
  postalCode: string;
  province: string;
  country: string;
  latitude?: number | null;
  longitude?: number | null;
};

export type SeedRoomElement = {
  id: string;
  type: 'floor' | 'wall' | 'ceiling' | 'window' | 'door' | 'furniture' | 'equipment';
  elementName?: string;
};

export type SeedRoom = {
  id: string;
  name: string;
  type:
    | 'bedroom'
    | 'living_room'
    | 'kitchen'
    | 'bathroom'
    | 'toilet'
    | 'entrance'
    | 'hallway'
    | 'dining_room'
    | 'office'
    | 'laundry_room'
    | 'storage_room'
    | 'garage'
    | 'basement'
    | 'attic'
    | 'balcony'
    | 'terrace'
    | 'garden'
    | 'veranda'
    | 'staircase'
    | 'other';
  elements: SeedRoomElement[];
};

export type SeedProperty = {
  id: string;
  name: string;
  description: string;
  address: SeedAddress;
  monthlyRent: number;
  surface: number;
  rooms: SeedRoom[];
  photos: string[];
  landlordId: string;
  isInBuilding: boolean;
  isAvailable: boolean;
  status: 'draft' | 'published' | 'paused' | 'archived';
  ratings: {
    propertyAverageRating: number;
    propertyReviewCount: number;
    buildingAverageRating: number;
    buildingReviewCount: number;
    neighborhoodAverageRating: number;
    neighborhoodReviewCount: number;
  };
  createdAt: number;
  updatedAt: number;
};

export type SeedRental = {
  id: string;
  propertyId: string;
  tenantId: string;
  landlordId: string;
  startDate: number;
  endDate: number;
  status: 'pending' | 'active' | 'ended' | 'cancelled';
  entryInventoryId?: string | null;
  exitInventoryId?: string | null;
  reviewId?: string | null;
  createdAt: number;
  acceptedAt?: number | null;
};

export type SeedFavorite = {
  id: string;
  userId: string;
  propertyId: string;
  addedAt: number;
};

export type SeedReview = {
  id: string;
  rentalId: string;
  propertyId: string;
  tenantId: string;
  propertyReview?: {
    generalCondition: number;
    comfort: number;
    compliance: number;
    valueForMoney: number;
    overallRating: number;
    comment: string;
    photos: string[];
  } | null;
  buildingReview?: {
    maintenance: number;
    neighborhood: number;
    security: number;
    services: number;
    overallRating: number;
    comment: string;
    photos: string[];
  } | null;
  neighborhoodReview?: {
    transport: number;
    amenities: number;
    calm: number;
    safety: number;
    atmosphere: number;
    overallRating: number;
    comment: string;
    photos: string[];
  } | null;
  createdAt: number;
};

export type SeedInventory = {
  id: string;
  rentalId: string;
  type: 'entry' | 'exit';
  rooms: Array<{
    roomId: string;
    roomName: string;
    elements: Array<{
      elementId: string;
      elementName: string;
      condition: 'good' | 'to_check' | 'damaged' | 'not_applicable';
      comment: string;
      photoUrls: string[];
    }>;
    status: 'todo' | 'in_progress' | 'completed';
    photoUrls: string[];
  }>;
  landlordSignature?: {
    userId: string;
    signatureImageUrl: string;
    signedAt: number;
  } | null;
  tenantSignature?: {
    userId: string;
    signatureImageUrl: string;
    signedAt: number;
  } | null;
  status: 'draft' | 'in_progress' | 'pending_signature' | 'signed' | 'completed' | 'cancelled';
  pdfUrl?: string | null;
  createdAt: number;
  completedAt?: number | null;
};

export type CreatedUsers = {
  tenants: SeededUser[];
  landlords: SeededUser[];
};

