import { page } from 'vitest/browser';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render } from 'vitest-browser-svelte';
import Page from './+page.svelte';
import { goto } from '$app/navigation';

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

describe('Dashboard +page.svelte Integration', () => {
	beforeEach(() => {
		vi.clearAllMocks();
		localStorage.clear();
	});

	it('should list projects on dashboard and allow searching', async () => {
		render(Page);

		// Lista padrão deve conter os demos, por exemplo "Boh: Recursos"
		const cardTitle = page.getByText('Boh: Recursos');
		await expect.element(cardTitle).toBeInTheDocument();

		// Realiza uma busca que filtra a lista
		const searchInput = page.getByPlaceholder('Buscar projetos pelo nome ou branch...');
		await searchInput.fill('Projeto Inexistente Muito Louco');

		// A lista deve ficar vazia
		await expect.element(page.getByText('Nenhum item encontrado')).toBeInTheDocument();
	});

	it('should filter by tag when tag is clicked', async () => {
		render(Page);

		// Localiza a tag "Demonstração" e clica nela
		const tagBtn = page.getByTitle('Filtrar por esta tag').first();
		await tagBtn.click();

		// O input de busca deve ser preenchido com a query de tag
		const searchInput = page.getByPlaceholder('Buscar projetos pelo nome ou branch...');
		await expect.element(searchInput).toHaveValue('tag:"Demonstração"');
	});

	it('should navigate to new project editor when "Novo projeto" button is clicked', async () => {
		render(Page);

		const newProjBtn = page.getByRole('button', { name: 'Novo projeto' });
		await newProjBtn.click();

		expect(goto).toHaveBeenCalledWith('/editor');
	});

	it('should navigate to project editor when "Editar diálogo" button is clicked', async () => {
		render(Page);

		const editBtn = page.getByTitle('Editar diálogo').first();
		await editBtn.click();

		expect(goto).toHaveBeenCalled();
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const calledUrl = (goto as any).mock.calls[0][0];
		expect(calledUrl).toContain('/editor/');
	});
});
