import { City } from './city.model';
import { Client } from './client.model';
import { Package } from './package.model';
import { PaymentMode, LTAStatus } from './common.model';

export interface LTA {
  id?: number;
  ltaNumber?: string;
  trackingNumber?: string;
  qrCodeData?: string;
  originCity: City;
  destinationCity: City;
  paymentMode: PaymentMode;
  client?: Client; // Required only when paymentMode is TO_INVOICE
  totalWeight: number;
  packageNature: string;
  packageCount: number;
  calculatedCost?: number;
  totalCost?: number;
  status: LTAStatus;
  shipperName?: string;
  shipperAddress?: string;
  shipperPhone?: string;
  consigneeName?: string;
  consigneeAddress?: string;
  consigneePhone?: string;
  specialInstructions?: string;
  declaredValue?: number;
  pickupDate?: string;
  deliveryDate?: string;
  packages?: Package[];
  createdAt?: string;
  updatedAt?: string;
}

export interface LTARequest {
  originCityId: number | null;
  destinationCityId: number | null;
  paymentMode: PaymentMode;
  clientId?: number; // Required when paymentMode is TO_INVOICE
  totalWeight: number;
  packageNature: string;
  packageCount: number;
  shipperName?: string;
  shipperAddress?: string;
  shipperPhone?: string;
  consigneeName?: string;
  consigneeAddress?: string;
  consigneePhone?: string;
  specialInstructions?: string;
  declaredValue?: number;
  pickupDate?: string;
  packages?: Package[];
}

export interface LTAResponse extends LTA {
  id: number;
  ltaNumber: string;
  calculatedCost: number;
  createdAt: string;
  updatedAt: string;
}

export interface LTAListResponse {
  content: LTAResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface LTAStats {
  pendingCount: number;
  confirmedCount: number;
  inTransitCount: number;
  deliveredCount: number;
  cancelledCount: number;
}

export interface LTASearchParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
  status?: LTAStatus;
  originCityId?: number;
  destinationCityId?: number;
  clientId?: number;
  paymentMode?: PaymentMode;
  dateFrom?: string;
  dateTo?: string;
  search?: string; // Search in LTA number, shipper, consignee
}
