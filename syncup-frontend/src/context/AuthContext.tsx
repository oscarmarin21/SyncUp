import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { LoginRequest, RegisterRequest } from '@/types/user.types';
import { authService } from '@/services/authService';

interface AuthContextType {
  user: { username: string; nombre: string; rol: string } | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => void;
  updateUser: (updates: Partial<{ nombre: string; rol: string }>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<{ username: string; nombre: string; rol: string } | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Cargar usuario desde localStorage al iniciar
    const storedUser = authService.getUser();
    if (storedUser && authService.isAuthenticated()) {
      setUser(storedUser);
    }
    setIsLoading(false);
  }, []);

  const login = async (credentials: LoginRequest) => {
    const jwtResponse = await authService.login(credentials);
    setUser({
      username: jwtResponse.username,
      nombre: jwtResponse.nombre,
      rol: jwtResponse.rol,
    });
  };

  const register = async (userData: RegisterRequest) => {
    const jwtResponse = await authService.register(userData);
    setUser({
      username: jwtResponse.username,
      nombre: jwtResponse.nombre,
      rol: jwtResponse.rol,
    });
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const updateUser = (updates: Partial<{ nombre: string; rol: string }>) => {
    setUser((prev) => {
      if (!prev) {
        return prev;
      }
      const updated = { ...prev, ...updates };
      authService.setUser(updated);
      return updated;
    });
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isAdmin: user?.rol === 'ADMIN',
    isLoading,
    login,
    register,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

