<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import IconButton from './internal/IconButton.vue'
import { gsap, motionSkipped, createFrameLoop, EASE } from './internal/motion'
import { useViewportPan } from './internal/viewport'
import type { OrgNode } from './types'
import './theme.css'

/* ============================================================
   组织架构画布：整齐树布局铺满内容区，四角留给页面放浮动玻璃面板（#tl / #tr / #bl / #br）。
   - 拖拽平移；Ctrl / ⌘ + 滚轮缩放（以指针为中心），普通滚轮平移
   - 连线先画到，节点才点亮（from: start，导 Conduct）
   - 单击选中（节点抬起），双击聚焦居中；方向键在选中节点间移动
   分支颜色取分类色，只用于区分一级分支。
   ============================================================ */
const props = withDefaults(defineProps<{
  tree: OrgNode[]
  selectedId?: number | null
  theme?: 'light' | 'dark'
}>(), { selectedId: null, theme: 'light' })
const emit = defineEmits<{ (e: 'select', dept: OrgNode): void }>()

const NODE_W = 196
const NODE_H = 72
const GAP_X = 26
const GAP_Y = 64

const viewport = ref<HTMLElement | null>(null)
const layer = ref<HTMLElement | null>(null)
const collapsed = ref<Set<number>>(new Set())

/* ── 视口：共享平移缩放，渲染走 quickTo（动画）/ gsap.set（直写） ── */
const { view, zoomAt, bind: bindViewport, unbind: unbindViewport, onPointerDown, onPointerMove, onPointerUp } = useViewportPan({
  el: () => viewport.value,
  apply: (_v, animate) => apply(animate),
  exclude: '.pg-org-node, .pg-org-controls, .pg-org-panel',
})

interface LaidNode { dept: OrgNode; x: number; y: number; depth: number; branch: number; parentId: number | null; hasChildren: boolean }

const layout = computed(() => {
  const nodes: LaidNode[] = []
  let cursor = 0
  const place = (dept: OrgNode, depth: number, branch: number, parentId: number | null): number => {
    const kids = collapsed.value.has(dept.id) ? [] : (dept.children ?? [])
    let x: number
    if (!kids.length) {
      x = cursor
      cursor += NODE_W + GAP_X
    } else {
      const xs = kids.map((child, i) => place(child, depth + 1, depth === 0 ? i : branch, dept.id))
      x = (xs[0] + xs[xs.length - 1]) / 2
    }
    nodes.push({ dept, x, y: depth * (NODE_H + GAP_Y), depth, branch, parentId, hasChildren: Boolean(dept.children?.length) })
    return x
  }
  props.tree.forEach((root, i) => place(root, 0, i, null))
  nodes.sort((a, b) => a.depth - b.depth || a.x - b.x)
  const byId = new Map(nodes.map(n => [n.dept.id, n]))
  const links = nodes.filter(n => n.parentId !== null && byId.has(n.parentId)).map((n) => {
    const p = byId.get(n.parentId!)!
    const px = p.x + NODE_W / 2
    const py = p.y + NODE_H
    const cx = n.x + NODE_W / 2
    const midY = py + GAP_Y / 2
    const r = Math.min(10, Math.abs(cx - px) / 2)
    const dir = cx > px ? 1 : -1
    const d = Math.abs(cx - px) < 1
      ? `M${px},${py} V${n.y}`
      : `M${px},${py} V${midY - r} Q${px},${midY} ${px + dir * r},${midY} H${cx - dir * r} Q${cx},${midY} ${cx},${midY + r} V${n.y}`
    return { id: n.dept.id, d, depth: n.depth, branch: n.branch }
  })
  const width = nodes.length ? Math.max(...nodes.map(n => n.x)) + NODE_W : 0
  const height = nodes.length ? Math.max(...nodes.map(n => n.y)) + NODE_H : 0
  return { nodes, links, width, height }
})

/* ── 视图变换：只动 layer 的 transform ── */
let setX: ((v: number) => void) | null = null
let setY: ((v: number) => void) | null = null
let setK: ((v: number) => void) | null = null
function apply(animate = true) {
  if (!layer.value) return
  if (!animate || motionSkipped() || !setX) {
    gsap.set(layer.value, { x: view.x, y: view.y, scale: view.k })
    return
  }
  setX(view.x); setY!(view.y); setK!(view.k)
}

