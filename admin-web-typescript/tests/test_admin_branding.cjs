const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc');
const root = path.resolve(__dirname, '..');

function runScript(relativePath, imports, globals = {}, source = null) {
  const module = { exports: {} };
  const code = ts.transpileModule(source ?? fs.readFileSync(path.join(root, relativePath), 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
  }).outputText;
  vm.runInNewContext(code, {
    module,
    exports: module.exports,
    require: (name) => {
      assert.ok(Object.hasOwn(imports, name), name);
      return imports[name];
    },
    ...globals,
  }, { filename: relativePath });
  return module.exports;
}

const defaults = runScript('src/config/app-config.ts', {}).appDefaultConfig;
assert.equal(defaults.websiteName, 'admin');
assert.equal(Object.hasOwn(defaults, 'footerFlag'), false);

function cachedConfig(value) {
  return runScript('src/store/modules/system/app-config.ts', {
    pinia: { defineStore: (config) => config },
    '/@/config/app-config': { appDefaultConfig: defaults },
    '/@/constants/local-storage-key-const': { default: { APP_CONFIG: 'app_config' } },
    '/@/lib/sentry': { sentry: { captureError: (error) => { throw error; } } },
    '/@/utils/local-util': { localRead: () => value ? JSON.stringify(value) : null },
  }).useAppConfigStore.state();
}

const legacy = cachedConfig({ ...defaults, websiteName: 'admin 3.X', footerFlag: true, layout: 'top', sideMenuWidth: 240 });
assert.equal(legacy.websiteName, 'admin');
assert.equal(legacy.layout, 'side');
assert.equal(legacy.pageTagLocation, 'center');
assert.equal(legacy.pageTagStyle, 'chrome');
assert.equal(legacy.sideMenuWidth, 240);
assert.equal(Object.hasOwn(legacy, 'footerFlag'), false);
assert.equal(cachedConfig({ ...defaults, websiteName: '客户工作台' }).websiteName, '客户工作台');
assert.equal(cachedConfig(null).websiteName, 'admin');

const state = { pageTagFlag: true, footerFlag: true };
const { calcTableHeight } = runScript('src/lib/table-auto-height.ts', {
  '../store/modules/system/app-config': { useAppConfigStore: () => ({ $state: state }) },
}, { document: { querySelector: () => ({ offsetHeight: 1000 }) } });
const height = { value: 0 };
calcTableHeight(height, [{ value: { $el: { offsetHeight: 50 } } }], 10);
assert.equal(height.value, 860);
state.pageTagFlag = false;
calcTableHeight(height, [], 10);
assert.equal(height.value, 950);

const vueFiles = [
  'src/views/system/login3/login.vue',
  'src/layout/side-layout.vue',
  'src/layout/help-doc-layout.vue',
  'src/layout/components/side-menu/index.vue',
  'src/layout/components/side-help-doc/index.vue',
  'src/views/system/home/index.vue',
  'src/views/support/level3protect/level3-protect-config-index.vue',
];
for (const filename of vueFiles) {
  const parsed = parse(fs.readFileSync(path.join(root, filename), 'utf8'), { filename });
  assert.equal(parsed.errors.length, 0, filename);
  const compiled = compileScript(parsed.descriptor, { id: filename });
  const template = compileTemplate({ source: parsed.descriptor.template.content, filename, id: filename, compilerOptions: { bindingMetadata: compiled.bindings } });
  assert.equal(template.errors.length, 0, filename);
  assert.doesNotMatch(parsed.descriptor.template.content, /logoImg|Footer|footer|a-layout-footer|footerFlag|ContactModal|OfficialAccountCard/);
}
for (const filename of ['.env.development', '.env.localhost', '.env.pre', '.env.production', '.env.test']) {
  assert.match(fs.readFileSync(path.join(root, filename), 'utf8'), /^VITE_APP_TITLE='admin'\r?$/m);
}
assert.match(fs.readFileSync(path.join(root, 'index.html'), 'utf8'), /rel="icon" href="data:,"/);

const userStore = runScript('src/store/modules/system/user.ts', {
  lodash: { default: require('lodash') },
  pinia: { defineStore: (config) => config },
  '/@/constants/local-storage-key-const': { default: { USER_TAG_NAV: 'tags' } },
  '/@/constants/system/home-const': { HOME_PAGE_NAME: 'Home' },
  '/@/constants/system/menu-const': { MENU_TYPE_ENUM: { POINTS: { value: 3 } } },
  '/@/utils/local-util': { localRead: () => null, localSave: () => {}, localRemove: () => {} },
}).useUserStore;
const user = { ...userStore.state(), ...userStore.actions };
user.tagNav = [
  { menuName: '47', menuTitle: '已删除商品' },
  { menuName: '26', menuTitle: '菜单管理' },
  { menuName: 'Account', menuTitle: '个人中心' },
];
Object.defineProperty(user, 'getTagNav', { get: () => userStore.getters.getTagNav(user) });
user.setUserLoginInfo({ menuList: [{ menuId: 26, menuType: 2, parentId: 0, path: '/menu/list', visibleFlag: true, disabledFlag: false }] });
assert.deepEqual(Array.from(user.tagNav, (tag) => tag.menuName), ['26', 'Account']);
assert.equal(Object.hasOwn(user, 'queryUnreadMessageCount'), false);
user.logout();
assert.equal(user.menuRouterList.length, 0);
assert.equal(user.menuTree.length, 0);
assert.equal(user.pointsList.length, 0);
assert.equal(user.menuRouterInitFlag, false);
const accountSource = fs.readFileSync(path.join(root, 'src/views/system/account/index.vue'), 'utf8');
const accountParsed = parse(accountSource);
const accountScript = compileScript(accountParsed.descriptor, { id: 'account' });
const accountMenu = {
  CENTER: { menuId: 'center', components: 'center' },
  PASSWORD: { menuId: 'password', components: 'password' },
};
const account = runScript('account.vue', {
  vue: require('vue'),
  './account-menu': { ACCOUNT_MENU: accountMenu },
  'vue-router': { useRoute: () => ({ query: { menuId: 'message' } }) },
  'ant-design-vue': { theme: { useToken: () => ({ token: {} }) } },
}, {}, accountScript.content).default.setup({}, { expose: () => {} });
assert.equal(account.selectedMenu.value.menuId, 'center');
account.selectMenu('password');
assert.equal(account.selectedMenu.value.menuId, 'password');
account.selectMenu('notice');
assert.equal(account.selectedMenu.value.menuId, 'center');
assert.doesNotMatch(fs.readFileSync(path.join(root, 'src/layout/index.vue'), 'utf8'), /TopLayout|SideExpandLayout|TopExpandLayout/);

process.stdout.write('Admin branding and cached configuration checks passed.\n');
