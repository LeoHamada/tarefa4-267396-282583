package pcb.servico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pcb.estrategia.EstrategiaProducao;
import pcb.estrategia.MaiorReceita;
import pcb.estrategia.MaiorUrgencia;
import pcb.estrategia.OrdemDeChegada;
import pcb.excecao.DemandaInvalidaException;
import pcb.excecao.EntradaInvalidaException;
import pcb.excecao.MaquinaIndisponivelException;
import pcb.excecao.PlantaException;
import pcb.modelo.Auditoria;
import pcb.modelo.CatalogoProdutos;
import pcb.modelo.Cenario;
import pcb.modelo.Demanda;
import pcb.modelo.Estoque;
import pcb.modelo.Maquina;
import pcb.modelo.MateriaPrima;
import pcb.modelo.Orcamento;
import pcb.modelo.Produto;
import pcb.modelo.ResultadoProducao;
import pcb.modelo.StatusDemanda;
import pcb.modelo.TipoMaquina;
import pcb.util.Formatacao;

/**
 * Núcleo de regras de negócio da fábrica de PCBs: gerencia demandas, estoque, orçamento, máquinas,
 * estratégia de produção, cenário de operação e auditoria.
 *
 * <p><b>Consistência de estado:</b> toda operação que altera a planta primeiro valida todas as
 * condições (estado da demanda, disponibilidade de máquinas, matéria-prima e saldo) e só depois
 * aplica as mudanças. Assim, se uma {@link PlantaException} for lançada, nada foi alterado.</p>
 *
 * <p>Esta classe não interage com o usuário: apenas lança exceções, que são tratadas pela camada de
 * interface.</p>
 *
 * @author 267396-282583
 */
public class GerenciadorProducao {

    private final Estoque estoque;
    private final Orcamento orcamento;
    private final List<Maquina> maquinas;
    private final List<Produto> produtos;
    private final List<EstrategiaProducao> estrategias = List.of(
            new OrdemDeChegada(), new MaiorUrgencia(), new MaiorReceita());
    private final List<Demanda> demandas = new ArrayList<>();
    private final Auditoria auditoria = new Auditoria();
    private EstrategiaProducao estrategia = estrategias.get(0);
    private Cenario cenario = Cenario.NORMAL;
    private int proximoIdDemanda = 1;

    /**
     * Cria um gerenciador com os recursos informados.
     *
     * @param estoque estoque inicial de matéria-prima
     * @param orcamento orçamento inicial
     * @param maquinas equipamentos da linha de produção
     * @param produtos produtos que a planta sabe fabricar
     */
    public GerenciadorProducao(Estoque estoque, Orcamento orcamento, List<Maquina> maquinas,
            List<Produto> produtos) {
        this.estoque = estoque;
        this.orcamento = orcamento;
        this.maquinas = new ArrayList<>(maquinas);
        this.produtos = new ArrayList<>(produtos);
    }

    /**
     * Cria a planta padrão: estoque inicial, orçamento de R$ 25.000,00, uma máquina de cada
     * processo (corte a laser, pick-and-place, reflow, onda, inspeção óptica e teste) e o catálogo
     * de produtos padrão.
     *
     * @return gerenciador pronto para uso, sem demandas
     */
    public static GerenciadorProducao criarPlantaPadrao() {
        Map<MateriaPrima, Integer> inicial = new EnumMap<>(MateriaPrima.class);
        inicial.put(MateriaPrima.LAMINADO_FR4, 200);
        inicial.put(MateriaPrima.PASTA_DE_SOLDA, 1000);
        inicial.put(MateriaPrima.FLUXO_DE_SOLDA, 500);
        inicial.put(MateriaPrima.KIT_COMPONENTES_SMD, 200);

        List<Maquina> maquinas = List.of(
                new Maquina("Cortadora Laser LS-100", TipoMaquina.CORTE_LASER, 40, 180.00),
                new Maquina("Pick&Place PP-2000", TipoMaquina.PICK_AND_PLACE, 100, 220.00),
                new Maquina("Forno Reflow RF-8", TipoMaquina.SOLDAGEM_REFLOW, 60, 150.00),
                new Maquina("Soldagem por Onda WS-3", TipoMaquina.SOLDAGEM_ONDA, 50, 130.00),
                new Maquina("Inspetora Óptica AOI-X", TipoMaquina.INSPECAO_OPTICA, 120, 140.00),
                new Maquina("Bancada de Teste BT-5", TipoMaquina.TESTE_FUNCIONAL, 80, 90.00));

        return new GerenciadorProducao(new Estoque(inicial), new Orcamento(25000.00), maquinas,
                CatalogoProdutos.padrao());
    }

