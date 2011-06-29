function net = train_net(fts, rsi, ema5, ema10, ema15, ema20, pippo)

%% parametri

test_num = 973;

%% struttura rete

hidden_neurons = 30;

%% strutture dati

begin_idx = 26;
% size(P) = RxQ1 -> (input_neurons x test_num)
% size(T) = SNxQ2 -> (output_neurons x test_num)

i = 1;
for j=1:test_num
    if(ismember(j,pippo) == 1)
        P(:,i) = [fts(j); fts(j-1);fts(j-2);fts(j-3);fts(j-4); rsi(j); ema5(j); ...
            ema10(j); ema15(j); ema20(j)];
            %macdline(begin_idx+i);nineperma(begin_idx+i)];
        T(:,i) = [sign(fts(j+5)-fts(j))]; 
        i = i+1;
    end
end



%% addestramento

% crea rete
net = newff(P, T, hidden_neurons, {}, 'trainscg');
%net.divideFcn = 'divideind';
% addestra la rete
net.trainParam.showWindow = 0;
net = train(net,P,T);

end
