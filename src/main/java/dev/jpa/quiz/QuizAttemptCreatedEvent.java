package dev.jpa.quiz;

public record QuizAttemptCreatedEvent(
        Long attemptId,
        Long quizId,
        int selectedNo,
        boolean correct
) {}
