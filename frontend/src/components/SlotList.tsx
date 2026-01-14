import React, { useEffect, useState } from 'react';
import api from '../services/api';

export default function SlotList() {
  const [slots, setSlots] = useState<any[]>([]);

  useEffect(() => {
    (async () => {
      const res = await api.get('/api/slots');
      setSlots(res.data);
    })();
  }, []);

  return (
    <div>
      <h2>Slots</h2>
      <ul>
        {slots.map(s => (
          <li key={s.id}>{s.owner?.displayName ?? s.owner?.email} — {s.startTime} to {s.endTime}</li>
        ))}
      </ul>
    </div>
  );
}

