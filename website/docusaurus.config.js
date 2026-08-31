// @ts-check
// Docusaurus site for the uncle-bob-junior showcase, served by GitHub Pages
// from the repo's /docs directory (see the build script's --out-dir).
// Docs-only mode: the docs plugin owns the site root.

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Uncle Bob Junior',
  tagline: 'A clean-code ruleset for coding agents',
  url: 'https://coenraadhuman.github.io',
  baseUrl: '/uncle-bob-junior/',
  organizationName: 'coenraadhuman',
  projectName: 'uncle-bob-junior',
  onBrokenLinks: 'throw',
  // 'detect': .md renders as CommonMark (the ruleset pages embed SKILL.md and
  // its references verbatim, which MDX parsing could mangle), .mdx as MDX.
  markdown: { mermaid: true, format: 'detect' },
  themes: ['@docusaurus/theme-mermaid'],
  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.js',
        },
        blog: false,
        theme: { customCss: './src/css/custom.css' },
      }),
    ],
  ],
  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: { respectPrefersColorScheme: true },
      navbar: {
        title: 'Uncle Bob Junior',
        items: [
          { to: '/benchmark/scoreboard', label: 'Benchmark', position: 'left' },
          { href: 'https://github.com/coenraadhuman/uncle-bob-junior', label: 'GitHub', position: 'right' },
        ],
      },
      footer: {
        style: 'dark',
        copyright: 'All benchmark code on this site is model-generated output, published for comparison. MIT licensed.',
      },
    }),
};

module.exports = config;
