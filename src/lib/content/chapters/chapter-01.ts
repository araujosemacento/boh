import type { Chapter } from '../types';

export const chapterOne: Chapter = {
	id: 'chapter-01',
	title: 'O Início',
	description: 'BOH começa sua jornada de ensino',
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
		// Static display steps
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
		// Laugh animation
		s09: {
			type: 'animation',
			id: 's09',
			expression: 'open mouth',
			speaker: 'boh',
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
		// Continue
		s10: {
			type: 'dialogue',
			id: 's10',
			text: 'Ai ai, sou meio comédia às vezes, sabe?',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 's11'
		},
		s11: {
			type: 'dialogue',
			id: 's11',
			text: 'Mas, enfim,',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 's12'
		},
		s12: {
			type: 'dialogue',
			id: 's12',
			text: 'E você, como se chama?',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 's13'
		},
		// Name input
		s13: {
			type: 'input',
			id: 's13',
			prompt: 'Digite seu nome aqui:',
			speaker: 'boh',
			expression: 'idle',
			minLength: 1,
			nextStepId: 's14'
		},
		// After name
		s14: {
			type: 'dialogue',
			id: 's14',
			text: 'Olha olha olha, na verdade, eu não tenho muito tempo...',
			speaker: 'boh',
			expression: 'pokerface',
			delay: 1.5,
			nextStepId: 's15'
		},
		s15: {
			type: 'dialogue',
			id: 's15',
			text: 'Me desculpa! Você parece ser uma pessoa muito legal, mas...',
			speaker: 'boh',
			expression: 'idle',
			nextStepId: 's16'
		},
		s16: {
			type: 'dialogue',
			id: 's16',
			text: 'A pessoa que me mandou aqui, queria falar sobre ↓ isso ↓',
			speaker: 'boh',
			expression: 'looking down',
			delay: 0.2,
			nextStepId: 's17'
		},
		// Show linked list model
		s17: {
			type: 'dialogue',
			id: 's17',
			text: 'Reconhece?',
			speaker: 'boh',
			expression: 'idle',
			staticArt: '\n\n\n\n                        None × ‹[H]» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[T]» × None',
			delay: 1.5,
			nextStepId: 's18'
		},
		// First choice
		s18: {
			type: 'choice',
			id: 's18',
			text: 'Reconhece isso aqui, né?',
			speaker: 'boh',
			expression: 'looking down',
			staticArt: '\n\n\n\n                        None × ‹[H]» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[T]» × None',
			choices: [
				{ label: 'Sim', key: 's', targetId: 's19' },
				{ label: 'Não', key: 'n', targetId: 's20' }
			],
			timeout: 5,
			timeoutTargetId: 's21'
		},
		// Yes branch
		s19: {
			type: 'dialogue',
			id: 's19',
			text: 'Pois é, uma lista.',
			speaker: 'boh',
			expression: 'idle',
			staticArt: '\n\n\n\n                        None × ‹[H]» ‹[]» ‹[]» ... ‹[]» ‹[]» ‹[T]» × None',
			nextStepId: 's22'
		},
		// No branch
		s20: {
			type: 'dialogue',
			id: 's20',
			text: 'Não?',
			speaker: 'boh',
			expression: 'open mouth',
			staticArt麻将

// (truncated due to length - will continue extracting)
	}
}