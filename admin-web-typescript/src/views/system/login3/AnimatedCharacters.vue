<template>
  <div class="characters" aria-hidden="true" :data-mode="hiding ? 'hiding' : glancing ? 'glancing' : leaning ? 'leaning' : 'tracking'">
    <div v-for="(character, index) in characters" :key="character.name" :ref="(element) => setBody(index, element)" class="character" :class="character.name">
      <div :ref="(element) => setFace(index, element)" class="eyes">
        <template v-for="eyeIndex in 2" :key="eyeIndex">
          <EyeBall
            v-if="character.eyeSize"
            :ref="(element) => setPupil(index, eyeIndex - 1, element)"
            :size="character.eyeSize"
            :pupil-size="character.pupilSize"
            :max-travel="character.maxTravel"
            :blinking="blinking[index]"
          />
          <Pupil
            v-else
            :ref="(element) => setPupil(index, eyeIndex - 1, element)"
            :size="character.pupilSize"
            :max-travel="character.maxTravel"
          />
        </template>
      </div>
      <div v-if="character.name === 'yellow'" :ref="setMouth" class="mouth"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
  import type { ComponentPublicInstance } from 'vue';
  import EyeBall from './EyeBall.vue';
  import Pupil from './Pupil.vue';

  const props = defineProps<{ isTyping: boolean; hasSecret: boolean; secretVisible: boolean }>();
  type Offset = { x: number; y: number };
  type MovingPupil = { movePupil: (mouseX: number, mouseY: number, forced?: Offset | null) => void };
  type TemplateElement = Element | ComponentPublicInstance | null;

  const characters = [
    { name: 'purple', base: { x: 42, y: 38 }, retreat: { x: 20, y: 34 }, eyeSize: 18, pupilSize: 7, maxTravel: 5 },
    { name: 'black', base: { x: 24, y: 30 }, retreat: { x: 10, y: 28 }, eyeSize: 16, pupilSize: 6, maxTravel: 4 },
    { name: 'orange', base: { x: 78, y: 86 }, retreat: { x: 46, y: 80 }, eyeSize: 0, pupilSize: 12, maxTravel: 5 },
    { name: 'yellow', base: { x: 48, y: 38 }, retreat: { x: 18, y: 34 }, eyeSize: 0, pupilSize: 12, maxTravel: 5 },
  ];
  const LOOK_DURATION_MS = 800;
  const BLINK_DURATION_MS = 150;
  const BLINK_MIN_MS = 3000;
  const BLINK_MAX_MS = 7000;
  const PEEK_MIN_MS = 2000;
  const PEEK_MAX_MS = 5000;
  const FACE_X_DIVISOR = 20;
  const FACE_Y_DIVISOR = 30;
  const SKEW_DIVISOR = 120;
  const FACE_X_LIMIT = 15;
  const FACE_Y_LIMIT = 10;
  const SKEW_LIMIT = 6;
  const PURPLE_HEIGHT = 356;
  const PURPLE_LEAN_HEIGHT = 392;
  const PURPLE_LEAN_SKEW = 12;
  const PURPLE_LEAN_SHIFT = 32;
  const BLACK_SKEW_FACTOR = 1.5;
  const BLACK_GLANCE_SKEW = 10;
  const BLACK_GLANCE_SHIFT = 18;
  const PURPLE_GLANCE_FACE = { x: 52, y: 62 };
  const BLACK_GLANCE_FACE = { x: 30, y: 12 };
  const PURPLE_GLANCE_PUPIL = { x: 3, y: 4 };
  const BLACK_GLANCE_PUPIL = { x: 0, y: -4 };
  const HIDDEN_EYEBALL_PUPIL = { x: -4, y: -4 };
  const HIDDEN_BARE_PUPIL = { x: -5, y: -4 };
  const PEEK_PUPIL = { x: 4, y: 5 };
  const MOUTH_BASE = { x: 38, y: 82 };
  const MOUTH_RETREAT = { x: 10, y: 82 };
  const STILL_PUPIL = { x: 0, y: 0 };

  const hiding = computed(() => props.hasSecret && props.secretVisible);
  const leaning = computed(() => props.isTyping || (props.hasSecret && !props.secretVisible));
  const glancing = ref(false);
  const peeking = ref(false);
  const blinking = ref([false, false]);
  const mouth = ref<HTMLDivElement | null>(null);
  const bodies: (HTMLElement | null)[] = characters.map(() => null);
  const faces: (HTMLElement | null)[] = characters.map(() => null);
  const pupils: (MovingPupil | null)[][] = characters.map(() => []);
  const timers = new Set<number>();
  let mouseX = 0;
  let mouseY = 0;
  let frameId = 0;
  let glanceTimer: number | undefined;
  let peekTimer: number | undefined;
  let peekEndTimer: number | undefined;
  let motionQuery: MediaQueryList | null = null;
  let mounted = false;

  function setBody(index: number, element: TemplateElement): void {
    bodies[index] = element as HTMLElement | null;
  }

  function setFace(index: number, element: TemplateElement): void {
    faces[index] = element as HTMLElement | null;
  }

  function setMouth(element: TemplateElement): void {
    mouth.value = element as HTMLDivElement | null;
  }

  function setPupil(index: number, eyeIndex: number, element: TemplateElement): void {
    pupils[index][eyeIndex] = element as MovingPupil | null;
  }

  function clamp(value: number, limit: number): number {
    return Math.max(-limit, Math.min(limit, value));
  }

  function randomDelay(min: number, max: number): number {
    return min + Math.random() * (max - min);
  }

  function later(callback: () => void, delay: number): number {
    const timer = window.setTimeout(() => {
      timers.delete(timer);
      callback();
    }, delay);
    timers.add(timer);
    return timer;
  }

  function cancelTimer(timer: number | undefined): void {
    if (timer === undefined) return;
    window.clearTimeout(timer);
    timers.delete(timer);
  }

  function scheduleBlink(index: number): void {
    later(() => {
      if (!motionQuery?.matches) {
        blinking.value[index] = true;
        later(() => { blinking.value[index] = false; }, BLINK_DURATION_MS);
      }
      scheduleBlink(index);
    }, randomDelay(BLINK_MIN_MS, BLINK_MAX_MS));
  }

  function schedulePeek(): void {
    peekTimer = later(() => {
      if (!hiding.value) return;
      if (!motionQuery?.matches) {
        peeking.value = true;
        peekEndTimer = later(() => { peeking.value = false; }, LOOK_DURATION_MS);
      }
      schedulePeek();
    }, randomDelay(PEEK_MIN_MS, PEEK_MAX_MS));
  }

  watch(() => props.isTyping, (value, previous) => {
    if (!value || previous || motionQuery?.matches) return;
    cancelTimer(glanceTimer);
    glancing.value = true;
    glanceTimer = later(() => { glancing.value = false; }, LOOK_DURATION_MS);
  });

  watch(hiding, (value) => {
    cancelTimer(peekTimer);
    cancelTimer(peekEndTimer);
    peeking.value = false;
    if (value && mounted) schedulePeek();
  });

  function trackMouse(event: MouseEvent): void {
    mouseX = event.clientX;
    mouseY = event.clientY;
  }

  function animate(): void {
    const reducedMotion = motionQuery?.matches;
    for (const [index, character] of characters.entries()) {
      const body = bodies[index];
      const face = faces[index];
      if (!body || !face) continue;
      const rect = body.getBoundingClientRect();
      const dx = reducedMotion ? 0 : mouseX - (rect.left + rect.width / 2);
      const dy = reducedMotion ? 0 : mouseY - (rect.top + rect.height / 3);
      const faceX = clamp(dx / FACE_X_DIVISOR, FACE_X_LIMIT);
      const faceY = clamp(dy / FACE_Y_DIVISOR, FACE_Y_LIMIT);
      const bodySkew = clamp(-dx / SKEW_DIVISOR, SKEW_LIMIT);
      let facePosition = { x: character.base.x + faceX, y: character.base.y + faceY };
      let transform = `skewX(${bodySkew}deg)`;
      let forced: Offset | null = reducedMotion ? STILL_PUPIL : null;

      if (hiding.value) {
        transform = 'skewX(0deg)';
        facePosition = character.retreat;
        forced = character.eyeSize ? HIDDEN_EYEBALL_PUPIL : HIDDEN_BARE_PUPIL;
        if (character.name === 'purple' && peeking.value) forced = PEEK_PUPIL;
      } else {
        if (character.name === 'purple' && leaning.value) {
          transform = `skewX(${bodySkew - PURPLE_LEAN_SKEW}deg) translateX(${PURPLE_LEAN_SHIFT}px)`;
        }
        if (character.name === 'black' && leaning.value) transform = `skewX(${bodySkew * BLACK_SKEW_FACTOR}deg)`;
        if (glancing.value && character.name === 'purple') {
          facePosition = PURPLE_GLANCE_FACE;
          forced = PURPLE_GLANCE_PUPIL;
        }
        if (glancing.value && character.name === 'black') {
          facePosition = BLACK_GLANCE_FACE;
          transform = `skewX(${bodySkew * BLACK_SKEW_FACTOR + BLACK_GLANCE_SKEW}deg) translateX(${BLACK_GLANCE_SHIFT}px)`;
          forced = BLACK_GLANCE_PUPIL;
        }
      }

      if (character.name === 'purple') body.style.height = `${leaning.value && !hiding.value ? PURPLE_LEAN_HEIGHT : PURPLE_HEIGHT}px`;
      body.style.transform = transform;
      face.style.left = `${facePosition.x}px`;
      face.style.top = `${facePosition.y}px`;
      for (const pupil of pupils[index]) pupil?.movePupil(mouseX, mouseY, forced);
      if (character.name === 'yellow' && mouth.value) {
        mouth.value.style.left = `${hiding.value ? MOUTH_RETREAT.x : MOUTH_BASE.x + faceX}px`;
        mouth.value.style.top = `${hiding.value ? MOUTH_RETREAT.y : MOUTH_BASE.y + faceY}px`;
      }
    }
    frameId = window.requestAnimationFrame(animate);
  }

  onMounted(() => {
    mounted = true;
    mouseX = window.innerWidth / 2;
    mouseY = window.innerHeight / 2;
    motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    window.addEventListener('mousemove', trackMouse, { passive: true });
    scheduleBlink(0);
    scheduleBlink(1);
    if (hiding.value) schedulePeek();
    frameId = window.requestAnimationFrame(animate);
  });

  onUnmounted(() => {
    mounted = false;
    window.removeEventListener('mousemove', trackMouse);
    window.cancelAnimationFrame(frameId);
    for (const timer of timers) window.clearTimeout(timer);
    timers.clear();
  });
