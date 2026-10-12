# Fábrica de PCBs — MC322, Tarefa 4 (Gradle, Javadoc e Tratamento de Erros)

Planta industrial de placas de circuito impresso (PCBs) com corte a laser, posicionamento de
componentes (pick-and-place), soldagem por refusão (reflow) e por onda, inspeção óptica (AOI) e teste
funcional. Gerencia demandas, estoque de matéria-prima, orçamento, máquinas, cenários de operação e
auditoria.

## Requisitos
- JDK 17 ou superior (`java -version`).

## Gradle Wrapper (passo único antes do primeiro commit)
O `gradle-wrapper.properties` já está no projeto (Gradle 8.10.2). Os arquivos binários/scripts do
wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`) precisam ser gerados uma vez
por quem tenha o Gradle instalado:

```bash
gradle wrapper --gradle-version 8.10.2
```

Depois, **faça commit** desses arquivos. A partir daí ninguém precisa instalar o Gradle.

## Comandos
| Comando | Efeito |
|---|---|
| `./gradlew build` | compila e constrói o projeto |
| `./gradlew run` | executa o menu interativo |
| `./gradlew javadoc` | gera a documentação em `build/docs/javadoc/index.html` |

No Windows use `gradlew.bat` no lugar de `./gradlew`.

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

## Tratamento de erros (resumo)
- `EntradaInvalidaException`: digitação/escolha inválida (texto no lugar de número, fora do intervalo).
- `DemandaInvalidaException` / `EstadoDemandaInvalidaException`: demanda inexistente, dados inválidos
  ou operação incompatível com o estado (ex.: produzir demanda já concluída).
- `RecursoInsuficienteException`: falta de matéria-prima (lista exatamente o que falta).
- `OrcamentoInsuficienteException`: saldo não cobre compra ou custo operacional.
- `MaquinaIndisponivelException`: etapa sem máquina operacional (em manutenção).

Toda operação valida **antes** de alterar o estado; se uma exceção é lançada, nada foi modificado.
O menu captura, exibe mensagem clara e volta ao menu. Veja `justificativa.txt`.
