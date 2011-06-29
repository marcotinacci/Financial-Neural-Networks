function [count sim_out fts] = forecast_plot(pippo, test)

%% calcola gli input
[fin_fts fin_rsi fin_ema5 fin_ema10 fin_ema15 fin_ema20 fin_macd] = ...
    import_financial_time_series('NASDAQ100');
fts = fts2mat(fin_fts.Close);
rsi = fts2mat(fin_rsi);
ema5 = fts2mat(fin_ema5.Close);
ema10 = fts2mat(fin_ema10.Close);
ema15 = fts2mat(fin_ema15.Close);
ema20 = fts2mat(fin_ema20.Close);
macdline = fts2mat(fin_macd.MACDLine);
nineperma = fts2mat(fin_macd.NinePerMA);

%% addestra la rete
net = train_net(fts, rsi, ema5, ema10, ema15, ema20, pippo);
sim_out = test_net(net, test);

%% stampa i risultati
%figure;
%hold all;
%plot(3028:3227,sim_out(1,:)');
%plot(sim_out(:)');
%plot(3028:3231,fts(3028:3231)');
%plot(fts(1002:1252)');
%legend('previsioni','dati reali');
count=0;
i=1;
for j=1:973
    if(ismember(j,test) == 1)
        %if (sign(sim_out(i)-fts(j))==sign(fts(j+5) - fts(j)))
        if (sign(sim_out(i))==sign(fts(j+5) - fts(j)))
            count=count+1;
            i=i+1;
        end
    end
end
%bar([sim_out(1,:)'.*( sign(fts(3029:3228) - fts(3028:3227)))]);
%plot(3028:3227,sim_out(1,:)','.');
%plot(3028:3231,fts(3028:3231)');
%plot(3028:3231,sign(fts(3029:3232) - fts(3028:3231)),'.');

end