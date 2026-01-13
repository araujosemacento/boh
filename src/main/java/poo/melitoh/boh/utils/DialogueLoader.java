package poo.melitoh.boh.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import poo.melitoh.boh.model.DialoguePhase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilitário para carregar fases de diálogo a partir de arquivos JSON.
 */
public class DialogueLoader {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIALOGUES_PATH = "/dialogues/";

    /**
     * Cache de fases carregadas para evitar recarregamento.
     */
    private static final Map<String, DialoguePhase> cache = new LinkedHashMap<>();

    /**
     * Carrega uma fase de diálogo a partir do arquivo JSON correspondente.
     *
     * @param phaseFileName Nome do arquivo (ex: "phase1_intro.json")
     * @return A fase de diálogo carregada, ou null se não encontrada.
     */
    public static DialoguePhase loadPhase(String phaseFileName) {
        // Verifica cache primeiro
        if (cache.containsKey(phaseFileName)) {
            return cache.get(phaseFileName);
        }

        String resourcePath = DIALOGUES_PATH + phaseFileName;

        try (InputStream is = DialogueLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Recurso de diálogo não encontrado: " + resourcePath);
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(is,
                    StandardCharsets.UTF_8)) {
                DialoguePhase phase = GSON.fromJson(reader, DialoguePhase.class);
                cache.put(phaseFileName, phase);
                return phase;
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar diálogo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Carrega uma fase pelo ID (ex: "intro" -> "phase1_intro.json").
     *
     * @param phaseId ID da fase
     * @return A fase de diálogo, ou null se não mapeada.
     */
    public static DialoguePhase loadPhaseById(String phaseId) {
        String fileName = mapPhaseIdToFile(phaseId);
        if (fileName == null) {
            System.err.println("Phase ID desconhecido: " + phaseId);
            return null;
        }
        return loadPhase(fileName);
    }

    /**
     * Mapeia IDs de fase para nomes de arquivo.
     */
    private static String mapPhaseIdToFile(String phaseId) {
        return switch (phaseId) {
        case "intro" -> "phase1_intro.json";
        case "list_intro" -> "phase2_list_intro.json";
        case "aux_intro" -> "phase3_aux_intro.json";
        case "pointers" -> "phase4_pointers.json";
        case "arrow_swap" -> "phase5_arrow_swap.json";
        case "traversal" -> "phase6_traversal.json";
        case "conclusion" -> "phase7_conclusion.json";
        default -> null;
        };
    }

    /**
     * Carrega todas as fases na ordem correta.
     *
     * @return Mapa ordenado de ID da fase para DialoguePhase.
     */
    public static Map<String, DialoguePhase> loadAllPhases() {
        Map<String, DialoguePhase> phases = new LinkedHashMap<>();
        String[] phaseIds = { "intro", "list_intro", "aux_intro", "pointers",
                "arrow_swap", "traversal", "conclusion"
        };

        for (String id : phaseIds) {
            DialoguePhase phase = loadPhaseById(id);
            if (phase != null) {
                phases.put(id, phase);
            }
        }

        return phases;
    }

    /**
     * Limpa o cache de fases carregadas.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Pré-carrega todas as fases no cache.
     */
    public static void preloadAll() {
        loadAllPhases();
    }
}
