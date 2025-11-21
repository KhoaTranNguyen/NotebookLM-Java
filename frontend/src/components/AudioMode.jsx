import React, { useState, useRef } from 'react';
import { LucideHeadphones, LucidePlay, LucideLoader, LucideFileText, LucideAlertCircle } from 'lucide-react';
import { callJavaAI, fetchAudio } from '../api';

const AudioMode = ({ docId, docContent }) => {
    const [loading, setLoading] = useState(false);
    const [scriptData, setScriptData] = useState(null); // Chứa { title, script }
    const [audioUrl, setAudioUrl] = useState(null);
    const [audioLoading, setAudioLoading] = useState(false);
    const [error, setError] = useState(null);
    
    const audioRef = useRef(null);

    // Bước 1: Tạo kịch bản Podcast (Text)
    const handleGenerateScript = async () => {
        setLoading(true);
        setError(null);
        // Backend sẽ tự lấy content từ Cache nếu docContent null
        const result = await callJavaAI('podcast', { docId, content: docContent });
        
        if (result) {
            setScriptData(result); // Kết quả trả về là object PodcastScript
        } else {
            setError("Không thể tạo kịch bản. Vui lòng thử lại.");
        }
        setLoading(false);
    };

    // Bước 2: Chuyển văn bản thành giọng nói (Audio)
    const handlePlayAudio = async () => {
        if (!scriptData?.script) return;

        setAudioLoading(true);
        setError(null);

        try {
            // Gọi API fetchAudio từ api.js
            const audioBlob = await fetchAudio({ text: scriptData.script });
            
            if (audioBlob) {
                // Tạo URL ảo cho file MP3 để phát
                const url = URL.createObjectURL(audioBlob);
                setAudioUrl(url);
                
                // Tự động phát sau khi tải xong
                setTimeout(() => {
                    if(audioRef.current) audioRef.current.play();
                }, 100);
            } else {
                setError("Lỗi khi tạo âm thanh từ Google Cloud.");
            }
        } catch (err) {
            setError("Lỗi kết nối: " + err.message);
        } finally {
            setAudioLoading(false);
        }
    };

    return (
        <div className="h-full flex flex-col p-6 max-w-3xl mx-auto">
            {/* Header */}
            <div className="text-center mb-8">
                <div className="w-16 h-16 bg-pink-100 rounded-full flex items-center justify-center mx-auto mb-4 text-pink-600">
                    <LucideHeadphones size={32} />
                </div>
                <h2 className="text-2xl font-bold text-slate-800">Audio Overview</h2>
                <p className="text-slate-500 mt-2">Nghe tóm tắt nội dung tài liệu bằng giọng đọc AI tự nhiên.</p>
            </div>

            {/* Error Message */}
            {error && (
                <div className="bg-red-50 text-red-600 p-3 rounded-lg mb-4 flex items-center gap-2 text-sm">
                    <LucideAlertCircle size={16} /> {error}
                </div>
            )}

            {/* Main Action Area */}
            <div className="flex-1 flex flex-col items-center justify-center min-h-[200px]">
                
                {!scriptData ? (
                    // Trạng thái 1: Chưa có kịch bản
                    <button 
                        onClick={handleGenerateScript} 
                        disabled={loading}
                        className="bg-pink-600 hover:bg-pink-700 text-white px-8 py-4 rounded-full font-bold text-lg shadow-lg hover:shadow-xl transition-all flex items-center gap-3 disabled:opacity-70"
                    >
                        {loading ? <LucideLoader className="animate-spin" /> : <LucideFileText />}
                        {loading ? "Đang viết kịch bản..." : "Tạo Podcast"}
                    </button>
                ) : (
                    // Trạng thái 2: Đã có kịch bản -> Hiển thị & Nút Play
                    <div className="w-full space-y-6 animate-fade-in">
                        
                        {/* Script Display */}
                        <div className="bg-white border border-slate-200 p-6 rounded-xl shadow-sm text-left">
                            <h3 className="font-bold text-lg text-slate-800 mb-2">🎙️ {scriptData.title || "Kịch bản Podcast"}</h3>
                            <div className="prose prose-slate text-slate-600 max-h-60 overflow-y-auto pr-2 whitespace-pre-line">
                                {scriptData.script}
                            </div>
                        </div>

                        {/* Player Controls */}
                        <div className="flex justify-center">
                            {!audioUrl ? (
                                <button 
                                    onClick={handlePlayAudio} 
                                    disabled={audioLoading}
                                    className="bg-slate-900 hover:bg-slate-800 text-white px-8 py-3 rounded-full font-bold shadow-lg flex items-center gap-2 disabled:opacity-70"
                                >
                                    {audioLoading ? <LucideLoader className="animate-spin" size={20}/> : <LucidePlay size={20} fill="currentColor" />}
                                    {audioLoading ? "Đang xử lý Audio (Google TTS)..." : "Nghe ngay"}
                                </button>
                            ) : (
                                <div className="w-full bg-slate-100 p-4 rounded-xl flex flex-col items-center gap-2">
                                    <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Đang phát</span>
                                    <audio ref={audioRef} controls className="w-full h-10" src={audioUrl}>
                                        Trình duyệt của bạn không hỗ trợ phát âm thanh.
                                    </audio>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AudioMode;