/* 视口尺寸取布局像素（clientWidth / Height），页面进场缩放期间量到的包围盒会偏小 */
const viewportBox = () => (viewport.value ? { width: viewport.value.clientWidth, height: viewport.value.clientHeight } : null)

function fit(animate = true) {
  const box = viewportBox()
  const { width, height } = layout.value
  if (!box || !width) return
  const pad = 96
  view.k = Math.min(1.2, Math.max(0.35, Math.min((box.width - pad * 2) / width, (box.height - pad * 2.4) / height)))
  view.x = (box.width - width * view.k) / 2
  view.y = Math.max(pad, (box.height - height * view.k) / 2)
  apply(animate)
}

function focusNode(id: number) {
  const node = layout.value.nodes.find(n => n.dept.id === id)
  const box = viewportBox()
  if (!node || !box) return
  view.k = Math.max(view.k, 0.9)
  view.x = box.width / 2 - (node.x + NODE_W / 2) * view.k
  view.y = box.height / 2 - (node.y + NODE_H / 2) * view.k
  apply()
}

function toggle(id: number) {
  const next = new Set(collapsed.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  collapsed.value = next
}

/* 方向键：上 = 上级，下 = 第一个下级，左右 = 同级相邻 */
function onKey(event: KeyboardEvent, node: LaidNode) {
  const nodes = layout.value.nodes
  let target: LaidNode | undefined
  if (event.key === 'ArrowUp') target = nodes.find(n => n.dept.id === node.parentId)
  else if (event.key === 'ArrowDown') target = nodes.find(n => n.parentId === node.dept.id)
  else if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
    const row = nodes.filter(n => n.depth === node.depth).sort((a, b) => a.x - b.x)
    target = row[row.indexOf(node) + (event.key === 'ArrowRight' ? 1 : -1)]
  } else if (event.key === 'Enter') {
    focusNode(node.dept.id)
    return
  } else return
  event.preventDefault()
  if (!target) return
  emit('select', target.dept)
  nextTick(() => viewport.value?.querySelector<HTMLElement>(`[data-node="${target!.dept.id}"]`)?.focus())
}

/* ── 选中路径：根 → 当前节点的连线点亮并走脉冲 ── */
const activePathIds = computed(() => {
  const ids = new Set<number>()
  if (props.selectedId === null) return ids
  const parentOf = new Map(layout.value.nodes.map(n => [n.dept.id, n.parentId]))
  let cur: number | null | undefined = props.selectedId
  while (cur != null) {
    const parent = parentOf.get(cur)
    if (parent == null) break
    ids.add(cur)
    cur = parent
  }
  return ids
})
const pulseNodes = computed(() => {
  const out: Array<{ key: string; link: number; cat: number }> = []
  const branchOf = new Map(layout.value.links.map(l => [l.id, (l.branch % 5) + 1]))
  activePathIds.value.forEach((id) => {
    for (let k = 0; k < 2; k += 1) out.push({ key: `${id}:${k}`, link: id, cat: branchOf.get(id) ?? 1 })
  })
  return out
})
const pulseEls = new Map<string, { c: SVGCircleElement; link: number }>()
function bindPulse(key: string, el: unknown) {
  if (el instanceof SVGCircleElement) pulseEls.set(key, { c: el, link: Number(key.split(':')[0]) })
  else pulseEls.delete(key)
}
const pathLens = new Map<number, number>()
const pulseLoop = createFrameLoop((dt, t) => {
  pulseEls.forEach((p) => {
    const path = layer.value?.querySelector<SVGPathElement>(`.pg-org-link[data-link="${p.link}"]`)
    if (!path || motionSkipped()) { p.c.style.opacity = '0'; return }
    let len = pathLens.get(p.link)
    if (len === undefined) { len = path.getTotalLength(); pathLens.set(p.link, len) }
    const k = Number(p.c.dataset.idx ?? 0)
    const u = (t * 0.3 + k * 0.5 + (p.link % 7) * 0.11) % 1
    const pt = path.getPointAtLength(len * u)
    p.c.setAttribute('cx', String(pt.x))
    p.c.setAttribute('cy', String(pt.y))
    p.c.style.opacity = String(Math.sin(u * Math.PI) * 0.9)
  })
})

