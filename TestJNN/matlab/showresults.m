function showresults(financial_index, strategy, learning_rule)
% SHOWRESULTS
%
% Parametri
%
% financial_index: SP500, NASDAQ100, NIKKEI225, MIB
% strategy: std, ema, rsi
% learning_rule: Backpropagation, TDPBackpropagation, 
%   MomentumBackpropagation
%

    % Import the file
    newData1 = importdata(['../data/',financial_index,'.csv']);
    vars = fieldnames(newData1);
    for i = 1:length(vars)
        vars{i}=newData1.(vars{i});
    end
    data=vars{1};
    data=flipud(data);
    result=importdata(['../data/result_',financial_index,'_',strategy, ...
        '_',learning_rule,'.csv']);
    result=result';
    
    n_test = 350;
    n_learn = 1000;
    
    % calcolo numero elementi NaN
    n_nan = 0;
    switch strategy
        case 'std'
            n_nan = 29;
        case 'ema'
            n_nan = 20;
        case 'rsi'
            n_nan = 20;
    end
    
    % stampa dei risultati
    figure;
    hold all;
    plot(result(1 : n_test - n_nan, 3));
    plot(data(n_learn + n_nan + 1 : n_learn + n_test, 4));
    legend('Previsioni simulazione','Dati reali');
    title([financial_index,' ',strategy,' ',learning_rule]);
end
