package comum;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Rede {
    private Rede() { }

    public static String ipLocalPara(String destino) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(destino), Constantes.PORTA_REGISTRY);
            InetAddress local = socket.getLocalAddress();
            if (local != null && !local.isAnyLocalAddress()) {
                return local.getHostAddress();
            }
        } catch (IOException ignorada) {
        }
        return "127.0.0.1";
    }

    public static List<String> ipsDaMaquina() {
        List<String> ips = new ArrayList<>();
        try {
            for (NetworkInterface placa : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!placa.isUp() || placa.isLoopback() || placa.isVirtual()) {
                    continue;
                }
                for (InetAddress endereco : Collections.list(placa.getInetAddresses())) {
                    if (endereco instanceof Inet4Address) {
                        ips.add(endereco.getHostAddress() + " (" + placa.getDisplayName() + ")");
                    }
                }
            }
        } catch (SocketException ignorada) {
        }
        return ips;
    }
}
