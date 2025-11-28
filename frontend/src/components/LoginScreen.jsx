import React, { useState } from 'react';
import { LucideBook, LucideLoader2 } from 'lucide-react';
import { login, register } from '../api';

export default function LoginScreen({ onLogin }) {
  const [mode, setMode] = useState('login'); // 'login' or 'register'
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [formData, setFormData] = useState({
    username: "student_1",
    password: "SecretPass123",
    confirmPassword: "",
    firstName: "",
    lastName: "",
    email: "",
    dateOfBirth: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      await login(formData.username, formData.password);
      onLogin();
    } catch (error) {
      console.error("Login error", error);
      setError(error.message || "Login failed. Please check credentials and server status.");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      const result = await register(formData);
      setSuccess(result.message || "Registration successful! Please log in.");
      setMode('login'); // Switch back to login mode
    } catch (error) {
      console.error("Registration error", error);
      setError(error.message || "Registration failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const renderLoginForm = () => (
    <form onSubmit={handleLogin} className="space-y-4">
      <div className="text-left">
        <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Username</label>
        <input name="username" value={formData.username} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="e.g., student_1" required />
      </div>
      <div className="text-left">
        <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Password</label>
        <input name="password" type="password" value={formData.password} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="e.g., SecretPass123" required />
      </div>
      <button disabled={loading} className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 rounded-lg flex justify-center transition-all active:scale-95 disabled:opacity-70">
        {loading ? <LucideLoader2 className="animate-spin" /> : "Login"}
      </button>
    </form>
  );

  const renderRegisterForm = () => (
    <form onSubmit={handleRegister} className="space-y-3">
        <div className="grid grid-cols-2 gap-3">
            <div className="text-left">
                <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">First Name</label>
                <input name="firstName" value={formData.firstName} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="John" required />
            </div>
            <div className="text-left">
                <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Last Name</label>
                <input name="lastName" value={formData.lastName} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="Doe" required />
            </div>
        </div>
         <div className="text-left">
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Email</label>
            <input name="email" type="email" value={formData.email} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="you@example.com" required />
        </div>
        <div className="text-left">
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Username</label>
            <input name="username" value={formData.username} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="new_user" required />
        </div>
         <div className="text-left">
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Date of Birth</label>
            <input name="dateOfBirth" type="date" value={formData.dateOfBirth} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" required />
        </div>
        <div className="grid grid-cols-2 gap-3">
            <div className="text-left">
                <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Password</label>
                <input name="password" type="password" value={formData.password} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="••••••••" required />
            </div>
            <div className="text-left">
                <label className="block text-xs font-bold text-slate-500 uppercase mb-1 ml-1">Confirm Password</label>
                <input name="confirmPassword" type="password" value={formData.confirmPassword} onChange={handleChange} className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" placeholder="••••••••" required />
            </div>
        </div>
      <button disabled={loading} className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 rounded-lg flex justify-center transition-all active:scale-95 disabled:opacity-70">
        {loading ? <LucideLoader2 className="animate-spin" /> : "Create Account"}
      </button>
    </form>
  );

  return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md text-center transition-all">
        <div className="bg-blue-600 w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
            <LucideBook className="text-white w-8 h-8" />
        </div>
        <h1 className="text-2xl font-bold mb-2 text-slate-800">
            {mode === 'login' ? "Welcome Back" : "Create New Account"}
        </h1>
        <p className="text-slate-500 mb-6 text-sm">
            {mode === 'login' ? "Login to access your intelligent study assistant" : "Join and start your smart learning journey"}
        </p>
        
        {error && <p className="bg-red-100 text-red-700 text-sm p-3 rounded-lg mb-4">{error}</p>}
        {success && <p className="bg-green-100 text-green-700 text-sm p-3 rounded-lg mb-4">{success}</p>}

        {mode === 'login' ? renderLoginForm() : renderRegisterForm()}

        <div className='mt-6 text-sm'>
            {mode === 'login' ? (
                <p className="text-slate-500">
                    Don't have an account?{' '}
                    <button onClick={() => { setMode('register'); setError(''); setSuccess(''); setFormData({username:'', password:'', confirmPassword:'', firstName:'', lastName:'', email:'', dateOfBirth:''}); }} className="font-medium text-blue-600 hover:underline">
                        Register
                    </button>
                </p>
            ) : (
                <p className="text-slate-500">
                    Already have an account?{' '}
                    <button onClick={() => { setMode('login'); setError(''); setSuccess(''); setFormData({username:'student_1', password:'SecretPass123', confirmPassword:'', firstName:'', lastName:'', email:'', dateOfBirth:''}); }} className="font-medium text-blue-600 hover:underline">
                        Login
                    </button>
                </p>
            )}
        </div>
      </div>
    </div>
  );
}