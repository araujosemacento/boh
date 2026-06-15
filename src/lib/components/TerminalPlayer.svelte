<script lang="ts">
    import type { Project } from '$lib/content/project';
    import { StoryEngine } from '$lib/engine/storyEngine.svelte';
    import { AudioManager } from '$lib/engine/audioManager';
    import { characters } from '$lib/content/characters';
    import { ArrowLeft, Pause, Play, SkipForward, Home } from 'lucide-svelte';

    interface Props {
        project: Project;
        onClose: () => void;
    }

    let { project, onClose }: Props = $props();

// Disable audio for now to avoid missing file errors
const audio = null;
const engine = new StoryEngine(project.chapter, audio);

let textInput = $state('');

$effect(() => {
    engine.init();
});

    function getExpressionFrame(): string {
        const char = characters[engine.speaker];
        if (!char) return '';
        const expr = char.expressions[engine.expression];
        if (!expr || expr.frames.length === 0) return char.defaultArt;
        const frameIndex = Math.floor(Date.now() / 500) % Math.max(1, expr.frames.length);
        return expr.frames[frameIndex] || char.defaultArt;
    }

    // Wraps a string and replaces arrow characters with colored spans when arrows should be highlighted
    function colorize(text: string, enabled: boolean): string {
        if (!enabled || !text) return text;
        // Orange crimson for simple arrows (‹ ›) and blue for double arrows (« »)
        return text
            .replace(/[‹›]/g, (m) => `${m}`)
            .replace(/[«»]/g, (m) => `${m}`);
    }

    function arrowHtml(text: string, enabled: boolean): { __html: string } | null {
        if (!enabled || !text) return null;
        const html = text
            .replace(/[&<>]/g, (c) => ({ '&': '&', '<': '<', '>': '>' }[c]!))
            .replace(/[‹›]/g, (m) => `<span style="color: var(--ctp-peach)">${m}</span>`)
            .replace(/[«»]/g, (m) => `<span style="color: var(--ctp-blue)">${m}</span>` );
        return { __html: html };
    }

    function handleClick() {
        if (engine.isWaitingForTextInput) {
            /* Let the text input keep focus */
            return;
        }
        engine.advance();
    }

    function handleKeydown(event: KeyboardEvent) {
        const step = engine.currentStep;

        // Choice handling: shortcut keys for each choice option
        if (engine.isWaitingForChoice && step?.type === 'choice') {
            const match = step.choices.find((c) => c.key.toLowerCase() === event.key.toLowerCase());
            if (match) {
                event.preventDefault();
                engine.selectChoice(match.targetId);
                return;
            }
        }

        // Text input
        if (engine.isWaitingForTextInput) {
            if (event.key === 'Enter') {
                event.preventDefault();
                const value = textInput.trim();
                if (!value) return;
                engine.submitInput(value);
                textInput = '';
            }
            return;
        }

        if (event.key === ' ' || event.key === 'Enter' || event.key === 'ArrowRight') {
            event.preventDefault();
            engine.advance();
        } else if (event.key === 'Escape') {
            event.preventDefault();
            onClose();
        }
    }

    function submitInput() {
        const value = textInput.trim();
        if (!value) return;
        engine.submitInput(value);
        textInput = '';
    }
</script>

<svelte:window onkeydown={handleKeydown} />

<div
    class="fixed inset-0 z-50 flex items-center justify-center"
    style:background-color="var(--ctp-crust)"
    role="presentation"