/* ── 入场：按层级先画连线，再点亮该层节点 ── */
let entrance: gsap.core.Timeline | null = null
function finishEntranceWhenHidden() {
  if (document.hidden) entrance?.progress(1, false)
}
function playEntrance() {
  if (!layer.value || motionSkipped()) return
  const tl = entrance = gsap.timeline()
  const depths = Math.max(0, ...layout.value.nodes.map(n => n.depth))
  for (let d = 0; d <= depths; d += 1) {
    const links = layer.value.querySelectorAll<SVGPathElement>(`.pg-org-link[data-depth="${d}"]`)
    links.forEach((path) => {
      const len = path.getTotalLength()
      tl.fromTo(path, { strokeDasharray: len, strokeDashoffset: len }, { strokeDashoffset: 0, duration: 0.42, ease: EASE.swap, clearProps: 'strokeDasharray,strokeDashoffset' }, d * 0.2 - 0.12 < 0 ? 0 : d * 0.2 - 0.12)
    })
    const nodes = layer.value.querySelectorAll(`.pg-org-node[data-depth="${d}"]`)
    tl.fromTo(nodes, { opacity: 0, y: 10, scale: 0.96 }, { opacity: 1, y: 0, scale: 1, duration: 0.5, ease: EASE.reveal, stagger: { each: 0.04, from: 'start' }, clearProps: 'opacity,transform' }, d * 0.2 + 0.18)
  }
}

let observer: ResizeObserver | null = null
let played = false
onMounted(() => {
  if (layer.value) {
    setX = gsap.quickTo(layer.value, 'x', { duration: 0.55, ease: 'power3.out' })
    setY = gsap.quickTo(layer.value, 'y', { duration: 0.55, ease: 'power3.out' })
    setK = gsap.quickTo(layer.value, 'scale', { duration: 0.55, ease: 'power3.out' })
  }
  observer = new ResizeObserver(() => fit(false))
  if (viewport.value) observer.observe(viewport.value)
  bindViewport()
  document.addEventListener('visibilitychange', finishEntranceWhenHidden)
  pulseLoop.start(viewport.value)
})
onBeforeUnmount(() => {
  observer?.disconnect()
  unbindViewport()
  pulseLoop.stop()
  document.removeEventListener('visibilitychange', finishEntranceWhenHidden)
  entrance?.kill()
  if (layer.value) {
    gsap.killTweensOf(layer.value)
    gsap.killTweensOf(layer.value.querySelectorAll('*'))
  }
})

watch(layout, async (value) => {
  pathLens.clear()
  if (!value.nodes.length) return
  await nextTick()
  if (!layer.value || !viewport.value) return
  fit(played)
  if (!played) {
    played = true
    playEntrance()
  }
}, { immediate: true })

defineExpose({ fit, focusNode })
</script>

