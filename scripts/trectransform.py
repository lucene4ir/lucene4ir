"""
Transform janky trec topics into lucene4ir topics.
"""

import argparse

import sys


def transform(data: str) -> str:
    """

    :param data:
    :return:
    """
    topics = ''
    num = None
    for n, line in enumerate(data.split('\n')):
        if '<num>' in line:
            num = line.split()[-1]
        elif '<title>' in line and num is not None:
            title = line.replace('<title>', '').strip()
            topics += '{} {}\n'.format(num, title)
            num = None
        elif '<title>' in line and num is None:
            print('<title> tag appeared before a <num> tag on line {}'.format(n))
            sys.exit(1)
    return topics


if __name__ == '__main__':
    argparser = argparse.ArgumentParser(description='Transform TREC topics->lucene4ir topics.')

    argparser.add_argument('-i', '--input', help='Input file', type=argparse.FileType('r'),
                           default=sys.stdin)
    argparser.add_argument('-o', '--output', help='Output file', type=argparse.FileType('w'),
                           default=sys.stdout)

    args = argparser.parse_args()

    args.output.write(transform(args.input.read()))
