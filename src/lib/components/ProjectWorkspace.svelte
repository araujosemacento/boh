<script lang="ts">
	import { onMount } from 'svelte';
	import {
		ArrowLeft,
		Save,
		Download,
		Upload,
		Plus,
		Minus,
		Trash2,
		Zap,
		Maximize,
		Sparkles
	} from '@lucide/svelte';
	import type { Project } from '../types';
	import DialoguePlayer from './DialoguePlayer.svelte';
	import { WorkspaceState } from '../workspaceState.svelte';
	import { getOutputPortPos, getInputPortPos } from '../utils/canvasUtils';

	let { project = $bindable(), onBack }: { project: Project; onBack: () => void } = $props();

	// Cria e gerencia o estado usando a classe WorkspaceState extraída
	const workspace = new WorkspaceState(project);
	let isMounted = false;

	$effect(() => {
		JSON.stringify(workspace.nodes);
		if (isMounted) workspace.debouncedSave();
	});

	onMount(() => {
		isMounted = true;
		// Ajusta a tela inicialmente após a montagem do DOM
		setTimeout(() => {
			workspace.fitView();
		}, 50);
	});
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	class="h-screen flex flex-col bg-base-300 select-none overflow-hidden text-base-content"
	onmousemove={(e) => workspace.handleGlobalMouseMove(e)}
	onmouseup={(e) => workspace.handleGlobalMouseUp(e)}
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
			<button
				onclick={() => workspace.triggerImportJson()}
				class="btn btn-sm btn-ghost gap-1.5 font-semibold"
			>
				<Upload class="size-4" /> Importar JSON
			</button>
			<button
				onclick={() => workspace.exportJson()}
				class="btn btn-sm btn-ghost gap-1.5 font-semibold"
			>
				<Download class="size-4" /> Exportar JSON
			</button>
			{#if workspace.saveStatus === 'saving'}
				<span class="text-xs text-base-content/50 font-medium ml-2">Salvando...</span>
			{:else if workspace.saveStatus === 'saved'}
				<span class="text-xs text-success font-medium ml-2">Salvo</span>
			{/if}
			<button
				onclick={() => workspace.saveProject()}
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
			style="width: {workspace.paletteWidth}px"
		>
			<div>
				<h3 class="text-sm font-bold tracking-wider text-base-content/60 uppercase">
					Paleta de Nós
				</h3>
				<p class="text-xs text-base-content/50 mt-1">Arraste para o canvas ou clique para criar</p>
			</div>
			<button
				onclick={() => workspace.addBohNode()}
				draggable="true"
				ondragstart={(e) => workspace.handleDragStart(e)}
				class="btn btn-outline btn-primary btn-block gap-2 text-sm justify-start font-semibold cursor-grab active:cursor-grabbing"
			>
				<Plus class="size-4" /> Fala do Boh
			</button>
		</aside>

		<!-- Handle de Resize: Paleta ↔ Canvas -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			class="w-1.5 cursor-col-resize bg-base-200 hover:bg-primary/30 active:bg-primary/50 transition-colors shrink-0 z-20"
			onmousedown={(e) => workspace.startResize(e, 'palette')}
		></div>

		<!-- ─── Coluna Central: Canvas Infinito ─── -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			bind:this={workspace.canvasElement}
			onmousedown={(e) => workspace.handleCanvasMouseDown(e)}
			onwheel={(e) => workspace.handleWheel(e)}
			ondragover={(e) => workspace.handleDragOver(e)}
			ondrop={(e) => workspace.handleDrop(e)}
			class="flex-1 relative overflow-hidden bg-base-300 canvas-grid"
			style="background-size: {24 * workspace.zoom}px {24 *
				workspace.zoom}px; background-position: {workspace.pan.x}px {workspace.pan.y}px;"
			class:cursor-grab={!workspace.isPanning &&
				!workspace.draggedNodeId &&
				!workspace.connectingFromId}
			class:cursor-grabbing={workspace.isPanning}
			class:cursor-crosshair={!!workspace.connectingFromId}
		>
			<div
				class="absolute inset-0 origin-top-left"
				style="transform: translate3d({workspace.pan.x}px, {workspace.pan
					.y}px, 0) scale({workspace.zoom}); transform-origin: 0 0;"
			>
				<!-- SVG Overlay: Conexões (Splines) -->
				<svg class="absolute inset-0 pointer-events-none w-[8000px] h-[8000px]">
					<!-- Conexões Existentes -->
					{#each Object.values(workspace.nodes) as node}
						{#if node.nextId && workspace.nodes[node.nextId]}
							{@const outPort = getOutputPortPos(node)}
							{@const inPort = getInputPortPos(workspace.nodes[node.nextId])}
							{@const dx = Math.abs(inPort.x - outPort.x)}
							{@const offset = Math.max(60, dx * 0.45)}

							<g
								class="pointer-events-auto group cursor-pointer"
								tabindex="0"
								role="button"
								aria-label="Remover conexão"
								onclick={() => workspace.removeConnection(node.id)}
								onkeydown={(e) => {
									if (e.key === 'Enter' || e.key === ' ') {
										e.preventDefault();
										workspace.removeConnection(node.id);
									}
								}}
							>
								<!-- Hitbox invisível para facilitar clique -->
								<path
									d={`M ${outPort.x} ${outPort.y} C ${outPort.x + offset} ${outPort.y}, ${inPort.x - offset} ${inPort.y}, ${inPort.x} ${inPort.y}`}
									fill="none"
									stroke="transparent"
									stroke-width="14"
								/>
								<!-- Spline Visível -->
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
					{#if workspace.connectingFromId && workspace.nodes[workspace.connectingFromId]}
						{@const outPort = getOutputPortPos(workspace.nodes[workspace.connectingFromId])}
						{@const dx = Math.abs(workspace.mouseX - outPort.x)}
						{@const offset = Math.max(60, dx * 0.45)}
						<path
							d={`M ${outPort.x} ${outPort.y} C ${outPort.x + offset} ${outPort.y}, ${workspace.mouseX - offset} ${workspace.mouseY}, ${workspace.mouseX} ${workspace.mouseY}`}
							fill="none"
							class="stroke-primary/60"
							stroke-dasharray="6,4"
							stroke-width="2.5"
							stroke-linecap="round"
						/>
					{/if}
				</svg>

				<!-- Renderização dos Nós -->
				{#each Object.values(workspace.nodes) as node (node.id)}
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div
						onmousedown={(e) => {
							if (e.button === 1) return;
							e.stopPropagation();
							if (!e.shiftKey && !e.ctrlKey && !workspace.selectedNodeIds.includes(node.id)) {
								workspace.selectedNodeIds = [node.id];
							}
						}}
						class="absolute select-none flex flex-col group/node transition-shadow {workspace.activePlayNodeId ===
						node.id
							? 'ring-2 ring-success/50'
							: ''} {workspace.selectedNodeIds.includes(node.id) ? 'ring-2 ring-primary' : ''}"
						class:w-32={node.type === 'start'}
						class:w-72={node.type === 'boh'}
						class:node-transition={workspace.draggedNodeId !== node.id}
						style="left: {node.x}px; top: {node.y}px;"
					>
						{#if node.type === 'start'}
							<!-- ══ Nó Start ══ -->
							<div
								onmousedown={(e) => workspace.handleNodeHeaderMouseDown(e, node.id)}
								class="flex items-center justify-between px-3 py-2 bg-base-100 border border-base-200 shadow-md rounded-md cursor-grab active:cursor-grabbing"
							>
								<span class="flex items-center gap-1.5 text-success font-bold text-sm"
									><Zap class="size-4" /> Start</span
								>

								<!-- Porta de Saída (Start) -->
								<button
									onmousedown={(e) => workspace.handleOutputPortMouseDown(e, node.id)}
									class="w-4 h-4 rounded-full border-2 border-success translate-x-5 hover:scale-125 transition-transform cursor-crosshair {workspace.connectedOutputIds.has(
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
									onmousedown={(e) => workspace.handleNodeHeaderMouseDown(e, node.id)}
									class="px-3 py-2 bg-base-200/60 border-b border-base-200 flex items-center justify-between cursor-grab active:cursor-grabbing"
								>
									<!-- Porta de Entrada -->
									<button
										class="w-4 h-4 rounded-full border-2 border-primary -translate-x-5 {workspace.connectedInputIds.has(
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
											onclick={() => workspace.deleteNode(node.id)}
											class="text-base-content/40 hover:text-error transition-colors p-0.5 rounded"
											title="Excluir Nó"
										>
											<Trash2 class="size-4" />
										</button>
										<!-- Porta de Saída -->
										<button
											onmousedown={(e) => workspace.handleOutputPortMouseDown(e, node.id)}
											class="w-4 h-4 rounded-full border-2 border-primary translate-x-5 hover:scale-125 transition-transform cursor-crosshair {workspace.connectedOutputIds.has(
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

			<!-- Box Selection Overlay (fora do container transformado) -->
			{#if workspace.isBoxSelecting}
				<div
					class="absolute border-2 border-primary bg-primary/10 pointer-events-none z-40 rounded-sm"
					style="left: {Math.min(
						workspace.selectionBoxStart.x,
						workspace.selectionBoxEnd.x
					)}px; top: {Math.min(
						workspace.selectionBoxStart.y,
						workspace.selectionBoxEnd.y
					)}px; width: {Math.abs(
						workspace.selectionBoxEnd.x - workspace.selectionBoxStart.x
					)}px; height: {Math.abs(workspace.selectionBoxEnd.y - workspace.selectionBoxStart.y)}px;"
				></div>
			{/if}

			<!-- Barra Flutuante de Controles do Canvas -->
			<div
				class="absolute bottom-4 right-4 flex items-center gap-1.5 z-10 bg-base-100/90 backdrop-blur-md p-1.5 rounded-lg border border-base-200 shadow-lg select-none"
			>
				<button
					onclick={() => {
						workspace.zoom = Math.min(3, workspace.zoom + 0.1);
					}}
					class="btn btn-square btn-ghost btn-xs text-base-content/70 hover:text-primary transition-colors"
					title="Aumentar Zoom"
				>
					<Plus class="size-4" />
				</button>
				<span class="text-[11px] font-bold text-base-content/60 min-w-[40px] text-center font-mono">
					{Math.round(workspace.zoom * 100)}%
				</span>
				<button
					onclick={() => {
						workspace.zoom = Math.max(0.15, workspace.zoom - 0.1);
					}}
					class="btn btn-square btn-ghost btn-xs text-base-content/70 hover:text-primary transition-colors"
					title="Diminuir Zoom"
				>
					<Minus class="size-4" />
				</button>
				<div class="divider divider-horizontal my-1 mx-0.5"></div>
				<button
					onclick={() => workspace.fitView()}
					class="btn btn-square btn-ghost btn-xs text-base-content/70 hover:text-primary transition-colors"
					title="Centralizar Visualização"
				>
					<Maximize class="size-4" />
				</button>
				<button
					onclick={() => workspace.tidyNodes()}
					class="btn btn-square btn-ghost btn-xs text-base-content/70 hover:text-primary transition-colors"
					title="Organizar Nós"
				>
					<Sparkles class="size-4" />
				</button>
			</div>
		</div>

		<!-- Handle de Resize: Canvas ↔ Terminal -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			class="w-1.5 cursor-col-resize bg-base-200 hover:bg-primary/30 active:bg-primary/50 transition-colors shrink-0 z-20"
			onmousedown={(e) => workspace.startResize(e, 'terminal')}
		></div>

		<!-- ─── Coluna Direita: Player de Terminal macOS ─── -->
		<aside
			class="bg-base-100 border-l border-base-200 flex flex-col z-10 shrink-0"
			style="width: {workspace.terminalWidth}px"
		>
			<DialoguePlayer
				nodes={workspace.nodes}
				bind:terminalWidth={workspace.terminalWidth}
				bind:activePlayNodeId={workspace.activePlayNodeId}
				selectedNodeIds={workspace.selectedNodeIds}
			/>
		</aside>
	</div>
</div>

<style>
	.canvas-grid {
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

	.node-transition {
		transition:
			left 0.3s cubic-bezier(0.25, 0.8, 0.25, 1),
			top 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
	}
</style>
