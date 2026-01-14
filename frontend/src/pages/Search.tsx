import React, { useState } from 'react';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { Link } from 'react-router-dom';

export default function Search() {
  const { token } = useAuth();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  const doSearch = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/users', { headers: { Authorization: `Bearer ${token}` } });
      const users = res.data as any[];
      const q = query.toLowerCase();
      setResults(users.filter(u => (u.displayName || '').toLowerCase().includes(q) || (u.email || '').toLowerCase().includes(q)));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Find users</h2>
      <div>
        <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search by name or email" />
        <button onClick={doSearch} disabled={loading}>Search</button>
      </div>
      <div style={{ marginTop: 12 }}>
        {results.map(u => (
          <div key={u.id} style={{ padding: 8, borderBottom: '1px solid #eee' }}>
            <Link to={`/book/${u.id}`}>{u.displayName ?? u.email}</Link>
            <div style={{ fontSize: 12, color: '#666' }}>{u.email}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

