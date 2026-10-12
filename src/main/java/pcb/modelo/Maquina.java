package pcb.modelo;

/**
 * Equipamento da linha de produção (cortadora a laser, forno de reflow, AOI etc.).
 *
 * <p>Uma máquina possui capacidade (placas por hora) e custo por hora. Quando está em manutenção
 * não pode ser usada em produção. O tempo e o custo efetivos dependem do {@link Cenario} vigente.</p>
 *
 * @author 267396-282583
 */
public class Maquina {

    private final String nome;
    private final TipoMaquina tipo;
    private final int capacidadePorHora;
    private final double custoPorHora;
    private boolean operacional = true;
    private double horasAcumuladas;

    /**
     * Cria uma máquina operacional.
     *
     * @param nome identificação do equipamento (modelo)
     * @param tipo tipo de processo que executa
     * @param capacidadePorHora placas processadas por hora (positivo)
     * @param custoPorHora custo operacional por hora, em reais (positivo)
     * @throws IllegalArgumentException se algum valor for inválido (erro de configuração)
     */
    public Maquina(String nome, TipoMaquina tipo, int capacidadePorHora, double custoPorHora) {
        if (nome == null || tipo == null || capacidadePorHora <= 0 || custoPorHora <= 0) {
            throw new IllegalArgumentException("Definição de máquina inconsistente: " + nome);
        }
        this.nome = nome;
        this.tipo = tipo;
        this.capacidadePorHora = capacidadePorHora;
        this.custoPorHora = custoPorHora;
    }

    /**
     * Calcula o tempo necessário para processar um lote.
     *
     * @param quantidade número de placas do lote
     * @param cenario cenário de operação vigente
     * @return tempo em horas
     */
    public double horasPara(int quantidade, Cenario cenario) {
        return quantidade / (double) capacidadePorHora * cenario.getFatorTempo();
    }

    /**
     * Calcula o custo operacional de usar a máquina por um período.
     *
     * @param horas tempo de uso, em horas
     * @param cenario cenário de operação vigente
     * @return custo em reais
     */
    public double custoPara(double horas, Cenario cenario) {
        return horas * custoPorHora * cenario.getFatorCusto();
    }

    /**
     * Registra horas de uso efetivo da máquina.
     *
     * @param horas horas trabalhadas a acumular
     */
    public void registrarUso(double horas) {
        horasAcumuladas += horas;
    }

    /**
     * Alterna a máquina entre operacional e em manutenção.
     *
     * @return {@code true} se, após a chamada, a máquina está operacional
     */
    public boolean alternarManutencao() {
        operacional = !operacional;
        return operacional;
    }

    /**
     * Informa se a máquina pode ser usada.
     *
     * @return {@code true} se está operacional
     */
    public boolean isOperacional() {
        return operacional;
    }

    /**
     * Retorna o nome do equipamento.
     *
     * @return modelo/identificação
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o tipo de processo da máquina.
     *
     * @return tipo de máquina
     */
    public TipoMaquina getTipo() {
        return tipo;
    }

    /**
     * Retorna a capacidade nominal.
     *
     * @return placas por hora
     */
    public int getCapacidadePorHora() {
        return capacidadePorHora;
    }

    /**
     * Retorna o custo por hora nominal (sem fator de cenário).
     *
     * @return custo em reais por hora
     */
    public double getCustoPorHora() {
        return custoPorHora;
    }

    /**
     * Retorna o total de horas já trabalhadas.
     *
     * @return horas acumuladas
     */
    public double getHorasAcumuladas() {
        return horasAcumuladas;
    }
}
