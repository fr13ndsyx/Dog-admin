<template>
  <div id="adminPageTag" class="page-tag-div"><ChromeTab /></div>
</template>
<script setup lang="ts">
  import ChromeTab from './components/chrome-tab.vue';
  import { theme } from 'ant-design-vue';
  const { token } = theme.useToken();
</script>
<style lang="less" scoped>
  .page-tag-div { border-bottom: 1px solid v-bind('token.colorBorderSecondary'); }
</style>
