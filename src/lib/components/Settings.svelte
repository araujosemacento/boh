<script lang="ts">
	import { Palette, IceCreamBowl, Soup, CupSoda, Beaker, Coffee } from '@lucide/svelte';
	import { onMount } from 'svelte';

	let theme: string = $state(''); // will be set on mount

	const defaultTheme = $derived(
		typeof window === 'undefined'
			? ''
			: window.matchMedia('(prefers-color-scheme: dark)').matches
				? 'mocha'
				: 'latte'
	);

	// Detecção de preferência de cor movida para o onMount abaixo

	// Sincroniza data‑theme no <html> e persiste a escolha
	$effect(() => {
		if (typeof document !== 'undefined' && theme) {
			document.documentElement.dataset.theme = theme;

			try {
				localStorage.setItem('preferred-theme', theme);
			} catch {
				/* empty */
			}
		}
	});

	// Carrega tema salvo ao montar o componente
	onMount(() => {
		if (typeof window !== 'undefined') {
			const saved = localStorage.getItem('preferred-theme');
			if (saved) {
				theme = saved;
			} else {
				theme = defaultTheme;
			}
		}
	});
</script>

<div class="dropdown dropdown-top dropdown-end fixed bottom-4 right-4 z-50">
	<div tabindex="0" role="button" class="btn btn-secondary m-1 p-2 rounded-full"><Palette /></div>
	<ul
		tabindex="-1"
		class="dropdown-content bg-base-300 text-base-content rounded-box w-40 z-1 p-2 shadow-2xl"
	>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={theme === 'default'}
				class:text-base-100={theme === 'default'}
				onclick={() => (theme = 'default')}
			>
				<IceCreamBowl />
				<span class="ml-2">Padrão</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={theme === 'latte'}
				class:text-base-100={theme === 'latte'}
				onclick={() => (theme = 'latte')}
			>
				<Soup />
				<span class="ml-2">Latte</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={theme === 'frappe'}
				class:text-base-100={theme === 'frappe'}
				onclick={() => (theme = 'frappe')}
			>
				<CupSoda />
				<span class="ml-2">Frappé</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={theme === 'macchiato'}
				class:text-base-100={theme === 'macchiato'}
				onclick={() => (theme = 'macchiato')}
			>
				<Beaker />
				<span class="ml-2">Macchiato</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={theme === 'mocha'}
				class:text-base-100={theme === 'mocha'}
				onclick={() => (theme = 'mocha')}
			>
				<Coffee />
				<span class="ml-2">Mocha</span>
			</button>
		</li>
	</ul>
</div>
