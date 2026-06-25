import type { Project, DialogueNode } from './types';
import { SvelteSet } from 'svelte/reactivity';
import {
	findFreePosition,
	getNonOutlierNodes,
	tidyNodesLayout,
	NODE_WIDTH,
	NODE_HEIGHT
} from './utils/canvasUtils';

export class WorkspaceState {
	// Referência do projeto e nós reativos
	project = $state<Project>() as Project;
	nodes = $state<Record<string, DialogueNode>>({});

	// Estado de navegação do Canvas
	pan = $state({ x: 0, y: 0 });
	zoom = $state(1);
	isPanning = $state(false);
	isSpaceDown = $state(false);

	// Estado do arraste de nós
	draggedNodeId = $state<string | null>(null);
	dragStartOffset = { x: 0, y: 0 };

	// Estado de criação de conexões
	connectingFromId = $state<string | null>(null);
	mouseX = $state(0);
	mouseY = $state(0);

	// Estado do redimensionador de colunas
	paletteWidth = $state(240);
	terminalWidth = $state(480);
	resizingColumn = $state<'palette' | 'terminal' | null>(null);
	resizeStartX = 0;
	resizeStartWidth = 0;

	// Estados de seleção
	activePlayNodeId = $state<string | null>(null);
	selectedNodeIds = $state<string[]>([]);

	// Estado do Box Selection
	isBoxSelecting = $state(false);
	selectionBoxStart = $state({ x: 0, y: 0 });
	selectionBoxEnd = $state({ x: 0, y: 0 });

	// Estado do Autosave
	saveStatus = $state<'idle' | 'saving' | 'saved'>('idle');
	private saveTimeout: ReturnType<typeof setTimeout> | null = null;

	// Referência do DOM para cálculos
	canvasElement: HTMLDivElement | null = $state(null);

	constructor(initialProject: Project) {
		this.project = initialProject;
		this.nodes = { ...initialProject.nodes };
	}

	// Propriedades derivadas reativas em formato getter
	get connectedInputIds() {
		return new SvelteSet(
			Object.values(this.nodes)
				.map((n) => n.nextId)
				.filter(Boolean) as string[]
		);
	}

	get connectedOutputIds() {
		return new SvelteSet(
			Object.values(this.nodes)
				.filter((n) => n.nextId)
				.map((n) => n.id)
		);
	}

	/**
	 * Cria um novo nó de fala do Boh.
	 * @param customX Posição horizontal customizada (opcional).
	 * @param customY Posição vertical customizada (opcional).
	 * @param exactPosition Se verdadeiro, ignora desvios de colisão e posiciona exatamente nas coordenadas fornecidas.
	 */
	addBohNode = (customX?: number, customY?: number, exactPosition = false) => {
		const id = `node-${Date.now()}`;
		let x: number;
		let y: number;

		if (customX !== undefined && customY !== undefined) {
			if (exactPosition) {
				x = customX;
				y = customY;
			} else {
				const pos = findFreePosition(this.nodes, customX, customY, NODE_WIDTH, NODE_HEIGHT);
				x = pos.x;
				y = pos.y;
			}
		} else {
			// Calcula posição baseada no centro do viewport visível
			let startX = 200;
			let startY = 150;

			if (this.canvasElement) {
				const rect = this.canvasElement.getBoundingClientRect();
				startX = (-this.pan.x + rect.width / 2) / this.zoom - NODE_WIDTH / 2;
				startY = (-this.pan.y + rect.height / 2) / this.zoom - NODE_HEIGHT / 2;
			}

			// Tenta referenciar a partir do nó selecionado ou o mais próximo do centro
			let refNode =
				this.selectedNodeIds.length > 0
					? this.nodes[this.selectedNodeIds[this.selectedNodeIds.length - 1]]
					: null;
			if (!refNode) {
				let minD = Infinity;
				Object.values(this.nodes).forEach((n) => {
					const d = Math.hypot(n.x - startX, n.y - startY);
					if (d < minD) {
						minD = d;
						refNode = n;
					}
				});
			}

			if (refNode) {
				const refW = refNode.type === 'start' ? 128 : NODE_WIDTH;
				startX = refNode.x + refW + 40;
				startY = refNode.y;
			}

			const pos = findFreePosition(this.nodes, startX, startY, NODE_WIDTH, NODE_HEIGHT);
			x = pos.x;
			y = pos.y;
		}

		this.nodes[id] = {
			id,
			type: 'boh',
			x,
			y,
			expression: 'idle',
			text: 'Olá! Escreva o diálogo aqui.'
		};

		this.selectedNodeIds = [id];
	};

