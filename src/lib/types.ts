export interface DialogueNode {
	id: string;
	type: 'start' | 'boh';
	x: number;
	y: number;
	expression: 'idle' | 'pokerface' | 'thinking' | 'open mouth' | 'annoyed' | 'looking down';
	text: string;
	nextId?: string; // ID do próximo nó conectado
}

export interface Project {
	id: string;
	name: string;
	description: string;
	tag: string;
	nodes: Record<string, DialogueNode>;
}
