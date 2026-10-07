const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');
const web = path.join(root, 'admin-web-typescript');
const api = path.join(root, 'admin-api-java17-springboot3');
const skippedDirectories = new Set(['.git', 'node_modules', 'target', 'dist', 'runtime', 'memory']);
const textExtensions = new Set(['.java', '.xml', '.yaml', '.factories', '.vm', '.ts', '.vue', '.less', '.cjs', '.json', '.md']);
const files = [];
function collectFiles(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const filename = path.join(directory, entry.name);
    if (entry.isDirectory() && !skippedDirectories.has(entry.name)) collectFiles(filename);
    if (entry.isFile()) files.push(filename);
  }
}
collectFiles(root);
const legacyIdentity = /lab\d+|\d+lab__|[s]mart[\w-]*|\bsa-(?:admin|base|parent)\b|classpath\*:sa-\*\.yaml/i;
for (const filename of files) {
  assert.doesNotMatch(path.relative(root, filename), legacyIdentity, filename);
  if (textExtensions.has(path.extname(filename)) || path.basename(filename).startsWith('.env.')) {
    assert.doesNotMatch(fs.readFileSync(filename, 'utf8'), legacyIdentity, filename);
  }
}
const packageJson = JSON.parse(fs.readFileSync(path.join(web, 'package.json'), 'utf8'));
const lockJson = JSON.parse(fs.readFileSync(path.join(web, 'package-lock.json'), 'utf8'));
assert.equal(packageJson.name, 'dogadmin');
assert.equal(lockJson.name, packageJson.name);
assert.equal(lockJson.packages[''].name, packageJson.name);
assert.deepEqual(lockJson.packages[''].dependencies, packageJson.dependencies);
assert.deepEqual(lockJson.packages[''].devDependencies, packageJson.devDependencies);
const javaFiles = files.filter(filename => filename.endsWith('.java'));
const classes = new Map();
for (const filename of javaFiles) {
  const text = fs.readFileSync(filename, 'utf8');
  const packageName = text.match(/^package\s+([\w.]+);/m)?.[1];
  assert.ok(packageName?.startsWith('net.dogadmin.'), filename);
  const sourceRoot = filename.match(/^(.*[\\/]src[\\/](?:main|test)[\\/]java)[\\/]/)?.[1];
  assert.ok(sourceRoot, filename);
  const expectedPath = packageName.split('.').join(path.sep) + path.sep + path.basename(filename);
  assert.equal(path.relative(sourceRoot, filename), expectedPath);
  classes.set(packageName + '.' + path.basename(filename, '.java'), filename);
}
for (const filename of javaFiles) {
  const text = fs.readFileSync(filename, 'utf8');
  for (const match of text.matchAll(/^import\s+(?:static\s+)?(net\.dogadmin\.[\w.*]+);/gm)) {
    const importedName = match[1];
    if (importedName.endsWith('.*')) {
      const prefix = importedName.slice(0, -1);
      assert.ok([...classes.keys()].some(name => name.startsWith(prefix) || prefix.startsWith(name + '.')), importedName);
    } else {
      assert.ok([...classes.keys()].some(name => importedName === name || importedName.startsWith(name + '.')), importedName);
    }
  }
}
const parentPom = fs.readFileSync(path.join(api, 'pom.xml'), 'utf8');
assert.match(parentPom, /<groupId>net\.dogadmin<\/groupId>/);
assert.match(parentPom, /<artifactId>dog-parent<\/artifactId>/);
for (const moduleName of ['dog-admin', 'dog-base']) {
  assert.ok(parentPom.includes('<module>' + moduleName + '</module>'));
  const modulePom = fs.readFileSync(path.join(api, moduleName, 'pom.xml'), 'utf8');
  assert.ok(modulePom.includes('<artifactId>' + moduleName + '</artifactId>'));
  assert.match(modulePom, /<artifactId>dog-parent<\/artifactId>/);
}
assert.match(fs.readFileSync(path.join(api, 'dog-admin/pom.xml'), 'utf8'), /<mainClass>net\.dogadmin\.admin\.AdminApplication<\/mainClass>/);
const yamlProcessor = fs.readFileSync(path.join(api, 'dog-base/src/main/java/net/dogadmin/base/config/YamlProcessor.java'), 'utf8');
assert.ok(yamlProcessor.includes('classpath*:dog-*.yaml'));
const factories = fs.readFileSync(path.join(api, 'dog-base/src/main/resources/META-INF/spring.factories'), 'utf8');
assert.ok(factories.includes('net.dogadmin.base.config.YamlProcessor'));
for (const environment of ['dev', 'test', 'pre', 'prod']) {
  const baseConfig = fs.readFileSync(path.join(api, 'dog-base/src/main/resources', environment, 'dog-base.yaml'), 'utf8');
  const applicationConfig = fs.readFileSync(path.join(api, 'dog-admin/src/main/resources', environment, 'application.yaml'), 'utf8');
  assert.ok(baseConfig.includes('${MYSQL_DATABASE:dogadmin}'));
  assert.match(applicationConfig, /name: dog-admin/);
}
for (const filename of files.filter(filename => filename.endsWith('.xml'))) {
  const text = fs.readFileSync(filename, 'utf8');
  for (const match of text.matchAll(/(?:namespace|type|resultType|parameterType|class)="(net\.dogadmin\.[\w.]+)"/g)) {
    assert.ok(classes.has(match[1]), match[1]);
  }
}
assert.ok(fs.existsSync(path.join(web, 'src/theme/dogadmin.less')));
assert.ok(fs.readFileSync(path.join(web, 'src/theme/index.less'), 'utf8').includes("'./dogadmin.less'"));
assert.ok(fs.readFileSync(path.join(web, 'src/config/app-config.ts'), 'utf8').includes("websiteName: 'dogadmin'"));
assert.equal(fs.readFileSync(path.join(root, 'mysql/admin.sql'), 'utf8').match(/^CREATE TABLE /gm).length, 23);
assert.ok(fs.statSync(path.join(api, 'dog-base/src/main/resources/ip2region.xdb')).size > 0);
process.stdout.write('dogadmin identity, Java packages, mapper references and configuration checks passed.\n');
const crypto = require('node:crypto');
const vm = require('node:vm');
const ts = require('typescript');
const encryptSource = fs.readFileSync(path.join(web, 'src/lib/encrypt.ts'), 'utf8');
const protocolModule = { exports: {} };
const protocolCode = ts.transpileModule(encryptSource + '\nexport const protocols = { AES, SM4, AES_KEY, SM4_KEY };', {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022, esModuleInterop: true },
}).outputText;
vm.runInNewContext(protocolCode, { module: protocolModule, exports: protocolModule.exports, require });
const protocols = protocolModule.exports.protocols;
const javaServices = [
  { name: 'AES', file: 'ApiEncryptServiceAesImpl.java', keyName: 'AES_KEY', cipher: 'aes-128-ecb' },
  { name: 'SM4', file: 'ApiEncryptServiceSmImpl.java', keyName: 'SM4_KEY', cipher: 'sm4-ecb' },
];
for (const service of javaServices) {
  const javaSource = fs.readFileSync(path.join(api, 'dog-base/src/main/java/net/dogadmin/base/module/support/apiencrypt/service', service.file), 'utf8');
  const backendKey = javaSource.match(new RegExp('private static final String ' + service.keyName + ' = "([^"]+)";'))?.[1];
  const frontendKey = protocols[service.keyName];
  assert.ok(backendKey === frontendKey, service.name + ' frontend and backend key mismatch');
  assert.ok(Buffer.byteLength(frontendKey, 'utf8') === 16, service.name + ' key length mismatch');
  assert.ok(crypto.getCiphers().includes(service.cipher), service.name + ' native cipher unavailable');
  for (const value of ['dogadmin login', '中文密码验证', { loginName: 'admin', data: '地区与权限' }]) {
    const plain = typeof value === 'object' ? JSON.stringify(value) : value;
    const nativeCipher = crypto.createCipheriv(service.cipher, Buffer.from(frontendKey, 'utf8'), null);
    const encrypted = Buffer.concat([nativeCipher.update(plain, 'utf8'), nativeCipher.final()]);
    const expected = service.name === 'AES' ? encrypted.toString('base64') : Buffer.from(encrypted.toString('hex'), 'utf8').toString('base64');
    assert.ok(protocols[service.name].encryptData(value) === expected, service.name + ' ciphertext mismatch');
    assert.ok(protocols[service.name].decryptData(expected) === plain, service.name + ' decrypt mismatch');
  }
}
process.stdout.write('AES and SM4 key pairing, Unicode round trips and native cipher format checks passed.\n');
