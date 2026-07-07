(function () {
  var state = {
    settings: { workingDirectory: '', language: 'en' },
    busy: false,
    lang: {}
  };

  var BASE = window.location.origin;
  var $ = function (s) { return document.querySelector(s); };
  var $$ = function (s) { return document.querySelectorAll(s); };
  var outputContent = $('#output-content');
  var logTimer = null;

  // ---- Navigation ----
  $$('.nav-item').forEach(function (el) {
    el.addEventListener('click', function (e) {
      e.preventDefault();
      $$('.nav-item').forEach(function (n) { n.classList.remove('active'); });
      $$('.tab').forEach(function (t) { t.classList.remove('active'); });
      el.classList.add('active');
      var tab = document.getElementById('tab-' + el.dataset.tab);
      if (tab) tab.classList.add('active');
      if (el.dataset.tab === 'settings') loadSettings();
    });
  });

  // ---- Output ----
  function writeOutput(msg) {
    if (typeof msg === 'object') msg = JSON.stringify(msg, null, 2);
    outputContent.textContent += msg + '\n';
    outputContent.scrollTop = outputContent.scrollHeight;
  }
  function clearOutput() { outputContent.textContent = ''; }
  $('#btn-clear-log').addEventListener('click', clearOutput);

  var outputCollapsed = false;
  $('#btn-toggle-log').addEventListener('click', function () {
    outputCollapsed = !outputCollapsed;
    outputContent.style.display = outputCollapsed ? 'none' : '';
    this.textContent = outputCollapsed ? '\u25BC' : '\u25B2';
  });

  // ---- i18n ----
  function loadLanguage(langCode) {
    return fetch(BASE + '/i18n/' + langCode + '.json')
      .then(function (r) { return r.json(); })
      .then(function (translations) {
        state.lang = translations;
        applyTranslations();
        return translations;
      })
      .catch(function () {
        // fallback: load English if selected language fails
        if (langCode !== 'en') return loadLanguage('en');
      });
  }

  function applyTranslations() {
    $$('[data-i18n]').forEach(function (el) {
      var key = el.dataset.i18n;
      if (state.lang[key]) {
        if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
          el.placeholder = state.lang[key];
        } else if (el.tagName === 'OPTION') {
          el.textContent = state.lang[key];
        } else {
          el.textContent = state.lang[key];
        }
      }
    });
  }

  function t(key) {
    return state.lang[key] || key;
  }

  // ---- API ----
  function api(endpoint, body, method) {
    var opts = { headers: { 'Content-Type': 'application/json' } };
    if (body !== undefined) {
      opts.method = method || 'POST';
      opts.body = JSON.stringify(body);
    } else {
      opts.method = method || 'GET';
    }
    return fetch(BASE + '/api/' + endpoint, opts).then(function (r) { return r.json(); });
  }

  // ---- Status ----
  function checkStatus() {
    api('status').then(function (json) {
      if (json.status === 'ok') {
        setOnline(true);
        var info = json.data;
        var html = '';
        for (var key in info) {
          if (key === 'revision' && (info[key] === null || info[key] === undefined)) continue;
          html += '<span class="label">' + key + '</span><span>' + info[key] + '</span>';
        }
        $('#server-info').innerHTML = html;
      } else {
        setOnline(false);
      }
    }).catch(function () { setOnline(false); });
  }

  function startLogPoll() {
    if (logTimer) return;
    logTimer = setInterval(function () {
      var xhr = new XMLHttpRequest();
      xhr.open('GET', BASE + '/api/log', true);
      xhr.onload = function () {
        if (xhr.status === 200) {
          try {
            var json = JSON.parse(xhr.responseText);
            if (json.status === 'ok' && json.data && json.data.length > 0) {
              writeOutput(json.data.replace(/\n$/, ''));
            }
          } catch (e) {}
        }
      };
      xhr.send();
    }, 2000);
  }

  function setOnline(online) {
    var dot = $('#status-dot');
    var label = $('#status-label');
    if (online) {
      dot.className = 'dot-online';
      label.textContent = t('connection.online');
      startLogPoll();
    } else {
      dot.className = 'dot-offline';
      label.textContent = t('connection.offline');
      if (logTimer) { clearInterval(logTimer); logTimer = null; }
    }
  }

  function setBusy(b) {
    state.busy = b;
    var dot = $('#status-dot');
    var label = $('#status-label');
    if (b) {
      dot.className = 'dot-busy';
      label.textContent = t('connection.busy');
    } else if (state.lang) {
      setOnline(true);
    }
  }

  // ---- Forms ----
  $$('.api-form').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      var endpoint = form.dataset.endpoint;
      var fd = new FormData(form);
      var body = {};
      var files = [];

      for (var pair of fd.entries()) {
        var key = pair[0], value = pair[1].trim();
        if (key === 'files') {
          files = value.split('\n').map(function (f) { return f.trim(); }).filter(function (f) { return f.length > 0; });
        } else if (key === 'def') {
          var defs = {};
          value.split('\n').forEach(function (line) {
            line = line.trim();
            if (line.length > 0) {
              var idx = line.indexOf('=');
              if (idx > 0) defs[line.substring(0, idx).trim()] = line.substring(idx + 1).trim();
              else defs[line] = '';
            }
          });
          if (Object.keys(defs).length > 0) body[key] = defs;
        } else if (form.querySelector('[name="' + key + '"]').type === 'checkbox') {
          body[key] = value === 'on';
        } else if (value.length > 0) {
          body[key] = value;
        }
      }
      if (files.length > 0) body.files = files;
      if (state.settings.workingDirectory) {
        body.workingDirectory = state.settings.workingDirectory;
      }

      writeOutput('> POST /api/' + endpoint + ' ' + JSON.stringify(body));
      setBusy(true);

      api(endpoint, body).then(function (json) {
        writeOutput(json);
        if (json.status === 'error') writeOutput('!! ' + (json.message || 'Unknown error'));
      }).catch(function (err) {
        writeOutput('!! ' + err.message);
      }).finally(function () {
        setBusy(false);
      });
    });
  });

  // ---- Settings ----
  function loadSettings() {
    api('settings').then(function (json) {
      if (json.status === 'ok' && json.data) {
        state.settings = json.data;
        $('#settings-wd').value = json.data.workingDirectory || '';
        $('#settings-lang').value = json.data.language || 'en';
        $('#wd-display').textContent = json.data.workingDirectory || '\u2014';
        if (json.data.workingDirectory) loadDirectory(json.data.workingDirectory);
      }
    }).catch(function () {});
  }

  function loadDirectory(dir) {
    var card = $('#wd-files-card');
    var container = $('#wd-files');
    card.style.display = 'none';
    api('files', { path: dir }).then(function (json) {
      if (json.status === 'ok' && Array.isArray(json.data)) {
        card.style.display = 'block';
        container.innerHTML = '';
        json.data.forEach(function (f) {
          var div = document.createElement('div');
          div.className = 'file-item' + (f.isDirectory ? ' is-dir' : '');
          div.textContent = (f.isDirectory ? '/ ' : '  ') + f.name;
          container.appendChild(div);
        });
      }
    }).catch(function () {});
  }

  $('#btn-save-settings').addEventListener('click', function () {
    var wd = $('#settings-wd').value.trim();
    var lang = $('#settings-lang').value;
    setBusy(true);
    var payload = {};
    if (wd !== state.settings.workingDirectory) payload.workingDirectory = wd;
    if (lang !== state.settings.language) payload.language = lang;
    if (Object.keys(payload).length === 0) { setBusy(false); return; }

    api('settings', payload).then(function (json) {
      if (json.status === 'ok') {
        writeOutput(t('output.saved'));
        state.settings = json.data;
        $('#wd-display').textContent = json.data.workingDirectory || '\u2014';
        if (json.data.language) loadLanguage(json.data.language);
        if (json.data.workingDirectory) loadDirectory(json.data.workingDirectory);
      }
    }).catch(function (err) {
      writeOutput('!! ' + err.message);
    }).finally(function () { setBusy(false); });
  });

  // ---- Init ----
  // load saved language, then start polling
  api('settings').then(function (json) {
    if (json.status === 'ok' && json.data && json.data.language) {
      state.settings = json.data;
      $('#settings-wd').value = json.data.workingDirectory || '';
      $('#settings-lang').value = json.data.language;
      $('#wd-display').textContent = json.data.workingDirectory || '\u2014';
      return loadLanguage(json.data.language);
    }
    return loadLanguage('en');
  }).then(function () {
    writeOutput(t('output.ready'));
    checkStatus();
    setInterval(checkStatus, 10000);
  }).catch(function () {
    writeOutput('FileBot Server ready.');
    checkStatus();
    setInterval(checkStatus, 10000);
  });
})();
