package servidor;

import comum.PartidaNaoEncontradaException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorPartidas {
    private final Map<String, Jogador> jogadores = new ConcurrentHashMap<>();
    private final Map<String, String> nomesEmUso = new ConcurrentHashMap<>();
    private final Map<String, Partida> partidas = new ConcurrentHashMap<>();

    public boolean reservarNome(String nome, String jogadorId) {
        return nomesEmUso.putIfAbsent(nome.toLowerCase(), jogadorId) == null;
    }

    public void registrarJogador(Jogador jogador) {
        jogadores.put(jogador.id(), jogador);
    }

    public Jogador buscarJogador(String jogadorId) {
        return jogadorId == null ? null : jogadores.get(jogadorId);
    }

    public Collection<Jogador> todosJogadores() {
        return jogadores.values();
    }

    public void removerJogador(Jogador jogador) {
        jogadores.remove(jogador.id());
        nomesEmUso.remove(jogador.nome().toLowerCase(), jogador.id());
    }

    public void registrarPartida(Partida partida) {
        partidas.put(partida.id(), partida);
    }

    public Partida buscarPartida(String partidaId) throws PartidaNaoEncontradaException {
        Partida partida = partidaId == null ? null : partidas.get(partidaId);
        if (partida == null) {
            throw new PartidaNaoEncontradaException("Partida não encontrada.");
        }
        return partida;
    }

    public void removerPartida(String partidaId) {
        partidas.remove(partidaId);
    }
}
