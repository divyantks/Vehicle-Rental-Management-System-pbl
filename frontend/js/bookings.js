const user = requireLogin('CUSTOMER');
let rentals = [];
if (user) {
  loadAll();
  document.getElementById('bookingsBox').addEventListener('click', onAction);
  document.getElementById('rentalsBox').addEventListener('click', onAction);
}

async function loadAll() {
  try {
    const bookings = (await api('/api/bookings/my')).data;
    rentals = (await api('/api/rentals/my')).data;
    const t = today();
    document.getElementById('bookingsBox').innerHTML = table(
      ['ID', 'Vehicle', 'Start', 'End', 'Days', 'Cost', 'Status', 'Actions'],
      bookings.map(b => {
        let buttons = '';
        if (b.status === 'BOOKED' && t <= b.startDate) 
          buttons += '<button class="btn small danger" data-do="cancel" data-id="' + esc(b.id) + '">Cancel</button> ';
        if (b.status === 'BOOKED' && t >= b.startDate)
          buttons += '<button class="btn small ok" data-do="return" data-id="' + esc(b.id) + '">Return</button>';
        return [esc(b.id), esc(b.vehicleName), esc(b.startDate), esc(b.endDate), b.days,
                money(b.totalCost), badge(b.status), buttons || '-'];
      }));
    document.getElementById('rentalsBox').innerHTML = table(
      ['Rental', 'Booking', 'Vehicle', 'Returned', 'Late days', 'Total', 'Receipt'],
      rentals.map(r => [esc(r.id), esc(r.bookingId), esc(r.vehicleName), esc(r.returnDate), r.lateDays,
        money(r.totalCost), '<button class="btn small outline" data-do="receipt" data-id="' + esc(r.id) + '">View</button>']));
  } catch (e) { showMessage(e.message, 'error'); }
}

async function onAction(e) {
  const action = e.target.dataset.do, id = e.target.dataset.id;
  if (!action) return;
  try {
    if (action === 'receipt') {
      showReceipt(rentals.find(r => r.id === id));
    } else if (action === 'cancel') {
      if (!confirm('Cancel this booking?')) return;
      showMessage((await api('/api/bookings/cancel', 'POST', { bookingId: id })).message);
      loadAll();
    } else if (action === 'return') {
      if (!confirm('Return this vehicle now?')) return;
      const r = await api('/api/bookings/return', 'POST', { bookingId: id });
      showMessage(r.message);
      showReceipt(r.data);
      loadAll();
    }
  } catch (err) { showMessage(err.message, 'error'); }
}

function showReceipt(rental) {
  if (rental) showModal('Rental Receipt', '<pre>' + esc(rental.receipt) + '</pre>');
}
