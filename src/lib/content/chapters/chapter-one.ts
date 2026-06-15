import type { Chapter } from '../types';

export const chapterOne: Chapter = {
	id: 'chapter-01',
	title: 'Listas Encadeadas',
	description: 'BOH explica listas duplamente encadeadas',
	initialStepId: 's01',
	steps: {
		// Intro dialogue steps
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

		 // Expanded storyline
		 s10: {
			 type: 'dialogue',
			 id: 's10',
			 text: 'Ai ai, sou meio comedia as vezes, sabe?',
			 speaker: 'boh',
			 expression: 'idle',
			 nextStepId: 's11'
		 },
		 // Prompt user for name
		 s11: {
			 type: 'input',
			 id: 's11',
			 prompt: 'Qual é o seu nome?',
			 speaker: 'boh',
			 expression: 'idle',
			 nextStepId: 's12'
		 },
		 // Acknowledge name (placeholder variable {{input:s11}})
		 s12: {
			 type: 'dialogue',
			 id: 's12',
			 text: 'Prazer em te conhecer, {{input:s11}}! Vamos continuar.',
			 speaker: 'boh',
			 expression: 'idle',
			 nextStepId: 's13'
		 },
		 // Laugh animation again
		 s13: {
			 type: 'animation',
			 id: 's13',
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
			 nextStepId: 's14'
		 },
		 s14: {
			 type: 'dialogue',
			 id: 's14',
			 text: 'Mas, enfim,',
			 speaker: 'boh',
			 expression: 'idle',
			 nextStepId: 's15'
		 },
		 // Introduce AUX with ASCII art
		 s15: {
			 type: 'dialogue',
			 id: 's15',
			 text: 'Meu mano aqui se chama AUX,',
			 speaker: 'aux',
			 expression: 'idle',
			 staticArt: `
       __
   _  |@@|
  / \\ \\--/ __
  ) O|----|  |   __
 / / \\ }{ /\\ )_ / _\\
 )/  /\\__/\\ \\__O (__)
|/  (--/\\--)    \\__/
/   _)(  )(_
   \`---''---\`
`,
			 nextStepId: 's16'
		 },
		 s16: {
			 type: 'dialogue',
			 id: 's16',
			 text: 'Tudo bem contigo, patrão?',
			 speaker: 'aux',
			 expression: 'idle',
			 nextStepId: 's17'
		 },
		 // Arrow colorization demo
		 s17: {
			 type: 'dialogue',
			 id: 's17',
			 text: 'Vamos mostrar setas coloridas: ‹›«»',
			 speaker: 'boh',
			 expression: 'idle',
			 colorizeArrows: true,
			 staticArt: 'None × ‹›«»',
			 nextStepId: 'end'
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