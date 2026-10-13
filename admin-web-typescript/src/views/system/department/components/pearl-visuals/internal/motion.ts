import { gsap } from 'gsap'

// 从公共 motion.ts 提取画布需要的动效；不注册全局交互，不修改其他页面的动画。
export const EASE = Object.freeze({
  reveal: 'expo.out',      // 显现
  glide: 'power4.out',     // 位移滑行
  exit: 'power2.in',       // 退场
  swap: 'power2.inOut',    // 双向交接
  settle: 'back.out(1.6)', // 落定确认
})

const reducedQuery = typeof window !== 'undefined' ? window.matchMedia('(prefers-reduced-motion: reduce)') : null
export function motionSkipped() {
  return Boolean(reducedQuery?.matches) || (typeof document !== 'undefined' && document.hidden)
}

export function createFrameLoop(step: (dt: number, t: number) => void) {
  let target: Element | null = null
  let offscreen = false
  let frames = 0
  let running = false
  let last = 0
  let time = 0
  let warned = false
  const run = (force: boolean) => {
    if (document.hidden && !force) { last = 0; return }
    // 每 20 帧量一次位置：离开视口就停帧（比 IntersectionObserver 更不受过渡与隐藏祖先影响）
    if (target && (offscreen || frames++ % 20 === 0)) {
      const r = target.getBoundingClientRect()
      offscreen = r.width === 0 || r.bottom < 0 || r.top > window.innerHeight
    }
    if (offscreen && !force) { last = 0; return }
    const now = performance.now()
    const dt = last ? Math.max(0, Math.min(1 / 30, (now - last) / 1000)) : 1 / 60
    last = now
    time += dt
    try { step(dt, time) } catch (error) {
      if (!warned) { warned = true; console.error(error) }
    }
  }
  const tick = () => run(false)
  return {
    start(el?: Element | null) {
      if (running) return
      running = true
      last = 0
      target = el ?? null
      gsap.ticker.add(tick)
    },
    stop() {
      running = false
      gsap.ticker.remove(tick)
      target = null
    },
    /** 立即执行一帧（减少动态效果时用来直接画到终态） */
    flush() { run(true) },
    get time() { return time },
  }
}


export { gsap }
