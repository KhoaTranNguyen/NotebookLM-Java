import React, { useState } from 'react';
import { LucideSparkles, LucideLoader2, LucideFileText } from 'lucide-react';
import { callJavaAI } from '../api';

export default function SummaryMode({ docId, docContent }) {
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(false);

    const generateSummary = async () => {
        setLoading(true);
        // Gửi docId (để backend tìm lại file gốc nếu có cache) và docContent (để backup context stuffing)
        // Lưu ý: Nếu file quá lớn, có thể backend sẽ cần logic đọc file từ disk thay vì nhận docContent từ frontend
        const result = await callJavaAI('summary', { docId, content: docContent || "Nội dung chưa được tải lên client" });
        setSummary(result);
        setLoading(false);
    };

    if (loading) {
        return (
            <div className="h-full flex flex-col items-center justify-center space-y-4">
                <LucideLoader2 className="animate-spin text-orange-500" size={40} />
                <p className="text-slate-500 animate-pulse font-medium">AI đang đọc và tóm tắt tài liệu...</p>
            </div>
        );
    }

    if (!summary) {
        return (
            <div className="h-full flex flex-col items-center justify-center p-8 text-center">
                <div className="w-20 h-20 bg-orange-100 text-orange-600 rounded-full flex items-center justify-center mb-6 shadow-sm">
                    <LucideFileText size={40} />
                </div>
                <h3 className="text-xl font-bold text-slate-800 mb-2">Tóm tắt nội dung</h3>
                <p className="text-slate-500 mb-8 max-w-xs">Nhận bản tóm tắt tổng quan, các điểm chính và hành động đề xuất trong vài giây.</p>
                <button 
                    onClick={generateSummary} 
                    className="bg-orange-600 hover:bg-orange-700 text-white px-8 py-3 rounded-full font-bold shadow-lg flex items-center gap-2 transition-all"
                >
                    <LucideSparkles size={20} /> Tạo bản tóm tắt
                </button>
            </div>
        );
    }

    return (
        <div className="h-full overflow-y-auto p-8 bg-white">
            <div className="max-w-3xl mx-auto space-y-8">
                <div className="bg-orange-50 p-8 rounded-2xl border border-orange-100 shadow-sm">
                    <h3 className="text-orange-800 font-bold text-lg mb-3 flex items-center gap-2">
                        <LucideSparkles size={20}/> Tổng quan
                    </h3>
                    <p className="text-slate-800 leading-relaxed text-lg">{summary.overview}</p>
                </div>
                
                <div>
                    <h3 className="font-bold text-xl text-slate-800 mb-4">Các điểm chính</h3>
                    <ul className="space-y-3">
                        {summary.keyPoints?.map((point, idx) => (
                            <li key={idx} className="flex items-start gap-3 bg-slate-50 p-4 rounded-xl border border-slate-100">
                                <span className="bg-blue-100 text-blue-600 font-bold w-6 h-6 rounded-full flex items-center justify-center shrink-0 text-sm mt-0.5">{idx + 1}</span>
                                <span className="text-slate-700">{point}</span>
                            </li>
                        ))}
                    </ul>
                </div>

                {summary.actionItem && (
                    <div className="bg-green-50 p-6 rounded-2xl border border-green-100 text-green-800">
                        <h3 className="font-bold mb-2 flex items-center gap-2">💡 Hành động đề xuất</h3>
                        <p>{summary.actionItem}</p>
                    </div>
                )}
                
                <div className="pt-8 text-center">
                     <button onClick={generateSummary} className="text-slate-400 hover:text-orange-600 text-sm font-medium underline">Tạo lại bản tóm tắt khác</button>
                </div>
            </div>
        </div>
    );
}