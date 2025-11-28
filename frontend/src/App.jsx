import React, { useState, useEffect } from 'react';
import { LucideBook, LucidePlus, LucideLogOut, LucideFileText, LucideTrash2, LucideMessageSquare, LucideBrain, LucideLoader2 } from 'lucide-react';

// Import Components
import LoginScreen from './components/LoginScreen';
import ChatMode from './components/ChatMode';
import FlashcardMode from './components/FlashcardMode';
import { uploadDocument, getDocuments, getFlashcardSet, getFlashcardSets, deleteFlashcardSet, updateFlashcardSetTopic, deleteDocument, logout } from './api';
import SavedFlashcardsPanel from './components/SavedFlashcardsPanel';


export default function App() {
  const [user, setUser] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [savedSets, setSavedSets] = useState([]); // State for saved sets

  // State for the main content view
  const [activeTab, setActiveTab] = useState("chat");
  const [activeContent, setActiveContent] = useState({ type: 'none' }); // type: 'none', 'doc', 'flashcardSet'

  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');

  const handleLogout = () => {
    logout();
    setUser(null);
    setDocuments([]);
    setSavedSets([]);
    setActiveContent({ type: 'none' });
  }

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        const decodedToken = JSON.parse(atob(token.split('.')[1]));
        setUser({ username: decodedToken.sub });
      } catch (error) {
        console.error("Failed to decode token", error);
        // Handle invalid token, e.g., by logging out
        handleLogout();
      }
    }
  }, []);

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

  const fetchSavedSets = async () => {
    if (!user) return;
    try {
      const sets = await getFlashcardSets();
      setSavedSets(sets);
    } catch (err) {
      console.error("Could not fetch saved sets:", err);
      // Don't show a blocking error, the panel will show its own.
    }
  };

  // Fetch initial data when user logs in
  useEffect(() => {
    if (user) {
      fetchDocuments();
      fetchSavedSets();
    }
  }, [user]);

  const handleLogin = () => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        const decodedToken = JSON.parse(atob(token.split('.')[1]));
        setUser({ username: decodedToken.sub });
      } catch (error) {
        console.error("Failed to decode token", error);
        // Handle invalid token
        handleLogout();
      }
    }
  };

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
  
  const handleDeleteDoc = async (docId) => {
      const isConfirmed = window.confirm("Are you sure you want to delete this document? All associated data (chunks and embeddings) will be permanently removed.");
      if (isConfirmed) {
          try {
              await deleteDocument(docId);
              // If the deleted doc is the one being viewed, go back to home screen
              if (activeContent.type === 'doc' && activeContent.id === docId) {
                  setActiveContent({ type: 'none' });
              }
              // Refresh the document list
              await fetchDocuments();
          } catch(err) {
              console.error("Failed to delete document:", err);
              alert("Error: " + err.message);
          }
      }
  };

  // When a document is selected from the library
  const handleSelectDocument = (doc) => {
    setActiveContent({ type: 'doc', id: doc.documentId, title: doc.filename });
    setActiveTab('chat');
  };

  // When a saved flashcard set is selected
  const handleSelectFlashcardSet = async (setId, setTitle) => {
    setActiveContent({ type: 'flashcardSet', id: setId, title: setTitle, cards: 'loading' });
    setActiveTab('flashcard');
    
    try {
      const cards = await getFlashcardSet(setId);
      setActiveContent({ type: 'flashcardSet', id: setId, title: setTitle, cards: cards });
    } catch (err) {
      console.error("Failed to fetch flashcard set:", err);
      alert("Failed to load the selected flashcard set.");
      setActiveContent({ type: 'none' }); // Revert on error
    }
  };
  
  const handleDeleteSet = async (setId) => {
      const isConfirmed = window.confirm("Are you sure? If you delete this, the flashcard set will be gone forever.");
      if (isConfirmed) {
          try {
              await deleteFlashcardSet(setId);
              // If the deleted set is the one being viewed, go back to home screen
              if (activeContent.type === 'flashcardSet' && activeContent.id === setId) {
                  setActiveContent({ type: 'none' });
              }
              // Refresh the list of sets in the sidebar
              await fetchSavedSets();
          } catch(err) {
              console.error("Failed to delete set:", err);
              alert("Error: Could not delete the flashcard set.");
          }
      }
  };

  const handleSetSaved = async () => {
    // This function will be called by the child component after a save operation
    // to trigger a refresh of the sets list.
    await fetchSavedSets();
  };

  const handleRenameSet = async (setId, newName) => {
    try {
        await updateFlashcardSetTopic(setId, newName);
        // Refresh the list to show the new name
        await fetchSavedSets();
        // Also update the title if the renamed set is the one being viewed
        if (activeContent.type === 'flashcardSet' && activeContent.id === setId) {
            setActiveContent(prev => ({ ...prev, title: newName }));
        }
    } catch(err) {
        console.error("Failed to rename set:", err);
        alert("Error: Could not rename the flashcard set.");
    }
  };

  if (!user) return <LoginScreen onLogin={handleLogin} />;

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden font-sans text-slate-800">
      {/* SIDEBAR */}
      <div className="w-72 bg-white border-r border-slate-200 flex flex-col shrink-0 shadow-sm z-10">
        {/* ... header and upload button ... */}
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

        <div className="flex-1 overflow-y-auto px-3 space-y-1 scrollbar-thin">
            <div className="px-1 pb-2 text-xs font-bold text-slate-400 uppercase tracking-wider">Your Library</div>
            {error && <p className='text-red-500 text-xs p-3'>{error}</p>}
            {documents.map(d => (
                <div key={d.documentId} onClick={() => handleSelectDocument(d)} className={`p-3 rounded-lg cursor-pointer flex justify-between items-center group transition-all ${activeContent.type === 'doc' && activeContent.id === d.documentId ? 'bg-blue-50 text-blue-700 font-medium ring-1 ring-blue-200' : 'hover:bg-slate-50 text-slate-600'}`}>
                    <div className="flex items-center gap-3 truncate">
                        <LucideFileText size={18} className={activeContent.type === 'doc' && activeContent.id === d.documentId ? "text-blue-500" : "text-slate-400"} /> 
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
            <SavedFlashcardsPanel 
                sets={savedSets}
                onSelectSet={handleSelectFlashcardSet} 
                onRenameSet={handleRenameSet}
                onDeleteSet={handleDeleteSet}
                selectedSetId={activeContent.type === 'flashcardSet' ? activeContent.id : null} 
            />
        </div>
        
        {/* ... user profile section ... */}
        <div className="p-4 border-t border-slate-100 bg-slate-50">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 text-white flex items-center justify-center font-bold text-xs">
                        {user && user.username.charAt(0).toUpperCase()}
                    </div>
                    <span className="text-sm font-medium text-slate-700">{user && user.username}</span>
                </div>
                <button onClick={handleLogout} className="text-slate-400 hover:text-red-600 transition-colors" title="Logout">
                    <LucideLogOut size={18}/>
                </button>
            </div>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex-1 flex flex-col min-w-0 bg-slate-50">
        {activeContent.type === 'none' ? (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400">
                <LucideBook size={64} className="text-slate-200 mb-4"/>
                <p className="text-lg font-medium text-slate-500">Select a document or a flashcard set to begin</p>
            </div>
        ) : (
            <>
                {/* ... Header and Tabs ... */}
                <div className="bg-white border-b border-slate-200 px-6 py-4 flex justify-between items-center sticky top-0 z-20">
                    <h2 className="font-bold text-lg text-slate-800 truncate flex items-center gap-2">
                         {activeContent.type === 'doc' ? <LucideFileText className="text-blue-600" /> : <LucideBrain className="text-purple-600" />}
                        {activeContent.title}
                    </h2>
                    <div className="text-xs text-slate-400 font-mono bg-slate-100 px-2 py-1 rounded">ID: {activeContent.id}</div>
                </div>
                <div className="bg-white border-b border-slate-200 px-4 flex gap-1 shadow-sm z-10">
                    {[
                        {id: 'chat', icon: LucideMessageSquare, label: 'Chat AI', disabled: activeContent.type !== 'doc'},
                        {id: 'flashcard', icon: LucideBrain, label: 'Flashcards'},
                    ].map(tab => (
                        <button 
                            key={tab.id} 
                            onClick={() => setActiveTab(tab.id)} 
                            disabled={tab.disabled}
                            className={`px-6 py-4 text-sm font-medium flex items-center gap-2 border-b-2 transition-all ${activeTab===tab.id ? 'border-blue-600 text-blue-600' : 'border-transparent text-slate-500 hover:text-slate-800 hover:bg-slate-50'} ${tab.disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
                        >
                            <tab.icon size={18} /> {tab.label}
                        </button>
                    ))}
                </div>

                <div className="flex-1 overflow-hidden relative">
                    <div className="absolute inset-0 p-4">
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 h-full overflow-hidden">
                            <div className={`h-full ${activeTab === 'chat' && activeContent.type === 'doc' ? 'block' : 'hidden'}`}>
                                <ChatMode key={`chat-${activeContent.id}`} docId={activeContent.id} />
                            </div>
                            <div className={`h-full ${activeTab === 'flashcard' ? 'block' : 'hidden'}`}>
                                { (activeContent.type === 'doc') && <FlashcardMode key={`fc-doc-${activeContent.id}`} docId={activeContent.id} onSetSaved={handleSetSaved} /> }
                                { (activeContent.type === 'flashcardSet') && (activeContent.cards === 'loading' ? (
                                    <div className="h-full flex flex-col items-center justify-center text-slate-500">
                                        <LucideLoader2 size={32} className="animate-spin mb-4" />
                                        <p className="text-lg font-medium">Loading Card Set...</p>
                                    </div>
                                ) : <FlashcardMode key={`fc-set-${activeContent.id}`} initialCards={activeContent.cards} isSavedSet={true} />) }
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
