<script setup lang="ts">
withDefaults(defineProps<{
  name: 'plus' | 'minus' | 'fit'
  label: string
  size?: number
  small?: boolean
}>(), { size: 17 })

// 从原图标表仅保留画布使用的三枚图标。
const paths = {
  plus: 'M12 5v14M5 12h14',
  minus: 'M5 12h14',
  fit: 'M4 9V5a1 1 0 0 1 1-1h4M15 4h4a1 1 0 0 1 1 1v4M20 15v4a1 1 0 0 1-1 1h-4M9 20H5a1 1 0 0 1-1-1v-4',
}
</script>

<template>
  <button type="button" :class="['pg-icon-btn', small && 'pg-icon-btn-sm']" :title="label" :aria-label="label">
    <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path :d="paths[name]" />
    </svg>
  </button>
</template>

<style scoped>
.pg-icon-btn { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; padding: 0; border: 0; background: none; border-radius: var(--pg-r-xs); color: var(--pg-ink-soft); flex: none; cursor: pointer; transition: color .2s ease, background .2s ease; }
.pg-icon-btn:hover { color: var(--pg-ink); background: var(--pg-hover-bg); }
.pg-icon-btn:focus-visible { outline: 2px solid var(--pg-focus); outline-offset: 2px; }
.pg-icon-btn-sm { width: 28px; height: 28px; }
.pg-icon-btn svg { display: block; flex: none; }
@media (prefers-reduced-motion: reduce) {
  .pg-icon-btn { transition: none; }
}
</style>
