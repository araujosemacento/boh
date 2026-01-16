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
 * Utilitário pra carregamento e digestão das fases de diálogo a partir dos
 * arquivos JSON.
 * <p>
 * Essa classe fornece métodos estáticos pra carregar e fazer um "cache" do
 * diálogo que ficam armazenadas como recursos no classpath. Utiliza Gson pra
 * transposição automática dos arquivos JSON em objetos
 * {@link poo.melitoh.boh.model.DialoguePhase}.
 * <p>
 * Funcionalidades: <br>
 * - Cache: é pra evitar o recarregamento de fases já processadas, se possível,
 * pra permitir a navegação das falas entre as fases. <br>
 * - Mapeamento por ID: converte IDs legíveis pra nomes de arquivo. <br>
 * - Batch Loading: carrega todas as fases de uma vez. <br>
 * <p>
 * Os arquivos JSON ficam em {@code /dialogues/} no classpath (na pasta dos
 * {@code resources}, caso haja duvida) e seguem o padrão
 * {@code phaseN_nome.json} (ex: phase1_intro.json).
 */
public class DialogueLoader {
    /** Instância Gson pra desempacotar o JSON. */
    private static final Gson GSON = new GsonBuilder().create();

    /** Caminho base dos recursos de diálogo. */
    private static final String DIALOGUES_PATH = "/dialogues/";

    /** Cache de fases já carregadas pra evitar I/O repetido. */
    private static final Map<String, DialoguePhase> cache = new LinkedHashMap<>();

    /**
     * Carrega uma fase de diálogo pelo nome do arquivo.
     * <p>
     * Verifica o cache primeiro; se não encontrar, carrega do classpath,
     * desempacota o JSON e armazena no cache pra futuras consultas.
     *
     * @param phaseFileName Nome do arquivo (ex: "phase1_intro.json").
     * @return A {@link DialoguePhase} carregada, ou null se não encontrada.
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
     * Carrega uma fase pelo ID legível.
     * <p>
     * Converte IDs como "intro" pra nomes de arquivo como "phase1_intro.json"
     * usando o mapeamento interno.
     *
     * @param phaseId ID da fase (ex: "intro", "pointers").
     * @return A {@link DialoguePhase} correspondente, ou null se não mapeada.
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
     * Mapeia IDs de fase pra nomes de arquivo.
     * <p>
     * Mapeamento interno usado por {@link #loadPhaseById(String)}.
     *
     * @param phaseId ID legível da fase.
     * @return Nome do arquivo correspondente, ou null se desconhecido.
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
     * Vai carregar todas as fases na ordem correta de apresentação.
     * <p>
     * Retorna um mapa ordenado (LinkedHashMap) que preserva a sequência das
     * fases como definida no roteiro: intro → list_intro → aux_intro → pointers
     * → arrow_swap → traversal → conclusion.
     *
     * @return Mapa ordenado de ID da fase pra
     *         {@link poo.melitoh.boh.utils.DialoguePhase}.
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
