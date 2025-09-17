export interface Account {
  id: number;
  accountNumber: string;
  accountName: string;
  accountType: AccountType;
  parentAccount?: Account;
  subAccounts?: Account[];
  isActive: boolean;
  description?: string;
  balance: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface JournalEntry {
  id: number;
  entryNumber: string;
  entryDate: Date;
  description: string;
  reference?: string;
  sourceType: SourceType;
  sourceId?: number;
  fiscalYear: FiscalYear;
  totalDebit: number;
  totalCredit: number;
  isBalanced: boolean;
  status: EntryStatus;
  accountingEntries: AccountingEntry[];
  createdBy: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface AccountingEntry {
  id: number;
  journalEntry: JournalEntry;
  account: Account;
  description: string;
  debitAmount: number;
  creditAmount: number;
  currency: string;
  exchangeRate: number;
  createdAt: Date;
}

export interface FiscalYear {
  id: number;
  year: number;
  startDate: Date;
  endDate: Date;
  isClosed: boolean;
  description?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface BalanceSummary {
  accountNumber: string;
  accountName: string;
  accountType: AccountType;
  totalDebit: number;
  totalCredit: number;
  balance: number;
  currency: string;
}

export interface DashboardStats {
  cashBalance: number;
  bankBalance: number;
  totalInvoicesIssued: number;
  totalInvoicesCollected: number;
  pendingEntries: number;
  unbalancedEntries: number;
  currentFiscalYear: string;
}

export interface AccountingRule {
  id: number;
  eventType: string;
  description: string;
  debitAccount: string;
  creditAccount: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface AuditLog {
  id: number;
  action: string;
  entityType: string;
  entityId: number;
  oldValue?: string;
  newValue?: string;
  userId: string;
  timestamp: Date;
  ipAddress?: string;
}

export enum AccountType {
  ASSET = 'ASSET',
  LIABILITY = 'LIABILITY',
  EQUITY = 'EQUITY',
  REVENUE = 'REVENUE',
  EXPENSE = 'EXPENSE'
}

export enum SourceType {
  INVOICE = 'INVOICE',
  PAYMENT = 'PAYMENT',
  LTA = 'LTA',
  TREASURY = 'TREASURY',
  MANUAL = 'MANUAL',
  ADJUSTMENT = 'ADJUSTMENT',
  OPENING = 'OPENING',
  CLOSING = 'CLOSING'
}

export enum EntryStatus {
  DRAFT = 'DRAFT',
  POSTED = 'POSTED',
  REVERSED = 'REVERSED'
}

export interface JournalEntryFilter {
  startDate?: Date;
  endDate?: Date;
  sourceType?: SourceType;
  status?: EntryStatus;
  accountNumber?: string;
  reference?: string;
}

export interface TrialBalanceFilter {
  fiscalYearId?: number;
  startDate?: Date;
  endDate?: Date;
  accountType?: AccountType;
  currency?: string;
  includeInactive?: boolean;
}