    // ------------------------------------------------------------------ demandas

    /**
     * Registra uma nova demanda pendente.
     *
     * @param produto produto solicitado
     * @param quantidade número de placas (positivo)
     * @param prazoDias prazo de entrega em dias (positivo)
     * @return a demanda criada, com identificador atribuído
     * @throws DemandaInvalidaException se o produto for nulo ou quantidade/prazo não forem positivos
     */
    public Demanda criarDemanda(Produto produto, int quantidade, int prazoDias) {
        Demanda demanda = new Demanda(proximoIdDemanda, produto, quantidade, prazoDias);
        proximoIdDemanda++;
        demandas.add(demanda);
        auditoria.registrar("DEMANDA", String.format("Demanda #%d criada: %d x %s, prazo de %d dia(s).",
                demanda.getId(), quantidade, produto.getNome(), prazoDias));
        return demanda;
    }

    /**
     * Busca uma demanda pelo identificador.
     *
     * @param id identificador da demanda
     * @return a demanda encontrada
     * @throws DemandaInvalidaException se não existir demanda com esse identificador
     */
    public Demanda buscarDemanda(int id) {
        return demandas.stream()
                .filter(d -> d.getId() == id)
                .findFirst()
                .orElseThrow(() -> new DemandaInvalidaException(
                        "Não existe demanda com o número " + id + "."));
    }

    /**
     * Cancela uma demanda pendente.
     *
     * @param id identificador da demanda
     * @throws DemandaInvalidaException se a demanda não existir
     * @throws pcb.excecao.EstadoDemandaInvalidaException se a demanda não estiver pendente
     */
    public void cancelarDemanda(int id) {
        try {
            Demanda demanda = buscarDemanda(id);
            demanda.cancelar();
            auditoria.registrar("DEMANDA", "Demanda #" + id + " cancelada.");
        } catch (PlantaException e) {
            registrarFalha("Cancelamento da demanda #" + id, e);
            throw e;
        }
    }

    /**
     * Lista todas as demandas, em ordem de criação.
     *
     * @return lista imutável de demandas
     */
    public List<Demanda> listarDemandas() {
        return Collections.unmodifiableList(demandas);
    }

