<script lang="ts">
	import { Play, Square } from '@lucide/svelte';
	import type { DialogueNode } from '../types';
	import { prepareWithSegments, layoutWithLines, measureNaturalWidth } from '@chenglou/pretext';

	let {
		nodes,
		terminalWidth = $bindable(480),
		activePlayNodeId = $bindable(null),
		selectedNodeId = null
	}: {
		nodes: Record<string, DialogueNode>;
		terminalWidth?: number;
		activePlayNodeId?: string | null;
		selectedNodeId?: string | null;
	} = $props();

	// Expressões fiéis ao Boh.py original
	const bohExpressions: Record<string, string[]> = {
		idle: ['[ ▀ ¸ ▀]', '[ ▀ ° ▀]', '[ ▀ ■ ▀]', '[ ▀ ─ ▀]', '[ ▀ ~ ▀]', '[ ▀ ▄ ▀]', '[ ▀ ¬ ▀]', '[ ▀ · ▀]', '[ ▀ _ ▀]'],
		pokerface: ['[ ▀ ‗ ▀]', '[ ▀ ¯ ▀]', '[ ▀ ¡ ▀]'],
		thinking: ['[ ─ ´ ─]', '[ ─ » ─]'],
		'open mouth': ['[ ▀ ß ▀]', '[ ▀ █ ▀]'],
		annoyed: ['[ ▀ ı ▀]', '[ ▀ ^ ▀]'],
		'looking down': ['[ ▄ . ▄]', '[ ▄ _ ▄]', '[ ▄ ₒ ▄]', '[ ▄ ₗ ▄]']
	};

	let isPlaying = $state(false);
	let currentFace = $state('[ ▀ ° ▀]');
	let currentBubbleText = $state('');
	let terminalHistory = $state<Array<{ type: 'dialogue' | 'system' | 'error'; face?: string; text: string }>>([]);
	let charIndex = $state(0);

	const choice = <T>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)];
	const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

	// Helper para calcular a largura máxima em caracteres
	const getMaxChars = (currentTerminalWidth: number): number => {
		if (typeof window === 'undefined') return 40;
		const availWidth = Math.max(100, currentTerminalWidth - 74);
		
		const font = "13px ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace";
		let charWidth = 8;
		try {
			const preparedChar = prepareWithSegments("A", font);
			charWidth = measureNaturalWidth(preparedChar) || 8;
		} catch (e) {}
		
		const prefixChars = 14;
		const suffixChars = 2;
		
		return Math.max(10, Math.floor(availWidth / charWidth) - (prefixChars + suffixChars));
	};

	// Helper para quebrar linhas de diálogo de forma responsiva com pretext
	const wrapDialogueText = (text: string, currentTerminalWidth: number): string[] => {
		if (typeof window === 'undefined' || !text) return [text || ''];
		
		const availWidth = Math.max(100, currentTerminalWidth - 74);
		
		const font = "13px ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace";
		let charWidth = 8;
		try {
			const preparedChar = prepareWithSegments("A", font);
			charWidth = measureNaturalWidth(preparedChar) || 8;
		} catch (e) {}
		
		const prefixChars = 14;
		const suffixChars = 2;
		
		const textMaxWidth = Math.max(50, availWidth - (prefixChars + suffixChars) * charWidth);
		
		try {
			const preparedText = prepareWithSegments(text, font);
			const layoutResult = layoutWithLines(preparedText, textMaxWidth, 16);
			return layoutResult.lines.map(line => line.text);
		} catch (e) {
			console.error("Erro no wrapping do pretext:", e);
			return [text];
		}
	};

	const playTypingSound = () => {
		try {
			const ctx = new AudioContext();
			const osc = ctx.createOscillator();
			const gain = ctx.createGain();
			osc.connect(gain);
			gain.connect(ctx.destination);
			osc.type = 'sine';
			osc.frequency.setValueAtTime(choice([180, 210, 240, 270]), ctx.currentTime);
			gain.gain.setValueAtTime(0.012, ctx.currentTime);
			gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.04);
			osc.start();
			osc.stop(ctx.currentTime + 0.04);
			setTimeout(() => ctx.close(), 100);
		} catch (_) {}
	};

	const startPlaying = async () => {
		if (isPlaying) return;
		isPlaying = true;
		terminalHistory = [{ type: 'system', text: '[Iniciando emulador do Boh...]' }];
		currentBubbleText = '';
		currentFace = choice(bohExpressions.idle);

		let targetNodeId: string | null = selectedNodeId;
		if (!targetNodeId || targetNodeId === 'start') {
			targetNodeId = nodes['start']?.nextId || null;
		}

		if (!targetNodeId || !nodes[targetNodeId]) {
			terminalHistory = [...terminalHistory, { type: 'error', text: 'Erro: Selecione um nó de diálogo ou conecte o nó Start!' }];
			isPlaying = false;
			return;
		}

		await sleep(400);

		activePlayNodeId = targetNodeId;
		const node = nodes[targetNodeId];
		const exprList = bohExpressions[node.expression] ?? bohExpressions.idle;
		currentBubbleText = '';
		charIndex = 0;

		for (let i = 0; i < node.text.length; i++) {
			if (!isPlaying) break;
			charIndex = i;
			currentFace = exprList[Math.floor(i / Math.max(1, Math.floor(exprList.length / 2))) % exprList.length];
			currentBubbleText += node.text[i];

			if (node.text[i].match(/[a-zA-Z0-9]/)) {
				playTypingSound();
			}
			await sleep(35);
		}

		if (isPlaying) {
			terminalHistory = [...terminalHistory, { type: 'dialogue', face: currentFace, text: currentBubbleText }];
			currentBubbleText = '';
		}

		activePlayNodeId = null;
		isPlaying = false;
		currentFace = choice(bohExpressions.idle);
		terminalHistory = [...terminalHistory, { type: 'system', text: '[Execução finalizada.]' }];
	};

	const stopPlaying = () => {
		isPlaying = false;
		activePlayNodeId = null;
	};
