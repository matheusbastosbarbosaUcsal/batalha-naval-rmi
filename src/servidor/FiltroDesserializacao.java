package servidor;

import java.io.ObjectInputFilter;
import java.lang.reflect.Proxy;
import java.util.List;

public final class FiltroDesserializacao {
    private static final int PROFUNDIDADE_MAXIMA = 20;
    private static final int TAMANHO_MAXIMO_ARRAY = 1_000;
    private static final List<String> PACOTES_PERMITIDOS =
            List.of("comum.", "java.lang.", "java.util.", "java.rmi.", "sun.rmi.");

    private FiltroDesserializacao() {
    }

    public static ObjectInputFilter criar() {
        return info -> {
            if (info.depth() > PROFUNDIDADE_MAXIMA || info.arrayLength() > TAMANHO_MAXIMO_ARRAY) {
                return ObjectInputFilter.Status.REJECTED;
            }
            Class<?> classe = info.serialClass();
            if (classe == null) {
                return ObjectInputFilter.Status.UNDECIDED;
            }
            while (classe.isArray()) {
                classe = classe.getComponentType();
            }
            if (classe.isPrimitive() || Proxy.isProxyClass(classe)) {
                return ObjectInputFilter.Status.ALLOWED;
            }
            String nome = classe.getName();
            for (String pacote : PACOTES_PERMITIDOS) {
                if (nome.startsWith(pacote)) {
                    return ObjectInputFilter.Status.ALLOWED;
                }
            }
            Log.info("desserialização recusada: %s", nome);
            return ObjectInputFilter.Status.REJECTED;
        };
    }
}
