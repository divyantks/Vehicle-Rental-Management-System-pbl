function getToken() { return sessionStorage.getItem('token'); }
function getUser() { try { return JSON.parse(sessionStorage.getItem('user')); } catch (e) { return null; } }
function esc(t) {   // stops names typed by users from being treated as HTML
  return String(t ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}
function money(n) { return 'Rs. ' + Number(n).toFixed(2); }
function today() { return new Date().toLocaleDateString('en-CA'); }   

async function api(path, method = 'GET', data = {}) {
  const options = { method, headers: {} };
  const token = getToken();
  if (token) options.headers['X-Auth-Token'] = token;
  const query = new URLSearchParams(data).toString();
  if (method === 'GET') {
    if (query) path += '?' + query;
  } else {
    options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
    options.body = query;
  }
  let res;
  try { res = await fetch(path, options); }
  catch (e) { throw new Error('Cannot reach the server. Is the Java program running?'); }
  let json;
  try { json = await res.json(); } catch (e) { throw new Error('Unexpected server response'); }
  if (res.status === 401 && token) { sessionStorage.clear(); location.href = '/login.html'; }
  if (!json.success) throw new Error(json.message);
  return json;
}

function showMessage(text, type = 'success') {
  const box = document.getElementById('message');
  if (!box) return;
  box.textContent = text;
  box.className = type;
  box.style.display = 'block';
  clearTimeout(box.timer);
  box.timer = setTimeout(() => { box.style.display = 'none'; }, 4000);
}

function requireLogin(role) {
  const user = getUser();
  if (!getToken() || !user) { location.href = '/login.html'; return null; }
  if (role && user.role !== role) { location.href = '/' + (sessionStorage.getItem('home') || 'login.html'); return null; }
  buildNavbar(user);
  return user;
}

function buildNavbar(user) {
  const links = user.role === 'ADMIN'
    ? [['/admin.html', 'Admin Panel']]
    : [['/dashboard.html', 'Dashboard'], ['/vehicles.html', 'Vehicles'], ['/bookings.html', 'My Bookings']];
  const here = location.pathname;
  document.getElementById('navbar').innerHTML =
    '<nav><a class="brand" href="/' + esc(sessionStorage.getItem('home')) + '">&#128663; VehicleRental</a><div class="links">' +
    links.map(l => '<a class="link ' + (here === l[0] ? 'active' : '') + '" href="' + l[0] + '">' + l[1] + '</a>').join('') +
    '<span>' + esc(user.name) + '</span><button id="logoutBtn">Logout</button></div></nav>';
  document.getElementById('logoutBtn').addEventListener('click', logout);
}

async function logout() {
  try { await api('/api/logout', 'POST'); } catch (e) { /* ignore */ }
  sessionStorage.clear();
  location.href = '/login.html';
}

function badge(text) {
  const cls = { BOOKED: 'info', RETURNED: 'ok', CANCELLED: 'err', Available: 'ok', Booked: 'err' }[text] || '';
  return '<span class="badge ' + cls + '">' + esc(text) + '</span>';
}

function table(head, rows) {
  return '<div class="table-wrap"><table><thead><tr>' + head.map(h => '<th>' + h + '</th>').join('') +
    '</tr></thead><tbody>' +
    (rows.length ? rows.map(r => '<tr>' + r.map(c => '<td>' + c + '</td>').join('') + '</tr>').join('')
                 : '<tr><td colspan="' + head.length + '" class="muted">No records</td></tr>') +
    '</tbody></table></div>';
}

function showModal(title, bodyHtml) {
  const overlay = document.createElement('div');
  overlay.className = 'overlay';
  overlay.innerHTML = '<div class="modal"><h2>' + esc(title) + '</h2>' + bodyHtml +
    '<div class="actions"><button class="btn small" id="closeModal">Close</button></div></div>';
  document.body.appendChild(overlay);
  const close = () => overlay.remove();
  overlay.addEventListener('click', e => { if (e.target === overlay) close(); });
  overlay.querySelector('#closeModal').addEventListener('click', close);
}
