import { describe, it, expect } from 'vitest';
import {
	getOutputPortPos,
	getInputPortPos,
	findFreePosition,
	getNonOutlierNodes,
	tidyNodesLayout,
	NODE_WIDTH,
	NODE_HEIGHT,
	START_WIDTH
} from './canvasUtils';
import type { DialogueNode } from '../types';

describe('canvasUtils', () => {
	describe('getOutputPortPos and getInputPortPos', () => {
		it('should return correct ports for start node', () => {
			const node: DialogueNode = {
				id: 'start',
				type: 'start',
				x: 100,
				y: 100,
				expression: 'idle',
				text: ''
			};
			expect(getOutputPortPos(node)).toEqual({ x: 100 + START_WIDTH, y: 100 + 20 });
			expect(getInputPortPos(node)).toEqual({ x: 100, y: 100 + 18 });
		});

		it('should return correct ports for boh node', () => {
			const node: DialogueNode = {
				id: 'node-1',
				type: 'boh',
				x: 200,
				y: 200,
				expression: 'idle',
				text: 'Hey'
			};
			expect(getOutputPortPos(node)).toEqual({ x: 200 + NODE_WIDTH, y: 200 + 18 });
			expect(getInputPortPos(node)).toEqual({ x: 200, y: 200 + 18 });
		});
	});

	describe('findFreePosition', () => {
		it('should return start position if there are no nodes', () => {
			const pos = findFreePosition({}, 100, 100, NODE_WIDTH, NODE_HEIGHT);
			expect(pos).toEqual({ x: 100, y: 100 });
		});

		it('should nudge position to the right if there is an overlapping node', () => {
			const nodes: Record<string, DialogueNode> = {
				'node-1': { id: 'node-1', type: 'boh', x: 100, y: 100, expression: 'idle', text: '' }
			};
			const pos = findFreePosition(nodes, 100, 100, NODE_WIDTH, NODE_HEIGHT, 40);
			expect(pos).toEqual({ x: 100 + NODE_WIDTH + 40, y: 100 });
		});

		it('should search iteratively if multiple overlaps exist', () => {
			const nodes: Record<string, DialogueNode> = {
				'node-1': { id: 'node-1', type: 'boh', x: 100, y: 100, expression: 'idle', text: '' },
				'node-2': { id: 'node-2', type: 'boh', x: 428, y: 100, expression: 'idle', text: '' }
			};
			const pos = findFreePosition(nodes, 100, 100, NODE_WIDTH, NODE_HEIGHT, 40);
			expect(pos).toEqual({ x: 756, y: 100 });
		});
	});

	describe('getNonOutlierNodes', () => {
		it('should return all nodes if there are 2 or fewer', () => {
			const nodes: Record<string, DialogueNode> = {
				start: { id: 'start', type: 'start', x: 100, y: 100, expression: 'idle', text: '' },
				'node-1': { id: 'node-1', type: 'boh', x: 200, y: 200, expression: 'idle', text: '' }
			};
			const result = getNonOutlierNodes(nodes);
			expect(result.length).toBe(2);
		});

		it('should filter out outlier nodes that are too far', () => {
			const nodes: Record<string, DialogueNode> = {
				start: { id: 'start', type: 'start', x: 0, y: 0, expression: 'idle', text: '' },
				'node-1': { id: 'node-1', type: 'boh', x: 100, y: 0, expression: 'idle', text: '' },
				'node-2': { id: 'node-2', type: 'boh', x: 200, y: 0, expression: 'idle', text: '' },
				outlier: { id: 'outlier', type: 'boh', x: 5000, y: 5000, expression: 'idle', text: '' }
			};
			const result = getNonOutlierNodes(nodes);
			expect(result.map((n) => n.id)).not.toContain('outlier');
			expect(result.map((n) => n.id)).toContain('start');
			expect(result.map((n) => n.id)).toContain('node-1');
			expect(result.map((n) => n.id)).toContain('node-2');
		});
	});

	describe('tidyNodesLayout', () => {
		it('should return empty array if nodes is empty', () => {
			expect(tidyNodesLayout({})).toEqual([]);
		});

		it('should layout main chain and isolated nodes', () => {
			const nodes: Record<string, DialogueNode> = {
				start: {
					id: 'start',
					type: 'start',
					x: 0,
					y: 0,
					expression: 'idle',
					text: '',
					nextId: 'node-1'
				},
				'node-1': {
					id: 'node-1',
					type: 'boh',
					x: 0,
					y: 0,
					expression: 'idle',
					text: '',
					nextId: 'node-2'
				},
				'node-2': { id: 'node-2', type: 'boh', x: 0, y: 0, expression: 'idle', text: '' },
				isolated: { id: 'isolated', type: 'boh', x: 0, y: 0, expression: 'idle', text: '' }
			};
			const layout = tidyNodesLayout(nodes);
			expect(layout.length).toBe(4);
			const startPos = layout.find((p) => p.id === 'start')!;
			const n1Pos = layout.find((p) => p.id === 'node-1')!;
			const n2Pos = layout.find((p) => p.id === 'node-2')!;
			const isolatedPos = layout.find((p) => p.id === 'isolated')!;

			expect(startPos.x).toBe(100);
			expect(startPos.y).toBe(100);
			expect(n1Pos.x).toBe(100 + 340);
			expect(n1Pos.y).toBe(100);
			expect(n2Pos.x).toBe(100 + 340 * 2);
			expect(n2Pos.y).toBe(100);

			expect(isolatedPos.x).toBe(100);
			expect(isolatedPos.y).toBe(500);
		});
	});
});
