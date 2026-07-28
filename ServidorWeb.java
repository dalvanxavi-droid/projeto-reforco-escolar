import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServidorWeb {

    private static List<Aluno> alunos = GerenciadorArquivo.carregarAlunos();
    private static List<Agendamento> agendamentos = GerenciadorArquivo.carregarAgendamentos();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ROTA API: ALUNOS
        server.createContext("/api/alunos", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    java.util.Map<String, Integer> contadorDatas = new java.util.HashMap<>();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < alunos.size(); i++) {
                        Aluno a = alunos.get(i);
                        String respNome = a.getResponsavel() != null ? a.getResponsavel().nome() : "Sem cadastro";
                        String respFone = a.getResponsavel() != null ? a.getResponsavel().telefone() : "--";
                        String respEnd = a.getResponsavel() != null ? a.getResponsavel().endereco() : "";
                        String statusPag = a.getStatusPagamento() != null ? a.getStatusPagamento().name() : "PENDENTE";
                        String statusPagDesc = a.getStatusPagamento() != null ? a.getStatusPagamento().getDescricao()
                                : "Pendente";

                        String dataBaseStr = "00000000";
                        if (a.getDataNascimento() != null) {
                            dataBaseStr = a.getDataNascimento()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
                        }
                        int seq = contadorDatas.getOrDefault(dataBaseStr, 0) + 1;
                        contadorDatas.put(dataBaseStr, seq);
                        String matriculaGerada = dataBaseStr + String.format("%03d", seq);

                        // Montando o JSON com Locale.US para garantir o ponto decimal ao invés de
                        // vírgula
                        json.append(String.format(java.util.Locale.US,
                                "{\"matricula\":\"%s\",\"nome\":\"%s\",\"dataNascimento\":\"%s\",\"anoEscolar\":\"%s\",\"nivelLeitura\":\"%s\",\"temNecessidade\":%b,\"descricaoNecessidade\":\"%s\",\"responsavelNome\":\"%s\",\"responsavelTelefone\":\"%s\",\"responsavelEndereco\":\"%s\",\"statusPagamento\":\"%s\",\"statusPagamentoDesc\":\"%s\",\"valorContrato\":%.2f,\"cicloPagamento\":\"%s\"}",
                                matriculaGerada, a.getNome(), a.getDataNascimento(), a.getAnoEscolar(),
                                a.getNivelLeitura(),
                                a.isTemNecessidadeEspecial(),
                                a.getDescricaoNecessidade() != null ? a.getDescricaoNecessidade() : "", respNome,
                                respFone, respEnd, statusPag, statusPagDesc,
                                a.getValorContrato(),
                                a.getCicloPagamento() != null ? a.getCicloPagamento() : "MENSAL"));

                        if (i < alunos.size() - 1)
                            json.append(",");
                    }
                    json.append("]");

                    byte[] response = json.toString().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                }

                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    String nome = extrairValorJson(body, "nome");
                    LocalDate dataNasc = LocalDate.parse(extrairValorJson(body, "dataNascimento"));
                    String anoEscolar = extrairValorJson(body, "anoEscolar");
                    NivelLeitura nivel = NivelLeitura.valueOf(extrairValorJson(body, "nivelLeitura"));
                    boolean temNecessidade = Boolean.parseBoolean(extrairValorJson(body, "temNecessidadeEspecial"));

                    String nomeResp = extrairValorJson(body, "nomeResponsavel");
                    String foneResp = extrairValorJson(body, "telefoneResponsavel");
                    String endResp = extrairValorJson(body, "enderecoResponsavel");
                    Responsavel resp = new Responsavel(nomeResp, foneResp, endResp);
                    String descNee = extrairValorJson(body, "descricaoNecessidade");

                    // Lendo valores financeiros vindos da requisição (com fallback seguro)
                    String valorStr = extrairValorJson(body, "valorContrato");
                    double valorContrato = (valorStr != null && !valorStr.isEmpty()) ? Double.parseDouble(valorStr)
                            : 1800.0;
                    String cicloPagamento = extrairValorJson(body, "cicloPagamento");
                    if (cicloPagamento == null || cicloPagamento.isEmpty())
                        cicloPagamento = "MENSAL";

                    alunos.add(new Aluno(nome, dataNasc, anoEscolar, resp, nivel, temNecessidade, descNee,
                            StatusPagamento.PENDENTE, valorContrato, cicloPagamento));
                    GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));

                    String response = "{\"status\":\"sucesso\"}";
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(201, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }

                if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    String nome = extrairValorJson(body, "nome");
                    for (Aluno a : alunos) {
                        if (a.getNome().equalsIgnoreCase(nome)) {
                            a.setNome(extrairValorJson(body, "nome"));
                            a.setDataNascimento(LocalDate.parse(extrairValorJson(body, "dataNascimento")));
                            a.setAnoEscolar(extrairValorJson(body, "anoEscolar"));
                            a.setNivelLeitura(NivelLeitura.valueOf(extrairValorJson(body, "nivelLeitura")));
                            a.setTemNecessidadeEspecial(
                                    Boolean.parseBoolean(extrairValorJson(body, "temNecessidadeEspecial")));
                            a.setDescricaoNecessidade(extrairValorJson(body, "descricaoNecessidade"));

                            String nomeResp = extrairValorJson(body, "nomeResponsavel");
                            String foneResp = extrairValorJson(body, "telefoneResponsavel");
                            String endResp = extrairValorJson(body, "enderecoResponsavel");
                            a.setResponsavel(new Responsavel(nomeResp, foneResp, endResp));

                            // Atualizando dados financeiros se enviados na edição
                            String valorStr = extrairValorJson(body, "valorContrato");
                            if (valorStr != null && !valorStr.isEmpty()) {
                                a.setValorContrato(Double.parseDouble(valorStr));
                            }
                            String ciclo = extrairValorJson(body, "cicloPagamento");
                            if (ciclo != null && !ciclo.isEmpty()) {
                                a.setCicloPagamento(ciclo);
                            }

                            break;
                        }
                    }

                    GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));

                    String response = "{\"status\":\"atualizado\"}";
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }

                if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    String matricula = extrairValorJson(body, "matricula");

                    java.util.Map<String, Integer> contadorDatas = new java.util.HashMap<>();
                    java.util.Iterator<Aluno> iterator = alunos.iterator();

                    while (iterator.hasNext()) {
                        Aluno a = iterator.next();

                        String dataBaseStr = "00000000";
                        if (a.getDataNascimento() != null) {
                            dataBaseStr = a.getDataNascimento()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
                        }
                        int seq = contadorDatas.getOrDefault(dataBaseStr, 0) + 1;
                        contadorDatas.put(dataBaseStr, seq);
                        String matriculaGerada = dataBaseStr + String.format("%03d", seq);

                        if (matriculaGerada.equals(matricula.trim())) {
                            iterator.remove();
                            break;
                        }
                    }

                    GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));

                    String response = "{\"status\":\"removido\"}";
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }
            }
        });

        // ROTA API: AGENDAMENTOS
        server.createContext("/api/agendamentos", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < agendamentos.size(); i++) {
                        Agendamento a = agendamentos.get(i);
                        json.append(String.format(
                                "{\"id\":\"%s\",\"matriculaAluno\":\"%s\",\"data\":\"%s\",\"hora\":\"%s\",\"pago\":%b,\"observacao\":\"%s\"}",
                                a.getId(), a.getMatriculaAluno(), a.getData(), a.getHora(), a.isPago(),
                                a.getObservacao() != null ? a.getObservacao() : ""));
                        if (i < agendamentos.size() - 1)
                            json.append(",");
                    }
                    json.append("]");

                    byte[] response = json.toString().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                }

                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    String matricula = extrairValorJson(body, "matriculaAluno");
                    String data = extrairValorJson(body, "data");
                    String hora = extrairValorJson(body, "hora");
                    String obs = extrairValorJson(body, "observacao");

                    // Validação para impedir choque de horário
                    boolean conflito = false;
                    for (Agendamento a : agendamentos) {
                        if (a.getData().toString().equals(data) && a.getHora().equals(hora)) {
                            conflito = true;
                            break;
                        }
                    }

                    if (conflito) {
                        String response = "{\"erro\":\"Conflito de horario\"}";
                        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                        exchange.sendResponseHeaders(409, response.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                        os.close();
                        return;
                    }

                    String id = "AG-" + System.currentTimeMillis();

                    Agendamento novo = new Agendamento(id, matricula, LocalDate.parse(data), hora, false, obs);
                    agendamentos.add(novo);
                    GerenciadorArquivo.salvarAgendamentos(agendamentos);

                    String response = "{\"status\":\"sucesso\"}";
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(201, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }
                if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    String id = extrairValorJson(body, "id");

                    for (Agendamento a : agendamentos) {
                        if (a.getId().equals(id)) {
                            String novaData = extrairValorJson(body, "data");
                            String novaHora = extrairValorJson(body, "hora");
                            String pagoStr = extrairValorJson(body, "pago");
                            if (!novaData.isEmpty())
                                a.setData(LocalDate.parse(novaData));
                            if (!novaHora.isEmpty())
                                a.setHora(novaHora);
                            if (!pagoStr.isEmpty())
                                a.setPago(Boolean.parseBoolean(pagoStr));
                            break;
                        }
                    }
                    GerenciadorArquivo.salvarAgendamentos(agendamentos);

                    String response = "{\"status\":\"atualizado\"}";
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }

                if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    if (body.startsWith("[")) {
                        body = body.replace("[", "").replace("]", "").replace("\"", "");
                        String[] ids = body.split(",");
                        for (String id : ids) {
                            agendamentos.removeIf(a -> a.getId().trim().equals(id.trim()));
                        }
                    } else {
                        String id = extrairValorJson(body, "id");
                        agendamentos.removeIf(a -> a.getId().equals(id));
                    }
                    GerenciadorArquivo.salvarAgendamentos(agendamentos);

                    String response = "{\"status\":\"removido\"}";
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }
            }
        });

        // ROTA API: ALTERAR STATUS DE PAGAMENTO
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
                        for (Aluno a : alunos) {
                            if (a.getNome().equalsIgnoreCase(matricula)) {
                                a.setStatusPagamento(novoStatus);
                                break;
                            }
                        }
                        GerenciadorArquivo.salvarAlunos(new ArrayList<>(alunos));
                        exchange.sendResponseHeaders(200, -1);
                    } else {
                        exchange.sendResponseHeaders(400, -1);
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

    private static String extrairValorJson(String json, String chave) {
        String busca = "\"" + chave + "\":";
        int inicio = json.indexOf(busca);
        if (inicio == -1)
            return "";
        inicio += busca.length();

        char primeiroCaractere = json.charAt(inicio);
        if (primeiroCaractere == '"') {
            inicio++;
            int fim = json.indexOf("\"", inicio);
            return json.substring(inicio, fim);
        } else {
            int fim = inicio;
            while (fim < json.length() && json.charAt(fim) != ',' && json.charAt(fim) != '}') {
                fim++;
            }
            return json.substring(inicio, fim).trim();
        }
    }
}
