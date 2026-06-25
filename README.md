# Boh! ─ Guia Interativo de Inversão de Listas Encadeadas

**Boh!** é um script interativo baseado em terminal feito em Python que apresenta, o conceito da inversão de uma lista duplamente encadeada. O tutorial é guiado pelo personagem chamado **Boh**, um robôzinho... Sabe? Ro-Boh.

---

## Controles e Interação

Durante o diálogo com o **Boh**, você pode interagir utilizando as seguintes teclas:

- **`S`** / **`Y`**: Responde "Sim" às perguntas.
- **`N`**: Responde "Não" às perguntas.
- **`Seta Direita`**: Pula a animação do diálogo atual ou avança para o próximo.
- **`Seta Esquerda`**: Volta ao diálogo anterior do histórico para rever a explicação.
- **`Espaço`**: Avança/confirma opções e estados.

---

## Funcionalidades

- **Mascote Interativo:** Expressões faciais em ASCII que mudam de acordo com o contexto da conversa (_idle, thinking, pokerface, annoyed, etc._).
- **Animações no Terminal:** Efeitos de digitação de texto letra a letra com renderização em tempo real de diagramas da estrutura de dados.
- **Efeitos Sonoros Sincronizados:** Sons de digitação que são reproduzidos à medida que o texto aparece.
- **Navegação de Histórico:** Permite avançar animações ou voltar para rever falas anteriores usando as setas do teclado e pausar a fala atual com a barra de espaço.
- **Bootstrap Automático (`.venv`):** Criação autônoma do ambiente virtual e instalação automatizada de dependências na primeira execução.

---

## Requisitos

1. **Python 3.13** (obrigatório).
2. **Efeitos Sonoros (Opcional):** Arquivos de áudio nos formatos `.wav`, `.ogg` ou `.mp3` localizados dentro de uma pasta chamada `sfx` no mesmo diretório do script.

---

## Como Executar

### 1. Clonar ou copiar o projeto

Garanta que a estrutura do seu diretório contenha o script e o arquivo de dependências:

```text
boh/
├── Boh.py
├── requirements.txt
└── sfx/               # Opcional (adicione seus arquivos .wav/.ogg/.mp3 aqui)
```

### 2. Rodar o script

Com o Python 3.13 instalado na sua máquina, basta executar o arquivo principal:

```bash
python Boh.py
```

> [!NOTE]
> **Como funciona o bootstrap do script:**
> Ao rodar o comando acima, o próprio script detecta se você está em um ambiente virtual (`.venv`). Caso não esteja, ele:
>
> 1. Cria a pasta `.venv` automaticamente com a versão correta do Python.
> 2. Instala/atualiza os pacotes necessários (`blessed` e `pygame`) a partir do arquivo `requirements.txt`.
> 3. Reinicia a si mesmo de dentro do ambiente virtual configurado.