</script>

<!-- Painel de Título -->
<div class="p-4 border-b border-base-200 flex items-center justify-between shrink-0">
	<div>
		<h3 class="text-sm font-bold tracking-wider text-base-content/60 uppercase">Simulação</h3>
		<p class="text-xs text-base-content/50 mt-1">Reproduza e teste o diálogo</p>
	</div>
	<div class="flex items-center gap-1.5">
		{#if isPlaying}
			<button onclick={stopPlaying} class="btn btn-sm btn-error gap-1 px-3.5 font-bold shadow-lg shadow-error/10">
				<Square class="size-4 fill-current" /> Parar
			</button>
		{:else}
			<button onclick={startPlaying} class="btn btn-sm btn-success gap-1 px-3.5 text-base-100 font-bold shadow-lg shadow-success/15">
				<Play class="size-4 fill-current" /> Play
			</button>
		{/if}
	</div>
</div>

<!-- Terminal macOS -->
<div class="flex-1 p-4 flex flex-col min-h-0 bg-base-200/30">
	<div class="flex-1 bg-[#0c0f16] text-[#e2e8f0] rounded-lg border border-[#1e2230] flex flex-col min-h-0 shadow-xl overflow-hidden">

		<!-- macOS Title Bar -->
		<div class="bg-[#161925] px-4 py-3 flex items-center gap-2 relative border-b border-[#0c0f16] shrink-0">
			<div class="flex items-center gap-1.5 z-10">
				<div class="w-3 h-3 rounded-full bg-error opacity-80"></div>
				<div class="w-3 h-3 rounded-full bg-warning opacity-80"></div>
				<div class="w-3 h-3 rounded-full bg-success opacity-80"></div>
			</div>
			<div class="w-full text-center text-[10px] tracking-wide font-mono text-[#8a91a5] uppercase font-bold absolute left-0 pr-4">
				Boh.py Terminal
			</div>
		</div>

		<!-- Terminal Body -->
		<div class="px-5 py-4 flex-1 font-mono text-[13px] flex flex-col gap-0 overflow-y-auto min-h-0 select-text">

			<!-- Histórico de diálogos finalizados -->
			{#each terminalHistory as entry}
				{#if entry.type === 'system'}
					<div class="text-[#38bdf8] text-[11px] font-semibold py-1">{entry.text}</div>
				{:else if entry.type === 'error'}
					<div class="text-[#f87171] font-bold py-1">{entry.text}</div>
				{:else}
					{@const maxChars = getMaxChars(terminalWidth)}
					{@const wrappedLines = wrapDialogueText(entry.text, terminalWidth)}
					{#each wrappedLines as line, i}
						{@const paddedLine = line.padEnd(maxChars, ' ')}
						<div class="flex items-baseline gap-0 py-0.5 text-[#f8fafc] whitespace-pre">
							{#if i === 0}
								<span class="text-[#4ade80] font-semibold shrink-0">{entry.face}{"  ──┤ "}</span>
								<span class="text-[#f8fafc]">{paddedLine}</span>
								<span class="text-[#4ade80] font-semibold">{" │"}</span>
							{:else}
								<span class="text-[#4ade80] font-semibold shrink-0">{"            │ "}</span>
								<span class="text-[#f8fafc]">{paddedLine}</span>
								<span class="text-[#4ade80] font-semibold">{" │"}</span>
							{/if}
						</div>
					{/each}
				{/if}
			{/each}

			<!-- Linha ativa sendo digitada agora -->
			{#if isPlaying && currentBubbleText}
				{@const maxChars = getMaxChars(terminalWidth)}
				{@const activeLines = wrapDialogueText(currentBubbleText, terminalWidth)}
				{#each activeLines as line, i}
					{@const paddedLine = line.padEnd(maxChars, ' ')}
					<div class="flex items-baseline gap-0 py-0.5 whitespace-pre">
						{#if i === 0}
							<span class="text-[#4ade80] font-semibold shrink-0">{currentFace}{"  ──┤ "}</span>
							<span class="text-[#f8fafc]">{paddedLine}</span>
							<span class="text-[#4ade80] font-semibold blink-cursor">{" │"}</span>
						{:else}
							<span class="text-[#4ade80] font-semibold shrink-0">{"            │ "}</span>
							<span class="text-[#f8fafc]">{paddedLine}</span>
							<span class="text-[#4ade80] font-semibold blink-cursor">{" │"}</span>
						{/if}
					</div>
				{/each}
			{:else if !isPlaying && terminalHistory.length === 0}
				<div class="flex items-baseline gap-0 py-0.5 whitespace-pre">
					<span class="text-[#4ade80]/50 shrink-0">{currentFace}{"  ──┤"}</span>
					<span class="ml-1 italic text-slate-400">Aperte Play para iniciar...</span>
					<span class="text-[#4ade80]/50">{" │"}</span>
				</div>
			{/if}

		</div>
	</div>
</div>

<style>
	/* Cursor piscante da barra do balão */
	.blink-cursor {
		animation: blink 0.8s step-end infinite;
	}
	@keyframes blink {
		50% { opacity: 0; }
	}
</style>
