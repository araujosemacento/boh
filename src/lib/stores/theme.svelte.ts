import { palettes, colorKeys, type Flavor, type ThemeOption, STORAGE_KEY } from '$lib/themes/catppuccin';

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
		for (const key of colorKeys) {
			root.style.setProperty(`--ctp-${key}`, palette[key]);
		}
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
