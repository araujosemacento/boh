<script lang="ts">
	let { hasSelection = false }: { hasSelection?: boolean } = $props();

	let mascotContainer = $state<HTMLDivElement | null>(null);
	let leftEyeEl = $state<HTMLSpanElement | null>(null);
	let rightEyeEl = $state<HTMLSpanElement | null>(null);

	// Offsets reativos finais para renderização
	let leftEyeOffset = $state({ x: 0, y: 0 });
	let rightEyeOffset = $state({ x: 0, y: 0 });

	// Alvos de posicionamento (setados pelo mouse)
	let targetLeft = { x: 0, y: 0 };
	let targetRight = { x: 0, y: 0 };

	let isHoveringBoh = $state(false);
	let isHoveringInteractive = $state(false);

	// Expressão reativa da boca
	const mouthChar = $derived.by(() => {
		if (isHoveringBoh) return '¯';
		if (isHoveringInteractive) return '·';
		if (hasSelection) return '■';
		return '─';
	});

	// Rastreia o movimento do mouse e calcula os offsets para cada olho de forma independente
	function handleGlobalMouseMove(e: MouseEvent) {
		const maxOffset = 8; // Movimento proporcional ao tamanho reduzido do widget

		// Cálculo do olho esquerdo
		if (leftEyeEl) {
			const rect = leftEyeEl.getBoundingClientRect();
			const centerX = rect.left + rect.width / 2;
			const centerY = rect.top + rect.height / 2;
			const dx = e.clientX - centerX;
			const dy = e.clientY - centerY;
			const distance = Math.hypot(dx, dy);

			if (distance === 0) {
				targetLeft = { x: 0, y: 0 };
			} else {
				// Easing cúbico para desacelerar com a distância
				const factor = Math.min(distance / 180, 1);
				const eased = 1 - Math.pow(1 - factor, 3);
				targetLeft = {
					x: (dx / distance) * maxOffset * eased,
					y: (dy / distance) * maxOffset * eased
				};
			}
		}

		// Cálculo do olho direito
		if (rightEyeEl) {
			const rect = rightEyeEl.getBoundingClientRect();
			const centerX = rect.left + rect.width / 2;
			const centerY = rect.top + rect.height / 2;
			const dx = e.clientX - centerX;
			const dy = e.clientY - centerY;
			const distance = Math.hypot(dx, dy);

			if (distance === 0) {
				targetRight = { x: 0, y: 0 };
			} else {
				const factor = Math.min(distance / 180, 1);
				const eased = 1 - Math.pow(1 - factor, 3);
				targetRight = {
					x: (dx / distance) * maxOffset * eased,
					y: (dy / distance) * maxOffset * eased
				};
			}
		}

		// Detecta se o mouse está sobre elementos interativos
		const target = e.target as HTMLElement;
		if (target) {
			isHoveringInteractive = !!(
				target.closest('button, a, input, select, textarea, [role="button"], .project-card-btn') ||
				window.getComputedStyle(target).cursor === 'pointer'
			);
		}
	}

	// Ciclo de interpolação suave (RequestAnimationFrame)
	$effect(() => {
		let animationFrameId: number;
		let curLeftX = 0;
		let curLeftY = 0;
		let curRightX = 0;
		let curRightY = 0;

		const easeFactor = 0.12;

		function animate() {
			curLeftX += (targetLeft.x - curLeftX) * easeFactor;
			curLeftY += (targetLeft.y - curLeftY) * easeFactor;
			curRightX += (targetRight.x - curRightX) * easeFactor;
			curRightY += (targetRight.y - curRightY) * easeFactor;

			leftEyeOffset = { x: curLeftX, y: curLeftY };
			rightEyeOffset = { x: curRightX, y: curRightY };

			animationFrameId = requestAnimationFrame(animate);
		}

		animate();

		return () => {
			cancelAnimationFrame(animationFrameId);
		};
	});
</script>

<svelte:window onmousemove={handleGlobalMouseMove} />

<!-- Container do Mascote Fixo e Monocromático (Sem painel de fundo, tamanho reduzido) -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	bind:this={mascotContainer}
	onmouseenter={() => (isHoveringBoh = true)}
	onmouseleave={() => (isHoveringBoh = false)}
	class="fixed bottom-6 left-6 z-50 flex items-center justify-center select-none bg-transparent p-3 transition-all duration-300 hover:scale-[1.04]"
	title="Este é o Boh!"
>
	<div
		class="flex items-center gap-3.5 text-2xl font-black font-mono tracking-widest leading-none select-none text-base-content/85"
		style="font-weight: 900; -webkit-text-stroke: 1.2px currentColor; text-shadow: 0.5px 0.5px 0px currentColor;"
	>
		<!-- Colchete Esquerdo -->
		<span class="text-base-content/30">[</span>

		<!-- Olho Esquerdo (Rastreia cursor com offset independente) -->
		<span
			bind:this={leftEyeEl}
			class="inline-block"
			style="transform: translate({leftEyeOffset.x}px, {leftEyeOffset.y}px);"
		>
			▀
		</span>

		<!-- Boca Dinâmica -->
		<span class="inline-block text-base-content/80 font-bold">
			{mouthChar}
		</span>

		<!-- Olho Direito (Rastreia cursor com offset independente) -->
		<span
			bind:this={rightEyeEl}
			class="inline-block"
			style="transform: translate({rightEyeOffset.x}px, {rightEyeOffset.y}px);"
		>
			▀
		</span>

		<!-- Colchete Direito -->
		<span class="text-base-content/30">]</span>
	</div>
</div>
