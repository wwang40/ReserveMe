import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../services/api';

type User = { id: string; email: string; displayName?: string } | null;

const AuthContext = createContext<{
  user: User;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  register: (email: string, password: string, displayName?: string) => Promise<void>;
} | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User>(() => {
    try { return JSON.parse(localStorage.getItem('rm_user') || 'null'); } catch { return null; }
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('rm_token'));

  useEffect(() => {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete api.defaults.headers.common['Authorization'];
    }
  }, [token]);

  useEffect(() => {
    if (user) localStorage.setItem('rm_user', JSON.stringify(user)); else localStorage.removeItem('rm_user');
  }, [user]);

  useEffect(() => {
    if (token) localStorage.setItem('rm_token', token); else localStorage.removeItem('rm_token');
  }, [token]);

  const login = async (email: string, password: string) => {
    const res = await api.post('/api/auth/login', { email, password });
    setToken(res.data.accessToken);
    const me = await api.get('/api/users/me');
    setUser(me.data);
  };

  const register = async (email: string, password: string, displayName?: string) => {
    const res = await api.post('/api/auth/register', { email, password, displayName });
    setToken(res.data.accessToken);
    const me = await api.get('/api/users/me');
    setUser(me.data);
  };

  const logout = () => {
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
