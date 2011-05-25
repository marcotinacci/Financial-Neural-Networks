function sim_out = test_net( net )

[fin_fts fin_rsi fin_ema5 fin_ema10 fin_ema15 fin_ema20] = ...
    import_financial_time_series('');
fts = fts2mat(fin_fts.Close);
rsi = fts2mat(fin_rsi);
ema5 = fts2mat(fin_ema5.Close);
ema10 = fts2mat(fin_ema10.Close);
ema15 = fts2mat(fin_ema15.Close);
ema20 = fts2mat(fin_ema20.Close);

output_nel = 200;
begin_idx = 1000;
sim_out = zeros(5,200);
for i=1:output_nel
    input = [fts(begin_idx + i); rsi(begin_idx + i); ema5(begin_idx + i);...
        ema10(begin_idx + i); ema15(begin_idx + i); ema20(begin_idx + i)];
    sim_out(:,i) = sim(net,input);
end

end
