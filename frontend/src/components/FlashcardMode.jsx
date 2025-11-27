import React, { useState, useEffect } from 'react';
import { LucideBrain, LucideLoader2, LucideRefreshCw, LucideSave } from 'lucide-react';
import { callJavaAI, saveFlashcardSet } from '../api';

export default function FlashcardMode({ docId, initialCards, isSavedSet = false }) {
    const [cards, setCards] = useState(initialCards || []);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isFlipped, setIsFlipped] = useState(false);

    // If a new set of initialCards is passed, reset the view
    useEffect(() => {
        if (initialCards) {
            setCards(initialCards);
            setCurrentIndex(0);
            setIsFlipped(false);
        }
    }, [initialCards]);

    const generateCards = async () => {
        if (!docId) return;
        setLoading(true);
        const result = await callJavaAI('flashcards', { docId });
        if (result && Array.isArray(result)) {
            setCards(result);
            setCurrentIndex(0);
            setIsFlipped(false);
        } else {
            alert("Không tạo được flashcard. Kiểm tra lại server.");
        }
        setLoading(false);
    };

    const saveCards = async () => {
        if (cards.length === 0 || isSavedSet) {
            alert(isSavedSet ? "Bộ này đã được lưu." : "Không có flashcard để lưu.");
            return;
        }
        setSaving(true);
        try {
            const userId = 1; // Placeholder
            const topic = docId ? `Flashcards for Document ${docId}` : `Saved Flashcard Set`;
            const response = await saveFlashcardSet(userId, topic, cards);
            alert(response.message || "Flashcard set saved successfully!");
        } catch (error) {
            console.error("Failed to save flashcard set:", error);
            alert("Lưu bộ Flashcard thất bại: " + error.message);
        } finally {
            setSaving(false);
        }
    };

    // Show generate screen only if there are no initial cards and a docId is provided
    if (cards.length === 0 && docId && !initialCards) {
        return (
            <div className="h-full flex flex-col items-center justify-center p-8 text-center">
                <div className="w-20 h-20 bg-purple-100 text-purple-600 rounded-full flex items-center justify-center mb-6 shadow-sm">
                    <LucideBrain size={40} />
                </div>
                <h3 className="text-xl font-bold text-slate-800 mb-2">Flashcards Thông Minh</h3>
                <p className="text-slate-500 mb-8 max-w-xs">Sử dụng AI để tự động tạo thẻ ghi nhớ từ các khái niệm chính trong tài liệu.</p>
                <button 
                    onClick={generateCards} 
                    disabled={loading} 
                    className="bg-purple-600 hover:bg-purple-700 text-white px-8 py-3 rounded-full font-bold shadow-lg hover:shadow-xl transition-all flex items-center gap-2"
                >
                    {loading ? <LucideLoader2 className="animate-spin" /> : "Tạo bộ Flashcard ngay"}
                </button>
            </div>
        );
    }
    
    // If there are no cards (e.g., an empty saved set was loaded), show a simple message.
    if (cards.length === 0) {
        return (
             <div className="h-full flex flex-col items-center justify-center p-8 text-center text-slate-500">
                <LucideBrain size={40} className="mb-4" />
                <p>This flashcard set is empty.</p>
            </div>
        )
    }

    return (
        <div className="h-full flex flex-col items-center justify-center p-8 bg-slate-50 relative">
             <div className="w-full max-w-xl aspect-[3/2] perspective-1000 cursor-pointer group" onClick={() => setIsFlipped(!isFlipped)}>
                <div className={`relative w-full h-full transition-transform duration-700 transform-style-3d ${isFlipped ? 'rotate-y-180' : ''}`}>
                    
                    {/* Mặt Trước */}
                    <div className="absolute w-full h-full bg-white rounded-3xl shadow-xl border border-slate-100 flex flex-col items-center justify-center p-10 backface-hidden hover:border-purple-200 transition-colors">
                        <div className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-4">Câu hỏi</div>
                        <p className="text-2xl font-semibold text-center text-slate-800 leading-relaxed">{cards[currentIndex].front}</p>
                        <div className="absolute bottom-6 text-sm text-purple-500 font-medium opacity-0 group-hover:opacity-100 transition-opacity">Nhấn để lật</div>
                    </div>

                    {/* Mặt Sau */}
                    <div className="absolute w-full h-full bg-gradient-to-br from-purple-600 to-indigo-700 rounded-3xl shadow-xl flex flex-col items-center justify-center p-10 backface-hidden rotate-y-180">
                        <div className="text-xs font-bold text-purple-200 uppercase tracking-widest mb-4">Đáp án</div>
                        <p className="text-xl text-center text-white font-medium leading-relaxed">{cards[currentIndex].back}</p>
                    </div>
                </div>
            </div>

            {/* Controls */}
            <div className="flex items-center gap-6 mt-10">
                <button 
                    onClick={() => {setCurrentIndex(Math.max(0, currentIndex-1)); setIsFlipped(false)}} 
                    disabled={currentIndex === 0}
                    className="p-4 rounded-full bg-white border border-slate-200 text-slate-600 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm transition-all"
                >
                    ←
                </button>
                <span className="font-bold text-slate-600 text-lg">{currentIndex + 1} / {cards.length}</span>
                <button 
                    onClick={() => {setCurrentIndex(Math.min(cards.length-1, currentIndex+1)); setIsFlipped(false)}} 
                    disabled={currentIndex === cards.length - 1}
                    className="p-4 rounded-full bg-white border border-slate-200 text-slate-600 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm transition-all"
                >
                    →
                </button>
            </div>
            
            <div className="absolute top-4 right-4 flex gap-2">
                <button 
                    onClick={saveCards} 
                    disabled={saving || isSavedSet}
                    className={`p-2 flex items-center gap-1 ${isSavedSet ? 'text-slate-400 cursor-default' : 'text-slate-400 hover:text-green-600'}`}
                    title={isSavedSet ? "Bộ flashcard đã được lưu" : "Lưu bộ Flashcard"}
                >
                    {saving ? <LucideLoader2 className="w-4 h-4 animate-spin" /> : <LucideSave size={20}/>}
                    <span className="text-sm">{saving ? "Đang lưu..." : (isSavedSet ? "Đã lưu" : "Lưu")}</span>
                </button>
                {/* Only show the regenerate button if we are in a document context */}
                {docId && (
                    <button onClick={generateCards} className="text-slate-400 hover:text-purple-600 p-2" title="Tạo lại bộ mới">
                        <LucideRefreshCw size={20}/>
                    </button>
                )}
            </div>
        </div>
    );
}
