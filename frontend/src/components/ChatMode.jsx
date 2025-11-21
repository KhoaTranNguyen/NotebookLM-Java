import React, { useState, useRef, useEffect } from 'react';
import { LucideLoader2, LucideChevronRight, LucideBot, LucideUser } from 'lucide-react';
import { callJavaAI } from '../api';

export default function ChatMode({ docId }) {
    const [messages, setMessages] = useState([{role: 'model', text: 'Chào bạn, mình đã đọc xong tài liệu này. Bạn muốn hỏi chi tiết nào?'}]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const scrollRef = useRef(null);

    // Tự động cuộn xuống tin nhắn mới nhất
    useEffect(() => {
        scrollRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    const handleSend = async () => {
        if (!input.trim() || loading) return;
        
        const userMsg = { role: 'user', text: input };
        setMessages(prev => [...prev, userMsg]);
        setInput("");
        setLoading(true);

        // Gọi API sang Java Backend
        const result = await callJavaAI('chat', { docId, query: userMsg.text });
        
        const botResponse = result ? result.answer : "Xin lỗi, server Java đang gặp sự cố hoặc chưa ingest tài liệu.";
        setMessages(prev => [...prev, { role: 'model', text: botResponse }]);
        setLoading(false);
    };

    return (
        <div className="flex flex-col h-full bg-white">
            <div className="flex-1 overflow-y-auto p-6 space-y-6">
                {messages.map((m, i) => (
                    <div key={i} className={`flex gap-3 ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                        {m.role === 'model' && (
                            <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                                <LucideBot size={16} className="text-blue-600"/>
                            </div>
                        )}
                        <div className={`max-w-[80%] rounded-2xl px-5 py-3 text-sm leading-relaxed shadow-sm ${m.role === 'user' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-800'}`}>
                            {m.text}
                        </div>
                        {m.role === 'user' && (
                             <div className="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center shrink-0">
                                <LucideUser size={16} className="text-slate-600"/>
                            </div>
                        )}
                    </div>
                ))}
                {loading && (
                    <div className="flex gap-3">
                         <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                            <LucideLoader2 className="animate-spin text-blue-600" size={16} />
                        </div>
                        <div className="bg-slate-50 px-4 py-3 rounded-2xl text-sm text-slate-400 italic">
                            Đang suy nghĩ...
                        </div>
                    </div>
                )}
                <div ref={scrollRef} />
            </div>
            <div className="p-4 border-t border-slate-100">
                <div className="relative flex items-center">
                    <input 
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && handleSend()}
                        className="w-full bg-slate-50 border border-slate-200 rounded-full pl-5 pr-12 py-3 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all"
                        placeholder="Hỏi câu hỏi về tài liệu..."
                    />
                    <button 
                        onClick={handleSend} 
                        disabled={loading || !input.trim()} 
                        className="absolute right-2 bg-blue-600 text-white p-2 rounded-full hover:bg-blue-700 disabled:opacity-50 disabled:hover:bg-blue-600 transition-colors"
                    >
                        <LucideChevronRight size={20} />
                    </button>
                </div>
            </div>
        </div>
    );
}