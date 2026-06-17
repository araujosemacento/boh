<script lang="ts">
	import { ArrowUpRight, Tag, Trash2 } from '@lucide/svelte';

	type Project = {
		name: string;
		description: string;
		tag: string;
	};

	let {
		project,
		selected = false,
		onDelete
	}: { project: Project; selected?: boolean; onDelete?: (e: MouseEvent) => void } = $props();
</script>

<article
	class="group relative overflow-hidden rounded-lg border p-5 shadow-sm transition-all duration-200 sm:p-6 {selected ? 'border-primary ring-2 ring-primary/40 bg-primary/5 shadow-md scale-[0.99]' : 'border-base-200 bg-base-100 hover:-translate-y-1 hover:shadow-xl'}"
>
	<!-- Indicator Checkbox -->
	<div class="absolute top-4 right-4 z-10">
		{#if selected}
			<span class="flex h-5 w-5 items-center justify-center rounded border border-primary bg-primary text-primary-content shadow-sm transition-all duration-200 animate-in zoom-in-75">
				<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="3.5" stroke="currentColor" class="size-3">
					<path stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5" />
				</svg>
			</span>
		{:else}
			<span class="flex h-5 w-5 items-center justify-center rounded border border-base-300 bg-base-200/50 text-transparent opacity-0 group-hover:opacity-100 hover:border-primary/50 transition-all duration-150 shadow-sm">
			</span>
		{/if}
	</div>

	<div class="relative flex h-full flex-col gap-4">
		<div class="flex items-start justify-between gap-4">
			<div class="space-y-2.5 pr-6">
				<div
					class="inline-flex items-center gap-1.5 rounded bg-primary/10 px-3 py-1 text-xs font-semibold text-primary transition-colors duration-300"
				>
					<Tag class="size-3.5" />
					{project.tag}
				</div>
				<div>
					<h3 class="text-xl font-semibold tracking-tight text-base-content sm:text-2xl">
						{project.name}
					</h3>
					<p class="mt-2 max-w-md text-sm leading-6 text-base-content/80">{project.description}</p>
				</div>
			</div>

			{#if !selected}
				<div class="flex items-center gap-1">
					<button
						type="button"
						onclick={(e) => {
							e.stopPropagation();
							onDelete?.(e);
						}}
						class="btn btn-ghost btn-sm p-2 text-error hover:bg-error/10 transition-colors duration-300 md:opacity-0 group-hover:opacity-100"
						title="Excluir projeto"
					>
						<Trash2 class="size-5" />
					</button>

					<button
						type="button"
						class="btn btn-ghost btn-sm p-2 text-base-content/70 hover:text-primary transition-colors duration-300 md:opacity-0 group-hover:opacity-100"
					>
						<ArrowUpRight
							class="size-5 transition-transform duration-300 group-hover:translate-x-0.5 group-hover:-translate-y-0.5"
						/>
					</button>
				</div>
			{/if}
		</div>
	</div>
</article>