<template>
  <div
    ref="viewport"
    class="pg-visual pg-org-canvas"
    :data-theme="theme"
    @pointerdown="onPointerDown"
    @pointermove="onPointerMove"
    @pointerup="onPointerUp"
    @pointercancel="onPointerUp"
  >
    <div class="pg-org-dots" aria-hidden="true" />
    <p v-if="!layout.nodes.length" class="pg-org-empty" role="status">暂无组织数据</p>
    <div ref="layer" class="pg-org-layer" :style="{ width: `${layout.width}px`, height: `${layout.height}px` }">
      <svg class="pg-org-links" :width="layout.width" :height="layout.height" aria-hidden="true">
        <path
          v-for="l in layout.links"
          :key="l.id"
          :d="l.d"
          :data-depth="l.depth"
          :data-link="l.id"
          :class="['pg-org-link', `pg-cat-${(l.branch % 5) + 1}`, { 'is-active': activePathIds.has(l.id) }]"
        />
        <circle
          v-for="p in pulseNodes"
          :key="p.key"
          :ref="el => bindPulse(p.key, el)"
          :data-idx="p.key.split(':')[1]"
          :class="['pg-org-pulse', `pg-cat-${p.cat}`]"
          r="3"
        />
      </svg>
      <div
        v-for="n in layout.nodes"
        :key="n.dept.id"
        role="button"
        tabindex="0"
        :data-node="n.dept.id"
        :data-depth="n.depth"
        :aria-pressed="n.dept.id === selectedId"
        :aria-label="`${n.dept.name}，${n.dept.memberCount ?? 0} 人`"
        :class="['pg-org-node', `pg-cat-${(n.branch % 5) + 1}`, { 'is-selected': n.dept.id === selectedId, 'is-root': n.depth === 0, 'is-off': n.dept.status === 0 }]"
        :style="{ left: `${n.x}px`, top: `${n.y}px`, width: `${NODE_W}px`, height: `${NODE_H}px` }"
        @click="emit('select', n.dept)"
        @dblclick="focusNode(n.dept.id)"
        @keydown="onKey($event, n)"
      >
        <span class="pg-org-strip" aria-hidden="true" />
        <span class="pg-org-name">{{ n.dept.name }}</span>
        <span class="pg-org-meta">
          <span class="pg-org-leader">{{ n.dept.leader || '未设负责人' }}</span>
          <span class="pg-org-count pg-tnum">{{ n.dept.memberCount ?? 0 }} 人</span>
        </span>
        <button
          v-if="n.hasChildren"
          type="button"
          class="pg-org-toggle"
          :aria-label="collapsed.has(n.dept.id) ? '展开下级' : '收起下级'"
          :aria-expanded="!collapsed.has(n.dept.id)"
          @keydown.stop
          @click.stop="toggle(n.dept.id)"
        >{{ collapsed.has(n.dept.id) ? (n.dept.children?.length ?? 0) : '−' }}</button>
      </div>
    </div>

    <div class="pg-org-panel is-tl"><slot name="tl" /></div>
    <div class="pg-org-panel is-tr"><slot name="tr" /></div>
    <div class="pg-org-panel is-bl"><slot name="bl" /></div>
    <div class="pg-org-panel is-br"><slot name="br" /></div>

    <div class="pg-org-controls pg-float" role="group" aria-label="画布缩放">
      <IconButton name="plus" label="放大" small @click="zoomAt(1.2)" />
      <IconButton name="minus" label="缩小" small @click="zoomAt(1 / 1.2)" />
      <IconButton name="fit" label="适应画布" small @click="fit()" />
    </div>
  </div>
</template>

