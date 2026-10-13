<template>
  <a-form class="dog-query-form">
    <a-row class="dog-query-form-row">
      <a-form-item label="部门名称" class="dog-query-form-item">
        <a-input style="width: 300px" v-model:value="keywords" placeholder="请输入部门名称" />
      </a-form-item>

      <a-form-item class="dog-query-form-item dog-margin-left10">
        <a-button-group>
          <a-button v-privilege="'support:department:query'" type="primary" @click="onSearch">
            <template #icon>
              <SearchOutlined />
            </template>
            查询
          </a-button>
          <a-button v-privilege="'support:department:query'" @click="resetQuery">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置
          </a-button>
        </a-button-group>
        <a-button v-privilege="'system:department:add'" type="primary" @click="addDepartment()" class="dog-margin-left20">
          <template #icon>
            <PlusOutlined />
          </template>
          新建一级部门
        </a-button>
        <a-radio-group v-model:value="viewMode" button-style="solid" class="dog-margin-left20">
          <a-radio-button :value="VIEW_MODE_TABLE">表格</a-radio-button>
          <a-radio-button :value="VIEW_MODE_CANVAS">画布</a-radio-button>
        </a-radio-group>
      </a-form-item>
    </a-row>
  </a-form>

  <OrgCanvas v-if="viewMode === VIEW_MODE_CANVAS" class="org-canvas" :tree="orgTree" :selected-id="selectedOrgNodeId" @select="selectOrgNode">
    <template #tl>
      <section v-if="selectedOrgNode" class="pg-org-card pg-float">
        <span class="pg-eyebrow">已选部门</span>
        <h2 class="pg-org-card-title">{{ selectedOrgNode.name }}</h2>
        <dl class="pg-org-card-rows">
          <dt>负责人</dt>
          <dd>{{ selectedOrgNode.leader || '未设负责人' }}</dd>
          <dt>本部门</dt>
          <dd>{{ selectedOrgNode.memberCount ?? 0 }} 人</dd>
          <dt>下级部门</dt>
          <dd>{{ selectedOrgNode.children?.length ?? 0 }} 个</dd>
        </dl>
        <span class="pg-eyebrow org-members-title">部门人员</span>
        <ul class="pg-org-members org-members-list" @wheel.stop>
          <li v-if="membersLoading" class="faint">加载中…</li>
          <li v-else-if="!selectedMembers.length" class="faint">暂无人员</li>
          <li v-for="member in selectedMembers" v-else :key="member.employeeId" :class="{ 'is-disabled': member.disabledFlag }">
            <span class="org-member-avatar" aria-hidden="true">{{ member.actualName.slice(0, 1) }}</span>
            <span class="org-member-name">{{ member.actualName }}</span>
            <span class="org-member-meta">{{ member.disabledFlag ? '已禁用' : member.phone || '' }}</span>
          </li>
        </ul>
      </section>
      <section v-else class="pg-org-card pg-float org-canvas-hint">点击部门查看人员；拖动空白处平移，Ctrl + 滚轮缩放。</section>
    </template>
  </OrgCanvas>
  <a-card v-else size="small" :bordered="true">
    <a-table
      size="small"
      bordered
      :loading="tableLoading"
      rowKey="departmentId"
      :columns="columns"
      :data-source="departmentTreeData"
      :defaultExpandAllRows="false"
      v-model:expandedRowKeys="defaultExpandedRowList"
      :pagination="false"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'action'">
          <div class="dog-table-operate">
            <a-button @click="addDepartment(record)" v-privilege="'system:department:add'" type="link">添加下级</a-button>
            <a-button @click="updateDepartment(record)" v-privilege="'system:department:update'" type="link">编辑</a-button>
            <a-button
              danger
              v-privilege="'system:department:delete'"
              @click="deleteDepartment(record.departmentId)"
              type="link"
              >删除</a-button
            >
          </div>
        </template>
      </template>
    </a-table>
  </a-card>
  <DepartmentFormModal ref="departmentFormModal" @refresh="queryDepartmentTree" />
</template>

