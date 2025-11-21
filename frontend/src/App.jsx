import React, { useState, useEffect } from 'react';
import { initializeApp } from 'firebase/app';
import { getAuth, onAuthStateChanged, signOut } from 'firebase/auth';
import { getFirestore, collection, onSnapshot, addDoc, doc, deleteDoc, serverTimestamp } from 'firebase/firestore';
import { LucideBook, LucidePlus, LucideLogOut, LucideFileText, LucideTrash2, LucideMessageSquare, LucideList, LucideHeadphones, LucideBrain } from 'lucide-react';

// Import Components
import LoginScreen from './components/LoginScreen';
import ChatMode from './components/ChatMode';
import FlashcardMode from './components/FlashcardMode';
import SummaryMode from './components/SummaryMode';
import AudioMode from './components/AudioMode';
import { uploadDocument } from './api';

// --- Firebase Init ---
// THAY THẾ DÒNG NÀY: const firebaseConfig = JSON.parse(__firebase_config || '{}');
// BẰNG CONFIG THẬT:
const firebaseConfig = {
  apiKey: "AIzaSyD9E4F1FuRqRaDo_lGJohhGxION-J1tydI",
  authDomain: "notebooklm-java.firebaseapp.com",
  projectId: "notebooklm-java",
  storageBucket: "notebooklm-java.firebasestorage.app",
  messagingSenderId: "447141560862",
  appId: "1:447141560862:web:6f7cb1f4038a6bd2d3640f",
  measurementId: "G-NG1X6YBYSC"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);
const appId = typeof __app_id !== 'undefined' ? __app_id : 'default-app-id';

