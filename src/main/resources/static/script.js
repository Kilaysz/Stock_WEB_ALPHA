document.getElementById('update-data').addEventListener('click', function() {
    const company = document.getElementById('company').value;
    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;
    const period = parseInt(document.getElementById('period').value, 10); 

    if (isNaN(period) || period <= 0) {
        alert('Please enter a valid positive integer.');
        return;
    }

    if (startDate > endDate) {
        alert('Start date cannot be later than end date.');
        return; // Get an error prompt
    }

    const data = { company, startDate, endDate };

    fetch('/stockData', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('No data available for the selected dates. Please try again with different dates.');
        }
        return response.json();
    })
    .then(data => {
        console.log(data); 
    })
    .catch(error => {
        alert(error.message);
    });
});