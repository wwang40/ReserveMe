import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function Dashboard() {
  const { user } = useAuth();
  return (
    <div>
      <h2>Welcome {user?.displayName ?? user?.email ?? 'User'}</h2>
      <div style={{ marginTop: 12 }}>
        <Link to="/search">Search Users & Book</Link>
      </div>
      <div style={{ marginTop: 8 }}>
        <Link to="/profile">Your Profile / Availability</Link>
      </div>
    </div>
  );
}

