function all_results()
%ALL_RESULTS
%
M = ones(2,3);

M(1,1) = showresults('MIB','std','Backpropagation');
M(2,1) = showresults('MIB','rsi','Backpropagation');
M(1,2) = showresults('MIB','std','TDPBackpropagation');
M(2,2) = showresults('MIB','rsi','TDPBackpropagation');
M(1,3) = showresults('MIB','std','STEBackpropagation');
M(2,3) = showresults('MIB','rsi','STEBackpropagation');

M

M(1,1) = showresults('NASDAQ100','std','Backpropagation');
M(2,1) = showresults('NASDAQ100','rsi','Backpropagation');
M(1,2) = showresults('NASDAQ100','std','TDPBackpropagation');
M(2,2) = showresults('NASDAQ100','rsi','TDPBackpropagation');
M(1,3) = showresults('NASDAQ100','std','STEBackpropagation');
M(2,3) = showresults('NASDAQ100','rsi','STEBackpropagation');

M

M(1,1) = showresults('NIKKEI225','std','Backpropagation');
M(2,1) = showresults('NIKKEI225','rsi','Backpropagation');
M(1,2) = showresults('NIKKEI225','std','TDPBackpropagation');
M(2,2) = showresults('NIKKEI225','rsi','TDPBackpropagation');
M(1,3) = showresults('NIKKEI225','std','STEBackpropagation');
M(2,3) = showresults('NIKKEI225','rsi','STEBackpropagation');

M


M(1,1) = showresults('SP500','std','Backpropagation');
M(2,1) = showresults('SP500','rsi','Backpropagation');
M(1,2) = showresults('SP500','std','TDPBackpropagation');
M(2,2) = showresults('SP500','rsi','TDPBackpropagation');
M(1,3) = showresults('SP500','std','STEBackpropagation');
M(2,3) = showresults('SP500','rsi','STEBackpropagation');


M


end

