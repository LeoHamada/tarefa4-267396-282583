package pcb.excecao;

/**
 * Raiz das exceções de domínio da fábrica de PCBs.
 *
 * <p>Representa qualquer situação que viole uma regra da planta (falta de insumo, orçamento
 * insuficiente, estado inválido de demanda etc.). É uma exceção não verificada: as operações do
 * domínio validam tudo <em>antes</em> de alterar o estado, e a camada de interface captura a
 * exceção, informa o usuário e devolve o controle ao menu.</p>
 *
 * @author 267396-282583
 */
public class PlantaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com uma mensagem destinada ao usuário.
     *
     * @param mensagem explicação clara do problema ocorrido
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção preservando a causa original.
     *
     * @param mensagem explicação clara do problema ocorrido
     * @param causa exceção que originou o problema
     */
    public PlantaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
