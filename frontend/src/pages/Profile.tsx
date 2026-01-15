import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';

type Slot = {
    id: string;
    owner: {
        id: string;
        displayName: string;
    };
    startTime: string;
    endTime: string;
};

type Reservation = {
    id: string;
    slot: Slot;
    user: {
        id: string;
        displayName: string;
    };
    status: string;
    createdAt: string;
};

export default function Profile() {
    const { token } = useAuth();
    const [me, setMe] = useState<any>(null);

    const [slots, setSlots] = useState<Slot[]>([]);
    const [incoming, setIncoming] = useState<Reservation[]>([]);
    const [confirmed, setConfirmed] = useState<Reservation[]>([]);

    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');

    const authHeaders = {
        Authorization: `Bearer ${token}`,
    };

    useEffect(() => {
        if (!token) return;
        (async () => {
            const res = await api.get('/api/users/me', { headers: authHeaders });
            setMe(res.data);
        })();
    }, [token]);

    const loadSlots = async (userId: string) => {
        const res = await api.get(`/api/slots/byOwner?ownerId=${userId}`);
        setSlots(res.data);
    };

    const loadReservations = async (userId: string) => {
        const res = await api.get(`/api/reservations?userId=${userId}`, {
            headers: authHeaders,
        });

        const all: Reservation[] = res.data;

        setIncoming(
            all.filter(
                r => r.slot.owner.id === userId && r.user.id !== userId && r.status === 'ACTIVE'
            )
        );

        setConfirmed(
            // Show confirmed reservations where the current user is either the requester or the slot owner
            all.filter(r => (r.user.id === userId || r.slot.owner.id === userId) && r.status === 'CONFIRMED')
        );
    };

    useEffect(() => {
        if (!me) return;
        loadSlots(me.id);
        loadReservations(me.id);
    }, [me]);

    const createSlot = async () => {
        if (!startTime || !endTime) return;

        const payload = {
            startTime: new Date(startTime).toISOString(),
            endTime: new Date(endTime).toISOString(),
        };

        try {
            const res = await api.post('/api/slots', payload, { headers: authHeaders });
            console.log('Created slot:', res.data);

            setStartTime('');
            setEndTime('');

            loadSlots(me.id);
        } catch (err) {
            console.error('Failed to create slot:', err);
        }
    };

    const deleteSlot = async (slotId: string) => {
        if (!window.confirm('Delete this availability slot?')) return;
        await api.delete(`/api/slots/${slotId}`, { headers: authHeaders });
        loadSlots(me.id);
    };

    const deleteReservation = async (reservationId: string) => {
        await api.delete(`/api/reservations/${reservationId}`, {
            headers: authHeaders,
        });
        loadReservations(me.id);
    };

    const rejectIncoming = async (r: Reservation) => {
        const ok = window.confirm(
            `Reject request from ${r.user.displayName}?`
        );
        if (!ok) return;
        await deleteReservation(r.id);
    };

    const cancelConfirmed = async (r: Reservation) => {
        // Show the other party's name: if current user is the requester, show the slot owner; otherwise show the requester
        const otherName = me && me.id === r.user.id ? r.slot.owner.displayName : r.user.displayName;
        const ok = window.confirm(
            `Cancel your reservation with ${otherName}?`
        );
        if (!ok) return;
        await deleteReservation(r.id);
    };

    if (!me) return <div>Loading...</div>;

    return (
        <div>
            <h2>Your profile</h2>
            <div>Email: {me.email}</div>
            <div>Display name: {me.displayName}</div>
            <div>Created: {new Date(me.createdAt).toLocaleString()}</div>

            <section style={{ marginTop: 24 }}>
                <h3>Your Availability</h3>

                <div style={{ display: 'flex', gap: 8 }}>
                    <input
                        type="datetime-local"
                        value={startTime}
                        onChange={e => setStartTime(e.target.value)}
                    />
                    <input
                        type="datetime-local"
                        value={endTime}
                        onChange={e => setEndTime(e.target.value)}
                    />
                    <button onClick={createSlot}>Add</button>
                </div>

                <ul>
                    {slots.map(s => (
                        <li key={s.id}>
                            {new Date(s.startTime).toLocaleString()} →{' '}
                            {new Date(s.endTime).toLocaleString()}
                            <button onClick={() => deleteSlot(s.id)}>Delete</button>
                        </li>
                    ))}
                </ul>
            </section>

            <section style={{ marginTop: 24 }}>
                <h3>Incoming Requests</h3>
                {incoming.length === 0 && <div>No incoming requests</div>}
                <ul>
                    {incoming.map(r => (
                        <li key={r.id}>
                            {r.user.displayName} —{' '}
                            {new Date(r.slot.startTime).toLocaleString()}
                            <button onClick={async () => { try { await api.put(`/api/reservations/${r.id}/confirm`, null, { headers: authHeaders }); loadReservations(me.id); } catch (err) { console.error('Failed to confirm', err); alert('Failed to confirm reservation'); } }}>
                                Accept
                            </button>
                            <button onClick={() => rejectIncoming(r)}>Reject</button>
                        </li>
                    ))}
                </ul>
            </section>

            <section style={{ marginTop: 24 }}>
                <h3>Your Confirmed Reservations</h3>
                {confirmed.length === 0 && <div>No confirmed reservations</div>}
                <ul>
                    {confirmed.map(r => (
                        <li key={r.id}>
                            With {me && me.id === r.user.id ? r.slot.owner.displayName : r.user.displayName} —{' '}
                            {new Date(r.slot.startTime).toLocaleString()}
                            <button onClick={() => cancelConfirmed(r)}>Cancel</button>
                        </li>
                    ))}
                </ul>
            </section>
        </div>
    );
}
