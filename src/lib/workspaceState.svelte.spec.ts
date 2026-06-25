import { describe, it, expect, beforeEach, vi } from 'vitest';
import { WorkspaceState } from './workspaceState.svelte';
import type { Project } from './types';

describe('WorkspaceState', () => {
	let project: Project;

	beforeEach(() => {
		localStorage.clear();
		project = {
			id: 'proj-1',
			name: 'Test Project',
			description: 'Test Desc',
			tag: 'Draft',
			nodes: {
				start: {
					id: 'start',
					type: 'start',
					x: 100,
					y: 100,
					expression: 'idle',
					text: '',
					nextId: 'node-1'
				},
				'node-1': {
					id: 'node-1',
					type: 'boh',
					x: 300,
					y: 100,
					expression: 'idle',
					text: 'Olá do Boh!'
				}
			}
		};
	});

	it('should initialize correctly with project data', () => {
		const workspace = new WorkspaceState(project);
		expect(workspace.project).toEqual(project);
		expect(workspace.nodes['start']).toBeDefined();
		expect(workspace.nodes['node-1']).toBeDefined();
		expect(workspace.pan).toEqual({ x: 0, y: 0 });
		expect(workspace.zoom).toBe(1);
	});

	it('should derive connected input and output IDs correctly', () => {
		const workspace = new WorkspaceState(project);
		// start is connected to node-1
		expect(workspace.connectedInputIds.has('node-1')).toBe(true);
		expect(workspace.connectedInputIds.has('start')).toBe(false);

		expect(workspace.connectedOutputIds.has('start')).toBe(true);
		expect(workspace.connectedOutputIds.has('node-1')).toBe(false);
	});

	it('should add a Boh node', () => {
		const workspace = new WorkspaceState(project);
		workspace.addBohNode();

		const keys = Object.keys(workspace.nodes);
		expect(keys.length).toBe(3);

		const addedNodeId = keys.find((k) => k !== 'start' && k !== 'node-1')!;
		expect(workspace.nodes[addedNodeId]).toBeDefined();
		expect(workspace.nodes[addedNodeId].type).toBe('boh');
		expect(workspace.nodes[addedNodeId].expression).toBe('idle');
		expect(workspace.selectedNodeId).toBe(addedNodeId);
	});

	it('should add a Boh node at exact position when specified', () => {
		const workspace = new WorkspaceState(project);
		workspace.addBohNode(500, 600, true);

		const keys = Object.keys(workspace.nodes);
		const addedNodeId = keys.find((k) => k !== 'start' && k !== 'node-1')!;
		const node = workspace.nodes[addedNodeId];
		expect(node.x).toBe(500);
		expect(node.y).toBe(600);
	});

	it('should delete a node and clean up connections referencing it', () => {
		const workspace = new WorkspaceState(project);
		// start -> node-1
		workspace.deleteNode('node-1');

		expect(workspace.nodes['node-1']).toBeUndefined();
		// start's nextId should be cleared
		expect(workspace.nodes['start'].nextId).toBeUndefined();
	});

	it('should not delete the start node', () => {
		const workspace = new WorkspaceState(project);
		workspace.deleteNode('start');
		expect(workspace.nodes['start']).toBeDefined();
	});

	it('should remove connection', () => {
		const workspace = new WorkspaceState(project);
		workspace.removeConnection('start');
		expect(workspace.nodes['start'].nextId).toBeUndefined();
	});

	it('should organize nodes layout using tidyNodes', () => {
		const workspace = new WorkspaceState(project);
		workspace.tidyNodes();

		expect(workspace.nodes['start'].x).toBe(100);
		expect(workspace.nodes['start'].y).toBe(100);
		expect(workspace.nodes['node-1'].x).toBe(440);
		expect(workspace.nodes['node-1'].y).toBe(100);
	});

	it('should calculate fitView zoom and pan correctly', () => {
		const workspace = new WorkspaceState(project);
		const mockCanvas = document.createElement('div');
		Object.defineProperty(mockCanvas, 'clientWidth', { value: 1000 });
		Object.defineProperty(mockCanvas, 'clientHeight', { value: 800 });
		workspace.canvasElement = mockCanvas;

		workspace.fitView();
		expect(workspace.zoom).toBeGreaterThan(0);
		expect(workspace.pan.x).toBeDefined();
		expect(workspace.pan.y).toBeDefined();
	});

	it('should start and perform palette resizing', () => {
		const workspace = new WorkspaceState(project);
		const event = new MouseEvent('mousedown', { clientX: 240 });
		workspace.startResize(event, 'palette');

		expect(workspace.resizingColumn).toBe('palette');
		expect(workspace.resizeStartX).toBe(240);
		expect(workspace.resizeStartWidth).toBe(240);

		// Drag 50px right
		const moveEvent = new MouseEvent('mousemove', { clientX: 290 });
		workspace.handleGlobalMouseMove(moveEvent);
		expect(workspace.paletteWidth).toBe(290);

		// Mouse up ends resizing
		workspace.handleGlobalMouseUp(moveEvent);
		expect(workspace.resizingColumn).toBeNull();
	});

	it('should start and perform terminal resizing', () => {
		const workspace = new WorkspaceState(project);
		const event = new MouseEvent('mousedown', { clientX: 800 });
		workspace.startResize(event, 'terminal');

		expect(workspace.resizingColumn).toBe('terminal');
		expect(workspace.resizeStartX).toBe(800);
		expect(workspace.resizeStartWidth).toBe(480);

		// Drag 50px left (resizing from right panel) -> terminal width increases
		const moveEvent = new MouseEvent('mousemove', { clientX: 750 });
		workspace.handleGlobalMouseMove(moveEvent);
		expect(workspace.terminalWidth).toBe(530);

		workspace.handleGlobalMouseUp(moveEvent);
		expect(workspace.resizingColumn).toBeNull();
	});

	it('should handle canvas mouse panning', () => {
		const workspace = new WorkspaceState(project);
		const downEvent = new MouseEvent('mousedown', { button: 0, clientX: 100, clientY: 100 });
		workspace.handleCanvasMouseDown(downEvent);
		expect(workspace.isPanning).toBe(true);

		// Move mouse to 150, 120 -> delta is +50, +20
		const moveEvent = new MouseEvent('mousemove', { clientX: 150, clientY: 120 });
		workspace.handleGlobalMouseMove(moveEvent);
		expect(workspace.pan).toEqual({ x: 50, y: 20 });

		workspace.handleGlobalMouseUp(moveEvent);
		expect(workspace.isPanning).toBe(false);
	});

	it('should handle zoom wheel event', () => {
		const workspace = new WorkspaceState(project);
		const mockCanvas = document.createElement('div');
		vi.spyOn(mockCanvas, 'getBoundingClientRect').mockReturnValue({
			left: 0,
			top: 0,
			width: 1000,
			height: 800,
			right: 1000,
			bottom: 800,
			x: 0,
			y: 0,
			toJSON: () => {}
		});
		workspace.canvasElement = mockCanvas;

		const zoomInWheel = new WheelEvent('wheel', { deltaY: -100, clientX: 500, clientY: 400 });
		workspace.handleWheel(zoomInWheel);
		expect(workspace.zoom).toBeGreaterThan(1);

		const zoomOutWheel = new WheelEvent('wheel', { deltaY: 100, clientX: 500, clientY: 400 });
		workspace.handleWheel(zoomOutWheel);
		// Zoom should decrease
		expect(workspace.zoom).toBeLessThan(3);
	});

	it('should handle dragging a node', () => {
		const workspace = new WorkspaceState(project);
		const event = new MouseEvent('mousedown', { button: 0, clientX: 300, clientY: 100 });
		workspace.handleNodeHeaderMouseDown(event, 'node-1');

		expect(workspace.draggedNodeId).toBe('node-1');

		// Drag node by 100px right, 50px down
		const moveEvent = new MouseEvent('mousemove', { clientX: 400, clientY: 150 });
		workspace.handleGlobalMouseMove(moveEvent);

		// Position should be updated
		expect(workspace.nodes['node-1'].x).toBe(400);
		expect(workspace.nodes['node-1'].y).toBe(150);

		workspace.handleGlobalMouseUp(moveEvent);
		expect(workspace.draggedNodeId).toBeNull();
	});

	it('should handle connecting nodes with mouse', () => {
		const workspace = new WorkspaceState(project);
		workspace.nodes['start'].nextId = undefined; // clear existing connection

		const mockCanvas = document.createElement('div');
		vi.spyOn(mockCanvas, 'getBoundingClientRect').mockReturnValue({
			left: 0,
			top: 0,
			width: 1000,
			height: 800,
			right: 1000,
			bottom: 800,
			x: 0,
			y: 0,
			toJSON: () => {}
		});
		workspace.canvasElement = mockCanvas;

		// Mouse down on start output port
		const downEvent = new MouseEvent('mousedown', { clientX: 228, clientY: 120 });
		workspace.handleOutputPortMouseDown(downEvent, 'start');
		expect(workspace.connectingFromId).toBe('start');

		// Move mouse close to node-1's input port (x: 300, y: 118)
		const moveEvent = new MouseEvent('mousemove', { clientX: 305, clientY: 120 });
		workspace.handleGlobalMouseMove(moveEvent);
		expect(workspace.mouseX).toBe(305);

		// Mouse up triggers connection if within threshold
		workspace.handleGlobalMouseUp(moveEvent);
		expect(workspace.connectingFromId).toBeNull();
		expect(workspace.nodes['start'].nextId).toBe('node-1');
	});

	it('should save project to localStorage', () => {
		const workspace = new WorkspaceState(project);
		vi.spyOn(window, 'alert').mockImplementation(() => {});

		workspace.saveProject();

		const saved = localStorage.getItem('saved-projects-v2');
		expect(saved).not.toBeNull();
		const list = JSON.parse(saved!);
		expect(list[0].id).toBe('proj-1');
	});

	it('should mock exportJson', () => {
		const workspace = new WorkspaceState(project);
		const appendSpy = vi.spyOn(document.body, 'appendChild').mockImplementation((node) => node);

		// To avoid actually clicking a real anchor, we spy on HTMLAnchorElement.prototype.click
		const clickSpy = vi
			.spyOn(window.HTMLAnchorElement.prototype, 'click')
			.mockImplementation(() => {});
		const removeSpy = vi
			.spyOn(window.HTMLAnchorElement.prototype, 'remove')
			.mockImplementation(() => {});

		workspace.exportJson();

		const a = appendSpy.mock.calls[0][0] as HTMLAnchorElement;
		expect(a.download).toBe('test project_dialogue.json');
		expect(clickSpy).toHaveBeenCalled();
		expect(appendSpy).toHaveBeenCalled();
		expect(removeSpy).toHaveBeenCalled();

		appendSpy.mockRestore();
		clickSpy.mockRestore();
		removeSpy.mockRestore();
	});
});
