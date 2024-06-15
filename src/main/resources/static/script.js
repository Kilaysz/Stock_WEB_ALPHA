let chart = null;

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
    .then(result => {
        const closingPrices = result.closingPrices;

        fetch('/calculateLineRegression', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ company, startDate, endDate })
        })
        .then(response => response.json())
        .then(lineregression => {
            const start = new Date(startDate);
            const end = new Date(endDate);
            const dates = [];
            const prices = [];
            const regressionLine = [];
            let t = 1;
            for (let d = start; d <= end; d.setDate(d.getDate() + 1)) {
                const dateStr = d.toISOString().split('T')[0];
                const [year, month, day] = dateStr.split('-');
                const formattedDate = `${month}/${day}/${year}`;

                if (closingPrices[dateStr] !== undefined) {
                    dates.push(formattedDate);
                    prices.push(closingPrices[dateStr]);
                    const regressionValue = lineregression.b0 + lineregression.b1 * t;
                    regressionLine.push(regressionValue);
                    t++;
                }
            }

            if (period > dates.length) {
                alert('The moving average period is larger than the number of stock-days. Please enter a smaller period.');
                return;
            }

            fetch('/calculateResistanceAndSupport', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ company, startDate, endDate })
            })
            .then(response => response.json())
            .then(resistanceAndSupport => {
                const { MajorResistance, MajorSupport, MinorResistance, MinorSupport } = resistanceAndSupport;

                fetch('/calculateMovingAverage', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ company, startDate, endDate, period })
                })
                .then(response => response.json())
                .then(movingAverage => {
                    // Process moving average to start from the nth point
                    const processedMovingAverage = new Array(dates.length).fill(null);
                    for (let i = period - 1; i < dates.length; i++) {
                        if (i >= period - 1 && movingAverage[i - (period - 1)] !== undefined) {
                            processedMovingAverage[i] = movingAverage[i - (period - 1)];
                        }
                    }

                    fetch('/calculateStandardDeviation', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ company, startDate, endDate })
                    })
                    .then(response => response.json())
                    .then(standardDeviation => {
                        document.getElementById('std-dev').innerText = `Standard Deviation: ${standardDeviation.toFixed(2)}`;
                    });
                });
            });
        });
    })
    .catch(error => {
        alert(error.message); 
    });
});