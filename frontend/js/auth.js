const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

if (loginForm) {
  sessionStorage.clear();
  loginForm.addEventListener('submit', async e => {
    e.preventDefault();
    try {
      const r = await api('/api/login', 'POST', {
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
      });
      sessionStorage.setItem('token', r.data.token);
      sessionStorage.setItem('user', JSON.stringify(r.data.user));
      sessionStorage.setItem('home', r.data.home);
      location.href = '/' + r.data.home;     // admin.html or dashboard.html, decided by Java
    } catch (err) { showMessage(err.message, 'error'); }
  });
}

if (registerForm) {
  registerForm.addEventListener('submit', async e => {
    e.preventDefault();
    const f = id => document.getElementById(id).value;
    if (f('password') !== f('confirm')) { showMessage('Passwords do not match', 'error'); return; }
    try {
      const r = await api('/api/register', 'POST', {
        name: f('name'), email: f('email'), phone: f('phone'), license: f('license'), password: f('password')
      });
      showMessage(r.message);
      setTimeout(() => { location.href = '/login.html'; }, 1500);
    } catch (err) { showMessage(err.message, 'error'); }
  });
}
