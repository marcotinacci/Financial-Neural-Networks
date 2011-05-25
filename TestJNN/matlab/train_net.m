function net = train_net()

%% parametri

test_num = 1000;

%% struttura rete

input_neurons = 6;
hidden_neurons = 10;
output_neurons = 5;

%% strutture dati

[fin_fts fin_rsi fin_ema5 fin_ema10 fin_ema15 fin_ema20] = ...
    import_financial_time_series('');
fts = fts2mat(fin_fts.Close);
rsi = fts2mat(fin_rsi);
ema5 = fts2mat(fin_ema5.Close);
ema10 = fts2mat(fin_ema10.Close);
ema15 = fts2mat(fin_ema15.Close);
ema20 = fts2mat(fin_ema20.Close);

begin_idx = 19;
% size(P) = RxQ1 -> (input_neurons x test_num)
for i=1:test_num
    P(:,i) = [fts(begin_idx+i); rsi(begin_idx+i); ema5(begin_idx+i); ...
        ema10(begin_idx+i); ema15(begin_idx+i); ema20(begin_idx+i)];
end

% size(T) = SNxQ2 -> (output_neurons x test_num)
for i=1:test_num
    T(:,i) = [fts(begin_idx+i+1); fts(begin_idx+i+2); ...
        fts(begin_idx+i+3); fts(begin_idx+i+4); fts(begin_idx+i+5)];
end

%% addestramento

% crea rete
net = newff(P, T, hidden_neurons, {}, 'trainscg');
%net.divideFcn = 'divideind';
% addestra la rete
net = train(net,P,T);

end

