v = zeros(500,1);
for i=1:500
   v(i) = 1 / (1+exp(6-(12/500)* i));
end
plot(v);
saveas(gcf,'imgs/prova.eps','psc2');