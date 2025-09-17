export interface Package {
  id?: number;
  weight: number;
  description?: string;
  trackingNumber?: string;
  packageSequence?: number;
  ltaId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PackageRequest {
  weight: number;
  description?: string;
  trackingNumber?: string;
}

export interface PackageResponse extends Package {
  id: number;
  trackingNumber: string;
  packageSequence: number;
  createdAt: string;
  updatedAt: string;
}

export interface PackageWithLTA extends PackageResponse {
  ltaNumber?: string;
  ltaStatus?: string;
}
