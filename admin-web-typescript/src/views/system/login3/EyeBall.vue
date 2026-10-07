<template>
  <span class="eyeball" :class="{ blinking }" :style="{ width: `${size}px`, '--eye-size': `${size}px` }">
    <Pupil ref="pupil" :size="pupilSize" :max-travel="maxTravel" :color="color" />
  </span>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import Pupil from './Pupil.vue';

  withDefaults(defineProps<{ size: number; pupilSize: number; maxTravel: number; color?: string; blinking?: boolean }>(), {
    color: '#252525',
    blinking: false,
  });
  const pupil = ref<InstanceType<typeof Pupil> | null>(null);

  function movePupil(mouseX: number, mouseY: number, forced: { x: number; y: number } | null = null): void {
    pupil.value?.movePupil(mouseX, mouseY, forced);
  }

  defineExpose({ movePupil });
</script>

<style scoped>
  .eyeball {
    display: inline-grid;
    place-items: center;
    flex: none;
    height: var(--eye-size);
    border-radius: 50%;
    background: white;
    overflow: hidden;
    transition: all .15s ease;
  }

  .blinking {
    height: 2px;
  }

  .blinking :deep(.pupil-anchor) {
    visibility: hidden;
  }

  @media (prefers-reduced-motion: reduce) {
    .eyeball {
      transition: none;
    }
  }
</style>
