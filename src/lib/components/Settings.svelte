<script lang="ts">
	import { Palette, IceCreamBowl, Soup, CupSoda, Beaker, Coffee } from '@lucide/svelte';

	// 1. Detecta o tema do sistema operacional
	const getSystemTheme = () => {
		if (typeof window === 'undefined') return 'latte';
		return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'mocha' : 'latte';
	};

	// 2. Inicializa o estado IMEDIATAMENTE, pegando o que o script do app.html JÁ INJETOU no HTML
	const getInitialTheme = () => {
		if (typeof document !== 'undefined' && document.documentElement.dataset.theme) {
			return document.documentElement.dataset.theme;
		}
		return getSystemTheme();
	};

	let theme: string = $state(getInitialTheme());

	// Controla se o usuário está usando o tema do sistema ou um fixo
	let isUsingSystemDefault = $state(
		typeof window !== 'undefined' ? !localStorage.getItem('preferred-theme') : true
	);

	// 3. Atualiza o DOM e o localStorage reativamente
	$effect(() => {
		if (typeof document !== 'undefined' && theme) {
			document.documentElement.dataset.theme = theme;

			if (isUsingSystemDefault) {
				localStorage.removeItem('preferred-theme');
			} else {
				localStorage.setItem('preferred-theme', theme);
			}
		}
	});

	// 4. Ouve mudanças no sistema operacional em tempo real (caso o usuário mude o Windows de dark para light)
	$effect(() => {
		if (typeof window === 'undefined') return;

		const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
		const handleChange = () => {
			if (isUsingSystemDefault) {
				theme = getSystemTheme();
			}
		};

		mediaQuery.addEventListener('change', handleChange);
		return () => mediaQuery.removeEventListener('change', handleChange);
	});

	// Função auxiliar para o botão "Padrão"
	const setSystemDefault = () => {
		isUsingSystemDefault = true;
		theme = getSystemTheme();
	};

	const setCustomTheme = (newTheme: string) => {
		isUsingSystemDefault = false;
		theme = newTheme;
	};
</script>

<div class="dropdown dropdown-top dropdown-end fixed bottom-4 right-4 z-50">
	<div tabindex="0" role="button" class="btn btn-secondary m-1 p-2"><Palette /></div>
	<ul
		tabindex="-1"
		class="dropdown-content bg-base-100 text-base-content border border-base-200 rounded-lg w-40 z-1 p-2 shadow-xl"
	>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={isUsingSystemDefault}
				class:text-base-100={isUsingSystemDefault}
				onclick={setSystemDefault}
			>
				<IceCreamBowl />
				<span class="ml-2">Padrão</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={!isUsingSystemDefault && theme === 'latte'}
				class:text-base-100={!isUsingSystemDefault && theme === 'latte'}
				onclick={() => setCustomTheme('latte')}
			>
				<Soup />
				<span class="ml-2">Latte</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={!isUsingSystemDefault && theme === 'frappe'}
				class:text-base-100={!isUsingSystemDefault && theme === 'frappe'}
				onclick={() => setCustomTheme('frappe')}
			>
				<CupSoda />
				<span class="ml-2">Frappé</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={!isUsingSystemDefault && theme === 'macchiato'}
				class:text-base-100={!isUsingSystemDefault && theme === 'macchiato'}
				onclick={() => setCustomTheme('macchiato')}
			>
				<Beaker />
				<span class="ml-2">Macchiato</span>
			</button>
		</li>
		<li>
			<button
				type="button"
				class="btn btn-sm btn-block btn-ghost justify-start theme-controller duration-300"
				class:bg-primary={!isUsingSystemDefault && theme === 'mocha'}
				class:text-base-100={!isUsingSystemDefault && theme === 'mocha'}
				onclick={() => setCustomTheme('mocha')}
			>
				<Coffee />
				<span class="ml-2">Mocha</span>
			</button>
		</li>
	</ul>
</div>
