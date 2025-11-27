import React, { useState, useEffect, useRef } from 'react';
import { BookMarked } from 'lucide-react';

export default function SavedFlashcardsPanel({ sets = [], onSelectSet, onRenameSet, selectedSetId }) {
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
                        <li key={set.setId}>
                            <div
                                onDoubleClick={() => handleDoubleClick(set)}
                                className={`w-full text-left flex items-center gap-3 p-2 rounded-lg text-sm font-medium transition-colors group ${
                                    selectedSetId === set.setId && editingSetId !== set.setId
                                        ? 'bg-purple-100 text-purple-700'
                                        : 'text-slate-600 hover:bg-slate-100'
                                }`}
                            >
                                <BookMarked className="w-4 h-4" />
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
                                    <button onClick={() => onSelectSet(set.setId, set.topicName)} className="truncate flex-1 text-left">
                                        {set.topicName}
                                    </button>
                                )}
                            </div>
                        </li>
                    ))
                )}
            </ul>
        </div>
    );
}
