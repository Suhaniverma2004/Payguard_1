import React,{useEffect,useState} from 'react';
import {createRoot} from 'react-dom/client';
import {ResponsiveContainer,LineChart,Line,XAxis,YAxis,Tooltip} from 'recharts';
import './styles.css';
const API=import.meta.env.VITE_API_URL||'http://localhost:8080';
function App(){
 const [tx,setTx]=useState([]); const [loading,setLoading]=useState(true);
 const load=()=>fetch(`${API}/api/v1/transactions`).then(r=>r.json()).then(setTx).finally(()=>setLoading(false));
 useEffect(()=>{load(); const i=setInterval(load,5000); return()=>clearInterval(i)},[]);
 const total=tx.length, high=tx.filter(x=>x.amount>=50000).length;
 const chart=tx.slice(0,10).reverse().map((x,i)=>({name:i+1,amount:Number(x.amount)}));
 return <main><header><div><div className="eyebrow">PAYGUARD</div><h1>Payment Risk Command Center</h1><p>Real-time transaction monitoring and fraud risk ingestion.</p></div><span className="live">● LIVE</span></header>
 <section className="cards"><Card label="Transactions" value={total}/><Card label="High-value" value={high}/><Card label="API" value="UP"/></section>
 <section className="grid"><div className="panel"><h2>Transaction Volume</h2><div className="chart"><ResponsiveContainer width="100%" height="100%"><LineChart data={chart}><XAxis dataKey="name"/><YAxis/><Tooltip/><Line type="monotone" dataKey="amount" strokeWidth={2}/></LineChart></ResponsiveContainer></div></div>
 <div className="panel"><h2>Recent Transactions</h2>{loading?<p>Loading...</p>:<div className="table">{tx.slice(0,8).map(x=><div className="row" key={x.transactionId}><b>{x.transactionId}</b><span>₹{Number(x.amount).toLocaleString('en-IN')}</span><span>{x.location||'—'}</span><span className={Number(x.amount)>=50000?'risk':''}>{Number(x.amount)>=50000?'REVIEW':'RECEIVED'}</span></div>)}</div>}</div></section>
 </main>}
function Card({label,value}){return <div className="card"><span>{label}</span><strong>{value}</strong></div>}
createRoot(document.getElementById('root')).render(<App/>);