</script>

<style scoped>
  .characters {
    position: relative;
    width: min(100%, 500px);
    height: 380px;
    margin-inline: auto;
    filter: drop-shadow(0 24px 36px rgba(0, 0, 0, .24));
  }

  .character {
    position: absolute;
    bottom: 0;
    transform-origin: bottom center;
    transition: all .7s ease-in-out;
  }

  .purple {
    left: 60px;
    width: 164px;
    height: 356px;
    border-radius: 10px 10px 0 0;
    background: linear-gradient(180deg, #7a54ff 0%, #5c33e3 100%);
    z-index: 1;
  }

  .black {
    left: 220px;
    width: 112px;
    height: 286px;
    border-radius: 8px 8px 0 0;
    background: linear-gradient(180deg, #3d3d3d 0%, #222222 100%);
    z-index: 2;
  }

  .orange {
    left: 0;
    width: 220px;
    height: 184px;
    border-radius: 120px 120px 0 0;
    background: linear-gradient(180deg, #ffb07e 0%, #ff9466 100%);
    z-index: 3;
  }

  .yellow {
    left: 288px;
    width: 130px;
    height: 212px;
    border-radius: 70px 70px 0 0;
    background: linear-gradient(180deg, #f2e574 0%, #ddcd4b 100%);
    z-index: 4;
  }

  .eyes {
    position: absolute;
    display: flex;
    align-items: center;
    transition: all .7s ease-in-out;
  }

  .purple .eyes {
    left: 42px;
    top: 38px;
    height: 18px;
    gap: 32px;
  }

  .black .eyes {
    left: 24px;
    top: 30px;
    height: 16px;
    gap: 24px;
  }

  .orange .eyes {
    left: 78px;
    top: 86px;
    gap: 32px;
  }

  .yellow .eyes {
    left: 48px;
    top: 38px;
    gap: 24px;
  }

  .mouth {
    position: absolute;
    left: 38px;
    top: 82px;
    width: 74px;
    height: 4px;
    border-radius: 4px;
    background: #252525;
    transition: all .7s ease-in-out;
  }

  @media (prefers-reduced-motion: reduce) {
    .character,
    .eyes,
    .mouth {
      transition: none;
    }
  }
</style>
