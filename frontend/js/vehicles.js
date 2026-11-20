const user = requireLogin('CUSTOMER');
let timer;
if (user) {
  loadVehicles();
  document.getElementById('keyword').addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(loadVehicles, 300); });
  document.getElementById('typeFilter').addEventListener('change', loadVehicles);
  document.getElementById('onlyAvailable').addEventListener('change', loadVehicles);
  document.getElementById('grid').addEventListener('click', showDetails);
}

async function loadVehicles() {
  try {
    const r = await api('/api/vehicles', 'GET', {
      keyword: document.getElementById('keyword').value,
      type: document.getElementById('typeFilter').value
    });
    let list = r.data;
    if (document.getElementById('onlyAvailable').checked) list = list.filter(v => v.available);
    document.getElementById('grid').innerHTML = list.length ? list.map(card).join('')
      : '<p class="muted">No vehicles found.</p>';
  } catch (e) { showMessage(e.message, 'error'); }
}

function card(v) {
  return '<div class="card vehicle"><span class="badge">' + esc(v.type) + '</span>' +
    '<h3>' + esc(v.brand) + ' ' + esc(v.model) + '</h3>' +
    '<div class="price">' + money(v.pricePerDay) + ' <small class="muted">/ day</small></div>' +
    badge(v.available ? 'Available' : 'Booked') +
    '<div class="actions"><button class="btn small outline" data-id="' + esc(v.id) + '">Details</button>' +
    (v.available ? '<a class="btn small" href="/book.html?id=' + encodeURIComponent(v.id) + '">Book</a>'
                 : '<button class="btn small" disabled>Unavailable</button>') + '</div></div>';
}

async function showDetails(e) {
  const id = e.target.dataset.id;
  if (!id) return;
  try {
    const v = (await api('/api/vehicles/details', 'GET', { id })).data;
    showModal(v.brand + ' ' + v.model,
      '<p><b>ID:</b> ' + esc(v.id) + '<br><b>Type:</b> ' + esc(v.type) +
      '<br><b>Price:</b> ' + money(v.pricePerDay) + ' per day<br><b>Late fee:</b> 1.5 x daily price per extra day' +
      '<br><b>Status:</b> ' + badge(v.available ? 'Available' : 'Booked') + '</p>');
  } catch (err) { showMessage(err.message, 'error'); }
}
