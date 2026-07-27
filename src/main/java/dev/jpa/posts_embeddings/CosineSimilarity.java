package dev.jpa.posts_embeddings;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CosineSimilarity {

    public double similarity(List<Double> a, List<Double> b) {

        if (a.size() != b.size())
            throw new RuntimeException("임베딩 벡터 길이가 다릅니다");

        double dot = 0.0;
        double magA = 0.0;
        double magB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            magA += Math.pow(a.get(i), 2);
            magB += Math.pow(b.get(i), 2);
        }

        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }
}
