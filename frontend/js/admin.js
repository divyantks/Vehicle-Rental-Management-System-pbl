const user = requireLogin('ADMIN');
let vehicles = [];
if (user) {
  document.querySelectorAll('[data-tab]').forEach(b => b.addEventListener('click', () => showTab(b.dataset.tab)));
  document.getElementById('vehicleForm').addEventListener('submit', saveVehicle);
  document.getElementById('resetForm').addEventListener('click', clearForm);
  document.getElementById('vehicleTable').addEventListener('click', vehicleAction);
  showTab('overview');
}

function showTab(name) {
  document.querySelectorAll('.tab').forEach(t => { t.hidden = t.id !== 'tab-' + name; });
  document.querySelectorAll('[data-tab]').forEach(b => b.classList.toggle('active', b.dataset.tab === name));
  ({ overview: loadOverview, vehicles: loadVehicles, customers: loadCustomers,
     bookings: loadBookings, rentals: loadRentals })[name]();
}

async function load(path, show) {
  try { show((await api(path)).data); } catch (e) { showMessage(e.message, 'error'); }
}

function loadOverview() {
  load('/api/admin/dashboard', d => {
    const items = [['Vehicles', d.totalVehicles], ['Available', d.availableVehicles], ['Customers', d.totalCustomers],
      ['Bookings', d.totalBookings], ['Active bookings', d.activeBookings], ['Rentals', d.totalRentals],
      ['Revenue', money(d.totalRevenue)]];
    document.getElementById('stats').innerHTML = items.map(i => '<div class="card stat"><b>' + i[1] + '</b>' + i[0] + '</div>').join('');
  });
}

function loadVehicles() {
  load('/api/vehicles', list => {
    vehicles = list;
    document.getElementById('vehicleTable').innerHTML = table(
      ['ID', 'Brand', 'Model', 'Type', 'Price/day', 'Status', 'Actions'],
      list.map(v => [esc(v.id), esc(v.brand), esc(v.model), esc(v.type), money(v.pricePerDay),
        badge(v.available ? 'Available' : 'Booked'),
        '<button class="btn small outline" data-do="edit" data-id="' + esc(v.id) + '">Edit</button> ' +
        '<button class="btn small outline" data-do="toggle" data-id="' + esc(v.id) + '">' + (v.available ? 'Mark unavailable' : 'Mark available') + '</button> ' +
        '<button class="btn small danger" data-do="delete" data-id="' + esc(v.id) + '">Delete</button>']));
  });
}

function loadCustomers() {
  load('/api/admin/customers', list => {
    document.getElementById('customerTable').innerHTML = table(['ID', 'Name', 'Email', 'Phone', 'Licence'],
      list.map(c => [esc(c.id), esc(c.name), esc(c.email), esc(c.phone), esc(c.licenseNumber)]));
  });
}

function loadBookings() {
  load('/api/admin/bookings', list => {
    document.getElementById('bookingTable').innerHTML = table(
      ['ID', 'Customer', 'Vehicle', 'Start', 'End', 'Days', 'Cost', 'Status'],
      list.map(b => [esc(b.id), esc(b.customerName), esc(b.vehicleName), esc(b.startDate), esc(b.endDate),
        b.days, money(b.totalCost), badge(b.status)]));
  });
}

function loadRentals() {
  load('/api/admin/rentals', list => {
    document.getElementById('rentalTable').innerHTML = table(
      ['Rental', 'Booking', 'Customer', 'Vehicle', 'Returned', 'Late days', 'Total'],
      list.map(r => [esc(r.id), esc(r.bookingId), esc(r.customerName), esc(r.vehicleName),
        esc(r.returnDate), r.lateDays, money(r.totalCost)]));
  });
}

async function saveVehicle(e) {
  e.preventDefault();
  const id = document.getElementById('vId').value;
  const data = { brand: vBrand.value, model: vModel.value, type: vType.value, price: vPrice.value };
  try {
    if (id) data.id = id;
    const r = await api(id ? '/api/vehicles/update' : '/api/vehicles/add', 'POST', data);
    showMessage(r.message);
    clearForm();
    loadVehicles();
  } catch (err) { showMessage(err.message, 'error'); }
}

function clearForm() {
  document.getElementById('vehicleForm').reset();
  document.getElementById('vId').value = '';
  document.getElementById('formTitle').textContent = 'Add New Vehicle';
}

async function vehicleAction(e) {
  const action = e.target.dataset.do, id = e.target.dataset.id;
  if (!action) return;
  const v = vehicles.find(x => x.id === id);
  try {
    if (action === 'edit') {
      document.getElementById('vId').value = v.id;
      vBrand.value = v.brand; vModel.value = v.model; vType.value = v.type; vPrice.value = v.pricePerDay;
      document.getElementById('formTitle').textContent = 'Update Vehicle ' + v.id;
      scrollTo({ top: 0, behavior: 'smooth' });
    } else if (action === 'toggle') {
      showMessage((await api('/api/vehicles/availability', 'POST', { id, available: !v.available })).message);
      loadVehicles();
    } else if (action === 'delete') {
      if (!confirm('Delete ' + v.brand + ' ' + v.model + '?')) return;
      showMessage((await api('/api/vehicles/delete', 'POST', { id })).message);
      loadVehicles();
    }
  } catch (err) { showMessage(err.message, 'error'); }
}
