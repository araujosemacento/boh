<script lang="ts">
	import { ArrowLeft, Save, Download, Upload, Play, Square, Plus, Trash2, Zap } from '@lucide/svelte';
	import type { Project, DialogueNode } from '../types';

	let { project = $bindable(), onBack }: { project: Project; onBack: () => void } = $props();

	// 1. Estados Reativos Principais (Svelte 5 Runes)
	let nodes = $state<Record<string, DialogueNode>>({ ...project.nodes });
	let pan = $state({ x: 0, y: 0 });
	let isPanning = $state(false);
	let panStart = { x: 0, y: 0 };

	// Estados de Arraste de Nó
	let draggedNodeId = $state<string | null>(null);
	let dragStartOffset = { x: 0, y: 0 };

	// Estados de Criação de Conexão
	let connectingFromId = $state<string | null>(null);
	let mouseX = $state(0);
	let mouseY = $state(0);
	let canvasElement = $state<HTMLDivElement | null>(null);

	// Estados do Player de Terminal
	let isPlaying = $state(false);
	let outputLines = $state<string[]>([]);
	let currentExpression = $state<'idle' | 'pokerface' | 'thinking' | 'open mouth' | 'annoyed' | 'looking down'>('idle');
	let activePlayNodeId = $state<string | null>(null);

	// Auxiliar de Timeout
	const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));
	const choice = <T>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)];

	// 2. Lógica do Pan (Arraste de Fundo do Canvas)
	const handleMouseDownBg = (e: MouseEvent) => {
		// Panning ativa com botão esquerdo no fundo ou botão do meio
		if (e.button === 0 || e.button === 1) {
			isPanning = true;
			panStart = { x: e.clientX - pan.x, y: e.clientY - pan.y };
		}
	};

	const handleMouseMoveBg = (e: MouseEvent) => {
		if (isPanning) {
			pan = { x: e.clientX - panStart.x, y: e.clientY - panStart.y };
		}

		if (draggedNodeId) {
			nodes[draggedNodeId].x = e.clientX - dragStartOffset.x;
			nodes[draggedNodeId].y = e.clientY - dragStartOffset.y;
		}

		if (connectingFromId && canvasElement) {
			const rect = canvasElement.getBoundingClientRect();
			mouseX = e.clientX - rect.left - pan.x;
			mouseY = e.clientY - rect.top - pan.y;
		}
	};

	const handleMouseUpBg = () => {
		isPanning = false;
		draggedNodeId = null;

		if (connectingFromId && canvasElement) {
			// Verifica colisão com nó de entrada
			const targetNode = Object.values(nodes).find(
				(n) => n.type === 'boh' && Math.hypot(mouseX - n.x, mouseY - (n.y + 80)) < 30
			);

			if (targetNode && targetNode.id !== connectingFromId) {
				nodes[connectingFromId].nextId = targetNode.id;
			}

			connectingFromId = null;
		}
	};

	// 3. Lógica de Drag de Nós
	const handleMouseDownNode = (e: MouseEvent, id: string) => {
		e.stopPropagation();
		draggedNodeId = id;
		dragStartOffset = { x: e.clientX - nodes[id].x, y: e.clientY - nodes[id].y };
	};

	// 4. Criação de Conexão (Click no Output Port)
	const handleStartConnection = (e: MouseEvent, id: string) => {
		e.stopPropagation();
		connectingFromId = id;

		if (canvasElement) {
			const rect = canvasElement.getBoundingClientRect();
			mouseX = e.clientX - rect.left - pan.x;
			mouseY = e.clientY - rect.top - pan.y;
		}
	};

	// 5. Adicionar e Deletar Nós
	const addBohNode = () => {
		const id = `node-${Date.now()}`;
		// Spawna o nó deslocado em relação ao pan atual para que apareça no centro visual
		nodes[id] = {
			id,
			type: 'boh',
			x: -pan.x + 150 + Math.random() * 50,
			y: -pan.y + 150 + Math.random() * 50,
			expression: 'idle',
			text: 'Olá! Escreva o diálogo aqui.'
		};
	};

	const deleteNode = (id: string) => {
		if (id === 'start') return; // Start não pode ser apagado
		
		// Remove referências a este nó nas conexões
		Object.keys(nodes).forEach((key) => {
			if (nodes[key].nextId === id) {
				nodes[key].nextId = undefined;
			}
		});

		delete nodes[id];
	};

	// Remove conexão específica ao clicar na linha
	const removeConnection = (fromId: string) => {
		nodes[fromId].nextId = undefined;
	};

	// 6. Engine do Player de Diálogo
	const playTypingSound = () => {
		try {
			const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
			const osc = audioCtx.createOscillator();
			const gain = audioCtx.createGain();
			osc.connect(gain);
			gain.connect(audioCtx.destination);

			osc.type = 'sine';
			osc.frequency.setValueAtTime(choice([180, 210, 240, 270]), audioCtx.currentTime);

			gain.gain.setValueAtTime(0.012, audioCtx.currentTime);
			gain.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + 0.04);

			osc.start();
			osc.stop(audioCtx.currentTime + 0.04);

			setTimeout(() => audioCtx.close(), 100);
		} catch (e) {
			// Fallback silenciável
		}
	};

	const startPlaying = async () => {
		if (isPlaying) return;
		isPlaying = true;
		outputLines = ['[Iniciando emulador do Boh...]'];
		currentExpression = 'idle';

		const startNode = nodes['start'];
		if (!startNode || !startNode.nextId) {
			outputLines = [...outputLines, 'Erro: Conecte o nó inicial a uma fala do Boh!'];
			isPlaying = false;
			return;
		}

		let currentNodeId: string | undefined = startNode.nextId;
		await sleep(600);

		while (currentNodeId && isPlaying) {
			activePlayNodeId = currentNodeId;
			const node = nodes[currentNodeId];
			if (!node) break;

			currentExpression = node.expression;
			outputLines = [...outputLines, ''];
			const lineIndex = outputLines.length - 1;
			const textToType = node.text;

			for (let i = 0; i < textToType.length; i++) {
				if (!isPlaying) break;
				outputLines[lineIndex] += textToType[i];
				playTypingSound();
				await sleep(40); // 40ms por caractere
			}

			if (isPlaying) {
				currentExpression = 'idle';
				await sleep(1200); // 1.2s de pausa entre as falas
			}

			currentNodeId = node.nextId;
		}

		activePlayNodeId = null;
		isPlaying = false;
		currentExpression = 'idle';
		outputLines = [...outputLines, '', '[Execução finalizada.]'];
	};

	const stopPlaying = () => {
		isPlaying = false;
		activePlayNodeId = null;
		currentExpression = 'idle';
	};

	// 7. Persistência e Exportação
	const saveProject = () => {
		project.nodes = { ...nodes };
		const savedProjects = JSON.parse(localStorage.getItem('saved-projects') || '[]');
		const index = savedProjects.findIndex((p: any) => p.id === project.id);
		
		const updatedProject = {
			...project,
			nodes: { ...nodes }
		};

		if (index >= 0) {
			savedProjects[index] = updatedProject;
		} else {
			savedProjects.push(updatedProject);
		}

		localStorage.setItem('saved-projects', JSON.stringify(savedProjects));
		alert('Projeto salvo localmente com sucesso!');
	};

	const exportJson = () => {
		const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify({
			...project,
			nodes
		}, null, 2));
		const downloadAnchor = document.createElement('a');
		downloadAnchor.setAttribute('href', dataStr);
		downloadAnchor.setAttribute('download', `${project.name.toLowerCase()}_dialogue.json`);
		document.body.appendChild(downloadAnchor);
		downloadAnchor.click();
		downloadAnchor.remove();
	};

	const triggerImportJson = () => {
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.json';
		input.onchange = (e) => {
			const file = (e.target as HTMLInputElement).files?.[0];
			if (!file) return;

			const reader = new FileReader();
			reader.onload = (readerEvent) => {
				try {
					const parsed = JSON.parse(readerEvent.target?.result as string);
					if (parsed && parsed.nodes) {
						nodes = parsed.nodes;
						alert('Grafo de diálogo importado com sucesso!');
					} else {
						alert('Arquivo JSON inválido.');
					}
				} catch (err) {
					alert('Erro ao processar o arquivo JSON.');
				}
			};
			reader.readAsText(file);
		};
		input.click();
	};

	// 8. Renderizador de Avatar ASCII
	const getBohAvatar = (expr: string) => {
		switch (expr) {
			case 'pokerface':
				return `
 [ BOH: POKERFACE ]
   +----------+
   |   -  -   |
   |   ____   |
   +----------+`;
			case 'thinking':
				return `
 [ BOH: PENSANDO ]
   +----------+
   |   o  ?   |
   |   ....   |
   +----------+`;
			case 'open mouth':
				return `
 [ BOH: FALANDO ]
   +----------+
   |   O  O   |
   |   |  |   |
   +----------+`;
			case 'annoyed':
				return `
 [ BOH: IRRITADO ]
   +----------+
   |   >  <   |
   |   \\__/   |
   +----------+`;
			case 'looking down':
				return `
 [ BOH: DESANIMADO ]
   +----------+
   |   u  u   |
   |   ____   |
   +----------+`;
			case 'idle':
			default:
				return `
 [ BOH: IDLE ]
   +----------+
   |   o  o   |
   |   ____   |
   +----------+`;
		}
	};
