import type { Chapter } from '../types';

export const chapterOne: Chapter = {
	id: 'chapter-01',
	title: 'Listas Encadeadas',
	description: 'BOH explica listas duplamente encadeadas',
	initialStepId: 's01',
	steps: {
		s01: {
			type: 'dialogue',
			id: 's01',
			text: 'Oi, tudo bem?',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 's02'
		},
		s02: {
			type: 'dialogue',
			id: 's02',
			text: 'Muito obrigado por executar o meu script',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 's03'
		},
		s03: {
			type: 'dialogue',
			id: 's03',
			text: 'Eu me chamo BOH!',
			speaker: 'boh',
			expression: 'open mouth',
			nextStepId: 's04'
		},
		s04: {
			type: 'dialogue',
			id: 's04',
			text: 'He He',
			speaker: 'boh',
			expression: 'idle',
			delay: 0.75,
			nextStepId: 's05'
		},
		s05: {
			type: 'dialogue',
			id: 's05',
			text: 'Sabe...',
			speaker: 'boh',
			expression: 'idle',
			delay: 0.75,
			nextStepId: 's06'
		},
		s06: {
			type: 'dialogue',
			id: 's06',
			text: 'Tipo,',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 's07'
		},
		s07: {
			type: 'dialogue',
			id: 's07',
			text: '',
			speaker: 'boh',
			expression: 'idle',
			staticArt: 'Tipo, ROH',
			nextStepId: 's08'
		},
		s08: {
			type: 'dialogue',
			id: 's08',
			text: '',
			speaker: 'boh',
			expression: 'idle',
			staticArt: 'Tipo, ROH-BOH',
			nextStepId: 's09'
		},
		s09: {
			type: 'animation',
			id: 's09',
			expression: 'open mouth',
			speaker: 'boh',
			staticArt: 'HahAHahAHahAHahA',
			frames: [
				{ text: 'H', delay: 0.05 },
				{ text: 'Hah', delay: 0.05 },
				{ text: 'HahAHah', delay: 0.05 },
				{ text: 'HahAHahA', delay: 0.05 },
				{ text: 'HahAHahAHah', delay: 0.05 },
				{ text: 'HahAHahAHahA', delay: 0.05 },
				{ text: 'HahAHahAHahAHah', delay: 0.05 },
				{ text: 'HahAHahAHahAHahA', delay: 0.05 }
			],
			nextStepId: 's10'
		},
		s10: {
			type: 'dialogue',
			id: 's10',
			text: 'Ai ai, sou meio comedia as vezes, sabe?',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 'end',
		},
		end: {
			type: 'dialogue',
			id: 'end',
			text: 'Fim da demonstracao! Esse foi o BOH web.',
			speaker: 'boh',
			expression: 'open mouth',
			nextStepId: 'end'
		}
	}
};
