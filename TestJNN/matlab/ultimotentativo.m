ftsts=ascii2fts('../index/BoA.dat',0,1,0);
    rsits=rsindex(ftsts,14);
    ema4ts = tsmovavg(ftsts, 'e', 4);
    ema9ts = tsmovavg(ftsts, 'e', 9);
    macdts= macd(ftsts);
    onbalts=onbalvol(ftsts);
    stoscts = stochosc(ftsts);
    
    
close = fts2mat(ftsts.Close);
volume=fts2mat(ftsts.Volume);
rsi = fts2mat(rsits);
ema4 = fts2mat(ema4ts.Close);
ema9 = fts2mat(ema9ts.Close);
emadiff=ema4-ema9;
macdline = fts2mat(macdts.MACDLine);
nineperma = fts2mat(macdts.NinePerMA);
macddiff=macdline-nineperma;
onbal=fts2mat(onbalts.OnBalVol);
sok=fts2mat(stoscts.SOK);
sod=fts2mat(stoscts.SOD);

%% parametri

test_num = 1000;

%% struttura rete

hidden_neurons = 30;
%% strutture dati

begin_idx = 2026;
% size(P) = RxQ1 -> (input_neurons x test_num)
for i=1:test_num
    P(:,i) = [close(begin_idx+i); volume(begin_idx+i);...
        rsi(begin_idx+i); emadiff(begin_idx+i); ...
        macddiff(begin_idx+i); onbal(begin_idx+i); sok(begin_idx+i); ...
        sod(begin_idx+i)];
end


% size(T) = SNxQ2 -> (output_neurons x test_num)
for i=1:test_num
    T(:,i) = [sign(close(begin_idx+i+1)-close(begin_idx+i)); ...
        sign(close(begin_idx+i+2)-close(begin_idx+i)); ...
        sign(close(begin_idx+i+3)-close(begin_idx+i)); ...
        sign(close(begin_idx+i+4)-close(begin_idx+i)); ...
        sign(close(begin_idx+i+5)-close(begin_idx+i))];
end

%% addestramento

% crea rete
net = newff(P, T, hidden_neurons, {}, 'trainscg');

net.trainParam.max_fail = 50;
net.trainParam.epochs = 1000;
net.trainParam.goal = 1e-5;
%net.divideFcn = 'divideind';
% addestra la rete
net = train(net,P,T);

output_nel = 200;
begin_idx = 3026;
sim_out = zeros(5,output_nel);
for i=1:output_nel
    input = [close(begin_idx+i); volume(begin_idx+i);...
        rsi(begin_idx+i); emadiff(begin_idx+i); ...
        macddiff(begin_idx+i); onbal(begin_idx+i); sok(begin_idx+i); ...
        sod(begin_idx+i)];
    sim_out(:,i) = sim(net,input);
end
count=0;
for i=1:output_nel
    if (sign(sim_out(1,i))==sign(close(3028+i) - close(3027+i)))
        count=count+1;
    end
end
count
bar([sim_out(1,:)'.*( sign(close(3029:3228) - close(3028:3227)))]);