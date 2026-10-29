/**
 * Theme toggle — light / dark
 * Persists preference in localStorage.
 * Applied on <html> as data-theme="light" | "dark" (dark is default/absent).
 */
(function () {
  const STORAGE_KEY = 'wh-theme';
  const root = document.documentElement;

  // Apply saved theme immediately (before paint) to avoid flash
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved === 'light') root.setAttribute('data-theme', 'light');

  function getTheme() {
    return root.getAttribute('data-theme') === 'light' ? 'light' : 'dark';
  }

  function setTheme(theme) {
    if (theme === 'light') {
      root.setAttribute('data-theme', 'light');
    } else {
      root.removeAttribute('data-theme');
    }
    localStorage.setItem(STORAGE_KEY, theme);
    // update every toggle button label on the page
    document.querySelectorAll('[data-theme-label]').forEach(el => {
      el.textContent = theme === 'light' ? '🌙 Dark mode' : '☀️ Light mode';
    });
  }

  // Run after DOM ready
  document.addEventListener('DOMContentLoaded', function () {
    // Sync labels
    const theme = getTheme();
    document.querySelectorAll('[data-theme-label]').forEach(el => {
      el.textContent = theme === 'light' ? '🌙 Dark mode' : '☀️ Light mode';
    });

    // Wire every toggle button
    document.querySelectorAll('.theme-toggle').forEach(btn => {
      btn.addEventListener('click', function () {
        setTheme(getTheme() === 'dark' ? 'light' : 'dark');
      });
    });
  });
})();
