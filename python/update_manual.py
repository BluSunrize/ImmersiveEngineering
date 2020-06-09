import argparse, os, json
from os import path
import page_special_loader, string_splitter

parser = argparse.ArgumentParser()
parser.add_argument('--no-pull', action='store_true', help='don\'t execute a fresh git pull')

args = parser.parse_args()

if not args.no_pull:
    print 'prep for git'
    script = path.join('python', 'get_repo_contents.sh')
    rc = os.system(script)
    print 'finished git'


entries = {}
target_path = path.join('raw', 'manual')
files = [f for f in os.listdir(target_path) if path.isfile(path.join(target_path, f))]
for handle in files:
    if len(handle) and '.json' in handle:
        dot_idx = handle.index('.')
        key = handle[0:dot_idx]
        f_txt = key+'.txt'
        text = ''
        with open(path.join(target_path, 'en_us', f_txt)) as file_content:
            title = file_content.readline().replace('\n', '')
            subtitle = file_content.readline().replace('\n', '')
            for line in file_content.readlines():
                text += line
        with open(path.join(target_path, handle)) as json_file:
            specials = json.load(json_file)
            print 'load specials for '+key
            specials = {key:page_special_loader.unpack_special(specials[key]) for key in specials}

        pages = string_splitter.split_pages(text, specials)
        anchor_map = {}
        for i, page in enumerate(pages):
            for anchor in page['anchors']:
                anchor_map[anchor] = i

        # for entry list
        entries[key] = {
            'title': title,
            'url': 'data/{}.json'.format(key),
            'anchors': anchor_map,
        }

        # for entry file
        entry = {
            'title': title,
            'subtitle': subtitle,
            'pages': pages,
            'specials': specials
        }

        file_path = path.join('manual', 'data', '{}.json'.format(key))
        with open(file_path, 'w') as outfile:
            json.dump(entry, outfile, indent=2)
'''
        pages.append({
            'json': f_json,
            'txt': 'en_us/'+f_txt,
        })
        '''
#print entries

with open('manual/entry_list.json', 'w') as outfile:
    json.dump(entries, outfile, indent=2)