	deleteNode = (id: string) => {
		if (id === 'start') return;
		const newNodes = { ...this.nodes };
		Object.keys(newNodes).forEach((key) => {
			if (newNodes[key].nextId === id) newNodes[key].nextId = undefined;
		});
		delete newNodes[id];
		this.nodes = newNodes;
		if (this.selectedNodeIds.includes(id)) {
			this.selectedNodeIds = this.selectedNodeIds.filter((sid) => sid !== id);
		}
		if (this.activePlayNodeId === id) this.activePlayNodeId = null;
	};

	removeConnection = (fromId: string) => {
		if (this.nodes[fromId]) {
			this.nodes[fromId].nextId = undefined;
		}
	};

	tidyNodes = () => {
		const positions = tidyNodesLayout(this.nodes);
		positions.forEach((pos) => {
			if (this.nodes[pos.id]) {
				this.nodes[pos.id].x = pos.x;
				this.nodes[pos.id].y = pos.y;
			}
		});
	};

	fitView = () => {
		if (!this.canvasElement || Object.keys(this.nodes).length === 0) return;

		const filtered = getNonOutlierNodes(this.nodes);
		let minX = Infinity;
		let maxX = -Infinity;
		let minY = Infinity;
		let maxY = -Infinity;

		filtered.forEach((n) => {
			const w = n.type === 'start' ? 128 : NODE_WIDTH;
			const h = n.type === 'start' ? 40 : NODE_HEIGHT;
			if (n.x < minX) minX = n.x;
			if (n.x + w > maxX) maxX = n.x + w;
			if (n.y < minY) minY = n.y;
			if (n.y + h > maxY) maxY = n.y + h;
		});

		const padding = 60;
		const boxWidth = maxX - minX + 2 * padding;
		const boxHeight = maxY - minY + 2 * padding;
		const boxCenterX = minX - padding + boxWidth / 2;
		const boxCenterY = minY - padding + boxHeight / 2;

		const canvasWidth = this.canvasElement.clientWidth;
		const canvasHeight = this.canvasElement.clientHeight;

		let targetZoom = Math.min(canvasWidth / boxWidth, canvasHeight / boxHeight);
		targetZoom = Math.max(0.25, Math.min(1.2, targetZoom));

		this.zoom = targetZoom;
		this.pan = {
			x: canvasWidth / 2 - boxCenterX * this.zoom,
			y: canvasHeight / 2 - boxCenterY * this.zoom
		};
	};

	// Manipuladores de Eventos do Canvas
	handleCanvasMouseDown = (e: MouseEvent) => {
		if (e.button === 1 || (e.button === 0 && this.isSpaceDown)) {
			e.preventDefault();
			this.isPanning = true;
			this.dragStartOffset = { x: e.clientX - this.pan.x, y: e.clientY - this.pan.y };
			return; // Stop here so we don't trigger box selection
		}
		if (e.button === 0) {
			if (!e.shiftKey && !e.ctrlKey) {
				this.selectedNodeIds = [];
			}
			this.isBoxSelecting = true;
			if (this.canvasElement) {
				const rect = this.canvasElement.getBoundingClientRect();
				this.selectionBoxStart = { x: e.clientX - rect.left, y: e.clientY - rect.top };
				this.selectionBoxEnd = { x: e.clientX - rect.left, y: e.clientY - rect.top };
			}
		}
	};

