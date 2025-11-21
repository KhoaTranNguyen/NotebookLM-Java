package com.khoa.notebooklm.model;

import java.util.List;

public record QuizResponse(
    List<MultipleChoiceQuestion> questions
) {}