<script lang="ts">
	import type { Project } from '$lib/content/project';
	import { StoryEngine } from '$lib/engine/storyEngine.svelte';
	import { characters } from '$lib/content/characters';
	import { ArrowLeft, Pause, Play, SkipForward, Home } from 'lucide-svelte';

	interface Props {
		project: Project;
		onClose: () => void;
	}

	let { project, onClose }: Props = $props();

	const engine = new StoryEngine(project.chapter);

	// Initialize engine when component mounts
	$effect(() => {
		engine.init();
		return () => {
			// Cleanup if needed
		};
	});

	function getExpressionFrame(): string {
		const char = characters[engine.speaker];
		if (!char) return '';
		const expr = char.expressions[engine.expression];
		if (!expr || expr.frames.length === 0) return char.defaultArt;
		const frameIndex = Math.floor(Date.now() / 500) % Math.max(1, expr.frames.length);
		return expr.frames[frameIndex] || char.defaultArt;
	}

	function handleClick() {
		engine.advance();
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === ' ' || event.key === 'Enter' || event.key === 'ArrowRight' || event.key === ' ') {
			event.preventDefault();
			engine.advance();
		}
		if (event.key === 'Escape') {
			onClose();
		}
	}
</script>

<svelte:window onkeydown={handleKeydown} />

<div class="fixed inset-0 z-50 flex items-center justify-center" style:background-color="var(--ctp-crust)">
	<!-- Terminal Window -->
	<div
		class="flex flex-col overflow-hidden rounded-lg shadow-2xl"
		style:background-color="var(--ctp-base)"
		style:border="1px solid var(--ctp-surface0)"
	>
		<!-- Title Bar -->
		<div
			class="flex items-center justify-between px-4 py-2"
			style:background-color="var(--ctp-mantle)"
			style:border-bottom="1px solid var(--ctp-surface0)"
		>
			<button
				onclick={onClose}
				class="flex items-center gap-1.5 rounded px-2 py-1 text-xs transition-colors hover:bg-white/10"
				style:color="var(--ctp-red)"
				title="Voltar"
			>
				<Home class="h-4 w-4" />
				<span style:color="var(--ctp-subtext0)">Fechar</span>
			</button>
			<span class="text-sm font-mono" style:color="var(--ctp-subtext0)">{project.title}</span>
			<div class="flex items-center gap-2">
				<button
					class="rounded p-1 transition-colors hover:bg-white/10"
					style:color="var(--ctp-yellow)"
					title="Minimizar"
					aria-label="Minimizar"
					onclick={() => alert('Funcao ainda nao implementada')}
				>
					<ArrowLeft class="h-4 w-4" />
				</button>
				<button
					class="rounded p-1 transition-colors hover:bg-white/10"
					style:color="var(--ctp-green)"
					title="Maximizar"
					aria-label="Maximizar"
					onclick={() => alert('Funcao ainda nao implementada')}
				>
					<Pause class="h-4 w-4" />
				</button>
				<button
					class="rounded p-1 transition-colors hover:bg-white/10"
					style:color="var(--ctp-red)"
					title="Fechar"
					aria-label="Fechar"
					onclick={onClose}
				>
					<Home class="h-4 w-4" />
				</button>
			</div>
		</div>

		<!-- Terminal Content -->
		<div
			class="flex flex-1 flex-col gap-4 p-6 font-mono cursor-pointer"
			onclick={handleClick}
			role="button"
			tabindex="0"
			aria-label="Avancar dialogo"
		>
			<!-- Character and Dialogue Area -->
			<div class="flex flex-1 flex-col gap-4">
				{#if engine.speaker && characters[engine.speaker]}
					<div class="flex items-start gap-4">
						<pre class="text-lg leading-none whitespace-pre" style:color="var(--ctp-green)">{getExpressionFrame()}</pre>
						<div class="flex-1">
							<span class="font-bold" style:color="var(--ctp-green)">{characters[engine.speaker].name}:</span>
						</div>
					</div>
				{/if}

				<!-- Typed Text -->
				<div class="min-h-[2rem] text-lg" style:color="var(--ctp-text)">
					{#if engine.typedText}
						{engine.typedText}<span class="animate-pulse" style:color="var(--ctp-overlay0)">_</span>
					{:else if engine.isTyping}
						<span class="animate-pulse" style:color="var(--ctp-overlay0)">_</span>
					{/if}
				</div>

				<!-- Static Art -->
				{#if engine.staticArt}
					<pre class="whitespace-pre" style:color="var(--ctp-subtext0)">{engine.staticArt}</pre>
				{/if}
			</div>

			<!-- User Input / Choices -->
			{#if engine.isWaitingForInput && engine.currentStep?.type === 'choice'}
				{@const step = engine.currentStep}
				<div class="flex gap-3">
					{#each step.choices as choice}
						<button
							onclick={() => engine.selectChoice(choice.targetId)}
							class="rounded border px-4 py-2 text-sm transition-colors duration-200 hover:opacity-80"
							style:border-color="var(--ctp-surface0)"
							style:background-color="var(--ctp-surface0)"
							style:color="var(--ctp-green)"
						>
							[{choice.key.toUpperCase()}] {choice.label}
						</button>
					{/each}
				</div>
			{/if}

			<!-- Prompt hint -->
			<div class="mt-4 text-center text-xs" style:color="var(--ctp-overlay0)">
				{#if engine.isTyping}
					Clique ou pressione Enter para pular
				{:else if engine.isWaitingForInput}
					Escolha uma opcao
				{:else}
					Clique ou pressione Enter para continuar
				{/if}
			</div>
		</div>
	</div>
</div>

<style>
	/* Terminal sizing */
	:global(.terminal-window) {
		width: 90vw;
		max-width: 64rem;
		height: 90vh;
		max-height: 48rem;
	}
</style>
