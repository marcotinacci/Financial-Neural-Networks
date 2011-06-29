function sim_out = test_net( net, test )

[fin_fts fin_rsi fin_ema5 fin_ema10 fin_ema15 fin_ema20 fin_macd] = ...
    import_financial_time_series('');
fts = fts2mat(fin_fts.Close);
rsi = fts2mat(fin_rsi);
ema5 = fts2mat(fin_ema5.Close);
ema10 = fts2mat(fin_ema10.Close);
ema15 = fts2mat(fin_ema15.Close);
ema20 = fts2mat(fin_ema20.Close);
%macdline= fts2mat(fin_macd.MACDLine);
%nineperma= fts2mat(fin_macd.NinePerMA);

output_nel = 973;
%begin_idx = 1;
sim_out = zeros(1,200);
i=1;
for j=1:output_nel
    if(ismember(j,test))
        input = [fts(j );fts(j-1);fts(j-2);fts(j-3);fts(j-4); rsi(j); ema5(j);...
            ema10(j); ema15(j); ema20(j)];
            %macdline(begin_idx + i); nineperma(begin_idx + i)];
        sim_out(:,i) = sim(net,input);
        i=i+1;
    end
end

end
