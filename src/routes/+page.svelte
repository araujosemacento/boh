<script lang="ts">
	import { projectStore } from '$lib/stores/projectStore.svelte';
	import ProjectCard from '$lib/components/ProjectCard.svelte';
	import TerminalPlayer from '$lib/components/TerminalPlayer.svelte';
	import type { Project } from '$lib/content/project';

	let activeProject = $state<Project | null>(null);
	let showPlayer = $state(false);

	function handlePlay(project: Project) {
		activeProject = project;
		showPlayer = true;
	}

	function handleClosePlayer() {
		showPlayer = false;
		activeProject = null;
	}

	function handleExport(project: Project) {
		const json = projectStore.exportProject(project.id);
		const blob = new Blob([json], { type: 'application/json' });
		const url = URL.createObjectURL(blob);
		const a = document.createElement('a');
		a.href = url;
		a.download = `${project.title}.json`;
		a.click();
		URL.revokeObjectURL(url);
	}

	function handleImport() {
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.json';
		input.onchange = (e) => {
			const file = (e.target as HTMLInputElement).files?.[0];
			if (!file) return;
			const reader = new FileReader();
			reader.onload = (ev) => {
				try {
					const json = ev.target?.result as string;
					projectStore.importProject(json);
				} catch (err) {
					alert('Erro ao importar projeto');
				}
			};
			reader.readAsText(file);
		};
		input.click();
	}
</script>

{#if showPlayer && activeProject}
	<TerminalPlayer project={activeProject} onClose={handleClosePlayer} />
{:else}
	<div class="min-h-screen bg-gray-950 p-8">
		<div class="mx-auto max-w-6xl">
			<header class="mb-8 flex items-center justify-between">
				<div>
					<h1 class="text-3xl font-bold text-white">Projetos</h1>
					<p class="mt-2 text-gray-400">Gerencie e reproduza seus projetos interativos</p>
				</div>
				<div class="flex gap-3">
					<button
						onclick={handleImport}
						class="rounded-lg border border-gray-600 bg-gray-800 px-4 py-2 text-sm text-gray-200 transition-colors hover:bg-gray-700"
					>
						Importar
					</button>
				</div>
			</header>

			<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
				{#each projectStore.projects as project (project.id)}
					<ProjectCard
						{project}
						onPlay={() => handlePlay(project)}
						onEdit={() => alert('Editor ainda nao implementado')}
					/>
				{/each}

				<!-- New Project Card -->
				<button
					class="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-gray-700 bg-gray-900/50 p-6 text-gray-400 transition-all hover:border-gray-500 hover:text-gray-200"
					onclick={() => alert('Criacao de projetos ainda nao implementada')}
				>
					<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="mb-2 h-12 w-12">
						<path fill-rule="evenodd" d="M12 3.75a.75.75 0 01.75.75v6.75h6.75a.75.75 0 010 1.5h-6.75v6.75a.75.75 0 01-1.5 0v-6.75H3.75a.75.75 0 010-1.5h6.75V4.5a.75.75 0 01.75-.75z" clip-rule="evenodd" />
					</svg>
					<span class="text-sm font-medium">Novo Projeto</span>
				</button>
			</div>
		</div>
	</div>
{/if}