	handleWheel = (e: WheelEvent) => {
		if (!this.canvasElement) return;
		e.preventDefault();

		const rect = this.canvasElement.getBoundingClientRect();
		const mx = e.clientX - rect.left;
		const my = e.clientY - rect.top;

		const zoomFactor = 1.08;
		let newZoom: number;
		if (e.deltaY < 0) {
			newZoom = Math.min(3, this.zoom * zoomFactor);
		} else {
			newZoom = Math.max(0.15, this.zoom / zoomFactor);
		}

		if (newZoom !== this.zoom) {
			const cx = (mx - this.pan.x) / this.zoom;
			const cy = (my - this.pan.y) / this.zoom;
			this.zoom = newZoom;
			this.pan.x = mx - cx * this.zoom;
			this.pan.y = my - cy * this.zoom;
		}
	};

	findPath = (startId: string, targetId: string): string[] | null => {
		const visited = new SvelteSet<string>();
		const queue: { id: string; path: string[] }[] = [{ id: startId, path: [startId] }];
		while (queue.length > 0) {
			const { id, path } = queue.shift()!;
			if (id === targetId) return path;
			visited.add(id);
			const node = this.nodes[id];
			if (node && node.nextId && !visited.has(node.nextId)) {
				queue.push({ id: node.nextId, path: [...path, node.nextId] });
			}
		}
		return null;
	};

	getConnectedChain = (startId: string): string[] => {
		const chain: string[] = [];
		let currentId: string | undefined = startId;
		const visited = new SvelteSet<string>();
		while (currentId && this.nodes[currentId] && !visited.has(currentId)) {
			chain.push(currentId);
			visited.add(currentId);
			currentId = this.nodes[currentId].nextId;
		}
		return chain;
	};

	handleNodeHeaderMouseDown = (e: MouseEvent, id: string) => {
		if (e.button !== 0) return; // Apenas arraste com botão esquerdo
		e.stopPropagation();
		e.preventDefault();

		if (e.ctrlKey || e.metaKey) {
			if (this.selectedNodeIds.includes(id)) {
				this.selectedNodeIds = this.selectedNodeIds.filter((sid) => sid !== id);
			} else {
				this.selectedNodeIds = [...this.selectedNodeIds, id];
			}
		} else if (e.shiftKey) {
			const lastSelected = this.selectedNodeIds[this.selectedNodeIds.length - 1];
			if (lastSelected && lastSelected !== id && this.nodes[lastSelected]) {
				const path = this.findPath(lastSelected, id);
				if (path) {
					const newSelection = new SvelteSet(this.selectedNodeIds);
					path.forEach((p) => newSelection.add(p));
					this.selectedNodeIds = Array.from(newSelection);
				} else {
					const chain = this.getConnectedChain(lastSelected);
					const newSelection = new SvelteSet([...this.selectedNodeIds, ...chain, id]);
					this.selectedNodeIds = Array.from(newSelection);
				}
			} else {
				this.selectedNodeIds = [...this.selectedNodeIds, id];
			}
		} else {
			if (!this.selectedNodeIds.includes(id)) {
				this.selectedNodeIds = [id];
			}
		}

		this.draggedNodeId = id;
		this.dragStartOffset = { x: e.clientX, y: e.clientY };
	};

	handleOutputPortMouseDown = (e: MouseEvent, id: string) => {
		e.stopPropagation();
		e.preventDefault();
		if (this.nodes[id].nextId) {
			this.nodes[id].nextId = undefined;
		}
		this.connectingFromId = id;
		if (this.canvasElement) {
			const rect = this.canvasElement.getBoundingClientRect();
			this.mouseX = (e.clientX - rect.left - this.pan.x) / this.zoom;
			this.mouseY = (e.clientY - rect.top - this.pan.y) / this.zoom;
		}
	};

