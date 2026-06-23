import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render } from 'vitest-browser-svelte';
import { writable } from 'svelte/store';
import Page from './+page.svelte';
import { page } from '$app/stores';
import { goto } from '$app/navigation';

vi.mock('$app/stores', () => {
	const mockPageStore = writable({
		params: {
			projectId: ''
		}
	});
	return {
		page: mockPageStore
	};
});

vi.mock('$app/navigation', () => {
	return {
		goto: vi.fn()
	};
});

vi.mock('$app/paths', () => {
	return {
		base: ''
	};
});

describe('editor route page', () => {
	beforeEach(() => {
		vi.clearAllMocks();
		localStorage.clear();
		// Inicializa a rota como limpa
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		(page as any).set({
			params: {
				projectId: ''
			}
		});
	});

	it('should create a new project and redirect to /editor/[newId] when no projectId is provided', async () => {
		// Renderiza o componente Page da rota
		render(Page);

		// Espera um pouco para que os efeitos rodem
		await new Promise((resolve) => setTimeout(resolve, 100));

		// Verifica se o goto foi chamado para redirecionar para a nova rota de projeto
		expect(goto).toHaveBeenCalled();
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const calledUrl = (goto as any).mock.calls[0][0];
		expect(calledUrl).toContain('/editor/project-');

		// Verifica se o projeto novo foi devidamente adicionado ao localStorage
		const saved = localStorage.getItem('saved-projects-v2');
		expect(saved).not.toBeNull();
		const projects = JSON.parse(saved!);
		expect(projects.length).toBeGreaterThan(0);
	});

	it('should load an existing project when a valid projectId is provided', async () => {
		// Mocka um projeto existente no localStorage
		const testProject = {
			id: 'test-project-123',
			name: 'Projeto Teste Spec',
			description: 'Desc',
			tag: 'Rascunho',
			nodes: {}
		};
		localStorage.setItem('saved-projects-v2', JSON.stringify([testProject]));

		// Simula que a URL atual é /editor/test-project-123
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		(page as any).set({
			params: {
				projectId: 'test-project-123'
			}
		});

		render(Page);

		// Espera a renderização
		await new Promise((resolve) => setTimeout(resolve, 100));

		// Não deve chamar goto para redirecionar
		expect(goto).not.toHaveBeenCalled();
	});
});
