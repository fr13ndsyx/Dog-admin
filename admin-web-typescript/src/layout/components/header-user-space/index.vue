<template>
  <a-space :size="10">
    <div class="user-space-item"><HeaderAvatar /></div>
  </a-space>
</template>
<script setup lang="ts">
  import HeaderAvatar from './header-avatar.vue';
  import { theme } from 'ant-design-vue';

  const { token } = theme.useToken();
</script>
<style lang="less" scoped>
  .user-space-item {
    height: 100%;
    color: inherit;
    padding: 0 12px;
    cursor: pointer;
    align-self: center;
  }
  .user-space-item:hover {
    color: v-bind('token.colorPrimary');
    background-color: @hover-bg-color !important;
  }
</style>
