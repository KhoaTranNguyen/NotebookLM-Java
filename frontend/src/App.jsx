import React, { useState, useEffect } from 'react';
import { LucideBook, LucidePlus, LucideLogOut, LucideFileText, LucideTrash2, LucideMessageSquare, LucideBrain } from 'lucide-react';

// Import Components
import LoginScreen from './components/LoginScreen';
import ChatMode from './components/ChatMode';
import FlashcardMode from './components/FlashcardMode';
import { uploadDocument, getDocuments } from './api';


export default function App() {
  const [user, setUser] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [currentDoc, setCurrentDoc] = useState(null);
  const [activeTab, setActiveTab] = useState("chat");
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');

  const fetchDocuments = async () => {
      if (!user) return;
      try {
        setError('');
        const docs = await getDocuments();
        setDocuments(docs.sort((a, b) => b.documentId - a.documentId));
      } catch(err) {
        setError('Could not fetch documents. Is the backend running?');
        console.error(err);
      }
  };

  // Fetch documents when user logs in
  useEffect(() => {
    fetchDocuments();
  }, [user]);

  const handleLogin = (loggedInUser) => {
    setUser(loggedInUser);
  };
  
  const handleLogout = () => {
    setUser(null);
    setDocuments([]);
    setCurrentDoc(null);
  }

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file || !user) return;
    
    setIsUploading(true);
    try {
        await uploadDocument(file);
        // Refresh the document list after upload
        await fetchDocuments();
    } catch (err) {
        console.error(err);
        alert("Upload error: " + err.message);
    } finally {
        setIsUploading(false);
        e.target.value = null; // Reset file input
    }
  };
  
  const handleDeleteDoc = (docId) => {
      // TODO: Implement backend document deletion
      alert(`Deletion not implemented yet. Would delete document with ID: ${docId}`);
  }

  if (!user) return <LoginScreen onLogin={handleLogin} />;

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
                <span className="font-medium">{isUploading ? "Processing..." : "Upload New PDF"}</span>
                <input type="file" accept=".pdf" className="hidden" onChange={handleFileUpload} disabled={isUploading} />
            </label>
        </div>

        <div className="px-4 pb-2 text-xs font-bold text-slate-400 uppercase tracking-wider">Your Library</div>

        <div className="flex-1 overflow-y-auto px-3 space-y-1 scrollbar-thin">
            {error && <p className='text-red-500 text-xs p-3'>{error}</p>}
            {documents.map(d => (
                <div key={d.documentId} onClick={() => {setCurrentDoc(d); setActiveTab('chat');}} className={`p-3 rounded-lg cursor-pointer flex justify-between items-center group transition-all ${currentDoc?.documentId === d.documentId ? 'bg-blue-50 text-blue-700 font-medium ring-1 ring-blue-200' : 'hover:bg-slate-50 text-slate-600'}`}>
                    <div className="flex items-center gap-3 truncate">
                        <LucideFileText size={18} className={currentDoc?.documentId === d.documentId ? "text-blue-500" : "text-slate-400"} /> 
                        <span className="truncate">{d.filename}</span>
                    </div>
                    <button 
                        onClick={(e) => {e.stopPropagation(); handleDeleteDoc(d.documentId)}} 
                        className="text-slate-300 hover:text-red-500 p-1.5 rounded-md hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-all"
                        title="Delete document"
                    >
                        <LucideTrash2 size={14} />
                    </button>
                </div>
            ))}
            {documents.length === 0 && !error &&(
                <div className="text-center py-8 text-slate-400 text-sm italic">No documents found.<br/>Upload your first PDF!</div>
            )}
        </div>
        
        <div className="p-4 border-t border-slate-100 bg-slate-50">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 text-white flex items-center justify-center font-bold text-xs">
                        {user.username.charAt(0).toUpperCase()}
                    </div>
                    <span className="text-sm font-medium text-slate-700">{user.username}</span>
                </div>
                <button onClick={handleLogout} className="text-slate-400 hover:text-red-600 transition-colors" title="Logout">
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
                <p className="text-lg font-medium text-slate-500">Select a document to begin</p>
            </div>
        ) : (
            <>
                {/* Header */}
                <div className="bg-white border-b border-slate-200 px-6 py-4 flex justify-between items-center sticky top-0 z-20">
                    <h2 className="font-bold text-lg text-slate-800 truncate flex items-center gap-2">
                        <LucideFileText className="text-blue-600" />
                        {currentDoc.filename}
                    </h2>
                    <div className="text-xs text-slate-400 font-mono bg-slate-100 px-2 py-1 rounded">DB ID: {currentDoc.documentId}</div>
                </div>
                
                {/* Tabs Navigation */}
                <div className="bg-white border-b border-slate-200 px-4 flex gap-1 shadow-sm z-10">
                    {[
                        {id: 'chat', icon: LucideMessageSquare, label: 'Chat AI'},
                        {id: 'flashcard', icon: LucideBrain, label: 'Flashcards'},
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

                <div className="flex-1 overflow-hidden relative">
                    <div className="absolute inset-0 p-4">
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 h-full overflow-hidden">
                            <div className={`h-full ${activeTab === 'chat' ? 'block' : 'hidden'}`}>
                                <ChatMode key={currentDoc.documentId} docId={currentDoc.documentId} />
                            </div>
                            <div className={`h-full ${activeTab === 'flashcard' ? 'block' : 'hidden'}`}>
                                <FlashcardMode key={currentDoc.documentId} docId={currentDoc.documentId} />
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