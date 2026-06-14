<script lang="ts">
	import { projectStore } from '$lib/stores/projectStore.svelte';
	import ProjectCard from '$lib/components/ProjectCard.svelte';
	import TerminalPlayer from '$lib/components/TerminalPlayer.svelte';
	import type { Project } from '$lib/content/project';
	import { Plus, Upload } from 'lucide-svelte';

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
	<div class="min-h-screen p-8" style:background-color="var(--ctp-base)" style:color="var(--ctp-text)">
		<div class="mx-auto max-w-6xl">
			<header class="mb-8 flex items-center justify-between">
				<div>
					<h1 class="text-3xl font-bold" style:color="var(--ctp-text)">Projetos</h1>
					<p class="mt-2" style:color="var(--ctp-subtext0)">Gerencie e reproduza seus projetos interativos</p>
				</div>
				<div class="flex gap-3">
					<button
						onclick={handleImport}
						class="flex items-center gap-2 rounded-lg border px-4 py-2 text-sm transition-colors duration-200 hover:opacity-80"
						style:border-color="var(--ctp-surface1)"
						style:color="var(--ctp-text)"
						style:background-color="var(--ctp-surface0)"
					>
						<Upload class="h-4 w-4" />
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
					class="flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 transition-all duration-300 hover:border-solid hover:opacity-80"
					style:border-color="var(--ctp-surface1)"
					style:color="var(--ctp-subtext0)"
					onclick={() => alert('Criacao de projetos ainda nao implementada')}
				>
					<Plus class="mb-2 h-12 w-12" />
					<span class="text-sm font-medium">Novo Projeto</span>
				</button>
			</div>
		</div>
	</div>
{/if}
