/**
 * Bundle the Angular widget build output into a single JS file.
 * Usage: node scripts/bundle-widget.js
 */
const fs = require('fs');
const path = require('path');

const distDir = path.join(__dirname, '..', 'dist', 'widget');
const outFile = path.join(distDir, 'rf-books-widget.js');

if (!fs.existsSync(distDir)) {
  console.error('Widget dist directory not found. Run: ng build --configuration widget');
  process.exit(1);
}

const jsFiles = fs.readdirSync(distDir)
  .filter(f => f.endsWith('.js') && f !== 'rf-books-widget.js')
  .sort((a, b) => {
    // polyfills first, then runtime, then main
    if (a.includes('polyfills')) return -1;
    if (b.includes('polyfills')) return 1;
    if (a.includes('runtime')) return -1;
    if (b.includes('runtime')) return 1;
    return a.localeCompare(b);
  });

let bundle = '';
for (const file of jsFiles) {
  bundle += fs.readFileSync(path.join(distDir, file), 'utf8') + '\n';
}

fs.writeFileSync(outFile, bundle);
console.log(`Bundled ${jsFiles.length} files into ${outFile} (${(bundle.length / 1024).toFixed(1)} KB)`);
