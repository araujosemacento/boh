<script lang="ts">
	import ProjectWorkspace from './ProjectWorkspace.svelte';
	import type { Project } from '../types';

	let { initialProject, onBack } = $props<{ initialProject: Project; onBack: () => void }>();
	let project = $state(initialProject);
</script>

<ProjectWorkspace bind:project {onBack} />
