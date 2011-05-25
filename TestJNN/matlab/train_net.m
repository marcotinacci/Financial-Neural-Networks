function net = train_net( closes )

%% parametri

test_num = 100;

%% struttura rete

input_neurons = 6;
hidden_neurons = 10;
output_neurons = 1;

%% strutture dati

% size(P) = RxQ1 -> (input_neurons x test_num)
for i=1:test_num-input_neurons
    P = [P closes( i : i+input_neurons-1 )];
end

%% addestramento

% crea rete
net = newff(P, T, hidden_neurons, {}, 'traincgp');
% addestra la rete
net = train(net,P,T);

end