<script setup lang="ts">
  import { computed, onMounted, ref, createVNode, watch } from 'vue';
  import { departmentApi } from '/@/api/system/department-api';
  import { employeeApi } from '/@/api/system/employee-api';
  import { Modal } from 'ant-design-vue';
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
  import _ from 'lodash';
  import { DogLoading } from '/@/components/framework/dog-loading';
  import DepartmentFormModal from './components/department-form-modal.vue';
  import OrgCanvas from './components/pearl-visuals/OrgCanvas.vue';
  import type { OrgNode } from './components/pearl-visuals/types';
  import { dogSentry } from '/@/lib/dog-sentry';

  interface Department {
    departmentId: number;
    departmentName: string;
    parentId: number;
    managerName?: string;
    children?: Department[];
  }

  interface DepartmentMember {
    employeeId: number;
    actualName: string;
    phone?: string;
    disabledFlag: boolean;
  }

  const DEPARTMENT_PARENT_ID = 0;
  const VIEW_MODE_TABLE = 'table';
  const VIEW_MODE_CANVAS = 'canvas';

  const viewMode = ref<typeof VIEW_MODE_TABLE | typeof VIEW_MODE_CANVAS>(VIEW_MODE_TABLE);
  const selectedOrgNodeId = ref<number | null>(null);
  const memberCountByDepartmentId = ref<Record<string, number>>({});
  const orgTree = computed<OrgNode[]>(() => departmentTreeData.value.map(toOrgNode));
  const selectedOrgNode = computed<OrgNode | undefined>(() => findOrgNode(orgTree.value, selectedOrgNodeId.value));
  const selectedMembers = ref<DepartmentMember[]>([]);
  const membersLoading = ref(false);

  function findOrgNode(nodes: OrgNode[], id: number | null): OrgNode | undefined {
    for (const node of nodes) {
      if (node.id === id) {
        return node;
      }
      const child = findOrgNode(node.children ?? [], id);
      if (child) {
        return child;
      }
    }
    return undefined;
  }

  async function selectOrgNode(node: OrgNode): Promise<void> {
    selectedOrgNodeId.value = node.id;
    selectedMembers.value = [];
    membersLoading.value = true;
    try {
      const res = await employeeApi.queryEmployeeByDeptId(node.id);
      if (selectedOrgNodeId.value === node.id) {
        selectedMembers.value = res.data;
      }
    } catch (e) {
      dogSentry.captureError(e);
    } finally {
      if (selectedOrgNodeId.value === node.id) {
        membersLoading.value = false;
      }
    }
  }

  function toOrgNode(department: Department): OrgNode {
    return {
      id: department.departmentId,
      name: department.departmentName,
      leader: department.managerName,
      memberCount: memberCountByDepartmentId.value[department.departmentId] ?? 0,
      children: department.children?.map(toOrgNode),
    };
  }

  async function queryMemberCount(): Promise<void> {
    try {
      const res = await employeeApi.queryAll();
      memberCountByDepartmentId.value = _.countBy(res.data, 'departmentId');
    } catch (e) {
      dogSentry.captureError(e);
    }
  }

  watch(viewMode, (mode) => {
    if (mode === VIEW_MODE_CANVAS) {
      queryMemberCount();
    }
  });

  // -----------------------  筛选 ---------------------
  const keywords = ref('');

  // ----------------------- 部门树的展示 ---------------------
  const tableLoading = ref(false);

  // 所有部门列表
  const departmentList = ref<Department[]>([]);
  // 部门树形数据
  const departmentTreeData = ref<Department[]>([]);
  // 存放部门id和部门，用于查找
  const idInfoMap = ref(new Map<number, Department>());
  // 默认展开的行
  const defaultExpandedRowList = ref<number[]>([]);

  const columns = ref([
    {
      title: '部门名称',
      dataIndex: 'departmentName',
      key: 'departmentName',
    },
    {
      title: '负责人',
      dataIndex: 'managerName',
      key: 'managerName',
      width: 100,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 150,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      width: 150,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 200,
    },
  ]);

  onMounted(() => {
    queryDepartmentTree();
  });

  // 查询部门列表并构建 部门树
  async function queryDepartmentTree() {
    try {
      tableLoading.value = true;
      let res = await departmentApi.queryAllDepartment();
      let data = res.data;

      idInfoMap.value.clear();
      data.forEach((e: Department) => {
        idInfoMap.value.set(e.departmentId, e);
      });

      departmentList.value = data;
      departmentTreeData.value = buildDepartmentTree(data, DEPARTMENT_PARENT_ID);

      defaultExpandedRowList.value = departmentTreeData.value.map((department) => department.departmentId);
    } catch (e) {
      dogSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // 构建部门树
  function buildDepartmentTree(data: Department[], parentId: number): Department[] {
    let children = data.filter((e) => e.parentId === parentId) || [];
    if (!_.isEmpty(children)) {
      children.forEach((e) => {
        const children = buildDepartmentTree(data, e.departmentId);
        e.children = children.length > 0 ? children : undefined;
      });
      return children;
    }
    return [];
  }

  // 重置
  function resetQuery() {
    keywords.value = '';
    onSearch();
  }

  // 搜索
  function onSearch() {
    if (!keywords.value) {
      departmentTreeData.value = buildDepartmentTree(departmentList.value, DEPARTMENT_PARENT_ID);
      return;
    }
    let originData = departmentList.value.concat();
    if (!originData) {
      return;
    }
    // 筛选出名称符合的部门
    let filterDepartment = originData.filter((e) => e.departmentName.indexOf(keywords.value) > -1);
    let filterDepartmentList = [];
    // 循环筛选出的部门 构建部门树
    filterDepartment.forEach((e) => {
      recursionFilterDepartment(filterDepartmentList, e.departmentId, false);
    });
    departmentTreeData.value = buildDepartmentTree(filterDepartmentList, DEPARTMENT_PARENT_ID);
  }

  // 根据ID递归筛选部门
  function recursionFilterDepartment(resList, id, unshift) {
    let info = idInfoMap.value.get(id);
    if (!info || resList.some((e) => e.departmentId === id)) {
      return;
    }
    if (unshift) {
      resList.unshift(info);
    } else {
      resList.push(info);
    }
    if (info.parentId && info.parentId !== 0) {
      recursionFilterDepartment(resList, info.parentId, unshift);
    }
  }

  // ----------------------- 表单操作：添加部门/修改部门/删除部门/上下移动 ---------------------
  const departmentFormModal = ref();
  // 添加
  function addDepartment(department?: Department): void {
    let data = {
      departmentId: 0,
      departmentName: '',
      parentId: department?.departmentId ?? DEPARTMENT_PARENT_ID,
    };
    departmentFormModal.value.showModal(data);
  }
  // 编辑
  function updateDepartment(e) {
    departmentFormModal.value.showModal(e);
  }

  // 删除
  function deleteDepartment(id) {
    Modal.confirm({
      title: '提醒',
      icon: createVNode(ExclamationCircleOutlined),
      content: '确定要删除该部门吗?',
      okText: '删除',
      okType: 'danger',
      async onOk() {
        DogLoading.show();
        try {
          await departmentApi.deleteDepartment(id);
          await queryDepartmentTree();
        } catch (error) {
          dogSentry.captureError(error);
        } finally {
          DogLoading.hide();
        }
      },
      cancelText: '取消',
      onCancel() {},
    });
  }
</script>

<style scoped lang="less">
  .org-canvas {
    :deep([class*='pg-cat-']) {
      --pg-cat-bg: var(--pg-cat-2-bg);
      --pg-cat-ink: var(--pg-cat-2-ink);
    }
    :deep(.pg-org-node.is-root) {
      background: var(--pg-surface);
      color: var(--pg-ink);
    }
    :deep(.pg-org-node.is-root:not(.is-selected)) {
      border-color: var(--pg-line-strong);
    }
    :deep(.pg-org-node.is-root .pg-org-meta) {
      color: var(--pg-ink-soft);
    }
    :deep(.pg-org-node.is-root .pg-org-strip) {
      background: var(--pg-cat-bg);
      box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--pg-cat-ink) 30%, transparent);
    }
  }
  .org-members-title {
    margin-top: 10px;
  }
  .org-members-list {
    max-height: 240px;
    overflow-y: auto;
    overscroll-behavior: contain;
    .faint {
      color: var(--pg-ink-faint);
    }
    .is-disabled {
      opacity: 0.55;
    }
  }
  .org-member-avatar {
    display: grid;
    place-items: center;
    width: 26px;
    height: 26px;
    border-radius: 50%;
    font-size: 12px;
    font-weight: 600;
    color: var(--pg-blue);
    background: var(--pg-blue-soft);
  }
  .org-member-name {
    font-weight: 600;
    white-space: nowrap;
  }
  .org-member-meta {
    overflow: hidden;
    color: var(--pg-ink-soft);
    font-family: var(--pg-font-mono);
    font-size: 11.5px;
    text-align: right;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .org-canvas-hint {
    font-size: 12px;
    line-height: 1.7;
    color: var(--pg-ink-soft);
  }
</style>
