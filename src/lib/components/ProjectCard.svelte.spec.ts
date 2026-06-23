import { page } from 'vitest/browser';
import { describe, expect, it, vi } from 'vitest';
import { render } from 'vitest-browser-svelte';
import ProjectCard from './ProjectCard.svelte';

describe('ProjectCard.svelte', () => {
	const project = {
		name: 'Projeto de Teste Card',
		description: 'Descrição longa do projeto de teste.',
		tag: 'Ideia'
	};

	it('should render project fields correctly', async () => {
		render(ProjectCard, { project });

		await expect.element(page.getByText('Projeto de Teste Card')).toBeInTheDocument();
		await expect
			.element(page.getByText('Descrição: Descrição longa do projeto de teste.'))
			.toBeInTheDocument();
		await expect.element(page.getByText('Ideia')).toBeInTheDocument();
	});

	it('should call callbacks on button clicks', async () => {
		const onPlay = vi.fn();
		const onEdit = vi.fn();
		const onDelete = vi.fn();
		const onTagClick = vi.fn();
		const onSelect = vi.fn();

		render(ProjectCard, {
			project,
			onPlay,
			onEdit,
			onDelete,
			onTagClick,
			onSelect
		});

		// 1. Play Button
		const playBtn = page.getByTitle('Iniciar reprodução');
		await playBtn.click();
		expect(onPlay).toHaveBeenCalled();

		// 2. Edit Button
		const editBtn = page.getByTitle('Editar diálogo');
		await editBtn.click();
		expect(onEdit).toHaveBeenCalled();

		// 3. Delete Button
		const deleteBtn = page.getByTitle('Excluir projeto');
		await deleteBtn.click();
		expect(onDelete).toHaveBeenCalled();

		// 4. Tag Button
		const tagBtn = page.getByTitle('Filtrar por esta tag');
		await tagBtn.click();
		expect(onTagClick).toHaveBeenCalledWith('Ideia');
	});
});
