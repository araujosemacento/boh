<script lang="ts">
    import { themeStore } from '$lib/stores/theme.svelte';
    import {
        flavorLabels,
        flavorDescriptions,
        type Flavor,
        type ThemeOption,
        palettes,
    } from '$lib/themes/catppuccin';
    import { Settings, X, Smartphone } from 'lucide-svelte';

    let isOpen = $state(false);

    const flavors: Flavor[] = ['latte', 'frappe', 'macchiato', 'mocha'];

    function handleSelect(option: ThemeOption) {
        themeStore.setTheme(option);
    }

    function toggle() {
        isOpen = !isOpen;
    }

    function close() {
        isOpen = false;
    }

    function selectSystem() {
        handleSelect('system');
        close();
    }

    function selectFlavor(flavor: Flavor) {
        handleSelect(flavor);
        close();
    }
</script>

<!-- Floating trigger button -->
<button
    onclick={toggle}
    class="fixed bottom-6 right-6 z-50 flex h-12 w-12 items-center justify-center rounded-full shadow-lg transition-all duration-300 hover:scale-110"
    style:background-color="var(--ctp-surface0)"
    style:color="var(--ctp-blue)"
    aria-label="Configuracoes"
>
    {#if isOpen}
        <X class="h-6 w-6" />
    {:else}
        <Settings class="h-6 w-6" />
    {/if}
</button>

<!-- Popover / Overlay -->
{#if isOpen}
    <div
        role="presentation"
        class="fixed inset-0 z-40 bg-black/20 backdrop-blur-sm transition-opacity duration-300"
        onclick={close}
    ></div>

    <div
        class="fixed bottom-20 right-6 z-50 w-80 overflow-hidden rounded-2xl shadow-2xl transition-all duration-300"
        style:background-color="var(--ctp-base)"
        style:border="1px solid var(--ctp-surface1)"
    >
        <!-- Header -->
        <div
            class="flex items-center justify-between px-5 py-4"
            style:border-bottom="1px solid var(--ctp-surface0)"
        >
            <h2 class="text-lg font-bold" style:color="var(--ctp-text)">Tema</h2>
            <button
                onclick={close}
                class="rounded-full p-1 transition-colors hover:bg-white/10"
                style:color="var(--ctp-subtext0)"
            >
                <X class="h-5 w-5" />
            </button>
        </div>

        <!-- System option -->
        <button
            class="flex w-full items-center gap-3 px-5 py-4 text-left transition-colors duration-200 hover:bg-white/5"
            class:opacity-60={themeStore.themeOption !== 'system'}
            onclick={selectSystem}
        >
            <div
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
                style:background-color="var(--ctp-surface0)"
            >
                <Smartphone class="h-5 w-5" color="var(--ctp-text)" />
            </div>
            <div class="min-w-0">
                <div class="font-semibold" style:color="var(--ctp-text)">Padrao</div>
                <div class="truncate text-sm" style:color="var(--ctp-subtext0)">
                    Automatico ({themeStore.effectiveFlavor === 'latte' ? 'Latte' : 'Mocha'})
                </div>
            </div>
            {#if themeStore.themeOption === 'system'}
                <div class="ml-auto h-2.5 w-2.5 rounded-full" style:background-color="var(--ctp-green)"></div>
            {/if}
        </button>

        <!-- Divider -->
        <div class="mx-5 h-px" style:background-color="var(--ctp-surface0)"></div>

        <!-- Flavors -->
        <div class="py-2">
            {#each flavors as flavor}
                {@const isSelected = themeStore.themeOption === flavor}
                {@const palette = palettes[flavor]}
                <button
                    class="flex w-full items-center gap-3 px-5 py-3 text-left transition-colors duration-200 hover:bg-white/5"
                    class:opacity-60={!isSelected}
                    onclick={() => selectFlavor(flavor)}
                >
                    <div
                        class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-sm font-bold"
                        style:background-color={palette.surface1}
                        style:color={palette.text}
                    >
                        {flavor[0].toUpperCase()}
                    </div>
                    <div class="min-w-0">
                        <div class="font-semibold" style:color="var(--ctp-text)">
                            {flavorLabels[flavor]}
                        </div>
                        <div class="truncate text-sm" style:color="var(--ctp-subtext0)">
                            {flavorDescriptions[flavor]}
                        </div>
                    </div>
                    {#if isSelected}
                        <div class="ml-auto h-2.5 w-2.5 rounded-full" style:background-color="var(--ctp-green)"></div>
                    {/if}
                </button>
            {/each}
        </div>
    </div>
{/if}
