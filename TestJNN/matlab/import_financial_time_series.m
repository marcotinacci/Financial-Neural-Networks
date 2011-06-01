function [fts rsi ema5 ema10 ema15 ema20 var_macd obvts] = import_financial_time_series(financial_index)
    fts=ascii2fts('../index/NIKKEI225.dat',0,1,0);
    rsi=rsindex(fts,14);
    ema5 = tsmovavg(fts, 'e', 5);
    ema10 = tsmovavg(fts, 'e', 10);
    ema15 = tsmovavg(fts, 'e', 15);
    ema20 = tsmovavg(fts, 'e', 20);
    var_macd= macd(fts);
end