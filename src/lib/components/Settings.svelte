<script lang="ts">
	import { Palette, IceCreamBowl, Soup, CupSoda, Beaker, Coffee } from '@lucide/svelte';
</script>

<div class="dropdown">
	<div tabindex="0" role="button" class="btn btn-secondary m-1 p-2"><Palette /></div>
	<ul tabindex="-1" class="dropdown-content bg-base-300 rounded-box w-40 z-1 p-2 shadow-2xl">
		<li>
			<IceCreamBowl />
			<input
				type="radio"
				name="theme-dropdown"
				class="theme-controller w-full btn btn-sm btn-block btn-ghost justify-start"
				aria-label="Padrão"
				value="default"
				checked
			/>
		</li>
		<li>
			<Soup />
			<input
				type="radio"
				name="theme-dropdown"
				class="theme-controller w-full btn btn-sm btn-block btn-ghost justify-start"
				aria-label="Latte"
				value="latte"
			/>
		</li>
		<li>
			<CupSoda />
			<input
				type="radio"
				name="theme-dropdown"
				class="theme-controller w-full btn btn-sm btn-block btn-ghost justify-start"
				aria-label="Frappé"
				value="frappe"
			/>
		</li>
		<li>
			<Beaker />
			<input
				type="radio"
				name="theme-dropdown"
				class="theme-controller w-full btn btn-sm btn-block btn-ghost justify-start"
				aria-label="Macchiato"
				value="macchiato"
			/>
		</li>
		<li>
			<Coffee />
			<input
				type="radio"
				name="theme-dropdown"
				class="theme-controller w-full btn btn-sm btn-block btn-ghost justify-start"
				aria-label="Mocha"
				value="mocha"
			/>
		</li>
	</ul>
</div>
