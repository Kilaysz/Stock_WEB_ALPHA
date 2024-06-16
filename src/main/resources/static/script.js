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

    const today = new Date().toISOString().split('T')[0];
    if (endDate > today) {
        alert('Selected date cannot be later than today.');
        return;
    }

    localStorage.setItem('company', company);
    localStorage.setItem('startDate', startDate);
    localStorage.setItem('endDate', endDate);
    localStorage.setItem('period', period);

    fetchDataAndUpdateChart(company, startDate, endDate, period);
});

function fetchDataAndUpdateChart(company, startDate, endDate, period) {
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

                    const ctx = document.getElementById('myChart').getContext('2d');
                    if (chart === null) {
                        chart = new Chart(ctx, {
                            type: 'line',
                            data: {
                                labels: dates,
                                datasets: [
                                    {
                                        label: 'Closing Prices',
                                        data: prices,
                                        borderColor: 'gray',
                                        borderWidth: 1,
                                        fill: false
                                    },
                                    {
                                        label: 'Regression Line',
                                        data: regressionLine,
                                        borderColor: 'red',
                                        borderWidth: 1,
                                        fill: false
                                    },
                                    {
                                        label: 'Major Resistance',
                                        data: new Array(dates.length).fill(MajorResistance),
                                        borderColor: 'green',
                                        borderWidth: 1,
                                        fill: false
                                    },
                                    {
                                        label: 'Major Support',
                                        data: new Array(dates.length).fill(MajorSupport),
                                        borderColor: 'blue',
                                        borderWidth: 1,
                                        fill: false
                                    },
                                    {
                                        label: 'Minor Resistance',
                                        data: new Array(dates.length).fill(MinorResistance),
                                        borderColor: 'purple',
                                        borderWidth: 1,
                                        fill: false
                                    },
                                    {
                                        label: 'Minor Support',
                                        data: new Array(dates.length).fill(MinorSupport),
                                        borderColor: 'orange',
                                        borderWidth: 1,
                                        fill: false
                                    },
                                    {
                                        label: 'Moving Average',
                                        data: processedMovingAverage,
                                        borderColor: 'yellow',
                                        borderWidth: 1,
                                        fill: false,
                                        spanGaps: true // Connects the dots if there are null values
                                    }
                                ]
                            },
                            options: {
                                scales: {
                                    xAxes: [{
                                        type: 'time',
                                        time: {
                                            unit: 'day',
                                            tooltipFormat: 'MM/DD/YYYY'
                                        },
                                        scaleLabel: {
                                            display: true,
                                            labelString: 'Date'
                                        }
                                    }],
                                    yAxes: [{
                                        scaleLabel: {
                                            display: true,
                                            labelString: 'Price'
                                        }
                                    }]
                                }
                            }
                        });
                    } else {
                        chart.data.labels = dates;
                        chart.data.datasets[0].data = prices;
                        chart.data.datasets[1].data = regressionLine;
                        chart.data.datasets[2].data = new Array(dates.length).fill(MajorResistance);
                        chart.data.datasets[3].data = new Array(dates.length).fill(MajorSupport);
                        chart.data.datasets[4].data = new Array(dates.length).fill(MinorResistance);
                        chart.data.datasets[5].data = new Array(dates.length).fill(MinorSupport);
                        chart.data.datasets[6].data = processedMovingAverage;
                        chart.update();
                    }
                });
            });
        });
    })
    .catch(error => {
        alert(error.message); 
    });
}

window.addEventListener('load', function() {
    const company = localStorage.getItem('company');
    const startDate = localStorage.getItem('startDate');
    const endDate = localStorage.getItem('endDate');
    const period = localStorage.getItem('period');

    if (company && startDate && endDate && period) {
        document.getElementById('company').value = company;
        document.getElementById('start-date').value = startDate;
        document.getElementById('end-date').value = endDate;
        document.getElementById('period').value = period;

        fetchDataAndUpdateChart(company, startDate, endDate, parseInt(period, 10));
    }
});