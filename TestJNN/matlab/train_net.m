function net = train_net(fts, rsi, ema5, ema10, ema15, ema20, macdline, nineperma)

%% parametri

test_num = 1000;

%% struttura rete

hidden_neurons = 10;

%% strutture dati

begin_idx = 2026;
% size(P) = RxQ1 -> (input_neurons x test_num)
for i=1:test_num
    P(:,i) = [fts(begin_idx+i); fts(begin_idx);fts(begin_idx-1);fts(begin_idx-2);fts(begin_idx-3); rsi(begin_idx+i); ema5(begin_idx+i); ...
        ema10(begin_idx+i); ema15(begin_idx+i); ema20(begin_idx+i); ...
        macdline(begin_idx+i);nineperma(begin_idx+i)];
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

