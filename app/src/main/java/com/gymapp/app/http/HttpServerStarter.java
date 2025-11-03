package com.gymapp.app.http;

import com.gymapp.app.data.Attendance;
import com.gymapp.app.data.AttendanceService;
import com.gymapp.app.data.Member;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpServerStarter {
    private HttpServer server;
    private final AttendanceService attendanceService = new AttendanceService();

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new RootHandler());
            server.createContext("/checkin", new CheckinHandler());
            server.setExecutor(null); // default
            server.start();
            System.out.println("Check-in server started on http://localhost:" + port + "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to start HTTP server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    static class RootHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed");
                return;
            }
            String html = "" +
                    "<html><head><meta charset='utf-8'><title>GYMAPP Check-in</title>" +
                    "<style>body{font-family:sans-serif;margin:40px}input{padding:8px;font-size:16px}button{padding:8px 12px;margin-left:8px}</style>" +
                    "</head><body>" +
                    "<h2>GYMAPP – Check-in</h2>" +
                    "<form action='/checkin' method='get'>" +
                    "<label>Code membre (QR ou code-barres): <input name='code' autofocus/></label>" +
                    "<button type='submit'>Valider</button>" +
                    "</form>" +
                    "</body></html>";
            send(exchange, 200, html);
        }
    }

    class CheckinHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed");
                return;
            }
            Map<String, String> q = parseQuery(exchange.getRequestURI());
            String code = q.get("code");
            if (code == null || code.isBlank()) {
                send(exchange, 400, page("Erreur", "Code manquant"));
                return;
            }
            // Verify subscription status before check-in
            Member m = attendanceService
                    .latest(0) // just to access SessionFactory via service; we won't use this result
                    .stream().findFirst().map(Attendance::getMember).orElse(null); // dummy to keep compiler happy
            // Re-query member by code using underlying service
            m = new com.gymapp.app.data.MemberService().findByQr(code);
            if (m == null) {
                send(exchange, 404, page("Non trouvé", "Aucun membre pour le code: " + escape(code)));
                return;
            }
            boolean active = m.getSubscriptionStatus() == Member.SubscriptionStatus.ACTIVE
                    && (m.getSubscriptionEndAt() == null || !m.getSubscriptionEndAt().isBefore(java.time.LocalDate.now()));
            if (!active) {
                String end = m.getSubscriptionEndAt() == null ? "—" : m.getSubscriptionEndAt().toString();
                String body = "<p class='err'>Abonnement expiré ou inactif (fin: " + escape(end) + ")</p><p>Veuillez vous adresser à la réception pour un réabonnement.</p>";
                send(exchange, 200, page("Abonnement expiré", body));
                return;
            }
            Attendance a = attendanceService.checkInByCode(code);
            String msg = "<p class='ok'>Présence enregistrée pour <strong>" + escape(a.getMember().getFullName()) + "</strong> à " + escape(a.getCheckedAt().toString()) + "</p>";
            send(exchange, 200, page("OK", msg));
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String q = Optional.ofNullable(uri.getRawQuery()).orElse("");
        for (String pair : q.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }

    private static String page(String title, String bodyHtml) {
        return "<html><head><meta charset='utf-8'><title>" + escape(title) + "</title>" +
                "<style>body{font-family:sans-serif;margin:40px}a,button,input{font-size:16px} .ok{color:green}.err{color:red}</style>" +
                "</head><body><h2>" + escape(title) + "</h2><p>" + bodyHtml + "</p><p><a href='/'>Retour</a></p></body></html>";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
