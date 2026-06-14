import type { Project, ProjectExport } from '$lib/content/project';
import { chapterOne } from '$lib/content/chapters/chapter-one';

const STORAGE_KEY = 'boh_projects';

function createProjectStore() {
	let projects = $state<Project[]>(loadProjects());

	function loadProjects(): Project[] {
		if (typeof window === 'undefined') return [createDemoProject()];
		const stored = localStorage.getItem(STORAGE_KEY);
		if (!stored) return [createDemoProject()];
		try {
			const parsed = JSON.parse(stored);
			if (Array.isArray(parsed) && parsed.length > 0) {
				return parsed;
			}
			return [createDemoProject()];
		} catch {
			return [createDemoProject()];
		}
	}

	function save() {
		if (typeof window !== 'undefined') {
			localStorage.setItem(STORAGE_KEY, JSON.stringify(projects));
		}
	}

	function createDemoProject(): Project {
		return {
			id: 'demo-linked-list',
			title: 'Listas Encadeadas',
			description: 'BOH explica como reverter uma lista duplamente encadeada usando uma variavel auxiliar.',
			createdAt: Date.now(),
			updatedAt: Date.now(),
			chapter: chapterOne,
		};
	}

	return {
		get projects() { return projects; },
		addProject(project: Project) {
			projects = [...projects, project];
			save();
		},
		removeProject(id: string) {
			projects = projects.filter(p => p.id !== id);
			save();
		},
		updateProject(id: string, updates: Partial<Project>) {
			projects = projects.map(p => p.id === id ? { ...p, ...updates, updatedAt: Date.now() } : p);
			save();
		},
		exportProject(id: string): string {
			const project = projects.find(p => p.id === id);
			if (!project) throw new Error('Project not found');
			const exportData: ProjectExport = {
				version: '1.0',
				project
			};
			return JSON.stringify(exportData, null, 2);
		},
		importProject(json: string): Project {
			const data = JSON.parse(json) as ProjectExport;
			const newId = Math.random().toString(36).substring(2, 9) + Date.now().toString(36);
			const project = { ...data.project, id: newId, createdAt: Date.now(), updatedAt: Date.now() };
			projects = [...projects, project];
			save();
			return project;
		}
	};
}

export const projectStore = createProjectStore();
