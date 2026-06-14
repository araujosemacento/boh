/**
 * Modular audio manager using the Web Audio API.
 * Sounds are mapped by key, making them swappable without changing business logic.
 */
interface AudioConfig {
	[key: string]: string;
}

// Default mapping to the copied legacy assets
const defaultAudioMapping: AudioConfig = {
	// We use a generic 'typing' key that plays one of the available clips randomly
	typing: '/assets/audio/',
};

export class AudioManager {
	ctx: AudioContext | null = null;
	buffers: Map<string, AudioBuffer> = new Map();
	gainNode: GainNode | null = null;
	mapping: AudioConfig;

	constructor(mapping: AudioConfig = defaultAudioMapping) {
		this.mapping = mapping;
	}

	async init(): Promise<void> {
		if (this.ctx) return;
		this.ctx = new AudioContext();
		this.gainNode = this.ctx.createGain();
		this.gainNode.gain.value = 0.7;
		this.gainNode.connect(this.ctx.destination);

		// Pre-load all known audio files
		await this.loadAll();
	}

	async loadAll(): Promise<void> {
		// For typing, we don't know the exact filenames at compile time
		// so we'll use the available ones from the legacy assets
		const typingFiles = [
			'p03voice_calm#1.wav',
			'p03voice_calm#2.wav',
			'p03voice_calm#3.wav',
			'p03voice_calm#4.wav',
			'p03voice_calm#5.wav',
			'p03voice_calm#6.wav',
			'p03voice_calm#7.wav',
			'p03voice_calm#8.wav',
			'p03voice_calm#9.wav',
		];

		for (const file of typingFiles) {
			const key = `typing_${file}`;
			if (!this.buffers.has(key)) {
				try {
					await this.loadBuffer(key, `/assets/audio/${file}`);
				} catch (e) {
					// Ignore missing files (e.g., .crdownload)
				}
			}
		}
	}

	async loadBuffer(key: string, url: string): Promise<AudioBuffer> {
		if (this.buffers.has(key)) return this.buffers.get(key)!;
		if (!this.ctx) throw new Error('AudioContext not initialized');

		const response = await fetch(url);
		const arrayBuffer = await response.arrayBuffer();
		const audioBuffer = await this.ctx.decodeAudioData(arrayBuffer);
		this.buffers.set(key, audioBuffer);
		return audioBuffer;
	}

	playTyping(): void {
		if (!this.ctx || !this.gainNode) return;

		// Play a random typing sound
		const typingKeys = Array.from(this.buffers.keys()).filter((k) => k.startsWith('typing_'));
		if (typingKeys.length === 0) return;

		const randomKey = typingKeys[Math.floor(Math.random() * typingKeys.length)];
		const buffer = this.buffers.get(randomKey);
		if (!buffer) return;

		const source = this.ctx.createBufferSource();
		source.buffer = buffer;
		// Connect to a shorter gain node that can be interrupted
		const node = this.ctx.createGain();
		node.gain.value = 0.7;
		node.connect(this.ctx.destination);
		source.connect(node);
		source.start();
	}

	setVolume(value: number): void {
		if (this.gainNode) {
			this.gainNode.gain.value = Math.max(0, Math.min(1, value));
		}
	}
}
