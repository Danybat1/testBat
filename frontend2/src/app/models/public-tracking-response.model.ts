export interface LTAStatusHistory {
  id: number;
  previousStatus: string | null;
  newStatus: string;
  changedBy: string;
  changeReason?: string;
  changedAt: string;
  statusLabel: string;
  statusDescription: string;
}

export interface CityInfo {
  id: number;
  name: string;
  iataCode: string;
  country: string;
}

export interface PublicTrackingResponse {
  trackingNumber: string;
  ltaNumber: string;
  status: string;
  originCity: CityInfo;
  destinationCity: CityInfo;
  shipperName: string;
  consigneeName: string;
  totalWeight: number;
  packageCount: number;
  packageNature: string;
  createdAt: string;
  updatedAt: string;
  estimatedDelivery?: string;
  statusHistory: LTAStatusHistory[];
}
