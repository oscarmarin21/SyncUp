export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
export const MEDIA_BASE_URL = import.meta.env.VITE_MEDIA_BASE_URL || 'http://localhost:8080';

export const TOKEN_KEY = 'syncup_token';
export const USER_KEY = 'syncup_user';

export const ROLES = {
  USER: 'USER',
  ADMIN: 'ADMIN',
} as const;

