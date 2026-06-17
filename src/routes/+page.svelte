<script lang="ts">
	import { Search, Plus } from '@lucide/svelte';
	import Settings from '$lib/components/Settings.svelte';
	import ProjectCard from '$lib/components/ProjectCard.svelte';
	import ProjectWorkspace from '$lib/components/ProjectWorkspace.svelte';
	import type { Project } from '$lib/types';
	import demoProjects from '$lib/assets/demos.json';
	import JSZip from 'jszip';
	import BohMascot from '$lib/components/BohMascot.svelte';
	import DialoguePlayer from '$lib/components/DialoguePlayer.svelte';

	// 1. Estado de Projetos Carregado do LocalStorage ou Inicializado com Padrões
	let projects = $state<Project[]>(demoProjects as unknown as Project[]);

	// Carrega dados persistidos ao montar o componente no navegador
	$effect(() => {
		if (typeof window !== 'undefined') {
			const saved = localStorage.getItem('saved-projects-v2');
			if (saved) {
				try {
					projects = JSON.parse(saved);
				} catch (e) {
					// Fallback silencioso
				}
			} else {
				localStorage.setItem('saved-projects-v2', JSON.stringify(projects));
			}
		}
	});

	// 2. Estados de Controle da Interface
	let search = $state('');
	let activeProject = $state<Project | null>(null);

	// 3. Estados de Seleção de Projetos
	let selectedProjectIds = $state<string[]>([]);
	let lastSelectedId = $state<string | null>(null);
	let showExportModal = $state(false);
	let playingProject = $state<Project | null>(null);

	// Estados do Drag-to-Select
	let gridContainer = $state<HTMLDivElement | null>(null);
	let isDragging = $state(false);
	let hasDragged = $state(false);
	let dragStart = $state({ x: 0, y: 0 });
	let dragCurrent = $state({ x: 0, y: 0 });
	let initialSelectionBeforeDrag: string[] = [];

	// Retângulo derivado da seleção por arraste
	const dragBoxRect = $derived.by(() => {
		if (!isDragging || !hasDragged) return null;
		const left = Math.min(dragStart.x, dragCurrent.x);
		const top = Math.min(dragStart.y, dragCurrent.y);
		const width = Math.abs(dragStart.x - dragCurrent.x);
		const height = Math.abs(dragStart.y - dragCurrent.y);
		return { left, top, width, height };
	});

	// Filtro de Busca Reativo
	// Filtro de Busca Reativo com suporte a tag:"nome"
	const filteredProjects = $derived(
		projects.filter((project) => {
			const query = search.trim();
			if (!query) return true;

			// Suporta tag:recursos, tag:"Demonstração", tag:'Rascunho'
			const tagMatch = query.match(/tag:\s*(?:"([^"]+)"|'([^']+)'|(\S+))/i);
			if (tagMatch) {
				const tagQuery = (tagMatch[1] || tagMatch[2] || tagMatch[3]).toLowerCase();
				const remainingQuery = query
					.replace(/tag:\s*(?:"[^"]*"|'[^']*'|\S+)/i, '')
					.trim()
					.toLowerCase();

				const matchesTag = project.tag.toLowerCase() === tagQuery;
				if (!matchesTag) return false;

				if (!remainingQuery) return true;
				return [project.name, project.description].join(' ').toLowerCase().includes(remainingQuery);
			}

			const queryLower = query.toLowerCase();
			return [project.name, project.description, project.tag]
				.join(' ')
				.toLowerCase()
				.includes(queryLower);
		})
	);

	// Estados e handlers para Reordenação por Arraste (Drag and Drop)
	let draggedProjectId = $state<string | null>(null);

	function handleDragStart(projectId: string, e: DragEvent) {
		const target = e.target as HTMLElement;
		// Apenas inicia arrastar se NÃO for em elementos interativos
		if (
			target.closest('button') ||
			target.closest('a') ||
			target.closest('input') ||
			target.closest('select')
		) {
			e.preventDefault();
			return;
		}
		draggedProjectId = projectId;
		if (e.dataTransfer) {
			e.dataTransfer.effectAllowed = 'move';
			e.dataTransfer.setData('text/plain', projectId);
		}
	}

	function handleDragOver(projectId: string, e: DragEvent) {
		e.preventDefault();
		if (e.dataTransfer) {
			e.dataTransfer.dropEffect = 'move';
		}
	}

	function handleDrop(targetProjectId: string, e: DragEvent) {
		e.preventDefault();
		if (!draggedProjectId || draggedProjectId === targetProjectId) return;

		const draggedIndex = projects.findIndex((p) => p.id === draggedProjectId);
		const targetIndex = projects.findIndex((p) => p.id === targetProjectId);

		if (draggedIndex !== -1 && targetIndex !== -1) {
			const updatedProjects = [...projects];
			const [draggedProject] = updatedProjects.splice(draggedIndex, 1);
			updatedProjects.splice(targetIndex, 0, draggedProject);
			projects = updatedProjects;
			localStorage.setItem('saved-projects-v2', JSON.stringify(projects));
		}
		draggedProjectId = null;
	}

	function handleDragEnd() {
		draggedProjectId = null;
	}

	// Criação de Novo Projeto
	const handleNewProject = () => {
		const id = `project-${Date.now()}`;
		const newProject: Project = {
			id,
			name: `Novo Projeto ${projects.length + 1}`,
			description: 'Crie e configure os nós do diálogo deste projeto.',
			tag: 'Rascunho',
			nodes: {
				start: { id: 'start', type: 'start', x: 100, y: 200, expression: 'idle', text: '' }
			}
		};

		projects = [...projects, newProject];
		localStorage.setItem('saved-projects-v2', JSON.stringify(projects));
		activeProject = newProject; // Abre o editor diretamente
	};

	// Recarrega lista ao fechar o editor
	const handleBack = () => {
		const saved = localStorage.getItem('saved-projects-v2');
		if (saved) {
			try {
				projects = JSON.parse(saved);
			} catch (e) {
				// Fallback
			}
		}
		activeProject = null;
	};

	// Alterna seleção individual
	function toggleSelect(id: string) {
		if (selectedProjectIds.includes(id)) {
			selectedProjectIds = selectedProjectIds.filter((x) => x !== id);
			if (lastSelectedId === id) {
				lastSelectedId = selectedProjectIds[selectedProjectIds.length - 1] || null;
			}
		} else {
			selectedProjectIds = [...selectedProjectIds, id];
			lastSelectedId = id;
		}
	}

	// Clique no card de projetos
	function handleCardClick(project: Project, event: MouseEvent) {
		const id = project.id;

		// Shift+Click para seleção contínua de intervalo
		if (event.shiftKey) {
			event.preventDefault();
			const projectIds = filteredProjects.map((p) => p.id);
			const currentIdx = projectIds.indexOf(id);
			const lastIdx = lastSelectedId ? projectIds.indexOf(lastSelectedId) : -1;

			if (lastIdx !== -1 && currentIdx !== -1) {
				const start = Math.min(lastIdx, currentIdx);
				const end = Math.max(lastIdx, currentIdx);
				const rangeIds = projectIds.slice(start, end + 1);

				const merged = new Set([...selectedProjectIds, ...rangeIds]);
				selectedProjectIds = Array.from(merged);
				lastSelectedId = id;
			} else {
				// Inicia a seleção a partir do item clicado se não houver seleção anterior
				if (!selectedProjectIds.includes(id)) {
					selectedProjectIds = [...selectedProjectIds, id];
				}
				lastSelectedId = id;
			}
			return;
		}

		// Ctrl+Click ou Meta+Click (Cmd) para alternar seleção
		if (event.ctrlKey || event.metaKey) {
			event.preventDefault();
			toggleSelect(id);
			return;
		}

		// Se já houver alguma seleção ativa, o clique normal apenas alterna a seleção
		if (selectedProjectIds.length > 0) {
			event.preventDefault();
			toggleSelect(id);
			return;
		}

		// Clique padrão abre o projeto
		activeProject = project;
	}

	// Lógica de início de arraste do mouse
	function handleMouseDown(e: MouseEvent) {
		if (e.button !== 0) return; // Apenas botão esquerdo

		// Ignora se o clique foi em botões, inputs, modais ou container de configurações
		const target = e.target as HTMLElement;
		if (
			target.closest('button') ||
			target.closest('input') ||
			target.closest('select') ||
			target.closest('a') ||
			target.closest('.modal-box') ||
			target.closest('.settings-container')
		) {
			return;
		}

		isDragging = true;
		hasDragged = false;
		dragStart = { x: e.clientX, y: e.clientY };
		dragCurrent = { x: e.clientX, y: e.clientY };
		initialSelectionBeforeDrag = [...selectedProjectIds];

		window.addEventListener('mousemove', handleMouseMove);
		window.addEventListener('mouseup', handleMouseUp);
	}

	// Movimento do mouse durante arraste
	function handleMouseMove(e: MouseEvent) {
		if (!isDragging) return;
		dragCurrent = { x: e.clientX, y: e.clientY };

		const dist = Math.hypot(dragCurrent.x - dragStart.x, dragCurrent.y - dragStart.y);
		if (dist > 5) {
			hasDragged = true;
		}

		if (hasDragged && gridContainer) {
			const box = dragBoxRect;
			if (!box) return;

			const cards = gridContainer.querySelectorAll('.project-card-btn');
			const newSelected = new Set<string>();

			cards.forEach((card) => {
				const rect = card.getBoundingClientRect();
				const projectId = card.getAttribute('data-project-id');
				if (!projectId) return;

				// Colisão entre o retângulo de seleção e a bounding box do card
				const overlap =
					box.left < rect.right &&
					box.left + box.width > rect.left &&
					box.top < rect.bottom &&
					box.top + box.height > rect.top;

				if (overlap) {
					newSelected.add(projectId);
				}
			});

			if (e.ctrlKey || e.metaKey) {
				selectedProjectIds = Array.from(new Set([...initialSelectionBeforeDrag, ...newSelected]));
			} else {
				selectedProjectIds = Array.from(newSelected);
			}
		}
	}

	// Fim do arraste do mouse
	function handleMouseUp(e: MouseEvent) {
		if (!isDragging) return;
		isDragging = false;

		window.removeEventListener('mousemove', handleMouseMove);
		window.removeEventListener('mouseup', handleMouseUp);

		// Se o usuário clicou no fundo e não arrastou, limpa a seleção
		if (!hasDragged) {
			const target = e.target as HTMLElement;
			if (!target.closest('.project-card-btn') && !target.closest('header')) {
				selectedProjectIds = [];
				lastSelectedId = null;
			}
		}
	}

	// Atalhos globais de teclado (ex: Ctrl+A)
	function handleKeydown(e: KeyboardEvent) {
		const target = e.target as HTMLElement;
		if (target.closest('input') || target.closest('textarea')) return;

		if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'a') {
			e.preventDefault();
			selectedProjectIds = filteredProjects.map((p) => p.id);
		}
	}

	// Exportação em arquivo JSON Único
	function handleExportSingleJSON() {
		showExportModal = false;
		const selectedProjects = projects.filter((p) => selectedProjectIds.includes(p.id));

		const jsonString = JSON.stringify(selectedProjects, null, 2);
		const blob = new Blob([jsonString], { type: 'application/json' });
		const url = URL.createObjectURL(blob);

		const a = document.createElement('a');
		a.href = url;
		a.download = `boh-projects-export-${Date.now()}.json`;
		document.body.appendChild(a);
		a.click();

		document.body.removeChild(a);
		URL.revokeObjectURL(url);
	}

	// Exportação em arquivo ZIP contendo múltiplos arquivos JSON
	async function handleExportZIP() {
		showExportModal = false;
		const zip = new JSZip();
		const selectedProjects = projects.filter((p) => selectedProjectIds.includes(p.id));

		selectedProjects.forEach((project) => {
			const safeName = project.name.replace(/[^a-zA-Z0-9-_]/g, '_').toLowerCase();
			const filename = `${safeName || 'project'}-${project.id}.json`;
			zip.file(filename, JSON.stringify(project, null, 2));
		});

		try {
			const blob = await zip.generateAsync({ type: 'blob' });
			const url = URL.createObjectURL(blob);

			const a = document.createElement('a');
			a.href = url;
			a.download = `boh-projects-export-${Date.now()}.zip`;
			document.body.appendChild(a);
			a.click();

			document.body.removeChild(a);
			URL.revokeObjectURL(url);
		} catch (error) {
			console.error('Falha ao exportar arquivo ZIP:', error);
			alert('Ocorreu um erro ao gerar o arquivo ZIP compactado.');
		}
	}

	// Exclusão individual de projeto
	function handleSingleDelete(project: Project) {
		if (
			confirm(
				`Deseja mesmo excluir o projeto "${project.name}"? Esta ação é irreversível e removerá todos os dados salvos.`
			)
		) {
			projects = projects.filter((p) => p.id !== project.id);
			localStorage.setItem('saved-projects-v2', JSON.stringify(projects));
			selectedProjectIds = selectedProjectIds.filter((id) => id !== project.id);
			if (lastSelectedId === project.id) {
				lastSelectedId = null;
			}
		}
	}

	// Exclusão em lote de projetos selecionados
	function handleBulkDelete() {
		const count = selectedProjectIds.length;
		if (
			confirm(
				`Deseja mesmo excluir os ${count} projetos selecionados? Esta ação é irreversível e removerá todos os dados salvos.`
			)
		) {
			projects = projects.filter((p) => !selectedProjectIds.includes(p.id));
			localStorage.setItem('saved-projects-v2', JSON.stringify(projects));
			selectedProjectIds = [];
			lastSelectedId = null;
		}
	}
