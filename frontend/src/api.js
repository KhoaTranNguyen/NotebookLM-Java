const RAG_API_URL = "/api/rag";
const AUTH_API_URL = "/api/auth";
const DOC_API_URL = "/api/documents";

/**
 * Logs in a user.
 * @param {string} username - The username.
 * @param {string} password - The password.
 * @returns {Promise<any>} The user object from the backend.
 */
export async function login(username, password) {
    const response = await fetch(`${AUTH_API_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Login failed');
    }
    return response.json();
}

/**
 * Registers a new user.
 * @param {object} userDetails - The user details (username, firstName, etc.).
 * @param {string} password - The password.
 * @returns {Promise<any>} The success message from the backend.
 */
export async function register(userDetails, password) {
    const response = await fetch(`${AUTH_API_URL}/register?password=${encodeURIComponent(password)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userDetails)
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Registration failed');
    }
    return response.json();
}

/**
 * Fetches the list of documents for the current user.
 * @returns {Promise<any>} The list of documents.
 */
export async function getDocuments() {
    const response = await fetch(DOC_API_URL);
     if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to fetch documents');
    }
    return response.json();
}

/**
 * Uploads a PDF file to the backend for vector embedding.
 */
export async function uploadDocument(file) {
    const formData = new FormData();
    formData.append("file", file);
    
    try {
        const response = await fetch(`${RAG_API_URL}/upload`, {
            method: 'POST',
            body: formData
        });
        
        if (!response.ok) throw new Error("Upload failed");
        return await response.json();
    } catch (error) {
        console.error("Upload error:", error);
        alert("Upload error. Make sure the Java Backend is running!");
        throw error;
    }
}

/**
 * A general function to call AI endpoints (chat, flashcard, summary...).
 */
export async function callJavaAI(endpoint, payload) {
    try {
        const response = await fetch(`${RAG_API_URL}/${endpoint}`, {
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