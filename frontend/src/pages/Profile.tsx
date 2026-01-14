import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';

export default function Profile() {
  const { token } = useAuth();
  const [me, setMe] = useState<any>(null);

  useEffect(() => {
    if (!token) return;
    (async () => {
      const res = await api.get('/api/users/me', { headers: { Authorization: `Bearer ${token}` } });
      setMe(res.data);
    })();
  }, [token]);

  if (!me) return <div>Loading...</div>;

  return (
    <div>
      <h2>Your profile</h2>
      <div>Email: {me.email}</div>
      <div>Display name: {me.displayName}</div>
      <div>Created: {new Date(me.createdAt).toLocaleString()}</div>
      <div style={{ marginTop: 12 }}>
        <p>Availability editor and incoming requests will be added here.</p>
      </div>
    </div>
  );
}

