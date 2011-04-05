function showresults(financial_index)

    % Import the file
    newData1 = importdata(['../data/',financial_index,'.csv']);
    vars = fieldnames(newData1);
    for i = 1:length(vars)
        vars{i}=newData1.(vars{i});
    end
    data=vars{1};
    data=flipud(data);
    result=importdata(['../data/result_',financial_index,'.csv']);
    result=result';
    newplot;
    plot(result(:,3));
    hold all;
    plot(data(1531:1791,4));
    legend('Risultati Simulazione','Dati Reali');
    title('Andamento delle azioni');
end
