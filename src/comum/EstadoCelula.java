package comum;

/** Conteúdo de uma célula na visão de um jogador. */
public enum EstadoCelula {
    DESCONHECIDA,   // tabuleiro adversário: ainda não atingida
    VAZIA,          // meu tabuleiro: sem navio e sem tiro
    NAVIO,          // meu tabuleiro: navio intacto
    AGUA,           // tiro que caiu na água
    ACERTO          // tiro que acertou um navio
}
