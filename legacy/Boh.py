import hashlib
import os
import shutil
import subprocess
import sys
from pathlib import Path
from re import match, sub
from time import sleep, time as current_time
from random import choice
from os import path, makedirs, listdir

# Bootstrap de ambiente virtual
# Este script exige Python 3.13 para criar o ambiente virtual e rodar.
# Se o 3.13 nao estiver instalado, instrucoes especificas por SO sao exibidas.

_REQUIRED_PYTHON = "3.13"
_VENV_DIR = Path(__file__).resolve().parent / ".venv"
_REQ_FILE = Path(__file__).resolve().parent / "requirements.txt"


def _print_install_instructions():
    """Imprime instrucoes de instalacao do Python 3.13 e encerra o programa."""
    msg = [
        "",
        "============================================================",
        "               PYTHON 3.13 NAO ENCONTRADO",
        "============================================================",
        "",
        f"Este projeto requer Python {_REQUIRED_PYTHON} para funcionar.",
        "Por favor, instale-o antes de continuar.",
        "",
    ]

    if os.name == "nt":
        msg.extend([
            "[Windows]",
            "1. Acesse o site oficial de downloads do Python:",
            "   https://www.python.org/downloads/release/python-3130/",
            "",
            "2. Role ate 'Windows installer (64-bit)' e baixe o executavel.",
            "",
            "3. Durante a instalacao, MARQUE a opcao:",
            "   [x] Add Python to PATH",
            "",
            "4. Clique em 'Install Now' e aguarde a conclusao.",
            "",
            "5. Apos a instalacao, feche e reabra o terminal/VS Code",
            "   e execute este script novamente.",
        ])
    elif sys.platform == "darwin":
        msg.extend([
            "[macOS]",
            "1. Acesse o site oficial de downloads do Python:",
            "   https://www.python.org/downloads/release/python-3130/",
            "",
            "2. Baixe o instalador 'macOS 64-bit universal2 installer'.",
            "",
            "3. Abra o .pkg baixado e siga as instrucoes do assistente.",
            "",
            "4. Apos a instalacao, reabra o terminal e execute",
            "   este script novamente.",
            "",
            "   Alternativa (Homebrew):",
            "   brew install python@3.13",
        ])
    else:
        msg.extend([
            "[Linux]",
            "Instale Python 3.13 usando o gerenciador de pacotes da sua distro:",
            "",
            "   Debian/Ubuntu:",
            "   sudo apt update && sudo apt install python3.13 python3.13-venv python3.13-pip",
            "",
            "   Fedora/RHEL:",
            "   sudo dnf install python3.13 python3.13-devel",
            "",
            "   Arch Linux:",
            "   sudo pacman -S python",
            "",
            "Apos a instalacao, execute este script novamente.",
        ])

    print("\n".join(msg))
    sys.exit(1)


def _find_python(version_str: str) -> str | None:
    """Tenta encontrar o executavel do Python da versao especificada."""
    candidates = []

    if os.name == "nt":
        localappdata = os.environ.get("LOCALAPPDATA", "")
        candidates = [
            rf"C:\Python{version_str.replace('.', '')}\python.exe",
            os.path.join(
                localappdata,
                fr"Programs\Python\Python{version_str.replace('.', '')}\python.exe",
            ),
        ]
    else:
        candidates = [
            f"/usr/bin/python{version_str}",
            f"/usr/local/bin/python{version_str}",
            f"/opt/python{version_str}/bin/python{version_str}",
            f"/opt/homebrew/bin/python{version_str}",
        ]

    for exe in candidates:
        if os.path.isfile(exe):
            return exe

    # py launcher (Windows)
    if os.name == "nt":
        try:
            result = subprocess.run(
                ["py", f"-{version_str}", "-c", "import sys; print(sys.executable)"],
                capture_output=True,
                text=True,
                check=False,
            )
            if result.returncode == 0:
                exec_path = result.stdout.strip()
                if os.path.isfile(exec_path):
                    return exec_path
        except FileNotFoundError:
            pass

    return None


