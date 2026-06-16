<script lang="ts">
	import { Search, Plus } from '@lucide/svelte';
	import Settings from '$lib/components/Settings.svelte';
	import ProjectCard from '$lib/components/ProjectCard.svelte';

	type Project = {
		name: string;
		description: string;
		tag: string;
	};

	const projects: Project[] = [
		{
			name: 'Aurora',
			description: 'Página institucional com visual claro e foco em conteúdo.',
			tag: 'Web'
		},
		{
			name: 'Atlas API',
			description: 'Estrutura de backend com documentação e rotas simples.',
			tag: 'API'
		},
		{
			name: 'Mercury Mobile',
			description: 'Experiência pensada para telas pequenas e navegação direta.',
			tag: 'Mobile'
		},
		{
			name: 'North Star',
			description: 'Base visual para componentes compartilhados e páginas futuras.',
			tag: 'UI kit'
		}
	];

	let search = $state('');

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
</script>

<svelte:head>
	<title>Projetos</title>
	<meta
		name="description"
		content="Dashboard minimalista para organizar projetos com busca e cards responsivos."
	/>
</svelte:head>

<div class="min-h-screen bg-base-300 text-base-content">
	<section class="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
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
						<ProjectCard {project} />
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
