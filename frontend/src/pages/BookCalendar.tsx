import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function BookCalendar() {
  const { id } = useParams(); // the ID of the user whose calendar we are booking
  const { token } = useAuth(); // only token available
  const [user, setUser] = useState<any>(null);
  const [slots, setSlots] = useState<any[]>([]);
  const [loadingRequest, setLoadingRequest] = useState<string | null>(null); // track which slot is being requested

  // --- Load user and slots ---
  useEffect(() => {
    if (!id || !token) return;
    (async () => {
      try {
        // Fetch all users and find the one we want
        const u = await api.get(`/api/users`, { headers: { Authorization: `Bearer ${token}` } });
        const found = (u.data as any[]).find(x => x.id === id);
        setUser(found);

        // Fetch slots for that user
        const s = await api.get(`/api/slots/byOwner?ownerId=${id}`, { headers: { Authorization: `Bearer ${token}` } });
        setSlots(s.data);
      } catch (err) {
        console.error('Failed to load user or slots', err);
      }
    })();
  }, [id, token]);

  if (!user) return <div>Loading user...</div>;

  // --- Request Slot Handler ---
  const handleRequestSlot = async (slotId: string) => {
    if (!token) return;

    setLoadingRequest(slotId);
    try {
      // Only send slotId; backend uses JWT to determine the user
      const payload = { slotId };
      // POST to the reservations endpoint (backend expects CreateReservationRequest)
      const res = await api.post('/api/reservations', payload, {
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log('Request created:', res.data);
      alert('Request sent successfully!');
      // Remove the requested slot from the local list so the UI updates immediately
      setSlots(prev => prev.filter(s => s.id !== slotId));
    } catch (err: any) {
      console.error('Failed to request slot:', err);
      if (err.response?.status === 403) {
        alert('You are not allowed to request this slot.');
      } else {
        alert('Failed to request slot. Please try again.');
      }
    } finally {
      setLoadingRequest(null);
    }
  };

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
                  <button
                      onClick={() => handleRequestSlot(s.id)}
                      disabled={loadingRequest === s.id}
                  >
                    {loadingRequest === s.id ? 'Requesting...' : 'Request'}
                  </button>
                </div>
              </div>
          ))}
        </div>
      </div>
  );
}
