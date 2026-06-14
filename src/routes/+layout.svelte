<script lang="ts">
	import './layout.css';
	import { onMount } from 'svelte';
	import { themeStore } from '$lib/stores/theme.svelte';
	import ThemeSwitcher from '$lib/components/ThemeSwitcher.svelte';
	import favicon from '$lib/assets/favicon.svg';

	let { children } = $props();

	onMount(() => {
		themeStore.init();
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<div
	class="min-h-screen"
	style:background-color="var(--ctp-base)"
	style:color="var(--ctp-text)"
>
	{@render children()}
</div>

<ThemeSwitcher />
