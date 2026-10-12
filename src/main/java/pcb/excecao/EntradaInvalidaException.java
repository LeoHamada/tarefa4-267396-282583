package pcb.excecao;

/**
 * Indica que um dado informado pelo usuário é inválido (texto no lugar de número, valor fora do
 * intervalo, opção de menu inexistente, quantidade não positiva etc.).
 *
 *
 * @author 267396-282583
 */
public class EntradaInvalidaException extends PlantaException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com uma mensagem destinada ao usuário.
     *
     * @param mensagem explicação do que está errado e de como corrigir
     */
    public EntradaInvalidaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção preservando a causa original (por exemplo, um {@code NumberFormatException}).
     *
     * @param mensagem explicação do que está errado e de como corrigir
     * @param causa exceção que originou o problema
     */
    public EntradaInvalidaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
