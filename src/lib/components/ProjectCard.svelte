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
	class="group relative overflow-hidden rounded-4xl border border-base-300 bg-base-100 p-5 shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl sm:p-6"
>
	<div class="relative flex h-full flex-col gap-4">
		<div class="flex items-start justify-between gap-4">
			<div class="space-y-2">
				<div class="inline-flex items-center gap-2 rounded-full bg-base-200 px-3 py-1 text-xs font-medium text-base-content/65">
					<Tag class="size-4" />
					{project.tag}
				</div>
				<div>
					<h3 class="text-xl font-semibold tracking-tight sm:text-2xl">{project.name}</h3>
					<p class="mt-2 max-w-md text-sm leading-6 text-base-content/65">{project.description}</p>
				</div>
			</div>

			<button type="button" class="btn btn-ghost btn-sm rounded-full px-3 text-sm font-medium">
				<ArrowUpRight class="size-4" />
			</button>
		</div>
	</div>
</article>
