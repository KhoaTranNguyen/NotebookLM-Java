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

/**
 * Saves a set of flashcards to the backend.
 * @param {Long} userId - The ID of the user.
 * @param {string} topic - The topic of the flashcard set.
 * @param {Array<Object>} flashcards - An array of flashcard objects.
 * @returns {Promise<any>} The response from the backend.
 */
export async function saveFlashcardSet(userId, topic, flashcards) {
    try {
        const response = await fetch(`${RAG_API_URL}/flashcards/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId, topic, flashcards })
        });

        if (!response.ok) {
            const errText = await response.text();
            console.error(`Java Server Error (saveFlashcardSet):`, errText);
            throw new Error(errText || 'Failed to save flashcard set');
        }

        return await response.json();
    } catch (error) {
        console.error(`Network Error calling saveFlashcardSet:`, error);
        throw error;
    }
}

/**
 * Fetches the list of saved flashcard sets for the current user.
 * @returns {Promise<Array<Object>>} A list of flashcard set metadata objects.
 */
export async function getFlashcardSets() {
    try {
        const response = await fetch(`${RAG_API_URL}/flashcards/sets`);
        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || 'Failed to fetch flashcard sets');
        }
        return await response.json();
    } catch (error) {
        console.error("Network Error calling getFlashcardSets:", error);
        throw error;
    }
}

/**
 * Fetches the full data for a specific flashcard set.
 * @param {number} setId - The ID of the flashcard set to fetch.
 * @returns {Promise<Array<Object>>} An array of flashcard objects.
 */
export async function getFlashcardSet(setId) {
    try {
        const response = await fetch(`${RAG_API_URL}/flashcards/set/${setId}`);
        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || 'Failed to fetch flashcard set');
        }
        return await response.json();
    } catch (error) {
        throw error;
    }
}

/**
 * Deletes a specific flashcard set from the backend.
 * @param {number} setId - The ID of the flashcard set to delete.
 * @returns {Promise<any>} The success message from the backend.
 */
export async function deleteFlashcardSet(setId) {
    try {
        const response = await fetch(`${RAG_API_URL}/flashcards/set/${setId}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || 'Failed to delete flashcard set');
        }
        return await response.json();
    } catch (error) {
        console.error(`Network Error calling deleteFlashcardSet(${setId}):`, error);
        throw error;
    }
}

/**
 * Updates the topic/name of a specific flashcard set.
 * @param {number} setId - The ID of the flashcard set to update.
 * @param {string} newTopic - The new topic name.
 * @returns {Promise<any>} The success message from the backend.
 */
export async function updateFlashcardSetTopic(setId, newTopic) {
    try {
        const response = await fetch(`${RAG_API_URL}/flashcards/set/${setId}/topic`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ topic: newTopic })
        });
        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || 'Failed to update topic');
        }
        return await response.json();
    } catch (error) {
        console.error(`Network Error calling updateFlashcardSetTopic(${setId}):`, error);
        throw error;
    }
}