def _ensure_venv() -> None:
    # 1. Checa se o script está rodando DE DENTRO de um ambiente virtual ativo
    if sys.prefix != sys.base_prefix:
        current_version = f"{sys.version_info.major}.{sys.version_info.minor}"
        if current_version != _REQUIRED_PYTHON:
            print("\n\033[1;31m[ERRO] Ambiente virtual ativo incompatível!\033[0m")
            print(f"Você está com um ambiente virtual ativado usando Python {current_version}.")
            print(f"Este projeto exige exclusivamente Python {_REQUIRED_PYTHON}.")
            print("\nPor favor, desative-o executando o comando:")
            print("  deactivate")
            print("\nEm seguida, apague a pasta .venv e rode este script novamente.\n")
            sys.exit(1)
        return

    python_exe = _find_python(_REQUIRED_PYTHON)
    if not python_exe:
        _print_install_instructions()

    # Corrigido: Linux/Mac não usam ".exe"
    exe_name = "python.exe" if os.name == "nt" else "python"
    venv_python = _VENV_DIR / ("Scripts" if os.name == "nt" else "bin") / exe_name

    # 2. Verifica a integridade e versão de um .venv já existente na pasta
    if _VENV_DIR.exists():
        recreate = False
        if not venv_python.exists():
            recreate = True  # Pasta existe, mas está corrompida/sem executável
        else:
            # Puxa a versão de dentro do venv existente
            try:
                res = subprocess.run(
                    [str(venv_python), "-c", "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')"],
                    capture_output=True, text=True
                )
                venv_version = res.stdout.strip() if res.returncode == 0 else None
            except Exception:
                venv_version = None

            if venv_version != _REQUIRED_PYTHON:
                print(f"Aviso: O ambiente virtual atual usa Python {venv_version}.")
                print(f"Ele será removido e recriado com Python {_REQUIRED_PYTHON}...")
                recreate = True

        if recreate:
            try:
                shutil.rmtree(_VENV_DIR)
            except PermissionError:
                print("\n\033[1;31m[ERRO] Não foi possível remover a pasta .venv antiga.\033[0m")
                print("Provavelmente o ambiente virtual está ativo em outro terminal ou editor (como o VS Code).")
                print("Por favor, feche as abas de terminal abertas, digite 'deactivate', ou apague a pasta manualmente e tente novamente.\n")
                sys.exit(1)

    # 3. Cria o ambiente virtual se necessário
    if not _VENV_DIR.exists():
        print(f"Criando ambiente virtual Python {_REQUIRED_PYTHON}...")
        result = subprocess.run([python_exe, "-m", "venv", str(_VENV_DIR)])
        if result.returncode != 0:
            print("Falha ao criar o ambiente virtual.")
            sys.exit(1)
        print("Ambiente virtual criado.")

    # 4. Instala/Atualiza dependencias
    if _REQ_FILE.exists():
        marker = _VENV_DIR / ".boh_reqs_hash"
        current_hash = hashlib.sha256(_REQ_FILE.read_bytes()).hexdigest()
        stored = ""
        if marker.exists():
            stored = marker.read_text().strip()

        if stored != current_hash:
            if not stored:
                print("Instalando dependencias...")
            else:
                print("Atualizando dependencias...")

            result = subprocess.run(
                [str(venv_python), "-m", "pip", "install", "-r", str(_REQ_FILE)]
            )
            if result.returncode != 0:
                print("Falha ao instalar dependencias.")
                sys.exit(1)
            marker.write_text(current_hash, encoding="utf-8")
            print("Dependencias prontas!")

    # 5. Re-executa dentro do venv
    args = [str(venv_python), os.path.abspath(__file__)] + sys.argv[1:]
    sys.stdout.flush()
    sys.stderr.flush()

    if os.name == "nt":
        try:
            result = subprocess.run(args)
            sys.exit(result.returncode)
        except KeyboardInterrupt:
            sys.exit(130)
    else:
        os.execv(str(venv_python), args)

_ensure_venv()

try:
    from blessed import Terminal
    from pygame import mixer, error as pygame_error
except ImportError as exc:
    print(f"Dependencia faltando: {exc.name}")
    print("Tente deletar a pasta .venv e rodar o script novamente.")
    sys.exit(1)

term = Terminal()


# Classe para selecionar itens aleatórios sem repetição
class ShuffledSelector:
    def __init__(self, items):
        self.items = list(items)  # Cria uma cópia da lista de itens
        self.available_indices = list(range(len(items)))  # Índices disponíveis

    def select(self):
        # Se não houver índices disponíveis, recarrega todos
        if not self.available_indices:
            self.available_indices = list(range(len(self.items)))

        # Escolhe um índice aleatório dos disponíveis
        idx = choice(self.available_indices)

        # Remove o índice escolhido da lista de disponíveis
        self.available_indices.remove(idx)

        # Retorna o item correspondente ao índice
        return self.items[idx]


# Inicializar o mixer do pygame
mixer.init(frequency=44100, size=-16, channels=1, buffer=512)

# Diretório onde os efeitos sonoros estão armazenados
sfx_dir = path.join(path.dirname(path.abspath(__file__)), "sfx")
makedirs(sfx_dir, exist_ok=True)

# Verificar se existem arquivos de som, ou criar uma lista padrão
sfx_files = (
    [f for f in listdir(sfx_dir) if f.endswith((".wav", ".ogg", ".mp3"))]
    if path.exists(sfx_dir)
    else []
)

# Se não existirem arquivos de som, usamos apenas o canal sem reproduzir nada
if not sfx_files:
    print(
        f"Aviso: Nenhum arquivo de som encontrado em {sfx_dir}. Os efeitos sonoros não serão reproduzidos."
    )
    print("Para adicionar sons, coloque arquivos .wav, .ogg ou .mp3 na pasta 'sfx'.")

# Carregar os efeitos sonoros
sound_effects = []
for sfx_file in sfx_files:
    try:
        sound = mixer.Sound(path.join(sfx_dir, sfx_file))
        sound.set_volume(0.7)  # Ajuste o volume conforme necessário (0.0 a 1.0)
        sound_effects.append(sound)
    except pygame_error:
        print(f"Erro ao carregar o arquivo de som: {sfx_file}")

# Criar o seletor de sons sem repetição
sound_selector = ShuffledSelector(sound_effects)

# Canal para reprodução de som
sound_channel = mixer.Channel(0)


def parse_formatted_text(text):
    # Padrão para encontrar códigos ANSI (somente cores e formatação tipográfica)
    ansi_pattern = r"\033\[[0-9;]*m"

    result = []
    current_format = ""
    i = 0

    while i < len(text):
        # Verifica se encontrou um código ANSI
        ansi_match = match(ansi_pattern, text[i:])
        if ansi_match:
            ansi_code = ansi_match.group()
            if ansi_code == "\033[0m":
                current_format = ""
            else:
                current_format += ansi_code
            i += len(ansi_code)
        else:
            # Adiciona o caractere com sua formatação atual
            char = text[i]
            if char != " " or current_format:  # Preserva espaços formatados
                formatted_char = (
                    current_format + char + ("\033[0m" if current_format else "")
                )
                result.append(formatted_char)
            else:
                result.append(char)
            i += 1

    return result


