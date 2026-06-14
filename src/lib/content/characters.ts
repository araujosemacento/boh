export interface CharacterExpression {
	name: string;
	frames: string[];
}

export interface Character {
	id: string;
	name: string;
	/** Multi-line ASCII art for the character's default appearance */
	defaultArt: string;
	/** Available expressions and their corresponding ASCII art frames */
	expressions: Record<string, CharacterExpression>;
}

export const BOH: Character = {
	id: 'boh',
	name: 'BOH',
	defaultArt: '[ ▀ ¸ ▀]',
	expressions: {
		idle: {
			name: 'idle',
			frames: [
				'[ ▀ ¸ ▀]',
				'[ ▀ ° ▀]',
				'[ ▀ ■ ▀]',
				'[ ▀ ─ ▀]',
				'[ ▀ ~ ▀]',
				'[ ▀ ▄ ▀]',
				'[ ▀ ¬ ▀]',
				'[ ▀ · ▀]',
				'[ ▀ _ ▀]',
			],
		},
		pokerface: {
			name: 'pokerface',
			frames: ['[ ▀ ‗ ▀]', '[ ▀ ¯ ▀]', '[ ▀ ¡ ▀]'],
		},
		thinking: {
			name: 'thinking',
			frames: ['[ ─ ´ ─]', '[ ─ » ─]'],
		},
		'open mouth': {
			name: 'open mouth',
			frames: ['[ ▀ ß ▀]', '[ ▀ █ ▀]'],
		},
		annoyed: {
			name: 'annoyed',
			frames: ['[ ▀ ı ▀]', '[ ▀ ^ ▀]'],
		},
		'looking down': {
			name: 'looking down',
			frames: ['[ ▄ . ▄]', '[ ▄ _ ▄]', '[ ▄ ₒ ▄]', '[ ▄ ‗ ▄]'],
		},
	},
};

export const AUX: Character = {
	id: 'aux',
	name: 'AUX',
	defaultArt: `
      __
  _  |@@|
 / \\ \\-/ __
 ) O|----|  |   __
/ / \\ }{ /\\ )_ / _\\
 )/  /\\__/\\ \\__O (__
|/  (--/\\--)    \\__/
/   _)(  )(_
   \`---''---\`
`,
	expressions: {
		idle: {
			name: 'idle',
			frames: [],
		},
	},
};

export const characters: Record<string, Character> = {
	boh: BOH,
	aux: AUX,
};
