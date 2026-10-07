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
      </a-form-item>
    </a-row>
  </a-form>

  <a-card size="small" :bordered="true">
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
    <!-- 添加编辑部门弹窗 -->
    <DepartmentFormModal ref="departmentFormModal" @refresh="queryDepartmentTree" />
  </a-card>
</template>

<script setup lang="ts">
  import { onMounted, ref, createVNode } from 'vue';
  import { departmentApi } from '/@/api/system/department-api';
  import { Modal } from 'ant-design-vue';
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
  import _ from 'lodash';
  import { DogLoading } from '/@/components/framework/dog-loading';
  import DepartmentFormModal from './components/department-form-modal.vue';
  import { dogSentry } from '/@/lib/dog-sentry';

  interface Department {
    departmentId: number;
    departmentName: string;
    parentId: number;
    children?: Department[];
  }

  const DEPARTMENT_PARENT_ID = 0;

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

<style scoped lang="less"></style>
