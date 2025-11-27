import React, { useState, useEffect, useRef } from 'react';
import { BookMarked, LucideTrash2 } from 'lucide-react';

export default function SavedFlashcardsPanel({ sets = [], onSelectSet, onRenameSet, onDeleteSet, selectedSetId }) {
    const [editingSetId, setEditingSetId] = useState(null);
    const [editText, setEditText] = useState("");
    const inputRef = useRef(null);

    // Focus the input when editing starts
    useEffect(() => {
        if (editingSetId !== null && inputRef.current) {
            inputRef.current.focus();
            inputRef.current.select();
        }
    }, [editingSetId]);

    const handleDoubleClick = (set) => {
        setEditingSetId(set.setId);
        setEditText(set.topicName);
    };

    const handleRename = () => {
        if (editingSetId === null) return;
        
        // Only call rename if the text has actually changed
        const originalSet = sets.find(s => s.setId === editingSetId);
        if (originalSet && originalSet.topicName !== editText && editText.trim() !== "") {
            onRenameSet(editingSetId, editText.trim());
        }
        setEditingSetId(null);
        setEditText("");
    };

    const handleKeyDown = (event) => {
        if (event.key === 'Enter') {
            handleRename();
        } else if (event.key === 'Escape') {
            setEditingSetId(null);
            setEditText("");
        }
    };
    
    const handleDeleteClick = (e, setId) => {
        e.stopPropagation(); // Prevent the item from being selected
        onDeleteSet(setId);
    }

    return (
        <div className="p-4 border-t border-slate-200">
            <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-3 px-2">
                Flashcard Sets
            </h3>
            <ul className="space-y-1">
                {sets.length === 0 ? (
                    <p className="text-xs text-slate-400 px-2">Chưa có bộ nào được lưu.</p>
                ) : (
                    sets.map(set => (
                        <li key={set.setId} className="group">
                             <div
                                onDoubleClick={() => editingSetId === null && handleDoubleClick(set)}
                                onClick={() => editingSetId === null && onSelectSet(set.setId, set.topicName)}
                                className={`w-full text-left flex items-center justify-between gap-3 p-2 rounded-lg text-sm font-medium transition-colors cursor-pointer ${
                                    selectedSetId === set.setId && editingSetId !== set.setId
                                        ? 'bg-purple-100 text-purple-700'
                                        : 'text-slate-600 hover:bg-slate-100'
                                }`}
                            >
                                <div className="flex items-center gap-3 truncate">
                                    <BookMarked className="w-4 h-4 shrink-0" />
                                    {editingSetId === set.setId ? (
                                        <input
                                            ref={inputRef}
                                            type="text"
                                            value={editText}
                                            onChange={(e) => setEditText(e.target.value)}
                                            onBlur={handleRename}
                                            onKeyDown={handleKeyDown}
                                            className="bg-white text-sm w-full outline-none p-0 -m-1 ring-1 ring-purple-500 rounded"
                                        />
                                    ) : (
                                        <span className="truncate">{set.topicName}</span>
                                    )}
                                </div>
                                <button 
                                    onClick={(e) => handleDeleteClick(e, set.setId)} 
                                    className="text-slate-300 hover:text-red-500 p-1 rounded-md hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-all shrink-0"
                                    title="Delete set"
                                >
                                    <LucideTrash2 size={14} />
                                </button>
                            </div>
                        </li>
                    ))
                )}
            </ul>
        </div>
    );
}