>
    <!-- Terminal Window -->
    <div
        class="terminal-window flex flex-col overflow-hidden rounded-lg shadow-2xl border"
        style:background-color="var(--ctp-base)"
        style:border="1px solid var(--ctp-surface0)"
    >
        <!-- Title Bar -->
        <div
            class="flex items-center justify-between px-4 py-2"
            style:background-color="var(--ctp-mantle)"
            style:border-bottom="1px solid var(--ctp-surface0)"
        >
            <!-- Traffic light buttons -->
            <div class="flex space-x-2">
                <button
                    onclick={onClose}
                    class="h-3 w-3 rounded-full"
                    style:background-color="var(--ctp-red)"
                    title="Fechar"
                    aria-label="Fechar"
                ></button>
                <button
                    disabled
                    class="h-3 w-3 rounded-full"
                    style:background-color="var(--ctp-yellow)"
                    title="Minimizar"
                    aria-label="Minimizar"
                ></button>
                <button
                    disabled
                    class="h-3 w-3 rounded-full"
                    style:background-color="var(--ctp-green)"
                    title="Maximizar"
                    aria-label="Maximizar"
                ></button>
            </div>
            <span class="text-sm font-mono ml-4" style:color="var(--ctp-subtext0)">{project.title}</span>
        </div>

        <!-- Terminal Content -->
        <div
            class="flex flex-1 flex-col gap-4 p-6 font-mono cursor-pointer overflow-y-auto"
            onclick={handleClick}
            role="button"
            tabindex="0"
            aria-label="Avancar dialogo"
            onkeydown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    engine.advance();
                }
            }}
        >
            <!-- Character and Dialogue Area -->
            <div class="flex flex-1 flex-col gap-4">
                {#if engine.speaker && characters[engine.speaker]}
                    <div class="flex items-start gap-4">
                        <pre
                            class="text-lg leading-none whitespace-pre"
                            style:color="var(--ctp-green)">{getExpressionFrame()}</pre>
                        <div class="flex-1">
                            <span class="font-bold" style:color="var(--ctp-green)"
                                >{characters[engine.speaker].name}:</span
                            >
                        </div>
                    </div>
                {/if}

                <!-- Typed Text -->
                <div class="min-h-[2rem] text-lg" style:color="var(--ctp-text)">
                    {#if engine.typedText}
                        {#if engine.colorizeArrows}
                            {#if arrowHtml(engine.typedText, true)}
                                <span>{@html arrowHtml(engine.typedText, true).__html}</span>
                            {:else}
                                {engine.typedText}
                            {/if}
                        {:else}
                            {engine.typedText}
                        {/if}
                        <span class="animate-pulse" style:color="var(--ctp-overlay0)">_</span>
                    {:else if engine.isTyping}
                        <span class="animate-pulse" style:color="var(--ctp-overlay0)">_</span>
                    {/if}
                </div>

                <!-- Static Art -->
                {#if engine.staticArt}
                    {#if engine.colorizeArrows}
                        {#if arrowHtml(engine.staticArt, true)}
                            <pre
                                class="whitespace-pre"
                                style:color="var(--ctp-subtext0)">{@html arrowHtml(engine.staticArt, true).__html}</pre>
                        {:else}
                            <pre class="whitespace-pre" style:color="var(--ctp-subtext0)">{engine.staticArt}</pre>
                        {/if}
                    {:else}
                        <pre class="whitespace-pre" style:color="var(--ctp-subtext0)">{engine.staticArt}</pre>
                    {/if}
                {/if}

                <!-- Prompt text for choice/input (above the input row) -->
                {#if engine.isWaitingForInput && engine.promptText}
                    <div class="text-base italic" style:color="var(--ctp-yellow)">
                        {engine.promptText}
                    </div>
                {/if}
            </div>

            <!-- User Input / Choices -->
            {#if engine.isWaitingForChoice && engine.currentStep?.type === 'choice'}
                <div class="flex flex-wrap gap-3">
                    {#each engine.currentStep.choices as choice}
                        <button
                            onclick={() => engine.selectChoice(choice.targetId)}
                            class="rounded border px-4 py-2 text-sm transition-colors duration-200 hover:opacity-80"
                            style:border-color="var(--ctp-surface0)"
                            style:background-color="var(--ctp-surface0)"
                            style:color="var(--ctp-green)"
                        >
                            [{choice.key.toUpperCase()}] {choice.label}
                        </button>
                    {/each}
                </div>
            {/if}

            <!-- Text input -->
            {#if engine.isWaitingForTextInput}
                <form
                    onsubmit={(e) => {
                        e.preventDefault();
                        submitInput();
                    }}
                    class="flex gap-2"
                >
                    <input
                        type="text"
                        bind:value={textInput}
                        autofocus
                        placeholder="Digite aqui..."
                        class="flex-1 rounded border px-3 py-2 font-mono text-sm focus:outline-none"
                        style:background-color="var(--ctp-surface0)"
                        style:border-color="var(--ctp-surface1)"
                        style:color="var(--ctp-text)"
                        onclick={(e) => e.stopPropagation()}
                    />
                    <button
                        type="submit"
                        class="rounded px-4 py-2 text-sm font-bold transition-opacity hover:opacity-80"
                        style:background-color="var(--ctp-green)"
                        style:color="var(--ctp-base)"
                    >
                        OK
                    </button>
                </form>
            {/if}

            <!-- Prompt hint -->
            <div class="mt-4 text-center text-xs" style:color="var(--ctp-overlay0)">
                {#if engine.isTyping}
                    Clique ou pressione Enter para pular
                {:else if engine.isWaitingForTextInput}
                    Digite e pressione Enter para enviar
                {:else if engine.isWaitingForChoice}
                    Use {engine.currentStep?.type === 'choice'
                        ? engine.currentStep.choices.map((c) => `[${c.key.toUpperCase()}]`).join(' / ')
                        : ''} ou clique
                {:else}
                    Clique ou pressione Enter para continuar
                {/if}
            </div>
        </div>
    </div>
</div>

<style>
    .terminal-window {
        width: 90vw;
        max-width: 64rem;
        height: 90vh;
        max-height: 48rem;
        border-radius: 0.5rem;
        box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
        border: 1px solid var(--ctp-surface0);
        overflow: hidden;
    }
    /* Ensure the content area scrolls if needed */
    .terminal-window > .flex.flex-1 {
        padding-bottom: 0.5rem; /* extra space for prompt hint */
    }
</style>