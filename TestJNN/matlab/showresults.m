function r = showresults(financial_index, strategy, learning_rule)
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
    result=importdata(['../data/',financial_index,'_',strategy, ...
        '_',learning_rule,'.csv']);
    result=result';
    
    n_test = 250;
    real_inc = diff(result(:,1));
    forecast_inc = result(1:size(real_inc),2) - result(1:size(real_inc),1);
    
    % trasforma in trend percentuali
%     for i=1:size(real_inc)
%         real_inc(i) = real_inc(i) / result(i,1);
%         forecast_inc(i) = forecast_inc(i) / result(i,1);
%     end
    
    count = 0;
    r = sign(forecast_inc .* real_inc);
    for i=1:size(real_inc)
        if r(i) == 1
           count = count +1; 
        end
    end
    
    r = [count / size(real_inc,1)]    
    
%     % stampa grafico trend con soglia
%     figure;
%     hold all;
%     g=bar([real_inc forecast_inc ]);
%     set(g,'edgecolor','none');
%     %plot(1:size(real_inc),0.01*ones(size(real_inc)),'--k');
%     %plot(1:size(real_inc),-0.01*ones(size(real_inc)),'--k');
%     legend('Trend reale', 'Trend previsto');
%     saveas(gcf,['imgs/',financial_index,'_',strategy,'_',learning_rule,'_trend.eps']);
%     %title([financial_index,' ',strategy,' ',learning_rule, ...
%      %   ' - Grafico previsioni trend']);
%     
    
     
     
    % stampa dati reali e previsioni
     figure;
     hold all;
     grid on;
     plot(1:n_test+1,[result(1 : n_test, 1); NaN],'-g');
     plot(1:n_test+1,[NaN; result(1 : n_test, 2)],'.b');
     legend('Dati reali','Previsioni simulazione');
     saveas(gcf,['imgs/',financial_index,'_',strategy,'_',learning_rule,'_forecast.eps'],'psc2');
     %title([financial_index,' ',strategy,' ',learning_rule, ...
     %   ' - Grafico previsioni puntuali']);
%     
%     % scarto quadratico medio
%     % scarto con i dati reali o con la media?

     

     mse = 0;
     for i=2:n_test-1
         mse = mse + ((result(i, 2) - result(i, 1)))^2;
         %mse = mse + ((result(i, 2) - result(i, 1))/ result(i,1))^2;
     end
     r = sqrt(mse / (n_test-2));
     
    
    % attualmente stampa i profitti
    %figure;
    %plot(result(1 : n_test, 3));
    %plot(data(n_learn + n_nan + 1 : n_learn + n_test, 2));
    %legend('Dati reali','Previsioni simulazione');
    %title([financial_index,' ',strategy,' ',learning_rule]);
end
