const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');
const vue = require('vue');
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc');
const root = path.resolve(__dirname, '..');
const defaultUrl = '/admin/assets/default-avatar.svg';

function loadScript(source, imports) {
  const module = { exports: {} };
  const code = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
  }).outputText;
  vm.runInNewContext(code, {
    module,
    exports: module.exports,
    require: (name) => {
      assert.ok(Object.hasOwn(imports, name), name);
      return imports[name];
    },
  });
  return module.exports;
}

const helper = loadScript(fs.readFileSync(path.join(root, 'src/lib/avatar.ts'), 'utf8'), {
  '/@/assets/images/default-avatar.svg': { default: defaultUrl },
});
assert.equal(helper.defaultAvatar, defaultUrl);
assert.match(fs.readFileSync(path.join(root, 'src/assets/images/default-avatar.svg'), 'utf8'), /^<svg [^>]*viewBox="0 0 128 128"/);

function avatarNode(node) {
  if (node.tag === 'img' && node.props.some((prop) => prop.name === 'class' && prop.value?.content === 'avatar-image')) return node;
  for (const child of node.children ?? []) {
    const match = avatarNode(child);
    if (match) return match;
  }
}

for (const filename of [
  'src/layout/components/header-user-space/header-avatar.vue',
  'src/views/system/account/components/center/index.vue',
]) {
  const parsed = parse(fs.readFileSync(path.join(root, filename), 'utf8'), { filename });
  assert.equal(parsed.errors.length, 0, filename);
  const script = compileScript(parsed.descriptor, { id: filename });
  const image = avatarNode(parsed.descriptor.template.ast);
  assert.ok(image, filename);
  const template = compileTemplate({
    source: image.loc.source, filename, id: filename,
    compilerOptions: { bindingMetadata: script.bindings },
  });
  assert.equal(template.errors.length, 0, filename);
  const { render } = loadScript(template.code, { vue });
  const validUrl = '/upload/new-avatar.png';
  const vnode = render({}, [], {}, { ...helper, avatar: validUrl, avatarUrl: validUrl });
  assert.equal(vnode.props.src, validUrl);
  assert.equal(typeof vnode.props.onError, 'function');

  let src = vnode.props.src;
  let writes = 0;
  const imageElement = {
    getAttribute: () => src,
    set src(value) { src = value; writes++; },
  };
  assert.equal(src, validUrl);
  assert.equal(writes, 0);
  src = '/upload/missing-avatar.jpeg';
  vnode.props.onError({ currentTarget: imageElement, target: null });
  assert.equal(src, defaultUrl, filename);
  assert.equal(writes, 1);
  vnode.props.onError({ currentTarget: imageElement });
  assert.equal(writes, 1, 'A failed default image must not cause a retry loop');
  src = '/upload/replacement-avatar.png';
  assert.equal(src, '/upload/replacement-avatar.png');
  vnode.props.onError({ currentTarget: imageElement });
  assert.equal(src, defaultUrl);
  assert.equal(writes, 2, 'New avatar failures must still fall back');
}
process.stdout.write('Avatar fallback and image error bindings checks passed.\n');
