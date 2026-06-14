<script lang="ts">
	import type { Project } from '$lib/content/project';
	import { Play, Wrench } from 'lucide-svelte';

	interface Props {
		project: Project;
		onPlay: () => void;
		onEdit: () => void;
	}

	let { project, onPlay, onEdit }: Props = $props();
</script>

<div
	class="group relative flex flex-col rounded-lg border p-6 transition-all duration-300 hover:shadow-xl"
	style:border-color="var(--ctp-surface1)"
	style:background-color="var(--ctp-surface0)"
>
	<div class="mb-4 flex items-center justify-between">
		<h3 class="text-lg font-semibold" style:color="var(--ctp-text)">{project.title}</h3>
		<span
			class="rounded-full px-2 py-0.5 text-xs"
			style:background-color="var(--ctp-surface1)"
			style:color="var(--ctp-subtext1)"
		>
			{Object.keys(project.chapter.steps).length} etapas
		</span>
	</div>
	<p class="mb-4 text-sm" style:color="var(--ctp-subtext0)">{project.description}</p>
	<div class="mt-auto text-xs" style:color="var(--ctp-overlay0)">
		Atualizado: {new Date(project.updatedAt).toLocaleDateString('pt-BR')}
	</div>

	<!-- Hover Overlay -->
	<div
		class="absolute inset-0 flex items-center justify-center gap-4 rounded-lg opacity-0 transition-opacity duration-200 group-hover:opacity-100"
		style:background-color="var(--ctp-mantle)"
	>
		<button
			onclick={onPlay}
			class="flex flex-col items-center gap-1 rounded-lg p-3 text-white transition-colors duration-200 hover:opacity-80"
			style:background-color="var(--ctp-green)"
			title="Play"
		>
			<Play class="h-8 w-8" fill="currentColor" />
			<span class="text-xs font-medium">Play</span>
		</button>
		<button
			onclick={onEdit}
			class="flex flex-col items-center gap-1 rounded-lg p-3 text-white transition-colors duration-200 hover:opacity-80"
			style:background-color="var(--ctp-blue)"
			title="Editar"
		>
			<Wrench class="h-8 w-8" />
			<span class="text-xs font-medium">Editar</span>
		</button>
	</div>
</div>
