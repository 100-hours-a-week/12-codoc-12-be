package _ganzi.codoc.user.api.validation;

import _ganzi.codoc.user.domain.BadWord;
import _ganzi.codoc.user.repository.BadWordRepository;
import jakarta.annotation.PostConstruct;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BannedWordMatcher {

    private final BadWordRepository badWordRepository;
    private volatile Node root = new Node();

    @PostConstruct
    public void initialize() {
        List<BadWord> words = badWordRepository.findAll();
        buildTrie(words);
        log.info("banned words loaded. count={}", words.size());
    }

    public String findFirstMatch(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = normalize(input);
        Node current = root;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            while (current != root && !current.next.containsKey(ch)) {
                current = current.fail;
            }
            current = current.next.getOrDefault(ch, root);
            if (!current.outputs.isEmpty()) {
                return current.outputs.get(0);
            }
        }
        return null;
    }

    private void buildTrie(List<BadWord> words) {
        Node newRoot = new Node();
        for (BadWord word : words) {
            if (word.getWord() == null || word.getWord().isBlank()) {
                continue;
            }
            String normalized = normalize(word.getWord());
            if (normalized.isBlank()) {
                continue;
            }
            insert(newRoot, normalized, word.getWord());
        }
        buildFailureLinks(newRoot);
        root = newRoot;
    }

    private void insert(Node rootNode, String word, String original) {
        Node current = rootNode;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            current = current.next.computeIfAbsent(ch, key -> new Node());
        }
        if (!current.outputSet.contains(original)) {
            current.outputs.add(original);
            current.outputSet.add(original);
        }
    }

    private void buildFailureLinks(Node rootNode) {
        rootNode.fail = rootNode;
        Deque<Node> queue = new ArrayDeque<>();
        for (Node child : rootNode.next.values()) {
            child.fail = rootNode;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Map.Entry<Character, Node> entry : current.next.entrySet()) {
                char ch = entry.getKey();
                Node nextNode = entry.getValue();
                Node fallback = current.fail;
                while (fallback != rootNode && !fallback.next.containsKey(ch)) {
                    fallback = fallback.fail;
                }
                if (fallback.next.containsKey(ch) && fallback.next.get(ch) != nextNode) {
                    nextNode.fail = fallback.next.get(ch);
                } else {
                    nextNode.fail = rootNode;
                }
                if (!nextNode.fail.outputs.isEmpty()) {
                    for (String output : nextNode.fail.outputs) {
                        if (!nextNode.outputSet.contains(output)) {
                            nextNode.outputs.add(output);
                            nextNode.outputSet.add(output);
                        }
                    }
                }
                queue.add(nextNode);
            }
        }
    }

    private String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    private static class Node {
        private final Map<Character, Node> next = new HashMap<>();
        private Node fail;
        private final List<String> outputs = new ArrayList<>();
        private final Set<String> outputSet = new HashSet<>();
    }
}
