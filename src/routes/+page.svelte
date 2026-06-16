<script lang="ts">
	import { Search, Plus } from '@lucide/svelte';
	import Settings from '$lib/components/Settings.svelte';
	import ProjectCard from '$lib/components/ProjectCard.svelte';
	import ProjectWorkspace from '$lib/components/ProjectWorkspace.svelte';
	import type { Project, DialogueNode } from '$lib/types';

	// Geração procedural de demonstrações
	function createDemoProject(
		id: string,
		name: string,
		description: string,
		tag: string,
		sentences: string[]
	): Project {
		const nodes: Record<string, DialogueNode> = {
			start: {
				id: 'start',
				type: 'start',
				x: 80,
				y: 180,
				expression: 'idle',
				text: '',
				nextId: 'node-1'
			}
		};

		sentences.forEach((text, index) => {
			const nodeId = `node-${index + 1}`;
			const nextNodeId = index < sentences.length - 1 ? `node-${index + 2}` : undefined;
			const expressions: Array<DialogueNode['expression']> = [
				'idle',
				'thinking',
				'open mouth',
				'pokerface',
				'annoyed',
				'looking down'
			];
			const expression = expressions[index % expressions.length];

			nodes[nodeId] = {
				id: nodeId,
				type: 'boh',
				x: 300 + index * 320,
				y: 120 + (index % 2 === 0 ? 60 : -60),
				expression,
				text,
				nextId: nextNodeId
			};
		});

		return { id, name, description, tag, nodes };
	}

	function getProceduralDemos(): Project[] {
		return [
			createDemoProject(
				'demo-intro',
				'Boh: Introdução',
				'Demonstração básica das capacidades de diálogo, expressões e efeito sonoro de digitação do emulador Boh.',
				'Demonstração',
				[
					'Olá! Eu sou o Boh, seu companheiro de diálogos.',
					'Este é o emulador de terminal rodando diretamente no seu navegador.',
					'Você pode arrastar nós no canvas para me fazer dizer o que quiser.'
				]
			),
			createDemoProject(
				'demo-features',
				'Boh: Recursos',
				'Demonstração avançada mostrando diferentes expressões faciais e o alinhamento da caixa de diálogo.',
				'Recursos',
				[
					'Viu só? Eu posso mudar de expressão enquanto digito!',
					'Minha fala agora é formatada automaticamente usando pretext.',
					'E a caixa de diálogo delimita o texto perfeitamente na lateral!'
				]
			)
		];
	}

	// 1. Estado de Projetos Carregado do LocalStorage ou Inicializado com Padrões
	let projects = $state<Project[]>(getProceduralDemos());

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
		<section
			class="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8 lg:py-8 flex-1"
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

			<section class="space-y-4">
				<div class="flex items-center justify-between gap-3">
					<div>
						<h2 class="text-lg font-semibold sm:text-xl">Lista</h2>
					</div>
				</div>

				{#if filteredProjects.length > 0}
					<div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
						{#each filteredProjects as project}
							<button
								type="button"
								onclick={() => (activeProject = project)}
								class="cursor-pointer hover:scale-[1.01] transition-transform duration-200 bg-transparent border-0 p-0"
							>
								<ProjectCard {project} />
							</button>
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
