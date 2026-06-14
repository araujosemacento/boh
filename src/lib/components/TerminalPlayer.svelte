<script lang="ts">
	import { onMount } from 'svelte';
	import type { Project } from '$lib/content/project';
	import { StoryEngine } from '$lib/engine/storyEngine.svelte';
	import { characters } from '$lib/content/characters';

	interface Props {
		project: Project;
		onClose: () => void;
	}

	let { project, onClose }: Props = $props();

	const engine = new StoryEngine(project.chapter);

	onMount(() => {
		engine.init();
	});

	function getExpressionFrame(): string {
		const char = characters[engine.speaker];
		if (!char) return '';
		const expr = char.expressions[engine.expression];
		if (!expr || expr.frames.length === 0) return char.defaultArt;
		// Simple animation: pick frame based on time
		const frameIndex = Math.floor(Date.now() / 500) % expr.frames.length;
		return expr.frames[frameIndex] || char.defaultArt;
	}

	function handleClick() {
		engine.advance();
	}

	function handleChoice(targetId: string) {
		engine.selectChoice(targetId);
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === ' ' || event.key === 'Enter') {
			event.preventDefault();
			engine.advance();
		}
	}

	let typedLength = $derived(engine.typedText.length);
</script>

<svelte:window onkeydown={handleKeydown} />

<div class="fixed inset-0 z-50 flex items-center justify-center bg-black">
	<!-- Terminal Window -->
	<div class="flex h-[90vh] w-[90vw] max-w-4xl flex-col overflow-hidden rounded-lg border border-gray-600 bg-gray-900 shadow-2xl">
		<!-- Title Bar -->
		<div class="flex items-center justify-between bg-gray-800 px-4 py-2">
			<div class="flex items-center gap-2">
				<div class="h-3 w-3 rounded-full bg-red-500"></div>
				<div class="h-3 w-3 rounded-full bg-yellow-500"></div>
				<div class="h-3 w-3 rounded-full bg-green-500"></div>
			</div>
			<span class="text-sm font-mono text-gray-400">{project.title} - boh.exe</span>
			<button
				onclick={onClose}
				class="rounded px-2 py-0.5 text-sm text-gray-400 hover:bg-gray-700 hover:text-white"
				title="Voltar"
			>
				×
			</button>
		</div>

		<!-- Terminal Content -->
		<div
			class="flex flex-1 flex-col gap-4 p-6 font-mono"
			onclick={handleClick}
			role="button"
			tabindex="0"
		>
			<!-- Character and Dialogue Area -->
			<div class="flex flex-1 flex-col gap-4">
				{#if engine.speaker && characters[engine.speaker]}
					<div class="flex items-start gap-4">
						<pre class="text-emerald-400 text-lg leading-none whitespace-pre">{getExpressionFrame()}</pre>
						<div class="flex-1">
							<span class="font-bold text-emerald-500">{characters[engine.speaker].name}:</span>
						</div>
					</div>
				{/if}

				<!-- Typed Text -->
				<div class="min-h-8 text-gray-200 text-lg">
					{#if engine.typedText}
						{engine.typedText}<span class="animate-pulse">_</span>
					{:else if engine.isTyping}
						<span class="animate-pulse">_</span>
					{/if}
				</div>

				<!-- Static Art -->
				{#if engine.staticArt}
					<pre class="whitespace-pre text-gray-300">{engine.staticArt}</pre>
				{/if}
			</div>

			<!-- User Input / Choices -->
			{#if engine.isWaitingForInput && engine.currentStep?.type === 'choice'}
				<div class="flex gap-3">
					{#each (engine.currentStep as any).choices as choice}
						<button
							onclick={() => handleChoice(choice.targetId)}
							class="rounded border border-emerald-600 bg-emerald-900/30 px-4 py-2 text-sm text-emerald-400 transition-colors hover:bg-emerald-900/50"
						>
							[{choice.key.toUpperCase()}] {choice.label}
						</button>
					{/each}
				</div>
			{/if}

			<!-- Prompt hint -->
			<div class="mt-4 text-center text-xs text-gray-500">
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
