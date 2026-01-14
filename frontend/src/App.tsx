import React from 'react';
import { Routes, Route, Navigate, BrowserRouter, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import Search from './pages/Search';
import BookCalendar from './pages/BookCalendar';

function AppRoutes() {
  const { user, logout } = useAuth();
  return (
    <div style={{ padding: 20 }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>ReserveMe</h1>
        <div>
          {user ? (
            <>
              <Link to="/">Home</Link> | <Link to="/profile">Profile</Link> |{' '}
              <button onClick={logout}>Logout</button>
            </>
          ) : (
            <Link to="/login">Login</Link>
          )}
        </div>
      </header>
      <main style={{ marginTop: 12 }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/profile" element={user ? <Profile /> : <Navigate to="/login" replace />} />
          <Route path="/search" element={user ? <Search /> : <Navigate to="/login" replace />} />
          <Route path="/book/:id" element={user ? <BookCalendar /> : <Navigate to="/login" replace />} />
          <Route path="/" element={user ? <Dashboard /> : <Navigate to="/login" replace />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
        <AppRoutes />
    </AuthProvider>
  );
}
