import { page } from 'vitest/browser';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render } from 'vitest-browser-svelte';
import ProjectWorkspaceWrapper from './ProjectWorkspaceWrapper.svelte';
import type { Project } from '../types';

describe('ProjectWorkspace.svelte', () => {
	let project: Project;
	let onBackMock: () => void;

	beforeEach(() => {
		onBackMock = vi.fn();
		localStorage.clear();
		project = {
			id: 'proj-1',
			name: 'Boh Aventuras',
			description: 'Description',
			tag: 'Draft',
			nodes: {
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
					x: 300,
					y: 100,
					expression: 'idle',
					text: 'Olá, viajante!'
				}
			}
		};
	});

	it('should render header with project details', async () => {
		render(ProjectWorkspaceWrapper, { initialProject: project, onBack: onBackMock });

		await expect.element(page.getByText('Boh Aventuras')).toBeInTheDocument();
		await expect.element(page.getByText('Draft')).toBeInTheDocument();

		await expect.element(page.getByText('Importar JSON')).toBeInTheDocument();
		await expect.element(page.getByText('Exportar JSON')).toBeInTheDocument();
		await expect.element(page.getByText('Salvar')).toBeInTheDocument();
	});

	it('should trigger onBack callback when Back button is clicked', async () => {
		render(ProjectWorkspaceWrapper, { initialProject: project, onBack: onBackMock });

		const backBtn = page.getByRole('button', { name: 'Voltar' });
		await backBtn.click();

		expect(onBackMock).toHaveBeenCalled();
	});

	it('should render left panel (palette) and add node when clicked', async () => {
		render(ProjectWorkspaceWrapper, { initialProject: project, onBack: onBackMock });

		const addBtn = page.getByRole('button', { name: 'Fala do Boh' });
		await expect.element(addBtn).toBeInTheDocument();

		await addBtn.click();

		const textareas = page.getByPlaceholder('Escreva a fala do Boh aqui...');
		const textareaElements = await textareas.all();
		expect(textareaElements.length).toBe(2);
	});

	it('should update reactive bindings when editing dialogue text and expression', async () => {
		render(ProjectWorkspaceWrapper, { initialProject: project, onBack: onBackMock });

		const textarea = page.getByPlaceholder('Escreva a fala do Boh aqui...');
		await expect.element(textarea).toHaveValue('Olá, viajante!');

		await textarea.clear();
		await textarea.fill('Nova fala do Boh!');
		await expect.element(textarea).toHaveValue('Nova fala do Boh!');

		const select = page.getByRole('combobox');
		const el = select.element() as HTMLSelectElement;
		el.value = 'thinking';
		el.dispatchEvent(new Event('change'));
		await expect.element(select).toHaveValue('thinking');
	});

	it('should delete node when trash button is clicked', async () => {
		render(ProjectWorkspaceWrapper, { initialProject: project, onBack: onBackMock });

		const textarea = page.getByPlaceholder('Escreva a fala do Boh aqui...');
		await expect.element(textarea).toBeInTheDocument();

		const deleteBtn = page.getByTitle('Excluir Nó');
		deleteBtn.element().dispatchEvent(new MouseEvent('click', { bubbles: true }));
		await new Promise((r) => setTimeout(r, 100));

		await expect.element(textarea).not.toBeInTheDocument();
	});

	it('should update zoom controls', async () => {
		render(ProjectWorkspaceWrapper, { initialProject: project, onBack: onBackMock });

		// Wait for fitView to complete
		await new Promise((resolve) => setTimeout(resolve, 150));

		const zoomTextContainer = document.querySelector('span.font-mono.text-center');
		const initialZoomStr = zoomTextContainer?.textContent || '100%';
		const initialZoom = parseInt(initialZoomStr.replace('%', '').trim());

		const zoomInBtn = page.getByTitle('Aumentar Zoom');
		await zoomInBtn.click();
		await expect.element(page.getByText(`${initialZoom + 10}%`)).toBeInTheDocument();

		const zoomOutBtn = page.getByTitle('Diminuir Zoom');
		await zoomOutBtn.click();
		await zoomOutBtn.click();
		await expect.element(page.getByText(`${initialZoom - 10}%`)).toBeInTheDocument();
	});
});
