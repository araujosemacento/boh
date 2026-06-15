import type {
	Chapter,
	Step,
	DialogueStep,
	ChoiceStep,
	InputStep,
	PauseStep,
	AnimationStep,
} from '$lib/content/types';
import type { AudioManager } from './audioManager';

// This .svelte.ts file uses Svelte 5 runes for reactive state
export class StoryEngine {
	declare chapter: Chapter;

	// Core state
	currentStepId = $state<string>('');
	get currentStep(): Step | null { return this.chapter.steps[this.currentStepId] ?? null; }

	// UI state for the current step
	typedText = $state<string>('');
	isTyping = $state<boolean>(false);
	isWaitingForInput = $state<boolean>(false);
	isWaitingForChoice = $state<boolean>(false);
	isWaitingForTextInput = $state<boolean>(false);

	// Current visual state derived from the step
	speaker = $state<string>('');
	expression = $state<string>('');
	staticArt = $state<string>('');
	colorizeArrows = $state<boolean>(false);
	promptText = $state<string>('');

	// Storage for user-provided text input (keyed by step id)
	inputValues: Record<string, string> = {};

	// Optional audio - injected to keep the engine decoupled from the AudioContext
	audio: AudioManager | null = null;

	private typingTimeout: ReturnType<typeof setTimeout> | null = null;
	private paused: $state<boolean> = $state(false);
	private choiceTimeoutHandle: ReturnType<typeof setTimeout> | null = null;
	private typingSpeed = 30; // ms per character

	constructor(chapter: Chapter, audio: AudioManager | null = null) {
		this.chapter = chapter;
		this.audio = audio;
	}

	init() {
		this.goToStep(this.chapter.initialStepId);
	}

	reset() {
		if (this.typingTimeout) {
			clearTimeout(this.typingTimeout);
			this.typingTimeout = null;
		}
		if (this.choiceTimeoutHandle) {
			clearTimeout(this.choiceTimeoutHandle);
			this.choiceTimeoutHandle = null;
		}
		this.typedText = '';
		this.isTyping = false;
		this.isWaitingForInput = false;
		this.isWaitingForChoice = false;
		this.isWaitingForTextInput = false;
		this.colorizeArrows = false;
		this.promptText = '';
	}

	private lastStepId: string = '';
private repeatCount: number = 0;

	goToStep(stepId: string) {
		this.reset();
		this.currentStepId = stepId;
		// Loop detection guard
		if (stepId === this.lastStepId) {
			this.repeatCount++;
			if (this.repeatCount > 1 && this.currentStep?.nextStepId && this.currentStep?.nextStepId !== stepId) {
				this.lastStepId = '';
				this.repeatCount = 0;
				this.goToStep(this.currentStep.nextStepId);
				return;
			}
		} else {
			this.lastStepId = stepId;
			this.repeatCount = 0;
		}
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
				this.isWaitingForChoice = true;
				this.promptText = (step as ChoiceStep).text || '';
				if (step.timeout && step.timeoutTargetId) {
					this.choiceTimeoutHandle = setTimeout(() => {
						this.selectChoice(step.timeoutTargetId as string, true);
					}, step.timeout * 1000);
				}
				break;
			case 'input':
				this.isWaitingForInput = true;
				this.isWaitingForTextInput = true;
				this.promptText = (step as InputStep).prompt;
				break;
			case 'animation':
				this.runAnimation(step as AnimationStep);
				break;
			case 'pause':
				this.typingTimeout = setTimeout(() => {
					this.goToStep((step as PauseStep).nextStepId);
				}, (step as PauseStep).duration * 1000);
				break;
		}
	}

	private startTyping(text: string, delay: number = 0.75) {
        // Replace input placeholders like {{input:s11}} with stored values
        const placeholderRegex = /{{input:([^}]+)}}/g;
        const resolvedText = text.replace(placeholderRegex, (_, id) => this.inputValues[id] ?? '');
        text = resolvedText;
		this.isTyping = true;
		let i = 0;
		const chars = text.split('');

		const typeNext = () => {
			if (i < chars.length) {
				this.typedText = chars.slice(0, i + 1).join('');
				const lastChar = chars[i];
				// Trigger typing sound for alphanumeric characters
				if (lastChar && /\w/.test(lastChar)) {
					this.audio?.playTyping();
				}
				i++;
				this.typingTimeout = setTimeout(typeNext, this.typingSpeed);
			} else {
				this.isTyping = false;
				const step = this.currentStep;
				if (step?.type === 'dialogue') {
					const wait = (step.delay ?? 0) * 1000;
					if (wait > 0) {
						this.typingTimeout = setTimeout(() => {
							this.goToStep(step.nextStepId);
						}, wait);
					} else {
						this.goToStep(step.nextStepId);
					}
				}
			}
		};

		this.typingTimeout = setTimeout(typeNext, this.typingSpeed);
	}

	private runAnimation(step: AnimationStep) {
		const frames = step.frames;
		let i = 0;

		const nextFrame = () => {
			if (i < frames.length) {
				this.typedText = frames[i].text;
				const wait = Math.max(frames[i].delay * 1000, 10);
				i++;
				this.typingTimeout = setTimeout(nextFrame, wait);
			} else {
				this.goToStep(step.nextStepId);
			}
		};

		// Render designated static art (for the "HA" laugh etc.)
		this.staticArt = step.staticArt ?? '';
		this.isTyping = true;
		nextFrame();
	}

	// Skip the current typing animation
	skip() {
		if (this.isWaitingForInput) return;
		const step = this.currentStep;
		if (!step) return;
		if (step.type === 'dialogue' || step.type === 'animation') {
			if (this.typingTimeout) {
				clearTimeout(this.typingTimeout);
				this.typingTimeout = null;
			}
			this.typedText = step.type === 'dialogue' ? step.text : step.frames[step.frames.length - 1].text;
			this.isTyping = false;
			const wait = (step.type === 'dialogue' ? step.delay ?? 0 : 0) * 1000;
			if (step.type === 'dialogue') {
				this.typingTimeout = setTimeout(() => {
					this.goToStep(step.nextStepId);
				}, wait);
			} else {
				this.goToStep(step.nextStepId);
			}
		}
	}

	// Advance past a waiting state (e.g., after a pause or user click when not typing)
	advance() {
		if (this.isWaitingForInput) return; // Choices and text input handle their own flow
		if (this.isTyping) {
			this.skip();
			return;
		}
		const step = this.currentStep;
		if (step?.type === 'dialogue') {
			this.goToStep(step.nextStepId);
		}
	}

	// Handle a choice selection
	selectChoice(targetId: string, isTimeout = false) {
		if (this.choiceTimeoutHandle) {
			clearTimeout(this.choiceTimeoutHandle);
			this.choiceTimeoutHandle = null;
		}
		this.isWaitingForInput = false;
		this.isWaitingForChoice = false;
		this.choiceTimeoutHandle = null;
		// Lightweight hook: caller can check targetId to react
		void isTimeout;
		this.goToStep(targetId);
	}

	// Handle an input submission
	submitInput(input: string) {
		if (this.currentStep?.type !== 'input') return;
		const step = this.currentStep as InputStep;
		this.inputValues[step.id] = input;
		this.isWaitingForInput = false;
		this.isWaitingForTextInput = false;
		this.promptText = '';
		this.goToStep(step.nextStepId);
	}
}
