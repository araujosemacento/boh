<script lang="ts">
	import { Play, Square, RotateCcw } from '@lucide/svelte';
	import type { DialogueNode } from '../types';
	import { prepareWithSegments, layoutWithLines, measureNaturalWidth } from '@chenglou/pretext';
	import { onDestroy } from 'svelte';

	let {
		nodes,
		terminalWidth = $bindable(480),
		// eslint-disable-next-line no-useless-assignment
		// eslint-disable-next-line no-useless-assignment
		activePlayNodeId = $bindable(null),
		selectedNodeIds = []
	}: {
		nodes: Record<string, DialogueNode>;
		terminalWidth?: number;
		activePlayNodeId?: string | null;
		selectedNodeIds?: string[];
	} = $props();

	// Expressões fiéis ao Boh.py original
	const bohExpressions: Record<string, string[]> = {
		idle: [
			'[ ▀ ¸ ▀]',
			'[ ▀ ° ▀]',
			'[ ▀ ■ ▀]',
			'[ ▀ ─ ▀]',
			'[ ▀ ~ ▀]',
			'[ ▀ ¬ ▀]',
			'[ ▀ · ▀]',
			'[ ▀ _ ▀]'
		],
		pokerface: ['[ ▀ ‗ ▀]', '[ ▀ ─ ▀]', '[ ▀ ¯ ▀]', '[ ▀ ¡ ▀]'],
		thinking: ['[ ─ ´ ─]', '[ ─ » ─]', '[ ─ ^ ─]', '[ ─ ~ ─]', '[ ─ ¬ ─]', '[ ─ · ─]'],
		'open mouth': ['[ ▀ o ▀]', '[ ▀ ß ▀]', '[ ▀ █ ▀]', '[ ▀ ° ▀]', '[ ▀ ■ ▀]'],
		annoyed: ['[ ▀ ı ▀]', '[ ▀ ^ ▀]', '[ ▀ ~ ▀]', '[ ▀ ¬ ▀]', '[ ▀ ß ▀]'],
		'looking down': ['[ ▄ . ▄]', '[ ▄ _ ▄]', '[ ▄ ₒ ▄]', '[ ▄ ₗ ▄]', '[ ▄ ‗ ▄]']
	};

	let isPlaying = $state(false);
	let statusState = $state<'idle' | 'playing' | 'paused' | 'finished'>('idle');
	let currentFace = $state('[ ▀ ─ ▀]');
	let currentBubbleText = $state('');

	// Histórico de navegação por teclado
	let visitedNodeIds = $state<string[]>([]);
	let historyIndex = $state(-1);

	let systemMessage = $state<string | null>(null);
	let errorMessage = $state<string | null>(null);

	let typingInterval: ReturnType<typeof setInterval> | null = null;
	let nextNodeTimeout: ReturnType<typeof setTimeout> | null = null;
	let isTyping = $state(false);

	let prevSelectedNodeIds = $state<string>('');
	let playbackSequence = $state<string[]>([]);

	$effect(() => {
		const curr = JSON.stringify(selectedNodeIds);
		if (curr !== prevSelectedNodeIds) {
			prevSelectedNodeIds = curr;
			if (statusState !== 'playing') {
				visitedNodeIds = [];
				historyIndex = -1;
				statusState = 'idle';
				systemMessage = null;
				errorMessage = null;
			}
		}
	});

	const computePlaybackSequence = (sIds: string[]): string[] => {
		const idSet = new Set(sIds);
		const chains: string[][] = [];
		const visited = new Set<string>();

		for (const id of sIds) {
			if (visited.has(id)) continue;
			
			let current = id;
			let prev = Object.values(nodes).find(n => n.nextId === current && idSet.has(n.id));
			while (prev) {
				current = prev.id;
				prev = Object.values(nodes).find(n => n.nextId === current && idSet.has(n.id));
			}

			const chain: string[] = [];
			let walk: string | undefined = current;
			while (walk && idSet.has(walk) && !visited.has(walk)) {
				chain.push(walk);
				visited.add(walk);
				walk = nodes[walk]?.nextId;
			}
			chains.push(chain);
		}

		chains.sort((a, b) => {
			const headA = nodes[a[0]];
			const headB = nodes[b[0]];
			if (!headA || !headB) return 0;
			
			if (Math.abs(headA.y - headB.y) > 100) {
				return headA.y - headB.y;
			}
			return headA.x - headB.x;
		});

		return chains.flat();
	};

	const getSequence = () => {
		if (selectedNodeIds.length > 0) {
			return computePlaybackSequence(selectedNodeIds);
		}
		
		const seq = [];
		let current = nodes['start']?.nextId;
		const visited = new Set();
		while (current && nodes[current] && !visited.has(current)) {
			seq.push(current);
			visited.add(current);
			current = nodes[current].nextId;
		}
		return seq;
	};

	const choice = <T,>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)];

	const cleanupTimers = () => {
		if (typingInterval) {
			clearInterval(typingInterval);
			typingInterval = null;
		}
		if (nextNodeTimeout) {
			clearTimeout(nextNodeTimeout);
			nextNodeTimeout = null;
		}
		isTyping = false;
	};

	// Helper para calcular a largura máxima em caracteres
	const getMaxChars = (currentTerminalWidth: number): number => {
		if (typeof window === 'undefined') return 40;
		const availWidth = Math.max(100, currentTerminalWidth - 74);

		const font = '13px ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace';
		let charWidth = 8;
		try {
			const preparedChar = prepareWithSegments('A', font);
			charWidth = measureNaturalWidth(preparedChar) || 8;
		} catch {
			// ignore
		}

		const prefixChars = 13;
		const suffixChars = 2;

		return Math.max(10, Math.floor(availWidth / charWidth) - (prefixChars + suffixChars));
	};

	// Helper para quebrar linhas de diálogo de forma responsiva com pretext
	const wrapDialogueText = (text: string, currentTerminalWidth: number): string[] => {
		if (typeof window === 'undefined' || !text) return [text || ''];

		const availWidth = Math.max(100, currentTerminalWidth - 74);

		const font = '13px ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace';
		let charWidth = 8;
		try {
			const preparedChar = prepareWithSegments('A', font);
			charWidth = measureNaturalWidth(preparedChar) || 8;
		} catch {
			// ignore
		}

		const prefixChars = 13;
		const suffixChars = 2;

		const textMaxWidth = Math.max(50, availWidth - (prefixChars + suffixChars) * charWidth);

		try {
			const preparedText = prepareWithSegments(text, font);
			const layoutResult = layoutWithLines(preparedText, textMaxWidth, 16);
			return layoutResult.lines.map((line) => line.text);
		} catch (e) {
			console.error('Erro no wrapping do pretext:', e);
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
		} catch {
			// ignore
		}
	};

	// Função para renderizar partes coloridas/formatadas do texto de forma segura e estilosa
	const parseTextParts = (text: string) => {
		if (!text) return [];

		const regex = /(‹|›|«|»|\[Sim\]|\[Não\]|\[[A-Za-z]+\])/gi;
		const parts = text.split(regex);

		return parts.map((part) => {
			if (part === '‹' || part === '›') {
				return { text: part, class: 'text-[#fb923c] font-bold' }; // laranja
			}
			if (part === '«' || part === '»') {
				return { text: part, class: 'text-[#38bdf8] font-bold' }; // azul celeste
			}
			if (part.startsWith('[') && part.endsWith(']')) {
				const content = part.slice(1, -1);
				if (content.toLowerCase() === 's' || content.toLowerCase() === 'sim') {
					return { text: part, class: 'text-[#34d399] font-extrabold' }; // verde esmeralda
				}
				if (content.toLowerCase() === 'n' || content.toLowerCase() === 'não') {
					return { text: part, class: 'text-[#fb7185] font-extrabold' }; // rosa avermelhado
				}
				return { text: part, class: 'text-[#c084fc] font-bold' }; // roxo
			}
			return { text: part, class: '' };
		});
	};

	const scheduleNextNode = (node: DialogueNode) => {
		nextNodeTimeout = setTimeout(() => {
			if (!isPlaying) return;

			const currentIndex = playbackSequence.indexOf(node.id);
			const nextId = currentIndex >= 0 ? playbackSequence[currentIndex + 1] : null;

			if (nextId && nodes[nextId]) {
				visitedNodeIds = [...visitedNodeIds, nextId];
				historyIndex = visitedNodeIds.length - 1;
				playNode(nextId, true);
			} else {
				finishPlayback();
			}
		}, 800);
	};

	const playNode = (nodeId: string, animate: boolean = true) => {
		cleanupTimers();

		const node = nodes[nodeId];
		if (!node) return;

		activePlayNodeId = nodeId;
		const exprList = bohExpressions[node.expression] ?? bohExpressions.idle;

		if (!animate) {
			currentBubbleText = node.text;
			currentFace = exprList[exprList.length - 1] ?? '[ ▀ ─ ▀]';
			isTyping = false;

			if (isPlaying) {
				scheduleNextNode(node);
			}
			return;
		}

		isTyping = true;
		currentBubbleText = '';
		currentFace = exprList[0] ?? '[ ▀ ─ ▀]';
		let charIdx = 0;

		typingInterval = setInterval(() => {
			if (charIdx < node.text.length) {
				currentFace =
					exprList[
						Math.floor(charIdx / Math.max(1, Math.floor(exprList.length / 2))) % exprList.length
					];
				currentBubbleText += node.text[charIdx];

				if (node.text[charIdx].match(/[a-zA-Z0-9]/)) {
					playTypingSound();
				}
				charIdx++;
			} else {
				cleanupTimers();
				if (isPlaying) {
					scheduleNextNode(node);
				}
			}
		}, 35);
	};

	const startPlaying = () => {
		cleanupTimers();

		let targetNodeId: string | null = null;

		if (historyIndex >= 0 && historyIndex < visitedNodeIds.length && statusState !== 'finished') {
			targetNodeId = visitedNodeIds[historyIndex];
		} else {
			if (statusState === 'finished') {
				visitedNodeIds = [];
				historyIndex = -1;
				currentBubbleText = '';
			}
			playbackSequence = getSequence();
			targetNodeId = playbackSequence[0] || null;
		}

		if (!targetNodeId || !nodes[targetNodeId]) {
			errorMessage = 'Erro: Selecione um nó de diálogo ou conecte o nó Start!';
			isPlaying = false;
			statusState = 'idle';
			return;
		}

		isPlaying = true;
		statusState = 'playing';
		systemMessage = null;
		errorMessage = null;

		if (visitedNodeIds.length === 0) {
			visitedNodeIds = [targetNodeId];
			historyIndex = 0;
			playNode(targetNodeId, true);
		} else {
			if (currentBubbleText.length < nodes[targetNodeId].text.length) {
				playNode(targetNodeId, true);
			} else {
				scheduleNextNode(nodes[targetNodeId]);
			}
		}
	};

	const pausePlaying = () => {
		cleanupTimers();
		isPlaying = false;
		statusState = 'paused';
	};

	const togglePlayPause = () => {
		if (isPlaying) {
			pausePlaying();
		} else {
			startPlaying();
		}
	};

	const stopPlaying = () => {
		cleanupTimers();
		isPlaying = false;
		statusState = 'idle';
		visitedNodeIds = [];
		historyIndex = -1;
		currentBubbleText = '';
		currentFace = choice(bohExpressions.idle);
		systemMessage = null;
		errorMessage = null;
		activePlayNodeId = null;
	};

	const finishPlayback = () => {
		cleanupTimers();
		isPlaying = false;
		statusState = 'finished';
		systemMessage = '[Execução finalizada.]';
		activePlayNodeId = null;
		currentFace = choice(bohExpressions.idle);
	};

	const navigateBack = () => {
		if (visitedNodeIds.length === 0) return;

		pausePlaying();

		if (historyIndex > 0) {
			historyIndex--;
			const nodeId = visitedNodeIds[historyIndex];
			playNode(nodeId, false);
		}
	};

	const navigateForward = () => {
		if (visitedNodeIds.length === 0) return;

		const currentId = visitedNodeIds[historyIndex];
		const node = nodes[currentId];

		if (isTyping) {
			cleanupTimers();
			currentBubbleText = node.text;
			const exprList = bohExpressions[node.expression] ?? bohExpressions.idle;
			currentFace = exprList[exprList.length - 1] ?? '[ ▀ ─ ▀]';
			isTyping = false;

			if (isPlaying) {
				scheduleNextNode(node);
			}
			return;
		}

		if (historyIndex < visitedNodeIds.length - 1) {
			historyIndex++;
			const nextId = visitedNodeIds[historyIndex];
			playNode(nextId, false);
		} else {
			const currentIndex = playbackSequence.indexOf(node.id);
			const nextId = currentIndex >= 0 ? playbackSequence[currentIndex + 1] : null;

			if (nextId && nodes[nextId]) {
				visitedNodeIds = [...visitedNodeIds, nextId];
				historyIndex = visitedNodeIds.length - 1;
				playNode(nextId, isPlaying);
			} else {
				finishPlayback();
			}
		}
	};

	function handleKeydown(e: KeyboardEvent) {
		const target = e.target as HTMLElement;
		if (target.closest('input') || target.closest('textarea') || target.closest('select')) {
			return;
		}

		if (!isPlaying && visitedNodeIds.length === 0 && e.key !== ' ') return;

		if (e.key === 'ArrowLeft') {
			e.preventDefault();
			navigateBack();
		} else if (e.key === 'ArrowRight') {
			e.preventDefault();
			navigateForward();
		} else if (e.key === ' ') {
			e.preventDefault();
			togglePlayPause();
		}
	}

	$effect(() => {
		if (typeof window !== 'undefined') {
			window.addEventListener('keydown', handleKeydown);
			return () => {
				window.removeEventListener('keydown', handleKeydown);
			};
		}
	});

	onDestroy(() => {
		cleanupTimers();
	});
