import sys
# If a new model has been created, add to this list.
models=['BM25','LMD','LMJ','PL2','TF.IDF']

beta = 500;
b = 0.75;
lam = 0.5; 
c = 10.0;
k = 1.2;
mu=500
rb=0.5

# Add any new parameters to this list and provide a default value above.
params=['b','k','beta','mu','lam','c']

# Also add the new models and the correspomding parameters to this dictionary.
model_params={'BM25':'b','LMD':'mu','LMJ':'lam','PL2':'c'}

if len(sys.argv) < 8:
    print "Not enough arguements provided."
    print "python lucene_model_bigram_script_generator.py path_to_lucene query_file collection model param_setting title_boost content_boost"
    sys.exit(1)

path_to_lucene=sys.argv[1]
query_file=sys.argv[2]
collection=sys.argv[3]
model='BM25'
param_setting=b
param = 'b'
model=sys.argv[4]
param = model_params[model]
param_setting=float(sys.argv[5])
title_boost=float(sys.argv[6])
content_boost=float(sys.argv[7])


if model not in models:
    print "Model not recognised."
    models_string=""
    for i in models:
        models_string = models_string + " " + i
    print "Available models are " + models_string
    sys.exit(1)

fmodel=model+"F"

print '%s/params/%s/bigram_files/%s/%s-%.2f-%s-%s.params' % (path_to_lucene,collection,fmodel,fmodel,param_setting,title_boost,content_boost)

filename = '%s/params/%s/bigram_files/%s/%s-%.2f-%s-%s.params' % (path_to_lucene,collection,fmodel,fmodel,param_setting,title_boost,content_boost)
file = open(filename, 'w')
file.write('<?xml version="1.0" encoding="UTF-8" standalone="yes"?> \n\
<retrievalParams> \n\
<indexName>%s/%sIndex</indexName> \n\
<queryFile>%s/data/%s/%s</queryFile> \n\
<maxResults>100</maxResults> \n\
<model>%s</model> \n\
<%s>%.2f</%s> \n\
<resultFile>%s/data/%s/bigram_files/%s/%s-%.2f-%s-%s.res</resultFile> \n\
<tokenFilterFile>%s/params/news_token_filters.xml</tokenFilterFile> \n\
<fieldsFile>%s/params/%s/bigram_files/%s-%.2f-%s-%s.fieldparams</fieldsFile> \n\
</retrievalParams>'% (path_to_lucene, collection, path_to_lucene, collection, query_file, model, param, param_setting, param,path_to_lucene, collection, fmodel, fmodel, param_setting,title_boost,content_boost,path_to_lucene,path_to_lucene,collection,fmodel,param_setting,title_boost,content_boost))
file.close()


filename = '%s/params/%s/bigram_files/%s/%s-%.2f-%s-%s.fieldparams' % (path_to_lucene,collection,fmodel,fmodel,param_setting,title_boost,content_boost)
file = open(filename, 'w')
file.write('<?xml version="1.0" encoding="UTF-8" standalone="yes"?> \n\
<fields> \n\
<field> \n\
<fieldName>title</fieldName> \n\
<fieldBoost>%.1f</fieldBoost> \n\
</field> \n\
<field> \n\
<fieldName>content</fieldName> \n\
<fieldBoost>%.1f</fieldBoost> \n\
</field> \n\
<resultFile>%s/data/%s/bigram_files/%s/%s-%.2f-%s-%s.res</resultFile> \n\
</fields>'% (title_boost,content_boost,path_to_lucene,collection,fmodel,fmodel,param_setting,title_boost,content_boost))
file.close()
