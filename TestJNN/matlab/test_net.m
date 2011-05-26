function sim_out = test_net( net )

[fin_fts fin_rsi fin_ema5 fin_ema10 fin_ema15 fin_ema20 fin_macd] = ...
    import_financial_time_series('');
fts = fts2mat(fin_fts.Close);
rsi = fts2mat(fin_rsi);
ema5 = fts2mat(fin_ema5.Close);
ema10 = fts2mat(fin_ema10.Close);
ema15 = fts2mat(fin_ema15.Close);
ema20 = fts2mat(fin_ema20.Close);
macdline= fts2mat(fin_macd.MACDLine);
nineperma= fts2mat(fin_macd.NinePerMA);

output_nel = 200;
begin_idx = 3026;
sim_out = zeros(5,200);
for i=1:output_nel
    input = [fts(begin_idx + i);fts(begin_idx);fts(begin_idx-1);fts(begin_idx-2);fts(begin_idx-3); rsi(begin_idx + i); ema5(begin_idx + i);...
        ema10(begin_idx + i); ema15(begin_idx + i); ema20(begin_idx + i);...
        macdline(begin_idx + i); nineperma(begin_idx + i)];
    sim_out(:,i) = sim(net,input);
end

end
