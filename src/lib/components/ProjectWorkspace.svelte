<script lang="ts">
	import {
		ArrowLeft,
		Save,
		Download,
		Upload,
		Play,
		Square,
		Plus,
		Trash2,
		Zap
	} from '@lucide/svelte';
	import type { Project, DialogueNode } from '../types';
	import DialoguePlayer from './DialoguePlayer.svelte';

	let { project = $bindable(), onBack }: { project: Project; onBack: () => void } = $props();

	// ── Estados Reativos Principais ──
	let nodes = $state<Record<string, DialogueNode>>({ ...project.nodes });
	let pan = $state({ x: 0, y: 0 });
	let isPanning = $state(false);
	let panStart = { x: 0, y: 0 };

	// Arraste de Nó
	let draggedNodeId = $state<string | null>(null);
	let dragStartOffset = { x: 0, y: 0 };

	// Criação de Conexão (drag de porta)
	let connectingFromId = $state<string | null>(null);
	let mouseX = $state(0);
	let mouseY = $state(0);
	let canvasElement = $state<HTMLDivElement | null>(null);

	// Redimensionamento de Colunas
	let paletteWidth = $state(240);
	let terminalWidth = $state(480);
	let resizingColumn = $state<'palette' | 'terminal' | null>(null);
	let resizeStartX = 0;
	let resizeStartWidth = 0;

	// Player de Terminal
	let activePlayNodeId = $state<string | null>(null);
	let selectedNodeId = $state<string | null>(null);

	// ── Derived: quais nós possuem conexão de entrada ──
	const connectedInputIds = $derived(
		new Set(
			Object.values(nodes)
				.map((n) => n.nextId)
				.filter(Boolean)
		)
	);
	const connectedOutputIds = $derived(
		new Set(
			Object.values(nodes)
				.filter((n) => n.nextId)
				.map((n) => n.id)
		)
	);

	// ── Coordenadas das Portas (relativas ao nó) ──
	const getOutputPortPos = (node: DialogueNode) => {
		if (node.type === 'start') return { x: node.x + 128, y: node.y + 32 };
		return { x: node.x + 288, y: node.y + 18 };
	};
	const getInputPortPos = (node: DialogueNode) => {
		return { x: node.x, y: node.y + 18 };
	};

	// ── Pan do Canvas ──
	const handleCanvasMouseDown = (e: MouseEvent) => {
		if (e.button === 0 || e.button === 1) {
			isPanning = true;
			panStart = { x: e.clientX - pan.x, y: e.clientY - pan.y };
		}
	};

	const handleGlobalMouseMove = (e: MouseEvent) => {
		// Resize de colunas
		if (resizingColumn) {
			const delta = e.clientX - resizeStartX;
			if (resizingColumn === 'palette') {
				paletteWidth = Math.max(180, Math.min(400, resizeStartWidth + delta));
			} else {
				terminalWidth = Math.max(320, Math.min(700, resizeStartWidth - delta));
			}
			return;
		}

		if (isPanning) {
			pan = { x: e.clientX - panStart.x, y: e.clientY - panStart.y };
		}

		if (draggedNodeId) {
			const node = nodes[draggedNodeId];
			if (node) {
				node.x = e.clientX - dragStartOffset.x;
				node.y = e.clientY - dragStartOffset.y;
			}
		}

		if (connectingFromId && canvasElement) {
			const rect = canvasElement.getBoundingClientRect();
			mouseX = e.clientX - rect.left - pan.x;
			mouseY = e.clientY - rect.top - pan.y;
		}
	};

	const handleGlobalMouseUp = (e: MouseEvent) => {
		// Finaliza resize
		if (resizingColumn) {
			resizingColumn = null;
			return;
		}

		isPanning = false;
		draggedNodeId = null;

		// Finaliza conexão
		if (connectingFromId && canvasElement) {
			const rect = canvasElement.getBoundingClientRect();
			const mx = e.clientX - rect.left - pan.x;
			const my = e.clientY - rect.top - pan.y;

			// Procura nó cuja porta de entrada está próxima do mouse
			const targetNode = Object.values(nodes).find((n) => {
				if (n.type === 'start' || n.id === connectingFromId) return false;
				const port = getInputPortPos(n);
				return Math.hypot(mx - port.x, my - port.y) < 25;
			});

			if (targetNode) {
				nodes[connectingFromId].nextId = targetNode.id;
			}

			connectingFromId = null;
		}
	};

	// ── Drag de Nós (apenas pelo header) ──
	const handleNodeHeaderMouseDown = (e: MouseEvent, id: string) => {
		e.stopPropagation();
		e.preventDefault();
		draggedNodeId = id;
		dragStartOffset = { x: e.clientX - nodes[id].x, y: e.clientY - nodes[id].y };
	};

	// ── Bloqueia propagação em qualquer clique dentro do nó ──
	const handleNodeMouseDown = (e: MouseEvent) => {
		e.stopPropagation();
	};

	const handleConnectionKeyDown = (e: KeyboardEvent, fromId: string) => {
		if (e.key === 'Enter' || e.key === ' ') {
			e.preventDefault();
			removeConnection(fromId);
		}
	};

	// ── Início de Conexão (porta de saída) ──
	const handleOutputPortMouseDown = (e: MouseEvent, id: string) => {
		e.stopPropagation();
		e.preventDefault();
		// Se já existe conexão, remove antes
		if (nodes[id].nextId) {
			nodes[id].nextId = undefined;
		}
		connectingFromId = id;
		if (canvasElement) {
			const rect = canvasElement.getBoundingClientRect();
			mouseX = e.clientX - rect.left - pan.x;
			mouseY = e.clientY - rect.top - pan.y;
		}
	};

	// ── Redimensionamento de Colunas ──
	const startResize = (e: MouseEvent, column: 'palette' | 'terminal') => {
		e.preventDefault();
		resizingColumn = column;
		resizeStartX = e.clientX;
		resizeStartWidth = column === 'palette' ? paletteWidth : terminalWidth;
	};

	// ── CRUD de Nós ──
	const addBohNode = () => {
		const id = `node-${Date.now()}`;
		nodes[id] = {
			id,
			type: 'boh',
			x: -pan.x + 200 + Math.random() * 60,
			y: -pan.y + 150 + Math.random() * 60,
			expression: 'idle',
			text: 'Olá! Escreva o diálogo aqui.'
		};
	};

	const deleteNode = (id: string) => {
		if (id === 'start') return;
		Object.keys(nodes).forEach((key) => {
			if (nodes[key].nextId === id) nodes[key].nextId = undefined;
		});
		delete nodes[id];
	};

	const removeConnection = (fromId: string) => {
		nodes[fromId].nextId = undefined;
	};

	// ── Persistência ──
	const saveProject = () => {
		project.nodes = { ...nodes };
		const savedProjects = JSON.parse(localStorage.getItem('saved-projects-v2') || '[]');
		const idx = savedProjects.findIndex((p: any) => p.id === project.id);
		const updated = { ...project, nodes: { ...nodes } };
		if (idx >= 0) savedProjects[idx] = updated;
		else savedProjects.push(updated);
		localStorage.setItem('saved-projects-v2', JSON.stringify(savedProjects));
		alert('Projeto salvo localmente com sucesso!');
	};

	const exportJson = () => {
		const dataStr =
			'data:text/json;charset=utf-8,' +
			encodeURIComponent(JSON.stringify({ ...project, nodes }, null, 2));
		const a = document.createElement('a');
		a.href = dataStr;
		a.download = `${project.name.toLowerCase()}_dialogue.json`;
		document.body.appendChild(a);
		a.click();
		a.remove();
	};

	const triggerImportJson = () => {
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.json';
		input.onchange = (e) => {
			const file = (e.target as HTMLInputElement).files?.[0];
			if (!file) return;
			const reader = new FileReader();
			reader.onload = (ev) => {
				try {
					const parsed = JSON.parse(ev.target?.result as string);
					if (parsed?.nodes) {
						nodes = parsed.nodes;
						alert('Grafo importado!');
					} else alert('JSON inválido.');
				} catch {
					alert('Erro ao processar JSON.');
				}
			};
			reader.readAsText(file);
		};
		input.click();
	};
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	class="h-screen flex flex-col bg-base-300 select-none overflow-hidden text-base-content"
	onmousemove={handleGlobalMouseMove}
	onmouseup={handleGlobalMouseUp}
