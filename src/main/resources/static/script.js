let chart = null;

// Add event listener when Update Data button is clicked
document.getElementById('update-data').addEventListener('click', function () {
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
        return;
    }

    const today = new Date().toISOString().split('T')[0];
    if (endDate > today) {
        alert('Selected date cannot be later than today.');
        return;
    }

    // Store data in local storage to handle chart reload problem
    localStorage.setItem('company', company);
    localStorage.setItem('startDate', startDate);
    localStorage.setItem('endDate', endDate);
    localStorage.setItem('period', period);

    fetchDataAndUpdateChart(company, startDate, endDate, period);
});

// Helper function to fetch data from the backend
async function fetchData(url, data) {
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error(`Failed to fetch data from ${url}: ${response.statusText}`);
    }
    return response.json();
}

// Handle request from client-side and response from backend
async function fetchDataAndUpdateChart(company, startDate, endDate, period) {
    try {
        // Fetch closing prices
        const stockData = await fetchData('/stockData', { company, startDate, endDate });
        const closingPrices = stockData.closingPrices;

        // Fetch linear regression data
        const lineregression = await fetchData('/calculateLineRegression', { company, startDate, endDate });

        // Fetch resistance and support levels
        const resistanceAndSupport = await fetchData('/calculateResistanceAndSupport', { company, startDate, endDate });
        const { MajorResistance, MajorSupport, MinorResistance, MinorSupport } = resistanceAndSupport.resistanceAndSupport;

        // Fetch moving average
        const movingAverage = await fetchData('/calculateMovingAverage', { company, startDate, endDate, period });

        // Fetch standard deviation
        const standardDeviationResponse = await fetchData('/calculateStandardDeviation', { company, startDate, endDate });
        const standardDeviation = standardDeviationResponse.standardDeviation;

        // Update standard deviation display
        document.getElementById('std-dev').innerText = `Standard Deviation: ${standardDeviation.toFixed(2)}`;

        // Prepare data for the chart
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

        // Process moving average to start from the nth point
        const processedMovingAverage = new Array(dates.length).fill(null);
        for (let i = period - 1; i < dates.length; i++) {
            if (i >= period - 1 && movingAverage.movingAverage[i - (period - 1)] !== undefined) {
                processedMovingAverage[i] = movingAverage.movingAverage[i - (period - 1)];
            }
        }

        // Draw or update the chart
        const ctx = document.getElementById('myChart').getContext('2d');
        const chartData = {
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
        };

        if (chart === null) {
            chart = new Chart(ctx, {
                type: 'line',
                data: chartData,
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
            // Dynamically update the chart
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
    } catch (error) {
        alert(error.message);
    }
}

// Load data from local storage on page load
window.addEventListener('load', function () {
    const company = localStorage.getItem('company');
    const startDate = localStorage.getItem('startDate');
    const endDate = localStorage.getItem('endDate');
    const period = localStorage.getItem('period');

    // Check local storage once the webpage is reloaded
    if (company && startDate && endDate && period) {
        document.getElementById('company').value = company;
        document.getElementById('start-date').value = startDate;
        document.getElementById('end-date').value = endDate;
        document.getElementById('period').value = period;

        fetchDataAndUpdateChart(company, startDate, endDate, parseInt(period, 10));
    }

    // Theme mode implementation
    const modeToggle = document.getElementById('mode-toggle');
    const modeIcon = document.getElementById('mode-icon');
    const iconPath = document.getElementById('icon-path');
    const iconCircle = document.getElementById('icon-circle');

    modeToggle.addEventListener('click', function () {
        document.body.classList.toggle('night-mode');
        const isNightMode = document.body.classList.contains('night-mode');

        if (isNightMode) {
            iconPath.setAttribute('d', 'M152.62 126.77c0-33 4.85-66.35 17.23-94.77C87.54 67.83 32 151.89 32 247.38 32 375.85 136.15 480 264.62 480c95.49 0 179.55-55.54 215.38-137.85-28.42 12.38-61.8 17.23-94.77 17.23-128.47 0-232.61-104.14-232.61-232.61z'); // Moon path
            iconPath.setAttribute('stroke', 'white');
            iconPath.setAttribute('fill', 'white');
            iconCircle.setAttribute('stroke', 'none');
        } else {
            iconPath.setAttribute('d', 'M256 48v48m0 320v48m147.08-355.08l-33.94 33.94M142.86 369.14l-33.94 33.94M464 256h-48m-320 0H48m355.08 147.08l-33.94-33.94M142.86 142.86l-33.94-33.94'); // Sun path
            iconPath.setAttribute('stroke', 'black');
            iconPath.setAttribute('fill', 'none');
            iconCircle.setAttribute('stroke', 'black');
        }
    });
});