time = None  # Variável para controlar o tempo do último som reproduzido


def play_typing_sound(static=True, input=" "):
    """Reproduz um som aleatório de digitação, interrompendo qualquer som em curso."""
    global time
    play = False
    if time:
        play = True if static and input == " " else current_time() - time > 0.3
    else:
        time = current_time()
        play = True
    if play and sound_effects:
        time = current_time()
        sound_channel.stop()  # Interrompe qualquer som em reprodução
        sound = sound_selector.select()
        sound_channel.play(sound)


def arrow_colorize(text, colorize_arrows=False):
    """Coloriza as setas no texto se colorize_arrows=True."""
    if not colorize_arrows:
        return text

    def colorize(char):
        if char in ["‹", "›"]:
            return term.orange + char + term.normal
        elif char in ["»", "«"]:
            return term.blue + char + term.normal
        return char

    if isinstance(text, list):
        return [colorize(char) for char in text]
    else:
        return "".join(colorize(char) for char in text)


seen_talks = []
current_talk_index = -1


def talk(input=" ", expression="idle", amount=.675, static="", colorize_arrows=False):
    """Exibe texto animado com expressões e efeitos sonoros a cada caractere alfanumérico."""
    global seen_talks, current_talk_index

    current_args = (input, expression, amount, static, colorize_arrows)
    if not seen_talks or seen_talks[-1] != current_args:
        seen_talks.append(current_args)
        current_talk_index = len(seen_talks) - 1
    else:
        current_talk_index = len(seen_talks) - 1

    expressions = {
        "idle": [
            "[ ▀ ¸ ▀]",
            "[ ▀ ° ▀]",
            "[ ▀ ■ ▀]",
            "[ ▀ ─ ▀]",
            "[ ▀ ~ ▀]",
            "[ ▀ ▄ ▀]",
            "[ ▀ ¬ ▀]",
            "[ ▀ · ▀]",
            "[ ▀ _ ▀]",
        ],
        "pokerface": ["[ ▀ ‗ ▀]", "[ ▀ ¯ ▀]", "[ ▀ ¡ ▀]"],
        "thinking": ["[ ─ ´ ─]", "[ ─ » ─]"],
        "open mouth": ["[ ▀ ß ▀]", "[ ▀ █ ▀]"],
        "annoyed": ["[ ▀ ı ▀]", "[ ▀ ^ ▀]"],
        "looking down": ["[ ▄ . ▄]", "[ ▄ _ ▄]", "[ ▄ ₒ ▄]", "[ ▄ ₗ ▄]", "[ ▄ ‗ ▄]"],
    }

    def render_frame(displayed_chars):
        expr_list = expressions.get(expression, expressions["idle"])
        current_expr = expr_list[
            len(displayed_chars) // (len(expr_list) // 2) % len(expr_list)
        ]

        displayed_text = ""
        if displayed_chars:
            if len(displayed_chars) == 1 and displayed_chars[0] == " ":
                displayed_text = arrow_colorize(static, colorize_arrows)
            else:
                displayed_text = "".join(arrow_colorize(displayed_chars, colorize_arrows))
        else:
            displayed_text = arrow_colorize(static, colorize_arrows)

        static_text = arrow_colorize(static, colorize_arrows) if displayed_chars and displayed_chars[0] != " " else ""

        full_output = f"\n   {current_expr}  ──┤ {displayed_text} │  {static_text}"
        lines = full_output.split("\n")
        
        home_seq = term.home or "\033[1;1H"
        clear_eol_seq = term.clear_eol or "\033[K"
        clear_eos_seq = term.clear_eos or "\033[J"
        
        formatted_lines = [line + clear_eol_seq for line in lines]
        print(home_seq + "\n".join(formatted_lines) + clear_eos_seq, end="", flush=True)

    print("\033[1;1H\033[0J", end="")

    if input == " ":
        remaining = list(input)
    else:
        remaining = parse_formatted_text(input)

    displayed = []
    skip_animation = False
    typing_delay = 0.05  # Aumentado de 0.03 para 0.05 para tornar a digitação um tico mais lenta

    while remaining:
        if not skip_animation:
            sleep(typing_delay)

        with term.cbreak():
            term_input = term.inkey(timeout=0.001)
            if term_input == " ":
                term_input = term.inkey(timeout=None)
            if term_input and term_input.name == "KEY_RIGHT":
                skip_animation = True
            if term_input and term_input.name == "KEY_LEFT":
                break

        if skip_animation:
            displayed.extend(remaining)
            remaining = []
        else:
            displayed.append(remaining.pop(0))

        current_char = displayed[-1]
        clean_char = sub(r"\033\[[0-9;]*m", "", current_char)

        if clean_char and any(c.isalnum() for c in clean_char):
            play_typing_sound(static=True if static else False, input=clean_char)
        elif static and input == " ":
            play_typing_sound()

        render_frame(displayed)

    total_wait_time = amount
    start_wait = current_time()
    enter_navigation = False

    if remaining:  # Interrompido por KEY_LEFT
        enter_navigation = True
        current_talk_index = len(seen_talks) - 2
        if current_talk_index < 0:
            current_talk_index = 0

    while (current_time() - start_wait < total_wait_time) or enter_navigation:
        with term.cbreak():
            timeout = None if enter_navigation else 0.05
            term_input = term.inkey(timeout=timeout)
            
            if term_input == " ":
                term_input = term.inkey(timeout=None)
                
            if term_input and term_input.name == "KEY_LEFT":
                enter_navigation = True
                if current_talk_index > 0:
                    current_talk_index -= 1
            elif term_input and term_input.name == "KEY_RIGHT":
                if enter_navigation:
                    if current_talk_index < len(seen_talks) - 1:
                        current_talk_index += 1
                    else:
                        enter_navigation = False
                        current_talk_index = len(seen_talks) - 1
                        render_frame(displayed)
                        break
                else:
                    break

        if enter_navigation:
            hist_input, hist_expr, hist_amount, hist_static, hist_color = seen_talks[current_talk_index]
            hist_displayed = [] if hist_input == " " else parse_formatted_text(hist_input)
            
            expr_list = expressions.get(hist_expr, expressions["idle"])
            current_expr = expr_list[
                len(hist_displayed) // (len(expr_list) // 2) % len(expr_list)
            ]
            displayed_text = ""
            if hist_displayed:
                if len(hist_displayed) == 1 and hist_displayed[0] == " ":
                    displayed_text = arrow_colorize(hist_static, hist_color)
                else:
                    displayed_text = "".join(arrow_colorize(hist_displayed, hist_color))
            else:
                displayed_text = arrow_colorize(hist_static, hist_color)
            static_text = arrow_colorize(hist_static, hist_color) if hist_displayed and hist_displayed[0] != " " else ""
            
            full_output = f"\n   {current_expr}  ──┤ {displayed_text} │  {static_text}"
            lines = full_output.split("\n")
            home_seq = term.home or "\033[1;1H"
            clear_eol_seq = term.clear_eol or "\033[K"
            clear_eos_seq = term.clear_eos or "\033[J"
            formatted_lines = [line + clear_eol_seq for line in lines]
            print(home_seq + "\n".join(formatted_lines) + clear_eos_seq, end="", flush=True)

    sleep(amount)


