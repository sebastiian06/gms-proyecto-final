export type Role = 'ADMIN' | 'RECEPTIONIST' | 'TRAINER' | 'MEMBER'

export type PaymentMethod = 'CASH' | 'TRANSFER' | 'CARD'

export interface User {
  id: number
  username: string
  role: Role
  active: boolean
  isSystem: boolean
}

export interface LoginResponse {
  token: string
  expiresAt: string
  user: User
}

export interface Member {
  id: number
  userId: number
  documentNumber: string
  fullName: string
  contact: string
  active: boolean
  trainerId?: number | null
  trainerName?: string | null
  accessCode?: string | null
}

export interface Plan {
  id: number
  name: string
  price: number
  durationDays: number
  active: boolean
}

export interface Trainer {
  id: number
  userId: number
  fullName: string
}

export interface Membership {
  id: number
  memberId: number
  planId: number
  lockedPrice: number
  startDate: string
  endDate: string
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED'
}

export interface Payment {
  id: number
  membershipId: number
  amount: number
  method: PaymentMethod
  recordedDate: string
}

export interface AttendanceRecord {
  id: number
  memberId: number
  timestamp: string
  direction: 'IN' | 'OUT'
}

export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

export interface Exercise {
  id: number
  routineId: number
  name: string
  sets: number
  repetitions: number
  displayOrder: number
}

export interface Routine {
  id: number
  trainerId: number
  trainerName: string
  name: string
  active: boolean
}

export interface PaymentRecord {
  id: number
  membershipId: number
  amount: number
  method: string
  recordedDate: string
}