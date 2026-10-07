import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import './styles.css';

const API = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const money = value => `₹${Number(value || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

async function request(path, options = {}) {
  const token = localStorage.getItem('payguard_token');
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(`${API}${path}`, { ...options, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.error || body.message || `Request failed (${response.status})`);
  }
  return response.status === 204 ? null : response.json();
}

function Auth({ onLogin }) {
  const [mode, setMode] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const submit = async e => {
    e.preventDefault(); setError('');
    try {
      const data = await request(`/api/v1/auth/${mode === 'login' ? 'login' : 'signup'}`, {
        method: 'POST', body: JSON.stringify({ username, password })
      });
      localStorage.setItem('payguard_token', data.token);
      localStorage.setItem('payguard_user', data.username);
      onLogin(data);
    } catch (err) { setError(err.message); }
  };
  return <main className="auth-shell">
    <section className="auth-card">
      <div className="eyebrow">PAYGUARD</div><h1>Payment Risk Command Center</h1>
      <p>Secure access to transaction monitoring and fraud-risk decisions.</p>
      <form onSubmit={submit}>
        <label>Username<input value={username} onChange={e => setUsername(e.target.value)} required /></label>
        <label>Password<input type="password" minLength="6" value={password} onChange={e => setPassword(e.target.value)} required /></label>
        {error && <div className="error">{error}</div>}
        <button className="primary" type="submit">{mode === 'login' ? 'Sign in' : 'Create account'}</button>
      </form>
      <button className="link" onClick={() => { setMode(mode === 'login' ? 'signup' : 'login'); setError(''); }}>
        {mode === 'login' ? 'Need an account? Sign up' : 'Already registered? Sign in'}
      </button>
    </section>
  </main>;
}

function TransactionForm({ onCreated }) {
  const [form, setForm] = useState({ userId: 'USR-DEMO', amount: 85000, currency: 'INR', merchantId: 'MER-100', merchantCategory: 'ELECTRONICS', location: 'BENGALURU', deviceId: 'DEV-01', transactionTime: new Date().toISOString().slice(0, 16) });
  const [message, setMessage] = useState('');
  const update = (key, value) => setForm(f => ({ ...f, [key]: value }));
  const submit = async e => {
    e.preventDefault(); setMessage('');
    try {
      const key = crypto.randomUUID();
      const data = await request('/api/v1/transactions', { method: 'POST', headers: { 'Idempotency-Key': key }, body: JSON.stringify({ ...form, amount: Number(form.amount), transactionTime: new Date(form.transactionTime).toISOString() }) });
      setMessage(`${data.transactionId} accepted — risk engine processing asynchronously.`); onCreated();
    } catch (err) { setMessage(err.message); }
  };
  return <div className="panel simulator"><div className="panel-head"><div><span className="eyebrow">TEST PIPELINE</span><h2>Submit transaction</h2></div></div>
    <form className="form-grid" onSubmit={submit}>
      <label>User ID<input value={form.userId} onChange={e=>update('userId',e.target.value)} /></label>
      <label>Amount<input type="number" min="1" value={form.amount} onChange={e=>update('amount',e.target.value)} /></label>
      <label>Merchant<input value={form.merchantId} onChange={e=>update('merchantId',e.target.value)} /></label>
      <label>Category<input value={form.merchantCategory} onChange={e=>update('merchantCategory',e.target.value)} /></label>
      <label>Location<input value={form.location} onChange={e=>update('location',e.target.value)} /></label>
      <label>Device ID<input value={form.deviceId} onChange={e=>update('deviceId',e.target.value)} /></label>
      <label>Transaction time<input type="datetime-local" value={form.transactionTime} onChange={e=>update('transactionTime',e.target.value)} /></label>
      <div className="form-action"><button className="primary" type="submit">Run risk assessment</button></div>
    </form>
    {message && <div className="notice">{message}</div>}
  </div>;
}

function Dashboard({ user, onLogout }) {
  const [tx, setTx] = useState([]); const [risk, setRisk] = useState([]); const [error, setError] = useState('');
  const load = async () => { try { const [t,r] = await Promise.all([request('/api/v1/transactions'), request('/api/v1/risk')]); setTx(t); setRisk(r); setError(''); } catch(e) { setError(e.message); } };
  useEffect(() => { load(); const timer = setInterval(load, 5000); return () => clearInterval(timer); }, []);
  const riskById = useMemo(() => Object.fromEntries(risk.map(r => [r.transactionId, r])), [risk]);
  const high = risk.filter(r => r.decision === 'BLOCK').length; const review = risk.filter(r => r.decision === 'REVIEW').length;
  const chart = risk.slice(0, 12).reverse().map((r, i) => ({ name: i + 1, score: r.riskScore }));
  return <main>
    <header><div><div className="eyebrow">PAYGUARD / RISK OPERATIONS</div><h1>Payment Risk Command Center</h1><p>Event-driven transaction ingestion, hybrid risk scoring and decision monitoring.</p></div><div className="header-actions"><span className="user">{user}</span><button className="ghost" onClick={onLogout}>Sign out</button></div></header>
    {error && <div className="error banner">{error}</div>}
    <section className="cards"><Card label="Transactions" value={tx.length}/><Card label="Blocked" value={high}/><Card label="Manual review" value={review}/><Card label="API" value="UP"/></section>
    <section className="grid top-grid"><div className="panel"><div className="panel-head"><div><span className="eyebrow">RISK TREND</span><h2>Recent risk scores</h2></div><span className="live">● LIVE</span></div><div className="chart"><ResponsiveContainer width="100%" height="100%"><LineChart data={chart}><CartesianGrid strokeDasharray="3 3" opacity={0.12}/><XAxis dataKey="name"/><YAxis domain={[0,100]}/><Tooltip/><Line type="monotone" dataKey="score" strokeWidth={3}/></LineChart></ResponsiveContainer></div></div>
      <div className="panel"><div className="panel-head"><div><span className="eyebrow">DECISIONS</span><h2>Latest assessments</h2></div></div><div className="table">{risk.slice(0,8).map(r=><div className="row" key={r.transactionId}><b>{r.transactionId}</b><span>{r.riskScore}/100</span><span>{r.riskLevel}</span><span className={`decision ${r.decision.toLowerCase()}`}>{r.decision}</span></div>)}{!risk.length && <p>No risk assessments yet. Use the simulator below.</p>}</div></div>
    </section>
    <TransactionForm onCreated={load}/>
    <section className="panel"><div className="panel-head"><div><span className="eyebrow">TRANSACTIONS</span><h2>Recent activity</h2></div></div><div className="table wide">{tx.slice(0,10).map(x=>{const r=riskById[x.transactionId]; return <div className="row" key={x.transactionId}><b>{x.transactionId}</b><span>{money(x.amount)}</span><span>{x.merchantCategory || '—'}</span><span>{x.location || '—'}</span><span className={`decision ${(r?.decision || 'pending').toLowerCase()}`}>{r?.decision || 'PENDING'}</span></div>})}</div></section>
  </main>;
}
function Card({label,value}){return <div className="card"><span>{label}</span><strong>{value}</strong></div>}
function App(){const [auth,setAuth]=useState(()=>localStorage.getItem('payguard_token') ? {username:localStorage.getItem('payguard_user')} : null); if(!auth)return <Auth onLogin={setAuth}/>; return <Dashboard user={auth.username} onLogout={()=>{localStorage.clear();setAuth(null)}}/>}
createRoot(document.getElementById('root')).render(<App/>);
