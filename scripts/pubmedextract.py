"""
Extract a set of doc ids from the pubmed xml files.
"""
import argparse
import glob
import gzip
import multiprocessing
import os
from functools import partial
from multiprocessing import Pool

import sys
from lxml import etree


def parse_pubmeds(pmids: list, file: str) -> str:
    """

    :param pmids:
    :param file:
    :return:
    """
    data = """<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE PubmedArticleSet SYSTEM "http://dtd.nlm.nih.gov/ncbi/pubmed/out/pubmed_170101.dtd">
<PubmedArticleSet>  
{}
</PubmedArticleSet> 
"""
    print(file)
    decompressed_file = gzip.GzipFile(file, mode='rb')
    tree = etree.parse(decompressed_file)
    root = tree.getroot()
    for node in root.findall('PubmedArticle'):
        pmid = node.find('MedlineCitation').find('PMID').text
        if pmid in pmids:
            print(pmid)
            file_data = data.format(
                etree.tostring(node, encoding='unicode', method='xml', pretty_print=True))
            with open('/datadrive2/pubmed_filter/{}.xml'.format(pmid), 'w') as f:
                f.write(file_data)


if __name__ == '__main__':
    argparser = argparse.ArgumentParser()

    argparser.add_argument('--pmids', help='Location of pmids file.',
                           type=argparse.FileType('r'), default=sys.stdin)
    argparser.add_argument('--pubmed', help='Location of pubmed gzip files.',
                           type=str, required=True)

    args = argparser.parse_args()

    parse_partial = partial(parse_pubmeds, [x.strip() for x in args.pmids.readlines()])
    print(list(glob.glob(os.path.join(args.pubmed, '*.xml.gz'))))
    p = Pool(multiprocessing.cpu_count() - 1 or 1)
    p.map(parse_partial, list(glob.glob(os.path.join(args.pubmed, '*.xml.gz'))))
    p.close()
    p.join()
