import { test, expect } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  // Ensure we are on the home page
  await page.goto('http://localhost:5173');
  // Wait for the app to load
  await page.waitForSelector('text=BOH');
});

// Helper to add a test project to localStorage
const addTestProject = async (page) => {
  await page.addInitScript(() => {
    // Create a simple chapter
    const chapter = {
      id: 'test-chapter',
      title: 'Test Project',
      description: 'A test project',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      initialStepId: 's01',
      steps: {
        s01: {
          type: 'dialogue',
          id: 's01',
          text: 'Hello',
          speaker: 'boh',
          expression: 'idle',
          nextStepId: 's02',
        },
        s02: {
          type: 'dialogue',
          id: 's02',
          text: 'World',
          speaker: 'boh',
          expression: 'idle',
          nextStepId: 'end',
        },
        end: {
          type: 'dialogue',
          id: 'end',
          text: 'The end',
          speaker: 'boh',
          expression: 'open mouth',
          nextStepId: 'end',
        },
      },
    };
    const project = {
      id: 'test-project',
      title: 'Test Project',
      description: 'A test project',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      chapter,
    };
    localStorage.setItem('projects', JSON.stringify([project]));
    localStorage.setItem('selectedProjectId', 'test-project');
  });
  // Reload to see the project
  await page.reload();
  await page.waitForSelector('text=Test Project');
};

test('displays project grid and can open player', async ({ page }) => {
  await addTestProject(page);
  // Click the project card to open player
  const card = page.locator('text=Test Project').locator('..'); // adjust selector
  await card.click();
  // Wait for terminal player to appear
  await page.waitForSelector('text=Hello');
  // Verify initial dialogue
  await expect(page.locator('text=Hello')).toBeVisible();
  // Click to advance
  await page.click('role=button[name="Avancar dialogo"]');
  // Wait for next dialogue
  await expect(page.locator('text=World')).toBeVisible();
  // Click again
  await page.click('role=button[name="Avancar dialogo"]');
  // Wait for final dialogue
  await expect(page.locator('text=The end')).toBeVisible();
});

test('handles input step', async ({ page }) => {
  // Create a project with an input step
  await page.addInitScript(() => {
    const chapter = {
      id: 'input-chapter',
      title: 'Input Test',
      description: '',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      initialStepId: 's01',
      steps: {
        s01: {
          type: 'input',
          id: 's01',
          prompt: 'Qual é o seu nome?',
          speaker: 'boh',
          expression: 'idle',
          nextStepId: 's02',
        },
        s02: {
          type: 'dialogue',
          id: 's02',
          text: 'Prazer em te conhecer, {{input:s01}}! Vamos continuar.',
          speaker: 'boh',
          expression: 'idle',
          nextStepId: 'end',
        },
        end: {
          type: 'dialogue',
          id: 'end',
          text: 'Fim',
          speaker: 'boh',
          expression: 'open mouth',
          nextStepId: 'end',
        },
      },
    };
    const project = {
      id: 'input-project',
      title: 'Input Test',
      description: '',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      chapter,
    };
    const projects = [project];
    localStorage.setItem('projects', JSON.stringify(projects));
    localStorage.setItem('selectedProjectId', 'input-project');
  });
  await page.reload();
  await page.waitForSelector('text=Input Test');
  // Open player
  await page.click('text=Input Test');
  // Wait for prompt
  await expect(page.locator('text=Qual é o seu nome?')).toBeVisible();
  // Fill input
  const input = page.locator('input[placeholder="Digite aqui..."]');
  await input.fill('Alice');
  // Press OK
  await page.click('role=button[name="OK"]');
  // Wait for dialogue with replaced placeholder
  await expect(page.locator('text=Prazer em te conhecer, Alice! Vamos continuar.')).toBeVisible();
});