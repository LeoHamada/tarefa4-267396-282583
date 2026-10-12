package pcb.modelo;

/**
 * Resumo de uma produção concluída com sucesso.
 *
 * @param idDemanda identificador da demanda produzida
 * @param produto nome do produto fabricado
 * @param quantidade placas fabricadas
 * @param horas tempo total de máquina utilizado, somando todas as etapas
 * @param custoOperacional custo de máquinas pago pela produção, em reais
 * @param receita valor recebido pela venda do lote, em reais
 * @author 267396-282583
 */
public record ResultadoProducao(int idDemanda, String produto, int quantidade, double horas,
        double custoOperacional, double receita) {

    /**
     * Calcula o resultado do lote descontando apenas o custo operacional (a matéria-prima já foi
     * paga no momento da compra).
     *
     * @return receita menos custo operacional, em reais
     */
    public double lucroOperacional() {
        return receita - custoOperacional;
    }
}