<style scoped>
.pg-org-canvas {
  position: relative;
  height: var(--pg-canvas-height, max(560px, calc(100dvh - 252px)));
  overflow: hidden;
  border-radius: var(--pg-r-xl);
  border: 1px solid var(--pg-line-strong);
  background:
    radial-gradient(60% 50% at 18% 12%, var(--pg-ambient-a), transparent 70%),
    radial-gradient(50% 45% at 85% 90%, var(--pg-ambient-b), transparent 70%),
    var(--pg-surface-solid);
  box-shadow: var(--pg-edge), var(--pg-e1);
  cursor: grab;
  touch-action: none;
  user-select: none;
}
.pg-org-canvas.is-panning { cursor: grabbing; }
.pg-org-dots {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: radial-gradient(rgba(var(--pg-shade), .14) 1px, transparent 1.2px);
  background-size: 22px 22px;
  mask-image: radial-gradient(80% 80% at 50% 50%, #000 40%, transparent 100%);
}
.pg-org-canvas[data-theme="dark"] .pg-org-dots { background-image: radial-gradient(rgba(255, 255, 255, .09) 1px, transparent 1.2px); }
.pg-org-layer { position: absolute; left: 0; top: 0; transform-origin: 0 0; will-change: transform; }
.pg-org-links { position: absolute; inset: 0; overflow: visible; pointer-events: none; }
.pg-org-link { fill: none; stroke: color-mix(in srgb, var(--pg-cat-ink) 45%, transparent); stroke-width: 1.6; stroke-linecap: round; }
.pg-org-node {
  position: absolute;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 0 16px 0 20px;
  border-radius: var(--pg-r-md);
  border: 1px solid var(--pg-line-strong);
  background: var(--pg-surface);
  box-shadow: var(--pg-edge), var(--pg-e1);
  cursor: pointer;
  transition: box-shadow .3s ease, border-color .3s ease, translate .34s var(--pg-ease-premium);
}
.pg-org-node:hover { box-shadow: var(--pg-edge), var(--pg-e2); translate: 0 -2px; }
.pg-org-node:focus-visible { outline: 2px solid var(--pg-focus); outline-offset: 3px; }
.pg-org-node.is-selected {
  border-color: color-mix(in srgb, var(--pg-blue) 55%, transparent);
  box-shadow: var(--pg-edge-strong), var(--pg-e3), 0 0 0 4px var(--pg-blue-soft);
  translate: 0 -3px;
}
.pg-org-node.is-root { background: var(--pg-plate-bg); border-color: var(--pg-plate-line); color: var(--pg-plate-ink); }
.pg-org-node.is-root .pg-org-meta { color: var(--pg-plate-soft); }
.pg-org-node.is-off { opacity: .55; }
.pg-org-strip { position: absolute; left: 8px; top: 14px; bottom: 14px; width: 4px; border-radius: 4px; background: var(--pg-cat-bg); box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--pg-cat-ink) 30%, transparent); }
.pg-org-node.is-root .pg-org-strip { background: var(--pg-lime); box-shadow: 0 0 10px rgba(130, 221, 45, .5); }
.pg-org-name { font-size: 14px; font-weight: 680; letter-spacing: -.01em; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pg-org-meta { display: flex; align-items: center; justify-content: space-between; gap: 8px; font-size: 11.5px; color: var(--pg-ink-soft); }
.pg-org-leader { overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.pg-org-count { font-family: var(--pg-font-mono); font-size: 10.5px; padding: 1px 7px; border-radius: 999px; color: var(--pg-cat-ink); background: var(--pg-cat-bg); flex: none; }
.pg-org-toggle {
  position: absolute;
  left: 50%;
  bottom: -11px;
  translate: -50% 0;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  font-family: var(--pg-font-mono);
  font-size: 11px;
  font-weight: 700;
  color: var(--pg-ink-muted);
  border: 1px solid var(--pg-line-strong);
  background: var(--pg-surface-solid);
  box-shadow: var(--pg-e1);
}
.pg-org-toggle:hover { color: var(--pg-blue); border-color: var(--pg-blue); }

.pg-org-panel { position: absolute; z-index: 4; max-width: min(320px, calc(50% - 32px)); cursor: default; }
.pg-org-panel:empty { display: none; }
.pg-org-panel.is-tl { left: 16px; top: 16px; }
.pg-org-panel.is-tr { right: 16px; top: 16px; }
.pg-org-panel.is-bl { left: 16px; bottom: 16px; }
.pg-org-panel.is-br { right: 16px; bottom: 16px; }
.pg-org-controls {
  position: absolute;
  left: 50%;
  bottom: 16px;
  z-index: 4;
  display: flex;
  gap: 2px;
  padding: 4px;
  border-radius: 999px;
  translate: -50% 0;
  cursor: default;
}

@media (max-width: 1080px) {
  .pg-org-panel.is-bl { display: none; }
}
@media (max-width: 720px) {
  .pg-org-canvas { height: var(--pg-canvas-height, 520px); }
  .pg-org-panel.is-tr { display: none; }
  .pg-org-panel { max-width: calc(100% - 32px); }
  .pg-org-controls { left: auto; right: 16px; translate: 0 0; bottom: auto; top: 16px; }
  .pg-org-panel.is-tl { right: 76px; }
}
@media (prefers-reduced-motion: reduce) {
  .pg-org-layer { will-change: auto; }
}

.pg-org-link.is-active { stroke: color-mix(in srgb, var(--pg-cat-ink, var(--pg-blue)) 85%, transparent); stroke-width: 2; }
.pg-org-pulse { fill: var(--pg-cat-ink, var(--pg-blue)); opacity: 0; stroke: var(--pg-pulse-halo); stroke-width: 6; paint-order: stroke; pointer-events: none; }
.pg-cat-1 { --pg-cat-bg: var(--pg-cat-1-bg); --pg-cat-ink: var(--pg-cat-1-ink); }
.pg-cat-2 { --pg-cat-bg: var(--pg-cat-2-bg); --pg-cat-ink: var(--pg-cat-2-ink); }
.pg-cat-3 { --pg-cat-bg: var(--pg-cat-3-bg); --pg-cat-ink: var(--pg-cat-3-ink); }
.pg-cat-4 { --pg-cat-bg: var(--pg-cat-4-bg); --pg-cat-ink: var(--pg-cat-4-ink); }
.pg-cat-5 { --pg-cat-bg: var(--pg-cat-5-bg); --pg-cat-ink: var(--pg-cat-5-ink); }

.pg-org-toggle { cursor: pointer; }
.pg-org-empty { position: absolute; inset: 0; display: grid; place-items: center; margin: 0; color: var(--pg-ink-soft); font-size: 14px; pointer-events: none; }
@media (prefers-reduced-motion: reduce) {
  .pg-org-node { transition: none; }
}
</style>
