import { palettes, type Flavor, type ThemeOption, STORAGE_KEY } from '$lib/themes/catppuccin';

function resolveEffectiveTheme(option: ThemeOption): Flavor {
	if (option === 'system') {
		return window.matchMedia('(prefers-color-scheme: light)').matches ? 'latte' : 'mocha';
	}
	return option;
}

function loadThemeOption(): ThemeOption {
	if (typeof window === 'undefined') return 'system';
	const stored = localStorage.getItem(STORAGE_KEY);
	if (stored === 'system' || stored === 'latte' || stored === 'frappe' || stored === 'macchiato' || stored === 'mocha') {
		return stored;
	}
	return 'system';
}

export function createThemeStore() {
	let themeOption = $state<ThemeOption>(loadThemeOption());
	let effectiveFlavor = $state<Flavor>(typeof window !== 'undefined' ? resolveEffectiveTheme(themeOption) : 'mocha');
	let isTransitioning = $state(false);

	function applyColors(flavor: Flavor) {
		const palette = palettes[flavor];
		const root = document.documentElement;
		root.style.setProperty('--ctp-base', palette.base);
		root.style.setProperty('--ctp-surface0', palette.surface0);
		root.style.setProperty('--ctp-surface1', palette.surface1);
		root.style.setProperty('--ctp-surface2', palette.surface2);
		root.style.setProperty('--ctp-text', palette.text);
		root.style.setProperty('--ctp-subtext0', palette.subtext0);
		root.style.setProperty('--ctp-subtext1', palette.subtext1);
		root.style.setProperty('--ctp-overlay0', palette.overlay0);
		root.style.setProperty('--ctp-overlay1', palette.overlay1);
		root.style.setProperty('--ctp-mantle', palette.mantle);
		root.style.setProperty('--ctp-crust', palette.crust);
		root.style.setProperty('--ctp-pink', palette.pink);
		root.style.setProperty('--ctp-mauve', palette.mauve);
		root.style.setProperty('--ctp-red', palette.red);
		root.style.setProperty('--ctp-maroon', palette.maroon);
		root.style.setProperty('--ctp-peach', palette.peach);
		root.style.setProperty('--ctp-yellow', palette.yellow);
		root.style.setProperty('--ctp-green', palette.green);
		root.style.setProperty('--ctp-teal', palette.teal);
		root.style.setProperty('--ctp-sky', palette.sky);
		root.style.setProperty('--ctp-sapphire', palette.sapphire);
		root.style.setProperty('--ctp-blue', palette.blue);
		root.style.setProperty('--ctp-lavender', palette.lavender);
	}

	function smoothTransition(targetFlavor: Flavor) {
		if (isTransitioning) return;
		isTransitioning = true;
		const root = document.documentElement;
		root.classList.add('transitioning-theme');
		window.setTimeout(() => {
			effectiveFlavor = targetFlavor;
			applyColors(targetFlavor);
			window.setTimeout(() => {
				root.classList.remove('transitioning-theme');
				isTransitioning = false;
			}, 350);
		}, 50);
	}

	function setTheme(newOption: ThemeOption) {
		if (newOption === themeOption) return;
		if (typeof window !== 'undefined') {
			localStorage.setItem(STORAGE_KEY, newOption);
		}
		themeOption = newOption;
		const target = resolveEffectiveTheme(newOption);
		smoothTransition(target);
	}

	function init() {
		if (typeof window === 'undefined') return;
		applyColors(effectiveFlavor);
		window.matchMedia('(prefers-color-scheme: light)').addEventListener('change', (e) => {
			if (themeOption === 'system') {
				const target = e.matches ? 'latte' : 'mocha';
				smoothTransition(target);
			}
		});
	}

	return {
		get themeOption() { return themeOption; },
		get effectiveFlavor() { return effectiveFlavor; },
		get isTransitioning() { return isTransitioning; },
		setTheme,
		init,
	};
}

export const themeStore = createThemeStore();
