import { page } from 'vitest/browser';
import { describe, expect, it, beforeEach } from 'vitest';
import { render } from 'vitest-browser-svelte';
import Settings from './Settings.svelte';

describe('Settings.svelte', () => {
	beforeEach(() => {
		localStorage.clear();
		document.documentElement.removeAttribute('data-theme');
	});

	it('should update html data-theme and localStorage when theme button is clicked', async () => {
		render(Settings);

		// O tema padrão do HTML deve ser alterado ao clicar em Latte
		const latteBtn = page.getByRole('button', { name: 'Latte' });
		await latteBtn.click();

		expect(document.documentElement.dataset.theme).toBe('latte');
		expect(localStorage.getItem('preferred-theme')).toBe('latte');

		// Altera para Mocha
		const mochaBtn = page.getByRole('button', { name: 'Mocha' });
		await mochaBtn.click();

		expect(document.documentElement.dataset.theme).toBe('mocha');
		expect(localStorage.getItem('preferred-theme')).toBe('mocha');
	});

	it('should reset preferred-theme in localStorage when Padrão is clicked', async () => {
		localStorage.setItem('preferred-theme', 'mocha');
		render(Settings);

		// Clica em Padrão
		const defaultBtn = page.getByRole('button', { name: 'Padrão' });
		await defaultBtn.click();

		expect(localStorage.getItem('preferred-theme')).toBeNull();
	});
});
