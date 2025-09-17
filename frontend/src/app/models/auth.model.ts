export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  username: string;
  roles: string[];
}

export interface User {
  username: string;
  roles: string[];
}

export interface TokenValidationResponse {
  valid: boolean;
  username?: string;
}
