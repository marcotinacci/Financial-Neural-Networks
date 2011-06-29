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
%     newData1 = importdata(['../index/',financial_index,'.csv']);
%     vars = fieldnames(newData1);
%     for i = 1:length(vars)
%         vars{i}=newData1.(vars{i});
%     end
%     data=vars{1};
%     data=flipud(data);
    result=importdata(['../data/0307',financial_index,'_',strategy, ...
        '_',learning_rule,'.csv']);
    result=result';
    
    n_test = 250;
    real_inc = diff(result(:,1));
    forecast_inc = result(1:size(real_inc),2) - result(1:size(real_inc),1);
    
    figure;
    grid on;
    bar([real_inc forecast_inc ]);
    
    % stampa dei risultati
    figure;
    hold all;
    grid on;
    % attualmente stampa i profitti
    plot([result(1 : n_test, 1); NaN],'g');
    plot([NaN; result(1 : n_test, 2)],'.b');
    legend('Dati reali','Previsioni simulazione');
    figure;
    plot(result(1 : n_test, 3));
    %plot(data(n_learn + n_nan + 1 : n_learn + n_test, 2));
    legend('Dati reali','Previsioni simulazione');
    title([financial_index,' ',strategy,' ',learning_rule]);
end
