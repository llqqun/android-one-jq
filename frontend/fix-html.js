import fs from 'fs';
import path from 'path';

const htmlPath = path.join(path.dirname(import.meta.url).replace('file:///', ''), '../app/src/main/assets/index.html');

fs.readFile(htmlPath, 'utf8', (err, data) => {
  if (err) {
    console.error('读取文件失败:', err);
    return;
  }
  
  const modifiedData = data
    .replace(/<script type="module" crossorigin/g, '<script')
    .replace(/<link rel="modulepreload"/g, '<!-- removed modulepreload -->');
  
  fs.writeFile(htmlPath, modifiedData, 'utf8', (err) => {
    if (err) {
      console.error('写入文件失败:', err);
      return;
    }
    console.log('HTML文件已修复，移除了type="module"属性');
  });
});
