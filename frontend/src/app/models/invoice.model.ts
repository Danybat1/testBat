import { Client } from './client.model';
import { LTA } from './lta.model';

export enum InvoiceStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED'
}

export enum InvoiceType {
  TRANSPORT = 'TRANSPORT',
  PASSENGER = 'PASSENGER',
  ADDITIONAL_SERVICES = 'ADDITIONAL_SERVICES',
  MIXED = 'MIXED'
}

export enum PaymentTerms {
  IMMEDIATE = 'IMMEDIATE',
  NET_15 = 'NET_15',
  NET_30 = 'NET_30',
  NET_45 = 'NET_45',
  NET_60 = 'NET_60'
}

export enum Currency {
  CDF = 'CDF', // Franc Congolais
  USD = 'USD'  // Dollar Américain
}

export interface InvoiceItem {
  id?: number;
  description: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  taxRate: number;
  taxAmount: number;
  ltaId?: number; // Reference to LTA if applicable
  ltaNumber?: string;
}

export interface InvoiceAddress {
  name: string;
  address: string;
  city: string;
  postalCode: string;
  country: string;
  taxId?: string;
  email?: string;
  phone?: string;
}

export interface Invoice {
  id?: number;
  invoiceNumber: string;
  invoiceDate: string;
  dueDate: string;
  status: InvoiceStatus;
  type: InvoiceType;
  
  // Client information
  client: Client;
  billingAddress: InvoiceAddress;
  
  // Company information (issuer)
  companyInfo: InvoiceAddress;
  
  // Financial details
  subtotal: number;
  totalTax: number;
  totalAmount: number;
  paidAmount?: number;
  remainingAmount?: number;
  
  // Payment details
  paymentTerms: PaymentTerms;
  paymentMethod?: string;
  bankDetails?: string;
  
  // Items
  items: InvoiceItem[];
  
  // Additional information
  notes?: string;
  internalNotes?: string;
  currency: Currency;
  exchangeRate?: number;
  
  // Related LTAs
  relatedLTAs?: LTA[];
  
  // Timestamps
  createdAt?: string;
  updatedAt?: string;
  sentAt?: string;
  paidAt?: string;
}

export interface InvoiceRequest {
  clientId: number;
  invoiceDate: string;
  dueDate: string;
  type: InvoiceType;
  paymentTerms: PaymentTerms;
  billingAddress: InvoiceAddress;
  items: Omit<InvoiceItem, 'id'>[];
  notes?: string;
  currency: Currency;
  ltaIds?: number[]; // LTAs to include in this invoice
}

export interface InvoiceResponse extends Invoice {
  id: number;
  invoiceNumber: string;
  createdAt: string;
  updatedAt: string;
}

export interface InvoiceListResponse {
  content: InvoiceResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface InvoiceStats {
  totalInvoices: number;
  draftCount: number;
  sentCount: number;
  paidCount: number;
  overdueCount: number;
  totalRevenue: number;
  pendingAmount: number;
  overdueAmount: number;
}

export interface InvoiceSearchParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
  status?: InvoiceStatus;
  type?: InvoiceType;
  clientId?: number;
  dateFrom?: string;
  dateTo?: string;
  dueDateFrom?: string;
  dueDateTo?: string;
  search?: string; // Search in invoice number, client name
}
