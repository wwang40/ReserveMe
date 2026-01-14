import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Register() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const res = await api.post('/api/auth/register', { email, password, displayName });
      // assume success when status 201 returned; API returns tokens currently
      setSuccess('Registration successful — redirecting to login...');
      setTimeout(() => navigate('/login'), 1200);
    } catch (err: any) {
      // inspect axios error
      if (err?.response?.status === 409) {
        setError('Email is already registered');
      } else if (err?.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Registration failed.');
      }
    }
  };

  return (
    <form onSubmit={submit}>
      <div>
        <label>Email</label>
        <input value={email} onChange={(e) => setEmail((e.target as HTMLInputElement).value)} />
      </div>
      <div>
        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword((e.target as HTMLInputElement).value)} />
      </div>
      <div>
        <label>Display name</label>
        <input value={displayName} onChange={(e) => setDisplayName((e.target as HTMLInputElement).value)} />
      </div>
      <button type="submit">Register</button>
      {success && <div style={{ color: 'green', marginTop: 8 }}>{success}</div>}
      {error && <div style={{ color: 'red', marginTop: 8 }}>{error}</div>}
    </form>
  );
}
