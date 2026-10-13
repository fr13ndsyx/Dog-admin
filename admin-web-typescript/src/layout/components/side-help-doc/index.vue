<template>
  <a-card v-if="loadFailed || helpDocs.length" class="home-help-doc" title="帮助文档" :bordered="false">
    <template #extra>
      <a-space>
        <router-link target="_blank" rel="noopener noreferrer" :to="{ path: '/help-doc/detail' }">全部文档</router-link>
        <a-button type="link" :loading="loading" @click="queryHelpDocs">刷新</a-button>
      </a-space>
    </template>
    <a-alert v-if="loadFailed" type="error" show-icon message="帮助文档加载失败，请点击刷新重试。" />
    <a-list
      v-else
      :data-source="helpDocs"
      :loading="loading"
      :locale="{ emptyText: '暂无帮助文档' }"
      :pagination="helpDocs.length > HOME_HELP_DOC_PAGE_SIZE ? { pageSize: HOME_HELP_DOC_PAGE_SIZE, size: 'small' } : false"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <router-link target="_blank" rel="noopener noreferrer" :to="{ path: '/help-doc/detail', query: { helpDocId: item.helpDocId } }">{{ item.title }}</router-link>
        </a-list-item>
      </template>
    </a-list>
  </a-card>
</template>
<script setup lang="ts">
  import { onActivated, onMounted, ref } from 'vue';
  import { helpDocApi } from '/@/api/support/help-doc-api';
  import { sentry } from '/@/lib/sentry';

  interface HelpDoc {
    helpDocId: number;
    title: string;
  }

  const HOME_HELP_DOC_PAGE_SIZE = 5;
  const helpDocs = ref<HelpDoc[]>([]);
  const loading = ref(false);
  const loadFailed = ref(false);

  async function queryHelpDocs(): Promise<void> {
    if (loading.value) return;
    loading.value = true;
    loadFailed.value = false;
    try {
      const response = await helpDocApi.getAllHelpDocList();
      helpDocs.value = response.data;
    } catch (error) {
      loadFailed.value = true;
      sentry.captureError(error);
    } finally {
      loading.value = false;
    }
  }

  onMounted(queryHelpDocs);
  onActivated(queryHelpDocs);
</script>
<style scoped>
  .home-help-doc { margin-top: 10px; }
</style>
