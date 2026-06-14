import type { Chapter, Step, DialogueStep, ChoiceStep, InputStep, PauseStep } from '$lib/content/types';
import type { Character } from '$lib/content/characters';

// This .svelte.ts file uses Svelte 5 runes for reactive state
export class StoryEngine {
	chapter: Chapter;

	// Core state
	currentStepId = $state<string>('');
	currentStep = $derived<Step | null>(this.chapter.steps[this.currentStepId] ?? null);

	// UI state for the current step
	typedText = $state<string>('');
	isTyping = $state<boolean>(false);
	isWaitingForInput = $state<boolean>(false);

	// Current visual state derived from the step
	speaker = $state<string>('');
	expression = $state<string>('');
	staticArt = $state<string>('');
	colorizeArrows = $state<boolean>(false);

	private typingTimeout: number | null = null;
	private typingSpeed = 30; // ms per character

	constructor(chapter: Chapter) {
		this.chapter = chapter;
	}

	init() {
		this.goToStep(this.chapter.initialStepId);
	}

	goToStep(stepId: string) {
		if (this.typingTimeout) {
			clearTimeout(this.typingTimeout);
			this.typingTimeout = null;
		}
		this.typedText = '';
		this.isTyping = false;
		this.currentStepId = stepId;
		const step = this.currentStep;
		if (!step) return;

		// Set visual state from the step
		this.speaker = 'speaker' in step ? (step as DialogueStep).speaker : '';
		this.expression = 'expression' in step ? (step as DialogueStep).expression : '';
		this.staticArt = step.staticArt ?? '';
		this.colorizeArrows = step.colorizeArrows ?? false;

		switch (step.type) {
			case 'dialogue':
				this.startTyping((step as DialogueStep).text, (step as DialogueStep).delay);
				break;
			case 'choice':
				this.isWaitingForInput = true;
				break;
			case 'input':
				this.isWaitingForInput = true;
				break;
			case 'pause':
				setTimeout(() => {
					this.goToStep((step as PauseStep).nextStepId);
				}, (step as PauseStep).duration * 1000);
				break;
		}
	}

	private startTyping(text: string, delay: number = 0.75) {
		this.isTyping = true;
		let i = 0;
		const chars = text.split('');

		const typeNext = () => {
			if (i < chars.length) {
				this.typedText = chars.slice(0, i + 1).join('');
				i++;
				// Play typing sound here (via AudioManager event)
				this.typingTimeout = window.setTimeout(typeNext, this.typingSpeed);
			} else {
				this.isTyping = false;
				if (this.currentStep?.type === 'dialogue') {
					const d = this.currentStep as DialogueStep;
					if (d.delay) {
						this.typingTimeout = window.setTimeout(() => {
							this.goToStep(d.nextStepId);
						}, (d.delay ?? 0) * 1000);
					} else {
						this.goToStep(d.nextStepId);
					}
				}
			}
		};

		this.typingTimeout = window.setTimeout(typeNext, this.typingSpeed);
	}

	// Skip the current typing animation
	skip() {
		if (!this.currentStep || this.currentStep.type !== 'dialogue') return;
		if (this.typingTimeout) {
			clearTimeout(this.typingTimeout);
			this.typingTimeout = null;
		}
		const step = this.currentStep as DialogueStep;
		this.typedText = step.text;
		this.isTyping = false;
		// Wait for the configured delay, then continue
		this.typingTimeout = window.setTimeout(() => {
			this.goToStep(step.nextStepId);
		}, (step.delay ?? 0.75) * 1000);
	}

	// Advance past a waiting state (e.g., after a pause or user click when not typing)
	advance() {
		if (this.isTyping) {
			this.skip();
			return;
		}
		if (this.isWaitingForInput) return; // Don't advance if waiting for input

		// If it's a dialogue step and we're already past typing
		if (this.currentStep?.type === 'dialogue') {
			const step = this.currentStep as DialogueStep;
			this.goToStep(step.nextStepId);
		}
	}

	// Handle a choice selection
	selectChoice(targetId: string) {
		this.isWaitingForInput = false;
		this.goToStep(targetId);
	}

	// Handle an input submission
	submitInput(input: string) {
		if (this.currentStep?.type !== 'input') return;
		const step = this.currentStep as InputStep;
		this.isWaitingForInput = false;
		this.goToStep(step.nextStepId);
		// TODO: store input value somewhere
	}
}
