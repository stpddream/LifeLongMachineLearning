addpath('lib/ImageProfClientLibMatlabHelper');
addpath('lib/ELLAv1.0');

connection = ImageConnection();
connection.connect('127.0.0.1', 8887);
data = connection.landQuery();
%celldisp(data);
%feature = cellfunc(str2double, data{1});
feature = cell(size(data{1}), size(data{1}{1}), size(data{1}{1}{1}));


for i = 1 : size(data{1})
    feature{i} = str2double(data{1}{i});
end

label = data{2};

    % shuffle the tasks
    T = length(feature);
       
    r = randperm(T);

    feature = {feature{r}};
    label = {label{r}};

    for t = 1 : T
	feature{t}(:,end+1) = 1;
    end
    d = size(feature{1},2);

    X = cell(T,1);
    Xtest = cell(T,1);
    Y = cell(T,1);
    Ytest = cell(T,1);
    for t = 1 : T
	r = randperm(size(feature{t},1));
	traininds = r(1:floor(length(r)/2));
	testinds = r(floor(length(r)/2)+1:end);
	X{t} = feature{t}(traininds,:);
	Xtest{t} = feature{t}(testinds,:);
	Y{t} = label{t}(traininds);
	Ytest{t} = label{t}(testinds);
    end

    model = initModelELLA(struct('k',2,... %latent dimensionality: number of different knowledge components. 10. complexity of hypothesis space. log grow on demand.
	'd',d,...  %size of the feature space input representation
	'mu',exp(-12),...  regularization coefficient
	'muRatio',Inf,...
	'lambda',exp(-10),... %regularization
	'ridgeTerm',exp(-5),... %for logistic
	'initializeWithFirstKTasks',true,...
	'useLogistic',useLogistic,...
	'lastFeatureIsABiasTerm',true));  %Make the last feature always one
    for t = 1 : T
	model = addTaskELLA(model,X{t},Y{t},t);
    end
    perf = zeros(T,1);
    for t = 1 : T
	preds = predictELLA(model,Xtest{t},t);
	perf(t) = roc(preds,Ytest{t});
    end
    y = mean(perf);