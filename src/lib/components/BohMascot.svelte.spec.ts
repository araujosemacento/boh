import { page } from 'vitest/browser';
import { describe, expect, it } from 'vitest';
import { render } from 'vitest-browser-svelte';
import BohMascot from './BohMascot.svelte';

describe('BohMascot.svelte', () => {
	it('should render the mascot with default mouth char', async () => {
		render(BohMascot, { hasSelection: false });

		// O mascote deve ser renderizado e o caractere padrão da boca é '─'
		await expect.element(page.getByText('[')).toBeInTheDocument();
		await expect.element(page.getByText('─')).toBeInTheDocument();
	});

	it('should render the selection mouth char when hasSelection is true', async () => {
		render(BohMascot, { hasSelection: true });

		// Quando há seleção, a boca deve mudar para '■'
		await expect.element(page.getByText('■')).toBeInTheDocument();
	});
});
