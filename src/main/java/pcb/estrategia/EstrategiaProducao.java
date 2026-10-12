package pcb.estrategia;

import java.util.List;
import java.util.Optional;
import pcb.modelo.Demanda;

/**
 * Estratégia de escolha da próxima demanda a ser produzida (padrão Strategy).
 *
 * <p>A estratégia apenas decide <em>qual</em> demanda vem a seguir; a validação de recursos e a
 * execução ficam a cargo do gerenciador de produção.</p>
 *
 * @author 267396-282583
 */
public interface EstrategiaProducao {

    /**
     * Retorna o nome da estratégia.
     *
     * @return nome de exibição
     */
    String getNome();

    /**
     * Retorna a regra de seleção em linguagem natural.
     *
     * @return descrição do critério usado
     */
    String getDescricao();

    /**
     * Escolhe a próxima demanda entre as pendentes.
     *
     * @param pendentes demandas ainda não produzidas
     * @return a demanda escolhida, ou vazio se a lista estiver vazia
     */
    Optional<Demanda> escolherProxima(List<Demanda> pendentes);
}
