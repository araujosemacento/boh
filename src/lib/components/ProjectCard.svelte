<script lang="ts">
	import { Play, Wrench, Trash2, Tag } from '@lucide/svelte';

	type Project = {
		name: string;
		description: string;
		tag: string;
	};

	let {
		project,
		selected = false,
		isDragging = false,
		onSelect,
		onPlay,
		onEdit,
		onDelete,
		onTagClick
	}: {
		project: Project;
		selected?: boolean;
		isDragging?: boolean;
		onSelect?: (e: MouseEvent) => void;
		onPlay?: (e: MouseEvent) => void;
		onEdit?: (e: MouseEvent) => void;
		onDelete?: (e: MouseEvent) => void;
		onTagClick?: (tag: string) => void;
	} = $props();
</script>

<!-- Container de card com altura e largura padronizadas, muda estilo se estiver sendo arrastado -->
<article
	class="group flex h-44 w-full overflow-hidden rounded-xl border select-none transition-all duration-200 {isDragging
		? 'opacity-40 scale-[0.97] border-dashed border-primary bg-base-200/50 shadow-none'
		: selected
			? 'border-primary bg-primary/5 ring-2 ring-primary/30 shadow-md'
			: 'border-base-200 bg-base-100 hover:border-base-300 hover:shadow-lg'}"
>
	<!-- svelte-ignore a11y_click_events_have_key_events -->
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div
		onclick={(e) => {
			if (e.ctrlKey || e.metaKey || e.shiftKey) {
				e.stopPropagation();
				onSelect?.(e);
			}
		}}
		class="flex-1 p-4 flex flex-col justify-between overflow-hidden"
	>
		<!-- Linha do Título e Lixeira -->
		<div class="flex items-center justify-between gap-2 min-w-0">
			<div class="flex items-center gap-2.5 min-w-0 flex-1">
				<!-- Checkbox Customizada (Único elemento de seleção na esquerda) -->
				<button
					type="button"
					onclick={(e) => {
						e.stopPropagation();
						onSelect?.(e);
					}}
					class="flex h-5 w-5 shrink-0 items-center justify-center rounded border transition-all duration-150 cursor-pointer {selected
						? 'bg-primary border-primary text-primary-content animate-in zoom-in-75'
						: 'bg-base-200 border-base-300 hover:border-primary/50'}"
				>
					{#if selected}
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="3.5"
							stroke="currentColor"
							class="size-3"
						>
							<path stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5" />
						</svg>
					{/if}
				</button>

				<!-- Título com Elipse -->
				<h3
					class="text-base font-bold tracking-tight text-base-content truncate"
					title={project.name}
				>
					{project.name}
				</h3>
			</div>

			<!-- Botão de Lixeira (Visível apenas em hover) -->
			<button
				type="button"
				onclick={(e) => {
					e.stopPropagation();
					onDelete?.(e);
				}}
				class="btn btn-ghost btn-xs p-1.5 h-8 w-8 text-error hover:bg-error/10 opacity-0 group-hover:opacity-100 transition-opacity duration-200 shrink-0 cursor-pointer"
				title="Excluir projeto"
			>
				<Trash2 class="size-4.5" />
			</button>
		</div>

		<!-- Descrição com Elipse Limitada (line-clamp-3) -->
		<div class="flex-1 min-h-0 mt-1.5">
			<p class="text-xs leading-5 text-base-content/70 line-clamp-3 overflow-hidden">
				<span class="font-semibold text-base-content/85">Descrição:</span>
				{project.description}
			</p>
		</div>

		<!-- Tag / Categoria no Rodapé (Clickable para filtragem) -->
		<div class="mt-2 shrink-0">
			<button
				type="button"
				onclick={(e) => {
					e.stopPropagation();
					onTagClick?.(project.tag);
				}}
				class="inline-flex items-center gap-1 rounded bg-primary/10 px-2 py-0.5 text-[10px] font-semibold text-primary border-0 hover:bg-primary/20 cursor-pointer transition-colors duration-150"
				title="Filtrar por esta tag"
			>
				<Tag class="size-3" />
				{project.tag}
			</button>
		</div>
	</div>

	<!-- Coluna Direita: Ações de Play e Edit -->
	<div class="w-20 border-l border-base-200 flex flex-col shrink-0">
		<!-- Botão de Reprodução (Modal DialoguePlayer) -->
		<button
			type="button"
			onclick={(e) => {
				e.stopPropagation();
				onPlay?.(e);
			}}
			class="grow w-full flex items-center justify-center hover:bg-success/15 text-base-content/60 hover:text-success border-0 bg-transparent transition-all duration-200 cursor-pointer"
			title="Iniciar reprodução"
		>
			<div
				class="flex h-10 w-10 items-center justify-center rounded-full border-2 border-current transition-transform duration-200 hover:scale-105"
			>
				<Play class="size-4 fill-current ml-0.5" />
			</div>
		</button>

		<!-- Divisor e Botão de Edição (Wrench) -->
		<button
			type="button"
			onclick={(e) => {
				e.stopPropagation();
				onEdit?.(e);
			}}
			class="h-12 w-full flex items-center justify-center hover:bg-primary/15 text-base-content/60 hover:text-primary border-t border-base-200 bg-transparent transition-colors duration-200 cursor-pointer"
			title="Editar diálogo"
		>
			<Wrench class="size-4.5" />
		</button>
	</div>
</article>