	handleGlobalMouseMove = (e: MouseEvent) => {
		if (this.resizingColumn) {
			const delta = e.clientX - this.resizeStartX;
			if (this.resizingColumn === 'palette') {
				this.paletteWidth = Math.max(180, Math.min(400, this.resizeStartWidth + delta));
			} else {
				this.terminalWidth = Math.max(320, Math.min(700, this.resizeStartWidth - delta));
			}
			return;
		}

		if (this.isBoxSelecting && this.canvasElement) {
			const rect = this.canvasElement.getBoundingClientRect();
			this.selectionBoxEnd = { x: e.clientX - rect.left, y: e.clientY - rect.top };
		}

		if (this.isPanning) {
			this.pan = { x: e.clientX - this.dragStartOffset.x, y: e.clientY - this.dragStartOffset.y };
		}

		if (this.draggedNodeId) {
			const dx = e.clientX - this.dragStartOffset.x;
			const dy = e.clientY - this.dragStartOffset.y;

			if (this.selectedNodeIds.includes(this.draggedNodeId)) {
				for (const sid of this.selectedNodeIds) {
					const node = this.nodes[sid];
					if (node) {
						node.x += dx / this.zoom;
						node.y += dy / this.zoom;
					}
				}
			} else {
				const node = this.nodes[this.draggedNodeId];
				if (node) {
					node.x += dx / this.zoom;
					node.y += dy / this.zoom;
				}
			}
			this.dragStartOffset = { x: e.clientX, y: e.clientY };
		}

		if (this.connectingFromId && this.canvasElement) {
			const rect = this.canvasElement.getBoundingClientRect();
			this.mouseX = (e.clientX - rect.left - this.pan.x) / this.zoom;
			this.mouseY = (e.clientY - rect.top - this.pan.y) / this.zoom;
		}
	};

	handleGlobalMouseUp = (e: MouseEvent) => {
		if (this.resizingColumn) {
			this.resizingColumn = null;
			return;
		}

		if (this.isBoxSelecting) {
			this.isBoxSelecting = false;
			const minX = Math.min(this.selectionBoxStart.x, this.selectionBoxEnd.x);
			const maxX = Math.max(this.selectionBoxStart.x, this.selectionBoxEnd.x);
			const minY = Math.min(this.selectionBoxStart.y, this.selectionBoxEnd.y);
			const maxY = Math.max(this.selectionBoxStart.y, this.selectionBoxEnd.y);

			if (maxX - minX > 5 || maxY - minY > 5) {
				const cMinX = (minX - this.pan.x) / this.zoom;
				const cMaxX = (maxX - this.pan.x) / this.zoom;
				const cMinY = (minY - this.pan.y) / this.zoom;
				const cMaxY = (maxY - this.pan.y) / this.zoom;

				const newlySelected = Object.values(this.nodes)
					.filter((n) => {
						const w = n.type === 'start' ? 128 : NODE_WIDTH;
						const h = n.type === 'start' ? 40 : NODE_HEIGHT;
						return !(n.x > cMaxX || n.x + w < cMinX || n.y > cMaxY || n.y + h < cMinY);
					})
					.map((n) => n.id);

				const uniqueSelection = new SvelteSet([...this.selectedNodeIds, ...newlySelected]);
				this.selectedNodeIds = Array.from(uniqueSelection);
			}
		}

		this.isPanning = false;
		this.draggedNodeId = null;

		if (this.connectingFromId && this.canvasElement) {
			const rect = this.canvasElement.getBoundingClientRect();
			const mx = (e.clientX - rect.left - this.pan.x) / this.zoom;
			const my = (e.clientY - rect.top - this.pan.y) / this.zoom;

			const targetNode = Object.values(this.nodes).find((n) => {
				if (n.type === 'start' || n.id === this.connectingFromId) return false;
				return mx >= n.x && mx <= n.x + NODE_WIDTH && my >= n.y && my <= n.y + NODE_HEIGHT;
			});

			if (targetNode) {
				this.nodes[this.connectingFromId].nextId = targetNode.id;
			}

			this.connectingFromId = null;
		}
	};

