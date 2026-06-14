// Catppuccin color palettes
export type Flavor = 'latte' | 'frappe' | 'macchiato' | 'mocha';
export type ThemeOption = 'system' | Flavor;

export const STORAGE_KEY = 'boh-theme';

export const flavorLabels: Record<Flavor, string> = {
	latte: 'Latte',
	frappe: 'Frappe',
	macchiato: 'Macchiato',
	mocha: 'Mocha',
};

export const flavorDescriptions: Record<Flavor, string> = {
	latte: 'Claro e arejado',
	frappe: 'Suave e acolhedor',
	macchiato: 'Médio e equilibrado',
	mocha: 'Escuro e elegante',
};

export interface Palette {
	ctp: Record<string, string>;
	base: string;
	surface0: string;
	surface1: string;
	surface2: string;
	text: string;
	subtext0: string;
	subtext1: string;
	overlay0: string;
	overlay1: string;
	mantle: string;
	crust: string;
	pink: string;
	mauve: string;
	red: string;
	maroon: string;
	peach: string;
	yellow: string;
	green: string;
	teal: string;
	sky: string;
	sapphire: string;
	blue: string;
	lavender: string;
}

// https://catppuccin.com/palette
export const palettes: Record<Flavor, Palette> = {
	latte: {
		ctp: {},
		base: '#eff1f5',
		surface0: '#ccd0da',
		surface1: '#bcc0cc',
		surface2: '#acb0be',
		text: '#4c4f69',
		subtext0: '#6c6f85',
		subtext1: '#5c5f77',
		overlay0: '#9ca0b0',
		overlay1: '#8c909b',
		mantle: '#e6e9ef',
		crust: '#dce0e8',
		pink: '#ea76cb',
		mauve: '#8839ef',
		red: '#d20f39',
		maroon: '#e64553',
		peach: '#fe640b',
		yellow: '#df8e1d',
		green: '#40a02b',
		teal: '#179299',
		sky: '#04a5e5',
		sapphire: '#209fb5',
		blue: '#1e66f5',
		lavender: '#7287fd',
	},
	frappe: {
		ctp: {},
		base: '#303446',
		surface0: '#414559',
		surface1: '#51576d',
		surface2: '#626880',
		text: '#c6d0f5',
		subtext0: '#a5adce',
		subtext1: '#b5bddf',
		overlay0: '#737994',
		overlay1: '#838ba7',
		mantle: '#292c3c',
		crust: '#232634',
		pink: '#f4b8e4',
		mauve: '#ca9ee6',
		red: '#e78284',
		maroon: '#ea999c',
		peach: '#ef9f76',
		yellow: '#e5c890',
		green: '#a6d189',
		teal: '#81c8be',
		sky: '#99d1db',
		sapphire: '#85c1dc',
		blue: '#8caaee',
		lavender: '#babbe1',
	},
	macchiato: {
		ctp: {},
		base: '#24273a',
		surface0: '#363a4f',
		surface1: '#494d64',
		surface2: '#5b6078',
		text: '#cad3f5',
		subtext0: '#a5adcb',
		subtext1: '#b8c0e0',
		overlay0: '#6e738d',
		overlay1: '#8087a2',
		mantle: '#1e2030',
		crust: '#181926',
		pink: '#f5bde6',
		mauve: '#c6a0f6',
		red: '#ed8796',
		maroon: '#ee99a0',
		peach: '#f5a97f',
		yellow: '#eed49f',
		green: '#a6da95',
		teal: '#8bd5ca',
		sky: '#91d7e3',
		sapphire: '#7dc4e4',
		blue: '#8aadf4',
		lavender: '#b7bdf8',
	},
	mocha: {
		ctp: {},
		base: '#1e1e2e',
		surface0: '#313244',
		surface1: '#45475a',
		surface2: '#585b70',
		text: '#cdd6f4',
		subtext0: '#a6adc8',
		subtext1: '#bac2de',
		overlay0: '#6c7086',
		overlay1: '#7f849c',
		mantle: '#181825',
		crust: '#11111b',
		pink: '#f5c2e7',
		mauve: '#cba6f7',
		red: '#f38ba8',
		maroon: '#eba0ac',
		peach: '#fab387',
		yellow: '#f9e2af',
		green: '#a6e3a1',
		teal: '#94e2d5',
		sky: '#89dceb',
		sapphire: '#74c7ec',
		blue: '#89b4fa',
		lavender: '#b4befe',
	},
};
