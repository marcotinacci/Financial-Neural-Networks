vet = randperm(973);
vet = vet + 27;
pippo = vet(1:773);
test = vet(774:973);
t_count=0
t_sim = zeros(1,200);
for i=1:100
    i
    [count sim_out fts] = forecast_plot(pippo, test);
    t_count = t_count + count;
    t_sim = t_sim + sim_out;
end
t_count/100
ordered = sort(test);
%% stampa i risultati
t_sim = t_sim./100;

figure;
hold all;
grid on;
plot([1:1000],fts(6:1005)','g');
plot(ordered(1:191), t_sim(1:191)','.b');
legend('previsioni','dati reali');

% ----
%ordered = sort(test);
for i=1:200
    temp(i) = (sim_out(i) - fts(ordered(i)+1))^2;
end
somma = sum(temp(1:191));
mse = somma / 191;
mse = sqrt(mse)
% ----