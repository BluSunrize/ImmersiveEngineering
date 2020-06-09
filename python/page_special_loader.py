import os, json, math
from os import path

def unpack_special(special):
    if type(special) is list:
        return [unpack_special(unit) for unit in special]
    special_type = special['type']

    if special_type == 'crafting':
        return load_crafting(special)

    return special

def load_crafting(special):
    recipes = []
    if 'recipes' in special:
        for r in special['recipes']:
            get_recipe_names(r, recipes)
    elif 'recipe' in special:
        get_recipe_names(special['recipe'], recipes)
    print '   ACC: '+str(recipes)
    recipes = [load_recipe(r) for r in recipes]
    print '   ARRAY: '+str(recipes)
    height = [r['height'] for r in recipes if r is not None]
    return {'type': 'crafting', 'recipes': recipes, 'height': max(height) if height else 1}

def get_recipe_names(obj, acc):
    if type(obj) is dict and 'recipe' in obj:
        acc.append(obj['recipe'])
    elif type(obj) is list:
        for r in obj:
            get_recipe_names(r, acc)
    elif isinstance(obj, basestring):
        acc.append(obj)

def load_recipe(key):
    key = path.join(*path.split(key)) # making paths with / windows-compatible
    target_path = path.join('raw', 'recipes', key+'.json')
    print '   -using path '+target_path
    if not path.isfile(target_path):
        print 'RECIPE NOT FOUND '+target_path
        return None
    recipe = {
        'type': 'crafting',
        'width': 1,
        'height': 1,
        'output': None,
        'input': [],
    }
    with open(target_path) as recipe_file:
        recipe_json = json.load(recipe_file)

    if 'pattern' in recipe_json: #shaped
        recipe['output'] = recipe_json['result']
        recipe['height'] = len(recipe_json['pattern'])
        ingredients = recipe_json['key']
        for row in recipe_json['pattern']:
            recipe['width'] = len(row)
            for c in row:
                if c in ingredients:
                    recipe['input'].append(ingredients[c])
                else:
                    recipe['input'].append(None)
    else:
        recipe['output'] = recipe_json['result']
        for ingr in recipe_json['ingredients']:
            recipe['input'].append(ingr)
    print '   LOADED: '+str(recipe)
    return recipe


def get_height_crafting(element):
    print 'get height for crafting:'
    print element
    return 1 + 2*element['height']

def get_height_image(element):
    total_height = 0
    for img in element['images']:
        total_height += img['vSize']
    return math.ceil(0.12 * total_height)

def get_height_items(element):
    count = len(element['items']) if 'items' in element else 1
    scale = 1.0 if count > 7 else 1.5 if count > 4 else 1.75
    longLineLen = math.floor(8/scale)
    shortLineLen = longLineLen - 1
    combinedLen = longLineLen + shortLineLen
    lines = (count/combinedLen*2)+ \
            (count%combinedLen/longLineLen)+ \
            (1 if count%combinedLen%longLineLen > 0 else 0)
    return 2 * lines * scale

def get_height_table(element):
    # TODO
    return 0

def get_height_blueprint(element):
    # TODO
    return 0

def get_height_multiblock(element):
    return 10

'''

special_types = [
    'crafting': element =>
    'image'
    'item_display'
    'table'
    'blueprint'
    'multiblock'
]

max_width = 31
max_height = 16

def split_pages(text, specials):
    word_list = tokenize(text)
    pages = []
    page_text = ''
    anchors = ['start']
    for word in word_list:
        end_page = False
        anchor = None
        if word == '<np>': #page break
            end_page = True
        elif word.startswith('<&'): #anchor
            #print 'got anchor {}'.format(word)
            if len(page_text):
                end_page = True
            anchor = word[2:-1]
        elif len(word):
            temp_text = page_text+' '+word
            if len(temp_text) > max_width*4:
                wrapped_lines = textwrap.wrap(temp_text, width=max_width, drop_whitespace=False)
                if len(wrapped_lines) > max_height:
                    print wrapped_lines
                    end_page = True
            if not end_page:
                page_text = temp_text

        if end_page:
            page_text = page_text.strip()
            if len(anchors) or len(page_text):
                pages.append({
                    'anchors': anchors,
                    'text': page_text,
                })
            page_text = ''
            anchors = []
        if anchor:
            anchors.append(anchor)
    # clean up remaining
    if len(anchors) or len(page_text):
        pages.append({
            'anchors': anchors,
            'text': page_text,
        })

    #print pages
    #print '\n\n'
    return pages

    '''