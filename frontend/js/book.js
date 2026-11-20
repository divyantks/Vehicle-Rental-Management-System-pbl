const user = requireLogin('CUSTOMER');
let vehicle = null;
if (user) init();

async function init() {
  const id = new URLSearchParams(location.search).get('id');
  const dateInput = document.getElementById('startDate');
  dateInput.min = today();
  dateInput.value = today();
  try {
    vehicle = (await api('/api/vehicles/details', 'GET', { id: id || '' })).data;
    document.getElementById('vehicleInfo').innerHTML =
      '<h2>' + esc(vehicle.brand) + ' ' + esc(vehicle.model) + '</h2><p>' + esc(vehicle.type) +
      ' &middot; ' + money(vehicle.pricePerDay) + ' per day</p>' + badge(vehicle.available ? 'Available' : 'Booked');
    if (!vehicle.available) {
      document.getElementById('bookBtn').disabled = true;
      showMessage('This vehicle is not available', 'error');
    }
    updateCost();
  } catch (e) {
    document.getElementById('bookBtn').disabled = true;
    showMessage(e.message, 'error');
  }
}

function updateCost() {   
  const days = parseInt(document.getElementById('days').value, 10);
  document.getElementById('cost').textContent = (vehicle && days > 0) ? money(vehicle.pricePerDay * days) : '-';
}
document.getElementById('days').addEventListener('input', updateCost);

document.getElementById('bookForm').addEventListener('submit', async e => {
  e.preventDefault();
  try {
    const r = await api('/api/bookings/book', 'POST', {
      vehicleId: vehicle.id,
      startDate: document.getElementById('startDate').value,
      days: document.getElementById('days').value
    });
    showMessage(r.message + '. Total: ' + money(r.data.totalCost));
    document.getElementById('bookBtn').disabled = true;
    setTimeout(() => { location.href = '/bookings.html'; }, 1500);
  } catch (err) { showMessage(err.message, 'error'); }
});
