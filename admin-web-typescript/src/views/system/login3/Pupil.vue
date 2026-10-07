<template>
  <span ref="anchor" class="pupil-anchor" :style="{ width: `${size}px`, height: `${size}px` }">
    <span ref="pupil" class="pupil" :style="{ background: color }"></span>
  </span>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  const props = withDefaults(defineProps<{ size: number; maxTravel: number; color?: string }>(), { color: '#252525' });
  const anchor = ref<HTMLSpanElement | null>(null);
  const pupil = ref<HTMLSpanElement | null>(null);

  function movePupil(mouseX: number, mouseY: number, forced: { x: number; y: number } | null = null): void {
    if (!anchor.value || !pupil.value) return;
    let x = forced?.x ?? 0;
    let y = forced?.y ?? 0;
    if (!forced) {
      const rect = anchor.value.getBoundingClientRect();
      const dx = mouseX - (rect.left + rect.width / 2);
      const dy = mouseY - (rect.top + rect.height / 2);
      const distance = Math.hypot(dx, dy);
      const travel = Math.min(distance, props.maxTravel);
      if (distance > 0) {
        x = (dx / distance) * travel;
        y = (dy / distance) * travel;
      }
    }
    pupil.value.style.transform = `translate(${x}px, ${y}px)`;
  }

  defineExpose({ movePupil });
</script>

<style scoped>
  .pupil-anchor {
    display: inline-block;
    position: relative;
    flex: none;
  }

  .pupil {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    transition: transform .1s ease-out;
  }

  @media (prefers-reduced-motion: reduce) {
    .pupil {
      transition: none;
    }
  }
</style>
