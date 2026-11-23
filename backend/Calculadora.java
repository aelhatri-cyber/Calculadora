import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class Calculadora {
    public static void main(String[] args) throws IOException {

        HttpServer servidor = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        servidor.createContext("/calcular", intercambio -> {

            intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );

            intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
            );

            intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "POST, OPTIONS"
            );

            if (intercambio.getRequestMethod().equals("OPTIONS")) {
                intercambio.sendResponseHeaders(204, -1);
                return;
            }

            String cuerpo = new String(
                intercambio.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
            );

            String respuesta;

            try {
                double n1 = obtenerNumero(cuerpo, "numero1");
                double n2 = obtenerNumero(cuerpo, "numero2");

                if (n1 != Math.floor(n1) || n2 != Math.floor(n2)
                    || n1 < -99999 || n1 > 99999
                    || n2 < -99999 || n2 > 99999) {
                    throw new IllegalArgumentException(
                        "Los números deben ser enteros entre -99999 y 99999."
                    );
                }

                String operacion = obtenerTexto(cuerpo, "operacion");

                double resultado;

                switch (operacion) {
                    case "suma":
                        resultado = n1 + n2;
                        break;

                    case "resta":
                        resultado = n1 - n2;
                        break;

                    case "multiplicacion":
                        resultado = n1 * n2;
                        break;

                    case "potencia":
                        resultado = Math.pow(n1, n2);
                        break;

                    default:
                        throw new IllegalArgumentException(
                            "Operación no permitida."
                        );
                }

                respuesta = "{\"resultado\":" + resultado + "}";

            } catch (Exception e) {
                respuesta = "{\"error\":\"" +
                    e.getMessage().replace("\"", "'") + "\"}";
            }

            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);

            intercambio.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
            );

            intercambio.sendResponseHeaders(200, bytes.length);

            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });

        servidor.start();

        System.out.println("Servidor iniciado en puerto 8080");
    }

    private static double obtenerNumero(String json, String campo) {

        Pattern patron = Pattern.compile(
            "\"" + campo + "\"\\s*:\\s*\"?(-?\\d+)\"?"
        );

        Matcher matcher = patron.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Número no válido.");
        }

        return Double.parseDouble(matcher.group(1));
    }

    private static String obtenerTexto(String json, String campo) {

        Pattern patron = Pattern.compile(
            "\"" + campo + "\"\\s*:\\s*\"([^\"]+)\""
        );

        Matcher matcher = patron.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Operación no válida.");
        }

        return matcher.group(1);
    }
}