export default function App() {
  const [user, setUser] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [currentDoc, setCurrentDoc] = useState(null);
  const [activeTab, setActiveTab] = useState("chat");
  const [isUploading, setIsUploading] = useState(false);

  // Auth Listener
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (u) => {
      setUser(u);
      if(!u) setCurrentDoc(null);
    });
    return () => unsubscribe();
  }, []);

  // Documents Realtime Listener
  useEffect(() => {
    if (!user) return;
    const q = collection(db, 'artifacts', appId, 'users', user.uid, 'documents');
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const docs = snapshot.docs.map(d => ({ id: d.id, ...d.data() }))
                                .sort((a, b) => b.createdAt?.seconds - a.createdAt?.seconds);
      setDocuments(docs);
    });
    return () => unsubscribe();
  }, [user]);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    setIsUploading(true);
    try {
        // 1. Upload lên Java Backend (Lấy docId và xử lý vector)
        const result = await uploadDocument(file);
        const docId = result.docId;

        // 2. Lưu metadata vào Firestore để hiển thị danh sách
        await addDoc(collection(db, 'artifacts', appId, 'users', user.uid, 'documents'), {
          title: file.name,
          docId: docId,
          createdAt: serverTimestamp(),
        });
    } catch (err) {
        console.error(err);
        alert("Lỗi upload: " + err.message);
    } finally {
        setIsUploading(false);
        e.target.value = null;
    }
  };

  if (!user) return <LoginScreen onLogin={() => {}} />;

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden font-sans text-slate-800">
      {/* SIDEBAR */}
      <div className="w-72 bg-white border-r border-slate-200 flex flex-col shrink-0 shadow-sm z-10">
        <div className="p-5 border-b border-slate-100 font-bold text-xl flex items-center gap-3 text-blue-700">
            <div className="bg-blue-100 p-1.5 rounded-lg"><LucideBook size={20}/></div>
            NotebookLM Java
        </div>
        
        <div className="p-4">
            <label className={`flex items-center justify-center gap-2 bg-blue-600 text-white px-4 py-3 rounded-xl cursor-pointer hover:bg-blue-700 transition-all shadow-md hover:shadow-lg ${isUploading ? 'opacity-70 pointer-events-none' : ''}`}>
                <LucidePlus size={20} /> 
                <span className="font-medium">{isUploading ? "Đang xử lý..." : "Upload PDF Mới"}</span>
                <input type="file" accept=".pdf" className="hidden" onChange={handleFileUpload} />
            </label>
        </div>

        <div className="px-4 pb-2 text-xs font-bold text-slate-400 uppercase tracking-wider">Thư viện của bạn</div>

        <div className="flex-1 overflow-y-auto px-3 space-y-1 scrollbar-thin">
            {documents.map(d => (
                <div key={d.id} onClick={() => {setCurrentDoc(d); setActiveTab('chat');}} className={`p-3 rounded-lg cursor-pointer flex justify-between items-center group transition-all ${currentDoc?.id === d.id ? 'bg-blue-50 text-blue-700 font-medium ring-1 ring-blue-200' : 'hover:bg-slate-50 text-slate-600'}`}>
                    <div className="flex items-center gap-3 truncate">
                        <LucideFileText size={18} className={currentDoc?.id === d.id ? "text-blue-500" : "text-slate-400"} /> 
                        <span className="truncate">{d.title}</span>
                    </div>
                    <button 
                        onClick={(e) => {e.stopPropagation(); deleteDoc(doc(db, 'artifacts', appId, 'users', user.uid, 'documents', d.id))}} 
                        className="text-slate-300 hover:text-red-500 p-1.5 rounded-md hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-all"
                        title="Xóa tài liệu"
                    >
                        <LucideTrash2 size={14} />
                    </button>
                </div>
            ))}
            {documents.length === 0 && (
                <div className="text-center py-8 text-slate-400 text-sm italic">Chưa có tài liệu nào.<br/>Hãy upload file PDF đầu tiên!</div>
            )}
        </div>
        
        <div className="p-4 border-t border-slate-100 bg-slate-50">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 text-white flex items-center justify-center font-bold text-xs">
                        U
                    </div>
                    <span className="text-sm font-medium text-slate-700">Học viên</span>
                </div>
                <button onClick={() => signOut(auth)} className="text-slate-400 hover:text-red-600 transition-colors" title="Đăng xuất">
                    <LucideLogOut size={18}/>
                </button>
            </div>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex-1 flex flex-col min-w-0 bg-slate-50">
        {!currentDoc ? (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400">
                <LucideBook size={64} className="text-slate-200 mb-4"/>
                <p className="text-lg font-medium text-slate-500">Chọn một tài liệu để bắt đầu học</p>
            </div>
        ) : (
            <>
                {/* Header */}
                <div className="bg-white border-b border-slate-200 px-6 py-4 flex justify-between items-center sticky top-0 z-20">
                    <h2 className="font-bold text-lg text-slate-800 truncate flex items-center gap-2">
                        <LucideFileText className="text-blue-600" />
                        {currentDoc.title}
                    </h2>
                    <div className="text-xs text-slate-400 font-mono bg-slate-100 px-2 py-1 rounded">ID: {currentDoc.docId?.substring(0,8)}...</div>
                </div>
                
                {/* Tabs Navigation */}
                <div className="bg-white border-b border-slate-200 px-4 flex gap-1 shadow-sm z-10">
                    {[
                        {id: 'chat', icon: LucideMessageSquare, label: 'Chat AI'},
                        {id: 'summary', icon: LucideList, label: 'Tóm tắt'},
                        {id: 'flashcard', icon: LucideBrain, label: 'Flashcards'},
                        {id: 'audio', icon: LucideHeadphones, label: 'Audio'},
                    ].map(tab => (
                        <button 
                            key={tab.id} 
                            onClick={() => setActiveTab(tab.id)} 
                            className={`px-6 py-4 text-sm font-medium flex items-center gap-2 border-b-2 transition-all ${activeTab===tab.id ? 'border-blue-600 text-blue-600' : 'border-transparent text-slate-500 hover:text-slate-800 hover:bg-slate-50'}`}
                        >
                            <tab.icon size={18} /> {tab.label}
                        </button>
                    ))}
                </div>

                {/* Workspace Area - KỸ THUẬT KEEP-ALIVE */}
                {/* Sử dụng CSS display:none (class 'hidden') thay vì unmount component */}
                <div className="flex-1 overflow-hidden relative">
                    <div className="absolute inset-0 p-4">
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 h-full overflow-hidden">
                            
                            {/* Chat Mode */}
                            <div className={`h-full ${activeTab === 'chat' ? 'block' : 'hidden'}`}>
                                <ChatMode key={currentDoc.id} docId={currentDoc.docId} />
                            </div>

                            {/* Summary Mode */}
                            <div className={`h-full ${activeTab === 'summary' ? 'block' : 'hidden'}`}>
                                {/* Backend tự xử lý việc lấy content từ cache, truyền null ở đây */}
                                <SummaryMode key={currentDoc.id} docId={currentDoc.docId} docContent={null} />
                            </div>

                            {/* Flashcard Mode */}
                            <div className={`h-full ${activeTab === 'flashcard' ? 'block' : 'hidden'}`}>
                                <FlashcardMode key={currentDoc.id} docId={currentDoc.docId} />
                            </div>

                            {/* Audio Mode */}
                            <div className={`h-full ${activeTab === 'audio' ? 'block' : 'hidden'}`}>
                                <AudioMode key={currentDoc.id} docId={currentDoc.docId} docContent={null} />
                            </div>

                        </div>
                    </div>
                </div>
            </>
        )}
      </div>
    </div>
  );
}