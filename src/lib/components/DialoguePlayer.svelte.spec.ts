import { page, userEvent } from 'vitest/browser';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { render } from 'vitest-browser-svelte';
import DialoguePlayer from './DialoguePlayer.svelte';

import type { DialogueNode } from '../types';

describe('DialoguePlayer.svelte', () => {
	const nodes: Record<string, DialogueNode> = {
		start: {
			id: 'start',
			type: 'start',
			x: 100,
			y: 100,
			expression: 'idle',
			text: '',
			nextId: 'node-1'
		},
		'node-1': {
			id: 'node-1',
			type: 'boh',
			x: 200,
			y: 200,
			expression: 'annoyed',
			text: 'Você concorda? [Sim] ou [Não] ‹Boh›',
			nextId: undefined
		}
	};

	beforeEach(() => {
		vi.useRealTimers();
	});

	afterEach(() => {
		vi.useRealTimers();
	});

	it('should render in idle state initially', async () => {
		render(DialoguePlayer, { nodes });

		await expect
			.element(page.getByText('Aperte Play ou Espaço para iniciar...'))
			.toBeInTheDocument();
		await expect.element(page.getByText('IDLE')).toBeInTheDocument();
	});

	it('should start playing and type text on play button click', async () => {
		render(DialoguePlayer, { nodes });

		const playBtn = page.getByRole('button', { name: 'Play' });
		await playBtn.click();

		// O status deve mudar para PLAYING
		await expect.element(page.getByText('PLAYING')).toBeInTheDocument();

		vi.useFakeTimers();
		await vi.advanceTimersByTimeAsync(1300);
		vi.useRealTimers();

		// O texto completo deve estar visível
		await expect.element(page.getByText('Você concorda?')).toBeInTheDocument();
	});

	it('should parse and style special tokens correctly', async () => {
		render(DialoguePlayer, { nodes });

		const playBtn = page.getByRole('button', { name: 'Play' });
		await playBtn.click();

		vi.useFakeTimers();
		await vi.advanceTimersByTimeAsync(1300);
		vi.useRealTimers();

		const simEl = page.getByText('[Sim]');
		await expect.element(simEl).toBeInTheDocument();
		await expect.element(simEl).toHaveClass('text-[#34d399]');

		const naoEl = page.getByText('[Não]');
		await expect.element(naoEl).toBeInTheDocument();
		await expect.element(naoEl).toHaveClass('text-[#fb7185]');

		const bEl = page.getByText('‹');
		await expect.element(bEl).toBeInTheDocument();
		await expect.element(bEl).toHaveClass('text-[#fb923c]');
	});

	it('should pause and play on space keypress', async () => {
		render(DialoguePlayer, { nodes });

		// Simula o pressionamento da barra de espaço
		await userEvent.keyboard(' ');

		// Deve iniciar
		await expect.element(page.getByText('PLAYING')).toBeInTheDocument();

		// Pressiona espaço novamente para pausar
		await userEvent.keyboard(' ');

		// Deve pausar
		await expect.element(page.getByText('PAUSED')).toBeInTheDocument();
	});
});
