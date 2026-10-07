<template>
  <a-card class="role-container">
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane key="1" tab="角色-功能权限">
        <RoleTree />
      </a-tab-pane>
      <a-tab-pane key="3" tab="角色-员工列表">
        <RoleEmployeeList />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>
<script setup lang="ts">
  import { ref } from 'vue';
  import RoleEmployeeList from '../role-employee-list/index.vue';
  import RoleTree from '../role-tree/index.vue';

  defineProps({
    value: Number,
  });

  defineEmits(['update:value']);

  let activeKey = ref();
</script>
