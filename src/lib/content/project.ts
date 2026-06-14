import type { Chapter } from './types';

export interface Project {
	id: string;
	title: string;
	description: string;
	/** Timestamp when project was created */
	createdAt: number;
	/** Timestamp when project was last modified */
	updatedAt: number;
	/** The chapter/scene data for this project */
	chapter: Chapter;
	/** Custom characters defined in this project (in addition to shared ones) */
	customCharacters?: Record<string, { name: string; art: string }>;
}

/** Serializable version of a project for import/export */
export interface ProjectExport {
	version: string;
	project: Project;
}
