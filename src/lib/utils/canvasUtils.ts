import type { DialogueNode } from '../types';

export const NODE_WIDTH = 288;
export const NODE_HEIGHT = 240;
export const START_WIDTH = 128;
export const START_HEIGHT = 40;

export const getOutputPortPos = (node: DialogueNode) => {
	if (node.type === 'start') return { x: node.x + START_WIDTH, y: node.y + 20 };
	return { x: node.x + NODE_WIDTH, y: node.y + 18 };
};

export const getInputPortPos = (node: DialogueNode) => {
	return { x: node.x, y: node.y + 18 };
};

/**
 * Encontra uma coordenada de posicionamento livre à direita de nós existentes
 * para evitar sobreposições indesejadas ao realizar spawn via cliques.
 */
export const findFreePosition = (
	nodes: Record<string, DialogueNode>,
	startX: number,
	startY: number,
	width: number,
	height: number,
	gap = 40
): { x: number; y: number } => {
	let x = startX;
	const y = startY;
	let attempts = 0;

	while (attempts < 100) {
		let overlapFound = false;
		for (const node of Object.values(nodes)) {
			const w = node.type === 'start' ? START_WIDTH : NODE_WIDTH;
			const h = node.type === 'start' ? START_HEIGHT : NODE_HEIGHT;

			const overlapX = x < node.x + w && x + width > node.x;
			const overlapY = y < node.y + h && y + height > node.y;

			if (overlapX && overlapY) {
				x = node.x + w + gap;
				overlapFound = true;
				break;
			}
		}

		if (!overlapFound) {
			break;
		}
		attempts++;
	}

	return { x, y };
};

/**
 * Retorna uma lista de nós excluindo os "outliers" (nós excessivamente distantes do cluster principal).
 */
export const getNonOutlierNodes = (nodes: Record<string, DialogueNode>): DialogueNode[] => {
	const nodeList = Object.values(nodes);
	if (nodeList.length <= 2) return nodeList;

	const nnDistances = nodeList.map((node) => {
		let minD = Infinity;
		nodeList.forEach((other) => {
			if (other.id !== node.id) {
				const d = Math.hypot(node.x - other.x, node.y - other.y);
				if (d < minD) minD = d;
			}
		});
		return { node, minD };
	});

	const sortedDists = [...nnDistances].map((item) => item.minD).sort((a, b) => a - b);
	const medianD = sortedDists[Math.floor(sortedDists.length / 2)];

	// Limite generoso para outlier: 1200px ou 4x a mediana da distância do vizinho mais próximo
	const threshold = Math.max(1200, medianD * 4);
	const filtered = nnDistances
		.filter((item) => item.node.id === 'start' || item.minD < threshold)
		.map((item) => item.node);

	return filtered.length > 0 ? filtered : nodeList;
};

export interface TidyPosition {
	id: string;
	x: number;
	y: number;
}

/**
 * Calcula um layout organizado em ziguezague (serpenteante) para as sequências de nós conectadas
 * e posiciona os nós soltos/isolados em formato de grade organizada no rodapé do grafo.
 */
export const tidyNodesLayout = (nodes: Record<string, DialogueNode>): TidyPosition[] => {
	const allNodeIds = Object.keys(nodes);
	if (allNodeIds.length === 0) return [];

	const visited = new Set<string>();
	const chains: string[][] = [];

	// 1. Rastreia a cadeia principal a partir do 'start'
	if (nodes['start']) {
		const mainChain: string[] = [];
		let currentId: string | undefined = 'start';
		while (currentId && nodes[currentId] && !visited.has(currentId)) {
			visited.add(currentId);
			mainChain.push(currentId);
			currentId = nodes[currentId].nextId;
		}
		chains.push(mainChain);
	}

	// 2. Rastreia outras subcadeias conectadas
	let remainingIds = allNodeIds.filter((id) => !visited.has(id));
	while (remainingIds.length > 0) {
		const targets = new Set(remainingIds.map((id) => nodes[id].nextId).filter(Boolean));
		let rootId = remainingIds.find((id) => !targets.has(id));

		if (!rootId) {
			rootId = remainingIds[0];
		}

		const subChain: string[] = [];
		let currentId: string | undefined = rootId;
		while (currentId && nodes[currentId] && !visited.has(currentId)) {
			visited.add(currentId);
			subChain.push(currentId);
			currentId = nodes[currentId].nextId;
		}

		if (subChain.length > 0) {
			chains.push(subChain);
		}
		remainingIds = allNodeIds.filter((id) => !visited.has(id));
	}

	// Separa cadeias de tamanho > 1 (cadeias de diálogo de fato) de nós isolados (tamanho 1)
	const connectedChains = chains.filter(
		(c) => c.length > 1 || (c.length === 1 && c[0] === 'start')
	);
	const isolatedNodes = chains.filter((c) => c.length === 1 && c[0] !== 'start').map((c) => c[0]);

	const positions: TidyPosition[] = [];

	const startX = 100;
	let currentY = 100;

	const maxWidth = 1400; // Largura limite para quebra de linha
	const rowGap = 320; // Espaçamento vertical generoso para as splines de carriage return
	const colGap = 80; // Espaçamento horizontal

	// Posiciona as cadeias conectadas no formato Leitura (esquerda para direita com word-wrap)
	for (const chain of connectedChains) {
		let currentX = startX;
		let lineMaxY = currentY;

		for (const id of chain) {
			const node = nodes[id];
			const w = node?.type === 'start' ? START_WIDTH : NODE_WIDTH;

			if (currentX + w > startX + maxWidth && currentX !== startX) {
				currentX = startX;
				currentY += rowGap;
			}

			positions.push({
				id,
				x: currentX,
				y: currentY
			});

			currentX += w + colGap;
			if (currentY > lineMaxY) lineMaxY = currentY;
		}
		currentY = lineMaxY + rowGap + 80;
	}

	// Posiciona os nós isolados/desconectados em grade estruturada no rodapé
	if (isolatedNodes.length > 0) {
		let currentX = startX;

		for (const id of isolatedNodes) {
			const w = NODE_WIDTH;

			if (currentX + w > startX + maxWidth && currentX !== startX) {
				currentX = startX;
				currentY += rowGap;
			}

			positions.push({
				id,
				x: currentX,
				y: currentY
			});

			currentX += w + colGap;
		}
	}

	return positions;
};
