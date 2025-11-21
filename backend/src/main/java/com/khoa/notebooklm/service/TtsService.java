package com.khoa.notebooklm.service;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Service
public class TtsService {

    // SỬA LẠI DÒNG NÀY: Thêm giá trị mặc định sau dấu hai chấm (:)
    // Nếu không tìm thấy trong file config, nó sẽ dùng luôn "geminijava-478112"
    @Value("${google.ai.project-id:geminijava-478112}")
    private String projectId;

    public byte[] synthesizeText(String text) {
        // Cấu hình Settings để ÉP BUỘC sử dụng Project ID cho hạn ngạch (Quota)
        // Đây là "thuốc đặc trị" cho lỗi 403 Forbidden trên Windows
        TextToSpeechSettings settings;
        try {
            settings = TextToSpeechSettings.newBuilder()
                    .setQuotaProjectId(projectId) // <--- DÒNG QUAN TRỌNG NHẤT
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi cấu hình TTS: " + e.getMessage());
        }

        // Khởi tạo Client với settings đã cấu hình
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
            
            // 1. Thiết lập nội dung đầu vào
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // 2. Chọn giọng đọc (Tiếng Việt, Neural2 - giọng AI tự nhiên nhất)
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("vi-VN")
                    .setName("vi-VN-Neural2-A") // Giọng nữ Neural xịn
                    .build();

            // 3. Cấu hình file âm thanh đầu ra (MP3)
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            // 4. Gọi API Google
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // 5. Trả về mảng byte (file âm thanh)
            ByteString audioContents = response.getAudioContent();
            return audioContents.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi IO khi gọi TTS: " + e.getMessage());
        } catch (Exception e) {
            // In chi tiết lỗi ra console để debug nếu vẫn bị
            e.printStackTrace(); 
            throw new RuntimeException("Lỗi TTS API: " + e.getMessage());
        }
    }
}