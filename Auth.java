import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticação simples por senha + sessão em cookie.
 *
 * Fluxo resumido:
 *   1. Browser envia a senha digitada -> /api/login
 *   2. Este arquivo compara com SENHA_ACESSO (variável de ambiente)
 *   3. Se bater, gera um token aleatório e o servidor devolve num cookie
 *   4. Toda rota /api/* usa sessaoValida() para conferir o cookie
 *
 * Atenção: as sessoes ficam na MEMORIA (mapa abaixo). Reiniciar o servidor
 * derruba todos os logins -- o navegador precisa logar de novo. Aceitável aqui,
 * e justamente porque não há sessão no banco: se algo vazar, não fica gravado.
 */
public class Auth {

    // Token -> instante (em ms) em que a sessao foi criada
    private static final Map<String, Long> sessoes = new ConcurrentHashMap<>();

    // Vencimento da sessao: 8 horas
    private static final long VIDA_SESSAO_MS = 8L * 60 * 60 * 1000;

    // Nome do cookie que identificara a sessao
    private static final String NOME_COOKIE = "sessao";

    // Reutilizado: menos alocação por requisição, comportamento igual
    private static final SecureRandom ALEATORIO = new SecureRandom();

    // Senha verdadeira lida uma única vez no boot.
    // Ordem de prioridade: variável de ambiente > hardcoded.
    // No Render (produção), SENHA_ACESSO vem das env vars e a hardcoded
    // nunca é usada. Localmente, se não houver env var, usa o fallback.
    // Mesmo padrão que o GerenciadorArquivo usa para as credenciais do banco.
    private static final String SENHA_ESPERADA =
            System.getenv("SENHA_ACESSO") != null
                    ? System.getenv("SENHA_ACESSO")
                    : "220815@Lidi";

    // --- SENHA ---

    /**
     * Compara a senha recebida com a configurada. Sensível a maiúsculas/minúsculas:
     * "Reforco123" e "reforco123" são coisas diferentes, o que evita surpresa.
     */
    public static boolean validarSenha(String candidata) {
        if (SENHA_ESPERADA == null || SENHA_ESPERADA.isEmpty()) return false;
        if (candidata == null) return false;
        return SENHA_ESPERADA.equals(candidata);
    }

    /** Indica se a senha está configurada. O login usa isso para dar erro claro. */
    public static boolean configurado() {
        return SENHA_ESPERADA != null && !SENHA_ESPERADA.isEmpty();
    }

    // --- CRIAR / VALIDAR SESSÃO ---

    /** Gera um token aleatório de 64 caracteres e registra a sessão. */
    public static String criarSessao() {
        byte[] bytes = new byte[32];            // 32 bytes = 256 bits de entropia
        ALEATORIO.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        sessoes.put(token, System.currentTimeMillis());
        return token;
    }

    /** Sessão válida: token conhecido E criado há menos de VIDA_SESSAO_MS. */
    public static boolean sessaoValida(String token) {
        if (token == null || token.isEmpty()) return false;
        Long criada = sessoes.get(token);
        if (criada == null) return false;
        if (System.currentTimeMillis() - criada > VIDA_SESSAO_MS) {
            sessoes.remove(token);              // expirou: descarta já
            return false;
        }
        return true;
    }

    /** Remove a sessão (usado no logout e na limpeza de expirados). */
    public static void encerrarSessao(String token) {
        if (token != null) sessoes.remove(token);
    }

    /** Apaga as sessões vencidas. chamado periodicamente por /api/login. */
    public static void limparVencidas() {
        long agora = System.currentTimeMillis();
        sessoes.entrySet().removeIf(e -> agora - e.getValue() > VIDA_SESSAO_MS);
    }

    // --- LER O COOKIE DA REQUISIÇÃO ---

    /** Extrai o valor do cookie "sessao" do header Cookie da requisição. */
    public static String tokenDaRequisicao(HttpExchange exchange) {
        var header = exchange.getRequestHeaders().getFirst("Cookie");
        if (header == null) return null;

        // "Cookie: sessao=abc123; outra=xyz"
        for (String parte : header.split(";")) {
            String item = parte.trim();
            int igual = item.indexOf('=');
            if (igual > 0 && NOME_COOKIE.equals(item.substring(0, igual))) {
                return item.substring(igual + 1);
            }
        }
        return null;
    }

    // --- CONSTRUTORES DE CABEÇALHO ---

    /** Cookie de sessão válida. HttpOnly: JavaScript do navegador não lê (não exibe no DevTools). */
    public static String cookieSessao(String token) {
        return NOME_COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Lax";
    }

    /** Cookie vazio: apaga a sessão do lado do navegador. */
    public static String cookieLimpar() {
        return NOME_COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
    }

    // --- RESPOSTA DE NEGATIVA ---

    /** Responde 401 e fecha a conexão. Todo o resto do handle() fica de lado. */
    public static void negar(HttpExchange exchange) throws IOException {
        String corpo = "{\"erro\":\"Nao autenticado\"}";
        byte[] dados = corpo.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(401, dados.length);
        try (var saida = exchange.getResponseBody()) {
            saida.write(dados);
        }
        exchange.close();
    }

    // --- DIAGNÓSTICO ---

    /**
     * Mascarar uma URL para poder imprimir no console do Render sem expor credenciais.
     * "jdbc:postgresql://ep-xyz.neon.tech/neondb?sslmode=require"
     *   vira "jdbc:postgresql://ep-***.neon.tech/neondb"
     */
    public static String mascarar(String texto) {
        if (texto == null) return "NULL";
        int inicio = texto.indexOf("//");
        if (inicio < 0) return texto;
        int fimHost = texto.indexOf("/", inicio + 2);
        String host = fimHost > 0 ? texto.substring(inicio + 2, fimHost) : texto.substring(inicio + 2);
        String resto = fimHost > 0 ? texto.substring(fimHost) : "";
        String curta = host.contains(".") ? host.substring(0, host.indexOf('.')) : host;
        return texto.substring(0, inicio) + "//" + curta + "***" + resto;
    }

    /** Linha de log já mascarada, para imprimir no boot. */
    public static String diagnosticar(String nome, String valor) {
        return nome + ": " + (valor == null ? "NULL" : mascarar(valor));
    }
}
