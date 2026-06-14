<script lang="ts">
	import type { Project } from '$lib/content/project';

	interface Props {
		project: Project;
		onPlay: () => void;
		onEdit: () => void;
	}

	let { project, onPlay, onEdit }: Props = $props();
</script>

<div class="group relative flex flex-col rounded-lg border border-gray-700 bg-gray-800 p-6 transition-all duration-300 hover:border-gray-500 hover:shadow-xl">
	<div class="mb-4 flex items-center justify-between">
		<h3 class="text-lg font-semibold text-gray-100">{project.title}</h3>
		<span class="rounded-full bg-gray-700 px-2 py-0.5 text-xs text-gray-300">
			{Object.keys(project.chapter.steps).length} etapas
		</span>
	</div>
	<p class="mb-4 grow text-sm text-gray-400">{project.description}</p>
	<div class="text-xs text-gray-500">
		Atualizado: {new Date(project.updatedAt).toLocaleDateString('pt-BR')}
	</div>

	<!-- Hover Overlay -->
	<div class="absolute inset-0 flex items-center justify-center gap-4 rounded-lg bg-gray-900/80 opacity-0 transition-opacity duration-200 group-hover:opacity-100">
		<button
			onclick={onPlay}
			class="flex flex-col items-center gap-1 rounded-lg bg-emerald-600 p-3 transition-colors hover:bg-emerald-500"
			title="Play"
		>
			<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="h-8 w-8 text-white">
				<path fill-rule="evenodd" d="M4.5 5.653c0-1.427 1.529-2.33 2.779-1.643l11.54 6.347c1.295.712 1.295 2.573 0 3.286L7.28 19.99c-1.25.687-2.779-.217-2.779-1.643V5.653z" clip-rule="evenodd" />
			</svg>
			<span class="text-xs text-white">Play</span>
		</button>
		<button
			onclick={onEdit}
			class="flex flex-col items-center gap-1 rounded-lg bg-blue-600 p-3 transition-colors hover:bg-blue-500"
			title="Editar"
		>
			<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="h-8 w-8 text-white">
				<path fill-rule="evenodd" d="M12 6.75a5.25 5.25 0 016.331-5.066c1.411.16 2.698.773 3.598 1.72 1.252 1.345 1.502 3.292.898 5.06a.375.375 0 01-.57.382l-1.142-.635a.375.375 0 01-.175-.453c.264-.797.195-1.685-.303-2.41a3.75 3.75 0 00-4.176-1.437 3.75 3.75 0 00-2.266 2.266 3.756 3.756 0 001.437 4.176c.725.498 1.613.567 2.41.303a.375.375 0 01.453.175l1.142.635a.375.375 0 01-.382.57c-1.768.604-3.715.354-5.06-.898-2.42-2.597-2.109-6.663.898-8.67zm-3 3a5.25 5.25 0 00-6.331 5.066c-1.411-.16-2.698-.773-3.598-1.72-1.252-1.345-1.502-3.292-.898-5.06a.375.375 0 01.57-.382l1.142.635a.375.375 0 01.175.453c-.264.797-.195 1.685.303 2.41a3.75 3.75 0 004.176 1.437 3.75 3.75 0 002.266-2.266 3.756 3.756 0 00-1.437-4.176c-.725-.498-1.613-.567-2.41-.303a.375.375 0 01-.453-.175l-1.142-.635a.375.375 0 01.382-.57c1.768-.604 3.715-.354 5.06.898 2.42 2.597 2.109 6.663-.898 8.67z" clip-rule="evenodd" />
					<path d="M11.25 4.5a3 3 0 01-3 3M5.25 7.5a6 6 0 00-6 6" stroke="currentColor" stroke-width="1.5" fill="none" />
				</svg>
			<span class="text-xs text-white">Editar</span>
		</button>
	</div>
</div>
