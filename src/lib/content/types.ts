export interface Chapter {
	id: string;
	title: string;
	description: string;
	steps: Record<string, Step>;
	initialStepId: string;
}

export interface BaseStep {
	id: string;
	staticArt?: string;
	colorizeArrows?: boolean;
}

export interface DialogueStep extends BaseStep {
	type: 'dialogue';
	text: string;
	speaker: string;
	expression: string;
	delay?: number;
	nextStepId: string;
}

export interface ChoiceStep extends BaseStep {
	type: 'choice';
	text: string;
	speaker: string;
	expression: string;
	choices: Array<{ label: string; key: string; targetId: string }>;
	timeout?: number;
	timeoutTargetId?: string;
}

export interface InputStep extends BaseStep {
	type: 'input';
	speaker: string;
	expression: string;
	prompt: string;
	minLength?: number;
	nextStepId: string;
}

export interface AnimationStep extends BaseStep {
	type: 'animation';
	frames: Array<{ text: string; delay: number }>;
	expression: string;
	speaker: string;
	staticArt?: string;
	nextStepId: string;
}

export interface PauseStep extends BaseStep {
	type: 'pause';
	duration: number;
	nextStepId: string;
}

export type Step = DialogueStep | ChoiceStep | InputStep | AnimationStep | PauseStep;
