const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { parse, compileScript } = require('@vue/compiler-sfc');
const ts = require('typescript');
const vue = require('vue');

const componentDir = path.resolve(__dirname, '../src/views/system/login3');
const modules = new Map();
const frames = new Map();
const timers = new Map();
const listeners = new Map();
let nextId = 0;
let now = 0;
const clock = {
  innerWidth: 1000,
  innerHeight: 800,
  matchMedia: () => ({ matches: false }),
  addEventListener: (name, callback) => listeners.set(name, callback),
  removeEventListener: (name) => listeners.delete(name),
  requestAnimationFrame: (callback) => { frames.set(++nextId, callback); return nextId; },
  cancelAnimationFrame: (id) => frames.delete(id),
  setTimeout: (callback, delay) => { timers.set(++nextId, { callback, due: now + delay }); return nextId; },
  clearTimeout: (id) => timers.delete(id),
};

function loadComponent(filename) {
  if (modules.has(filename)) return modules.get(filename);
  const source = fs.readFileSync(filename, 'utf8');
  const parsed = parse(source, { filename });
  assert.equal(parsed.errors.length, 0);
  const compiled = compileScript(parsed.descriptor, { id: path.basename(filename), inlineTemplate: true });
  const code = ts.transpileModule(compiled.content, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
  }).outputText;
  const module = { exports: {} };
  const fixedMath = Object.create(Math);
  fixedMath.random = () => .5;
  vm.runInNewContext(code, {
    module,
    exports: module.exports,
    require: (name) => name.endsWith('.vue') ? loadComponent(path.resolve(path.dirname(filename), name)) : require(name),
    window: clock,
    Math: fixedMath,
    Set,
  }, { filename });
  modules.set(filename, module.exports);
  return module.exports;
}

function element(type) {
  return {
    type, props: {}, style: {}, children: [], parent: null,
    getBoundingClientRect: () => ({ left: 0, top: 0, width: 200, height: 300 }),
  };
}

const renderer = vue.createRenderer({
  createElement: element,
  createText: (text) => ({ ...element('text'), text }),
  createComment: (text) => ({ ...element('comment'), text }),
  setText: (node, text) => { node.text = text; },
  setElementText: (node, text) => { node.text = text; },
  parentNode: (node) => node.parent,
  nextSibling: (node) => node.parent?.children[node.parent.children.indexOf(node) + 1] ?? null,
  patchProp: (node, key, previous, value) => {
    node.props[key] = value;
    if (key === 'style') Object.assign(node.style, value);
  },
  insert: (node, parent, anchor) => {
    if (node.parent) node.parent.children.splice(node.parent.children.indexOf(node), 1);
    node.parent = parent;
    const index = parent.children.indexOf(anchor);
    parent.children.splice(index < 0 ? parent.children.length : index, 0, node);
  },
  remove: (node) => {
    node.parent.children.splice(node.parent.children.indexOf(node), 1);
    node.parent = null;
  },
});

function nodes(root, className) {
  const matches = (root.props.class ?? '').split(' ').includes(className) ? [root] : [];
  return matches.concat(root.children.flatMap((child) => nodes(child, className)));
}

function frame() {
  assert.equal(frames.size, 1);
  const [id, callback] = [...frames][0];
  frames.delete(id);
  callback();
  assert.equal(frames.size, 1);
}

async function advance(milliseconds) {
  const target = now + milliseconds;
  while (true) {
    const due = [...timers].filter(([, timer]) => timer.due <= target).sort((a, b) => a[1].due - b[1].due)[0];
    if (!due) break;
    now = due[1].due;
    timers.delete(due[0]);
    due[1].callback();
    await vue.nextTick();
  }
  now = target;
  await vue.nextTick();
}

