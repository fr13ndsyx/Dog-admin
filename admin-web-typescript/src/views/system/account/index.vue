<template>
  <div class="account-container">
    <div class="account-menu-list">
      <a-menu :selectedKeys="[selectedMenu.menuId]" mode="inline" @click="selectMenu($event.key)">
        <a-menu-item v-for="item in menuList" :key="item.menuId">{{ item.menuName }}</a-menu-item>
      </a-menu>
    </div>
    <div class="account-content"><component :is="selectedMenu.components" /></div>
  </div>
</template>
<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { ACCOUNT_MENU } from './account-menu';
  import { useRoute } from 'vue-router';
  import { theme } from 'ant-design-vue';

  type AccountMenu = (typeof ACCOUNT_MENU)[keyof typeof ACCOUNT_MENU];
  const menuList = Object.values(ACCOUNT_MENU);
  const selectedMenu = ref<AccountMenu>(ACCOUNT_MENU.CENTER);
  function selectMenu(menuId: unknown): void {
    selectedMenu.value = menuList.find((item) => item.menuId === menuId) ?? ACCOUNT_MENU.CENTER;
  }
  const route = useRoute();
  watch(() => route.query.menuId, selectMenu, { immediate: true });
  const { token } = theme.useToken();
</script>
<style lang="less" scoped>
  .account-container {
    display: flex;
    height: 100%;
    .account-menu-list { width: 180px; }
    .account-content {
      flex: 1;
      min-width: 0;
      margin-left: 10px;
      padding: 20px;
      border-radius: 8px;
      background-color: v-bind('token.colorBgContainer');
    }
  }
</style>
