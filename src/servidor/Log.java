package servidor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Log {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static volatile boolean ativo = true;

    private Log() {
    }

    public static void setAtivo(boolean valor) {
        ativo = valor;
    }

    public static void info(String formato, Object... args) {
        if (ativo) {
            System.out.printf("%s [%s] %s%n", LocalTime.now().format(HORA),
                    Thread.currentThread().getName(), String.format(formato, args));
        }
    }
}
