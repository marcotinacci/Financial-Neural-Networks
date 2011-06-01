function count = forecast_plot()

%% calcola gli input
[fin_fts fin_rsi fin_ema5 fin_ema10 fin_ema15 fin_ema20 fin_macd] = ...
    import_financial_time_series('NIKKEI225');
fts = fts2mat(fin_fts.Close);
rsi = fts2mat(fin_rsi);
ema5 = fts2mat(fin_ema5.Close);
ema10 = fts2mat(fin_ema10.Close);
ema15 = fts2mat(fin_ema15.Close);
ema20 = fts2mat(fin_ema20.Close);
macdline = fts2mat(fin_macd.MACDLine);
nineperma = fts2mat(fin_macd.NinePerMA);

%% addestra la rete
net = train_trend_net(fts, rsi, ema5, ema10, ema15, ema20, macdline, nineperma);
sim_out = test_net(net);

%% stampa i risultati
% figure;
% hold all;
% plot(3028:3227,sim_out(1,:)');
% plot(3029:3228,sim_out(2,:)');
% plot(3030:3229,sim_out(3,:)');
% plot(3031:3230,sim_out(4,:)');
% plot(3032:3231,sim_out(5,:)');
% plot(3028:3231,fts(3028:3231)');
% legend('distanza 1', 'distanza 2', 'distanza 3', 'distanza 4', 'distanza 5','dati reali');
% figure; hold all;
% count=0;
% gt02=0;
% for i=1:200
%     if (abs(sim_out(1,i))>0)
%         gt02=gt02+1;
%         if (sign(sim_out(1,i))==sign(fts(3028+i) - fts(3027+i)))
%             count=count+1;
%         end
%     end
% end
% gt02
% count
count=0;
for i=1:200
        if (sign(sim_out(1,i))==sign(fts(3028+i) - fts(3027+i)))
            count=count+1;
        end
    end
%bar([sim_out(1,:)'.*( sign(fts(3029:3228) - fts(3028:3227)))]);
%plot(3028:3227,sim_out(1,:)','.');
%plot(3028:3231,fts(3028:3231)');
%plot(3028:3231,sign(fts(3029:3232) - fts(3028:3231)),'.');

end