	startResize = (e: MouseEvent, column: 'palette' | 'terminal') => {
		e.preventDefault();
		this.resizingColumn = column;
		this.resizeStartX = e.clientX;
		this.resizeStartWidth = column === 'palette' ? this.paletteWidth : this.terminalWidth;
	};

	// Métodos de Persistência / Import / Export
	saveProject = (silent = false) => {
		this.project.nodes = { ...this.nodes };
		const savedProjects = JSON.parse(localStorage.getItem('saved-projects-v2') || '[]');
		const idx = savedProjects.findIndex((p: Project) => p.id === this.project.id);
		const updated = { ...this.project, nodes: { ...this.nodes } };
		if (idx >= 0) savedProjects[idx] = updated;
		else savedProjects.push(updated);
		localStorage.setItem('saved-projects-v2', JSON.stringify(savedProjects));

		if (!silent) {
			alert('Projeto salvo localmente com sucesso!');
		}

		this.saveStatus = 'saved';
		if (this.saveTimeout) clearTimeout(this.saveTimeout);
		this.saveTimeout = setTimeout(() => {
			if (this.saveStatus === 'saved') this.saveStatus = 'idle';
		}, 2000);
	};

	debouncedSave = () => {
		this.saveStatus = 'saving';
		if (this.saveTimeout) clearTimeout(this.saveTimeout);
		this.saveTimeout = setTimeout(() => {
			this.saveProject(true);
		}, 1500);
	};

	exportJson = () => {
		const dataStr =
			'data:text/json;charset=utf-8,' +
			encodeURIComponent(JSON.stringify({ ...this.project, nodes: this.nodes }, null, 2));
		const a = document.createElement('a');
		a.href = dataStr;
		a.download = `${this.project.name.toLowerCase()}_dialogue.json`;
		document.body.appendChild(a);
		a.click();
		a.remove();
	};

	triggerImportJson = () => {
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.json';
		input.onchange = (e) => {
			const file = (e.target as HTMLInputElement).files?.[0];
			if (!file) return;
			const reader = new FileReader();
			reader.onload = (ev) => {
				try {
					const parsed = JSON.parse(ev.target?.result as string);
					if (parsed?.nodes) {
						this.nodes = parsed.nodes;
						alert('Grafo importado!');
					} else alert('JSON inválido.');
				} catch {
					alert('Erro ao processar JSON.');
				}
			};
			reader.readAsText(file);
		};
		input.click();
	};

	// Drag & Drop
	handleDragStart = (e: DragEvent) => {
		if (e.dataTransfer) {
			e.dataTransfer.setData('text/plain', 'boh');
			e.dataTransfer.effectAllowed = 'copy';
		}
	};

	handleDragOver = (e: DragEvent) => {
		e.preventDefault();
		if (e.dataTransfer) {
			e.dataTransfer.dropEffect = 'copy';
		}
	};

	handleDrop = (e: DragEvent) => {
		e.preventDefault();
		if (!this.canvasElement) return;

		const type = e.dataTransfer?.getData('text/plain');
		if (type === 'boh') {
			const rect = this.canvasElement.getBoundingClientRect();

			// Converte para coordenadas de canvas levando em conta pan e zoom
			const dropX = (e.clientX - rect.left - this.pan.x) / this.zoom;
			const dropY = (e.clientY - rect.top - this.pan.y) / this.zoom;

			// Posiciona no local exato do drop (centro do bloco)
			const targetX = dropX - NODE_WIDTH / 2;
			const targetY = dropY - 20;

			// Exclui nudging/colisão para priorizar a posição exata de drop do usuário
			this.addBohNode(targetX, targetY, true);
		}
	};
}
