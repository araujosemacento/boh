<script lang="ts">
	import { Search, Plus } from '@lucide/svelte';
	import Settings from '$lib/components/Settings.svelte';
	import ProjectCard from '$lib/components/ProjectCard.svelte';
	import ProjectWorkspace from '$lib/components/ProjectWorkspace.svelte';
	import type { Project } from '$lib/types';

	// 1. Estado de Projetos Carregado do LocalStorage ou Inicializado com Padrões
	let projects = $state<Project[]>([
		{
			id: 'aurora',
			name: 'Aurora',
			description: 'Página institucional com visual claro e foco em conteúdo.',
			tag: 'Web',
			nodes: {
				'start': { id: 'start', type: 'start', x: 80, y: 180, expression: 'idle', text: '', nextId: 'node-1' },
				'node-1': { id: 'node-1', type: 'boh', x: 300, y: 100, expression: 'thinking', text: 'Bem-vindo ao projeto Aurora!', nextId: 'node-2' },
				'node-2': { id: 'node-2', type: 'boh', x: 650, y: 150, expression: 'open mouth', text: 'Espero que este editor de diálogos seja produtivo para você.' }
			}
		},
		{
			id: 'atlas',
			name: 'Atlas API',
			description: 'Estrutura de backend com documentação e rotas simples.',
			tag: 'API',
			nodes: {
				'start': { id: 'start', type: 'start', x: 80, y: 180, expression: 'idle', text: '', nextId: 'node-1' },
				'node-1': { id: 'node-1', type: 'boh', x: 300, y: 100, expression: 'pokerface', text: 'Iniciando o servidor Atlas...' }
			}
		},
		{
			id: 'mercury',
			name: 'Mercury Mobile',
			description: 'Experiência pensada para telas pequenas e navegação direta.',
			tag: 'Mobile',
			nodes: {
				'start': { id: 'start', type: 'start', x: 80, y: 180, expression: 'idle', text: '', nextId: 'node-1' },
				'node-1': { id: 'node-1', type: 'boh', x: 300, y: 100, expression: 'looking down', text: 'Mercury Mobile está pronto para testes de responsividade.' }
			}
		},
		{
			id: 'northstar',
			name: 'North Star',
			description: 'Base visual para componentes compartilhados e páginas futuras.',
			tag: 'UI kit',
			nodes: {
				'start': { id: 'start', type: 'start', x: 80, y: 180, expression: 'idle', text: '', nextId: 'node-1' },
				'node-1': { id: 'node-1', type: 'boh', x: 300, y: 100, expression: 'idle', text: 'Biblioteca de componentes North Star iniciada.' }
			}
		}
	]);

	// Carrega dados persistidos ao montar o componente no navegador
	$effect(() => {
		if (typeof window !== 'undefined') {
			const saved = localStorage.getItem('saved-projects');
			if (saved) {
				try {
					projects = JSON.parse(saved);
				} catch (e) {
					// Fallback silencioso
				}
			} else {
				localStorage.setItem('saved-projects', JSON.stringify(projects));
			}
		}
	});

	// 2. Estados de Controle da Interface
	let search = $state('');
	let activeProject = $state<Project | null>(null);

	// Filtro de Busca Reativo
	const filteredProjects = $derived(
		projects.filter((project) => {
			const query = search.trim().toLowerCase();
			if (!query) return true;

			return [project.name, project.description, project.tag]
				.join(' ')
				.toLowerCase()
				.includes(query);
		})
	);

	// Criação de Novo Projeto
	const handleNewProject = () => {
		const id = `project-${Date.now()}`;
		const newProject: Project = {
			id,
			name: `Novo Projeto ${projects.length + 1}`,
			description: 'Crie e configure os nós do diálogo deste projeto.',
			tag: 'Rascunho',
			nodes: {
				'start': { id: 'start', type: 'start', x: 100, y: 200, expression: 'idle', text: '' }
			}
		};

		projects = [...projects, newProject];
		localStorage.setItem('saved-projects', JSON.stringify(projects));
		activeProject = newProject; // Abre o editor diretamente
	};

	// Recarrega lista ao fechar o editor
	const handleBack = () => {
		const saved = localStorage.getItem('saved-projects');
		if (saved) {
			try {
				projects = JSON.parse(saved);
			} catch (e) {
				// Fallback
			}
		}
		activeProject = null;
	};
</script>

<svelte:head>
	<title>{activeProject ? `Editor: ${activeProject.name}` : 'Projetos'}</title>
	<meta
		name="description"
		content="Dashboard minimalista para organizar projetos com busca e cards responsivos."
	/>
</svelte:head>

{#if activeProject}
	<!-- Coluna de Edição Interativa -->
	<ProjectWorkspace bind:project={activeProject} onBack={handleBack} />
{:else}
	<!-- Dashboard de Projetos -->
	<div class="min-h-screen bg-base-300 text-base-content flex flex-col">
		<section class="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8 lg:py-8 flex-1">
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

			<section class="space-y-4">
				<div class="flex items-center justify-between gap-3">
					<div>
						<h2 class="text-lg font-semibold sm:text-xl">Lista</h2>
					</div>
				</div>

				{#if filteredProjects.length > 0}
					<div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
						{#each filteredProjects as project}
							<!-- svelte-ignore a11y_click_events_have_key_events -->
							<!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
							<div onclick={() => activeProject = project} class="cursor-pointer hover:scale-[1.01] transition-transform duration-200">
								<ProjectCard {project} />
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

		<Settings />
	</div>
{/if}
