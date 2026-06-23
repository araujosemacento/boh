<script lang="ts">
	import { untrack } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import ProjectWorkspace from '$lib/components/ProjectWorkspace.svelte';
	import type { Project } from '$lib/types';
	import demoProjects from '$lib/assets/demos.json';

	let projects = $state<Project[]>([]);
	let project = $state<Project | null>(null);
	let isLoading = $state(true);
	let errorMsg = $state<string | null>(null);

	const projectId = $derived($page.params.projectId);

	// 1. Carregar projetos do localStorage ou padrão
	$effect(() => {
		if (typeof window !== 'undefined') {
			const saved = localStorage.getItem('saved-projects-v2');
			if (saved) {
				try {
					projects = JSON.parse(saved);
				} catch {
					projects = demoProjects as unknown as Project[];
				}
			} else {
				projects = demoProjects as unknown as Project[];
				localStorage.setItem('saved-projects-v2', JSON.stringify(projects));
			}
			isLoading = false;
		}
	});

	// 2. Efeito para lidar com a URL e selecionar/criar o projeto
	$effect(() => {
		if (isLoading) return;

		// Observamos reativamente a mudança no projectId
		const id = projectId;

		untrack(() => {
			if (id) {
				const matched = projects.find((p) => p.id === id);
				if (matched) {
					project = matched;
					errorMsg = null;
				} else {
					project = null;
					errorMsg = 'Projeto não encontrado!';
				}
			} else {
				// URL é apenas /editor -> Criar novo projeto
				const newId = `project-${Date.now()}`;
				const newProject: Project = {
					id: newId,
					name: `Novo Projeto ${projects.length + 1}`,
					description: 'Crie e configure os nós do diálogo deste projeto.',
					tag: 'Rascunho',
					nodes: {
						start: { id: 'start', type: 'start', x: 100, y: 200, expression: 'idle', text: '' }
					}
				};

				projects = [...projects, newProject];
				localStorage.setItem('saved-projects-v2', JSON.stringify(projects));

				// Redireciona substituindo o histórico
				goto(`${base}/editor/${newId}`, { replaceState: true });
			}
		});
	});

	const handleBack = () => {
		goto(`${base}/`);
	};
</script>

<div class="h-screen w-screen flex flex-col bg-base-300">
	{#if isLoading}
		<div
			class="flex-1 flex flex-col items-center justify-center gap-4 text-base-content/60 font-mono"
		>
			<span class="loading loading-spinner loading-lg text-primary"></span>
			<span>Carregando dados do emulador...</span>
		</div>
	{:else if errorMsg}
		<div class="flex-1 flex flex-col items-center justify-center gap-4 font-mono p-4 text-center">
			<div class="bg-error/10 text-error border border-error/20 p-6 rounded-lg max-w-md shadow-lg">
				<h2 class="text-lg font-bold">Erro de Carregamento</h2>
				<p class="text-sm text-error/80 mt-2">{errorMsg}</p>
				<button onclick={handleBack} class="btn btn-outline btn-error btn-sm mt-5">
					Voltar para o Dashboard
				</button>
			</div>
		</div>
	{:else if project}
		<ProjectWorkspace bind:project onBack={handleBack} />
	{/if}
</div>
