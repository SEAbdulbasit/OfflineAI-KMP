/* Supplemental presentation behavior. The downloaded deck-stage.js is unchanged. */
(() => {
  const ready = () => {
    const deck = document.querySelector('deck-stage');
    if (!deck || !deck.goTo) return;
    const notes = JSON.parse(document.getElementById('speaker-notes').textContent);
    const panel = document.getElementById('presenter-notes');
    const guard = document.getElementById('quiz-guard');
    const cue = document.getElementById('quiz-cue');
    const status = document.getElementById('runtime-status');
    const slides = Array.from(deck.querySelectorAll(':scope > section'));
    const total = slides.length;
    const qaIndex = slides.findIndex(slide => slide.dataset.role === 'qa');
    const firstReference = slides.findIndex(slide => slide.dataset.backup === 'true');
    const quizIndex = slides.indexOf(guard.closest('section'));
    const go = deck._go.bind(deck);
    let references = deck.index >= firstReference;
    let statusTimer;
    const tell = (message) => { status.textContent = message; clearTimeout(statusTimer); statusTimer = setTimeout(() => status.textContent = '', 3500); };
    const reveal = () => { guard.setAttribute('data-revealed', ''); guard.setAttribute('aria-hidden', 'false'); cue.style.visibility = 'hidden'; };
    deck._go = (index, reason = 'api') => {
      if (index <= 0) references = false;
      if (deck.index === quizIndex && index > quizIndex && !guard.hasAttribute('data-revealed')) { reveal(); return; }
      if (!references && reason === 'keyboard' && index === total - 1) index = qaIndex;
      if (!references && deck.index === qaIndex && index > qaIndex) { tell('Q&A · press B to open the reference slides'); return; }
      go(index, reason);
    };
    const update = () => {
      const i = deck.index;
      document.getElementById('notes-heading').textContent = `Slide ${i + 1} of ${total} · ${deck.querySelectorAll('section')[i]?.dataset.label || ''}`;
      document.getElementById('notes-content').textContent = notes[i] || '';
      document.getElementById('progress').style.width = `${(i + 1) / (references ? total : qaIndex + 1) * 100}%`;
      document.getElementById('backup-toggle').textContent = references ? 'Main talk B' : 'References B';
    };
    deck.addEventListener('slidechange', (event) => {
      guard.removeAttribute('data-revealed'); guard.setAttribute('aria-hidden', 'true'); cue.style.visibility = '';
      update();
    });
    const toggleNotes = () => { panel.hidden = !panel.hidden; document.getElementById('notes-toggle').setAttribute('aria-pressed', String(!panel.hidden)); update(); };
    const fullscreen = async () => { try { if (document.fullscreenElement) await document.exitFullscreen(); else await document.documentElement.requestFullscreen(); } catch { tell('Fullscreen is unavailable in this browser surface.'); } };
    const backups = () => { references = !references; go(references ? firstReference : qaIndex, 'api'); update(); };
    document.getElementById('notes-toggle').addEventListener('click', toggleNotes);
    document.getElementById('fullscreen-toggle').addEventListener('click', fullscreen);
    document.getElementById('backup-toggle').addEventListener('click', backups);
    window.addEventListener('keydown', (event) => {
      if (event.metaKey || event.ctrlKey || event.altKey || event.target?.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(event.target?.tagName || '')) return;
      const key = event.key.toLowerCase();
      if (key === 'n') { event.preventDefault(); toggleNotes(); }
      if (key === 'f') { event.preventDefault(); fullscreen(); }
      if (key === 'b') { event.preventDefault(); backups(); }
      if (event.key === 'Escape' && !panel.hidden) { panel.hidden = true; update(); }
    });
    const hash = () => {
      const match = location.hash.match(/^#(?:slide[-=]?)?(\d+)$/i);
      if (!match) return;
      const target = Math.max(0, Math.min(total - 1, Number(match[1]) - 1));
      references = target >= firstReference;
      go(target, 'api'); update();
    };
    window.addEventListener('hashchange', hash);
    window.addEventListener('keydown', (event) => {
      if (event.target?.closest?.('#kmp-controls') && [' ', 'Enter'].includes(event.key)) event.stopImmediatePropagation();
    }, true);
    // The original component reads #N at mount; this also supports #slide-N links.
    hash(); update();
  };
  customElements.whenDefined('deck-stage').then(() => {
    if (document.readyState === 'loading') window.addEventListener('DOMContentLoaded', () => requestAnimationFrame(ready), { once: true });
    else requestAnimationFrame(ready);
  });
})();