</script>

<svelte:window onkeydown={handleKeydown} />

<svelte:head>
	<title>{activeProject ? `Editor: ${activeProject.name}` : 'Projetos'}</title>
	<meta
		name="description"
		content="Dashboard minimalista para organizar projetos com busca e cards responsivos."
	/>
</svelte:head>

<!-- Overlay do Retângulo de Seleção por Arraste -->
{#if dragBoxRect}
	<div
		style="
			position: fixed;
			left: {dragBoxRect.left}px;
			top: {dragBoxRect.top}px;
			width: {dragBoxRect.width}px;
			height: {dragBoxRect.height}px;
			border: 1.5px solid var(--color-primary, #641ae6);
			background-color: rgba(100, 26, 230, 0.12);
			pointer-events: none;
			z-index: 9999;
			border-radius: 4px;
		"
	></div>
{/if}

{#if activeProject}
	<!-- Coluna de Edição Interativa -->
	<ProjectWorkspace bind:project={activeProject} onBack={handleBack} />
{:else}
	<!-- Dashboard de Projetos -->
	<!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
	<div
		class="h-screen bg-base-300 text-base-content flex flex-col select-none overflow-hidden"
		onmousedown={handleMouseDown}
		role="region"
		aria-label="Dashboard de Projetos"
	>
		<section
			class="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8 lg:py-8 h-full overflow-hidden flex-1"
		>
			<header
				class="flex flex-col gap-5 rounded-lg border border-base-200 bg-base-100 p-5 shadow-sm sm:p-6 lg:flex-row lg:items-center lg:justify-between lg:p-8"
			>
				<div class="max-w-2xl space-y-3">
					<h1 class="text-3xl font-semibold tracking-tight text-balance sm:text-4xl lg:text-5xl">
						Projetos
					</h1>
				</div>

				<div class="flex w-full flex-col gap-3 sm:max-w-sm">
					<div
						class="join w-full overflow-hidden rounded-lg border border-base-300 bg-base-200 shadow-sm focus-within:border-primary/60 focus-within:ring-2 focus-within:ring-primary/15"
					>
						<div class="join-item flex items-center justify-center px-4 text-base-content/70">
							<Search class="size-5" />
						</div>
						<input
							type="search"
							placeholder="Buscar projetos pelo nome ou branch..."
							class="input join-item h-12 w-full border-0 bg-transparent px-0 text-sm placeholder:text-base-content/60 outline-none focus:outline-none sm:text-base"
							bind:value={search}
						/>
					</div>

					<button
						type="button"
						onclick={handleNewProject}
						class="btn btn-primary h-12 gap-2 px-5 text-sm font-semibold shadow-lg shadow-primary/20 sm:text-base"
					>
						<Plus class="size-5" />
						Novo projeto
					</button>
				</div>
			</header>

			<section class="flex-1 min-h-0 overflow-y-auto custom-scrollbar pr-2 pb-24 space-y-4">
				<div class="flex items-center justify-between gap-3">
					<div>
						<h2 class="text-lg font-semibold sm:text-xl">Lista</h2>
					</div>
				</div>

				{#if filteredProjects.length > 0}
					<div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3" bind:this={gridContainer}>
						{#each filteredProjects as project}
							<div
								class="project-card-btn text-left"
								data-project-id={project.id}
								draggable="true"
								role="listitem"
								ondragstart={(e) => handleDragStart(project.id, e)}
								ondragover={(e) => handleDragOver(project.id, e)}
								ondrop={(e) => handleDrop(project.id, e)}
								ondragend={handleDragEnd}
							>
								<ProjectCard
									{project}
									selected={selectedProjectIds.includes(project.id)}
									isDragging={draggedProjectId === project.id}
									onSelect={(e) => handleCardClick(project, e)}
									onPlay={() => (playingProject = project)}
									onEdit={() => (activeProject = project)}
									onDelete={() => handleSingleDelete(project)}
									onTagClick={(tag) => {
										search = `tag:"${tag}"`;
									}}
								/>
							</div>
						{/each}
					</div>
				{:else}
					<div
						class="rounded-lg border border-dashed border-base-300 bg-base-100 px-6 py-14 text-center"
					>
						<div
							class="mx-auto flex size-14 items-center justify-center rounded-lg bg-base-200 shadow-sm"
						>
							<Search class="size-6 text-primary" />
						</div>
						<h3 class="mt-5 text-xl font-semibold">Nenhum item encontrado</h3>
						<p class="mx-auto mt-2 max-w-md text-sm text-base-content/75">
							Tente outro termo na busca para refinar a lista.
						</p>
					</div>
				{/if}
			</section>
		</section>

		<!-- Barra de Ações Flutuante (Multi-seleção) -->
		{#if selectedProjectIds.length > 0}
			<div
				class="fixed bottom-6 left-1/2 -translate-x-1/2 z-40 bg-base-100 border border-base-300 rounded-xl shadow-2xl px-6 py-4 flex items-center gap-5 justify-between w-[calc(100%-2rem)] max-w-xl animate-in fade-in slide-in-from-bottom-4 duration-300"
			>
				<div class="flex items-center gap-3">
					<div class="bg-primary/10 text-primary rounded-lg p-2.5">
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="size-5"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
							/>
						</svg>
					</div>
					<div>
						<p class="font-semibold text-sm text-base-content">
							{selectedProjectIds.length} selecionado{selectedProjectIds.length > 1 ? 's' : ''}
						</p>
						<p class="text-xs text-base-content/60">
							Shift/Ctrl+Clique ou arraste para selecionar mais
						</p>
					</div>
				</div>
				<div class="flex items-center gap-2">
					<button
						type="button"
						class="btn btn-ghost btn-sm text-base-content/75 hover:text-base-content"
						onclick={() => {
							selectedProjectIds = [];
							lastSelectedId = null;
						}}
					>
						Desmarcar
					</button>
					<button
						type="button"
						class="btn btn-error btn-outline btn-sm px-4 gap-1.5"
						onclick={handleBulkDelete}
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="size-4"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="m14.74 9-.34 9m-4.78 0L9 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0"
							/>
						</svg>
						Excluir
					</button>
					<button
						type="button"
						class="btn btn-primary btn-sm px-4 gap-1.5 shadow-md shadow-primary/20"
						onclick={() => (showExportModal = true)}
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="size-4"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3"
							/>
						</svg>
						Exportar
					</button>
				</div>
			</div>
		{/if}

		<!-- Modal de Diálogo Customizado para Exportação -->
		{#if showExportModal}
			<div class="modal modal-open z-50">
				<div
					class="modal-box max-w-md bg-base-100 border border-base-200 shadow-2xl p-6 rounded-xl"
				>
					<h3 class="text-xl font-bold text-base-content flex items-center gap-2">
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="1.5"
							stroke="currentColor"
							class="size-6 text-primary"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3"
							/>
						</svg>
						Exportar {selectedProjectIds.length} Projetos
					</h3>
					<p class="py-4 text-sm text-base-content/70">
						Escolha o formato de exportação ideal para os seus projetos selecionados:
					</p>

					<div class="flex flex-col gap-3">
						<button
							type="button"
							onclick={handleExportSingleJSON}
							class="btn btn-outline hover:btn-primary justify-start h-auto py-3.5 px-4 flex gap-3 text-left w-full border-base-300"
						>
							<div class="flex-1">
								<span class="font-semibold block text-sm">Arquivo JSON Único</span>
								<span class="text-xs text-base-content/60 font-normal mt-0.5 block"
									>Um único arquivo contendo a lista com todos os projetos.</span
								>
							</div>
						</button>

						<button
							type="button"
							onclick={handleExportZIP}
							class="btn btn-outline hover:btn-primary justify-start h-auto py-3.5 px-4 flex gap-3 text-left w-full border-base-300"
						>
							<div class="flex-1">
								<span class="font-semibold block text-sm">Arquivo ZIP (Múltiplos JSONs)</span>
								<span class="text-xs text-base-content/60 font-normal mt-0.5 block"
									>Um arquivo compactado contendo cada projeto em seu próprio arquivo JSON.</span
								>
							</div>
						</button>
					</div>

					<div class="modal-action mt-6">
						<button type="button" class="btn btn-ghost" onclick={() => (showExportModal = false)}
							>Cancelar</button
						>
					</div>
				</div>
				<button
					type="button"
					class="modal-backdrop bg-black/40 cursor-default border-0 outline-none"
					onclick={() => (showExportModal = false)}
					aria-label="Fechar modal"
				></button>
			</div>
		{/if}

		<!-- Modal de Reprodução Focada (DialoguePlayer) -->
		{#if playingProject}
			<div class="modal modal-open z-50">
				<div
					class="modal-box max-w-2xl bg-base-100 border border-base-200 shadow-2xl p-0 rounded-xl overflow-hidden flex flex-col h-[520px] animate-in zoom-in-95 duration-200"
				>
					<!-- Header do Modal -->
					<div
						class="px-5 py-4 bg-base-200/50 flex items-center justify-between border-b border-base-200 shrink-0"
					>
						<div>
							<h3 class="text-xs font-bold tracking-wider text-base-content/60 uppercase">
								Emulador de Diálogo
							</h3>
							<p class="text-base font-bold text-base-content mt-0.5">
								Reproduzindo: {playingProject.name}
							</p>
						</div>
						<button
							type="button"
							class="btn btn-circle btn-ghost btn-sm text-base-content/75 hover:bg-base-200"
							onclick={() => (playingProject = null)}>✕</button
						>
					</div>

					<!-- Corpo do Modal (DialoguePlayer) -->
					<div class="flex-1 min-h-0 flex flex-col bg-base-100">
						<DialoguePlayer nodes={playingProject.nodes} />
					</div>
				</div>
				<button
					type="button"
					class="modal-backdrop bg-black/60 cursor-default border-0 outline-none"
					onclick={() => (playingProject = null)}
					aria-label="Fechar player"
				></button>
			</div>
		{/if}

		<Settings />
		<BohMascot hasSelection={selectedProjectIds.length > 0} />
	</div>
{/if}

<style>
	/* Scrollbar customizada e fina para a lista de cards */
	.custom-scrollbar::-webkit-scrollbar {
		width: 6px;
	}
	.custom-scrollbar::-webkit-scrollbar-track {
		background: transparent;
	}
	.custom-scrollbar::-webkit-scrollbar-thumb {
		background-color: var(--color-base-content, rgba(166, 173, 187, 0.25));
		border-radius: 9999px;
		transition: background-color 0.2s;
	}
	.custom-scrollbar::-webkit-scrollbar-thumb:hover {
		background-color: var(--color-primary, #641ae6);
	}
</style>
