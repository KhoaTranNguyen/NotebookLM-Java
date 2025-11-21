// API_BASE_URL là tương đối vì đã cấu hình proxy trong vite.config.js
const API_BASE_URL = "/api/rag"; 

/**
 * Upload file PDF lên Java Backend để xử lý Vector Embedding
 */
export async function uploadDocument(file) {
    const formData = new FormData();
    formData.append("file", file);
    
    try {
        const response = await fetch(`${API_BASE_URL}/upload`, {
            method: 'POST',
            body: formData
        });
        
        if (!response.ok) throw new Error("Upload failed");
        return await response.json(); // Trả về { docId: "..." }
    } catch (error) {
        console.error("Upload error:", error);
        alert("Lỗi upload file. Hãy chắc chắn Java Backend đang chạy!");
        throw error;
    }
}

/**
 * Hàm chung để gọi các endpoint AI (Chat, Flashcard, Summary...)
 */
export async function callJavaAI(endpoint, payload) {
    try {
        const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (!response.ok) {
            const errText = await response.text();
            console.error(`Java Server Error (${endpoint}):`, errText);
            return null;
        }
        
        return await response.json();
    } catch (error) {
        console.error(`Network Error calling ${endpoint}:`, error);
        return null;
    }
}

/**
 * Hàm riêng để gọi API TTS (Text-to-Speech)
 * Khác biệt: Trả về BLOB (Binary Large Object) để phát âm thanh
 */
export async function fetchAudio(payload) {
    try {
        // Gọi endpoint /tts mà ta đã viết trong StudyController
        const response = await fetch(`${API_BASE_URL}/tts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            console.error("TTS Error:", await response.text());
            return null;
        }

        // QUAN TRỌNG: Lấy dữ liệu dưới dạng Blob (File âm thanh)
        return await response.blob(); 
    } catch (error) {
        console.error("Network Error calling TTS:", error);
        return null;
    }
}