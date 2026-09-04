import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServidorWeb {

    private static final Gson gson = new Gson();
    private static List<Aluno> alunos = GerenciadorArquivo.carregarAlunos();
    private static List<Agendamento> agendamentos = GerenciadorArquivo.carregarAgendamentos();

    public static void main(String[] args) throws IOException {
        // === DIAGNÓSTICO: verificar variáveis de ambiente ===
        System.out.println("=== DIAGNÓSTICO DE VARIÁVEIS DE AMBIENTE ===");
        System.out.println("DB_URL: [" + System.getenv("DB_URL") + "]");
        System.out.println("DB_USER: [" + System.getenv("DB_USER") + "]");
        System.out.println("DB_PASSWORD: [" + (System.getenv("DB_PASSWORD") != null ? "DEFINIDA" : "NULL") + "]");
        System.out.println("Todas as env vars: " + System.getenv().keySet());
        System.out.println("============================================");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ROTA API: ALUNOS
        server.createContext("/api/alunos", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // CORS
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                // GET - Listar alunos
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    List<Aluno> alunosOrdenados = new ArrayList<>(alunos);
                    alunosOrdenados.sort(java.util.Comparator.comparing(Aluno::getNome, String.CASE_INSENSITIVE_ORDER));

                    Map<String, Integer> contadorDatas = new HashMap<>();
                    List<Map<String, Object>> listaJson = new ArrayList<>();

                    for (Aluno a : alunosOrdenados) {
                        String matricula = gerarMatricula(a, contadorDatas);
                        String respNome = a.getResponsavel() != null ? a.getResponsavel().nome() : "Sem cadastro";
                        String respFone = a.getResponsavel() != null ? a.getResponsavel().telefone() : "--";
                        String respEnd = a.getResponsavel() != null ? a.getResponsavel().endereco() : "";
                        String statusPag = a.getStatusPagamento() != null ? a.getStatusPagamento().name() : "PENDENTE";
                        String statusPagDesc = a.getStatusPagamento() != null ? a.getStatusPagamento().getDescricao() : "Pendente";

                        Map<String, Object> item = new HashMap<>();
                        item.put("matricula", matricula);
                        item.put("nome", a.getNome());
                        item.put("dataNascimento", a.getDataNascimento() != null ? a.getDataNascimento().toString() : "");
                        item.put("anoEscolar", a.getAnoEscolar());
                        item.put("nivelLeitura", a.getNivelLeitura());
                        item.put("temNecessidade", a.isTemNecessidadeEspecial());
                        item.put("descricaoNecessidade", a.getDescricaoNecessidade() != null ? a.getDescricaoNecessidade() : "");
                        item.put("responsavelNome", respNome);
                        item.put("responsavelTelefone", respFone);
                        item.put("responsavelEndereco", respEnd);
                        item.put("statusPagamento", statusPag);
                        item.put("statusPagamentoDesc", statusPagDesc);
                        item.put("valorContrato", a.getValorContrato());
                        item.put("cicloPagamento", a.getCicloPagamento() != null ? a.getCicloPagamento() : "MENSAL");
                        listaJson.add(item);
                    }

                    enviarResposta(exchange, 200, gson.toJson(listaJson));
                }

                // POST - Cadastrar aluno
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String body = lerBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String nome = json.get("nome").getAsString();
                    LocalDate dataNasc = LocalDate.parse(json.get("dataNascimento").getAsString());
                    String anoEscolar = json.get("anoEscolar").getAsString();
                    NivelLeitura nivel = NivelLeitura.valueOf(json.get("nivelLeitura").getAsString());
                    boolean temNecessidade = json.has("temNecessidadeEspecial") && json.get("temNecessidadeEspecial").getAsBoolean();
                    String descNee = json.has("descricaoNecessidade") ? json.get("descricaoNecessidade").getAsString() : "";

                    String nomeResp = json.has("nomeResponsavel") ? json.get("nomeResponsavel").getAsString() : "";
                    String foneResp = json.has("telefoneResponsavel") ? json.get("telefoneResponsavel").getAsString() : "";
                    String endResp = json.has("enderecoResponsavel") ? json.get("enderecoResponsavel").getAsString() : "";
                    Responsavel resp = new Responsavel(nomeResp, foneResp, endResp);

                    double valorContrato = 1800.0;
                    if (json.has("valorContrato") && !json.get("valorContrato").getAsString().isEmpty()) {
                        valorContrato = json.get("valorContrato").getAsDouble();
                    }
                    String cicloPagamento = json.has("cicloPagamento") ? json.get("cicloPagamento").getAsString() : "MENSAL";
                    if (cicloPagamento.isEmpty()) cicloPagamento = "MENSAL";

                    Aluno novo = new Aluno(nome, dataNasc, anoEscolar, resp, nivel, temNecessidade, descNee,
                            StatusPagamento.PENDENTE, valorContrato, cicloPagamento);
                    alunos.add(novo);
                    GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));

                    JsonObject resposta = new JsonObject();
                    resposta.addProperty("status", "sucesso");
                    enviarResposta(exchange, 201, gson.toJson(resposta));
                }

                // PUT - Editar aluno
                if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String body = lerBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String nome = json.get("nome").getAsString();
                    for (Aluno a : alunos) {
                        if (a.getNome().equalsIgnoreCase(nome)) {
                            a.setNome(json.get("nome").getAsString());
                            a.setDataNascimento(LocalDate.parse(json.get("dataNascimento").getAsString()));
                            a.setAnoEscolar(json.get("anoEscolar").getAsString());
                            a.setNivelLeitura(NivelLeitura.valueOf(json.get("nivelLeitura").getAsString()));
                            a.setTemNecessidadeEspecial(json.get("temNecessidadeEspecial").getAsBoolean());
                            a.setDescricaoNecessidade(json.has("descricaoNecessidade") ? json.get("descricaoNecessidade").getAsString() : "");

                            String nomeR = json.has("nomeResponsavel") ? json.get("nomeResponsavel").getAsString() : "";
                            String foneR = json.has("telefoneResponsavel") ? json.get("telefoneResponsavel").getAsString() : "";
                            String endR = json.has("enderecoResponsavel") ? json.get("enderecoResponsavel").getAsString() : "";
                            a.setResponsavel(new Responsavel(nomeR, foneR, endR));

                            if (json.has("valorContrato") && !json.get("valorContrato").getAsString().isEmpty()) {
                                a.setValorContrato(json.get("valorContrato").getAsDouble());
                            }
                            if (json.has("cicloPagamento") && !json.get("cicloPagamento").getAsString().isEmpty()) {
                                a.setCicloPagamento(json.get("cicloPagamento").getAsString());
                            }
                            break;
                        }
                    }

                    GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));
                    JsonObject resposta = new JsonObject();
                    resposta.addProperty("status", "atualizado");
                    enviarResposta(exchange, 200, gson.toJson(resposta));
                }

                // DELETE - Remover aluno
                if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String body = lerBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String matricula = json.get("matricula").getAsString();

                    List<Aluno> alunosOrdenados = new ArrayList<>(alunos);
                    alunosOrdenados.sort(java.util.Comparator.comparing(Aluno::getNome, String.CASE_INSENSITIVE_ORDER));

                    Map<String, Integer> contadorDatas = new HashMap<>();
                    Aluno alunoParaRemover = null;
                    String matriculaAlvo = null;

                    for (Aluno a : alunosOrdenados) {
                        String matriculaGerada = gerarMatricula(a, contadorDatas);
                        if (matriculaGerada.equals(matricula.trim())) {
                            alunoParaRemover = a;
                            matriculaAlvo = matriculaGerada;
                            break;
                        }
                    }

                    if (alunoParaRemover != null) {
                        alunos.remove(alunoParaRemover);
                        final String alvoFinal = matriculaAlvo;
                        agendamentos.removeIf(ag -> ag.getMatriculaAluno().equals(alvoFinal));
                        GerenciadorArquivo.salvarAgendamentos(agendamentos);
                    }

                    GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));
                    JsonObject resposta = new JsonObject();
                    resposta.addProperty("status", "removido");
                    enviarResposta(exchange, 200, gson.toJson(resposta));
                }
            }
        });

        // ROTA API: AGENDAMENTOS
        server.createContext("/api/agendamentos", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // CORS
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                // GET - Listar agendamentos
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    List<Map<String, Object>> listaJson = new ArrayList<>();
                    for (Agendamento a : agendamentos) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", a.getId());
                        item.put("matriculaAluno", a.getMatriculaAluno());
                        item.put("data", a.getData().toString());
                        item.put("hora", a.getHora());
                        item.put("pago", a.isPago());
                        item.put("observacao", a.getObservacao() != null ? a.getObservacao() : "");
                        item.put("realizada", a.isRealizada());
                        listaJson.add(item);
                    }
                    enviarResposta(exchange, 200, gson.toJson(listaJson));
                }

                // POST - Criar agendamento
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String body = lerBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String matricula = json.get("matriculaAluno").getAsString();
                    String data = json.get("data").getAsString();
                    String hora = json.get("hora").getAsString();
                    String obs = json.has("observacao") ? json.get("observacao").getAsString() : "";

                    // Verificar conflito de horário
                    boolean conflito = false;
                    for (Agendamento a : agendamentos) {
                        if (a.getData().toString().equals(data) && a.getHora().equals(hora)) {
                            conflito = true;
                            break;
                        }
                    }

                    if (conflito) {
                        JsonObject erro = new JsonObject();
                        erro.addProperty("erro", "Conflito de horario");
                        enviarResposta(exchange, 409, gson.toJson(erro));
                        return;
                    }

                    String id = "AG-" + System.currentTimeMillis();
                    Agendamento novo = new Agendamento(id, matricula, LocalDate.parse(data), hora, false, obs);
                    agendamentos.add(novo);
                    GerenciadorArquivo.salvarAgendamentos(agendamentos);

                    JsonObject resposta = new JsonObject();
                    resposta.addProperty("status", "sucesso");
                    enviarResposta(exchange, 201, gson.toJson(resposta));
                }

                // PUT - Atualizar agendamento
                if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String body = lerBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String id = json.get("id").getAsString();

                    for (Agendamento a : agendamentos) {
                        if (a.getId().equals(id)) {
                            if (json.has("data") && !json.get("data").getAsString().isEmpty())
                                a.setData(LocalDate.parse(json.get("data").getAsString()));
                            if (json.has("hora") && !json.get("hora").getAsString().isEmpty())
                                a.setHora(json.get("hora").getAsString());
                            if (json.has("pago"))
                                a.setPago(json.get("pago").getAsBoolean());
                            if (json.has("realizada"))
                                a.setRealizada(json.get("realizada").getAsBoolean());
                            break;
                        }
                    }
                    GerenciadorArquivo.salvarAgendamentos(agendamentos);

                    JsonObject resposta = new JsonObject();
                    resposta.addProperty("status", "atualizado");
                    enviarResposta(exchange, 200, gson.toJson(resposta));
                }

                // DELETE - Remover agendamento(s)
                if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String query = exchange.getRequestURI().getQuery();
                    String tempId = null;
                    if (query != null) {
                        for (String param : query.split("&")) {
                            String[] par = param.split("=");
                            if (par.length == 2 && par[0].equals("id")) {
                                try {
                                    tempId = java.net.URLDecoder.decode(par[1], "UTF-8");
                                } catch (Exception e) {
                                    tempId = par[1];
                                }
                            }
                        }
                    }
                    final String idParam = tempId;

                    if (idParam != null && !idParam.isEmpty()) {
                        agendamentos.removeIf(a -> a.getId().equals(idParam));
                    } else {
                        String body = lerBody(exchange);
                        if (body.startsWith("[")) {
                            String[] ids = body.replace("[", "").replace("]", "").replace("\"", "").split(",");
                            for (String idItem : ids) {
                                final String targetId = idItem.trim();
                                agendamentos.removeIf(a -> a.getId().trim().equals(targetId));
                            }
                        } else if (!body.isEmpty()) {
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            if (json.has("id")) {
                                final String targetId = json.get("id").getAsString();
                                agendamentos.removeIf(a -> a.getId().equals(targetId));
                            }
                        }
                    }
                    GerenciadorArquivo.salvarAgendamentos(agendamentos);

                    JsonObject resposta = new JsonObject();
                    resposta.addProperty("status", "removido");
                    enviarResposta(exchange, 200, gson.toJson(resposta));
                }
            }
        });

        // ROTA API: STATUS DE PAGAMENTO
        server.createContext("/api/alunos/status", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "PUT, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String query = exchange.getRequestURI().getQuery();
                    String matricula = null;
                    String novoStatusStr = null;

                    if (query != null) {
                        for (String param : query.split("&")) {
                            String[] par = param.split("=");
                            if (par.length == 2) {
                                if (par[0].equals("matricula"))
                                    matricula = java.net.URLDecoder.decode(par[1], StandardCharsets.UTF_8);
                                if (par[0].equals("status"))
                                    novoStatusStr = java.net.URLDecoder.decode(par[1], StandardCharsets.UTF_8);
                            }
                        }
                    }

                    if (matricula != null && novoStatusStr != null) {
                        StatusPagamento novoStatus = StatusPagamento.valueOf(novoStatusStr);
                        Map<String, Integer> contadorDatas = new HashMap<>();
                        List<Aluno> ordenados = new ArrayList<>(alunos);
                        ordenados.sort(java.util.Comparator.comparing(Aluno::getNome, String.CASE_INSENSITIVE_ORDER));

                        for (Aluno a : ordenados) {
                            String matGerada = gerarMatricula(a, contadorDatas);
                            if (matGerada.equals(matricula)) {
                                a.setStatusPagamento(novoStatus);
                                break;
                            }
                        }
                        GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));
                        JsonObject resposta = new JsonObject();
                        resposta.addProperty("status", "atualizado");
                        enviarResposta(exchange, 200, gson.toJson(resposta));
                    } else {
                        JsonObject erro = new JsonObject();
                        erro.addProperty("erro", "Parâmetros inválidos");
                        enviarResposta(exchange, 400, gson.toJson(erro));
                    }
                    exchange.close();
                }
            }
        });

        // ARQUIVOS ESTÁTICOS (FRONT-END)
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) {
                    path = "/index.html";
                }

                File file = new File("." + path);
                if (!file.exists()) {
                    String response = "404 (Not Found)";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }

                String contentType = "text/plain";
                if (path.endsWith(".html"))
                    contentType = "text/html; charset=UTF-8";
                else if (path.endsWith(".css"))
                    contentType = "text/css";
                else if (path.endsWith(".js"))
                    contentType = "application/javascript";

                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                OutputStream os = exchange.getResponseBody();
                java.nio.file.Files.copy(file.toPath(), os);
                os.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor rodando em http://localhost:8080/");
    }

    // === MÉTODOS AUXILIARES ===

    private static void enviarResposta(HttpExchange exchange, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String lerBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String gerarMatricula(Aluno a, Map<String, Integer> contadorDatas) {
        String dataBaseStr = "00000000";
        if (a.getDataNascimento() != null) {
            dataBaseStr = a.getDataNascimento()
                    .format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
        }
        int seq = contadorDatas.getOrDefault(dataBaseStr, 0) + 1;
        contadorDatas.put(dataBaseStr, seq);
        return dataBaseStr + String.format("%03d", seq);
    }
}