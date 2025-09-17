export interface Shipment {
  id: number;
  trackingNumber: string;
  referenceNumber?: string;
  customerReference?: string;
  status: ShipmentStatus;
  serviceType?: ServiceType;
  
  // Sender Information
  senderName: string;
  senderCompany?: string;
  senderAddress: string;
  senderCity: string;
  senderPostalCode?: string;
  senderCountry: string;
  senderPhone?: string;
  senderEmail?: string;
  
  // Recipient Information
  recipientName: string;
  recipientCompany?: string;
  recipientAddress: string;
  recipientCity: string;
  recipientPostalCode?: string;
  recipientCountry: string;
  recipientPhone?: string;
  recipientEmail?: string;
  
  // Package Information
  packageCount: number;
  packageWeight?: number;
  totalWeight?: number;
  totalVolume?: number;
  declaredValue?: number;
  currency?: string;
  description?: string;
  
  // Dates
  pickupDate?: Date;
  expectedDeliveryDate?: Date;
  actualDeliveryDate?: Date;
  createdAt: Date;
  updatedAt: Date;
  
  // Delivery Information
  deliverySignature?: string;
  deliveryNotes?: string;
  specialInstructions?: string;
  
  // Business Information
  clientId?: number;
  agentId?: number;
  manifestId?: number;
  
  // Tracking Events
  trackingEvents?: TrackingEvent[];
}

export interface TrackingEvent {
  id: number;
  shipmentId: number;
  status: ShipmentStatus;
  eventDate: Date;
  location?: string;
  city?: string;
  country?: string;
  description: string;
  details?: string;
  operatorName?: string;
  operatorId?: number;
  facilityName?: string;
  facilityCode?: string;
  signatureName?: string;
  proofOfDelivery?: string;
  exceptionCode?: string;
  exceptionReason?: string;
  nextAttemptDate?: Date;
  createdAt: Date;
  updatedAt: Date;
}

export enum ShipmentStatus {
  PENDING = 'PENDING',
  CREATED = 'CREATED',
  CONFIRMED = 'CONFIRMED',
  PICKUP_SCHEDULED = 'PICKUP_SCHEDULED',
  PICKED_UP = 'PICKED_UP',
  IN_TRANSIT = 'IN_TRANSIT',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  DELIVERY_ATTEMPTED = 'DELIVERY_ATTEMPTED',
  EXCEPTION = 'EXCEPTION',
  RETURNED_TO_SENDER = 'RETURNED_TO_SENDER',
  CANCELLED = 'CANCELLED',
  LOST = 'LOST',
  DAMAGED = 'DAMAGED'
}

export enum ServiceType {
  STANDARD = 'STANDARD',
  EXPRESS = 'EXPRESS',
  OVERNIGHT = 'OVERNIGHT',
  ECONOMY = 'ECONOMY',
  SAME_DAY = 'SAME_DAY',
  INTERNATIONAL_STANDARD = 'INTERNATIONAL_STANDARD',
  INTERNATIONAL_EXPRESS = 'INTERNATIONAL_EXPRESS'
}

export interface ShipmentSearchCriteria {
  trackingNumber?: string;
  senderName?: string;
  recipientName?: string;
  status?: ShipmentStatus;
  clientId?: number;
  page?: number;
  size?: number;
}

export interface ShipmentSearchRequest {
  trackingNumber?: string;
  senderName?: string;
  recipientName?: string;
  status?: ShipmentStatus;
  clientId?: number;
  page?: number;
  size?: number;
}

export interface ShipmentStatistics {
  total: number;
  inTransit: number;
  overdue: number;
  byStatus: { [key: string]: number };
}

export interface PagedResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Status labels in French
export const SHIPMENT_STATUS_LABELS: { [key in ShipmentStatus]: string } = {
  [ShipmentStatus.PENDING]: 'En attente',
  [ShipmentStatus.CREATED]: 'Créé',
  [ShipmentStatus.CONFIRMED]: 'Confirmé',
  [ShipmentStatus.PICKUP_SCHEDULED]: 'Enlèvement programmé',
  [ShipmentStatus.PICKED_UP]: 'Enlevé',
  [ShipmentStatus.IN_TRANSIT]: 'En transit',
  [ShipmentStatus.OUT_FOR_DELIVERY]: 'En cours de livraison',
  [ShipmentStatus.DELIVERED]: 'Livré',
  [ShipmentStatus.DELIVERY_ATTEMPTED]: 'Tentative de livraison',
  [ShipmentStatus.EXCEPTION]: 'Exception',
  [ShipmentStatus.RETURNED_TO_SENDER]: 'Retourné à l\'expéditeur',
  [ShipmentStatus.CANCELLED]: 'Annulé',
  [ShipmentStatus.LOST]: 'Perdu',
  [ShipmentStatus.DAMAGED]: 'Endommagé'
};

// Service type labels in French
export const SERVICE_TYPE_LABELS: { [key in ServiceType]: string } = {
  [ServiceType.STANDARD]: 'Standard',
  [ServiceType.EXPRESS]: 'Express',
  [ServiceType.OVERNIGHT]: 'Overnight',
  [ServiceType.ECONOMY]: 'Économique',
  [ServiceType.SAME_DAY]: 'Même jour',
  [ServiceType.INTERNATIONAL_STANDARD]: 'International Standard',
  [ServiceType.INTERNATIONAL_EXPRESS]: 'International Express'
};

// Status colors for UI
export const SHIPMENT_STATUS_COLORS: { [key in ShipmentStatus]: string } = {
  [ShipmentStatus.PENDING]: '#6c757d',
  [ShipmentStatus.CREATED]: '#6c757d',
  [ShipmentStatus.CONFIRMED]: '#17a2b8',
  [ShipmentStatus.PICKUP_SCHEDULED]: '#ffc107',
  [ShipmentStatus.PICKED_UP]: '#fd7e14',
  [ShipmentStatus.IN_TRANSIT]: '#007bff',
  [ShipmentStatus.OUT_FOR_DELIVERY]: '#20c997',
  [ShipmentStatus.DELIVERED]: '#28a745',
  [ShipmentStatus.DELIVERY_ATTEMPTED]: '#ffc107',
  [ShipmentStatus.EXCEPTION]: '#dc3545',
  [ShipmentStatus.RETURNED_TO_SENDER]: '#6f42c1',
  [ShipmentStatus.CANCELLED]: '#6c757d',
  [ShipmentStatus.LOST]: '#dc3545',
  [ShipmentStatus.DAMAGED]: '#dc3545'
};
