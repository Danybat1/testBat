export const environment = {
  production: true,
  apiUrl: 'https://api.freightops.com',
  appName: 'FreightOps',
  version: '1.0.0',
  currency: {
    refreshInterval: 300000, // 5 minutes en millisecondes
    fallbackRates: {
      'USD_CDF': 2700,
      'CDF_USD': 0.000370
    },
    defaultCurrency: 'USD',
    supportedCurrencies: ['USD', 'CDF']
  }
};