def wait_for_response(
    affirmative=["s", "y"],
    negative=["n"],
    static="",
    ask_template="",
    timeout=5,
    messages=None,
):
    """Espera pela resposta do usuário e processa de acordo."""
    if messages is None:
        messages = {
            "timeout": "Poxa, tá difícil assim de encontrar a tecla?",
            "invalid": "Oh! Digitou uma letra que eu não pedi! Presta atenção aí, pô!",
            "negative": "Não?",
            "retry": "Pera, deixa eu repetir",
            "positive": "Show de bola!",
        }

    while True:
        key = term.inkey(timeout=timeout)

        if key.lower() in affirmative:
            if key.lower() == "y":
                talk(
                    "Sim, inglês também tá valendo...",
                    "pokerface",
                    static=ask_template,
                )
            else:
                talk(messages["positive"], "open mouth", static=ask_template)
            return True

        elif key.lower() in negative:
            talk(messages["negative"], "pokerface", static=ask_template)
            talk(messages["retry"], static=ask_template)

        elif key.is_sequence:
            continue

        elif not key:
            talk(
                messages["timeout"],
                expression="thinking",
                amount=1.5,
                static=static + ask_template,
            )

        else:
            talk(
                messages["invalid"],
                "annoyed",
                static=static + ask_template,
            )


