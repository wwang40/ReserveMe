import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function BookCalendar() {
  const { id } = useParams();
  const { token } = useAuth();
  const [user, setUser] = useState<any>(null);
  const [slots, setSlots] = useState<any[]>([]);

  useEffect(() => {
    if (!id || !token) return;
    (async () => {
      const u = await api.get(`/api/users`, { headers: { Authorization: `Bearer ${token}` } });
      // find the user in list (simple approach)
      const found = (u.data as any[]).find(x => x.id === id);
      setUser(found);
      // fetch slots by owner
      const s = await api.get(`/api/slots/byOwner?ownerId=${id}`, { headers: { Authorization: `Bearer ${token}` } });
      setSlots(s.data);
    })();
  }, [id, token]);

  if (!user) return <div>Loading user...</div>;

  return (
    <div>
      <h2>Book with {user.displayName ?? user.email}</h2>
      <div>
        <p>Available slots:</p>
        {slots.length === 0 && <div>No availability listed</div>}
        {slots.map(s => (
          <div key={s.id} style={{ padding: 6, borderBottom: '1px solid #ddd' }}>
            {new Date(s.startTime).toLocaleString()} - {new Date(s.endTime).toLocaleString()}
            <div>
              <button disabled>Request (not implemented)</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

