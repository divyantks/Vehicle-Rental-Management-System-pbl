const user = requireLogin('CUSTOMER');
if (user) {
  document.getElementById('welcome').textContent = 'Welcome, ' + user.name + '!';
  loadSummary();
}
async function loadSummary() {
  try {
    const vehicles = (await api('/api/vehicles')).data;
    const bookings = (await api('/api/bookings/my')).data;
    const rentals = (await api('/api/rentals/my')).data;
    document.getElementById('sAvailable').textContent = vehicles.filter(v => v.available).length;
    document.getElementById('sActive').textContent = bookings.filter(b => b.status === 'BOOKED').length;
    document.getElementById('sRentals').textContent = rentals.length;
  } catch (e) { showMessage(e.message, 'error'); }
}