>
	<!-- ═══ Barra Superior ═══ -->
	<header
		class="h-14 bg-base-100 border-b border-base-200 px-4 flex items-center justify-between z-10 shrink-0 shadow-sm"
	>
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
				<Upload class="size-4" /> Importar JSON
			</button>
			<button onclick={exportJson} class="btn btn-sm btn-ghost gap-1.5 font-semibold">
				<Download class="size-4" /> Exportar JSON
			</button>
			<button
				onclick={saveProject}
				class="btn btn-sm btn-primary gap-1.5 font-bold shadow-lg shadow-primary/10"
			>
				<Save class="size-4" /> Salvar
			</button>
		</div>
	</header>

	<!-- ═══ Área de 3 Colunas Redimensionáveis ═══ -->
	<div class="flex-1 flex overflow-hidden">
		<!-- ─── Coluna Esquerda: Paleta ─── -->
		<aside
			class="bg-base-100 border-r border-base-200 p-4 flex flex-col gap-4 shrink-0 overflow-y-auto"
			style="width: {paletteWidth}px"
		>
			<div>
				<h3 class="text-sm font-bold tracking-wider text-base-content/60 uppercase">
					Paleta de Nós
				</h3>
				<p class="text-xs text-base-content/50 mt-1">Clique para adicionar nós no canvas</p>
			</div>
			<button
				onclick={addBohNode}
				class="btn btn-outline btn-primary btn-block gap-2 text-sm justify-start font-semibold"
			>
				<Plus class="size-4" /> Fala do Boh
			</button>
		</aside>

		<!-- Handle de Resize: Paleta ↔ Canvas -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			class="w-1.5 cursor-col-resize bg-base-200 hover:bg-primary/30 active:bg-primary/50 transition-colors shrink-0 z-20"
			onmousedown={(e) => startResize(e, 'palette')}
		></div>

		<!-- ─── Coluna Central: Canvas Infinito ─── -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			bind:this={canvasElement}
			onmousedown={handleCanvasMouseDown}
			class="flex-1 relative overflow-hidden bg-base-300 canvas-grid"
			class:cursor-grab={!isPanning && !draggedNodeId && !connectingFromId}
			class:cursor-grabbing={isPanning}
			class:cursor-crosshair={!!connectingFromId}
		>
			<div
				class="absolute inset-0 origin-top-left"
				style="transform: translate3d({pan.x}px, {pan.y}px, 0)"
			>
				<!-- SVG Overlay: Conexões (Splines) -->
				<svg class="absolute inset-0 pointer-events-none w-[8000px] h-[8000px]">
					<!-- Conexões Existentes -->
					{#each Object.values(nodes) as node}
						{#if node.nextId && nodes[node.nextId]}
							{@const outPort = getOutputPortPos(node)}
							{@const inPort = getInputPortPos(nodes[node.nextId])}
							{@const dx = Math.abs(inPort.x - outPort.x)}
							{@const offset = Math.max(60, dx * 0.45)}

							<g
								class="pointer-events-auto group cursor-pointer"
								tabindex="0"
								role="button"
								aria-label="Remover conexão"
								onclick={() => removeConnection(node.id)}
								onkeydown={(e) => handleConnectionKeyDown(e, node.id)}
							>
								<!-- Hitbox invisível para facilitar clique -->
								<path
									d={`M ${outPort.x} ${outPort.y} C ${outPort.x + offset} ${outPort.y}, ${inPort.x - offset} ${inPort.y}, ${inPort.x} ${inPort.y}`}
									fill="none"
									stroke="transparent"
									stroke-width="14"
								/>
								<!-- Spline Visível (SEM seta) -->
								<path
									d={`M ${outPort.x} ${outPort.y} C ${outPort.x + offset} ${outPort.y}, ${inPort.x - offset} ${inPort.y}, ${inPort.x} ${inPort.y}`}
									fill="none"
									class="stroke-base-content/25 group-hover:stroke-error/70 transition-colors duration-200"
									stroke-width="2.5"
									stroke-linecap="round"
								/>
							</g>
						{/if}
					{/each}

					<!-- Conexão temporária sendo arrastada -->
					{#if connectingFromId && nodes[connectingFromId]}
						{@const outPort = getOutputPortPos(nodes[connectingFromId])}
						{@const dx = Math.abs(mouseX - outPort.x)}
						{@const offset = Math.max(60, dx * 0.45)}
						<path
							d={`M ${outPort.x} ${outPort.y} C ${outPort.x + offset} ${outPort.y}, ${mouseX - offset} ${mouseY}, ${mouseX} ${mouseY}`}
							fill="none"
							class="stroke-primary/60"
							stroke-dasharray="6,4"
							stroke-width="2.5"
							stroke-linecap="round"
						/>
					{/if}
				</svg>

				<!-- Renderização dos Nós -->
				{#each Object.values(nodes) as node (node.id)}
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div
						onmousedown={(e) => {
							handleNodeMouseDown(e);
							selectedNodeId = node.id;
						}}
						class="absolute select-none flex flex-col group/node transition-shadow {activePlayNodeId ===
						node.id
							? 'ring-2 ring-success/50'
							: ''} {selectedNodeId === node.id ? 'ring-2 ring-primary' : ''}"
						class:w-32={node.type === 'start'}
						class:w-72={node.type === 'boh'}
						style="left: {node.x}px; top: {node.y}px;"
					>
						{#if node.type === 'start'}
							<!-- ══ Nó Start ══ -->
							<div
								onmousedown={(e) => handleNodeHeaderMouseDown(e, node.id)}
								class="flex items-center justify-between px-3 py-2 bg-base-100 border border-base-200 shadow-md rounded-md cursor-grab active:cursor-grabbing"
							>
								<span class="flex items-center gap-1.5 text-success font-bold text-sm"
									><Zap class="size-4" /> Start</span
								>

								<!-- Porta de Saída (Start) -->
								<button
									onmousedown={(e) => handleOutputPortMouseDown(e, node.id)}
									class="w-4 h-4 rounded-full border-2 border-success translate-x-5 hover:scale-125 transition-transform cursor-crosshair {connectedOutputIds.has(
										node.id
									)
										? 'bg-success'
										: 'bg-base-100'}"
									title="Conectar saída"
									aria-label="Conectar saída do nó Start"
								></button>
							</div>
						{:else}
							<!-- ══ Nó Diálogo do Boh ══ -->
							<div class="bg-base-100 border border-base-200 shadow-md rounded-lg overflow-hidden">
								<!-- Header (arrastável) -->
								<div
									onmousedown={(e) => handleNodeHeaderMouseDown(e, node.id)}
									class="px-3 py-2 bg-base-200/60 border-b border-base-200 flex items-center justify-between cursor-grab active:cursor-grabbing"
								>
									<!-- Porta de Entrada -->
									<button
										class="w-4 h-4 rounded-full border-2 border-primary -translate-x-5 {connectedInputIds.has(
											node.id
										)
											? 'bg-primary'
											: 'bg-base-100'}"
										title="Entrada"
										aria-label="Porta de entrada"
									></button>

									<span class="font-bold text-xs text-base-content/70 uppercase tracking-wide"
										>Diálogo do Boh</span
									>

									<div class="flex items-center gap-1.5">
										<button
											onclick={() => deleteNode(node.id)}
											class="text-base-content/40 hover:text-error transition-colors p-0.5 rounded"
											title="Excluir Nó"
										>
											<Trash2 class="size-4" />
										</button>
										<!-- Porta de Saída -->
										<button
											onmousedown={(e) => handleOutputPortMouseDown(e, node.id)}
											class="w-4 h-4 rounded-full border-2 border-primary translate-x-5 hover:scale-125 transition-transform cursor-crosshair {connectedOutputIds.has(
												node.id
											)
												? 'bg-primary'
												: 'bg-base-100'}"
											title="Conectar saída"
											aria-label="Conectar saída do diálogo"
										></button>
									</div>
								</div>

								<!-- Corpo -->
								<div class="p-3 flex flex-col gap-3">
									<div class="form-control">
										<label class="label py-1" for="expr-{node.id}">
											<span class="label-text text-[11px] font-bold text-base-content/60"
												>Expressão do Boh</span
											>
										</label>
										<select
											id="expr-{node.id}"
											bind:value={node.expression}
											class="select select-bordered select-xs w-full bg-base-200 font-semibold text-xs rounded-md"
										>
											<option value="idle">Idle (Padrão)</option>
											<option value="pokerface">Pokerface</option>
											<option value="thinking">Pensando</option>
											<option value="open mouth">Gritando</option>
											<option value="annoyed">Irritado</option>
											<option value="looking down">Desanimado</option>
										</select>
									</div>
									<div class="form-control">
										<label class="label py-1" for="text-{node.id}">
											<span class="label-text text-[11px] font-bold text-base-content/60"
												>Texto da Fala</span
											>
										</label>
										<textarea
											id="text-{node.id}"
											bind:value={node.text}
											class="textarea textarea-bordered text-xs leading-relaxed font-medium bg-base-200 rounded-md h-20 resize-none placeholder:text-base-content/45"
											placeholder="Escreva a fala do Boh aqui..."></textarea>
									</div>
								</div>
							</div>
						{/if}
					</div>
				{/each}
			</div>
		</div>

		<!-- Handle de Resize: Canvas ↔ Terminal -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			class="w-1.5 cursor-col-resize bg-base-200 hover:bg-primary/30 active:bg-primary/50 transition-colors shrink-0 z-20"
			onmousedown={(e) => startResize(e, 'terminal')}
		></div>

		<!-- ─── Coluna Direita: Player de Terminal macOS ─── -->
		<aside
			class="bg-base-100 border-l border-base-200 flex flex-col z-10 shrink-0"
			style="width: {terminalWidth}px"
		>
			<DialoguePlayer {nodes} bind:terminalWidth bind:activePlayNodeId {selectedNodeId} />
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
	:global([data-theme='mocha']) .canvas-grid,
	:global([data-theme='macchiato']) .canvas-grid,
	:global([data-theme='frappe']) .canvas-grid {
		background-image:
			linear-gradient(to right, rgba(255, 255, 255, 0.03) 1px, transparent 1px),
			linear-gradient(to bottom, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
	}
</style>