def main():
    list_model = "\n\n\n\n                        None × ‹[H]» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[T]» × None"
    swapped_edges = "\n\n\n\n                        None × ‹[T]» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None"
    ask_template = f"\n\n\n\n                    [{term.green}S{term.normal}im]     [{term.red}N{term.normal}ão]"
    affirmative = ["s", "y"]
    negative = ["n"]

    with term.fullscreen(), term.cbreak():
        talk("Oi, tudo bem?")
        talk("Muito obrigado por executar o meu script")
        talk(f"Eu me chamo {term.green}{term.bold}BOH!")
        talk("He He", amount=0.75)
        talk("Sabe...", amount=0.75)
        talk("Tipo,")
        talk(static=f"Tipo, {term.bold}ROH{term.normal}")
        talk(
            static=f"Tipo, {term.bold}ROH-{term.normal}{term.bold}{term.green}BOH{term.normal}"
        )

        for i in range(10):
            talk(static=f"{"HahA" * i}", expression="open mouth", amount=0.05)

        sleep(1)
        talk("Ai ai, sou meio comédia às vezes, sabe?")
        talk("Mas, enfim,")
        talk("E você, como se chama?", amount=0.5)

        print(
            term.move_yx(term.height - 3, 0) + "Digite seu nome aqui: ",
            end="",
            flush=True,
        )
        sike = []
        while len(sike) < 4:
            key_input = term.inkey(timeout=10)
            if not key_input.is_sequence and key_input.isalnum():
                sike.append(key_input)
                print(sike[-1], end="", flush=True)
            else:
                talk("Você sabe seu nome, né?", amount=0.5)
                print(
                    term.move_yx(term.height - 3, 0) + "Digite seu nome aqui: ",
                    end="",
                    flush=True,
                )

        talk(
            "Olha olha olha, na verdade, eu não tenho muito tempo...", "pokerface", 1.5
        )
        talk("Me desculpa! Você parece ser uma pessoa muito legal, mas...")
        talk("A pessoa que me mandou aqui, queria falar sobre ↓ isso ↓", amount=0.2)
        talk(
            expression="looking down",
            static="A pessoa que me mandou aqui, queria falar sobre ↓ isso ↓",
            amount=0.2,
        )

        print(list_model, flush=True)
        sleep(2)
        talk("Reconhece?", static=list_model, amount=1.5)
        talk("Ih, verdade, cê não consegue me responder, né?", static=list_model)
        talk("Hmmm", expression="thinking")

        for i in range(3):
            talk(static=f"Hmmm{"." * (i + 1)}", expression="thinking")

        talk("Já sei!", expression="open mouth")
        talk("Aqui, toma", static=ask_template, amount=1.5)

        # Primeira instrução sobre como responder
        talk("Agora sempre que eu te perguntar algo,", static=ask_template)
        talk("Você pode responder digitando", static=ask_template)
        talk("A letra destacada que achar mais cabível.", static=ask_template)
        talk("Entendeu, né?", static=ask_template)

        wait_for_response(
            affirmative=affirmative, negative=negative, ask_template=ask_template
        )

        talk("Enfim, voltando ao assunto...", expression="idle")
        talk("Reconhece isso aqui, né?", amount=0.1)
        talk(static="Reconhece isso aqui, né?", expression="looking down", amount=0.25)
        print(list_model, flush=True)
        print(ask_template, flush=True)

        # Verificando se o usuário reconhece a lista
        recognition_messages = {
            "timeout": "Poxa, tá difícil assim de encontrar a tecla?",
            "invalid": "Oh! Digitou uma letra que eu não pedi! Presta atenção aí, pô!",
            "negative": "Não?",
            "retry": "Como assim pô? Me esforcei tanto desenhar ela...",
        }

        while True:
            key = term.inkey(timeout=5)
            if key.lower() in affirmative:
                talk(
                    "Pois é, uma lista.",
                    static=list_model,
                )
                break
            elif key.lower() in negative:
                talk("Não?", "open mouth", static=list_model)
                talk(
                    recognition_messages["retry"],
                    expression="pokerface",
                    static=list_model,
                )
                talk("É uma lista! A estrutura de dados!", static=list_model)
                talk("Tá vendo?", static=f"{list_model}{ask_template}")
            elif key.is_sequence:
                continue
            elif not key:
                talk(
                    recognition_messages["timeout"],
                    expression="thinking",
                    amount=1.5,
                    static=f"{list_model}{ask_template}",
                )
            else:
                talk(
                    recognition_messages["invalid"],
                    "annoyed",
                    static=f"{list_model}{ask_template}",
                )

        # Explicações sobre a lista
        talk_sequence = [
            "Bom, como você já sabe... a lista é uma estrutura de dados",
            "Mas tô aqui pra discutir um desafio específico relacionado a ela...",
            "O desafio é o seguinte:",
            "Que tal inverter uma lista?",
            "Ou melhor, qual seria a maneira mais eficiente de fazer isso?",
            "Bom, a gente pode fazer isso de várias maneiras...",
            "Mas, acho que a primeira coisa que vem à cabeça é...",
        ]

        for text in talk_sequence[:-1]:
            talk(text, static=list_model)

        talk(talk_sequence[-1], static=list_model)

        # Lista com Head e Tail invertidos
        colored_list = f"\n\n\n\n                        None × ‹{term.red}[T]{term.normal}» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹{term.blue}[H]{term.normal}» × None"
        talk(
            "Fazer isso, né?",
            static=colored_list,
            amount=1.5,
        )

        # Explicações sobre inversão de lista
        swap_explanations = [
            'Trocando Head e Tail, o que era a "frente" da lista,',
            'Passa a ser o "final" dela, e vice-versa.',
            "Mas, pera aí! Como isso acontece exatamente?",
            f"Digamos que, a gente iguale {term.red}Tail \033[0ma {term.blue}Head",
        ]

        for explanation in swap_explanations:
            talk(explanation, static=swapped_edges)

        # Visualizações da lista após operações
        list_with_two_heads = f"\n\n\n\n                        None × ‹{term.blue}[H]{term.normal}» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹{term.blue}[H]{term.normal}» × None"

        talk(
            "Eita... agora temos duas Heads!",
            static=list_with_two_heads,
            amount=1.5,
        )

        # Explicação sobre a atribuição de variáveis
        assignment_explanations = [
            f"Isso porque {term.red}Tail \033[0m= {term.blue}Head \033[0mnão é uma troca de valores,",
            f"Só estamos dizendo que {term.red}Tail \033[0magora recebe",
            f"O objeto contido dentro de {term.blue}Head\033[0m.",
            f"Mas assim como abrir espaço numa estante pra guardar um livro,",
            f"Não significa que haverá espaço para guardar novamente",
            f"O antigo livro que tiramos para guardar o livro novo...",
            "O que significa que precisamos salvar",
            "O antigo valor de Tail, antes de trocá-lo por Head.",
        ]

        # Mostrando as explicações
        for i, explanation in enumerate(assignment_explanations):
            static_display = list_with_two_heads
            amount = 1.5 if i == 3 or i == 7 else 1.0
            talk(explanation, static=static_display, amount=amount)

        # Introduzindo o auxiliar
        aux_ascii = """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
"""

        # Sequência do AUX
        aux_sequence = [
            "Meu mano aqui se chama AUX,",
            "Tudo bem contigo, patrão?",
            "Ele se ofereceu pra guardar o valor de Tail",
            "Pra que a gente não perca na hora de trocar...",
        ]

        for i, text in enumerate(aux_sequence):
            if i == 2:
                aux_ascii = """
        __
(_|)   |@@|
 \ \__ \--/ __ 
  \o__|----|  |   __
      \ }{ /\ )_ / _\\
      /\__/\ \__O (\033[31m[T]\033[0m
      (--/\--)    \__/
      _)(  )(_
     `---''---`
"""
            elif i == 3:
                aux_ascii = """
         __
 _(\    |@@|
(__/\__ \--/ __
   \___|----|  |   __
       \ }{ /\ )_ / _\\
       /\__/\ \__O (\033[31m[T]\033[0m
      (--/\--)    \__/
      _)(  )(_
     `---''---`
"""
            amount = 1.25 if i == 0 else 0.75 if i == 1 else 1.0
            talk(text, static=aux_ascii, amount=amount)

        # Revisão do estado da lista com AUX
        aux_with_list = """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (\033[31m[T]\033[0m      None × ‹\033[34m[H]\033[0m» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹\033[34m[H]\033[0m» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
"""

        review_sequence = [
            "Revisitando então o estado da nossa lista",
            "Graças ao AUX, que guardou o valor de Tail",
            "Podemos facilmente colocar Tail onde o Head original está",
        ]

        for i, text in enumerate(review_sequence):
            if i == 2:
                aux_with_list = """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × ‹\033[31m[T]\033[0m» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹\033[34m[H]\033[0m» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
"""
            talk(text, static=aux_with_list)

        # Despedindo-se do AUX
        talk(
            "Obrigado AUX, você é o cara! Até mais tarde!",
            static="""
        __
(_|)   |@@|
 \ \__ \--/ __ 
  \o__|----|  |   __
      \ }{ /\ )_ / _\\
      /\__/\ \__O (__
     (--/\--)    \__/
     _)(  )(_
    `---''---`
""",
        )

        # Explicando sobre os ponteiros da lista
        pointer_explanations = [
            "Só que, isso não é o suficiente, né?",
            "Por causa desses caras aqui: ‹[]»",
            "Mais especificamente, ‹ » , esses dois.",
            "No nosso caso, eles representam",
            "Os ponteiros que identificam",
            "Quais elementos precedem e sucedem",
            "O objeto observado, seja lá qual você escolha.",
            "Até aí tudo bem, né?",
        ]

        for explanation in pointer_explanations:
            talk(explanation, static=swapped_edges)

        # Verificando entendimento
        talk(
            "Estamos na mesma página, então?",
            expression="thinking",
            static=swapped_edges,
            amount=0.2,
        )
        talk(
            static="Estamos na mesma página, então?",
            expression="thinking",
            amount=0.5,
        )
        print(swapped_edges, ask_template, flush=True)

        # Resposta do usuário sobre entendimento
        understanding_check = False
        while not understanding_check:
            key = term.inkey(timeout=10)
            if key.lower() in affirmative:
                understanding_check = True
            elif key.lower() in negative:
                # Explicações adicionais sobre a lista
                additional_explanations = [
                    "Bom, resumidamente, nesse conceito de lista,",
                    "Não usamos um conceito de índice,",
                    'Então a única forma de saber "aonde"',
                    "Cada objeto se encontra, é através desses ponteiros,",
                    "Pense que é como uma corrente.",
                    "Cada elo da corrente aponta para o próximo,",
                    "E cada um também sabe qual é o elo anterior.",
                ]

                for explanation in additional_explanations:
                    talk(explanation, static=swapped_edges)

                # Verificando novamente o entendimento
                confirmation = False
                while not confirmation:
                    talk("Agora fez mais sentido?", static=ask_template)
                    incepted = term.inkey(timeout=5)
                    if incepted.lower() in affirmative:
                        understanding_check = True
                        confirmation = True
                    elif incepted.lower() in negative:
                        # Oferecendo uma pausa
                        pause_sequence = [
                            "Tudo bem então, vamos fazer o seguinte...",
                            "Vôce tá precisando de um descanso,",
                            "Eu tô precisando de um descanso.",
                            "Vou dar uma pausa aqui, beleza?",
                            "Quando quiser continuar, é só teclar",
                        ]

                        for text in pause_sequence:
                            talk(text, static=ask_template)

                        print("\033[1;1H\033[0J", end="", flush=True)
                        print(
                            "──┤ Tô aqui pertinho, quando quiser continuar é só chamar! │"
                        )
                        term.inkey(timeout=None)
                        confirmation = True
                        understanding_check = True
                    elif incepted.is_sequence:
                        continue
                    elif not incepted:
                        talk(
                            "Muita falta de educação, ignorar os outros desse jeito!",
                            expression="annoyed",
                            static=ask_template,
                        )
                    else:
                        talk(
                            "Oh! Digitou uma letra que eu não pedi! Presta atenção aí, pô!",
                            "annoyed",
                            static=ask_template,
                        )
            elif key.is_sequence:
                continue
            elif not key:
                talk(
                    "Me deixa no vácuo assim não, poxa! ;-;",
                    expression="looking down",
                    amount=1.5,
                    static=ask_template,
                )
            else:
                talk(
                    "Oh! Digitou uma letra que eu não pedi! Presta atenção aí, pô!",
                    "annoyed",
                    static=ask_template,
                )

        # Continuando com a explicação
        talk("Então, vamos lá!", expression="open mouth", static=swapped_edges)

        # Explicações sobre as setinhas
        arrow_explanations = [
            "De modo geral, essas setinhas são tão importantes",
            "Pra esse exercício, que a gente vai precisar",
            "Deixar elas bem visíveis, pra não confundir.",
        ]

        for explanation in arrow_explanations:
            talk(explanation, static=swapped_edges)

        # Mostrando setas coloridas
        talk("Que tal...", expression="thinking", static=swapped_edges, amount=1.5)
        talk(static="Assim...", expression="thinking", amount=0.5)
        talk(
            static="Assim... ‹›«»",
            expression="thinking",
            colorize_arrows=True,
        )

        # Explicando as setas coloridas
        colored_arrow_explanations = [
            "Melhorou, né?",
            "As setas simples, ou seja, ‹ & › , destacadas em laranja,",
            "Representam a variável do nosso objeto que",
            "Nos mostra qual é o elemento que o precede",
            "Já as setas duplas, ou seja, » & « , destacadas em azul,",
            "Representam a variável do nosso objeto que",
            "Nos mostra qual é o elemento que o sucede",
        ]

        for i, explanation in enumerate(colored_arrow_explanations):
            amount = 1.5 if i == 6 else 1.0
            talk(explanation, static=swapped_edges, colorize_arrows=True, amount=amount)

        # Explicando a necessidade de inverter as setas
        inversion_explanations = [
            "Seguindo essa lógica,",
            "Acho que deu pra perceber que a gente",
            "Também vai precisar inverter essas setinhas",
            "Pra inverter a lista, certo?",
            "Já que, simplesmente trocar Head e Tail",
            "Não trocou as setinhas, de cada elemento.",
            "Então é como se olhássemos para trás,",
            "Mas continuássemos andando para frente.",
        ]

        for i, explanation in enumerate(inversion_explanations):
            amount = 1.5 if i == 3 else 1.0
            talk(explanation, static=swapped_edges, colorize_arrows=True, amount=amount)

        # Verificando se pode continuar
        talk(
            "Até aqui tudo bem? Posso continuar?",
            static=swapped_edges,
            colorize_arrows=True,
        )
        talk(static="Até aqui tudo bem? Posso continuar?", amount=0.2)
        print(ask_template, flush=True)

        # Verificando resposta do usuário
        while True:
            key = term.inkey(timeout=10)
            if key.lower() in affirmative:
                break
            elif key.lower() in negative:
                pause_sequence = [
                    "Tudo bem então, vamos fazer o seguinte...",
                    "Vôce tá precisando de um descanso,",
                    "Eu tô precisando de um descanso.",
                    "Vou dar uma pausa aqui, beleza?",
                    "Quando quiser continuar, é só teclar",
                ]

                for text in pause_sequence:
                    talk(text, static=ask_template, colorize_arrows=True)

                print("\033[1;1H\033[0J", end="", flush=True)
                print("──┤ Tô aqui pertinho, quando quiser continuar é só chamar! │")
                term.inkey(timeout=None)
                break
            elif key.is_sequence:
                continue
            elif not key:
                talk(
                    "Muita falta de educação, ignorar os outros desse jeito!",
                    expression="annoyed",
                    static=ask_template,
                    colorize_arrows=True,
                )
            else:
                talk(
                    "Oh! Digitou uma letra que eu não pedi! Presta atenção aí, pô!",
                    "annoyed",
                    static=ask_template,
                    colorize_arrows=True,
                )

        # Continuando com a explicação
        talk(
            "Então, vamos lá!",
            expression="open mouth",
            static=swapped_edges,
            colorize_arrows=True,
        )

        # Explicações sobre as direções das setas
        direction_explanations = [
            "Se formos então focar particularmente",
            "Na posição que a Tail ocupa agora,",
        ]

        for explanation in direction_explanations:
            talk(explanation, static=swapped_edges, colorize_arrows=True)

        # Lista com Tail destacada
        list_with_highlighted_tail = f"\n\n\n\n                        None × ‹{term.red}[T]{term.normal}» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None"

        # Explicações sobre como inverter as setas
        arrow_inversion_explanations = [
            "Ora, se as setas são direções,",
            "Podemos simplemente invertê-las, certo?",
            "Assim como fizemos antes entre Tail e Head.",
            "Ou seja...",
            f"{term.bold}AUX!",
            "Chega aí, meu querido!",
        ]

        for i, explanation in enumerate(arrow_inversion_explanations):
            expression = "thinking" if i == 1 else "idle"
            talk(
                explanation,
                static=list_with_highlighted_tail,
                colorize_arrows=True,
                expression=expression,
            )

        # Sequência com AUX manipulando as setas
        aux_sequences = [
            (
                "Segura aqui pá nóis, fazendo favô.",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (‹_        None × \033[31m[T]\033[0m» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Agora que Aux tem o valor de anterior,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (‹_        None × \033[31m[T]\033[0m» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "A gente coloca o valor de próximo no lugar de anterior,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (‹_        None × «\033[31m[T]\033[0m» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "E o valor do Aux, no lugar de próximo. Mas...",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
        ]

        for i, (text, ascii_art) in enumerate(aux_sequences):
            amount = 1.5 if i == 3 else 1.0
            talk(text, static=ascii_art, colorize_arrows=True, amount=amount)

        # Explicação sobre inversão de direções
        direction_questions = [
            (
                "Se a gente inverteu anterior e próximo,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Como mudar os valores subsequentes?",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "O que é frente e o que é trás?",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Bom, tudo depende se estamos começando de Head ou Tail.",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
        ]

        for i, (text, ascii_art) in enumerate(direction_questions):
            amount = 1.5 if i == 3 else 1.0
            talk(text, static=ascii_art, colorize_arrows=True, amount=amount)

        # Explicações sobre percorrer a lista invertida
        traversal_explanations = [
            (
                "Como estamos começando de Tail, estamos no final.",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Para alterar todos os demais valores,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Só precisamos ir até cada elemento que nos antecede,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «\033[31m[T]\033[0m› ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
        ]

        for text, ascii_art in traversal_explanations:
            talk(text, static=ascii_art, colorize_arrows=True)

        # Animação de percorrer e inverter cada elemento
        node_traversal = [
            (
                "Que da nossa perspectiva atual, seria esse aqui:",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «[T]› ‹\033[31m[]\033[0m» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "E ao invertermos as setinhas desse elemento também,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (‹_        None × «[T]› «\033[31m[]\033[0m» ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Vemos que se simplesmente continuarmos indo em direção",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «[T]› «\033[31m[]\033[0m› ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "À atual direção do elemento anterior,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «[T]› «\033[31m[]\033[0m› ‹[]» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Em algum momento...",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «[T]› «[]› ‹\033[31m[]\033[0m» ... ‹[]» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Nós estaremos de cara com a outra ponta da lista,",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «[T]› «[]› «[]› ... ‹\033[31m[]\033[0m» ‹[]» ‹[H]» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "E ao perceber que não há mais elementos",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (‹_        None × «[T]› «[]› «[]› ... «[]› «[]› «\033[31m[H]\033[0m» × None
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Tcharam! Invertemos a lista com sucesso! :D",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__        None × «[T]› «[]› «[]› ... «[]› «[]› «[H]› \033[31m× \033[38;5;93mNone\033[0m
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
        ]

        for text, ascii_art in node_traversal:
            talk(
                text, expression="looking down", static=ascii_art, colorize_arrows=True
            )

        # Conclusão
        conclusion = [
            (
                "E era isso que eu e AUX tínhamos pra te mostrar hoje!",
                """
       __
   _  |@@|
  / \ \--/ __
  ) O|----|  |   __
 / / \ }{ /\ )_ / _\\
 )/  /\__/\ \__O (__
|/  (--/\--)    \__/
/   _)(  )(_
   `---''---`
""",
            ),
            (
                "Espero que tenhamos conseguido te ajudar!",
                """
         __
 _(\    |@@|
(__/\__ \--/ __
   \___|----|  |   __
       \ }{ /\ )_ / _\\
       /\__/\ \__O (\033[31m[T]\033[0m
      (--/\--)    \__/
      _)(  )(_
     `---''---`
""",
            ),
            (
                "A gente se vê na próxima, beleza?",
                """
        __
(_|)   |@@|
 \ \__ \--/ __ 
  \o__|----|  |   __
      \ }{ /\ )_ / _\\
      /\__/\ \__O (__
     (--/\--)    \__/
     _)(  )(_
    `---''---`
""",
            ),
        ]

        for i, (text, ascii_art) in enumerate(conclusion):
            expression = (
                "looking down" if i == 0 else "open mouth" if i == 2 else "idle"
            )
            talk(
                text, expression=expression, static=ascii_art, colorize_arrows=(i == 0)
            )

        # Arte final
        print(
            "\033[1;1H\033[0J",
            end="",
            flush=True,
        )
        print(
            """
(„• ֊ •„)੭      ⠀⠀⠀⢀⡴⠟⠛⢷⡄⠀⣠⠞⠋⠉⠳⡄⠀⠀⠀⠀
                ⠀⠀⠀⣸⠁⠀⠀⠈⣧⢰⠇⠀⠀⠀⢠⡇⠀⠀⠀⠀
                ⠀⠀⠀⠸⣆⠀⠀⠀⠘⣿⠀⠀⠀⠀⡞⠀⠀⠀⠀⠀
                ⠀⠀⠀⠀⠹⣦⠀⠀⠀⠘⡄⠀⠀⠀⡇⠀⠀⠀⠀⠀
                ⠀⠀⠀⡴⠚⠙⠳⣀⡴⠂⠁⠒⢄⠀⢿⡀⠀⠀⠀⠀
                ⠀⠀⢸⡇⠀⢀⠔⠉⠀⠀⠀⡀⠀⠂⠘⣇⠀⠀⠀⠀
                ⠀⠀⠀⢳⡀⠘⢄⣀⣀⠠⠶⠄⠀⠀⠀⡿⠀⠀⠀⠀
                ⠀⠀⠀⠀⠻⣕⠂⠁⠀⠀⠀⠀⠀⠀⣰⡇⠀⠀⠀⠀
                ⠀⠀⠀⠀⠀⠹⣅⠒⠀⠒⠂⠐⠒⢉⡼⢁⣤⠀⠀⠀
                ⠀⢀⣼⣻⣆⣤⢈⣙⣒⠶⠶⣶⣞⢿⣸⡟⠳⣾⣂⣤
                ⠸⢿⣽⠏⢠⡿⠋⣿⣭⢁⣈⣿⣽⣆⠙⢷⣄⠙⠋⠁
                ⠀⠀⠀⠀⠛⠃⠰⠿⣤⠄⠀⠸⠷⠟⠀⠀⠁⠀⠀⠀
"""
        )

        if term.inkey(timeout=None):
            return


if __name__ == "__main__":
    main()

"""Setinhas: «»‹›"""
