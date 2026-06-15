import { test, expect } from '@playwright/test';
import { StoryEngine } from '../src/lib/engine/storyEngine.svelte';
import type { Chapter, DialogueStep, InputStep, AnimationStep, ChoiceStep, PauseStep, Step } from '$lib/content/types';

const createChapter = (steps: Record<string, Step>): Chapter => ({
  id: 'test-chapter',
  title: 'Test',
  description: 'Test chapter',
  createdAt: Date.now(),
  updatedAt: Date.now(),
  initialStepId: Object.keys(steps)[0],
  steps,
});

test('advances to next step after typing finishes', async () => {
  const steps: Record<string, Step> = {
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
      nextStepId: 's03',
    },
    s03: {
      type: 'dialogue',
      id: 's03',
      text: 'End',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 'end',
    },
    end: {
      type: 'dialogue',
      id: 'end',
      text: 'The end',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 'end',
    },
  };

  const chapter = createChapter(steps);
  const engine = new StoryEngine(chapter, null);

  engine.init();

  // Wait for typing to finish and auto-advance
  // We'll use a promise to wait for a change in currentStepId
  let resolved = false;
  const waitForStep = (targetId: string) => new Promise<void>((resolve) => {
    const check = () => {
      if (engine.currentStepId === targetId) {
        resolved = true;
        resolve();
      } else {
        setTimeout(check, 10);
      }
    };
    check();
  });

  // Initially should be s01
  expect(engine.currentStepId).toBe('s01');
  // Wait for it to advance to s02 after typing s01
  await waitForStep('s02');
  expect(engine.currentStepId).toBe('s02');
  // Then wait for s03
  await waitForStep('s03');
  expect(engine.currentStepId).toBe('s03');
  // Then wait for end
  await waitForStep('end');
  expect(engine.currentStepId).toBe('end');
});

test('replaces input placeholders in dialogue text', async () => {
  const steps: Record<string, Step> = {
    s01: {
      type: 'input',
      id: 's01',
      prompt: 'What is your name?',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 's02',
    },
    s02: {
      type: 'dialogue',
      id: 's02',
      text: 'Nice to meet you, {{input:s01}}!',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 'end',
    },
    end: {
      type: 'dialogue',
      id: 'end',
      text: 'Bye',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 'end',
    },
  };

  const chapter = createChapter(steps);
  const engine = new StoryEngine(chapter, null);

  engine.init();
  // Simulate input after a short delay
  setTimeout(() => {
    engine.submitInput('Alice');
  }, 50);

  // Wait for dialogue step to appear
  await new Promise<void>((resolve) => {
    const check = () => {
      if (engine.currentStepId === 's02') {
        resolve();
      } else {
        setTimeout(check, 10);
      }
    };
    check();
  });

  // Wait for typing to finish
  await new Promise<void>((resolve) => {
    const checkTyped = () => {
      if (!engine.isTyping) {
        resolve();
      } else {
        setTimeout(checkTyped, 10);
      }
    };
    setTimeout(checkTyped, 100);
  });

  expect(engine.typedText).toBe('Nice to meet you, Alice!');
});

test('prevents infinite loop on same step', async () => {
  const steps: Record<string, Step> = {
    s01: {
      type: 'dialogue',
      id: 's01',
      text: 'Loop',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 's01', // loop to itself
    },
  };

  const chapter = createChapter(steps);
  const engine = new StoryEngine(chapter, null);

  engine.init();

  // Wait a bit to see if it advances (it shouldn't)
  await new Promise<void>((resolve) => {
    setTimeout(() => {
      expect(engine.currentStepId).toBe('s01');
      expect(engine.isTyping).toBe(false);
      resolve();
    }, 200);
  });
});

test('advances after loop detection when nextStepId differs', async () => {
  const steps: Record<string, Step> = {
    s01: {
      type: 'dialogue',
      id: 's01',
      text: 'First',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 's02',
    },
    s02: {
      type: 'dialogue',
      id: 's02',
      text: 'Second (loop back)',
      speaker: 'boh',
      expression: 'idle',
      nextStepId: 's01', // loop back to s01
    },
  };

  const chapter = createChapter(steps);
  const engine = new StoryEngine(chapter, null);

  engine.init();

  // Expect: s01 -> s02 -> s01 (repeat) -> because repeatCount>1 and nextStepId of s01 is s02 (different), should go to s02
  // So after two visits to s01, we should go to s02.

  await new Promise<void>((resolve) => {
    const check = async () => {
      // Wait a bit for transitions
      await new Promise(r => setTimeout(r, 300));
      // After some time, we expect the engine to be on s02 (since it advanced after repeat)
      setTimeout(() => {
        expect(engine.currentStepId).toBe('s02');
        resolve();
      }, 500);
    };
    // Wait for initial typing to finish and first transition
    setTimeout(check, 200);
  });
});