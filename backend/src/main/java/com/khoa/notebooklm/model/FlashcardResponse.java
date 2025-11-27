package com.khoa.notebooklm.model;

import java.util.List;

public record FlashcardResponse(
    List<Flashcard> flashcards
) {}