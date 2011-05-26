%% calcola gli input
[fin_fts fin_rsi fin_ema5 fin_ema10 fin_ema15 fin_ema20] = ...
    import_financial_time_series('NIKKEI225');
fts = fts2mat(fin_fts.Close);
rsi = fts2mat(fin_rsi);
ema5 = fts2mat(fin_ema5.Close);
ema10 = fts2mat(fin_ema10.Close);
ema15 = fts2mat(fin_ema15.Close);
ema20 = fts2mat(fin_ema20.Close);

%% addestra la rete
net = train_net(fts, rsi, ema5, ema10, ema15, ema20);
sim_out = test_net(net);

%% stampa i risultati
figure;
hold all;
plot(1002:1201,sim_out(1,:)');
plot(1003:1202,sim_out(2,:)');
plot(1004:1203,sim_out(3,:)');
plot(1005:1204,sim_out(4,:)');
plot(1006:1205,sim_out(5,:)');
plot(1002:1205,fts(1002:1205)');
legend('distanza 1', 'distanza 2', 'distanza 3', 'distanza 4', 'distanza 5','dati reali');