
<template>
  <a-dropdown class="header-trigger" :get-popup-container="getPopupContainer">
    <div class="wrapper">
      <img class="avatar-image" :src="avatar" alt="头像" @error="showDefaultAvatar" />
      <span class="name">{{ actualName }}</span>
    </div>
    <template #overlay>
      <a-menu :class="['avatar-menu']">
        <a-menu-item @click="toAccount()">
          <span>个人中心</span>
        </a-menu-item>
        <a-menu-item @click="toAccount(ACCOUNT_MENU.PASSWORD.menuId)">
          <span>修改密码</span>
        </a-menu-item>
        <a-menu-item @click="onLogout">
          <span>退出登录</span>
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</template>
<script setup lang="ts">
  import { computed } from 'vue';
  import { loginApi } from '/@/api/system/login-api';
  import { useUserStore } from '/@/store/modules/system/user';
  import { dogSentry } from '/@/lib/dog-sentry';
  import { useRouter } from 'vue-router';
  import { ACCOUNT_MENU } from '/@/views/system/account/account-menu';
  import { defaultAvatar, showDefaultAvatar } from '/@/lib/avatar';

  //监听退出登录方法
  async function onLogout() {
    try {
      await loginApi.logout();
    } catch (e) {
      dogSentry.captureError(e);
    } finally {
      useUserStore().logout();
      location.reload();
    }
  }

  // ------------------------ 个人中心 ------------------------
  const router = useRouter();
  function toAccount(menuId) {
    router.push({
      path: '/account',
      query: { menuId },
    });
  }

  function getPopupContainer() {
    return document.body;
  }

  const avatar = computed(() => useUserStore().avatar || defaultAvatar);
  const actualName = computed(() => useUserStore().actualName);
</script>
<style lang="less" scoped>
  .wrapper {
    cursor: pointer;
    display: flex;
    align-items: center;

    .avatar-image {
      width: 20px;
      height: 20px;
      object-fit: cover;
      border-radius: 50%;
    }
  }

  .header-trigger {
    height: @header-user-height;
    line-height: @header-user-height;

    .avatar {
      vertical-align: middle;
    }

    .name {
      margin-left: 5px;
      font-weight: 500;
    }
  }
</style>
