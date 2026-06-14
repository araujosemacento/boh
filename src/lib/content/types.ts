export interface Chapter {
	id: string;
	title: string;
	description: string;
	steps: Record<string, Step>;
	initialStepId: string;
}

export interface BaseStep {
	id: string;
	/** Optional static art to display alongside this step */
	staticArt?: string;
	/** Colorize arrows in the static art or text */
	colorizeArrows?: boolean;
}

export interface DialogueStep extends BaseStep {
	type: 'dialogue';
	/** The text to display (supports simple HTML for styling within the typing animation) */
	text: string;
	/** The character speaking this line */
	speaker: string;
	/** Expression key (references characters.ts) */
	expression: string;
	/** Seconds to wait after the text finishes typing */
	delay?: number;
	/** ID of the next step */
	nextStepId: string;
}

export interface ChoiceStep extends BaseStep {
	type: 'choice';
	/** The question text */
	text: string;
	/** The character asking */
	speaker: string;
	/** Expression key */
	expression: string;
	/** Available choices */
	choices: {
		label: string;
		/** Keyboard shortcut key */
		key: string;
		/** Target step ID */
		targetId: string;
	}[];
	/** Timeout in seconds. If the user doesn't respond, the timeoutPath is taken */
	timeout?: number;
	/** Step ID to jump to on timeout */
	timeoutTargetId?: string;
}

export interface InputStep extends BaseStep {
	type: 'input';
	/** The character prompting for input */
	speaker: string;
	expression: string;
	/** Prompt text */
	prompt: string;
	/** Minimum length of input required */
	minLength?: number;
	nextStepId: string;
}

export interface AnimationStep extends BaseStep {
	type: 'animation';
	/** Frames for the animation */
	frames: { text: string; delay: number }[];
	/** Expression to use during animation */
	expression: string;
	speaker: string;
	nextStepId: string;
}

export interface PauseStep extends BaseStep {
	type: 'pause';
	/** Duration in seconds */
	duration: number;
	nextStepId: string;
}

export type Step = DialogueStep | ChoiceStep | InputStep | AnimationStep | PauseStep;

export interface StoryState {
	/** Currently active step */
	currentStep: Step | null;
	/** Is the typing animation in progress? */
	isTyping: boolean;
	/** Is the engine waiting for user input? */
	isWaitingForInput: boolean;
	/** The text that has been typed out so far */
	typedText: string;
	/** Full text of the current dialogue */
	currentText: string;
	/** Current speaker */
	speaker: string;
	/** Current expression */
	expression: string;
	/** Current static art */
	staticArt: string;
	/** Whether arrows are colorized in the current step */
	colorizeArrows: boolean;
}