</script>

<div class="h-screen flex flex-col bg-base-300 select-none overflow-hidden text-base-content">
	<!-- Barra Superior -->
	<header class="h-14 bg-base-100 border-b border-base-200 px-4 flex items-center justify-between z-10 shrink-0 shadow-sm">
		<div class="flex items-center gap-3">
			<button onclick={onBack} class="btn btn-ghost btn-sm gap-2">
				<ArrowLeft class="size-4" />
				Voltar
			</button>
			<div class="divider divider-horizontal my-3"></div>
			<div class="flex items-center gap-2">
				<span class="font-bold text-lg">{project.name}</span>
				<span class="badge badge-primary badge-sm font-semibold">{project.tag}</span>
			</div>
		</div>

		<div class="flex items-center gap-2">
			<button onclick={triggerImportJson} class="btn btn-sm btn-ghost gap-1.5 font-semibold">
				<Upload class="size-4" />
				Importar JSON
			</button>
			<button onclick={exportJson} class="btn btn-sm btn-ghost gap-1.5 font-semibold">
				<Download class="size-4" />
				Exportar JSON
			</button>
			<button onclick={saveProject} class="btn btn-sm btn-primary gap-1.5 font-bold shadow-lg shadow-primary/10">
				<Save class="size-4" />
				Salvar
			</button>
		</div>
	</header>

	<!-- Área de Trabalho de 3 Colunas -->
	<div class="flex-1 flex overflow-hidden">
		<!-- Coluna Esquerda: Paleta -->
		<aside class="w-60 bg-base-100 border-r border-base-200 p-4 flex flex-col gap-4 select-none shrink-0">
			<div>
				<h3 class="text-sm font-bold tracking-wider text-base-content/60 uppercase">Paleta de Nós</h3>
				<p class="text-xs text-base-content/50 mt-1">Clique para adicionar nós no canvas</p>
			</div>

			<button onclick={addBohNode} class="btn btn-outline btn-primary btn-block gap-2 text-sm justify-start font-semibold">
				<Plus class="size-4" />
				+ Fala do Boh
			</button>
		</aside>

		<!-- Coluna Central: Canvas Bidimensional -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<main
			bind:this={canvasElement}
			onmousedown={handleMouseDownBg}
			onmousemove={handleMouseMoveBg}
			onmouseup={handleMouseUpBg}
			class="flex-1 relative overflow-hidden bg-base-300 canvas-grid cursor-grab active:cursor-grabbing"
		>
			<!-- Container Movível do Canvas (Translação via GPU) -->
			<div class="absolute inset-0 origin-top-left" style="transform: translate3d({pan.x}px, {pan.y}px, 0)">
				
				<!-- SVG Overlay para Renderizar Conexões (Splines) -->
				<svg class="absolute inset-0 pointer-events-none w-[5000px] h-[5000px]">
					<!-- Definições para Marcadores de Setas -->
					<defs>
						<marker id="arrow" viewBox="0 0 10 10" refX="6" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
							<path d="M 0 1.5 L 8 5 L 0 8.5 z" class="fill-base-content/40" />
						</marker>
					</defs>

					<!-- Conexões Existentes -->
					{#each Object.values(nodes) as node}
						{#if node.nextId && nodes[node.nextId]}
							{@const target = nodes[node.nextId]}
							{@const startX = node.type === 'start' ? node.x + 128 : node.x + 288}
							{@const startY = node.type === 'start' ? node.y + 32 : node.y + 80}
							{@const endX = target.x}
							{@const endY = target.y + 80}
							{@const offset = Math.abs(endX - startX) * 0.45}
							
							<!-- Caminho Interativo da Spline (Bézier Cúbica) -->
							<g class="pointer-events-auto group cursor-pointer" onclick={() => removeConnection(node.id)}>
								<!-- Hitbox mais larga para facilitar clique -->
								<path
									d="M {startX} {startY} C {startX + offset} {startY}, {endX - offset} {endY}, {endX} {endY}"
									fill="none"
									stroke="transparent"
									stroke-width="12"
								/>
								<path
									d="M {startX} {startY} C {startX + offset} {startY}, {endX - offset} {endY}, {endX} {endY}"
									fill="none"
									class="stroke-base-content/30 group-hover:stroke-error transition-colors duration-200"
									stroke-width="3.5"
									marker-end="url(#arrow)"
								/>
							</g>
						{/if}
					{/each}

					<!-- Conexão Sendo Arrasta no Momento -->
					{#if connectingFromId}
						{@const source = nodes[connectingFromId]}
						{@const startX = source.type === 'start' ? source.x + 128 : source.x + 288}
						{@const startY = source.type === 'start' ? source.y + 32 : source.y + 80}
						{@const offset = Math.abs(mouseX - startX) * 0.45}
						<path
							d="M {startX} {startY} C {startX + offset} {startY}, {mouseX - offset} {mouseY}, {mouseX} {mouseY}"
							fill="none"
							class="stroke-primary/70"
							stroke-dasharray="4,4"
							stroke-width="3"
						/>
					{/if}
				</svg>

				<!-- Renderização dos Nós -->
				{#each Object.values(nodes) as node (node.id)}
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div
						class="absolute bg-base-100 border border-base-200 shadow-md select-none transition-shadow flex flex-col group/node {activePlayNodeId === node.id ? 'border-primary shadow-lg ring-2 ring-primary/20' : ''}"
						class:w-32={node.type === 'start'}
						class:h-16={node.type === 'start'}
						class:rounded-md={node.type === 'start'}
						class:w-72={node.type === 'boh'}
						class:rounded-lg={node.type === 'boh'}
						style="left: {node.x}px; top: {node.y}px;"
					>
						{#if node.type === 'start'}
							<!-- Nó Inicial -->
							<div
								onmousedown={(e) => handleMouseDownNode(e, node.id)}
								class="flex-1 flex items-center justify-between px-3 py-2 bg-success/10 text-success rounded-md cursor-grab active:cursor-grabbing font-bold text-sm"
							>
								<span class="flex items-center gap-1"><Zap class="size-4" /> Start</span>
								<!-- Portas de Saída -->
								<button
									onmousedown={(e) => handleStartConnection(e, node.id)}
									class="w-4 h-4 rounded-full bg-success border-2 border-base-100 hover:scale-125 transition-transform translate-x-5 cursor-crosshair"
									title="Conectar"
									aria-label="Conectar nó de início"
								></button>
							</div>
						{:else}
							<!-- Nó Boh Dialogue -->
							<!-- Cabeçalho do Nó -->
							<div
								onmousedown={(e) => handleMouseDownNode(e, node.id)}
								class="px-3 py-2 bg-base-200 border-b border-base-200 rounded-t-lg flex items-center justify-between cursor-grab active:cursor-grabbing select-none"
							>
								<!-- Portas de Entrada -->
								<div
									class="w-4 h-4 rounded-full bg-primary border-2 border-base-100 -translate-x-5"
									title="Entrada"
								></div>

								<span class="font-bold text-xs text-base-content/70 uppercase tracking-wide">Diálogo do Boh</span>
								
								<div class="flex items-center gap-1.5">
									<button
										onclick={() => deleteNode(node.id)}
										class="text-base-content/40 hover:text-error transition-colors p-0.5 rounded"
										title="Excluir Nó"
									>
										<Trash2 class="size-4" />
									</button>
									<!-- Portas de Saída -->
									<button
										onmousedown={(e) => handleStartConnection(e, node.id)}
										class="w-4 h-4 rounded-full bg-primary border-2 border-base-100 translate-x-5 hover:scale-125 transition-transform cursor-crosshair"
										title="Conectar"
										aria-label="Conectar saída do diálogo"
									></button>
								</div>
							</div>

							<!-- Corpo do Nó -->
							<div class="p-3 flex flex-col gap-3">
								<div class="form-control">
									<label class="label py-1" for="expr-{node.id}">
										<span class="label-text text-[11px] font-bold text-base-content/60">Expressão do Boh</span>
									</label>
									<select
										id="expr-{node.id}"
										bind:value={node.expression}
										class="select select-bordered select-xs w-full bg-base-200 font-semibold text-xs rounded-md"
									>
										<option value="idle">Idle (Padrão)</option>
										<option value="pokerface">Pokerface</option>
										<option value="thinking">Pensando</option>
										<option value="open mouth">Falando</option>
										<option value="annoyed">Irritado</option>
										<option value="looking down">Desanimado</option>
									</select>
								</div>

								<div class="form-control">
									<label class="label py-1" for="text-{node.id}">
										<span class="label-text text-[11px] font-bold text-base-content/60">Texto da Fala</span>
									</label>
									<textarea
										id="text-{node.id}"
										bind:value={node.text}
										class="textarea textarea-bordered text-xs leading-relaxed font-medium bg-base-200 rounded-md h-20 resize-none placeholder:text-base-content/45"
										placeholder="Escreva a resposta do Boh aqui..."
									></textarea>
								</div>
							</div>
						{/if}
					</div>
				{/each}
			</div>
		</main>

		<!-- Coluna Direita: Player de Terminal macOS -->
		<aside class="w-96 bg-base-100 border-l border-base-200 flex flex-col z-10 shrink-0 select-none">
			<!-- Painel de Título -->
			<div class="p-4 border-b border-base-200 flex items-center justify-between shrink-0">
				<div>
					<h3 class="text-sm font-bold tracking-wider text-base-content/60 uppercase">Simulação</h3>
					<p class="text-xs text-base-content/50 mt-1">Reproduza e teste o diálogo ativo</p>
				</div>
				<div class="flex items-center gap-1.5">
					{#if isPlaying}
						<button onclick={stopPlaying} class="btn btn-sm btn-error gap-1 px-3.5 font-bold shadow-lg shadow-error/10">
							<Square class="size-4 fill-current" /> Parar
						</button>
					{:else}
						<button onclick={startPlaying} class="btn btn-sm btn-success gap-1 px-3.5 text-base-100 font-bold shadow-lg shadow-success/15">
							<Play class="size-4 fill-current" /> Play
						</button>
					{/if}
				</div>
			</div>

			<!-- Terminal Mockup macOS -->
			<div class="flex-1 p-4 flex flex-col min-h-0 bg-base-200/30">
				<div class="flex-1 bg-neutral text-neutral-content rounded-lg border border-neutral-800 flex flex-col min-h-0 shadow-xl overflow-hidden">
					
					<!-- macOS Window Title Bar -->
					<div class="bg-base-200 px-4 py-3 flex items-center gap-2 select-none relative border-b border-neutral-900 shrink-0">
						<div class="flex items-center gap-1.5 z-10">
							<div class="w-3 h-3 rounded-full bg-error opacity-80"></div>
							<div class="w-3 h-3 rounded-full bg-warning opacity-80"></div>
							<div class="w-3 h-3 rounded-full bg-success opacity-80"></div>
						</div>
						<div class="w-full text-center text-[10px] tracking-wide font-mono text-base-content/50 uppercase font-bold absolute left-0 pr-4">
							Boh.py Terminal
						</div>
					</div>

					<!-- Terminal Screen Body -->
					<div class="p-4 flex-1 font-mono text-xs flex flex-col gap-4 overflow-y-auto min-h-0 scrollbar-thin select-text">
						
						<!-- Boh ASCII Art Character -->
						<pre class="text-primary font-bold text-[11px] leading-tight select-none shrink-0 bg-primary/5 py-2.5 px-3 border border-primary/10 rounded-md">
{getBohAvatar(currentExpression)}
						</pre>

						<!-- Output Terminal Log -->
						<div class="flex-1 flex flex-col gap-2 leading-relaxed">
							{#each outputLines as line, i}
								{#if line === ''}
									<br />
								{:else if line.startsWith('[')}
									<div class="text-primary/70 text-[11px] font-semibold">{line}</div>
								{:else if line.startsWith('Erro:')}
									<div class="text-error font-bold">{line}</div>
								{:else}
									<div class="text-neutral-content/90 font-medium whitespace-pre-wrap"><span class="text-success font-bold mr-1.5">&gt;</span>{line}</div>
								{/if}
							{/each}
						</div>
					</div>
				</div>
			</div>
		</aside>
	</div>
</div>

<style>
	.canvas-grid {
		background-size: 24px 24px;
		background-image: 
			linear-gradient(to right, rgba(0, 0, 0, 0.05) 1px, transparent 1px),
			linear-gradient(to bottom, rgba(0, 0, 0, 0.05) 1px, transparent 1px);
	}
	:global([data-theme="mocha"]) .canvas-grid,
	:global([data-theme="macchiato"]) .canvas-grid,
	:global([data-theme="frappe"]) .canvas-grid {
		background-image: 
			linear-gradient(to right, rgba(255, 255, 255, 0.03) 1px, transparent 1px),
			linear-gradient(to bottom, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
	}
</style>