</script>

<!-- Painel de Título -->
<div class="p-4 border-b border-base-200 flex items-center justify-between shrink-0">
	<div>
		<h3 class="text-sm font-bold tracking-wider text-base-content/60 uppercase">Simulação</h3>
		<p class="text-xs text-base-content/50 mt-1">Reproduza e teste o diálogo</p>
	</div>
	<div class="flex items-center gap-2">
		{#if statusState !== 'idle'}
			<button
				onclick={stopPlaying}
				class="btn btn-sm btn-ghost gap-1 px-3 border border-base-300 font-semibold"
				title="Reiniciar player"
			>
				<RotateCcw class="size-4" /> Reiniciar
			</button>
		{/if}
		{#if isPlaying}
			<button
				onclick={pausePlaying}
				class="btn btn-sm btn-error gap-1 px-3.5 font-bold shadow-lg shadow-error/10"
			>
				<Square class="size-4 fill-current" /> Pausar
			</button>
		{:else}
			<button
				onclick={startPlaying}
				class="btn btn-sm btn-success gap-1 px-3.5 text-base-100 font-bold shadow-lg shadow-success/15"
			>
				<Play class="size-4 fill-current" /> Play
			</button>
		{/if}
	</div>
</div>

<!-- Terminal macOS -->
<div class="flex-1 p-4 flex flex-col min-h-0 bg-base-200/30">
	<div
		class="flex-1 bg-[#0c0f16] text-[#e2e8f0] rounded-lg border border-[#1e2230] flex flex-col min-h-0 shadow-xl overflow-hidden"
	>
		<!-- macOS Title Bar -->
		<div
			class="bg-[#161925] px-4 py-3 flex items-center gap-2 relative border-b border-[#0c0f16] shrink-0"
		>
			<div class="flex items-center gap-1.5 z-10">
				<div class="w-3 h-3 rounded-full bg-error opacity-80"></div>
				<div class="w-3 h-3 rounded-full bg-warning opacity-80"></div>
				<div class="w-3 h-3 rounded-full bg-success opacity-80"></div>
			</div>
			<div
				class="w-full text-center text-[10px] tracking-wide font-mono text-[#8a91a5] uppercase font-bold absolute left-0 pr-4"
			>
				Boh.py Terminal
			</div>
			<!-- Status Indicator -->
			<div
				class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-1.5 z-10 font-mono text-[9px] font-bold tracking-wider"
			>
				{#if statusState === 'playing'}
					<span class="flex items-center gap-1 text-success">
						<span class="w-1.5 h-1.5 rounded-full bg-success animate-ping"></span>
						PLAYING
					</span>
				{:else if statusState === 'paused'}
					<span class="flex items-center gap-1 text-warning">
						<span class="w-1.5 h-1.5 rounded-full bg-warning"></span>
						PAUSED
					</span>
				{:else if statusState === 'finished'}
					<span class="flex items-center gap-1 text-slate-400">
						<span class="w-1.5 h-1.5 rounded-full bg-slate-400"></span>
						FINISHED
					</span>
				{:else}
					<span class="flex items-center gap-1 text-slate-500">
						<span class="w-1.5 h-1.5 rounded-full bg-slate-600"></span>
						IDLE
					</span>
				{/if}
			</div>
		</div>

		<!-- Terminal Body -->
		<div
			class="px-5 py-4 flex-1 font-mono text-[13px] flex flex-col justify-start gap-1 overflow-y-auto min-h-0 select-text"
		>
			{#if systemMessage}
				<div class="text-[#38bdf8] text-[11px] font-semibold py-1">{systemMessage}</div>
			{/if}
			{#if errorMessage}
				<div class="text-[#f87171] font-bold py-1">{errorMessage}</div>
			{/if}

			{#if visitedNodeIds.length > 0 && historyIndex >= 0 && historyIndex < visitedNodeIds.length}
				{@const maxChars = getMaxChars(terminalWidth)}
				{@const activeLines = wrapDialogueText(currentBubbleText, terminalWidth)}
				<div
					class="grid grid-cols-[auto_1fr] gap-0 font-mono text-[13px] select-text whitespace-pre"
				>
					{#each activeLines as line, i}
						{@const paddedLine = line.padEnd(maxChars, ' ')}
						{#if i === 0}
							<span class="text-[#4ade80] font-semibold select-none">{currentFace} ──┤</span>
						{:else}
							<span class="text-[#4ade80] font-semibold text-right select-none">│</span>
						{/if}
						<div class="flex items-baseline gap-0">
							<span class="text-[#4ade80] font-semibold shrink-0"> </span>
							<span class="text-[#f8fafc]">
								{#each parseTextParts(paddedLine) as part}
									<span class={part.class}>{part.text}</span>
								{/each}
							</span>
							<span
								class="text-[#4ade80] font-semibold"
								class:blink-cursor={isTyping && i === activeLines.length - 1}>│</span
							>
						</div>
					{/each}
				</div>
			{:else if statusState === 'idle' && !systemMessage}
				<div class="flex items-baseline gap-0 py-0.5 whitespace-pre">
					<span class="text-[#4ade80]/50 shrink-0">{currentFace} ──┤</span>
					<span class="ml-1 italic text-slate-400">Aperte Play ou Espaço para iniciar...</span>
					<span class="text-[#4ade80]/50"> │</span>
				</div>
			{/if}
		</div>

		<!-- Terminal Footer with Shortcuts -->
		{#if statusState !== 'idle'}
			<div
				class="px-4 py-1.5 bg-[#0a0c12] border-t border-[#131722] flex items-center justify-between text-[10px] font-mono text-[#4b526d] select-none shrink-0"
			>
				<div class="flex gap-4">
					<span><span class="text-[#fb923c] font-bold">←</span> anterior</span>
					<span><span class="text-[#38bdf8] font-bold">→</span> avançar/pular</span>
				</div>
				<div>
					<span>[Espaço] Play/Pause</span>
				</div>
			</div>
		{/if}
	</div>
</div>

<style>
	/* Cursor piscante da barra do balão */
	.blink-cursor {
		animation: blink 0.8s step-end infinite;
	}
	@keyframes blink {
		50% {
			opacity: 0;
		}
	}
</style>