    /**
     * Lista as demandas que ainda aguardam produção.
     *
     * @return nova lista com as demandas pendentes
     */
    public List<Demanda> listarPendentes() {
        return demandas.stream()
                .filter(d -> d.getStatus() == StatusDemanda.PENDENTE)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ produção

    /**
     * Produz uma demanda específica.
     *
     * <p>Regras: a demanda deve existir e estar pendente; cada etapa do processo precisa de uma
     * máquina operacional do tipo exigido; deve haver matéria-prima suficiente; o orçamento deve
     * cobrir o custo operacional (tempo de máquina x custo/hora x fator do cenário). Em caso de
     * sucesso, consome a matéria-prima, debita o custo, credita a receita e conclui a demanda.</p>
     *
     * @param idDemanda identificador da demanda
     * @return resumo da produção
     * @throws DemandaInvalidaException se a demanda não existir
     * @throws pcb.excecao.EstadoDemandaInvalidaException se a demanda não estiver pendente
     * @throws MaquinaIndisponivelException se faltar máquina operacional para alguma etapa
     * @throws pcb.excecao.RecursoInsuficienteException se faltar matéria-prima
     * @throws pcb.excecao.OrcamentoInsuficienteException se o saldo não cobrir o custo operacional
     */
    public ResultadoProducao produzir(int idDemanda) {
        try {
            return executarProducao(buscarDemanda(idDemanda));
        } catch (PlantaException e) {
            registrarFalha("Produção da demanda #" + idDemanda, e);
            throw e;
        }
    }

    /**
     * Produz a próxima demanda segundo a estratégia de produção vigente.
     *
     * @return resumo da produção
     * @throws DemandaInvalidaException se não houver demandas pendentes
     * @throws pcb.excecao.RecursoInsuficienteException se faltar matéria-prima
     * @throws MaquinaIndisponivelException se faltar máquina operacional
     * @throws pcb.excecao.OrcamentoInsuficienteException se o saldo não cobrir o custo operacional
     */
    public ResultadoProducao produzirProxima() {
        Demanda escolhida = estrategia.escolherProxima(listarPendentes())
                .orElseThrow(() -> new DemandaInvalidaException(
                        "Não há demandas pendentes para produzir."));
        auditoria.registrar("ESTRATEGIA", String.format("Estratégia '%s' escolheu a demanda #%d.",
                estrategia.getNome(), escolhida.getId()));
        return produzir(escolhida.getId());
    }

    private ResultadoProducao executarProducao(Demanda demanda) {
        // 1) Validações: nada é alterado nesta fase.
        demanda.exigirPendente("produzir");
        Produto produto = demanda.getProduto();
        int quantidade = demanda.getQuantidade();

        Map<Maquina, Double> horasPorMaquina = new LinkedHashMap<>();
        double custoOperacional = 0;
        double horasTotais = 0;
        for (TipoMaquina etapa : produto.getEtapas()) {
            Maquina maquina = selecionarMaquina(etapa);
            double horas = maquina.horasPara(quantidade, cenario);
            horasPorMaquina.merge(maquina, horas, Double::sum);
            custoOperacional += maquina.custoPara(horas, cenario);
            horasTotais += horas;
        }

        Map<MateriaPrima, Integer> insumos = produto.insumosPara(quantidade);
        estoque.verificarDisponibilidade(insumos);
        orcamento.garantirSaldo(custoOperacional);

        // 2) Efetivação: todas as condições foram verificadas, nenhuma etapa abaixo pode falhar.
        estoque.consumir(insumos);
        orcamento.debitar(custoOperacional);
        horasPorMaquina.forEach(Maquina::registrarUso);
        double receita = demanda.getReceitaPrevista();
        orcamento.creditar(receita);
        demanda.concluir();

        ResultadoProducao resultado = new ResultadoProducao(demanda.getId(), produto.getNome(),
                quantidade, horasTotais, custoOperacional, receita);
        auditoria.registrar("PRODUCAO", String.format(
                "Demanda #%d produzida: %d x %s em %s h (custo operacional %s, receita %s).",
                demanda.getId(), quantidade, produto.getNome(), Formatacao.decimal(horasTotais),
                Formatacao.moeda(custoOperacional), Formatacao.moeda(receita)));
        return resultado;
    }

    private Maquina selecionarMaquina(TipoMaquina tipo) {
        return maquinas.stream()
                .filter(m -> m.getTipo() == tipo && m.isOperacional())
                .findFirst()
                .orElseThrow(() -> new MaquinaIndisponivelException(String.format(
                        "Nenhuma máquina operacional para a etapa \"%s\" (equipamento em manutenção).",
                        tipo.getDescricao())));
    }

    // ------------------------------------------------------------------ compras e estoque

    /**
     * Calcula o custo de uma compra no cenário atual (preço-base x fator de preço dos insumos).
     *
     * @param materiaPrima item a comprar
     * @param quantidade unidades (positivo)
     * @return custo total em reais
     * @throws EntradaInvalidaException se o item for nulo ou a quantidade não for positiva
     */
    public double custoCompra(MateriaPrima materiaPrima, int quantidade) {
        if (materiaPrima == null) {
            throw new EntradaInvalidaException("A matéria-prima não foi informada.");
        }
        if (quantidade <= 0) {
            throw new EntradaInvalidaException("A quantidade a comprar deve ser maior que zero.");
        }
        return materiaPrima.getPrecoUnitario() * quantidade * cenario.getFatorPrecoInsumos();
    }

    /**
     * Compra matéria-prima: debita o custo do orçamento e adiciona a quantidade ao estoque.
     *
     * @param materiaPrima item a comprar
     * @param quantidade unidades (positivo)
     * @return custo da compra em reais
     * @throws EntradaInvalidaException se o item for nulo ou a quantidade não for positiva
     * @throws pcb.excecao.OrcamentoInsuficienteException se o saldo não cobrir a compra; nesse
     *         caso estoque e orçamento permanecem inalterados
     */
    public double comprarMateriaPrima(MateriaPrima materiaPrima, int quantidade) {
        try {
            double custo = custoCompra(materiaPrima, quantidade);
            orcamento.debitar(custo);
            estoque.adicionar(materiaPrima, quantidade);
            auditoria.registrar("COMPRA", String.format("Compra de %d %s de %s por %s.",
                    quantidade, materiaPrima.getUnidade(), materiaPrima.getNome(),
                    Formatacao.moeda(custo)));
            return custo;
        } catch (PlantaException e) {
            registrarFalha("Compra de matéria-prima", e);
            throw e;
        }
    }

    // ------------------------------------------------------------------ máquinas

    /**
     * Alterna uma máquina entre operacional e em manutenção.
     *
     * @param numero posição da máquina na lista, começando em 1
     * @return a máquina alterada
     * @throws EntradaInvalidaException se o número não corresponder a nenhuma máquina
     */
    public Maquina alternarManutencao(int numero) {
        if (numero < 1 || numero > maquinas.size()) {
            throw new EntradaInvalidaException(
                    "Não existe máquina com o número " + numero + ". Informe um valor de 1 a "
                            + maquinas.size() + ".");
        }
        Maquina maquina = maquinas.get(numero - 1);
        boolean operacional = maquina.alternarManutencao();
        auditoria.registrar("MAQUINA", String.format("%s agora está %s.", maquina.getNome(),
                operacional ? "operacional" : "em manutenção"));
        return maquina;
    }

    // ------------------------------------------------------------------ configuração

    /**
     * Define a estratégia usada por {@link #produzirProxima()}.
     *
     * @param numero posição da estratégia em {@link #getEstrategiasDisponiveis()}, começando em 1
     * @throws EntradaInvalidaException se o número for inválido
     */
    public void selecionarEstrategia(int numero) {
        if (numero < 1 || numero > estrategias.size()) {
            throw new EntradaInvalidaException(
                    "Estratégia inexistente. Informe um valor de 1 a " + estrategias.size() + ".");
        }
        estrategia = estrategias.get(numero - 1);
        auditoria.registrar("CONFIGURACAO", "Estratégia alterada para: " + estrategia.getNome());
    }

    /**
     * Define o cenário de operação vigente.
     *
     * @param novoCenario cenário desejado
     * @throws EntradaInvalidaException se o cenário for nulo
     */
    public void setCenario(Cenario novoCenario) {
        if (novoCenario == null) {
            throw new EntradaInvalidaException("O cenário não foi informado.");
        }
        cenario = novoCenario;
        auditoria.registrar("CONFIGURACAO", "Cenário alterado para: " + novoCenario.getNome());
    }

    private void registrarFalha(String operacao, PlantaException e) {
        auditoria.registrar("ERRO", operacao + " recusada: " + e.getMessage());
    }

    // ------------------------------------------------------------------ consultas

    /**
     * Retorna as estratégias de produção disponíveis.
     *
     * @return lista imutável de estratégias
     */
    public List<EstrategiaProducao> getEstrategiasDisponiveis() {
        return estrategias;
    }

    /**
     * Retorna a estratégia vigente.
     *
     * @return estratégia de produção
     */
    public EstrategiaProducao getEstrategia() {
        return estrategia;
    }

    /**
     * Retorna o cenário vigente.
     *
     * @return cenário de operação
     */
    public Cenario getCenario() {
        return cenario;
    }

    /**
     * Retorna o estoque de matéria-prima.
     *
     * @return estoque
     */
    public Estoque getEstoque() {
        return estoque;
    }

    /**
     * Retorna o saldo atual do orçamento.
     *
     * @return saldo em reais
     */
    public double getSaldo() {
        return orcamento.getSaldo();
    }

    /**
     * Retorna os equipamentos da linha, na ordem de numeração.
     *
     * @return lista imutável de máquinas
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /**
     * Retorna o catálogo de produtos fabricáveis.
     *
     * @return lista imutável de produtos
     */
    public List<Produto> getProdutos() {
        return Collections.unmodifiableList(produtos);
    }

    /**
     * Retorna o histórico de auditoria.
     *
     * @return auditoria da planta
     */
    public Auditoria getAuditoria() {
        return auditoria;
    }
}
