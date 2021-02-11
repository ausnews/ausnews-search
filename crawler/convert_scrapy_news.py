#! /usr/bin/env python3

import os
import io
import sys
import csv
import json
import hashlib

in_file = sys.argv[1]
out_file = os.path.join(".", in_file + "_vespa.json")

def main():
    with io.open(in_file, "r", encoding="utf-8") as f, open(out_file, "w") as out:
        out.write("[\n")
        line = f.readline()
        while line:
            raw_json = json.loads(line)
            hash = hashlib.sha256(raw_json['url'].encode()).hexdigest()
            id = hash
            del raw_json['fetchtime']
            del raw_json['language']
            if ('summary' in raw_json):
                raw_json['abstract'] = raw_json['summary']
                del raw_json['summary']
            obj = { 'put': 'id:news:newsarticle::' + hash, 'fields': raw_json }
            if ('bodytext' in raw_json):
                json.dump(obj, out)
            else:
                line = f.readline()
                continue
            line = f.readline()
            if (line and len(line.strip()) > 0):
                out.write(",\n")
        out.write("]\n")
            
if __name__ == "__main__":
    main()
