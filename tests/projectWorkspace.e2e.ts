import { test, expect } from '@playwright/test';

test.describe('ProjectWorkspace E2E Tests', () => {
	test.beforeEach(async ({ page }) => {
		// Limpa o localStorage antes de cada teste para iniciar com estado limpo
		await page.goto('/');
		await page.evaluate(() => localStorage.clear());
	});

	test('should create a new project, edit details, connect nodes, run player, and save/reload', async ({
		page
	}) => {
		// 1. Navegar para a página inicial
		await page.goto('/');
		await expect(page.locator('h1')).toContainText('Projetos');

		// 2. Clicar em "Novo projeto" para iniciar o editor
		await page.click('button:has-text("Novo projeto")');

		// Deve redirecionar para /editor/project-xxxx
		await expect(page).toHaveURL(/.*\/editor\/project-.*/);

		// 3. Verificar o estado inicial do workspace
		const projectName = page.locator('header span.font-bold');
		await expect(projectName).toContainText('Novo Projeto');
		await expect(page.locator('header span.badge')).toHaveText('Rascunho');

		// 4. Adicionar um nó de diálogo ("Fala do Boh") pelo botão da paleta
		const addNodeBtn = page.locator('button:has-text("Fala do Boh")');
		await expect(addNodeBtn).toBeVisible();
		await addNodeBtn.click();

		// Agora deve haver um nó de diálogo no canvas.
		const textarea = page.locator('textarea[placeholder="Escreva a fala do Boh aqui..."]');
		await expect(textarea).toBeVisible();
		await expect(textarea).toHaveValue('Olá! Escreva o diálogo aqui.');

		// 5. Editar o texto do diálogo e a expressão
		await textarea.fill('E aí, tudo bem com você? Eu sou o Boh.');

		const selectExpression = page.locator('select[id^="expr-"]');
		await selectExpression.selectOption('thinking');
		await expect(selectExpression).toHaveValue('thinking');

		// 6. Conectar o nó Start ao novo nó de diálogo
		const startPort = page.locator('button[aria-label="Conectar saída do nó Start"]');
		const targetPort = page.locator('button[aria-label="Porta de entrada"]');

		await expect(startPort).toBeVisible();
		await expect(targetPort).toBeVisible();

		// Usa o dragTo nativo do Playwright para maior confiabilidade
		await startPort.dragTo(targetPort);

		// 7. Simular o player no painel lateral
		const playBtn = page.locator('button:has-text("Play")');
		await expect(playBtn).toBeVisible();
		await playBtn.click();

		// O status deve mudar para PLAYING
		const statusText = page.locator('span', { hasText: 'PLAYING' });
		await expect(statusText).toBeVisible();

		// Aguarda o término da digitação (cerca de 40 caracteres * 35ms = ~1400ms + folga)
		await page.waitForTimeout(2500);

		// O texto digitado deve estar completo na tela do terminal
		const terminalText = page.locator('div.font-mono:has-text("E aí, tudo bem com você?")').first();
		await expect(terminalText).toBeVisible();

		// 8. Salvar o projeto e recarregar a página
		const saveBtn = page.locator('button:has-text("Salvar")');

		// Lida com o diálogo de alert() do navegador que o saveProject dispara
		page.once('dialog', async (dialog) => {
			expect(dialog.message()).toContain('Projeto salvo localmente com sucesso!');
			await dialog.accept();
		});
		await saveBtn.click();

		// Recarrega a página
		await page.reload();

		// Verifica se o texto editado persiste
		await expect(textarea).toHaveValue('E aí, tudo bem com você? Eu sou o Boh.');
		await expect(selectExpression).toHaveValue('thinking');
	});

	test('should zoom and pan canvas', async ({ page }) => {
		await page.goto('/editor');
		await expect(page).toHaveURL(/.*\/editor\/project-.*/);

		// Aguarda o fitView do onMount rodar e estabilizar o zoom
		await page.waitForTimeout(500);

		const zoomText = page.locator('span:has-text("%")');
		const initialZoomText = await zoomText.textContent();
		const initialZoomVal = parseInt(initialZoomText || '100');

		// Aumentar zoom
		const zoomInBtn = page.locator('button[title="Aumentar Zoom"]');
		await zoomInBtn.click();
		await expect(zoomText).toHaveText(`${initialZoomVal + 10}%`);

		// Diminuir zoom duas vezes (deve ser initialZoom - 10%)
		const zoomOutBtn = page.locator('button[title="Diminuir Zoom"]');
		await zoomOutBtn.click();
		await zoomOutBtn.click();
		await expect(zoomText).toHaveText(`${initialZoomVal - 10}%`);
	});
});
