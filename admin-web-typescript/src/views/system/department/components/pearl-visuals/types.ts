/** 组织画布的展示数据；无需使用原后台的 Dept、API 或权限模块。 */
export interface OrgNode {
  /** 节点唯一编号，同一棵树内不能重复。 */
  id: number
  /** 节点显示名称。 */
  name: string
  /** 负责人姓名，可选。 */
  leader?: string
  /** 本节点成员数，可选，默认显示 0。 */
  memberCount?: number
  /** 状态：0 停用，1 正常；省略时按正常展示。 */
  status?: number
  /** 联系电话，仅供外部详情面板展示。 */
  phone?: string
  /** 邮箱，仅供外部详情面板展示。 */
  email?: string
  /** 下级节点，省略或空数组表示没有下级。 */
  children?: OrgNode[]
}
