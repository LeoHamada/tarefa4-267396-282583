# Fábrica de PCBs — MC322, Tarefa 4 267396 282583

Planta industrial de placas de circuito impresso (PCBs) com corte a laser, posicionamento de
componentes (pick-and-place), soldagem por refusão (reflow) e por onda, inspeção óptica (AOI) e teste
funcional. Gerencia demandas, estoque de matéria-prima, orçamento, máquinas, cenários de operação e
auditoria.

## Comandos
| Comando | Efeito |
|---|---|
| `./gradlew build` | compila e constrói o projeto |
| `./gradlew run` | executa o menu interativo |
| `./gradlew javadoc` | gera a documentação em `build/docs/javadoc/index.html` |


## Estrutura
```
src/main/java/pcb/
  Main.java                  ponto de entrada (monta a planta e demandas de exemplo)
  excecao/                   exceções de domínio (PlantaException e filhas)
  modelo/                    entidades, enums, estoque, orçamento, auditoria
  estrategia/                EstrategiaProducao + FIFO, Maior urgência, Maior receita
  servico/GerenciadorProducao regras de negócio (sem interação com o usuário)
  ui/                        MenuPrincipal e LeitorEntrada (captura e exibe erros)
  util/Formatacao.java       formatação de moeda/números em pt-BR
```

