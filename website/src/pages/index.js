// Homepage: a standalone landing page (hero + features, no docs sidebar),
// modelled on docusaurus.io's own front page. All volatile content (rules,
// scores, code) lives in the generated docs; this page only links to it.
import React from 'react';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';
import useBaseUrl from '@docusaurus/useBaseUrl';

const FEATURES = [
  {
    title: 'A reviewer in every session',
    body: 'A 13-point clean-code checklist and a final gate are injected into '
      + 'every Claude Code session: one job per function, ten statements max, '
      + 'no magic values, tests in the same reply. The slim core rides along; '
      + 'the depth loads on demand.',
    to: '/ruleset/skill',
    cta: 'Read the ruleset',
  },
  {
    title: 'Trust, but verify',
    body: 'A Stop hook runs habit-hooks over the branch\'s changed files when '
      + 'the agent finishes a turn — findings block the finish once with the '
      + 'report as the fix-it prompt. Advisory rules become a feedback loop.',
    to: '/plugin',
    cta: 'Install the plugin',
  },
  {
    title: 'Measured, not promised',
    body: 'The repo benchmarks itself: the same tasks in Java, Python, and C#, '
      + 'with and without the ruleset, judged by an independent smell detector '
      + 'and correctness gates. Every run is published, code included.',
    to: '/benchmark',
    cta: 'See the scoreboard',
  },
];

function Feature({ title, body, to, cta }) {
  return (
    <div className="col col--4">
      <div className="homeFeature">
        <h3>{title}</h3>
        <p>{body}</p>
        <Link to={to}>{cta} →</Link>
      </div>
    </div>
  );
}

export default function Home() {
  return (
    <Layout description="Makes your AI agent think like the meticulous senior dev in the room: clean code that is easy to read, simple to understand, and safe to change.">
      <header className="homeHero">
        <div className="container homeHeroInner">
          <div className="homeHeroText">
            <h1>Uncle Bob Junior</h1>
            <p className="homeTagline">
              Makes your AI agent think like the meticulous senior dev in the room.
              <br />
              Code is read far more often than it is written, so it writes for
              the reader: clean code that is easy to read, simple to
              understand, and safe to change.
            </p>
            <div className="homeButtons">
              <Link className="button button--secondary button--lg" to="/plugin">
                Install the plugin
              </Link>
              <Link className="button button--outline button--secondary button--lg" to="/benchmark">
                See the benchmark
              </Link>
            </div>
          </div>
          <img
            className="homeMascot"
            src={useBaseUrl('/img/mascot-bot.svg')}
            alt="Checkbot, the Uncle Bob Junior mascot, holding a ticked checklist"
            width="280"
            height="300"
          />
        </div>
      </header>
      <main>
        <section className="homeFeatures">
          <div className="container">
            <div className="row">
              {FEATURES.map((feature) => (
                <Feature key={feature.title} {...feature} />
              ))}
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
