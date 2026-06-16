<script lang="ts">
	import { ArrowUpRight, Tag } from '@lucide/svelte';

	type Project = {
		name: string;
		description: string;
		tag: string;
	};

	let { project }: { project: Project } = $props();
</script>

<article
	class="group relative overflow-hidden rounded-lg border border-base-200 bg-base-100 p-5 shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl sm:p-6"
>
	<div class="relative flex h-full flex-col gap-4">
		<div class="flex items-start justify-between gap-4">
			<div class="space-y-2.5">
				<div class="inline-flex items-center gap-1.5 rounded bg-primary/10 px-3 py-1 text-xs font-semibold text-primary transition-colors duration-300">
					<Tag class="size-3.5" />
					{project.tag}
				</div>
				<div>
					<h3 class="text-xl font-semibold tracking-tight text-base-content sm:text-2xl">{project.name}</h3>
					<p class="mt-2 max-w-md text-sm leading-6 text-base-content/80">{project.description}</p>
				</div>
			</div>

			<button type="button" class="btn btn-ghost btn-sm p-2 text-base-content/70 hover:text-primary transition-colors duration-300">
				<ArrowUpRight class="size-5 transition-transform duration-300 group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
			</button>
		</div>
	</div>
</article>