async function check() {
  const pupilRoot = element('root');
  const pupilApp = renderer.createApp(loadComponent(path.join(componentDir, 'Pupil.vue')).default, { size: 12, maxTravel: 5 });
  const pupil = pupilApp.mount(pupilRoot);
  nodes(pupilRoot, 'pupil-anchor')[0].getBoundingClientRect = () => ({ left: 0, top: 0, width: 12, height: 12 });
  pupil.movePupil(9, 10);
  assert.equal(nodes(pupilRoot, 'pupil')[0].style.transform, 'translate(3px, 4px)');
  pupil.movePupil(106, 6);
  assert.equal(nodes(pupilRoot, 'pupil')[0].style.transform, 'translate(5px, 0px)');
  pupil.movePupil(6, 6);
  assert.equal(nodes(pupilRoot, 'pupil')[0].style.transform, 'translate(0px, 0px)');
  pupil.movePupil(6, 6, { x: 4, y: 5 });
  assert.equal(nodes(pupilRoot, 'pupil')[0].style.transform, 'translate(4px, 5px)');
  pupilApp.unmount();

  const props = vue.reactive({ isTyping: false, hasSecret: false, secretVisible: false });
  const root = element('root');
  const component = loadComponent(path.join(componentDir, 'AnimatedCharacters.vue')).default;
  const app = renderer.createApp({ setup: () => () => vue.h(component, props) });
  app.mount(root);
  const purple = nodes(root, 'purple')[0];
  const black = nodes(root, 'black')[0];
  const yellow = nodes(root, 'yellow')[0];
  assert.equal(nodes(root, 'eyeball').length, 4);
  assert.equal(nodes(root, 'pupil').length, 8);
  listeners.get('mousemove')({ clientX: 100, clientY: 100 });
  frame();
  assert.equal(nodes(purple, 'eyes')[0].style.top, '38px');
  assert.equal(nodes(purple, 'eyes')[0].style.left, '42px');
  assert.equal(purple.style.transform, 'skewX(0deg)');
  assert.equal(purple.style.height, '356px');
  listeners.get('mousemove')({ clientX: 10000, clientY: -10000 });
  frame();
  assert.equal(nodes(purple, 'eyes')[0].style.left, '57px');
  assert.equal(nodes(purple, 'eyes')[0].style.top, '28px');
  assert.equal(purple.style.transform, 'skewX(-6deg)');
  listeners.get('mousemove')({ clientX: 100, clientY: 100 });

  props.isTyping = true;
  await vue.nextTick();
  frame();
  assert.equal(purple.style.height, '392px');
  assert.equal(nodes(purple, 'eyes')[0].style.left, '52px');
  assert.equal(nodes(purple, 'eyes')[0].style.top, '62px');
  assert.equal(nodes(purple, 'pupil')[0].style.transform, 'translate(3px, 4px)');
  assert.equal(black.style.transform, 'skewX(10deg) translateX(18px)');
  assert.equal(nodes(black, 'pupil')[0].style.transform, 'translate(0px, -4px)');
  await advance(800);
  frame();
  assert.equal(purple.style.transform, 'skewX(-12deg) translateX(32px)');
  assert.equal(nodes(purple, 'eyes')[0].style.left, '42px');

  props.isTyping = false;
  props.hasSecret = true;
  await vue.nextTick();
  frame();
  assert.equal(purple.style.height, '392px');
  props.secretVisible = true;
  await vue.nextTick();
  frame();
  for (const body of nodes(root, 'character')) assert.equal(body.style.transform, 'skewX(0deg)');
  assert.equal(nodes(purple, 'eyes')[0].style.left, '20px');
  assert.equal(nodes(black, 'eyes')[0].style.top, '28px');
  assert.equal(nodes(purple, 'pupil')[0].style.transform, 'translate(-4px, -4px)');
  assert.equal(nodes(yellow, 'pupil')[0].style.transform, 'translate(-5px, -4px)');
  assert.equal(nodes(yellow, 'mouth')[0].style.left, '10px');

  await advance(3500);
  frame();
  assert.equal(nodes(purple, 'pupil')[0].style.transform, 'translate(4px, 5px)');
  assert.equal(nodes(purple, 'eyes')[0].style.left, '20px');
  await advance(700);
  assert.equal(nodes(root, 'blinking').length, 4);
  await advance(100);
  frame();
  assert.equal(nodes(purple, 'pupil')[0].style.transform, 'translate(-4px, -4px)');
  await advance(50);
  assert.equal(nodes(root, 'blinking').length, 0);
  props.hasSecret = false;
  await vue.nextTick();
  frame();
  assert.equal(nodes(root, 'characters')[0].props['data-mode'], 'tracking');
  assert.equal(timers.size, 2);
  app.unmount();
  assert.equal(frames.size, 0);
  assert.equal(timers.size, 0);
  assert.equal(listeners.size, 0);
  process.stdout.write('Login character animation checks passed.\n');
}

check().catch((error) => { process.stderr.write(`${error.stack}\n`); process.exitCode = 1; });
