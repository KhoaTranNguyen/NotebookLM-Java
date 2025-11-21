import React, { useState } from 'react';
import { LucideBook, LucideLoader2 } from 'lucide-react';
import { getAuth, signInAnonymously } from 'firebase/auth';

export default function LoginScreen({ onLogin }) {
  const [loading, setLoading] = useState(false);
  const [username, setUsername] = useState("");
  const auth = getAuth();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await signInAnonymously(auth); 
      onLogin(username || "Học viên"); 
    } catch (error) {
      console.error("Login error", error);
      alert("Đăng nhập lỗi: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md text-center">
        <div className="bg-blue-600 w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
            <LucideBook className="text-white w-8 h-8" />
        </div>
        <h1 className="text-2xl font-bold mb-2 text-slate-800">NotebookLM Java</h1>
        <p className="text-slate-500 mb-8 text-sm">Hệ thống học tập thông minh hỗ trợ bởi Gemini</p>
        
        <form onSubmit={handleLogin} className="space-y-4">
          <div className="text-left">
             <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Tên hiển thị</label>
             <input 
                className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" 
                placeholder="Ví dụ: Khoa Trần" 
                value={username} onChange={e => setUsername(e.target.value)}
                required
              />
          </div>
          <button disabled={loading} className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 rounded-lg flex justify-center transition-all active:scale-95">
            {loading ? <LucideLoader2 className="animate-spin" /> : "Bắt đầu học ngay"}
          </button>
        </form>
      </div>
    </div>
  );
}