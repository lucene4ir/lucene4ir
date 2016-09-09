import sys
# If a new model has been created, add to this list.
models=['BM25','LMD','LMJ','PL2','TF.IDF']

beta = 500;
b = 0.75;
lam = 0.5; 
c = 10.0;
k = 1.2;
mu=500

# Add any new parameters to this list and provide a default value above.
params=['b','k','beta','mu','lam','c']

# Also add the new models and the correspomding parameters to this dictionary.
model_params={'BM25':'b','LMD':'mu','LMJ':'lam','PL2':'c'}

if len(sys.argv) < 4:
    print "Not enough arguements provided. <optional-params>"
    print "python lucene_model_trec_script_generator.py path_to_lucene query_file collection <model> <param_setting>"
    sys.exit(1)

path_to_lucene=sys.argv[1]
query_file=sys.argv[2]
collection=sys.argv[3]
model='BM25'
param_setting=b
param = 'b'

if len(sys.argv)==5:
    model=sys.argv[4]
    param = model_params[model]
elif len(sys.argv)==6:
    model=sys.argv[4]
    param_setting=float(sys.argv[5])

if model not in models:
    print "Model not recognised."
    models_string=""
    for i in models:
        models_string = models_string + " " + i
    print "Available models are " + models_string
    sys.exit(1)

print 'Writing param file to %s/params/%s.%s-%.2f.params' % (path_to_lucene,collection,model,param_setting)

filename = '%s/params/%s.%s-%.2f.params' % (path_to_lucene,collection,model,param_setting)
file = open(filename, 'w')
file.write('<?xml version="1.0" encoding="UTF-8" standalone="yes"?> \n\
<retrievalParams> \n\
<indexName>%s/index</indexName> \n\
<queryFile>%s/data/%s/%s</queryFile> \n\
<maxResults>1000</maxResults> \n\
<model>%s</model> \n\
<%s>%.2f</%s> \n\
<resultFile>%s/data/%s/%s.%s-%.2f.res</resultFile> \n\
</retrievalParams>'% (path_to_lucene, path_to_lucene, collection, query_file, model, param, param_setting, param,path_to_lucene, collection, collection, model, param_setting))
file.close()