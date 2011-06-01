count=0

for i=1:200
    count=count+forecast_plot();
end
precision=count/(200*200)